package com.d3t.citybuilder;

public class CityAreaHandler {

	public static final int chunkIndexRange = 1024;
	
	
	public static int chunkPosToIndex(int x, int z) {
		int r = chunkIndexRange;
		int ix = x+r;
		int iz = z+r;
		return ix+r*2*iz;
	}
}
