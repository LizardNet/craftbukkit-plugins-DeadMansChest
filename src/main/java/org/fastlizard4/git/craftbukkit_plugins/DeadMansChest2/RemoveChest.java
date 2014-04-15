package org.fastlizard4.git.craftbukkit_plugins.DeadMansChest2;

import java.util.Iterator;
import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.griefcraft.model.Protection;

public class RemoveChest implements Runnable {

	DeadMansChest2 plugin;
	LinkedList<Block> changeblocks;
	Block chestblock;
	public Block chestblock2 = null;
	int taskid = -1;

	public RemoveChest(DeadMansChest2 plugin, LinkedList<Block> changeblocks, Block chestblock, Block chestblock2) {
		this.plugin = plugin;
		this.changeblocks = changeblocks;
		this.chestblock = chestblock;
		this.chestblock2 = chestblock2;
	}

	public void setTaskID(int id) {
		taskid = id;
	}

	public int getTaskID() {
		return taskid;
	}

	public void run() {
		removeTheChest();
	}

	public void removeTheChest() {
		//A little fix to fix the pop off signs...
		Iterator<Block> rblocks = changeblocks.descendingIterator();
		while(rblocks.hasNext()) {
			Block tblock = rblocks.next();
			if(plugin.nodropblocks.contains(tblock)) {
				tblock.setType(Material.AIR);
				plugin.nodropblocks.remove(tblock);
			}
		}
		if(chestblock != null && plugin.lwc != null) {
			Protection protection = plugin.lwc.findProtection(chestblock);
			if(protection != null) {
				protection.remove();
			}
		}
		plugin.deathchests.remove(chestblock);
	}

}
