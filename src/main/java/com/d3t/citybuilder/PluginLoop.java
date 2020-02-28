package com.d3t.citybuilder;

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
		
	}
	
	private void onSecondUpdate() {
		
	}
}
