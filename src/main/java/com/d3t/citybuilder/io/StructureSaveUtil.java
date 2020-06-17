package com.d3t.citybuilder.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.Banner;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.TileState;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.data.BlockData;

import com.d3t.citybuilder.framework.CBMain;
import com.d3t.citybuilder.structures.Structure;
import com.d3t.citybuilder.structures.StructureFrontline;
import com.d3t.citybuilder.structures.StructureLibrary;
import com.d3t.citybuilder.zones.RealEstateData;

public class StructureSaveUtil {

	public static final int fileVersion = 1;
	public static final String fileExtension = ".cbstructure";

	public static final String realEstateDataSaveMark = "#REALESTATEDATA";
	public static final String blockDataSaveMark = "#BLOCKS";
	public static final String tileDataSaveMark = "#TILEDATA";
	
	public static final String signLineDelimiter = "\\n";
	public static final String bannerPatternDelimiter = "@";

	public static int successfullyLoadedFiles = 0;
	public static int failedToLoadFiles = 0;

	public static boolean saveStructure(Structure s) {
		String subfolder = "structures/";
		if (s.category != null && s.category.length() > 1)
			subfolder += s.category;
		FileUtil writer = new FileUtil();
		writer.SetValue("version", 1);
		writer.SetValue("category", s.category);
		writer.SetValue("name", s.structureName);
		writer.SetValue("creator", s.creator);
		writer.SetValue("size", String.format("%sx%s\n", s.sizeX, s.sizeZ));
		writer.SetValue("totalheight", s.getTotalHeight());
		writer.SetValue("legalheight", s.legalHeight);
		String[] front = new String[16];
		for(int i = 0; i < 16; i++) front[i] = s.frontline[i].name();
		writer.SetValue("front", front);
		ArrayList<String> realEstate = new ArrayList<String>();

		for (RealEstateData red : s.realEstateData)
			realEstate.add(red.getSaveString());
		writer.SetArrayList("realestatedata", realEstate);

		ArrayList<String> blocks = new ArrayList<String>();
		for (int y = 0; y < s.blocks[0].length; y++) {
			for (int z = 0; z < s.blocks[0][0].length; z++) {
				String str = "";
				for (int x = 0; x < s.blocks.length; x++) {
					str += s.blocks[x][y][z].getAsString();
					if (x < s.blocks.length - 1)
						str += " ";
				}
				blocks.add(str);
			}
		}
		writer.SetArrayList("blocks", blocks);

		ArrayList<String> tiledata = new ArrayList<String>();
		for (int y = 0; y < s.blockTiles[0].length; y++) {
			for (int z = 0; z < s.blockTiles[0][0].length; z++) {
				for (int x = 0; x < s.blockTiles.length; x++) {
					if (s.blockTiles[x][y][z] != null) {
						tiledata.add(String.format("%s %s %s %s", x, y, z, s.blockTiles[x][y][z]));
					}
				}
			}
		}
		writer.SetArrayList("tiledata", tiledata);

		return writer.Save(subfolder, s.structureName.toLowerCase() + fileExtension);
	}

	public static String getTileStateSaveString(TileState state) {
		String data;
		if (state instanceof Sign) {
			data = "SIGN:";
			Sign sign = (Sign) state;
			data += sign.getLine(0) + signLineDelimiter;
			data += sign.getLine(1) + signLineDelimiter;
			data += sign.getLine(2) + signLineDelimiter;
			data += sign.getLine(3);
		} else if (state instanceof Banner) {
			data = "BANR:";
			Banner banner = (Banner) state;
			data += banner.getBaseColor().name() + ",";
			List<Pattern> patterns = banner.getPatterns();
			for (int i = 0; i < patterns.size(); i++) {
				Pattern pat = patterns.get(i);
				data += pat.getPattern().name() + bannerPatternDelimiter + pat.getColor().name();
				if (i < patterns.size() - 1)
					data += ",";
			}
		} else if (state instanceof Skull) {
			data = "SKUL:";
			data += NBTSaveUtil.getSkullNBTString(state);
		} else {
			data = "UNKNOWN";
		}
		return data;
	}

