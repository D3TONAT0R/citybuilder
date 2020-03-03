package com.d3t.citybuilder;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.World;
import org.bukkit.entity.Player;

public class City {

	public World world;
	public String mayorName;
	public HashMap<Integer, Zone> chunks;
	public ChunkPosition origin;
	public ArrayList<ConstructionData> constructions = new ArrayList<ConstructionData>();
	
	public String cityName;
	
	public City(World w, int x, int z) {
		world = w;
		origin = new ChunkPosition(x, z);
	}
	
	public City(World w, int x, int z, String owner, String name) {
		this(w,x,z);
		chunks = new HashMap<Integer, Zone>();
		mayorName = owner;
		cityName = name;
		addArea(x-2,z-2,x+2,z+2);
	}
	
	public static City loadFromSaveData(World w, int x, int z, String owner, String name, ArrayList<String> zoneStrings) {
		HashMap<Integer, Zone> zones = new HashMap<Integer, Zone>();
		City city = new City(w,x,z);
		city.mayorName = owner;
		city.cityName = name;
		city.chunks = zones;
		for(String s : zoneStrings) {
			Zone zone = Zone.loadFromSaveData(w, s, city);
			if(zone != null) zones.put(zone.pos.getIndex(), zone);
		}
		return city;
	}
	
	public void addArea(int x1, int z1, int x2, int z2) {
		int existingZones = 0;
		int newZones = 0;
		for(int i = x1; i <= x2; i++) {
			for(int j = z1; j <= z2; j++) {
				if(addChunk(i, j)) {
					newZones++;
				} else {
					existingZones++;
				}
			}
		}
		CBMain.log.info(String.format("Added %s zones to City '%s'. (already zoned chunks: %s)", newZones, cityName, existingZones));
	}
	
	public boolean addChunk(int x, int z) {
		if(chunks.containsKey(CityAreaHandler.chunkPosToIndex(x, z))) {
			return false;
		} else {
			Zone zone = new Zone(world, x, z, this);
			chunks.put(zone.pos.getIndex(), zone);
			return true;
		}
	}
	
	public void update() {
		ConstructionData toBeRemoved = null;
		for(ConstructionData c : constructions) {
			if(c == null || !c.updateConstruction()) toBeRemoved = c;
		}
		if(toBeRemoved != null) constructions.remove(toBeRemoved);
	}
	
	public boolean setZone(Player sender, int chunkX, int chunkZ, ZoneType zone, ZoneDensity density) {
		int index = new ChunkPosition(chunkX, chunkZ).getIndex();
		if(chunks.containsKey(index)) {
			chunks.get(index).reZone(zone, density);
			sender.sendMessage("Zone set to '"+zone.toString()+"'");
			return true;
		} else {
			sender.sendMessage("Out of city bounds!");
			return false;
		}
	}
	
	public boolean buildStructureAtChunk(String structureName, ChunkPosition pos, Orientation orientation, boolean forceBuild, boolean buildInstantly) {
		int index = pos.getIndex();
		if(chunks.containsKey(index)) {
			Structure s = StructureLibrary.allStructures.get(structureName);
			if(s != null) {
				chunks.get(index).build(s, orientation, forceBuild, true, buildInstantly);
				return true;
			} else {
				System.out.println("Can't build '"+structureName+"' here, the structure does not exist");
			}
		} else {
			System.out.println("Can't build here, out of bounds!");
		}
		return false;
	}
	
	public Zone[] getNeighborZones(int x, int z) {
		Zone[] neighbors = new Zone[4];
		neighbors[0] = chunks.get(new ChunkPosition(x, z-1).getIndex());
		neighbors[1] = chunks.get(new ChunkPosition(x+1, z).getIndex());
		neighbors[2] = chunks.get(new ChunkPosition(x, z+1).getIndex());
		neighbors[3] = chunks.get(new ChunkPosition(x-1, z).getIndex());
		return neighbors;
	}
	
	public void registerConstruction(ConstructionData cons) {
		if(!constructions.contains(cons)) constructions.add(cons);
	}
}
