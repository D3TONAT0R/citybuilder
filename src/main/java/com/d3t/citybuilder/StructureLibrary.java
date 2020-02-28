package com.d3t.citybuilder;

import java.io.File;
import java.util.HashMap;

public class StructureLibrary {

	public static HashMap<String, Structure> allStructures = new HashMap<String, Structure>();
	
	public static void loadSavedStructures(File dir) {
		System.out.println("loading structures ...");
		if(dir.isDirectory()) {			
			for(File f : dir.listFiles()) {
				if(f.isFile() && f.getName().endsWith(Structure.fileExtension)) {
					System.out.println("Loading structure file "+f.getName());
					Structure s = Structure.loadFromFile(f);
					if(s != null) {
						allStructures.put(s.structureName, s);
					} else {
						System.out.println("Failed to load structure file "+f.getName());
					}
				}
			}
		} else {
			System.out.println("Failed to load structures! Not a directory: "+dir.getAbsolutePath());
		}
	}
	
	public static void registerStructure(Structure s) {
		allStructures.put(s.structureName, s);
	}
}
