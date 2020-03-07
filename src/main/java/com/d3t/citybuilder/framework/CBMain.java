package com.d3t.citybuilder.framework;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.d3t.citybuilder.cities.City;
import com.d3t.citybuilder.io.CitySaveUtil;
import com.d3t.citybuilder.io.StructureSaveUtil;
import com.d3t.citybuilder.userinteractive.ClickEventHandler;
import com.d3t.citybuilder.userinteractive.HotbarHandler;

import net.milkbowl.vault.economy.Economy;

public final class CBMain extends JavaPlugin {

	public List<String> playerList;

	public static CBMain INSTANCE;
	public static final Logger log = Logger.getLogger("Minecraft");
	public static Economy econ = null;

	public static BukkitTask task;
	public static Commands commandHandler;
	
	public static HashMap<String, City> cities;
	public static HashMap<Player, Integer> playerEditModes;
	public static HashMap<Player, City> currentlyEditingCity;

	public static Server getServerInstance() {
		return INSTANCE.getServer();
	}
	
	@Override
	public void onEnable() {
		INSTANCE = this;
		setupEconomy();
		cities = new HashMap<String, City>();
		playerEditModes = new HashMap<Player, Integer>();
		currentlyEditingCity = new HashMap<Player, City>();
		commandHandler = new Commands(getServer());
		getServer().getScheduler().runTaskTimer(this, new PluginLoop(), 20, 1);
		getServer().getPluginManager().registerEvents(new ClickEventHandler(), this);
		CitySaveUtil.loadCities();
		StructureSaveUtil.loadSavedStructures();
	}
	
	 @Override
	 public void onDisable() {
		 CitySaveUtil.saveCities();
	 }

	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return commandHandler.onCommand(sender, cmd, label, args);
	}

	public OfflinePlayer getOfflinePlayer(String name) {
		for (OfflinePlayer p : getServer().getOfflinePlayers()) {
			if (p.getName().equalsIgnoreCase(name))
				return p;
		}
		log.warning(String.format("Player named '%s' not found!", name));
		return null;
	}

	public Player toOnlinePlayer(OfflinePlayer offPlayer) {
		return getServer().getPlayer(offPlayer.getUniqueId());
	}
	
	public static boolean createCity(Player creator, String cityName, World w, int chunkX, int chunkZ) {
		float closestCity = getClosestCityDistance(w, chunkX, chunkZ);
		if(closestCity < City.minChunkDistanceBetweenCities) {
			creator.sendMessage("Too close to another city!");
			return false;
		}
		City c = getCityAtChunk(w, new ChunkPosition(chunkX, chunkZ));
		if(c != null) {
			creator.sendMessage("This area already belongs to "+c.cityName+"! Choose another area.");
		}
		for(String key : cities.keySet()) {
			if(key.equalsIgnoreCase(cityName)) {
				log.warning(String.format("City named '%s' already exists!", cityName));
				return false;
			}
		}
		City newcity = new City(w, chunkX, chunkZ, creator.getName(), cityName);
		cities.put(cityName, newcity);
		log.info(String.format("%s has created a new City: '%s'!", creator.getName(), cityName));
		creator.sendMessage("You founded: " + cityName + "!");
		return true;
	}
	
	public static int getEditModeForPlayer(Player p) {
		if(!playerEditModes.containsKey(p)) {
			playerEditModes.put(p, 0);
			return 0;
		} else {
			return playerEditModes.get(p);
		}
	}
	
	public static void setEditModeForPlayer(Player p, int m, City c, PlayerInteractEvent event) {
		if(event != null) event.setCancelled(true);
		playerEditModes.put(p, m);
		currentlyEditingCity.put(p, c);
		HotbarHandler.setHotbarItemsForMode(p, m);
	}
	
	public static void updateCities() {
		for(City c : cities.values()) {
			c.update();
		}
	}
	
	public static City findClosestCity(World w, int chunkX, int chunkZ) {
		float dist = 9999999f;
		City closest = null;
		for(City c : cities.values()) {
			if(c.world == w) {
				float d2 = distance2d(c.origin.x, c.origin.z, chunkX, chunkZ);
				if(d2 < dist) {
					closest = c;
					dist = d2;
				}
			}
		}
		return closest;
	}
	
	public static float getClosestCityDistance(World w, int chunkX, int chunkZ) {
		float dist = 9999999f;
		for(City c : cities.values()) {
			if(c.world == w) {
				float d2 = distance2d(c.origin.x, c.origin.z, chunkX, chunkZ);
				if(d2 < dist) {
					dist = d2;
				}
			}
		}
		return dist;
	}
	
	public static City findClosestCity(Player p) {
		ChunkPosition pos = new ChunkPosition(p.getLocation());
		return findClosestCity(p.getWorld(), pos.x, pos.z);
	}
	
	public static void openEditorForPlayer(Player p) {
		setEditModeForPlayer(p, 0, null, null);
	}
	
	private static float distance2d(int x1, int z1, int x2, int z2) {
		float deltaX = x1 - x2;
		float deltaZ = z1 - z2;
		return (float)Math.sqrt((deltaX * deltaX) + (deltaZ * deltaZ));
	}
	
	public static File getDataFolderPath() {
		return INSTANCE.getDataFolder();
	}
	
	public static City getCityAtChunk(World w, ChunkPosition pos) {
		for(City c : cities.values()) {
			if(c.world == w && c.chunks.containsKey(pos.getIndex())) return c;
		}
		return null;
	}
}
