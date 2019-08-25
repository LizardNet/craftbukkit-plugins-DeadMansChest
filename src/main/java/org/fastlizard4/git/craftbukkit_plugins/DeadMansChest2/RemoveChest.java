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

import java.util.Iterator;
import java.util.LinkedList;

import javax.annotation.Nullable;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class RemoveChest implements Runnable
{
	private Persistence persistence;
	@Nullable
	private LWC lwc;

	private LinkedList<Block> changeblocks;
	private Block chestblock;
	public Block chestblock2 = null;
	private int taskid = -1;

	public RemoveChest(
			Persistence persistence,
			@Nullable LWC lwc,
			LinkedList<Block> changeblocks,
			Block chestblock,
			Block chestblock2
	)
	{
		this.persistence = persistence;
		this.lwc = lwc;
		this.changeblocks = changeblocks;
		this.chestblock = chestblock;
		this.chestblock2 = chestblock2;
	}

	public void setTaskID(int id)
	{
		taskid = id;
	}

	public int getTaskID()
	{
		return taskid;
	}

	public void run()
	{
		removeTheChest();
	}

	public void removeTheChest()
	{
		//A little fix to fix the pop off signs...
		Iterator<Block> rblocks = changeblocks.descendingIterator();
		while (rblocks.hasNext())
		{
			Block tblock = rblocks.next();
			if (persistence.isFakeBlock(tblock))
			{
				tblock.setType(Material.AIR);
				persistence.unregisterFakeBlock(tblock);
			}
		}
		if (chestblock != null && lwc != null)
		{
			Protection protection = lwc.findProtection(chestblock);
			if (protection != null)
			{
				protection.remove();
			}
		}
		persistence.unregisterDeathChest(chestblock);
	}
}
