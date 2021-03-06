package me.lordvakar.divinevillages.managers;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.lordvakar.divinevillages.DivineVillages;
import me.lordvakar.divinevillages.objects.Village;
import me.lordvakar.divinevillages.util.Util;

import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class VillageManager 
{
	public static List<Village> villages = new ArrayList<Village>();
	String prefix = ChatColor.GOLD + "" + ChatColor.BOLD + "DivineVillages> " + ChatColor.RESET + ChatColor.GOLD;
	DivineVillages main = (DivineVillages) DivineVillages.pl;
	private static JavaPlugin plugin = DivineVillages.getJavaPlugin();
	
	DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss z");
	Date date = new Date();
	String dateString = dateFormat.format(date);

	private static VillageManager am = new VillageManager();
	
	public static VillageManager getManager() {
		return am;
	}
	
    public Village getVillage(String villageName) {
        for (Village village : villages) {
        	if (village.getVillageName().equals(villageName)) {
                return village;
            }
        }
        return null;
    }
    
    public Village getPlayersVillage(Player player) {
        if(player == null) return null;
        for(Village v : villages) {
            if(v.getVillageLeader() == null) continue;
            if(v.isVillageLeader(player) || v.isVillageCitizen(player)) return v;
        }
        return null;
    }
    
    public void createVillage(String villageName, String villageDesc, Player villageLeader) {
    	
    	Village vil = new Village(villageName, villageDesc, villageLeader);
    	vil.setVillageCreationDate(dateString);
    	
    	File folder = new File("plugins/DivineVillages/Villages");
    	File villageFile = new File("plugins/DivineVillages/Villages" + "/" + villageName + ".yml");
    	YamlConfiguration villageConfig = YamlConfiguration.loadConfiguration(villageFile);
		if (!folder.exists()) {
			folder.mkdir();
			try {
				villageFile.createNewFile();
				//villageConfig.set("Villages." + villageName, null);
				String path = villageName + ".";
				villageConfig.set(path + "villageName", villageName);
				villageConfig.set(path + "villageDesc", villageDesc);
				villageConfig.set(path + "villageLeader", villageLeader.getName());
				villageConfig.set(path + "villageCreationDate", dateString);
				villageConfig.set(path + "villageOpen", true);
				//TODO: JUM JUM JUM villageSpawn
				villageConfig.save(villageFile);
				villageConfig.load(villageFile);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
			} 
		}
		else if(!villageFile.exists()) {
			try {
				villageFile.createNewFile();
				//villageConfig.set("Villages." + villageName, null);
				String path = villageName + ".";
				villageConfig.set(path + "villageName", villageName);
				villageConfig.set(path + "villageDesc", villageDesc);
				villageConfig.set(path + "villageLeader", villageLeader.getName());
				villageConfig.set(path + "villageCreationDate", dateString);
		 		villageConfig.set(path + "villageOpen", true);
				villageConfig.save(villageFile);
				villageConfig.load(villageFile);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
			}
		}
		else if(villageFile.exists()) {
		}
    }
    
    public void removeVillage(String villageName) {
    	File villageFile = new File("plugins/DivineVillages/Villages" + "/" + villageName + ".yml");
    	YamlConfiguration villageConfig = YamlConfiguration.loadConfiguration(villageFile);
        String path = villageName;
        villageConfig.set(path, null);
        villages.remove(getVillage(villageName));
		try {
			villageConfig.save(villageFile);
			villageConfig.load(villageFile);
	        villageFile.delete();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
    }
    
    public void joinVillage(String villageName, Player player) 
    {
    	if(!isInVillage(player)) {
    		if(villageExists(villageName)) {
    			Village vil = getVillage(villageName);
    			vil.makeCitizen(player);
    			player.teleport(vil.getVillageSpawn());
    		}
    	}
    }
    
    public void invitePlayer(String villageName, Player player) 
    {
    	if(!isInVillage(player)) {
    		if(villageExists(villageName)) {
    			player.sendMessage(prefix + "You have been invited to the village:" + villageName);
    			player.sendMessage(prefix + "Type /village join " + villageName + " to join!");
    			Village vil = getVillage(villageName);
    			vil.addInvited(player);
    		}
    	}
    }
    
    public boolean isInVillage(Player player) {
        for(Village v : villages) {
        	if(v.isVillageLeader(player) || v.isVillageCitizen(player)) {
        		return true;
        	}
        }
        return false;
    }
    
    public void loadAllVillages() {
    	//If this works, then FUCK YEA!
    	File folder = new File("plugins/DivineVillages/Villages");
        if (!folder.exists()) folder.mkdir();
        File[] villages = folder.listFiles();
        for (File f : villages) {
          Village v = loadVillage(f);
          if (v == null) { log(prefix + "Unable to load " + f.getName() + " as a Village!"); }
        }
    }
    
    public Village loadVillage(File f) {
    	if (!f.getName().toLowerCase().endsWith(".yml".toLowerCase())) {
    		return null;
    	} else {
    		YamlConfiguration yml = YamlConfiguration.loadConfiguration(f);
    		
    		String filename = f.getName();
    		int pos = filename.lastIndexOf(".");
    		String justName = pos > 0 ? filename.substring(0, pos) : filename;
    		
    		String path = justName + ".";
    		String villageName = yml.getString(path + "villageName");
    		String villageDesc = yml.getString(path + "villageDesc");
    		String villageLeader = yml.getString(path + "villageLeader");
    		String villageCreationDate = yml.getString(path + "villageCreationDate");
    		Player leaderPlayerObject = Util.getPlayer(villageLeader);
    		boolean villageOpen = yml.getBoolean(path + "villageOpen");
    		Village v = new Village(villageName, villageDesc, leaderPlayerObject);
    		v.setVillageName(villageName);
    		v.setVillageDesc(villageDesc);
    		v.setVillageLeader(leaderPlayerObject);
    		v.setVillageCreationDate(villageCreationDate);
    		v.setOpen(villageOpen);
    		try {
				yml.save(f);
				yml.load(f);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
			}
    		return v;
    	}
    }
    
	public boolean villageExists(String villageName) {
		if (getVillage(villageName) != null) {
			return true;
		} else {
			return false;
		}
	}
    
    public static void log(Object o) {
        getPlugin().getLogger().info(o.toString());
   	}

	public static JavaPlugin getPlugin() {
		return plugin;
	}
}
