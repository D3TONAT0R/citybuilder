package com.d3t.citybuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.MalformedInputException;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.World;

public class SaveHandler {
	
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
	
	public static void loadCities() {
		successfullyLoadedCities = 0;
		failedToLoadCities = 0;
		CBMain.cities = new HashMap<String, City>();
		File dir = new File(CBMain.getDataFolderPath().getAbsolutePath() + "/"+subfolder);
		System.out.println("loading cities ...");
		if (dir.isDirectory()) {
			for (File f : listFiles(dir)) {
				System.out.println("Loading city file " + f.getName());
				if(loadCity(f)) {
					successfullyLoadedCities++;
				} else {
					System.out.println("Failed to load city file " + f.getName());
					failedToLoadCities++;
				}
			}
		} else {
			System.out.println("Failed to load cities! Not a directory: " + dir.getAbsolutePath());
		}
	}
	
	private static boolean saveCity(City c) {
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
	
	private static boolean loadCity(File file) {
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
			City c = City.loadFromSaveData(world, pos.x, pos.z, mayorname, cityname, zoneStrings);
			CBMain.cities.put(cityname, c);
			return true;
		}
		catch(Exception e) {
			System.out.println("Error while creating city from file: "+file.getAbsolutePath());
			e.printStackTrace();
			return false;
		}
	}
	
	private static ArrayList<File> listFiles(File directory) {
		ArrayList<File> files = new ArrayList<File>();
		for (File f : directory.listFiles()) {
			if (f.isDirectory()) {
				files.addAll(listFiles(f));
			} else if (f.getName().endsWith(fileExtension)) {
				files.add(f);
			}
		}
		return files;
	}
}
