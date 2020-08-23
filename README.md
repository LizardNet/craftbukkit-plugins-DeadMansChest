DeadMansChest2
==============

* LizardNet Code Review to GitHub Mirroring Status: [![LizardNet Code Review to GitHub Mirroring Status](https://integration.fastlizard4.org:444/jenkins/buildStatus/icon?job=craftbukkit-plugins-DeadMansChest%20github%20mirror)](https://integration.fastlizard4.org:444/jenkins/job/craftbukkit-plugins-DeadMansChest%20github%20mirror/)
* Jenkins Autobuild Status: [![Jenkins Autobuild Status](https://integration.fastlizard4.org:444/jenkins/buildStatus/icon?job=craftbukkit-plugins-DeadMansChest)](https://integration.fastlizard4.org:444/jenkins/job/craftbukkit-plugins-DeadMansChest/)

**Warning: This plugin is still in development.  Use at your own risk.  It is
considered highly unstable, and is not yet ready for distribution.**

*A plugin for CraftBukkit-based Minecraft servers.*

Background (DeadMansChest)
--------------------------
DeadMansChest is a CraftBukkit plugin that causes players, when they die, to
drop a chest or double chest tile entity containing their inventory, instead of
just spewing their inventory out as items.

In addition, DeadMansChest supports integration with LWC for chest locking, and
supports features such as requiring players to actually carry chests in their
inventory to be able to spawn chests when they die.

See [BukkitDev][] for more information about DeadMansChest and its capabilities.

Unfortunately, the developer of DeadMansChest has stopped developing his plugin,
so LizardNet is starting development of this plugin, DeadMansChest2, to update
it and make a great plugin usable again on modern CraftBukkit servers.

The Plan for DeadMansChest2
---------------------------
The plan for DeadMansChest2 is to, at least initially, be as true to
the original DeadMansChest as possible.  For now, we plan on fixing bugs like
odd sign placement, chests being able to be destroyed by explosions, and the
glowstone beacons sometimes being minable.  We will have our first release of
DeadMansChest2 once we have fixed at least a majority of these bugs.  For now,
though, DeadMansChest2 is mostly just a copy of DeadMansChest that we are
currently doing work on.  When DeadMansChest2 is ready, this readme document
will be updated.

How To Help
-----------
DeadMansChest2 is open source, and you are welcome to help!  The GitHub
repository, which you are probably reading this at, is actually a mirror of
the main repository at LizardNet Code Review, which can be found
[here][lizardnet-repo].  Anyone may commit code to this repository by creating
an account at [LizardNet Code Review][gerrit].  Instructions on actually using
LizardNet Code Review will be placed here sometime in the near future, but if
you are already familiar with Gerrit-based repositories, go ahead and have at
it!  The Gerrit repository information can be found [here][gerrit-repo].

Note that you can submit pull requests and issue reports to GitHub (just in case
you aren't reading this on GitHub, [here's a link][github-repo]), but pull
requests will be converted to LizardNet Code Review patchsets before they are
acted upon by the developers.  Bug reports should be reported as issues on the
GitHub repository.

Getting the Plugin and Builds
-----------------------------
If you want to run DeadMansChest2, don't bother - we don't have anything
interesting for you yet.  If you are a Java programmer, you are welcome to help
us with the project, though (see above section)!  Builds of the project can be
acquired at our Jenkins Continuous Integration server, specifically at
[this project][jenkins-build].  If you want to download a build, you can get
them at the [Jenkins download page][jenkins-download], where builds are marked
as follows:

* Development (red star): Build reviewed by developers and confirmed to at least
  run, but may be extremely buggy and break in other ways.  Not recommended for
  use in production environments
* Beta (orange star): Build tested to some degree, most obvious bugs already
  handled but some may be left.
* Stable/recommended (green star): Build thoroughly tested by devs, recommended
  for use on production servers.

Downloading a build not marked as one of these is not recommended, as these
may very well simply refuse to work and have not been tested *at all*!

There is also a Jenkins job that handles mirroring of the Git repository from
LizardNet Code Review to GitHub, and that can be found [here][jenkins-mirror].

Licensing and Ackowledgements
-----------------------------
**DeadMansChest2**

by Andrew "FastLizard4" Adams, TLUL, and the LizardNet CraftBukkit Plugins
Development Team (see AUTHORS.txt file)

BASED UPON:
* DeadMansChest by Tux2, <https://github.com/Tux2/PlayerChestDeath>, GPL v3
* *(which was in turn based upon:)*
* PlayerChestDeath by Wesnc, <https://github.com/Wesnc/PlayerChestDeath>

Copyright (C) 2014 by Andrew "FastLizard4" Adams, TLUL, and the LizardNet
CraftBukkit Plugins Development Team. Some rights reserved.

License GPLv3+: GNU General Public License version 3 or later (at your choice):
<http://gnu.org/licenses/gpl.html>. This is free software: you are free to
change and redistribute it at your will provided that your redistribution, with
or without modifications, is also licensed under the GNU GPL. (Although not
required by the license, we also ask that you attribute us!) There is **NO
WARRANTY FOR THIS SOFTWARE** to the extent permitted by law.

[BukkitDev]: http://dev.bukkit.org/bukkit-plugins/deadmanschest/
[lizardnet-repo]: https://git.fastlizard4.org/gitblit/summary/?r=craftbukkit-plugins/DeadMansChest.git
[gerrit]: https://gerrit.fastlizard4.org
[gerrit-repo]: https://gerrit.fastlizard4.org/r/gitweb?p=craftbukkit-plugins/DeadMansChest.git;a=summary
[github-repo]: https://github.com/LizardNet/craftbukkit-plugins-DeadMansChest
[jenkins-build]: https://integration.fastlizard4.org:444/jenkins/job/craftbukkit-plugins-DeadMansChest/
[jenkins-download]: https://integration.fastlizard4.org:444/jenkins/job/craftbukkit-plugins-DeadMansChest/promotion/
[jenkins-mirror]: https://integration.fastlizard4.org:444/jenkins/job/craftbukkit-plugins-DeadMansChest%20github%20mirror/
	