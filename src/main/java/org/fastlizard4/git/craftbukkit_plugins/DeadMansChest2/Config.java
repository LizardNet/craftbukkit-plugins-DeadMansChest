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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class Config
{
	public boolean drops = true;
	public boolean mineabledrops = false;
	public boolean deathMessage = true;
	public String deathMessageString = "died. Deploying death chest.";
	public boolean SignOnChest = true;
	public boolean LWC_Enabled = true;
	public boolean LWC_PrivateDefault = true;
	public boolean Sign_BeaconEnabled = true;
	public int Sign_BeaconHeight = 10;
	public boolean LiquidReplace = true;
	public int ChestDeleteInterval = 80;
	public boolean ChestDeleteIntervalEnabled = true;
	public boolean ChestLoot = false;
	public boolean needChestinInventory = false;

	public void load(File configFile) throws IOException
	{
		try (
				FileInputStream in = new FileInputStream(configFile);
		)
		{
			Properties prop = new Properties();
			prop.load(in);

			needChestinInventory = Boolean.parseBoolean(prop.getProperty("NeedChestInInventory", "false"));
			drops = Boolean.parseBoolean(prop.getProperty("DropsEnabled", "true"));
			deathMessage = Boolean.parseBoolean(prop.getProperty("DeathMessage", "true"));
			deathMessageString = prop.getProperty("DeathMessageString", "died. Deploying death chest.");
			SignOnChest = Boolean.parseBoolean(prop.getProperty("SignOnChest", "true"));
			LWC_Enabled = Boolean.parseBoolean(prop.getProperty("LWCEnabled", "true"));
			LWC_PrivateDefault = Boolean.parseBoolean(prop.getProperty("LWCPrivateDefault", "true"));
			Sign_BeaconEnabled = Boolean.parseBoolean(prop.getProperty("BeaconEnabled", "true"));
			try
			{
				Sign_BeaconHeight = Integer.parseInt(prop.getProperty("BeaconHeight", "10"));
			}
			catch (NumberFormatException e)
			{
				System.out.println("[DeadMansChest2] Couldn't process BeaconHeight, using default");
			}
			LiquidReplace = Boolean.parseBoolean(prop.getProperty("BeaconReplacesLiquid", "true"));
			mineabledrops = Boolean.parseBoolean(prop.getProperty("MineableDrops", "false"));
			try
			{
				ChestDeleteInterval = Integer.parseInt(prop.getProperty("ChestDeleteInterval", "80"));
			}
			catch (NumberFormatException e)
			{
				System.out.println("[DeadMansChest2] Couldn't process ChestDeleteInterval, using default");
			}
			ChestDeleteIntervalEnabled = Boolean.parseBoolean(prop.getProperty("ChestDeleteIntervalEnabled", "true"));
			ChestLoot = Boolean.parseBoolean(prop.getProperty("ChestLoot", "false"));
			double sversion = Double.parseDouble(prop.getProperty("version", "0.4"));

			// Automatically update the ini file here.
			if (sversion < 0.8)
			{
				save(configFile);
			}
		}
	}

	public void save(File configFile) throws IOException
	{
		try (
				FileWriter fw = new FileWriter(configFile);
				BufferedWriter outChannel = new BufferedWriter(fw)
		)
		{
			outChannel.write("#This is the main DeadMansChest2 config file\n" +
					"#Death Message must be true for the death message String to work!\n" +
					"#ChestDeleteInterval is in seconds.\n" +
					"\n" +
					"#NEW! Do players need a chest in their inventory to get a death chest?\n" +
					"NeedChestInInventory=" + needChestinInventory + "\n" +
					"# Should we lock chests with LWC\n" +
					"LWCEnabled=" + LWC_Enabled + "\n" +
					"#Should the glowstone, chest and sign drop their respective items when mined?\n" +
					"MineableDrops=" + mineabledrops + "\n" +
					"#Should we build a glowstone tower\n" +
					"BeaconEnabled=" + Sign_BeaconEnabled + "\n" +
					"#And how high?\n" +
					"BeaconHeight=" + Sign_BeaconHeight + "\n" +
					"#Should the beacon replace water/lava blocks as well or just air blocks?\n" +
					"BeaconReplacesLiquid=" + LiquidReplace + "\n" +
					"#Should we show a death message?\n" +
					"DeathMessage=" + deathMessage + "\n" +
					"#Put a sign on the chest with the player name?\n" +
					"SignOnChest=" + SignOnChest + "\n" +
					"#If we are using LWC to lock the chest should it be a private lock or a public lock?\n" +
					"LWCPrivateDefault=" + LWC_PrivateDefault + "\n" +
					"#If death messages are enabled the string to display.\n" +
					"DeathMessageString=" + deathMessageString + "\n" +
					"#How long before the chest disappears and the items spill out in seconds.\n" +
					"ChestDeleteInterval=" + ChestDeleteInterval + "\n" +
					"#Should we drop any items normally that don't fit into the chest, or just remove them from the world.\n" +
					"DropsEnabled=" + drops + "\n" +
					"#Should we delete the chests after a certain time frame?\n" +
					"ChestDeleteIntervalEnabled=" + ChestDeleteIntervalEnabled + "\n" +
					"#Should players be allowed to loot death chests when they sneak click on one?\n" +
					"# Players can only loot their own chests if LWC protection is set to private \n" +
					"# or to loot any chest with lwc they need the DeadMansChest2.loot permission node.\n" +
					"ChestLoot=" + ChestLoot + "\n" +
					"#Do not change anything below this line unless you know what you are doing!\n" +
					"version = " + Constants.VERSION);
		}
	}
}
