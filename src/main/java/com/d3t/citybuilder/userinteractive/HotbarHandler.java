package com.d3t.citybuilder.userinteractive;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

public class HotbarHandler {

	public static final int[] mode0items = new int[] { 0, 0, 0, 0, 0, 0, 0, 0,
			ItemClickEvents.OPEN_EDITOR.getOrdinal() };
	public static final int[] mode1items = new int[] { ItemClickEvents.ZONE_TOOL.getOrdinal(),
			ItemClickEvents.BUILD_TOOL.getOrdinal(), ItemClickEvents.BULLDOZE_TOOL.getOrdinal(),
			ItemClickEvents.PROTECT_TOOL.getOrdinal(), ItemClickEvents.INFORMATION_TOOL.getOrdinal(), 0, 0, 0,
			ItemClickEvents.BACK.getOrdinal() };
	public static final int[] zoneModeItems = new int[] { ItemClickEvents.ZONETYPE_NONE.getOrdinal(),
			ItemClickEvents.ZONETYPE_RESIDENTAL.getOrdinal(), ItemClickEvents.ZONETYPE_RETAIL.getOrdinal(), ItemClickEvents.ZONETYPE_OFFICE.getOrdinal(), ItemClickEvents.ZONETYPE_INDUSTRIAL.getOrdinal(), ItemClickEvents.ZONETYPE_TRANSPORT.getOrdinal(), ItemClickEvents.ZONETYPE_NONSTANDARD.getOrdinal(), 0, ItemClickEvents.BACK.getOrdinal() };
	public static final int[] resZoneItems = new int[] { ItemClickEvents.ZONETYPE_RESIDENTAL_LOW.getOrdinal(), ItemClickEvents.ZONETYPE_RESIDENTAL_MED.getOrdinal(), ItemClickEvents.ZONETYPE_RESIDENTAL_HIGH.getOrdinal(), ItemClickEvents.ZONETYPE_RESIDENTAL_CUSTOM.getOrdinal(), 0, 0, 0, 0, ItemClickEvents.BACK.getOrdinal() };
	public static final int[] retZoneItems = new int[] { ItemClickEvents.ZONETYPE_RETAIL_LOW.getOrdinal(), ItemClickEvents.ZONETYPE_RETAIL_MED.getOrdinal(), ItemClickEvents.ZONETYPE_RETAIL_CUSTOM.getOrdinal(), 0, 0, 0, 0, 0, ItemClickEvents.BACK.getOrdinal() };
	public static final int[] officeZoneItems = new int[] { ItemClickEvents.ZONETYPE_OFFICE_MED.getOrdinal(), ItemClickEvents.ZONETYPE_OFFICE_HIGH.getOrdinal(), ItemClickEvents.ZONETYPE_RETAIL_CUSTOM.getOrdinal(), 0, 0, 0, 0, 0, ItemClickEvents.BACK.getOrdinal() };
	public static final int[] indZoneItems = new int[] { ItemClickEvents.ZONETYPE_INDUSTRIAL_FARMLAND.getOrdinal(), ItemClickEvents.ZONETYPE_INDUSTRIAL_MED.getOrdinal(), ItemClickEvents.ZONETYPE_INDUSTRIAL_HIGH.getOrdinal(), ItemClickEvents.ZONETYPE_INDUSTRIAL_CUSTOM.getOrdinal(), 0, 0, 0, 0, ItemClickEvents.BACK.getOrdinal() };
	public static final int[] otherZoneItems = new int[] { ItemClickEvents.ZONETYPE_PREDEFINED.getOrdinal(), ItemClickEvents.ZONETYPE_PUBLIC.getOrdinal(), ItemClickEvents.ZONETYPE_TRANSPORT_FACILITIES.getOrdinal(), ItemClickEvents.ZONETYPE_ADMINISTRATION.getOrdinal(), ItemClickEvents.ZONETYPE_NOBUILD.getOrdinal(), 0, 0, 0, ItemClickEvents.BACK.getOrdinal() };

	public static void setHotbarItemsForMode(Player p, int mode) {
		int[] items = new int[9];
		if (mode == 0) {
			items = mode0items;
		} else if (mode == 1) {
			items = mode1items;
		} else if (mode == 2) {
			items = zoneModeItems;
		} else if (mode == 10) {
			items = resZoneItems;
		} else if (mode == 11) {
			items = retZoneItems;
		} else if (mode == 12) {
			items = officeZoneItems;
		} else if (mode == 13) {
			items = indZoneItems;
		} else if (mode == 14) {
			items = otherZoneItems;
		}
		for (int i = 0; i < items.length; i++) {
			if(isSignificantItem(p.getInventory().getItem(i))) {
				if(!transferSlotToInventory(p.getInventory(), i)) {
					p.getWorld().dropItem(p.getLocation(), p.getInventory().getItem(i));
					p.sendMessage("§cYour "+p.getInventory().getItem(i).getType().toString()+" was dropped!");
				}
			}
			if (items[i] <= 0) {
				p.getInventory().setItem(i, new ItemStack(Material.AIR));
			} else {
				ItemStack stack = new ItemStack(ClickEventHandler.EVENT_ITEM, 1);
				ItemMeta meta = stack.getItemMeta();
				meta.setCustomModelData(items[i]);
				meta.setDisplayName(getItemNames(items[i]));
				stack.setItemMeta(meta);
				p.getInventory().setItem(i, stack);
			}
		}
		p.getInventory().setHeldItemSlot(0);
	}
	
