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
import com.griefcraft.model.Protection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class DeathChest {
  @Nullable private final LWC lwc;
  private final Persistence persistence;
  private final List<Block> chestBlocks = new ArrayList<>();
  private final Map<Block, Material> replacedBlocks = new HashMap<>();
  @Nullable private Cancellable removalTask;

  /**
   * Creates a new instance that, when removed, will unregister itself with the given Persistence
   * instance. The new chest blocks will be created and filled synchronously in the constructor, and
   * the list of items will be mutated to remove items that have been placed in the chests.
   */
  public DeathChest(
      LWC lwc,
      Persistence persistence,
      Collection<Block> chestBlocks,
      List<ItemStack> items) {
    this.lwc = lwc;
    this.persistence = persistence;
    this.chestBlocks.addAll(chestBlocks);

    for (Block chestBlock : chestBlocks) {
      setBlock(chestBlock, Material.CHEST);
    }

    Iterator<ItemStack> iter = items.iterator();
    for (Inventory inventory : getInventories()) {
      int remaining = Constants.MAX_ITEMS_PER_CHEST;
      while (iter.hasNext() && remaining > 0) {
        ItemStack itemStack = iter.next();
        if (itemStack == null || itemStack.getType() == Material.AIR) {
          continue;
        }
        inventory.addItem(itemStack);
        iter.remove();
        remaining--;
      }
    }
  }

  public Set<Block> getBlocks() {
    return Collections.unmodifiableSet(replacedBlocks.keySet());
  }

  public boolean containsBlock(Block b) {
    return replacedBlocks.containsKey(b);
  }

  public List<Block> getLootableBlocks() {
    return Collections.unmodifiableList(chestBlocks);
  }

  public boolean containsLootableBlock(Block b) {
    return chestBlocks.contains(b);
  }

  public void setBlock(Block block, Material material) {
    if (!replacedBlocks.containsKey(block)) {
      replacedBlocks.put(block, block.getType());
    }
    block.setType(material);
  }

  public void removeBlock(Block block) {
    if (chestBlocks.contains(block) && lwc != null) {
      Protection protection = lwc.findProtection(block);
      if (protection != null) {
        protection.remove();
      }
    }
    if (replacedBlocks.containsKey(block)) {
      block.setType(replacedBlocks.get(block));
      replacedBlocks.remove(block);
    }
    chestBlocks.remove(block);
    if (chestBlocks.isEmpty()) {
      removeAll();
    }
  }

  public void removeAll() {
    if (removalTask != null) {
      removalTask.cancel();
    }
    persistence.unregisterDeathChest(this);
    for (Map.Entry<Block, Material> entry : replacedBlocks.entrySet()) {
      entry.getKey().setType(entry.getValue());
    }
    chestBlocks.clear();
    replacedBlocks.clear();
  }

  public void scheduleRemoval(Scheduler scheduler, long delay) {
    removalTask = scheduler.schedule(this::removeAll, delay);
  }

  public void loot(PlayerInventory target) {
    for (Inventory inventory : getInventories()) {
      for (ItemStack stack : inventory.getContents()) {
        if (target.firstEmpty() == -1) {
          return;
        }
        if (stack != null) {
          target.addItem(stack);
          inventory.removeItem(stack);
        }
      }
    }
    // we got to the end without returning, so we've fully looted the chest
    removeAll();
  }

  private List<Inventory> getInventories() {
    return chestBlocks.stream()
        .map(DeathChest::getChestInventory)
        .collect(Collectors.toList());
  }

  private static Inventory getChestInventory(Block chestBlock) {
    return ((Chest) chestBlock.getState()).getInventory();
  }
}
