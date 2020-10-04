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

import com.google.common.collect.ImmutableList;
import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@ParametersAreNonnullByDefault
public class CreateChest implements Runnable {
  private static final int MAX_OFFSET = 8;
  private static final BlockFace[] HORIZONTALLY_ADJACENT = {
      BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH
  };

  private final Config config;
  @Nullable private final LWC lwc;
  private final Persistence persistence;
  private final Scheduler scheduler;
  private final Player player;

  /**
   * List of blocks to be replaced with chests.
   */
  private final List<Block> chestBlocks;
  /**
   * DeathChest persistence instance.
   */
  private final DeathChest deathChest;

  /**
   * Block to be replaced with a sign. Null if no sign is to be placed.
   */
  private Block signBlock;
  /**
   * Direction the death chest sign will be facing. Null if the sign to be placed is a signpost.
   */
  @Nullable private BlockFace signFacing;

  public CreateChest(
      Config config,
      @Nullable LWC lwc,
      Persistence persistence,
      Scheduler scheduler,
      Player player,
      List<ItemStack> drops,
      int chestsToDeploy) throws Exception {
    this.config = config;
    this.lwc = lwc;
    this.persistence = persistence;
    this.scheduler = scheduler;
    this.player = player;

    chestBlocks = findBlocksForChest(player.getLocation().getBlock(), chestsToDeploy > 1);

    deathChest = new DeathChest(lwc, persistence, chestBlocks, drops);

    persistence.registerDeathChest(deathChest);
  }

  @Override
  public void run() {
    tryLock();

    if (config.isSignOnChest()) {
      findBlockForSign();

      if (signBlock != null) {
        Sign sign;
        if (signFacing != null) {
          deathChest.setBlock(signBlock, Material.WALL_SIGN);
          sign = (Sign) signBlock.getState();
          org.bukkit.material.Sign matSign = new org.bukkit.material.Sign(Material.WALL_SIGN);
          matSign.setFacingDirection(signFacing);
          sign.setData(matSign);
        } else {
          deathChest.setBlock(signBlock, Material.SIGN_POST);
          sign = (Sign) signBlock.getState();
        }
        sign.setLine(0, player.getDisplayName() + "'s");
        sign.setLine(1, "Deathpile");
        sign.update();
      }
    }

    if (config.isBeaconEnabled() && player.hasPermission("DeadMansChest2.beacon")) {
      int height = config.getBeaconHeight();
      Block nextBlock = chestBlocks.get(0).getRelative(0, 2, 0);

      for (int i = 0; i < height; i++) {
        if (canReplaceBlock(nextBlock)) {
          deathChest.setBlock(nextBlock, Material.GLOWSTONE);
        }
        nextBlock = nextBlock.getRelative(BlockFace.UP);
      }
    }

    if (config.isChestDeleteIntervalEnabled() && !player.hasPermission("DeadMansChest2.nodelete")) {
      int delay = config.getChestDeleteInterval() * 20;
      deathChest.scheduleRemoval(scheduler, delay);
    }
  }

  private void tryLock() {
    if (lwc != null && player.hasPermission("DeadMansChest2.lock")) {
      Block chest = chestBlocks.get(0);
      lwc.getPhysicalDatabase().registerProtection(
          chest.getTypeId(),
          config.isLWCPrivateDefault() ? Protection.Type.PRIVATE : Protection.Type.PUBLIC,
          chest.getWorld().getName(),
          player.getName(),
          "",
          chest.getX(),
          chest.getY(),
          chest.getZ());
    }
  }

  private List<Block> findBlocksForChest(Block searchStart, boolean needAdjacent) throws Exception {
    for (int totalOffset = 0; totalOffset <= MAX_OFFSET; totalOffset++) {
      for (int xOffset = -totalOffset; xOffset <= totalOffset; xOffset++) {
        int remainingOffset = xOffset < 0 ? totalOffset + xOffset : totalOffset - xOffset;
        for (int zOffset = -remainingOffset; zOffset <= remainingOffset; zOffset++) {
          int yOffset = zOffset < 0 ? remainingOffset + zOffset : remainingOffset - zOffset;

          Block cursor = searchStart.getRelative(xOffset, yOffset, zOffset);
          if (!canPlaceChest(cursor)) {
            continue;
          }
          if (!needAdjacent) {
            return ImmutableList.of(cursor);
          }
          for (BlockFace direction : HORIZONTALLY_ADJACENT) {
            Block adjacent = cursor.getRelative(direction);
            if (canPlaceChest(adjacent)) {
              return ImmutableList.of(cursor, adjacent);
            }
          }
        }
      }
    }
    throw new Exception("Unable to place chest; no open blocks");
  }

  /**
   * Finds a valid block to place a sign and saves it in {@link #signBlock} and {@link #signFacing}.
   */
  private void findBlockForSign() {
    for (Block chestBlock : chestBlocks) {
      for (BlockFace direction : HORIZONTALLY_ADJACENT) {
        Block adjacent = chestBlock.getRelative(direction);
        if (canReplaceBlock(adjacent) && !chestBlocks.contains(adjacent)) {
          signBlock = adjacent;
          signFacing = direction;
          return;
        }
      }
    }
    for (Block chestBlock : chestBlocks) {
      Block above = chestBlock.getRelative(BlockFace.UP);
      if (canReplaceBlock(above)) {
        signBlock = above;
        return;
      }
    }
  }

  private boolean canPlaceChest(Block target) {
    return canReplaceBlock(target)
        && target.getRelative(BlockFace.UP).getType().isTransparent();
  }

  private boolean canReplaceBlock(Block target) {
    if (config.isBeaconReplacesLiquid()) {
      return Constants.MATERIALS_REPLACEABLE.contains(target.getType());
    }
    return target.getType() == Material.AIR;
  }
}
