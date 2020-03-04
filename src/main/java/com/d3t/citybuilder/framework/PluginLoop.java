package com.d3t.citybuilder.framework;

import com.d3t.citybuilder.cities.City;

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
		for(City c : CBMain.cities.values()) c.update();
	}
	
	private void onSecondUpdate() {
		
	}
}
