package com.d3t.citybuilder;

public enum Orientation {
	NONE,
	NORTH,
	EAST,
	SOUTH,
	WEST,
	NORTH_EAST,
	SOUTH_EAST,
	SOUTH_WEST,
	NORTH_WEST;
	
	public static Orientation fromString(String s) {
		if(s.equalsIgnoreCase("n") || s.equalsIgnoreCase("north")) return NORTH;
		if(s.equalsIgnoreCase("e") || s.equalsIgnoreCase("east")) return EAST;
		if(s.equalsIgnoreCase("s") || s.equalsIgnoreCase("south")) return SOUTH;
		if(s.equalsIgnoreCase("w") || s.equalsIgnoreCase("west")) return WEST;
		return NONE;
	}
}
