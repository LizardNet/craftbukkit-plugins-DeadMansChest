/**
 * DEADMANSCHEST2
 * by Andrew "FastLizard4" Adams, TLUL, and the LizardNet CraftBukkit Plugins
 * Development Team (see AUTHORS.txt file)
 *
 * BASED UPON:
 * DeadMansChest by Tux2, <https://github.com/Tux2/PlayerChestDeath>, GPL v3
 * (which was in turn based upon:)
 * PlayerChestDeath by Wesnc, <https://github.com/Wesnc/PlayerChestDeath>
 *
 * Copyright (C) 2013-2019 by Andrew "FastLizard4" Adams, TLUL, and the LizardNet
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

import java.util.LinkedList;

import javax.annotation.Nullable;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CreateChest implements Runnable
{
	private Config config;
	private Persistence persistence;
	@Nullable
	private LWC lwc;
	private Scheduler scheduler;

	private Block chestblock;
	private Block chestblock2;
	private LinkedList<ItemStack> chestitems;
	private Player player;
	private boolean doublechest;

	public CreateChest(
			Config config,
			Persistence persistence,
			@Nullable LWC lwc,
			Scheduler scheduler,
			Block chestblock,
			LinkedList<ItemStack> chestitems,
			Player player,
			boolean doublechest
	)
	{
		this.config = config;
		this.persistence = persistence;
		this.lwc = lwc;
		this.scheduler = scheduler;
		this.chestblock = chestblock;
		this.chestitems = chestitems;
		this.player = player;
		this.doublechest = doublechest;
	}

	@Override
	public void run()
	{
		LinkedList<Block> changedblocks = new LinkedList<Block>();
		chestblock.setType(Material.CHEST);
		changedblocks.add(chestblock);
		persistence.nodropblocks.add(chestblock);
		BlockState state = chestblock.getState();
		Chest chest = (Chest)state;
		Chest chest2 = null;
		if (doublechest)
		{
			BlockFace[] direction = { BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH };
			boolean noroom = true;
			for (int y = 0; y < direction.length && noroom; y++)
			{
				Block tempblock = chestblock.getRelative(direction[y]);
				chestblock2 = tempblock;
				if (Constants.AIR_BLOCKS.contains(tempblock.getType()))
				{
					tempblock.setType(Material.CHEST);
					changedblocks.add(tempblock);
					persistence.nodropblocks.add(tempblock);
					BlockState state2 = tempblock.getState();
					chest2 = (Chest)state2;
					noroom = false;
				}
			}
		}
		//Block protectionblock = null;
		int j = 0;
		for (ItemStack item : chestitems)
		{
			if (item != null && item.getType() != Material.AIR)
			{
				if (j < 27)
				{
					chest.getInventory().addItem(item);
				}
				else
				{
					chest2.getInventory().addItem(item);
				}
				j++;
			}
		}

		if (config.isLWC_Enabled() && lwc != null && player.hasPermission("DeadMansChest2.lock"))
		{
			int blockId = chest.getTypeId();
			Type type = Type.PUBLIC;
			String world = chest.getWorld().getName();
			String owner = player.getName();
			String password = "";
			int x = chest.getX();
			int y = chest.getY();
			int z = chest.getZ();

			if (this.config.isLWC_PrivateDefault())
			{
				type = com.griefcraft.model.Protection.Type.PRIVATE;
			}
			else
			{
				type = com.griefcraft.model.Protection.Type.PUBLIC;
			}
			lwc.getPhysicalDatabase().registerProtection(blockId, type, world, owner, password, x, y, z);
			//protectionblock = chestblock;
		}

		if (this.config.isSignOnChest())
		{
			boolean foundair = false;
			BlockFace[] directions = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
			int signdirection = 1;
			for (int i = 0; i < directions.length && !foundair; i++)
			{
				if (config.isLiquidReplace())
				{
					//If we can replace water, let's do it with the sign too!
					Block tempblock = chestblock.getRelative(directions[i]);
					if (tempblock.getType() == Material.AIR || tempblock.getType() == Material.WATER
							|| tempblock.getType() == Material.STATIONARY_WATER
							|| tempblock.getType() == Material.LAVA
							|| tempblock.getType() == Material.STATIONARY_LAVA)
					{
						signdirection = i;
						foundair = true;
					}
				}
				else
				{
					if (chestblock.getRelative(directions[i]).getType() == Material.AIR)
					{
						signdirection = i;
						foundair = true;
					}
				}
			}

			if (foundair)
			{
				//-----------------------------------------------------------
				Block signBlock = chestblock.getRelative(directions[signdirection]);
				signBlock.setType(Material.WALL_SIGN);
				Sign sign = (Sign)signBlock.getState();
				org.bukkit.material.Sign matSign = new org.bukkit.material.Sign(Material.WALL_SIGN);
				matSign.setFacingDirection(directions[signdirection]);
				sign.setData(matSign);
				//-----------------------------------------------------------

				sign.setLine(0, player.getDisplayName() + "'s");
				sign.setLine(1, "Deathpile");
				sign.update();
				changedblocks.add(signBlock);
				persistence.nodropblocks.add(signBlock);
				//plugin.signblocks.put(chestblock, signBlock);
			}
			else
			{
				// If we didn't find a free spot, let's put the sign above the chest...
				// Will probably look very ugly though and pop off anyways...
				//-----------------------------------------------------------
				Block signBlock = chestblock.getRelative(BlockFace.UP);
				//Let's make sure we aren't overwriting a block here
				if (Constants.AIR_BLOCKS.contains(signBlock.getType()))
				{
					signBlock.setType(Material.SIGN_POST);
					Sign sign = (Sign)signBlock.getState();
					//-----------------------------------------------------------

					sign.setLine(0, player.getDisplayName() + "'s");
					sign.setLine(1, "Deathpile");
					sign.update();
					changedblocks.add(signBlock);
					persistence.nodropblocks.add(signBlock);
					//plugin.signblocks.put(chestblock, signBlock);
				}
			}
		}

		if (config.isSign_BeaconEnabled() && player.hasPermission("DeadMansChest2.beacon"))
		{
			int height = config.getSign_BeaconHeight();
			Location chestLocation1 = chestblock.getLocation();

			Location firstlocation = chestLocation1.add(0.0, 2.0, 0.0);
			Block nextblock = firstlocation.getBlock();

			for (int i = 0; i < height; i++)
			{
				if (config.isLiquidReplace())
				{
					if (nextblock.getType() == Material.AIR || nextblock.getType() == Material.WATER
							|| nextblock.getType() == Material.STATIONARY_WATER
							|| nextblock.getType() == Material.LAVA
							|| nextblock.getType() == Material.STATIONARY_LAVA)
					{
						nextblock.setType(Material.GLOWSTONE);
						persistence.nodropblocks.add(nextblock);
						changedblocks.add(nextblock);
					}
				}
				else
				{
					if (nextblock.getType() == Material.AIR)
					{
						nextblock.setType(Material.GLOWSTONE);
						persistence.nodropblocks.add(nextblock);
						changedblocks.add(nextblock);
					}
				}
				nextblock = nextblock.getRelative(BlockFace.UP);
			}
		}

		if (config.isChestDeleteIntervalEnabled() && !player.hasPermission("DeadMansChest2.nodelete"))
		{
			int delay = config.getChestDeleteInterval() * 20;
			RemoveChest rc = new RemoveChest(persistence, lwc, changedblocks, chestblock, chestblock2);
			int taskid = scheduler.schedule(rc, delay);
			if (taskid != -1)
			{
				rc.setTaskID(taskid);
				persistence.deathchests.put(chestblock, rc);
			}

		}
		else
		{
			RemoveChest rc = new RemoveChest(persistence, lwc, changedblocks, chestblock, chestblock2);
			persistence.deathchests.put(chestblock, rc);
		}
	}
}