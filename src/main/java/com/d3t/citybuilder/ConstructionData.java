package com.d3t.citybuilder;

public class ConstructionData {
	
	public String structureName;
	
	public Orientation orientation;
	
	public float constructionProgress = 1f;
	
	public ConstructionData(String structure, Orientation orient) {
		structureName = structure;
		orientation = orient;
	}
	
	public String getSaveString() {
		return structureName+"@"+orientation;
	}
	
	public static ConstructionData loadFromSaveString(String data) {
		String[] dataSplit = data.split("@");
		return new ConstructionData(dataSplit[0], Orientation.valueOf(dataSplit[1]));
	}
}
