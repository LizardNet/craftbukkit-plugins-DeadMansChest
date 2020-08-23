/*
 * DEADMANSCHEST2
 * by Andrew "FastLizard4" Adams, TLUL, and the LizardNet CraftBukkit Plugins
 * Development Team (see AUTHORS.txt file)
 *
 * BASED UPON:
 * DeadMansChest by Tux2, <https://github.com/Tux2/PlayerChestDeath>, GPL v3
 * (which was in turn based upon:)
 * PlayerChestDeath by Wesnc, <https://github.com/Wesnc/PlayerChestDeath>
 *
 * Copyright (C) 2013-2020 by Andrew "FastLizard4" Adams, TLUL, and the LizardNet
 * CraftBukkit Plugins Development Team. Some rights reserved.
 *
 * License GPLv3+: GNU General Public License version 3 or later (at your
 * choice): <http://gnu.org/licenses/gpl.html>. This is free software: you are
 * free to change and redistribute it at your will provided that your
 * redistribution, with or without modifications, is also licensed under the GNU
 * GPL. (Although not required by the license, we also ask that you attribute
 * us!) There is NO WARRANTY FOR THIS SOFTWARE to the extent permitted by law.
 *
 * This is an open source project. The source Git repositories, which you are
 * welcome to contribute to, can be found here:
 * <https://gerrit.fastlizard4.org/r/gitweb?p=craftbukkit-plugins/DeadMansChest.git;a=summary>
 * <https://git.fastlizard4.org/gitblit/summary/?r=craftbukkit-plugins/DeadMansChest.git>
 *
 * Gerrit Code Review for the project:
 * <https://gerrit.fastlizard4.org/r/#/q/project:craftbukkit-plugins/DeadMansChest,n,z>
 *
 * Continuous Integration for this project:
 * <https://integration.fastlizard4.org:444/jenkins/job/craftbukkit-plugins-DeadMansChest/>
 *
 * Alternatively, the project source code can be found on the PUBLISH-ONLY
 * mirror on GitHub:
 * <https://github.com/LizardNet/craftbukkit-plugins-DeadMansChest>
 *
 * Note: Pull requests and patches submitted to GitHub will be transferred by a
 * developer to Gerrit before they are acted upon.
 */

package org.fastlizard4.git.craftbukkit_plugins.DeadMansChest2;

import com.griefcraft.lwc.LWC;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class EntLis implements Listener {
  private static final Logger log = Logger.getLogger("Minecraft");

  private final Server server;
  private final Config config;
  private final Persistence persistence;
  @Nullable private final LWC lwc;
  private final Scheduler scheduler;

  public EntLis(
      Server server,
      Config config,
      Persistence persistence,
      @Nullable LWC lwc,
      Scheduler scheduler) {
    this.server = server;
    this.config = config;
    this.persistence = persistence;
    this.lwc = lwc;
    this.scheduler = scheduler;
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onEntityDeath(EntityDeathEvent event) throws Exception {
    Entity entity = event.getEntity();
    if (!(entity instanceof Player)) {
      return;
    }

    Player player = (Player) entity;
    if (!player.hasPermission("DeadMansChest2.chest")) {
      return;
    }

    List<ItemStack> drops = event.getDrops();
    if (isEmpty(drops)) {
      return;
    }

    int chestsToDeploy;
    if (config.isNeedChestInInventory() && !player.hasPermission("DeadMansChest2.freechest")) {
      int chestCount = countChests(drops);
      if (chestCount == 0) {
        return;
      }
      chestsToDeploy = 1;
      int tentativeItemCount = countStacks(drops, 1);
      if (chestCount > 1
          && tentativeItemCount > Constants.MAX_ITEMS_PER_CHEST
          && player.hasPermission("DeadMansChest2.doublechest")) {
        chestsToDeploy = 2;
      }
      removeChests(drops, chestsToDeploy);
    } else {
      if (countStacks(drops, 0) > Constants.MAX_ITEMS_PER_CHEST
          && player.hasPermission("DeadMansChest2.doublechest")) {
        chestsToDeploy = 2;
      } else {
        chestsToDeploy = 1;
      }
    }

    try {
      CreateChest creator = new CreateChest(config, lwc, persistence, scheduler, player, drops,
          chestsToDeploy);

      if (!config.isDropsEnabled() && !player.hasPermission("DeadMansChest2.drops")) {
        drops.clear();
      }

      if (config.isDeathMessage() && player.hasPermission("DeadMansChest2.message")) {
        Block block = player.getLocation().getBlock();
        String positionString = block.getX() + ", " + block.getY() + ", " + block.getZ();
        String deathMessageString =
            config
                .getDeathMessageString()
                .replaceAll("\\{position}", positionString)
                .replaceAll("\\{player}", player.getDisplayName());
        for (ChatColor c : ChatColor.values()) {
          if (c.isColor()) {
            deathMessageString = deathMessageString
                .replaceAll("\\{" + c.name() + "}", c.toString());
          }
        }
        this.server.broadcastMessage(deathMessageString);
      }

      scheduler.schedule(creator, 1);
    } catch (Exception e) {
      this.server.broadcastMessage(ChatColor.BOLD + "" + ChatColor.YELLOW +
          "Failed to deploy death chest for " + player.getDisplayName() + "!");
      throw e;
    }
  }

  private boolean isEmpty(List<ItemStack> items) {
    for (ItemStack itemStack : items) {
      if (itemStack != null && itemStack.getType() != Material.AIR) {
        return false;
      }
    }
    return true;
  }

  private int countChests(List<ItemStack> items) {
    int count = 0;
    for (ItemStack itemStack : items) {
      if (itemStack != null && itemStack.getType() == Material.CHEST) {
        count += itemStack.getAmount();
      }
    }
    return count;
  }

  private int countStacks(List<ItemStack> items, int skipChests) {
    int count = 0;
    for (ItemStack itemStack : items) {
      if (itemStack == null) {
        continue;
      }
      if (itemStack.getType() == Material.CHEST) {
        skipChests -= itemStack.getAmount();
        if (skipChests >= 0) {
          continue;
        }
      }
      count++;
    }
    return count;
  }

  private void removeChests(List<ItemStack> items, int count) {
    Iterator<ItemStack> iter = items.iterator();
    while (iter.hasNext() && count > 0) {
      ItemStack itemStack = iter.next();
      if (itemStack == null || itemStack.getType() != Material.CHEST) {
        continue;
      }
      int newAmount = itemStack.getAmount() - count;
      if (newAmount > 0) {
        itemStack.setAmount(newAmount);
      } else {
        count = -newAmount;
        iter.remove();
      }
    }
  }
}
