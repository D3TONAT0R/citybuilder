package com.d3t.citybuilder.userinteractive;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import com.d3t.citybuilder.util.RealEstateType;

public class DynmapHandler {
	
	public static DynmapAPI api;
	public static MarkerSet markerSet;
	
	private static void initialize() {
		api = (DynmapAPI)Bukkit.getServer().getPluginManager().getPlugin("Dynmap");
		markerSet = api.getMarkerAPI().getMarkerSet("immo");
	}
	
	public static void createRealEstateMarker(int realEstateID, RealEstateType type, Player owner, int x, int y, int z, int amount) {
		if(api == null) initialize();
		Marker existing = markerSet.findMarker(getMarkerID(realEstateID));
		if(existing != null) existing.deleteMarker();
		
		String amountStr;
		if(type.isResidental()) {
			if(amount > 1) {
				amountStr = amount+"x";
			} else {
				amountStr = "";
			}
		} else {
			amountStr = amount+"";
		}
		String label = "["+owner.getName()+"] "+amountStr;
		markerSet.createMarker(getMarkerID(realEstateID), label, false, owner.getWorld().getName(), x, y, z, getIcon(type), true);
	}
	
	private static String getMarkerID(int id) {
		return "marker_"+String.format("%04d", id);
	}
	
	private static MarkerIcon getIcon(RealEstateType type) {
		return api.getMarkerAPI().getMarkerIcon(type.getDynmapIconName());
	}
	
	public static void removeRealEstateMarker(int realEstateID) {
		markerSet.findMarker(getMarkerID(realEstateID)).deleteMarker();
	}
	
	public static void transferRealEstateMarkerOwnership(int realEstateID, Player newOwner) {
		Marker m = markerSet.findMarker(getMarkerID(realEstateID));
		String label = m.getLabel();
		int index = label.indexOf("]");
		label = label.substring(index+1, label.length());
		label = "["+newOwner.getName()+"]";
		m.setLabel(label);
	}
}
