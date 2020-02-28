package com.d3t.citybuilder;

import org.bukkit.Location;

public class ChunkPosition {
	
	public int x;
	public int z;
	
	public ChunkPosition(int x, int z) {
		this.x = x;
		this.z = z;
	}
	
	public ChunkPosition(int index) {
		int r = CityAreaHandler.chunkIndexRange;
		this.z = (int)(Math.floor(index/r*2));
		this.x = index-r*z*2;
		this.z -= r;
		this.x -= r;
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
