/**
 * DEADMANSCHEST2
 * by Andrew "FastLizard4" Adams, TLUL, and the LizardNet CraftBukkit Plugins
 * Development Team (see AUTHORS.txt file)

 * BASED UPON:
 * DeadMansChest by Tux2, <https://github.com/Tux2/PlayerChestDeath>, GPL v3
 * (which was in turn based upon:)
 * PlayerChestDeath by Wesnc, <https://github.com/Wesnc/PlayerChestDeath>

 * Copyright (C) 2013 by Andrew "FastLizard4" Adams, TLUL, and the LizardNet
 * CraftBukkit Plugins Development Team. Some rights reserved.

 * License GPLv3+: GNU General Public License version 3 or later (at your
 * choice): <http://gnu.org/licenses/gpl.html>. This is free software: you are
 * free to change and redistribute it at your will provided that your
 * redistribution, with or without modifications, is also licensed under the GNU
 * GPL. (Although not required by the license, we also ask that you attribute
 * us!) There is NO WARRANTY FOR THIS SOFTWARE to the extent permitted by law.

 * This is an open source project. The source Git repositories, which you are
 * welcome to contribute to, can be found here:
 * <https://gerrit.fastlizard4.org/r/gitweb?p=craftbukkit-plugins/DeadMansChest.git;a=summary>
 * <https://git.fastlizard4.org/gitblit/summary/?r=craftbukkit-plugins/DeadMansChest.git>

 * Gerrit Code Review for the project:
 * <https://gerrit.fastlizard4.org/r/#/q/project:craftbukkit-plugins/DeadMansChest,n,z>

 * Continuous Integration for this project:
 * <https://integration.fastlizard4.org:444/jenkins/job/craftbukkit-plugins-DeadMansChest/>

 * Alternatively, the project source code can be found on the PUBLISH-ONLY
 * mirror on GitHub:
 * <https://github.com/LizardNet/craftbukkit-plugins-DeadMansChest>

 * Note: Pull requests and patches submitted to GitHub will be transferred by a
 * developer to Gerrit before they are acted upon.
*/

package org.fastlizard4.git.craftbukkit_plugins.DeadMansChest2;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.griefcraft.model.Protection;

public class CdBlockListener implements Listener {

	DeadMansChest2 plugin;

	public CdBlockListener(DeadMansChest2 plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {
		if(!event.isCancelled()) {
			if(plugin.nodropblocks.contains(event.getBlock())) {
				Block block = event.getBlock();
				if(!plugin.mineabledrops) {
					event.setCancelled(true);
					if(block.getType() == Material.CHEST) {
						//This fixes the sign bug and removes the glowstone tower
						//as well.
						if(plugin.deathchests.containsKey(block)) {
							RemoveChest dcstuff = plugin.deathchests.get(block);
							dcstuff.removeTheChest();
							//cancel the removal task.
							if(dcstuff.getTaskID() != -1) {
								plugin.getServer().getScheduler().cancelTask(dcstuff.getTaskID());
							}
						}else {
							block.setType(Material.AIR);
						}
					}else {
						block.setType(Material.AIR);
					}
				}
				plugin.nodropblocks.remove(block);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPistonRetract(BlockPistonRetractEvent event) {
		if(!event.isCancelled()) {
			if(plugin.nodropblocks.contains(event.getBlock()) && !plugin.mineabledrops) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPistonExtend(BlockPistonExtendEvent event) {
		if(!event.isCancelled()) {
			if(plugin.nodropblocks.contains(event.getBlock()) && !plugin.mineabledrops) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockDamage (BlockDamageEvent event) {
		if(!plugin.ChestLoot) {
			return;
		}
		Player player = event.getPlayer();
		Block chestblock = event.getBlock();
		if(player.isSneaking() && chestblock.getType() == Material.CHEST) {
			if(plugin.deathchests.containsKey(event.getBlock())) {
				if(plugin.LWC_Enabled && plugin.lwc != null) {
					Protection protection = plugin.lwc.findProtection(chestblock);
					if(protection.getType() == com.griefcraft.model.Protection.Type.PRIVATE) {
						if(protection.isOwner(player) || player.hasPermission("DeadMansChest2.loot")) {
							lootChest(player, chestblock);
						}
					}else {
						lootChest(player, chestblock);
					}
				}else {
					lootChest(player, chestblock);
				}
			}
		}
	}

	private void lootChest(Player player, Block chestblock) {
		PlayerInventory pi = player.getInventory();
		BlockState state = chestblock.getState();
		Chest chest = (Chest)state;
		ItemStack[] chestinventory = chest.getInventory().getContents();
		for(int i = 0; i < chestinventory.length && pi.firstEmpty() != -1; i++) {
			if(chestinventory[i] != null) {
				pi.addItem(chestinventory[i]);
				chest.getInventory().removeItem(chestinventory[i]);
			}
		}
		RemoveChest rc = plugin.deathchests.get(chestblock);
		//Looting double chests requires more work...
		if(rc.chestblock2 != null) {
			state = rc.chestblock2.getState();
			chest = (Chest)state;
			chestinventory = chest.getInventory().getContents();
			for(int i = 0; i < chestinventory.length && pi.firstEmpty() != -1; i++) {
				if(chestinventory[i] != null) {
					pi.addItem(chestinventory[i]);
					chest.getInventory().removeItem(chestinventory[i]);
				}
			}
		}
		rc.removeTheChest();
		if(rc.getTaskID() != -1) {
			plugin.getServer().getScheduler().cancelTask(rc.getTaskID());
		}
	}
}
