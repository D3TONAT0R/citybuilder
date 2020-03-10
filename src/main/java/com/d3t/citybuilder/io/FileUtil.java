package com.d3t.citybuilder.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.d3t.citybuilder.framework.CBMain;

public class FileUtil {

	public static final String separator = "=";
	
	public static final String arrayMark = "=>";
	public static final String arrayIndent = "\t";
	
	public LinkedHashMap<String, Object> content;

	public FileUtil(String[] fileContents) {
		//if (fileContents[0].StartsWith("#")) title = fileContents[0].SubString(1); else Debug.LogError("File is not formatted correctly!");
		content = new LinkedHashMap<String, Object>();
		for(String str : fileContents) str.replace("\r", ""); //Line ending cleanup
		for(int ln = 0; ln < fileContents.length; ln++) {
			if(!fileContents[ln].startsWith("#") && fileContents[ln].contains(separator)) {
				//Is the current line an Array?
				if(fileContents[ln].contains(arrayMark)) {
					String valName = fileContents[ln].split(arrayMark)[0];
					int length = 0;
					int i = ln+1;
					while(i < fileContents.length && fileContents[i].startsWith(arrayIndent)) {
						i++;
						length++;
					}
					String[] arr = new String[length];
					for(int j = 0; j < arr.length; j++) {
						arr[j] = fileContents[ln + 1 + j].substring(1).split("\n")[0];
					}
					content.put(valName, arr);
				} else {
					String[] s = fileContents[ln].split(separator);
					if(s.length > 1) {
						String val = s[1];
						if(s.length > 2) for (int i = 2; i < s.length; i++) val += separator+s[i];
						content.put(s[0], val);
					}
				}
			}
		}
	}
	
	public static FileUtil createFromFile(File file) {
		try {
			FileInputStream stream = new FileInputStream(file);
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			ArrayList<String> lines = new ArrayList<String>();
			String ln = reader.readLine();
			while(ln != null) {
				lines.add(ln);
				ln = reader.readLine();
			}
			reader.close();
			String[] arr = new String[lines.size()];
			lines.toArray(arr);
			return new FileUtil(arr);
		} catch (Exception e) {
			CBMain.log.severe("Exception encountered while reading file "+file.getPath());
			e.printStackTrace();
		}
		return null;
	}

	public static FileUtil loadSection(String[] fileContents, String section) {
		ArrayList<String> lines = new ArrayList<String>();
		boolean b = false;
		for(int i = 0; i < fileContents.length; i++) {
			if (fileContents[i].startsWith("#")) b = fileContents[i].startsWith("#" + section);
			if (b) lines.add(fileContents[i]);
		}
		String[] array = new String[0];
		lines.toArray(array);
		return new FileUtil(array);
	}

	public FileUtil() {
		content = new LinkedHashMap<String, Object>();
	}

	public boolean Save(String path, String fileName) {
		String t = ""; //"#" + title + "\n";
		ArrayList<String> keys = new ArrayList<String>(content.keySet());
		ArrayList<Object> values = new ArrayList<Object>(content.values());
		for(int i = 0; i < keys.size(); i++) {
			if(values.get(i) instanceof String[]) {
				t += keys.get(i) + "=>\n";
				for(int j = 0; j < ((String[])values.get(i)).length; j++) {
					String item = ((String[])values.get(i))[j];
					t += "\t"+item+"\n";
				}
			} else {
				t += keys.get(i) + separator + values.get(i).toString();
				t += "\n";
			}
		}
		String p = path + "/" + fileName;
		File file = new File(CBMain.getDataFolderPath(), p);
	    if (!file.exists()) {
	    	file.getParentFile().mkdirs();
	    }
	    try {
	    	FileOutputStream stream = new FileOutputStream(file);
	    	stream.write(t.getBytes());
	    	stream.close();
	    	System.out.println("Saved to " + p);
	    	return true;
	    }
	    catch(Exception e) {
	    	System.out.println("Error encountered while trying to write to file " + p);
	    	e.printStackTrace();
	    }
	    return false;
	}

	public boolean ContainsField(String field) {
		return content.containsKey(field);
	}

	public void SetValue(String name, Object value) {
		content.put(name, value);
	}
	
	public void SetArrayList(String name, ArrayList<String> value) {
		String[] array = new String[value.size()];
		value.toArray(array);
		content.put(name, array);
	}

	public boolean GetBool(String name) {
		return Boolean.parseBoolean((String)content.get(name));
	}

	public int GetInt(String name) {
		return Integer.parseInt((String)content.get(name));
	}

	public float GetFloat(String name) {
		return Float.parseFloat((String)content.get(name));
	}

	public String GetString(String name) {
		return (String)content.get(name);
	}

	public String[] GetArray(String name) {
		return (String[])content.get(name);
	}

}
