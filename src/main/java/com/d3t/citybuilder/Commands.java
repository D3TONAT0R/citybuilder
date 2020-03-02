package com.d3t.citybuilder;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
		if (p == null) return false;
		if (c.equalsIgnoreCase("foundcity")) {
			if (args.length > 0) {
				CBMain.createCity(args[0], p.getName(), p.getWorld(), p.getLocation().getBlockX() / 16,
						p.getLocation().getBlockZ() / 16);
				p.sendMessage("NEW CITY: " + args[0]);
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
					boolean result = city.buildStructureAtChunk(args[0], new ChunkPosition(p.getLocation()), orientation, true);
					if(!result) p.sendMessage("Failed to build structure! See log for details.");
				} else {
					p.sendMessage("There is no city around to place the building!");
				}
				return true;
			}
		} else if (c.equalsIgnoreCase("reloadstructurefiles")) {
			StructureLibrary.loadSavedStructures();
			int success = StructureLibrary.successfullyLoadedFiles;
			int fail = StructureLibrary.failedToLoadFiles;
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
		} else if (c.equalsIgnoreCase("savecities")) {
			SaveHandler.saveCities();
			if(SaveHandler.successfullySavedCities > 0) {
				p.sendMessage("§aSaved "+SaveHandler.successfullySavedCities+" cities");
			}
			if(SaveHandler.failedToSaveCities > 0) {
				p.sendMessage("§c"+SaveHandler.failedToSaveCities+" cities failed to save");
			}
			if(SaveHandler.successfullySavedCities == 0 && SaveHandler.failedToSaveCities == 0) {
				p.sendMessage("§6Warning: No cities were saved!");
			}
			return true;
		} else if (c.equalsIgnoreCase("loadcities")) {
			SaveHandler.loadCities();
			if(SaveHandler.successfullyLoadedCities > 0) {
				p.sendMessage("§aLoaded "+SaveHandler.successfullyLoadedCities+" cities");
			}
			if(SaveHandler.failedToSaveCities > 0) {
				p.sendMessage("§c"+SaveHandler.failedToLoadCities+" cities failed to load");
			}
			if(SaveHandler.successfullyLoadedCities == 0 && SaveHandler.failedToLoadCities == 0) {
				p.sendMessage("§6Warning: No cities were loaded!");
			}
			return true;
		}
		return false;
	}
}
