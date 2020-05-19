package com.d3t.citybuilder.zones;

import java.util.Random;

import com.d3t.citybuilder.util.RealEstateType;

public class RealEstateData {
	
	public static final String saveStrSeparator = "|";
	
	public RealEstateType type;
	public int space;
	
	public int residentsOrWorkplaces;
	
	public RealEstateData(String loadStr) {
		String[] split = loadStr.split(saveStrSeparator);
		type = Enum.valueOf(RealEstateType.class, split[0]);
		space = Integer.parseInt(split[1]);
		residentsOrWorkplaces = Integer.parseInt(split[2]);
	}
	
	public boolean hasTenant() {
		return residentsOrWorkplaces > 0;
	}
	
	public String getSaveString() {
		return type.name()+saveStrSeparator+space+saveStrSeparator+residentsOrWorkplaces;
	}
	
	public void populate(Random r) {
		if(type.isResidental()) residentsOrWorkplaces = 1+r.nextInt(5); //Random between 1 and 5
		else if(type == RealEstateType.RETAIL) residentsOrWorkplaces = (int)Math.ceil(space/20d); //One workplace for every 16 m2
		else if(type == RealEstateType.OFFICE) residentsOrWorkplaces = (int)Math.ceil(space/12d); //One workplace for every 12 m2
		else if(type.isIndustrial()) {
			if(type == RealEstateType.INDUSTRY_AGRICULTURE) residentsOrWorkplaces = 3+r.nextInt(4); //Random between 3 and 6
			else residentsOrWorkplaces = (int)Math.ceil(space/25d); //One workplace for every 20 m2
		} else {
			//It's a custom type. The workplaces need to be set manually
			residentsOrWorkplaces = 0;
		}
	}
	
	public void depopulate() {
		residentsOrWorkplaces = 0;
	}
}
