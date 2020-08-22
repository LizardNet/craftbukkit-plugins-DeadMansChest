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
import javax.annotation.Nullable;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class CreateChest implements Runnable {
  private final Config config;
  @Nullable private final LWC lwc;
  private final Scheduler scheduler;

  private final Block chestblock;
  private final Player player;
  private final DeathChest deathChest;

  public CreateChest(
      Config config,
      @Nullable LWC lwc,
      Scheduler scheduler,
      Block chestblock,
      Player player,
      DeathChest deathChest) {
    this.config = config;
    this.lwc = lwc;
    this.scheduler = scheduler;
    this.chestblock = chestblock;
    this.player = player;
    this.deathChest = deathChest;
  }

  @Override
  public void run() {
    if (lwc != null && player.hasPermission("DeadMansChest2.lock")) {
      deathChest.lock(lwc, player.getName());
    }

    if (this.config.isSignOnChest()) {
      boolean foundair = false;
      BlockFace[] directions = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
      int signdirection = 1;
      for (int i = 0; i < directions.length && !foundair; i++) {
        if (config.isBeaconReplacesLiquid()) {
          // If we can replace water, let's do it with the sign too!
          Block tempblock = chestblock.getRelative(directions[i]);
          if (tempblock.getType() == Material.AIR
              || tempblock.getType() == Material.WATER
              || tempblock.getType() == Material.STATIONARY_WATER
              || tempblock.getType() == Material.LAVA
              || tempblock.getType() == Material.STATIONARY_LAVA) {
            signdirection = i;
            foundair = true;
          }
        } else {
          if (chestblock.getRelative(directions[i]).getType() == Material.AIR) {
            signdirection = i;
            foundair = true;
          }
        }
      }

      if (foundair) {
        // -----------------------------------------------------------
        Block signBlock = chestblock.getRelative(directions[signdirection]);
        deathChest.setBlock(signBlock, Material.WALL_SIGN);
        Sign sign = (Sign) signBlock.getState();
        org.bukkit.material.Sign matSign = new org.bukkit.material.Sign(Material.WALL_SIGN);
        matSign.setFacingDirection(directions[signdirection]);
        sign.setData(matSign);
        // -----------------------------------------------------------

        sign.setLine(0, player.getDisplayName() + "'s");
        sign.setLine(1, "Deathpile");
        sign.update();
      } else {
        // If we didn't find a free spot, let's put the sign above the chest...
        // Will probably look very ugly though and pop off anyways...
        // -----------------------------------------------------------
        Block signBlock = chestblock.getRelative(BlockFace.UP);
        // Let's make sure we aren't overwriting a block here
        if (Constants.AIR_BLOCKS.contains(signBlock.getType())) {
          deathChest.setBlock(signBlock, Material.SIGN_POST);
          Sign sign = (Sign) signBlock.getState();
          // -----------------------------------------------------------

          sign.setLine(0, player.getDisplayName() + "'s");
          sign.setLine(1, "Deathpile");
          sign.update();
          // plugin.signblocks.put(chestblock, signBlock);
        }
      }
    }

    if (config.isBeaconEnabled() && player.hasPermission("DeadMansChest2.beacon")) {
      int height = config.getBeaconHeight();
      Location chestLocation1 = chestblock.getLocation();

      Location firstlocation = chestLocation1.add(0.0, 2.0, 0.0);
      Block nextblock = firstlocation.getBlock();

      for (int i = 0; i < height; i++) {
        if (config.isBeaconReplacesLiquid()) {
          if (nextblock.getType() == Material.AIR
              || nextblock.getType() == Material.WATER
              || nextblock.getType() == Material.STATIONARY_WATER
              || nextblock.getType() == Material.LAVA
              || nextblock.getType() == Material.STATIONARY_LAVA) {
            deathChest.setBlock(nextblock, Material.GLOWSTONE);
          }
        } else {
          if (nextblock.getType() == Material.AIR) {
            deathChest.setBlock(nextblock, Material.GLOWSTONE);
          }
        }
        nextblock = nextblock.getRelative(BlockFace.UP);
      }
    }

    if (config.isChestDeleteIntervalEnabled() && !player.hasPermission("DeadMansChest2.nodelete")) {
      int delay = config.getChestDeleteInterval() * 20;
      deathChest.scheduleRemoval(scheduler, delay);
    }
  }
}
