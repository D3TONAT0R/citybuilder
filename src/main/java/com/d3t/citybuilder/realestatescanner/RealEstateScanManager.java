package com.d3t.citybuilder.realestatescanner;

import org.bukkit.World;
import org.bukkit.entity.Player;

public class RealEstateScanManager {

	public void startScan(World w, int x, int y, int z, Player executor) {
		try {
			RealEstateScan scan = new RealEstateScan(w, x, y, z, false);
			scan.logAllMessages(executor);
		}
		catch(Exception e) {
			System.out.println("Scan failed!");
			e.printStackTrace();
		}
	}
	
}
