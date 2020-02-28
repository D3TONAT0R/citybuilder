package com.d3t.citybuilder;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class Zone {

	public static final Material INVALID_ZONE_CONFIGURATION_BORDER = Material.OBSIDIAN;
	public static final int BUILT_DETECTION_THRESHOLD = 8;
	
	public World world;
	public ChunkPosition pos;
	public ZoneType zoneType;
	private ZoneDensity density;
	public byte heightLimit;
	public int extraData;
	
	public int averageTerrainLevel = 64;
	
	public ConstructionData building;
	
	public Zone(World w, int x, int z) {
		world = w;
		pos = new ChunkPosition(x,z);
		zoneType = ZoneType.UNZONED;
		density = ZoneDensity.Low;
		extraData = 0;
		updateSurfaceInformation();
	}
	
	public void reZone(ZoneType zt, ZoneDensity zd) {
		zoneType = zt;
		density = zd;
		build(null, Orientation.SOUTH, false);
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
	
	public void build(Structure s, Orientation orientation, boolean force) {
		//if(!force && isBuilt()) return;
		if(s == null) {
			makeOutline();
		} else {
			s.build(this, orientation);
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
}
