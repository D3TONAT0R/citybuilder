package com.d3t.citybuilder.io;

import java.io.File;
import java.util.ArrayList;

public class SaveHandler {
	
	public static ArrayList<File> listFiles(File directory, String fileExtension) {
		ArrayList<File> files = new ArrayList<File>();
		for (File f : directory.listFiles()) {
			if (f.isDirectory()) {
				files.addAll(listFiles(f, fileExtension));
			} else if (f.getName().endsWith(fileExtension)) {
				files.add(f);
			}
		}
		return files;
	}
}
