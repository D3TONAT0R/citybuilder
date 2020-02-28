package com.d3t.citybuilder;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ClickEventHandler implements Listener {

	public static final Material EVENT_ITEM = Material.STICK;

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerUse(PlayerInteractEvent event) {
		Player p = event.getPlayer();

		ItemStack item = p.getInventory().getItemInMainHand();
		if (item.getType() == EVENT_ITEM) {
			if (item.getItemMeta().hasCustomModelData()) {
				int id = item.getItemMeta().getCustomModelData();
				int em = CBMain.getEditModeForPlayer(event.getPlayer());
				City closestCity = CBMain.findClosestCity(p.getWorld(), p.getLocation().getBlockX() / 16, p.getLocation().getBlockZ() / 16);
				if (id == ItemClickEvents.OPEN_EDITOR.getOrdinal() && em == 0) {
					CBMain.setEditModeForPlayer(p, 1, closestCity, null);
					if (closestCity != null) {
						p.sendMessage("Entered editor for city " + closestCity.cityName);
					} else {
						p.sendMessage("There is no city around to edit!");
					}
				} else if (id == ItemClickEvents.BACK.getOrdinal()) {
					if (em == 1) {
						CBMain.setEditModeForPlayer(p, 0, null, event);
						p.sendMessage("Left editor");
					} else if (em >= 2) {
						CBMain.setEditModeForPlayer(p, 1, null, event);
					}
				} else if (id == ItemClickEvents.ZONE_TOOL.getOrdinal()) {
					CBMain.setEditModeForPlayer(p, 2, closestCity, event);
				} else if (id == ItemClickEvents.ZONETYPE_RESIDENTAL.getOrdinal()) {
					CBMain.setEditModeForPlayer(p, 10, closestCity, event);
				} else if (id == ItemClickEvents.ZONETYPE_RETAIL.getOrdinal()) {
					CBMain.setEditModeForPlayer(p, 11, closestCity, event);
				} else if (id == ItemClickEvents.ZONETYPE_OFFICE.getOrdinal()) {
					CBMain.setEditModeForPlayer(p, 12, closestCity, event);
				} else if (id == ItemClickEvents.ZONETYPE_INDUSTRIAL.getOrdinal()) {
					CBMain.setEditModeForPlayer(p, 13, closestCity, event);
				} else if (id == ItemClickEvents.ZONETYPE_NONSTANDARD.getOrdinal()) {
					CBMain.setEditModeForPlayer(p, 14, closestCity, event);
				} else if (id == ItemClickEvents.ZONETYPE_TRANSPORT.getOrdinal()) {
					setZone(p, ZoneType.TRANSPORT, ZoneDensity.Low);
				} else if (id == ItemClickEvents.ZONETYPE_RESIDENTAL_LOW.getOrdinal()) {
					setZone(p, ZoneType.Residental, ZoneDensity.Low);
				} else if (id == ItemClickEvents.ZONETYPE_RESIDENTAL_MED.getOrdinal()) {
					setZone(p, ZoneType.Residental, ZoneDensity.Medium);
				} else if (id == ItemClickEvents.ZONETYPE_RESIDENTAL_HIGH.getOrdinal()) {
					setZone(p, ZoneType.Residental, ZoneDensity.High);
				} else if (id == ItemClickEvents.ZONETYPE_RESIDENTAL_CUSTOM.getOrdinal()) {
					setZone(p, ZoneType.Residental, ZoneDensity.Custom);
				} else if (id == ItemClickEvents.ZONETYPE_RETAIL_LOW.getOrdinal()) {
					setZone(p, ZoneType.Retail, ZoneDensity.Low);
				} else if (id == ItemClickEvents.ZONETYPE_RETAIL_MED.getOrdinal()) {
					setZone(p, ZoneType.Retail, ZoneDensity.Medium);
				} else if (id == ItemClickEvents.ZONETYPE_RETAIL_CUSTOM.getOrdinal()) {
					setZone(p, ZoneType.Retail, ZoneDensity.Custom);
				} else if (id == ItemClickEvents.ZONETYPE_OFFICE_MED.getOrdinal()) {
					setZone(p, ZoneType.Office, ZoneDensity.Medium);
				} else if (id == ItemClickEvents.ZONETYPE_OFFICE_HIGH.getOrdinal()) {
					setZone(p, ZoneType.Office, ZoneDensity.High);
				} else if (id == ItemClickEvents.ZONETYPE_OFFICE_CUSTOM.getOrdinal()) {
					setZone(p, ZoneType.Office, ZoneDensity.Custom);
				} else if (id == ItemClickEvents.ZONETYPE_INDUSTRIAL_FARMLAND.getOrdinal()) {
					setZone(p, ZoneType.Industrial, ZoneDensity.Low);
				} else if (id == ItemClickEvents.ZONETYPE_INDUSTRIAL_MED.getOrdinal()) {
					setZone(p, ZoneType.Industrial, ZoneDensity.Medium);
				} else if (id == ItemClickEvents.ZONETYPE_INDUSTRIAL_HIGH.getOrdinal()) {
					setZone(p, ZoneType.Industrial, ZoneDensity.High);
				} else if (id == ItemClickEvents.ZONETYPE_NOBUILD.getOrdinal()) {
					setZone(p, ZoneType.DO_NOT_BUILD, ZoneDensity.Low);
				} else if (id == ItemClickEvents.ZONETYPE_PARK.getOrdinal()) {
					setZone(p, ZoneType.Park, ZoneDensity.Low);
				} else if (id == ItemClickEvents.ZONETYPE_PREDEFINED.getOrdinal()) {
					setZone(p, ZoneType.Predefined, ZoneDensity.Low);
				} else if (id == ItemClickEvents.ZONETYPE_PUBLIC.getOrdinal()) {
					setZone(p, ZoneType.Public, ZoneDensity.Low);
				} else if (id == ItemClickEvents.ZONETYPE_SPECIAL.getOrdinal()) {
					setZone(p, ZoneType.Special, ZoneDensity.Low);
				} else if (id == ItemClickEvents.ZONETYPE_TRANSPORT_FACILITIES.getOrdinal()) {
					setZone(p, ZoneType.TransportFacilites, ZoneDensity.Low);
				}
			}
		}
	}
	
	private void setZone(Player p, ZoneType t, ZoneDensity d) {
		City c = CBMain.currentlyEditingCity.get(p);
		if(c != null) {
			ChunkPosition pos = new ChunkPosition(p.getLocation());
			c.setZone(p, pos.x, pos.z, t, d);
		}
	}
}
