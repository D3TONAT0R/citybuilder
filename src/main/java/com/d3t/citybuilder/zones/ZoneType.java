package com.d3t.citybuilder.zones;

public enum ZoneType {
	OUT_OF_BOUNDS,
	UNZONED,
	TRANSPORT,
	DO_NOT_BUILD,
	ADMINISTRATION,
	Residental,
	Retail,
	Office,
	Industrial,
	Farmland,
	Park,
	Public,
	TransportFacilites,
	Predefined,
	Special;
	
	public String toColorString() {
		if(this == UNZONED) {
			return "§8§oKeine Zone§f";
		} else if(this == TRANSPORT) {
			return "§7Verkehr§f";
		} else if(this == DO_NOT_BUILD) {
			return "§4§oBauverbot§f";
		} else if(this == ADMINISTRATION) {
			return "§dAdministration§f";
		} else if(this == Residental) {
			return "§aWohnen§f";
		} else if(this == Retail) {
			return "§9Gewerbe§f";
		} else if(this == Office) {
			return "§dBüro§f";
		} else if(this == Industrial) {
			return "§eIndustrie§f";
		} else if(this == Farmland) {
			return "§eAgrarland§f";
		} else if(this == Park) {
			return "§bPark§f";
		} else if(this == Public) {
			return "§bÖffentliche Bauten§f";
		} else if(this == TransportFacilites) {
			return "§7Transportanlagen§f";
		} else if(this == Predefined) {
			return "§3Mischzone§f";
		} else if(this == Special) {
			return "§cSonderbauten§f";
		} else {
			return this.name();
		}
	}
	
	public boolean isAutoBuildable() {
		switch(this) {
		case Residental:
		case Retail:
		case Office:
		case Industrial:
		case Farmland:
			return true;
		default: return false;
		}
	}
}