	public static void loadSavedStructures() {
		successfullyLoadedFiles = 0;
		failedToLoadFiles = 0;
		StructureLibrary.initialize();
		File dir = new File(CBMain.getDataFolderPath().getAbsolutePath() + "/structures/");
		System.out.println("loading structures ...");
		if (dir.isDirectory()) {
			for (File f : SaveHandler.listFiles(dir, fileExtension)) {
				System.out.println("Loading structure file " + f.getName());
				Structure s = loadStructure(f);
				if (s != null) {
					StructureLibrary.registerStructure(s, s.category, s.targetZone, s.targetDensity);
					successfullyLoadedFiles++;
				} else {
					System.out.println("Failed to load structure file " + f.getName());
					failedToLoadFiles++;
				}
			}
		} else {
			System.out.println("Failed to load structures! Not a directory: " + dir.getAbsolutePath());
		}
	}

	public static Structure loadStructure(File file) {
		if (!file.exists()) {
			System.out.println("Failed to read file (not found): " + file.getAbsolutePath());
			return null;
		}
		FileUtil reader = FileUtil.createFromFile(file);
		try {
			int version = reader.GetInt("version");
			if (version == 1) {
				return readFileVersion1(reader);
			} else {
				System.out.println("Unknown file version: " + version);
				return null;
			}
		} catch (Exception e) {
			System.out.println("Error while reading structure file: " + file.getAbsolutePath());
			e.printStackTrace();
			return null;
		}
	}

	private static Structure readFileVersion1(FileUtil reader) {
		String cat = reader.GetString("category");
		String name = reader.GetString("name");
		String creatorName = reader.GetString("creator");
		String[] sizeStr = reader.GetString("size").split("x");
		int sizeX = Integer.parseInt(sizeStr[0]);
		int sizeZ = Integer.parseInt(sizeStr[1]);
		int totalHeight = reader.GetInt("totalheight");
		int legalHeight = reader.GetInt("legalheight");
		String[] frontStr = reader.GetArray("front");
		StructureFrontline[] front = new StructureFrontline[16];
		for(int i = 0; i < 16; i++) front[i] = StructureFrontline.valueOf(frontStr[i]);
		RealEstateData[] realestate = readRealEstateData(reader.GetArray("realestatedata"));
		BlockData[][][] blocks = readBlockData(reader.GetArray("blocks"), sizeX * 16, totalHeight, sizeZ * 16);
		String[][][] tileStates = readTileStateData(reader.GetArray("tiledata"), sizeX * 16, totalHeight, sizeZ * 16);
		Structure s = new Structure(blocks, tileStates, name, cat, creatorName, sizeX, sizeZ, legalHeight, realestate);
		s.frontline = front;
		s.verifyIntegrity();
		return s;
	}

	private static BlockData[][][] readBlockData(String[] input, int sizeX, int sizeY, int sizeZ) {
		BlockData[][][] blocks = new BlockData[sizeX][sizeY][sizeZ];
		int x = 0, y = 0, z = 0;
		for (String str : input) {
			String[] ln = str.split(" ");
			for (x = 0; x < sizeX; x++) {
				BlockData bd = CBMain.INSTANCE.getServer().createBlockData(ln[x]);
				blocks[x][y][z] = bd;
			}
			z++;
			if (z >= sizeZ) {
				y++;
				if (y >= sizeY)
					break;
				z = 0;
			}
		}
		return blocks;
	}

	private static String[][][] readTileStateData(String[] input, int sizeX, int sizeY, int sizeZ) {
		String[][][] states = new String[sizeX][sizeY][sizeZ];
		for (String str : input) {
			String[] split = str.split(" ");
			int x = Integer.parseInt(split[0]);
			int y = Integer.parseInt(split[1]);
			int z = Integer.parseInt(split[2]);
			String rest = str.substring(split[0].length() + split[1].length() + split[2].length() + 3,
					str.length() - 1);
			states[x][y][z] = rest;
		}
		return states;
	}

	private static RealEstateData[] readRealEstateData(String[] input) {
		ArrayList<RealEstateData> data = new ArrayList<RealEstateData>();
		RealEstateData[] arr = new RealEstateData[0];
		for (String str : input) {
			// TODO: read real estate data
		}
		data.toArray(arr);
		return arr;
	}
}
