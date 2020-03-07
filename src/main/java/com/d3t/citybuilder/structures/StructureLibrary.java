package com.d3t.citybuilder.structures;

import java.util.HashMap;

public class StructureLibrary {

	public static HashMap<String, Structure> allStructures;
	public static HashMap<String, HashMap<String, Structure>> categories;
	public static HashMap<String, TrafficStructurePieces> trafficStructures;

	public static void registerStructure(Structure s, String cat) {
		allStructures.put(s.structureName, s);
		if (cat != null && cat.length() > 1) {
			registerStructureInCategory(cat, s);
		}
	}

	private static void registerStructureInCategory(String cat, Structure s) {
		if (cat.startsWith("#")) {
			String catString = cat.replace("#", "").toLowerCase();
			if (!trafficStructures.containsKey(catString)) {
				trafficStructures.put(catString, new TrafficStructurePieces());
			}
			System.out.println("registered structure to trafficstructurecollection!");
			registerTrafficStructure(trafficStructures.get(catString), s.structureName, s);
		}
		if (!categories.containsKey(cat)) {
			categories.put(cat, new HashMap<String, Structure>());
		}
		categories.get(cat).put(s.structureName, s);
	}
	
	private static void registerTrafficStructure(TrafficStructurePieces collection, String name, Structure s) {
		name = name.replace("#", "").toLowerCase();
		switch(name) {
		case "dot": collection.dot = s; break;
		case "end_n": collection.end_N = s; break;
		case "end_e": collection.end_E = s; break;
		case "end_s": collection.end_S = s; break;
		case "end_w": collection.end_W = s; break;
		case "straight_sn":
		case "straight_ns": collection.straight_NS = s; break;
		case "straight_ew": 
		case "straight_we": collection.straight_WE = s; break;
		case "curve_ne": collection.curve_NE = s; break;
		case "curve_es": collection.curve_ES = s; break;
		case "curve_sw": collection.curve_SW = s; break;
		case "curve_wn": collection.curve_WN = s; break;
		case "cross_t_n": collection.crossT_N = s; break;
		case "cross_t_e": collection.crossT_E = s; break;
		case "cross_t_s": collection.crossT_S = s; break;
		case "cross_t_w": collection.crossT_W = s; break;
		case "cross_x": collection.crossX = s; break;
		default: System.out.println("Registering traffic structure failed: "+name);
		}
	}
}
