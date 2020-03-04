package com.d3t.citybuilder.structures;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.MalformedInputException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.block.Banner;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.TileState;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.d3t.citybuilder.framework.CBMain;
import com.d3t.citybuilder.zones.RealEstateData;
import com.d3t.citybuilder.zones.Zone;
import com.deanveloper.skullcreator.SkullCreator;

public class Structure {

	public static final int fileVersion = 1;
	public static final String fileExtension = ".cbstructure";

	public static final String realEstateDataSaveMark = "#REALESTATEDATA";
	public static final String blockDataSaveMark = "#BLOCKS";
	public static final String tileDataSaveMark = "#TILEDATA";

	public boolean drawDebugVolume;

	public String structureName;
	public String category;
	public String creator;
	public BlockData[][][] blocks = new BlockData[16][32][16];
	public TileState[][][] blockTiles = new TileState[16][32][16];
	public int sizeX = 1;
	public int sizeZ = 1;
	public int lawfulHeight = 0;
	public RealEstateData[] realEstateData;

	public Structure() {

	}

	public Structure(BlockData[][][] blocks, TileState[][][] tileStates, String name, String cat, String creatorName,
			int chunksX, int chunksZ, int lawfulHeight, RealEstateData[] realEstate) {
		this();
		structureName = name;
		category = cat;
		creator = creatorName;
		this.blocks = blocks;
		this.blockTiles = tileStates;
		this.sizeX = chunksX;
		this.sizeZ = chunksZ;
		this.lawfulHeight = lawfulHeight;
		this.realEstateData = realEstate;
	}

	public void setTileStates(TileState[][][] states) {
		blockTiles = states;
	}

	public void setTileState(int x, int y, int z, TileState state) {
		blockTiles[x][y][z] = state;
	}

	public int getTotalHeight() {
		return blocks[0].length;
	}

	public void startBuild(Zone zone, Orientation facing) {
		ConstructionData cd = new ConstructionData(zone, this, facing, false);
		zone.building = cd;
		zone.city.registerConstruction(cd);
	}

	public void buildNow(Zone zone, Orientation facing) {
		ConstructionData cd = new ConstructionData(zone, this, facing, true);
		zone.building = cd;
	}

	public int getStructureVolume() {
		return blocks.length * blocks[0].length * blocks[0][0].length;
	}

	public BlockData getBlockForOrientation(int x, int y, int z, Orientation orientation) {
		switch (orientation) {
		case SOUTH:
		case SOUTH_WEST:
			return applyRotation(blocks[x][y][z], 0);
		case WEST:
		case NORTH_WEST:
			return applyRotation(blocks[z][y][15 - x], 1);
		case NORTH:
		case NORTH_EAST:
			return applyRotation(blocks[15 - x][y][15 - z], 2);
		case EAST:
		case SOUTH_EAST:
			return applyRotation(blocks[15 - z][y][x], 3);
		default:
			return blocks[x][y][z];
		}
	}

	// TODO: Rotate angled signs & banners
	private BlockData applyRotation(BlockData data, int steps) {
		data = data.clone();
		final BlockFace[] blockFaces = new BlockFace[] { BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH,
				BlockFace.EAST };
		if (data instanceof Directional) {
			Directional d = (Directional) data;
			BlockFace face = d.getFacing();
			int r = 0;
			if (face == BlockFace.SOUTH)
				r = 0;
			else if (face == BlockFace.WEST)
				r = 1;
			else if (face == BlockFace.NORTH)
				r = 2;
			else if (face == BlockFace.EAST)
				r = 3;
			d.setFacing(blockFaces[(r + steps) % 4]);
			return d;
		} else if (data instanceof MultipleFacing) {
			MultipleFacing mf = (MultipleFacing) data;
			boolean[] facings = new boolean[4];
			facings[0] = mf.hasFace(BlockFace.SOUTH);
			facings[1] = mf.hasFace(BlockFace.WEST);
			facings[2] = mf.hasFace(BlockFace.NORTH);
			facings[3] = mf.hasFace(BlockFace.EAST);
			mf.setFace(BlockFace.SOUTH, facings[(0 + steps) % 4]);
			mf.setFace(BlockFace.WEST, facings[(1 + steps) % 4]);
			mf.setFace(BlockFace.NORTH, facings[(2 + steps) % 4]);
			mf.setFace(BlockFace.EAST, facings[(3 + steps) % 4]);
		}
		return data;
	}

	public boolean writeToFile() {
		String subfolder = "structures/";
		if (category != null && category.length() > 1)
			subfolder += category + "/";
		File file = new File(CBMain.getDataFolderPath(), subfolder + structureName.toLowerCase() + fileExtension);
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			// CBMain.getDataFolderPath().mkdirs();
		}
		int x = 0;
		int y = 0;
		int z = 0;
		try {
			byte[] version = (fileVersion + "\n").getBytes();
			byte[] cat = (category + "\n").getBytes();
			byte[] name = (structureName + "\n").getBytes();
			byte[] creatorName = (creator + "\n").getBytes();
			byte[] size = String.format("%sx%s\n", sizeX, sizeZ).getBytes();
			byte[] height = (getTotalHeight() + ";" + lawfulHeight + "\n").getBytes();
			FileOutputStream stream = new FileOutputStream(file);
			stream.write(version);
			stream.write(cat);
			stream.write(name);
			stream.write(creatorName);
			stream.write(size);
			stream.write(height);
			stream.write((realEstateDataSaveMark + "\n").getBytes());
			for (RealEstateData re : realEstateData) {
				stream.write((re.getSaveString() + "\n").getBytes());
			}
			stream.write((blockDataSaveMark + "\n").getBytes());
			for (y = 0; y < blocks[0].length; y++) {
				for (z = 0; z < blocks[0][0].length; z++) {
					for (x = 0; x < blocks.length; x++) {
						stream.write(blocks[x][y][z].getAsString().getBytes());
						if (x < blocks.length - 1)
							stream.write(" ".getBytes());
					}
					stream.write("\n".getBytes());
				}
			}
			stream.write((tileDataSaveMark + "\n").getBytes());
			for (y = 0; y < blockTiles[0].length; y++) {
				for (z = 0; z < blockTiles[0][0].length; z++) {
					for (x = 0; x < blockTiles.length; x++) {
						if(blockTiles[x][y][z] != null) {							
							stream.write(String.format("%s,%s,%s:%s", x, y, z, getTileStateSaveString(blockTiles[x][y][z])).getBytes());
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
			data += sign.getLine(0) + "�";
			data += sign.getLine(1) + "�";
			data += sign.getLine(2) + "�";
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

	public static Structure loadFromFile(File file) {
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

	public boolean verifyIntegrity() {
		int nullBlocks = 0;
		int blockArraySize = blocks.length * blocks[0].length * blocks[0][0].length;
		for (int y = 0; y < blocks[0].length; y++) {
			for (int z = 0; z < blocks[0][0].length; z++) {
				for (int x = 0; x < blocks.length; x++) {
					if (blocks[x][y][z] == null) {
						System.out.println(String.format("NULL @ %s %s %s - %s", x, y, z, blocks[x][y][z]));
						nullBlocks++;
					}
				}
			}
		}
		if (nullBlocks > 0)
			System.out.println(String.format(
					"integrity check for structure '%s' has detected null entries in the block array. %s out of %s values are NULL.",
					structureName, nullBlocks, blockArraySize));
		return nullBlocks == 0;
	}
}