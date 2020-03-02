package com.d3t.citybuilder;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class Zone {

	public static final Material INVALID_ZONE_CONFIGURATION_BORDER = Material.OBSIDIAN;
	public static final int BUILT_DETECTION_THRESHOLD = 8;
	
	public City city;
	public World world;
	public ChunkPosition pos;
	public ZoneType zoneType;
	private ZoneDensity density;
	public int heightLimit;
	public int extraData;
	public boolean[] neighborRoadmap = new boolean[4];
	
	public int averageTerrainLevel = 64;
	
	public ConstructionData building;
	
	public Zone(World w, int x, int z, City c) {
		city = c;
		world = w;
		pos = new ChunkPosition(x,z);
		zoneType = ZoneType.UNZONED;
		density = ZoneDensity.Low;
		extraData = 0;
		updateSurfaceInformation();
		neighborRoadmap = getNeighborRoadmap();
	}
	
	public Zone(City c, World w, ChunkPosition cp, ZoneType zone, ZoneDensity dens, int hlimit, int extra, int avgterrain, ConstructionData cons) {
		city = c;
		world = w;
		pos = cp;
		zoneType = zone;
		density = dens;
		heightLimit = hlimit;
		extraData = extra;
		averageTerrainLevel = avgterrain;
		building = cons;
	}
	
	public void reZone(ZoneType zt, ZoneDensity zd) {
		neighborRoadmap = getNeighborRoadmap();
		zoneType = zt;
		density = zd;
		build(null, Orientation.SOUTH, false, true);
	}
	
	public ZoneDensity getDensity() {
		return density;
	}
	
	public void setDensity(ZoneDensity d) {
		density = d;
		int floors = 0;
		if(d == ZoneDensity.Low) {
			floors = 2;
		} else if(d == ZoneDensity.Medium) {
			floors = 6;
		} else {
			floors = 24;
		}
		heightLimit = (byte)(floors*4);
	}
	
	public int[] updateSurfaceInformation() {
		int[] heights = new int[256];
		for(int x = 0; x < 16; x++) {
			for(int z = 0; z < 16; z++) {
				heights[x+z*16] = world.getHighestBlockYAt(pos.getBlockX()+x, pos.getBlockZ()+z);
			}
		}
		Arrays.sort(heights);
		//averageTerrainLevel = heights[128];
		averageTerrainLevel = 64;
		return heights;
	}
	
	public void build(Structure s, Orientation orientation, boolean force, boolean updateNeighbors) {
		//if(!force && isBuilt()) return;
		if(zoneType != ZoneType.TRANSPORT) {
			if(s == null) {
				makeOutline();
			} else {
				s.build(this, orientation);
				building = new ConstructionData(s.structureName, orientation);
			}
		} else {
			if(s == null) {
				buildRoad();
			} else {
				s.build(this, Orientation.NONE);
				building = new ConstructionData(s.structureName, Orientation.NONE);
			}
		}
		if(updateNeighbors) {
			for(Zone z : city.getNeighborZones(pos.x, pos.z)) {
				if(z != null) z.onNeighborUpdate(pos.x, pos.z);
			}
		}
	}
	
	private void buildRoad() {
		String conn = "";
		for(int i = 0; i < 4; i++) {
			conn += neighborRoadmap[i] ? "1" : "0";
		}
		TrafficStructurePieces collection = StructureLibrary.trafficStructures.get("road1");
		if(collection != null) {
			build(collection.getStructureForChunk(conn), Orientation.NONE, true, false);
		} else {
			System.out.println("collection road1 does not exist!");
		}
	}
	
	private void onNeighborUpdate(int updateFromX, int updateFromZ) {
		if(zoneType == ZoneType.TRANSPORT) {
			boolean[] newNRM = getNeighborRoadmap();
			//if(!Arrays.equals(neighborRoadmap, newNRM)) {
			neighborRoadmap = newNRM;
			buildRoad();
			/*} else {
				System.out.println("not hoi");
			}*/
		}
	}
	
	private boolean isBuilt() {
		//check for any construction inside the zone
		int nonNaturalSurfaceBlocks = 0;
		int[] heights = updateSurfaceInformation();
		for(int x = 0; x < 15; x++) {
			for(int z = 0; z < 15; z++) {
				Block b = getBlock(x, heights[x+z*16], z);
				if(!BlockCategories.isNaturalBlock(b.getType())) nonNaturalSurfaceBlocks++; 
			}
		}
		if(nonNaturalSurfaceBlocks >= BUILT_DETECTION_THRESHOLD) return true;
		return false;
	}
	
	private void makeOutline() {
		Material m = getZoneBorderBlock(zoneType, density);
		if(m == null) return;
		for(int i = 0; i < 16; i++) {
			int xn = pos.getBlockX();
			int xp = pos.getBlockX()+15;
			int zn = pos.getBlockZ();
			int zp = pos.getBlockZ()+15;
			world.getBlockAt(xn+i, world.getHighestBlockYAt(xn+i, zn)-1, zn).setType(m);
			world.getBlockAt(xn+i, world.getHighestBlockYAt(xn+i, zp)-1, zp).setType(m);
			world.getBlockAt(xn, world.getHighestBlockYAt(xn, zn+i)-1, zn+i).setType(m);
			world.getBlockAt(xp, world.getHighestBlockYAt(xp, zn+i)-1, zn+i).setType(m);
		}
		System.out.println("outline built!");
	}
	
	public Block getBlock(int x, int y, int z) {
		return world.getBlockAt(pos.x*16+x, y, pos.z*16+z);
	}
	
	public void setBlock(int x, int y, int z, Material m) {
		getBlock(x,y,z).setType(m);
	}
	
	public static Material getZoneBorderBlock(ZoneType t, ZoneDensity d) {
		if(t == ZoneType.OUT_OF_BOUNDS || t == ZoneType.UNZONED) {
			return null;
		} else if(t == ZoneType.ADMINISTRATION) {
			return Material.BEDROCK;
		} else if(t == ZoneType.DO_NOT_BUILD) {
			return Material.BLACK_CONCRETE;
		} else if(t == ZoneType.TRANSPORT) {
			return Material.ANDESITE;
		} else if(t == ZoneType.Residental) {
			if(d == ZoneDensity.Low) {
				return Material.LIME_CONCRETE;
			} else if(d == ZoneDensity.Medium) {
				return Material.GREEN_CONCRETE;
			} else {
				return Material.BROWN_CONCRETE;
			}
		} else if(t == ZoneType.Retail) {
			if(d == ZoneDensity.Low) {
				return Material.LIGHT_BLUE_CONCRETE;
			} else {
				return Material.BLUE_CONCRETE;
			}
		} else if(t == ZoneType.Industrial) {
			if(d == ZoneDensity.Low) {
				return Material.YELLOW_CONCRETE;
			} else if(d == ZoneDensity.Medium) {
				return Material.ORANGE_CONCRETE;
			} else {
				return Material.RED_CONCRETE;
			}
		} else if(t == ZoneType.Office) {
			if(d == ZoneDensity.Low) {
				return INVALID_ZONE_CONFIGURATION_BORDER;
			} else if(d == ZoneDensity.Medium) {
				return Material.MAGENTA_CONCRETE;
			} else {
				return Material.PURPLE_CONCRETE;
			}
		} else if(t == ZoneType.Park) {
			return Material.WHITE_CONCRETE;
		} else if(t == ZoneType.Public) {
			return Material.LIGHT_GRAY_CONCRETE;
		} else if(t == ZoneType.TransportFacilites) {
			return Material.GRAY_CONCRETE;
		} else if(t == ZoneType.Predefined) {
			return Material.CYAN_CONCRETE;
		} else if(t == ZoneType.Special) {
			return Material.PINK_CONCRETE;
		}
		return INVALID_ZONE_CONFIGURATION_BORDER;
	}
	
	private boolean[] getNeighborRoadmap() {
		boolean[] b = new boolean[4];
		Zone[] neighbors = city.getNeighborZones(pos.x, pos.z);
		for(int i = 0; i < 4; i++) {
			if(neighbors[i] != null) b[i] = (neighbors[i].zoneType == ZoneType.TRANSPORT);
		}
		return b;
	}
	
	public String getSaveString() {
		String str = "";
		str += zoneType.toString();
		str += "@";
		str += density.toString();
		str += ",";
		str += heightLimit;
		str += ",";
		str += extraData;
		str += ",";
		str += averageTerrainLevel;
		str += ",";
		if(building != null) {
			str += building.getSaveString();
		} else {
			str += "null";
		}
		return str;
	}
	
	public static Zone loadFromSaveData(World w, String fulldata, City c) {
		try {
		String[] data = fulldata.split(" ");
		ChunkPosition pos = new ChunkPosition(Integer.parseInt(data[0]));
		String[] split = data[1].split(",");
		String[] zoneSplit = split[0].split("@");
		ZoneType zonetype = ZoneType.valueOf(zoneSplit[0]);
		ZoneDensity zonedensity = ZoneDensity.valueOf(zoneSplit[1]);
		int heightlimit = Integer.parseInt(split[1]);
		byte extradata = Byte.parseByte(split[2]);
		int averageterrain = Integer.parseInt(split[3]);
		ConstructionData building = ConstructionData.loadFromSaveString(split[4]);
		return new Zone(c, w, pos, zonetype, zonedensity, heightlimit, extradata, averageterrain, building);
		}
		catch(Exception e) {
			System.out.println("Failed to load Zone from save data:");
			System.out.println(fulldata);
			e.printStackTrace();
			return null;
		}
	}
}
