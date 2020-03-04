package com.d3t.citybuilder.structures;

public enum ConstructionStage {
	IDLE,
	DEMOLITION,
	EXCAVATION,
	BASE_CONSTRUCTION,
	INTERIOR_FINISHING,
	DECORATION,
	DONE;
	
	public ConstructionStage getNextStage(ConstructionStage s) {
		if(s == IDLE) {
			return DEMOLITION;
		} else if(s == DEMOLITION) {
			return EXCAVATION;
		} else if(s == EXCAVATION) {
			return BASE_CONSTRUCTION;
		} else if(s == BASE_CONSTRUCTION) {
			return INTERIOR_FINISHING;
		} else if(s == INTERIOR_FINISHING) {
			return DECORATION;
		} else {
			return DONE;
		}
	}
}
