package com.d3t.citybuilder.cities;

public class CityStatistics {
	
	public int ageInDays;
	public int population;
	
	public long moneyBalance;
	
	public String getSaveString() {
		String s = "STATS:";
		return s;
		//TODO use new file system
	}
	
	public void setStartValues() {
		moneyBalance = 50000;
	}
}