	public static boolean isSignificantItem(ItemStack stack) {
		if(stack == null) return false;
		Material mat = stack.getType();
		if(mat == Material.FILLED_MAP || mat == Material.WRITABLE_BOOK || mat == Material.WRITTEN_BOOK) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean transferSlotToInventory(PlayerInventory inv, int slotNum) {
		if(slotNum >= 9) return false;
		ItemStack stack = inv.getItem(slotNum);
		for(int i = 9; i < 36; i++) {
			if(inv.getItem(i) == null) {
				inv.setItem(slotNum, new ItemStack(Material.AIR));
				inv.setItem(i, stack);
				return true;
			}
		}
		return false;
	}

	public static String getItemNames(int data) {
		if (data == ItemClickEvents.OPEN_EDITOR.getOrdinal()) {
			return "Editor Öffnen";
		} else if (data == ItemClickEvents.ZONE_TOOL.getOrdinal()) {
			return "Zonen bearbeiten";
		} else if (data == ItemClickEvents.BUILD_TOOL.getOrdinal()) {
			return "Bauwerkzeug";
		} else if (data == ItemClickEvents.BULLDOZE_TOOL.getOrdinal()) {
			return "Abreissen";
		} else if (data == ItemClickEvents.PROTECT_TOOL.getOrdinal()) {
			return "Chunk schützen";
		} else if (data == ItemClickEvents.INFORMATION_TOOL.getOrdinal()) {
			return "Info-Abfrage";
		} else if (data == ItemClickEvents.CONFIRM_ACTION.getOrdinal()) {
			return "Bestätigen";
		} else if (data == ItemClickEvents.CANCEL_ACTION.getOrdinal()) {
			return "Abbrechen";
		} else if (data == ItemClickEvents.BACK.getOrdinal()) {
			return "Zurück";
		} else if (data == ItemClickEvents.ZONETYPE_NONE.getOrdinal()) {
			return "Zone aufheben";
		} else if (data == ItemClickEvents.ZONETYPE_NOBUILD.getOrdinal()) {
			return "Bauverbot";
		} else if (data == ItemClickEvents.ZONETYPE_TRANSPORT.getOrdinal()) {
			return "Strassen";
		} else if (data == ItemClickEvents.ZONETYPE_RESIDENTAL.getOrdinal()) {
			return "Wohnzone";
		} else if (data == ItemClickEvents.ZONETYPE_RESIDENTAL_LOW.getOrdinal()) {
			return "Wohnzone (leicht bebaut)";
		} else if (data == ItemClickEvents.ZONETYPE_RESIDENTAL_MED.getOrdinal()) {
			return "Wohnzone (mittel bebaut)";
		} else if (data == ItemClickEvents.ZONETYPE_RESIDENTAL_HIGH.getOrdinal()) {
			return "Wohnzone (stark bebaut)";
		} else if (data == ItemClickEvents.ZONETYPE_RESIDENTAL_CUSTOM.getOrdinal()) {
			return "Wohnzone (benutzerdefiniert)";
		} else if (data == ItemClickEvents.ZONETYPE_RETAIL.getOrdinal()) {
			return "Gewerbezone";
		} else if (data == ItemClickEvents.ZONETYPE_RETAIL_LOW.getOrdinal()) {
			return "Gewerbezone (leicht bebaut)";
		} else if (data == ItemClickEvents.ZONETYPE_RETAIL_MED.getOrdinal()) {
			return "Gewerbezone (mittel bebaut)";
		} else if (data == ItemClickEvents.ZONETYPE_RETAIL_CUSTOM.getOrdinal()) {
			return "Gewerbezone (benutzerdefiniert)";
		} else if (data == ItemClickEvents.ZONETYPE_OFFICE.getOrdinal()) {
			return "Bürozone";
		} else if (data == ItemClickEvents.ZONETYPE_OFFICE_MED.getOrdinal()) {
			return "Bürozone (normal bebaut)";
		} else if (data == ItemClickEvents.ZONETYPE_OFFICE_HIGH.getOrdinal()) {
			return "Bürozone (stark bebaut)";
		} else if (data == ItemClickEvents.ZONETYPE_OFFICE_CUSTOM.getOrdinal()) {
			return "Bürozone (benutzerdefiniert)";
		} else if (data == ItemClickEvents.ZONETYPE_INDUSTRIAL.getOrdinal()) {
			return "Industriezone";
		} else if (data == ItemClickEvents.ZONETYPE_INDUSTRIAL_FARMLAND.getOrdinal()) {
			return "Industriezone (Landwirtschaft)";
		} else if (data == ItemClickEvents.ZONETYPE_INDUSTRIAL_MED.getOrdinal()) {
			return "Industriezone (normal bebaut)";
		} else if (data == ItemClickEvents.ZONETYPE_INDUSTRIAL_HIGH.getOrdinal()) {
			return "Industriezone (schwer bebaut)";
		} else if (data == ItemClickEvents.ZONETYPE_INDUSTRIAL_CUSTOM.getOrdinal()) {
			return "Industriezone (benutzerdefiniert)";
		} else if (data == ItemClickEvents.ZONETYPE_NONSTANDARD.getOrdinal()) {
			return "Sonstige Zonen...";
		} else if (data == ItemClickEvents.ZONETYPE_PARK.getOrdinal()) {
			return "Park";
		} else if (data == ItemClickEvents.ZONETYPE_ADMINISTRATION.getOrdinal()) {
			return "Adminstrativ";
		} else if (data == ItemClickEvents.ZONETYPE_PREDEFINED.getOrdinal()) {
			return "Benutzerdefinierte Zone";
		} else if (data == ItemClickEvents.ZONETYPE_SPECIAL.getOrdinal()) {
			return "Sonderbauten";
		} else if (data == ItemClickEvents.ZONETYPE_PUBLIC.getOrdinal()) {
			return "Öffentliche Bauten";
		} else if (data == ItemClickEvents.ZONETYPE_TRANSPORT_FACILITIES.getOrdinal()) {
			return "Transportanlagen";
		}
		return null;
	}
}
