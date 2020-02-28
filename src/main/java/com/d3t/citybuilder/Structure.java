package com.d3t.citybuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;

public class Structure {

	public static final int fileVersion = 1;
	public static final String fileExtension = ".cbstructure";
	
	public boolean drawDebugVolume;
	
	public String structureName;
	public String category;
	public String creator;
	public BlockData[][][] blocks = new BlockData[16][32][16];
	public int sizeX = 1;
	public int sizeZ = 1;
	public int lawfulHeight = 0;
	public RealEstateData[] realEstateData;
	
	public Structure() {
		
	}
	
	public Structure(BlockData[][][] blocks, String name, String cat, String creatorName, int chunksX, int chunksZ, int lawfulHeight, RealEstateData[] realEstate) {
		this();
		structureName = name;
		category = cat;
		creator = creatorName;
		this.blocks = blocks;
		this.sizeX = chunksX;
		this.sizeZ = chunksZ;
		this.lawfulHeight = lawfulHeight;
		this.realEstateData = realEstate;
	}
	
	public int getTotalHeight() {
		return blocks[0].length;
	}
	
	public void build(Zone zone, Orientation facing) {
		if(drawDebugVolume) {
			createDebugStructure(zone);
		} else {
			for(int y = 0; y < getTotalHeight(); y++) {
				for(int z = 0; z < sizeZ*16; z++) {
					for(int x = 0; x < sizeX*16; x++) {
						setBlockAt(zone,x,y,z,getBlockForOrientation(x,y,z,facing));
					}
				}
			}
		}
	}
	
	private BlockData getBlockForOrientation(int x, int y, int z, Orientation orientation) {
		switch(orientation) {
		case SOUTH:
		case SOUTH_WEST:
			return applyRotation(blocks[x][y][z],0);
		case WEST:
		case NORTH_WEST:
			return applyRotation(blocks[z][y][15-x],1);
		case NORTH:
		case NORTH_EAST:
			return applyRotation(blocks[15-x][y][15-z],2);
		case EAST:
		case SOUTH_EAST:
			return applyRotation(blocks[15-z][y][x],3);
		default:
			return blocks[x][y][z];
		}
	}
	
	private void setBlockAt(Zone zone, int x, int y, int z, BlockData data) {
		int underground = StructureFactory.undergroundLayers;
		ChunkPosition chunk = zone.pos;
		zone.world.getBlockAt(chunk.getBlockX()+x, zone.averageTerrainLevel-underground+y, chunk.getBlockZ()+z).setType(Material.AIR);
		zone.world.getBlockAt(chunk.getBlockX()+x, zone.averageTerrainLevel-underground+y, chunk.getBlockZ()+z).setBlockData(data);

	}
	
	//TODO: Rotate angled signs & banners
	private BlockData applyRotation(BlockData data, int steps) {
		data = data.clone();
		final BlockFace[] blockFaces = new BlockFace[] { BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST };
		if(data instanceof Directional) {
			Directional d = (Directional)data;
			BlockFace face = d.getFacing();
			int r = 0;
			if(face == BlockFace.SOUTH) r = 0;
			else if(face == BlockFace.WEST) r = 1;
			else if(face == BlockFace.NORTH) r = 2;
			else if(face == BlockFace.EAST) r = 3;
			d.setFacing(blockFaces[(r+steps) % 4]);
			return d;
		} else if(data instanceof MultipleFacing) {
			MultipleFacing mf = (MultipleFacing)data;
			boolean[] facings = new boolean[4];
			facings[0] = mf.hasFace(BlockFace.SOUTH);
			facings[1] = mf.hasFace(BlockFace.WEST);
			facings[2] = mf.hasFace(BlockFace.NORTH);
			facings[3] = mf.hasFace(BlockFace.EAST);
			mf.setFace(BlockFace.SOUTH, facings[(0+steps)%4]);
			mf.setFace(BlockFace.WEST, facings[(1+steps)%4]);
			mf.setFace(BlockFace.NORTH, facings[(2+steps)%4]);
			mf.setFace(BlockFace.EAST, facings[(3+steps)%4]);
		}
		return data;
	}
	
