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

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.block.Block;

public class Persistence {
  /**
   * Authoritative set of death chests. This is a prime target for storing to disk or to a database.
   */
  private final Set<DeathChest> deathChests = Collections.newSetFromMap(new ConcurrentHashMap<>());

  /**
   * Creates a new instance with no registered death chests.
   */
  public Persistence() {
  }

  /**
   * Creates a new instance with a collection of existing known death chests.
   */
  public Persistence(Collection<DeathChest> deathChests) {
    deathChests.forEach(this::registerDeathChest);
  }

  /**
   * Registers a new death chest with this instance. The provided death chest must not be modified
   * after calling this method; use {@link #removeAndUnregisterFakeBlock(Block)} to remove blocks.
   */
  public void registerDeathChest(DeathChest chest) {
    deathChests.add(chest);
  }

  public void unregisterDeathChest(DeathChest chest) {
    deathChests.remove(chest);
  }

  public boolean isFakeBlock(Block block) {
    return deathChests.stream()
        .anyMatch(deathChest -> deathChest.containsBlock(block));
  }

  public void removeAndUnregisterFakeBlock(Block block) {
    deathChests.stream()
        .filter(deathChest -> deathChest.containsBlock(block))
        .forEach(deathChest -> deathChest.removeBlock(block));
  }

  public boolean isLootableBlock(Block block) {
    return deathChests.stream()
        .anyMatch(deathChest -> deathChest.containsLootableBlock(block));
  }

  public DeathChest getDeathChestByLootableBlock(Block block) {
    return deathChests.stream()
        .filter(deathChest -> deathChest.containsLootableBlock(block))
        .findAny()
        .orElse(null);
  }
}
