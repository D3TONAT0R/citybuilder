package com.d3t.citybuilder.realestatescanner;

import org.bukkit.World;
import org.bukkit.command.CommandSender;

public class RealEstateScanManager {

	public void startScan(World w, int x, int y, int z, CommandSender executor) {
		try {
			//RealEstateScan scan = new RealEstateScan(w, x, y, z, false);
		}
		catch(Exception e) {
			System.out.println("Scan failed!");
			e.printStackTrace();
		}
	}
	
}
