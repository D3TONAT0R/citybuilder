package com.d3t.citybuilder.framework;

import org.bukkit.Location;

import com.d3t.citybuilder.cities.CityAreaHandler;

public class ChunkPosition {
	
	public int x;
	public int z;
	
	public ChunkPosition(int x, int z) {
		this.x = x;
		this.z = z;
	}
	
	public ChunkPosition(int index) {
		int r = CityAreaHandler.chunkIndexRange;
		this.z = (int)(Math.floor(index/r/2))-r;
		this.x = (index % (r*2))-r;
	}
	
	public ChunkPosition(Location loc) {
		this.x = (int)Math.floor(loc.getBlockX()/16f);
		this.z = (int)Math.floor(loc.getBlockZ()/16f);
	}
	
	public int getIndex() {
		return CityAreaHandler.chunkPosToIndex(x, z);
	}
	
	public int getBlockX() {
		return x*16;
	}
	
	public int getBlockZ() {
		return z*16;
	}
}
