package com.d3t.citybuilder.util;

public class FiveScaleIntList {
	
	public int lowest;
	public int low;
	public int med;
	public int high;
	public int highest;
	
	public FiveScaleIntList() {
		this(0);
	}
	
	public FiveScaleIntList(int initValue) {
		lowest = initValue;
		low = initValue;
		med = initValue;
		high = initValue;
		highest = initValue;
	}
	
	public int getTotal() {
		int[] arr = toArray();
		int total = 0;
		for(int i = 0; i < arr.length; i++) {
			total += i;
		}
		return total;
	}
	
	public int[] toArray() {
		return new int[] {
			lowest, low, med, high, highest
		};
	}
	
	public int get(int i) {
		return toArray()[i];
	}
	
	public void set(int i, int value) throws ArrayIndexOutOfBoundsException {
		if(i == 0) lowest = value;
		else if(i == 1) low = value;
		else if(i == 2) med = value;
		else if(i == 3) high = value;
		else if(i == 4) highest = value;
		else throw new ArrayIndexOutOfBoundsException(i);
	}
	
	public void add(int i, int value) {
		set(i, get(i)+value);
	}
}
