package com.d3t.citybuilder.structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import com.d3t.citybuilder.framework.CBMain;
import com.d3t.citybuilder.zones.ZoneDensity;
import com.d3t.citybuilder.zones.ZoneType;

public class StructureLibrary {

	public static HashMap<String, Structure> allStructures;
	public static HashMap<String, HashMap<String, Structure>> categories;
	public static HashMap<Byte, HashMap<String, Structure>> perZoneStructures;
	public static HashMap<String, TrafficStructurePieces> trafficStructures;

	public static void initialize() {
		allStructures = new HashMap<String, Structure>();
		categories = new HashMap<String, HashMap<String, Structure>>();
		perZoneStructures = new HashMap<Byte, HashMap<String, Structure>>();
		trafficStructures = new HashMap<String, TrafficStructurePieces>();
	}
	
	public static void registerStructure(Structure s, String cat, ZoneType zone, ZoneDensity density) {
		if(allStructures.containsKey(s.structureName)) {
			CBMain.log.warning("Structure named '"+s.structureName+"' is already registered!");
			return;
		}
		allStructures.put(s.structureName, s);
		if (cat != null && cat.length() > 1) {
			registerStructureInCategory(cat, s);
		}
		byte id = getZoneTypeId(zone, density);
		if(id > 0) {
			registerZonedStructure(s, id);
			CBMain.log.info("Registered structure '"+s.structureName+"' for Zone "+zone.name()+"@"+density.name());
		}
	}

	private static void registerStructureInCategory(String cat, Structure s) {
		if (cat.startsWith("#")) {
			String catString = cat.replace("#", "").toLowerCase();
			if (!trafficStructures.containsKey(catString)) {
				trafficStructures.put(catString, new TrafficStructurePieces());
			}
			CBMain.log.info("registered structure to trafficstructurecollection!");
			registerTrafficStructure(trafficStructures.get(catString), s.structureName, s);
		}
		if (!categories.containsKey(cat)) {
			categories.put(cat, new HashMap<String, Structure>());
			CBMain.log.info("Created new structure category: "+cat);
		}
		categories.get(cat).put(s.structureName, s);
	}
	
	private static void registerZonedStructure(Structure s, byte id) {
		if(!perZoneStructures.containsKey(id)) {
			perZoneStructures.put(id, new HashMap<String, Structure>());
		}
		perZoneStructures.get(id).put(s.structureName, s);
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
	
	private static byte getZoneTypeId(ZoneType zone, ZoneDensity density) {
		byte z = 0;
		if(zone == ZoneType.Residental) z = 10;
		else if(zone == ZoneType.Retail) z = 20;
		else if(zone == ZoneType.Office) z = 30;
		else if(zone == ZoneType.Industrial) z = 40;
		else if(zone == ZoneType.Park) z = 50;
		else if(zone == ZoneType.Public) z = 51;
		else if(zone == ZoneType.TransportFacilites) z = 52;
		else z = 0;
		if(z > 0 && z < 50) {
			if(density == ZoneDensity.Medium) z += 1;
			else if(density == ZoneDensity.High) z += 2;
			else if(density == ZoneDensity.Custom) z += 3;
		}
		return z;
	}
	
	public static Structure getStructureForZone(ZoneType zone, ZoneDensity density) {
		Random r = new Random();
		byte id = getZoneTypeId(zone, density);
		if(id <= 0) {
			CBMain.log.warning("The zone type "+zone.name()+" isn't available in the Structure Library!");
			return null;
		}
		if(perZoneStructures.containsKey(id)) {
			HashMap<String, Structure> map = perZoneStructures.get(id);
			ArrayList<String> keyArray = new ArrayList<String>(map.keySet());
			return map.get(keyArray.get(r.nextInt(keyArray.size())));
		} else {
			return null;
		}
	}
}
