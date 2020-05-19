package com.d3t.citybuilder.cities;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.World;
import org.bukkit.entity.Player;

import com.d3t.citybuilder.framework.CBMain;
import com.d3t.citybuilder.framework.ChunkPosition;
import com.d3t.citybuilder.structures.Construction;
import com.d3t.citybuilder.structures.Orientation;
import com.d3t.citybuilder.structures.Structure;
import com.d3t.citybuilder.structures.StructureLibrary;
import com.d3t.citybuilder.zones.Zone;
import com.d3t.citybuilder.zones.ZoneDensity;
import com.d3t.citybuilder.zones.ZoneType;

public class City {
	
	public static final int minChunkDistanceBetweenCities = 4;

	public World world;
	public String mayorName;
	public HashMap<Integer, Zone> chunks;
	public ChunkPosition origin;
	public ArrayList<Construction> constructions = new ArrayList<Construction>();
	
	public String cityName;
	
	public CityStatistics stats = new CityStatistics(this);
	
	private int lastCheckedTime = 0;
	
	public City(World w, int x, int z) {
		world = w;
		origin = new ChunkPosition(x, z);
	}
	
	public City(World w, int x, int z, String owner, String name) {
		this(w,x,z);
		chunks = new HashMap<Integer, Zone>();
		mayorName = owner;
		cityName = name;
		addArea(x-4,z-4,x+4,z+4);
		stats.setStartValues();
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
		if(chunks.containsKey(CityAreaHandler.chunkPosToIndex(x, z)) || CBMain.getCityAtChunk(world, new ChunkPosition(x, z)) != null) {
			return false;
		} else {
			Zone zone = new Zone(world, x, z, this);
			chunks.put(zone.pos.getIndex(), zone);
			return true;
		}
	}
	
	public void update() {
		Construction toBeRemoved = null;
		for(Construction c : constructions) {
			if(c == null || !c.updateConstruction()) toBeRemoved = c;
		}
		if(lastCheckedTime > world.getTime()) {
			onDayStart();
		}
		if(toBeRemoved != null) constructions.remove(toBeRemoved);
	}
	
	private void onDayStart() {
		stats.onDayStart();
		stats.moneyBalance += stats.moneyBalance*0.05f;
	}
	
	public boolean setZone(Player sender, int chunkX, int chunkZ, ZoneType zone, ZoneDensity density) {
		int index = new ChunkPosition(chunkX, chunkZ).getIndex();
		if(chunks.containsKey(index)) {
			chunks.get(index).reZone(sender, zone, density);
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
				CBMain.log.info("Can't build '"+structureName+"' here, the structure does not exist");
			}
		} else {
			CBMain.log.info("Can't build here, out of bounds!");
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
	
	public void registerConstruction(Construction cons) {
		if(!constructions.contains(cons)) constructions.add(cons);
	}
}
