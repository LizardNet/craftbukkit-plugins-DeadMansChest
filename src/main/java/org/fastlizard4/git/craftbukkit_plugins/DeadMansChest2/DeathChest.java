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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class DeathChest
{
	private final Config config;
	private final Persistence persistence;
	private final Block chest;
	@Nullable
	private final Block secondChest;
	private final List<Block> blocks = new ArrayList<>();
	@Nullable
	private LWC lwc;
	private Cancellable removalTask;

	public DeathChest(Config config, Persistence persistence, Block chest, @Nullable Block secondChest, List<ItemStack> items)
	{
		this.config = config;
		this.persistence = persistence;
		this.chest = chest;
		setBlock(chest, Material.CHEST);
		this.secondChest = secondChest;
		if (secondChest != null)
		{
			setBlock(secondChest, Material.CHEST);
		}

		Iterator<ItemStack> iter = items.iterator();
		for (Inventory inventory : getInventories())
		{
			int remaining = 27;
			while (iter.hasNext() && remaining > 0)
			{
				ItemStack itemStack = iter.next();
				if (itemStack == null || itemStack.getType() == Material.AIR)
				{
					continue;
				}
				inventory.addItem(itemStack);
				iter.remove();
				remaining--;
			}
		}
	}

	public Block getChest()
	{
		return chest;
	}

	public void setBlock(Block block, Material material)
	{
		blocks.add(block);
		block.setType(material);
	}

	public boolean containsBlock(Block block)
	{
		return blocks.contains(block);
	}

	public void removeBlock(Block block)
	{
		if (block.equals(chest))
		{
			removeAll();
			return;
		}
		if (blocks.contains(block))
		{
			block.setType(Material.AIR);
			blocks.remove(block);
		}
	}

	public void removeAll()
	{
		if (lwc != null)
		{
			Protection protection = lwc.findProtection(chest);
			if (protection != null)
			{
				protection.remove();
			}
		}
		for (int i = blocks.size() - 1; i >= 0; i--)
		{
			blocks.get(i).setType(Material.AIR);
		}
		blocks.clear();
		persistence.unregisterDeathChest(this);
		removalTask.cancel();
	}

	@SuppressWarnings("deprecation")
	public void lock(LWC lwc, String playerName)
	{
		this.lwc = lwc;
		lwc.getPhysicalDatabase().registerProtection(chest.getTypeId(),
				config.isLWCPrivateDefault() ? Protection.Type.PRIVATE : Protection.Type.PUBLIC,
				chest.getWorld().getName(), playerName, "",
				chest.getX(), chest.getY(), chest.getZ());
	}

	public void scheduleRemoval(Scheduler scheduler, long delay)
	{
		removalTask = scheduler.schedule(this::removeAll, delay);
	}

	public void loot(PlayerInventory target)
	{
		for (Inventory inventory : getInventories())
		{
			for (ItemStack stack : inventory.getContents())
			{
				if (target.firstEmpty() == -1)
				{
					return;
				}
				if (stack != null)
				{
					target.addItem(stack);
					inventory.removeItem(stack);
				}
			}
		}
		// we got to the end without returning, so we've fully looted the chest
		removeAll();
	}

	private Inventory[] getInventories()
	{
		if (secondChest != null)
		{
			return new Inventory[] { getChestInventory(chest), getChestInventory(secondChest) };
		}
		return new Inventory[] { getChestInventory(chest) };
	}

	private static Inventory getChestInventory(Block chestBlock)
	{
		return ((Chest)chestBlock.getState()).getInventory();
	}
}
