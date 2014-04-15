package org.fastlizard4.git.craftbukkit_plugins.DeadMansChest2;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class EntLis implements Listener {
	public DeadMansChest2 plugin;

	public EntLis(DeadMansChest2 instance) {
		plugin = instance;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDeath(EntityDeathEvent event) {
		Entity entity = event.getEntity();

		if(entity instanceof Player) {
			Player player = (Player)entity;
			if(player.hasPermission("DeadMansChest2.chest")) {
				Location lastLoc = player.getLocation();
				Block block = lastLoc.getBlock();
				//See if the block we are on is a block we can safely write over...
				if(!plugin.airblocks.contains(block.getType())) {
					//Must not be, let's go a block up and see if that one is free...
					Block tempblock = block.getRelative(BlockFace.UP);
					if(plugin.airblocks.contains(tempblock.getType())) {
						block = tempblock;
					}else {
						//We can't find an open spot, so just spill the stuff on the ground...
						return;
					}
				}
				boolean doublechest = false;
				int j = 0;
				List<ItemStack> items = event.getDrops();
				LinkedList<ItemStack> addeditems = new LinkedList<ItemStack>();
				int i;
				//Code to check if the player's inventory is empty.
				boolean isempty = true;
				for(i = 0; i < items.size() && isempty; i++) {
					ItemStack item = items.get(i);
					if(item != null && item.getType() != Material.AIR) {
						isempty = false;
					}
				}
				if(isempty) {
					return;
				}

				//Check to see if the player has chests in his inventory
				//if he doesn't have the free chest permission.
				boolean needschests = false;
				int chestcount = 0;
				if(plugin.needChestinInventory && !player.hasPermission("DeadMansChest2.freechest")) {
					needschests = true;
					for(i = 0; i < items.size(); i++) {
						ItemStack item = items.get(i);
						if(item != null && item.getType() == Material.CHEST) {
							if(chestcount == 0) {
								chestcount += item.getAmount();
								if(item.getAmount() > 2) {
									item.setAmount(item.getAmount() - 2);
								}else {
									items.remove(i);
									//hack to get it to point in correct place.
									i--;
								}
							}else if(chestcount == 1) {
								chestcount += item.getAmount();
								if(item.getAmount() > 1) {
									item.setAmount(item.getAmount() - 1);
								}else {
									items.remove(i);
									//hack to get it to point in correct place.
									i--;
								}
							}else {
								chestcount += item.getAmount();
							}
						}
					}
					//If the chest count is still zero, the player doesn't have
					//any chests...
					if(chestcount == 0) {
						return;
					}
				}

				for(i = 0; i < items.size() && j < 27; i++)	{
					ItemStack item = items.get(i);
					if(item != null && item.getType() != Material.AIR) {
						addeditems.add(item);
						items.remove(i);
						//A little hack to make sure the pointer is pointing to the right place...
						i--;
						j++;
					}
				}
				//The player is carrying too many items to fit in one chest. Let's make it a double chest (if they have permission).
				if(j == 27 && player.hasPermission("DeadMansChest2.doublechest") && (!needschests || (needschests && chestcount > 1))) {
					BlockFace[] direction = {BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH};
					boolean noroom = true;
					for(int y = 0; y < direction.length && noroom; y++) {
						Block tempblock = block.getRelative(direction[y]);
						if(plugin.airblocks.contains(tempblock.getType())) {
							//we have an adjacent empty block, let's go ahead and add those items to another chest!
							for(; i < items.size(); i++)	{
								ItemStack item = items.get(i);
								if(item != null && item.getType() != Material.AIR) {
									addeditems.add(item);
									items.remove(i);
									//A little hack to make sure the pointer is pointing to the right place...
									i--;
									j++;
								}
							}
							//Let's exit the loop.
							noroom = false;
							doublechest = true;
						}
					}
				}else if(needschests && chestcount > 1) {
					//The player didn't have enough items to be in a double chest,
					//so let's add the other chest if there is room.
					addeditems.add(new ItemStack(Material.CHEST, 1));
				}

				if(!this.plugin.drops && !player.hasPermission("DeadMansChest2.drops"))	{
					event.getDrops().clear();
				}

				if(this.plugin.deathMessage && player.hasPermission("DeadMansChest2.message")) {
					this.plugin.getServer().broadcastMessage(ChatColor.RED + player.getDisplayName() + ChatColor.WHITE + " " + this.plugin.deathMessageString);
				}

				this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new CreateChest(plugin, block, addeditems, player, doublechest), 1);
			}

		}
	}
}
