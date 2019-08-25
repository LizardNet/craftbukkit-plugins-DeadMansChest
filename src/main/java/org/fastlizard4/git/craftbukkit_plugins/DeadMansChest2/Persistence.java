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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.block.Block;

public class Persistence
{
	private Set<Block> nodropblocks = new HashSet<>();
	private ConcurrentHashMap<Block, RemoveChest> deathchests = new ConcurrentHashMap<>();

	public void registerFakeBlock(Block block)
	{
		nodropblocks.add(block);
	}

	public void unregisterFakeBlock(Block block)
	{
		nodropblocks.remove(block);
	}

	public boolean isFakeBlock(Block block)
	{
		return nodropblocks.contains(block);
	}

	public void registerDeathChest(Block block, RemoveChest removalHook)
	{
		deathchests.put(block, removalHook);
	}

	public void unregisterDeathChest(Block block)
	{
		deathchests.remove(block);
	}

	public boolean isDeathChest(Block block)
	{
		return deathchests.containsKey(block);
	}

	public RemoveChest getRemovalHook(Block block)
	{
		return deathchests.get(block);
	}
}
