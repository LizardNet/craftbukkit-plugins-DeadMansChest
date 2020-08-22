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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.Properties;
import java.util.logging.Logger;

public class Config {
  private static final Logger logger = Logger.getLogger("Minecraft");

  @ConfigDescription("Do players need a chest in their inventory to get a death chest?")
  private boolean NeedChestInInventory = false;

  @ConfigDescription("Should we lock chests with LWC?")
  private boolean LWCEnabled = true;

  @ConfigDescription("Should the glowstone, chest and sign drop their respective items when mined?")
  private boolean MineableDrops = false;

  @ConfigDescription("Should we build a glowstone tower?")
  private boolean BeaconEnabled = true;

  @ConfigDescription("And how high?")
  private int BeaconHeight = 10;

  @ConfigDescription("Should the beacon replace water/lava blocks as well or just air blocks?")
  private boolean BeaconReplacesLiquid = true;

  @ConfigDescription("Should we show a death message?")
  private boolean DeathMessage = true;

  @ConfigDescription("Put a sign on the chest with the player name?")
  private boolean SignOnChest = true;

  @ConfigDescription(
      "If we are using LWC to lock the chest should it be a private lock or a public lock?")
  private boolean LWCPrivateDefault = true;

  @ConfigDescription("If death messages are enabled, the string to display")
  private String DeathMessageString = "died. Deploying death chest.";

  @ConfigDescription("How long, in seconds, before the chest disappears and the items spill out?")
  private int ChestDeleteInterval = 3600;

  @ConfigDescription(
      "Should we drop items that don't fit into the chest (true), or just remove them from the"
          + " world (false)?")
  private boolean DropsEnabled = true;

  @ConfigDescription("Should we delete the chests after a certain time frame?")
  private boolean ChestDeleteIntervalEnabled = false;

  @ConfigDescription("Should players be allowed to loot death chests when they sneak click on one?")
  private boolean ChestLoot = false;

  public void load(File configFile) throws IOException {
    try (FileInputStream in = new FileInputStream(configFile)) {
      Properties prop = new Properties();
      prop.load(in);

      for (Field field : Config.class.getDeclaredFields()) {
        if (Modifier.isStatic(field.getModifiers())) {
          continue;
        }
        field.setAccessible(true);

        String value = prop.getProperty(field.getName());
        if (value == null) {
          continue;
        }

        Class<?> type = field.getType();
        if (String.class.isAssignableFrom(type)) {
          field.set(this, value);
        } else if (int.class.isAssignableFrom(type)) {
          field.setInt(this, Integer.parseInt(value));
        } else if (boolean.class.isAssignableFrom(type)) {
          field.setBoolean(this, Boolean.parseBoolean(value));
        }
      }
      if (new BigDecimal(prop.getProperty("version", "0"))
              .compareTo(new BigDecimal(Constants.VERSION))
          < 0) {
        save(configFile);
      }
    } catch (IllegalAccessException e) {
      logger.warning("Access error loading config: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public void save(File configFile) throws IOException {
    try (FileWriter fw = new FileWriter(configFile);
        BufferedWriter outChannel = new BufferedWriter(fw)) {
      outChannel.write("# DeadMansChest2 configuration\n" + "version=" + Constants.VERSION + "\n");

      for (Field field : Config.class.getDeclaredFields()) {
        if (Modifier.isStatic(field.getModifiers())) {
          continue;
        }
        field.setAccessible(true);
        ConfigDescription description = field.getAnnotation(ConfigDescription.class);
        if (description != null) {
          outChannel.write("# " + description.value() + "\n");
        }
        outChannel.write(field.getName() + "=" + field.get(this) + "\n");
      }
    } catch (IllegalAccessException e) {
      logger.warning("Access error saving config: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public boolean isDropsEnabled() {
    return DropsEnabled;
  }

  public void setDropsEnabled(boolean dropsEnabled) {
    this.DropsEnabled = dropsEnabled;
  }

  public boolean isMineableDrops() {
    return MineableDrops;
  }

  public void setMineableDrops(boolean mineableDrops) {
    this.MineableDrops = mineableDrops;
  }

  public boolean isDeathMessage() {
    return DeathMessage;
  }

  public void setDeathMessage(boolean deathMessage) {
    this.DeathMessage = deathMessage;
  }

  public String getDeathMessageString() {
    return DeathMessageString;
  }

  public void setDeathMessageString(String deathMessageString) {
    this.DeathMessageString = deathMessageString;
  }

  public boolean isSignOnChest() {
    return SignOnChest;
  }

  public void setSignOnChest(boolean signOnChest) {
    SignOnChest = signOnChest;
  }

  public boolean isLWCEnabled() {
    return LWCEnabled;
  }

  public void setLWCEnabled(boolean LWCEnabled) {
    this.LWCEnabled = LWCEnabled;
  }

  public boolean isLWCPrivateDefault() {
    return LWCPrivateDefault;
  }

  public void setLWCPrivateDefault(boolean LWCPrivateDefault) {
    this.LWCPrivateDefault = LWCPrivateDefault;
  }

  public boolean isBeaconEnabled() {
    return BeaconEnabled;
  }

  public void setBeaconEnabled(boolean beaconEnabled) {
    BeaconEnabled = beaconEnabled;
  }

  public int getBeaconHeight() {
    return BeaconHeight;
  }

  public void setBeaconHeight(int beaconHeight) {
    BeaconHeight = beaconHeight;
  }

  public boolean isBeaconReplacesLiquid() {
    return BeaconReplacesLiquid;
  }

  public void setBeaconReplacesLiquid(boolean beaconReplacesLiquid) {
    BeaconReplacesLiquid = beaconReplacesLiquid;
  }

  public int getChestDeleteInterval() {
    return ChestDeleteInterval;
  }

  public void setChestDeleteInterval(int chestDeleteInterval) {
    ChestDeleteInterval = chestDeleteInterval;
  }

  public boolean isChestDeleteIntervalEnabled() {
    return ChestDeleteIntervalEnabled;
  }

  public void setChestDeleteIntervalEnabled(boolean chestDeleteIntervalEnabled) {
    ChestDeleteIntervalEnabled = chestDeleteIntervalEnabled;
  }

  public boolean isChestLoot() {
    return ChestLoot;
  }

  public void setChestLoot(boolean chestLoot) {
    ChestLoot = chestLoot;
  }

  public boolean isNeedChestInInventory() {
    return NeedChestInInventory;
  }

  public void setNeedChestInInventory(boolean needChestInInventory) {
    this.NeedChestInInventory = needChestInInventory;
  }
}
