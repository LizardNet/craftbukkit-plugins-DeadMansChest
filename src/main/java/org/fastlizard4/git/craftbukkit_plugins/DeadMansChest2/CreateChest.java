package org.fastlizard4.git.craftbukkit_plugins.DeadMansChest2;

import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.griefcraft.model.Protection.Type;

public class CreateChest implements Runnable {
	
	DeadMansChest2 plugin;
	Block chestblock;
	Block chestblock2;
	LinkedList<ItemStack> chestitems;
	Player player;
	boolean doublechest;
	
	public CreateChest(DeadMansChest2 plugin, Block chestblock, LinkedList<ItemStack> chestitems, Player player, boolean doublechest) {
		this.plugin = plugin;
		this.chestblock = chestblock;
		this.chestitems = chestitems;
		this.player = player;
		this.doublechest = doublechest;
	}

	@Override
	public void run() {
		LinkedList<Block> changedblocks = new LinkedList<Block>();
		chestblock.setType(Material.CHEST);
		changedblocks.add(chestblock);
		plugin.nodropblocks.add(chestblock);
		BlockState state = chestblock.getState();
		Chest chest = (Chest)state;
		Chest chest2 = null;
		if(doublechest) {
			BlockFace[] direction = {BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH};
			boolean noroom = true;
			for(int y = 0; y < direction.length && noroom; y++) {
				Block tempblock = chestblock.getRelative(direction[y]);
				chestblock2 = tempblock;
				if(plugin.airblocks.contains(tempblock.getType())) {
					tempblock.setType(Material.CHEST);
					changedblocks.add(tempblock);
					plugin.nodropblocks.add(tempblock);
					BlockState state2 = tempblock.getState();
					chest2 = (Chest)state2;
					noroom = false;
				}
			}
		}
		//Block protectionblock = null;
		int j = 0;
		for(ItemStack item : chestitems) {
			if(item != null && item.getType() != Material.AIR) {
				if(j < 27) {
					chest.getInventory().addItem(item);
				}else {
					chest2.getInventory().addItem(item);
				}
				j++;
			}
		}
		
		if(plugin.LWC_Enabled && plugin.lwc != null && player.hasPermission("DeadMansChest2.lock")) {
			int blockId = chest.getTypeId();
			Type type = Type.PUBLIC;
			String world = chest.getWorld().getName();
			String owner = player.getName();
			String password = "";
			int x = chest.getX();
			int y = chest.getY();
			int z = chest.getZ();
			
			if(this.plugin.LWC_PrivateDefault) {
				type = com.griefcraft.model.Protection.Type.PRIVATE;
			}else {
				type = com.griefcraft.model.Protection.Type.PUBLIC;
			}
			plugin.lwc.getPhysicalDatabase().registerProtection(blockId, type, world, owner, password, x, y, z);
			//protectionblock = chestblock;
		}
		
		if(this.plugin.SignOnChest)	{
			boolean foundair = false;
			BlockFace[] directions = {BlockFace.EAST,BlockFace.WEST,BlockFace.NORTH,BlockFace.SOUTH};
			byte[] signbyte = {0x2,0x3,0x4,0x5};
			int signdirection = 1;
			for(int i = 0; i < directions.length && !foundair; i++) {
				if(plugin.LiquidReplace) {
					//If we can replace water, let's do it with the sign too!
					Block tempblock = chestblock.getRelative(directions[i]);
					if(tempblock.getType() == Material.AIR|| tempblock.getType() == Material.WATER 
							|| tempblock.getType() == Material.STATIONARY_WATER
							|| tempblock.getType() == Material.LAVA 
							|| tempblock.getType() == Material.STATIONARY_LAVA) {
						signdirection = i;
						foundair = true;
					}
				}else {
					if(chestblock.getRelative(directions[i]).getType() == Material.AIR) {
						signdirection = i;
						foundair = true;
					}
				}
			}
			
			if(foundair) {
				//-----------------------------------------------------------
				Block signBlock = chestblock.getRelative(directions[signdirection]);
				signBlock.setTypeIdAndData(68, signbyte[signdirection], false);
				BlockState signState = signBlock.getState();
				Sign sign = (Sign)signState;
				//-----------------------------------------------------------
				
				sign.setLine(0, player.getDisplayName()+"'s");
				sign.setLine(1, "Deathpile");
				sign.update();
				changedblocks.add(signBlock);
				plugin.nodropblocks.add(signBlock);
				//plugin.signblocks.put(chestblock, signBlock);
			}else {
				// If we didn't find a free spot, let's put the sign above the chest...
				// Will probably look very ugly though and pop off anyways...
				//-----------------------------------------------------------
				Block signBlock = chestblock.getRelative(BlockFace.UP);
				//Let's make sure we aren't overwriting a block here
				if(plugin.airblocks.contains(signBlock.getType())) {
					signBlock.setTypeIdAndData(63, signbyte[signdirection], false);
					BlockState signState = signBlock.getState();
					Sign sign = (Sign)signState;
					//-----------------------------------------------------------
					
					sign.setLine(0, player.getDisplayName()+"'s");
					sign.setLine(1, "Deathpile");
					sign.update();
					changedblocks.add(signBlock);
					plugin.nodropblocks.add(signBlock);
					//plugin.signblocks.put(chestblock, signBlock);
				}
			}
		}
		
		if(this.plugin.Sign_BeaconEnabled && player.hasPermission("DeadMansChest2.beacon"))	{
			int height = this.plugin.Sign_BeaconHeight;
			Location chestLocation1 = chestblock.getLocation();
			
			Location firstlocation = chestLocation1.add(0.0, 2.0, 0.0);
			Block nextblock = firstlocation.getBlock();
			
			for(int i = 0; i < height; i++) {
				if(plugin.LiquidReplace) {
					if(nextblock.getType() == Material.AIR || nextblock.getType() == Material.WATER 
							|| nextblock.getType() == Material.STATIONARY_WATER
							|| nextblock.getType() == Material.LAVA 
							|| nextblock.getType() == Material.STATIONARY_LAVA ) {
						nextblock.setType(Material.GLOWSTONE);
						plugin.nodropblocks.add(nextblock);
						changedblocks.add(nextblock);
					}
				}else {
					if(nextblock.getType() == Material.AIR) {
						nextblock.setType(Material.GLOWSTONE);
						plugin.nodropblocks.add(nextblock);
						changedblocks.add(nextblock);
					}
				}
				nextblock = nextblock.getRelative(BlockFace.UP);
			}
		}
		
		if(this.plugin.ChestDeleteIntervalEnabled && !player.hasPermission("DeadMansChest2.nodelete")) {
			int delay = this.plugin.ChestDeleteInterval*20;
			RemoveChest rc = new RemoveChest(plugin, changedblocks, chestblock, chestblock2);
			int taskid = this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, rc, delay);
			if(taskid != -1) {
				rc.setTaskID(taskid);
				plugin.deathchests.put(chestblock, rc);
			}
			
		}else {
			RemoveChest rc = new RemoveChest(plugin, changedblocks, chestblock, chestblock2);
			plugin.deathchests.put(chestblock, rc);
		}

	}

}
