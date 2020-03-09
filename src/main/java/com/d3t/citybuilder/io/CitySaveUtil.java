package com.d3t.citybuilder.io;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.World;

import com.d3t.citybuilder.cities.City;
import com.d3t.citybuilder.framework.CBMain;
import com.d3t.citybuilder.framework.ChunkPosition;
import com.d3t.citybuilder.zones.Zone;

public class CitySaveUtil {
	
	public static final String subfolder = "cities/";
	public static final String fileExtension = ".cbcity";
	
	public static final String zoneDataSaveMark = "#ZONES";
	
	public static int successfullySavedCities = 0;
	public static int failedToSaveCities = 0;
	
	public static int successfullyLoadedCities = 0;
	public static int failedToLoadCities = 0;
	
	public static void saveCities() {
		successfullySavedCities = 0;
		failedToSaveCities = 0;
		for(City c : CBMain.cities.values()) {
			if(saveCity(c)) {
				successfullySavedCities++;
			} else {
				failedToSaveCities++;
			}
		}
	}
	
	public static boolean saveCity(City c) {
		File file = new File(CBMain.getDataFolderPath(), subfolder+c.cityName.toLowerCase()+fileExtension);
	    if (!file.exists()) {
	    	file.getParentFile().mkdirs();
	    }
	    try {
	    	FileUtil futil = new FileUtil();
	    	futil.SetValue("version", 1);
	    	futil.SetValue("cityname", c.cityName);
	    	futil.SetValue("mayorname", c.mayorName);
	    	futil.SetValue("world", c.world.getName());
	    	futil.SetValue("origin", c.origin.getIndex());
	    	ArrayList<String> zones = new ArrayList<String>();
	    	for(int zonekey : c.chunks.keySet()) {
	    		Zone zone = c.chunks.get(zonekey);
	    		if(zone != null) {
	    			zones.add(zonekey+" "+zone.getSaveString());
	    		}
	    	}
	    	futil.SetArrayList("zones", zones);
	    	futil.Save("cities", c.cityName+fileExtension);
	    	System.out.println("City saved: "+file.getAbsolutePath());
	    	return true;
	    }
	    catch (Exception e) {
	    	System.out.println("Failed to write city file: "+file.getAbsolutePath());
	    	e.printStackTrace();
	    	return false;
		}
	}
	
	public static void loadCities() {
		CitySaveUtil.successfullyLoadedCities = 0;
		CitySaveUtil.failedToLoadCities = 0;
		CBMain.cities = new HashMap<String, City>();
		File dir = new File(CBMain.getDataFolderPath().getAbsolutePath() + "/"+subfolder);
		System.out.println("loading cities ...");
		if (dir.isDirectory()) {
			for (File f : SaveHandler.listFiles(dir, fileExtension)) {
				System.out.println("Loading city file " + f.getName());
				if(loadCity(f)) {
					CitySaveUtil.successfullyLoadedCities++;
				} else {
					System.out.println("Failed to load city file " + f.getName());
					CitySaveUtil.failedToLoadCities++;
				}
			}
		} else {
			System.out.println("Failed to load cities! Not a directory: " + dir.getAbsolutePath());
		}
	}
	
	public static boolean loadCity(File file) {
		if(!file.exists()) {
			System.out.println("Failed to read file (not found): "+file.getAbsolutePath());
			return false;
		}
		try {
			FileUtil reader = FileUtil.createFromFile(file);
			int version = reader.GetInt("version");
			if(version == 1) {
				return readFileVersion1(reader, file);
			} else {
				System.out.println("Unknown file version: "+version);
				return false;
			}
		}
		catch(Exception e) {
			System.out.println("Error while reading city file: "+file.getAbsolutePath());
	    	e.printStackTrace();
	    	return false;
		}
	}
	
	private static boolean readFileVersion1(FileUtil reader, File file) {
		try {
			String cityname = reader.GetString("cityname");
			String mayorname = reader.GetString("mayorname");
			String worldname = reader.GetString("world");
			int origin = reader.GetInt("origin");
			String[] zoneStrings = reader.GetArray("zones");
			World world = CBMain.INSTANCE.getServer().getWorld(worldname);
			ChunkPosition pos = new ChunkPosition(origin);
			City c = loadFromSaveData(world, pos.x, pos.z, mayorname, cityname, zoneStrings);
			CBMain.cities.put(cityname, c);
			return true;
		}
		catch(Exception e) {
			System.out.println("Error while creating city from file: "+file.getAbsolutePath());
			e.printStackTrace();
			return false;
		}
	}
	
	public static City loadFromSaveData(World w, int x, int z, String owner, String name, String[] zoneStrings) {
		HashMap<Integer, Zone> zones = new HashMap<Integer, Zone>();
		City city = new City(w,x,z);
		city.mayorName = owner;
		city.cityName = name;
		for(String s : zoneStrings) {
			Zone zone = Zone.loadFromSaveData(w, s, city);
			if(zone != null) {
				zones.put(zone.pos.getIndex(), zone);
			}
		}
		city.chunks = zones;
		return city;
	}
}
