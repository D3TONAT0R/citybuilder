package com.d3t.citybuilder.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.MalformedInputException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.block.Banner;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.TileState;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.data.BlockData;

import com.d3t.citybuilder.framework.CBMain;
import com.d3t.citybuilder.structures.Structure;
import com.d3t.citybuilder.structures.StructureLibrary;
import com.d3t.citybuilder.structures.TrafficStructurePieces;
import com.d3t.citybuilder.zones.RealEstateData;

public class StructureSaveUtil {

	public static final int fileVersion = 1;
	public static final String fileExtension = ".cbstructure";

	public static final String realEstateDataSaveMark = "#REALESTATEDATA";
	public static final String blockDataSaveMark = "#BLOCKS";
	public static final String tileDataSaveMark = "#TILEDATA";
	
	public static int successfullyLoadedFiles = 0;
	public static int failedToLoadFiles = 0;
	
	public static boolean saveStructure(Structure s) {
		String subfolder = "structures/";
		if (s.category != null && s.category.length() > 1)
			subfolder += s.category + "/";
		File file = new File(CBMain.getDataFolderPath(), subfolder + s.structureName.toLowerCase() + fileExtension);
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			// CBMain.getDataFolderPath().mkdirs();
		}
		int x = 0;
		int y = 0;
		int z = 0;
		try {
			byte[] version = (fileVersion + "\n").getBytes();
			byte[] cat = (s.category + "\n").getBytes();
			byte[] name = (s.structureName + "\n").getBytes();
			byte[] creatorName = (s.creator + "\n").getBytes();
			byte[] size = String.format("%sx%s\n", s.sizeX, s.sizeZ).getBytes();
			byte[] height = (s.getTotalHeight() + ";" + s.lawfulHeight + "\n").getBytes();
			FileOutputStream stream = new FileOutputStream(file);
			stream.write(version);
			stream.write(cat);
			stream.write(name);
			stream.write(creatorName);
			stream.write(size);
			stream.write(height);
			stream.write((realEstateDataSaveMark + "\n").getBytes());
			for (RealEstateData re : s.realEstateData) {
				stream.write((re.getSaveString() + "\n").getBytes());
			}
			stream.write((blockDataSaveMark + "\n").getBytes());
			for (y = 0; y < s.blocks[0].length; y++) {
				for (z = 0; z < s.blocks[0][0].length; z++) {
					for (x = 0; x < s.blocks.length; x++) {
						stream.write(s.blocks[x][y][z].getAsString().getBytes());
						if (x < s.blocks.length - 1)
							stream.write(" ".getBytes());
					}
					stream.write("\n".getBytes());
				}
			}
			stream.write((tileDataSaveMark + "\n").getBytes());
			for (y = 0; y < s.blockTiles[0].length; y++) {
				for (z = 0; z < s.blockTiles[0][0].length; z++) {
					for (x = 0; x < s.blockTiles.length; x++) {
						if(s.blockTiles[x][y][z] != null) {							
							stream.write(String.format("%s,%s,%s:%s", x, y, z, getTileStateSaveString(s.blockTiles[x][y][z])).getBytes());
						}
					}
					stream.write("\n".getBytes());
				}
			}
			stream.close();
			System.out.println("Structure file created: " + file.getAbsolutePath());
			return true;
		} catch (Exception e) {
			System.out.println("Failed to write structure file: " + file.getAbsolutePath());
			System.out.println(String.format("Fail at: X %s, Y %s, Z %s", x, y, z));
			e.printStackTrace();
			return false;
		}
	}

	public static String getTileStateSaveString(TileState state) {
		String data;
		if (state instanceof Sign) {
			data = "SIGN:";
			Sign sign = (Sign) state;
			data += sign.getLine(0) + "§";
			data += sign.getLine(1) + "§";
			data += sign.getLine(2) + "§";
			data += sign.getLine(3);
		} else if (state instanceof Banner) {
			data = "BANR:";
			Banner banner = (Banner) state;
			data += banner.getBaseColor().name() + ",";
			List<Pattern> patterns = banner.getPatterns();
			for (int i = 0; i < patterns.size(); i++) {
				Pattern pat = patterns.get(i);
				data += pat.getPattern().name() + "@" + pat.getColor().name();
				if (i < patterns.size() - 1)
					data += ",";
			}
		} else if(state instanceof Skull) {
			data = "SKUL:";
			OfflinePlayer skullPlayer = ((Skull)state).getOwningPlayer();
			//TODO: fetch skull data!
			//ItemStack stack = SkullCreator.withBase64(item, base64)
		} else {
			data = "UNKNOWN";
		}
		return data;
	}

	public static void loadSavedStructures() {
		successfullyLoadedFiles = 0;
		failedToLoadFiles = 0;
		StructureLibrary.allStructures = new HashMap<String, Structure>();
		StructureLibrary.categories = new HashMap<String, HashMap<String, Structure>>();
		StructureLibrary.trafficStructures = new HashMap<String, TrafficStructurePieces>();
		File dir = new File(CBMain.getDataFolderPath().getAbsolutePath() + "/structures/");
		System.out.println("loading structures ...");
		if (dir.isDirectory()) {
			for (File f : SaveHandler.listFiles(dir, fileExtension)) {
				System.out.println("Loading structure file " + f.getName());
				Structure s = loadStructure(f);
				if (s != null) {
					StructureLibrary.registerStructure(s, s.category);
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
		try {
			FileInputStream stream = new FileInputStream(file);
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			int version = Integer.parseInt(reader.readLine());
			if (version == 1) {
				return readFileVersion1(reader, file);
			} else {
				System.out.println("Unknown file version: " + version);
				reader.close();
				return null;
			}
		} catch (Exception e) {
			System.out.println("Error while reading structure file: " + file.getAbsolutePath());
			e.printStackTrace();
			return null;
		}
	}

	private static Structure readFileVersion1(BufferedReader reader, File file) {
		try {
			String cat = reader.readLine();
			String name = reader.readLine();
			String creatorName = reader.readLine();
			String[] sizeStr = reader.readLine().split("x");
			int sizeX = 1;
			int sizeZ = 1;
			sizeX = Integer.parseInt(sizeStr[0]);
			sizeZ = Integer.parseInt(sizeStr[1]);
			String[] heightsStr = reader.readLine().split(";");
			int[] heights = new int[2];
			for (int i = 0; i < 2; i++)
				heights[i] = Integer.parseInt(heightsStr[i]);
			if (!reader.readLine().equals(realEstateDataSaveMark))
				throw new MalformedInputException(0);
			RealEstateData[] realestate = readRealEstateData(reader);
			if (!reader.readLine().equals(blockDataSaveMark))
				throw new MalformedInputException(0);
			BlockData[][][] blocks = readBlockData(reader, sizeX * 16, heights[0], sizeZ * 16);
			if (!reader.readLine().equals(tileDataSaveMark))
				throw new MalformedInputException(0);
			TileState[][][] tileStates = readTileStateData(reader, sizeX * 16, heights[0], sizeZ * 16);
			reader.close();
			Structure s = new Structure(blocks, tileStates, name, cat, creatorName, sizeX, sizeZ, heights[1],
					realestate);
			s.verifyIntegrity();
			return s;
		} catch (Exception e) {
			System.out.println("Error while creating structure from file:" + file.getAbsolutePath());
			e.printStackTrace();
			return null;
		}
	}

	private static BlockData[][][] readBlockData(BufferedReader reader, int sizeX, int sizeY, int sizeZ)
			throws IOException {
		BlockData[][][] blocks = new BlockData[sizeX][sizeY][sizeZ];
		int x = 0, y = 0, z = 0;
		boolean done = false;
		while (!done) {
			reader.mark(10000);
			String lnr = reader.readLine();
			if (lnr == null || lnr.startsWith("#")) {
				if (lnr != null && lnr.startsWith("#"))
					reader.reset();
				done = true;
				break;
			}
			String[] ln = lnr.split(" ");
			for (x = 0; x < sizeX; x++) {
				BlockData bd = CBMain.INSTANCE.getServer().createBlockData(ln[x]);
				;
				blocks[x][y][z] = bd;
			}
			z++;
			if (z >= sizeZ) {
				y++;
				if (y >= sizeY)
					done = true;
				z = 0;
			}
		}
		return blocks;
	}

	private static TileState[][][] readTileStateData(BufferedReader reader, int sizeX, int sizeY, int sizeZ)
			throws IOException {
		TileState[][][] states = new TileState[sizeX][sizeY][sizeZ];
		boolean done = false;
		while (!done) {
			reader.mark(10000);
			String lnr = reader.readLine();
			if (lnr == null || lnr.startsWith("#")) {
				if (lnr != null && lnr.startsWith("#"))
					reader.reset();
				done = true;
				break;
			}
			if (lnr.length() > 1) {
				String[] split = lnr.split(" ");
				int x = Integer.parseInt(split[0]);
				int y = Integer.parseInt(split[1]);
				int z = Integer.parseInt(split[2]);
				String rest = lnr.substring(split[0].length() + split[1].length() + split[2].length() + 3,
						lnr.length() - 1);
				// TODO: create tilestates
			}
		}
		return states;
	}

	private static RealEstateData[] readRealEstateData(BufferedReader reader) throws IOException {
		ArrayList<RealEstateData> data = new ArrayList<RealEstateData>();
		boolean done = false;
		while (!done) {
			reader.mark(10000);
			String s = reader.readLine();
			if (s == null || s.startsWith("#")) {
				if (s != null && s.startsWith("#"))
					reader.reset();
				done = true;
				break;
			}
			// TODO: read real estate data
		}
		RealEstateData[] arr = new RealEstateData[0];
		data.toArray(arr);
		return arr;
	}
}
