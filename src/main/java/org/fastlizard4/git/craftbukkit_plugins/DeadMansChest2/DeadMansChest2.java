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

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCPlugin;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class DeadMansChest2 extends JavaPlugin
{
	private static final Logger logger = Logger.getLogger("Minecraft");

	private final Config configuration = new Config();

	@Override
	public void onDisable()
	{
		logger.log(Level.INFO, "[DeadMansChest2] unloaded.");

	}

	@Override
	public void onEnable()
	{
		try
		{
			File mainDir = new File(Constants.PLUGIN_DATA_DIRECTORY);

			if (!mainDir.exists() && !mainDir.mkdirs())
			{
				logger.warning("[DeadMansChest2] Could not create plugin data directory at " +
						Constants.PLUGIN_DATA_DIRECTORY);
			}

			File configFile = new File(mainDir + "/Config.cfg");
			if (configFile.exists())
			{
				logger.info("[DeadMansChest2] Found configuration file at " + configFile + "; loading");
				configuration.load(configFile);
			}
			else
			{
				logger.warning("[DeadMansChest2] Configuration file not found at " + configFile +
						"; creating a fresh one for you");
				configuration.save(configFile);
			}

			// TODO: Comment out when we're reasonably sure after reasonable testing that everything works
			logger.info("[DeadMansChest2] " + ConfigDebugHelper.dumpConfig(configuration));
		}
		catch (Exception e)
		{
			logger.warning("[DeadMansChest2] Configuration error: " + e.getMessage());
		}

		LWC lwc = null;
		if (configuration.isLWCEnabled())
		{
			Plugin lwcPlugin = getServer().getPluginManager().getPlugin("LWC");
			if (lwcPlugin != null)
			{
				logger.info("[DeadMansChest2] LWC plugin found!");
				lwc = ((LWCPlugin)lwcPlugin).getLWC();
			}
		}

		Server server = getServer();
		Persistence persistence = new Persistence();
		Scheduler scheduler = (task, delay) -> {
			BukkitScheduler bukkitScheduler = server.getScheduler();
			int taskId = bukkitScheduler.scheduleSyncDelayedTask(DeadMansChest2.this, task, delay);
			return () -> bukkitScheduler.cancelTask(taskId);
		};

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new EntLis(server, configuration, persistence, lwc, scheduler), this);
		pm.registerEvents(new CdBlockListener(configuration, persistence, lwc), this);

		logger.info("[DeadMansChest2] loaded.");
	}
}
