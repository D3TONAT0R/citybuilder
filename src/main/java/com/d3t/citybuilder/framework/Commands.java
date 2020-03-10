package com.d3t.citybuilder.framework;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.d3t.citybuilder.cities.City;
import com.d3t.citybuilder.io.CitySaveUtil;
import com.d3t.citybuilder.io.StructureSaveUtil;
import com.d3t.citybuilder.structures.Orientation;
import com.d3t.citybuilder.structures.Structure;
import com.d3t.citybuilder.structures.StructureFactory;
import com.d3t.citybuilder.structures.StructureLibrary;
import com.d3t.citybuilder.zones.Zone;

public class Commands {

	private Server server;

	public Commands(Server server) {
		this.server = server;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		String c = cmd.getName();
		Player p = null;
		if (sender instanceof Player) {
			p = (Player) sender;
		}
		if (c.equalsIgnoreCase("savecities")) {
			CitySaveUtil.saveCities();
			if(CitySaveUtil.successfullySavedCities > 0) {
				p.sendMessage("§aSaved "+CitySaveUtil.successfullySavedCities+" cities");
			}
			if(CitySaveUtil.failedToSaveCities > 0) {
				p.sendMessage("§c"+CitySaveUtil.failedToSaveCities+" cities failed to save");
			}
			if(CitySaveUtil.successfullySavedCities == 0 && CitySaveUtil.failedToSaveCities == 0) {
				p.sendMessage("§6Warning: No cities were saved!");
			}
			return true;
		} else if (c.equalsIgnoreCase("loadcities")) {
			CitySaveUtil.loadCities();
			if(CitySaveUtil.successfullyLoadedCities > 0) {
				p.sendMessage("§aLoaded "+CitySaveUtil.successfullyLoadedCities+" cities");
			}
			if(CitySaveUtil.failedToSaveCities > 0) {
				p.sendMessage("§c"+CitySaveUtil.failedToLoadCities+" cities failed to load");
			}
			if(CitySaveUtil.successfullyLoadedCities == 0 && CitySaveUtil.failedToLoadCities == 0) {
				p.sendMessage("§6Warning: No cities were loaded!");
			}
			return true;
		}
		if (p == null) return false;
		if (c.equalsIgnoreCase("foundcity")) {
			if (args.length > 0) {
				CBMain.createCity(p, args[0], p.getWorld(), p.getLocation().getBlockX() / 16,
						p.getLocation().getBlockZ() / 16);
				return true;
			}
		} else if (c.equalsIgnoreCase("updatecity")) {
			CBMain.updateCities();
			p.sendMessage("Update done!");
			return true;
		} else if (c.equalsIgnoreCase("opencityeditor")) {
			CBMain.openEditorForPlayer((Player) sender);
			p.sendMessage("Here you go!");
			return true;
		} else if (c.equalsIgnoreCase("createstructure")) {
			if(args.length > 0) {
				boolean infoline = true;
				String category = "";
				if(args.length > 1) category = args[1];
				if(args.length > 2 && args[2].equalsIgnoreCase("-ignoreinfoline")) infoline = false;;
				StructureFactory.onBeginCreateNewStructure(p, args[0], category, infoline);
				return true;
			}
		} else if (c.equalsIgnoreCase("buildstructure")) {
			if(args.length > 0) {
				City city = CBMain.currentlyEditingCity.get(p);
				if(city == null) city = CBMain.findClosestCity(p);
				if(city != null) {
					Orientation orientation = Orientation.SOUTH;
					if(args.length > 1) orientation = Orientation.fromString(args[1]);
					boolean result = city.buildStructureAtChunk(args[0], new ChunkPosition(p.getLocation()), orientation, true, false);
					if(!result) p.sendMessage("Failed to build structure! See log for details.");
				} else {
					p.sendMessage("There is no city around to place the building!");
				}
				return true;
			}
		} else if (c.equalsIgnoreCase("reloadstructurefiles")) {
			StructureSaveUtil.loadSavedStructures();
			int success = StructureSaveUtil.successfullyLoadedFiles;
			int fail = StructureSaveUtil.failedToLoadFiles;
			if(success > 0) {
				p.sendMessage("§aLoaded "+success+" structures");
			}
			if(fail > 0) {
				p.sendMessage("§c"+fail+" structures failed to load");
			}
			if(success == 0 && fail == 0) {
				p.sendMessage("§6Warning: No structures were loaded!");
			}
			return true;
		} else if (c.equalsIgnoreCase("verifystructure")) {
			if(args.length > 0) {
				Structure s = StructureLibrary.allStructures.get(args[0]);
				if(s != null) {
					boolean b = s.verifyIntegrity();
					if(b) {
						p.sendMessage("§aStructure integrity is OK");
					} else {
						p.sendMessage("§cStructure integrity detected errorneous data");
					}
				} else {
					p.sendMessage("§cStructure "+args[0]+" not found");
				}
				return true;
			}
		} else if(c.equalsIgnoreCase("zoneinfo")) {
			sender.sendMessage("----------------");
			ChunkPosition cp = new ChunkPosition(p.getLocation());
			sender.sendMessage("Chunk #"+cp.getIndex());
			for(City city : CBMain.cities.values()) {
				if(city.world == p.getWorld()) {
					Zone z = city.chunks.get(cp.getIndex());
					if(z != null) {
						sender.sendMessage("---");
						sender.sendMessage("§6Zone in "+city.cityName);
						sender.sendMessage(String.format("POS: x%s y%s, index %s", cp.x, cp.z, cp.getIndex()));
						sender.sendMessage(String.format("Zone: %s @ %s", z.zoneType, z.getDensity()));
						sender.sendMessage(String.format("Building: %s", z.building != null ? z.building.getSaveString() : "none"));
					}
				}
			}
			return true;
		}
		return false;
	}
}
