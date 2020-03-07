package com.d3t.citybuilder.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.MalformedInputException;
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
	    	byte[] version = (1+"\n").getBytes();
	    	byte[] cityname = (c.cityName+"\n").getBytes();
	    	byte[] mayorname = (c.mayorName+"\n").getBytes();
	    	byte[] worldname = (c.world.getName()+"\n").getBytes();
	    	byte[] origin = (c.origin.getIndex()+"\n").getBytes();
	    	FileOutputStream stream = new FileOutputStream(file);
	    	stream.write(version);
	    	stream.write(cityname);
	    	stream.write(mayorname);
	    	stream.write(worldname);
	    	stream.write(origin);
	    	stream.write((zoneDataSaveMark+"\n").getBytes());
	    	for(int zonekey : c.chunks.keySet()) {
	    		Zone zone = c.chunks.get(zonekey);
	    		if(zone != null) {
	    			stream.write((zonekey+" "+zone.getSaveString()+"\n").getBytes());
	    		}
	    	}
	    	stream.close();
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
			FileInputStream stream = new FileInputStream(file);
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream)); 
			int version = Integer.parseInt(reader.readLine());
			if(version == 1) {
				return readFileVersion1(reader, file);
			} else {
				System.out.println("Unknown file version: "+version);
				reader.close();
				return false;
			}
		}
		catch(Exception e) {
			System.out.println("Error while reading city file: "+file.getAbsolutePath());
	    	e.printStackTrace();
	    	return false;
		}
	}
	
	private static boolean readFileVersion1(BufferedReader reader, File file) {
		try {
			String cityname = reader.readLine();
			String mayorname = reader.readLine();
			String worldname = reader.readLine();
			String originStr = reader.readLine();
			if(!reader.readLine().equals(zoneDataSaveMark)) throw new MalformedInputException(0);
			ArrayList<String> zoneStrings = new ArrayList<String>();
			boolean done = false;
			while(!done) {
				reader.mark(1000);
				String s = reader.readLine();
				if(s == null || s.startsWith("#")) {
					done = true;
					if(s != null && s.startsWith("#")) reader.reset();
					break;
				}
				zoneStrings.add(s);
			}
			World world = CBMain.INSTANCE.getServer().getWorld(worldname);
			ChunkPosition pos = new ChunkPosition(Integer.parseInt(originStr));
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
	
	public static City loadFromSaveData(World w, int x, int z, String owner, String name, ArrayList<String> zoneStrings) {
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
