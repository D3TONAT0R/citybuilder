package com.d3t.citybuilder.framework;

import org.bukkit.entity.Player;

import com.d3t.citybuilder.userinteractive.GUIHandler;

public class PluginLoop implements Runnable {

	int second;
	int tick;
	
	public void run() {
		onTickUpdate();
		if(tick % 20 == 0) {
			onSecondUpdate();
			second++;
		}
		tick++;
	}

	private void onTickUpdate() {
		CBMain.updateCities();
		for(Player p : CBMain.getServerInstance().getOnlinePlayers()) GUIHandler.update(p);
	}
	
	private void onSecondUpdate() {
		
	}
}
