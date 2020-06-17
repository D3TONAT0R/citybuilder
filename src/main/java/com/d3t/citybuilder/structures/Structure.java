package com.d3t.citybuilder.structures;

import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;

import com.d3t.citybuilder.zones.RealEstateData;
import com.d3t.citybuilder.zones.Zone;
import com.d3t.citybuilder.zones.ZoneDensity;
import com.d3t.citybuilder.zones.ZoneType;

public class Structure {

	public boolean drawDebugVolume;

	public String structureName;
	public String category;
	public String creator;
	public BlockData[][][] blocks = new BlockData[16][32][16];
	public String[][][] blockTiles = new String[16][32][16];
	public StructureFrontline[] frontline = new StructureFrontline[16];
	public int sizeX = 1;
	public int sizeZ = 1;
	public int legalHeight = 0;
	public RealEstateData[] realEstateData;
	
	public ZoneType targetZone = ZoneType.UNZONED;
	public ZoneDensity targetDensity = ZoneDensity.Low;

	public Structure() {

	}

	public Structure(BlockData[][][] blocks, String[][][] tileStates, String name, String cat, String creatorName,
			int chunksX, int chunksZ, int legalHeight, RealEstateData[] realEstate) {
		this();
		structureName = name;
		category = cat;
		creator = creatorName;
		this.blocks = blocks;
		this.blockTiles = tileStates;
		this.frontline = new StructureFrontline[16];
		this.sizeX = chunksX;
		this.sizeZ = chunksZ;
		this.legalHeight = legalHeight;
		this.realEstateData = realEstate;
	}

	public void setTileStates(String[][][] states) {
		blockTiles = states;
	}

	public void setTileState(int x, int y, int z, String state) {
		blockTiles[x][y][z] = state;
	}

	public int getTotalHeight() {
		return blocks[0].length;
	}

	public void startBuild(Zone zone, Orientation facing) {
		Construction cd = new Construction(zone, this, facing, BuildTask.NORMAL);
		zone.building = cd;
	}

	public void buildNow(Zone zone, Orientation facing) {
		Construction cd = new Construction(zone, this, facing, BuildTask.INSTANT);
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
