package com.d3t.citybuilder.util;

public class ThreeScaleIntList {
	
	public int low;
	public int med;
	public int high;
	
	public ThreeScaleIntList() {
		this(0);
	}
	
	public ThreeScaleIntList(int initValue) {
		low = initValue;
		med = initValue;
		high = initValue;
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
			low, med, high
		};
	}
	
	public int get(int i) {
		return toArray()[i];
	}
	
	public void set(int i, int value) throws ArrayIndexOutOfBoundsException {
		if(i == 0) low = value;
		else if(i == 1) med = value;
		else if(i == 2) high = value;
		else throw new ArrayIndexOutOfBoundsException(i);
	}
	
	public void add(int i, int value) {
		set(i, get(i)+value);
	}
}
