package com.d3t.citybuilder.util;

public enum RealEstateType {
	RESIDENTAL_W1,
	RESIDENTAL_W2,
	RESIDENTAL_W3,
	RESIDENTAL_W4,
	RESIDENTAL_W5,
	RETAIL,
	OFFICE,
	INDUSTRY_AGRICULTURE,
	INDUSTRY_FACTORY,
	INDUSTRY_HIGHTECH,
	CUSTOM_W_LOW,
	CUSTOM_W_MED,
	CUSTOM_W_HIGH;
	
	public boolean isResidental() {
		return this == RESIDENTAL_W1 ||
			this == RESIDENTAL_W2 ||
			this == RESIDENTAL_W3 ||
			this == RESIDENTAL_W4 ||
			this == RESIDENTAL_W5;
	}
	
	public boolean isBusiness() {
		return this == RETAIL || this == OFFICE;
	}
	
	public boolean isIndustrial() {
		return this == INDUSTRY_AGRICULTURE ||
			this == INDUSTRY_FACTORY ||
			this == INDUSTRY_HIGHTECH;
	}
	
	public boolean isCustom() {
		return this == CUSTOM_W_LOW || this == CUSTOM_W_MED || this == CUSTOM_W_HIGH;
	}
	
	public boolean canProvideWorkplaces() {
		return isIndustrial() || isBusiness();
	}
	
	public int getScaleIndex() {
		if(this == RESIDENTAL_W1) return 0;
		else if(this == RESIDENTAL_W2) return 1;
		else if(this == RESIDENTAL_W3) return 2;
		else if(this == RESIDENTAL_W4) return 3;
		else if(this == RESIDENTAL_W5) return 4;
		else if(this == INDUSTRY_AGRICULTURE) return 0;
		else if(this == INDUSTRY_FACTORY) return 1;
		else if(this == INDUSTRY_HIGHTECH) return 2;
		else if(this == CUSTOM_W_LOW) return 0;
		else if(this == CUSTOM_W_MED) return 1;
		else if(this == CUSTOM_W_HIGH) return 2;
		else return -1;
	}
	
	public String getDynmapIconName() {
		if(this == RESIDENTAL_W1) return "immo_w1";
		else if(this == RESIDENTAL_W2) return "immo_w2";
		else if(this == RESIDENTAL_W3) return "immo_w3";
		else if(this == RESIDENTAL_W4) return "immo_w4";
		else if(this == RESIDENTAL_W5) return "immo_w5";
		else if(this == RETAIL) return "immo_gs";
		else if(this == OFFICE) return "immo_go";
		else if(this == INDUSTRY_AGRICULTURE) return "immo_i";
		else if(this == INDUSTRY_FACTORY) return "immo_i";
		else if(this == INDUSTRY_HIGHTECH) return "immo_i";
		else return "immo_generic";
	}
}