	public static void createDebugStructure(Zone zone) {
		ZoneType type = zone.zoneType;
		ZoneDensity density = zone.getDensity();
		Structure s = new Structure();
		Material mat = null;
		int height = 0;
		s.drawDebugVolume = true;
		if(type == ZoneType.Residental) {
			mat = Material.BRICK;
		} else if(type == ZoneType.Retail) {
			mat = Material.GLASS;
		} else if(type == ZoneType.Office) {
			mat = Material.CLAY;
		} else if(type == ZoneType.Industrial) {
			mat = Material.IRON_BLOCK;
		}
		if(mat != null) {
			if(density == ZoneDensity.Low) {
				height = 3;
			} else if(density == ZoneDensity.Medium) {
				height = 6;
			} else {
				height = 12;
			}
		} else {
			return;
		}
		for(int z = 4; z < 12; z++) {
			for(int x = 4; x < 12; x++) {
				for(int y = zone.averageTerrainLevel+1; y < zone.averageTerrainLevel+height+2; y++) {
					if(x == 4 || x == 11 || z == 4 || z == 11) {
						zone.setBlock(x, y, z, mat);
					}
				}
			}
		}
	}
	
	public boolean writeToFile() {
		String subfolder = "";
		if(category != null && category.length() > 1) subfolder = category+"/";
		File file = new File(CBMain.getDataFolderPath(), subfolder+structureName.toLowerCase()+fileExtension);
	    if (!file.exists()) {
	    	file.getParentFile().mkdirs();
	    	//CBMain.getDataFolderPath().mkdirs();
	    }
	    int x = 0;
	    int y = 0;
	    int z = 0;
	    try {
	    	byte[] version = (fileVersion+"\n").getBytes();
	    	byte[] cat = (category+"\n").getBytes();
	    	byte[] name = (structureName+"\n").getBytes();
	    	byte[] creatorName = (creator+"\n").getBytes();
	    	byte[] size = String.format("%sx%s\n", sizeX, sizeZ).getBytes();
	    	byte[] height = (getTotalHeight()+";"+lawfulHeight+"\n").getBytes();
	    	FileOutputStream stream = new FileOutputStream(file);
	    	stream.write(version);
	    	stream.write(cat);
	    	stream.write(name);
	    	stream.write(creatorName);
	    	stream.write(size);
	    	stream.write(height);
	    	for(y = 0; y < blocks[0].length; y++) {
		    	for(z = 0; z < blocks[0][0].length; z++) {
			    	for(x = 0; x < blocks.length; x++) {
			    		stream.write(blocks[x][y][z].getAsString().getBytes());
			    		if(x < blocks.length-1) stream.write(" ".getBytes());
			    	}
			    	stream.write("\n".getBytes());
		    	}
		    	//stream.write("#\n".getBytes());
	    	}
	    	stream.close();
	    	System.out.println("Structure file created: "+file.getAbsolutePath());
	    	return true;
	    }
	    catch (Exception e) {
	    	System.out.println("Failed to write structure file: "+file.getAbsolutePath());
	    	System.out.println(String.format("Fail at: X %s, Y %s, Z %s", x,y,z));
	    	e.printStackTrace();
	    	return false;
		}
	}
	
	public static Structure loadFromFile(File file) {
		if(!file.exists()) {
			System.out.println("Failed to read file (not found): "+file.getAbsolutePath());
			return null;
		}
		try {
			FileInputStream stream = new FileInputStream(file);
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream)); 
			int version = Integer.parseInt(reader.readLine());
			if(version == 1) {
				return readFileVersion1(reader, file);
			} else {
				System.out.println("Unknown file version: "+version);
				return null;
			}
		}
		catch(Exception e) {
			System.out.println("Error while reading structure file: "+file.getAbsolutePath());
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
			int[] sizes = new int[2];
			for(int i = 0; i < 2; i++) sizes[i] = Integer.parseInt(sizeStr[i]);
			String[] heightsStr = reader.readLine().split(";");
			int[] heights = new int[2];
			for(int i = 0; i < 2; i++) heights[i] = Integer.parseInt(heightsStr[i]);
			BlockData[][][] blocks = new BlockData[sizes[0]*16][heights[0]][sizes[1]*16];
			for(int i = 0; i < sizes[1]*heights[0]; i++) {
				int z = Math.floorMod(i, sizes[1]);
				int y = Math.floorDiv(i, sizes[1]);
				String[] ln = reader.readLine().split(" ");
				for(int x = 0; x < sizes.length*16; x++) {
					blocks[x][y][z] = CBMain.INSTANCE.getServer().createBlockData(ln[x]);
				}
			}
			return new Structure(blocks, name, cat, creatorName, sizes[0], sizes[1], heights[1], new RealEstateData[0]);
		}
		catch(Exception e) {
			System.out.println("Error while creating structure from file:"+file.getAbsolutePath());
			e.printStackTrace();
			return null;
		}
	}
}
