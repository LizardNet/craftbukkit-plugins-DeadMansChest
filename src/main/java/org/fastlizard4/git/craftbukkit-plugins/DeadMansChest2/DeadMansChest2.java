package org.fastlizard4.git.craftbukkit-plugins.DeadMansChest2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.griefcraft.lwc.*;

public class DeadMansChest2 extends JavaPlugin
{
	
	Logger logger = Logger.getLogger("Minecraft");
	
	public String mainDir = "plugins/DeadMansChest2/";
	public File configFile = new File(mainDir+"Config.cfg");
	public Properties prop = new Properties();
	public LinkedList<Block> nodropblocks = new LinkedList<Block>();
	public ConcurrentHashMap<Block, RemoveChest> deathchests = new ConcurrentHashMap<Block, RemoveChest>();

	public LinkedList<Material> airblocks = new LinkedList<Material>();
	
	EntLis entityListener = new EntLis(this);

	public LWC lwc = null;
	
	public String configFileHeader = 
		"Edit this file as needed.\n" +
		"Death Message must be true for the death message String to work!" +
		"ChestDeleteInterval is in seconds.\n";
	
	public boolean drops = true;
	public boolean mineabledrops = false;
	public boolean deathMessage = true;
	public String deathMessageString = "died. Deploying death chest.";
	public boolean SignOnChest = true;
	public boolean LWC_Enabled = true;
	public boolean LWC_PrivateDefault =true;
	public boolean Sign_BeaconEnabled = true;
	public int Sign_BeaconHeight = 10;
	public boolean LiquidReplace = true;
	public int ChestDeleteInterval = 80;
	public boolean ChestDeleteIntervalEnabled = true;
	public boolean ChestLoot = false;
	public boolean needChestinInventory = false;
	public String version = "0.5";
	
	public DeadMansChest2() {

		airblocks.add(Material.AIR);
		//airblocks.add(Material.GRASS);
		airblocks.add(Material.LONG_GRASS);
		airblocks.add(Material.SNOW);
		airblocks.add(Material.VINE);
		airblocks.add(Material.WATER_LILY);
		airblocks.add(Material.WATER);
		airblocks.add(Material.STATIONARY_WATER);
		airblocks.add(Material.LAVA);
		airblocks.add(Material.STATIONARY_LAVA);
	}
	
	@Override
	public void onDisable()	{
		logger.log(Level.INFO, "[DeadMansChest2] unloaded.");
		
	}

	@Override
	public void onEnable() {
		Plugin lwcPlugin = getServer().getPluginManager().getPlugin("LWC");
		if(lwcPlugin != null) {
			System.out.println("[DeadMansChest2] LWC plugin found!");
		    lwc = ((LWCPlugin) lwcPlugin).getLWC();
		}
		logger.log(Level.INFO, "[DeadMansChest2] loaded.");
		new File(mainDir).mkdir();
		
		if(!configFile.exists()) {
			updateIni();
		}else {
			loadConfig();
		}

		registerEvents();
	}

	private void loadConfig() {
		try	{
			FileInputStream in = new FileInputStream(configFile);
			prop.load(in);
			
			needChestinInventory = Boolean.parseBoolean(prop.getProperty("NeedChestInInventory", "false"));
			drops = Boolean.parseBoolean(prop.getProperty("DropsEnabled", "true"));
			deathMessage = Boolean.parseBoolean(prop.getProperty("DeathMessage", "true"));
			deathMessageString = prop.getProperty("DeathMessageString", "died. Deploying death chest.");
			SignOnChest = Boolean.parseBoolean(prop.getProperty("SignOnChest", "true"));
			LWC_Enabled = Boolean.parseBoolean(prop.getProperty("LWCEnabled", "true"));
			LWC_PrivateDefault = Boolean.parseBoolean(prop.getProperty("LWCPrivateDefault", "true"));
			Sign_BeaconEnabled = Boolean.parseBoolean(prop.getProperty("BeaconEnabled", "true"));
			try{
				Sign_BeaconHeight = Integer.parseInt(prop.getProperty("BeaconHeight", "10"));
			}catch (NumberFormatException e) {
				System.out.println("[DeadMansChest2] Couldn't process BeaconHeight, using default");
			}
			LiquidReplace = Boolean.parseBoolean(prop.getProperty("BeaconReplacesLiquid", "true"));
			mineabledrops = Boolean.parseBoolean(prop.getProperty("MineableDrops", "false"));
			try{
				ChestDeleteInterval = Integer.parseInt(prop.getProperty("ChestDeleteInterval", "80"));
			}catch (NumberFormatException e) {
				System.out.println("[DeadMansChest2] Couldn't process ChestDeleteInterval, using default");
			}
			ChestDeleteIntervalEnabled = Boolean.parseBoolean(prop.getProperty("ChestDeleteIntervalEnabled", "true"));
			ChestLoot = Boolean.parseBoolean(prop.getProperty("ChestLoot", "false"));
			double sversion = Double.parseDouble(prop.getProperty("version", "0.4"));
			
			//Autmatically update the ini file here.
			if(sversion < 0.8) {
				updateIni();
			}
		}
		catch(IOException ex) { }
		
	}

	private void registerEvents() {
		PluginManager pm = getServer().getPluginManager();
		CdBlockListener bl = new CdBlockListener(this);
		pm.registerEvents(entityListener, this);
		pm.registerEvents(bl, this);
	}
    
    private void updateIni() {
		try {
			BufferedWriter outChannel = new BufferedWriter(new FileWriter(configFile));
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
					"ChestLoot=" + ChestLoot + "\n\n" +
					"#Do not change anything below this line unless you know what you are doing!\n" +
					"version = " + version );
			outChannel.close();
		} catch (Exception e) {
			System.out.println("[DeadMansChest2] - file creation failed, using defaults.");
		}
		
	}

}
