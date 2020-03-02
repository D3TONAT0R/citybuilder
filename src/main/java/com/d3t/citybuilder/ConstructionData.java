package com.d3t.citybuilder;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.util.Vector;

public class ConstructionData {
	
	public static final int maxProgressIterations = 64;
	
	public Zone zone;
	
	public String structureName;
	
	public Structure structure;
	public Orientation orientation;
	
	public ConstructionStage constructionStage = ConstructionStage.IDLE;
	public float constructionProgress = 1f;
	public int constructionBlockProgress = -1;
	
	public ConstructionData(Zone z, String structure, Orientation orient, boolean buildInstantly) {
		zone = z;
		structureName = structure;
		orientation = orient;
		constructionBlockProgress = StructureFactory.undergroundLayers;
	}
	
	public String getSaveString() {
		return structureName+"@"+orientation;
	}
	
	public boolean updateConstruction() {
		if(constructionStage == ConstructionStage.DONE) return false;
		
		return true;
	}
	
	private void onFinishConstruction() {
		
	}
	
	public static ConstructionData loadFromSaveString(Zone z, String data) {
		String[] dataSplit = data.split("@");
		int progress = Integer.parseInt(dataSplit[1]);
		ConstructionData cdata = new ConstructionData(z, dataSplit[0], Orientation.valueOf(dataSplit[1]), progress >= 1);
		cdata.constructionBlockProgress = progress; 
		return cdata;
	}
	
	private void construct() {
		int i = 0;
		while(i < maxProgressIterations) {
			i++;
			if(constructionStage == ConstructionStage.DONE) break;
			Vector vec = increaseConstructionProgress();
			if(vec != null) setBlockAt(zone, vec.getBlockX(), vec.getBlockY(), vec.getBlockZ(), structure.getBlockForOrientation(vec.getBlockX(), vec.getBlockY(), vec.getBlockZ(), orientation));
		}
	}
	
	public Vector getConstructingBlockLocation() {
		int x,y,z = 0;
		int p = constructionBlockProgress;
		if(constructionStage == ConstructionStage.IDLE || constructionStage == ConstructionStage.DONE) return null;
		if(constructionStage == ConstructionStage.EXCAVATION) {
			if(p >= 16*16*StructureFactory.undergroundLayers) {
				return null;
			} else {
				y = (int)Math.floor(p/16f/16f);
				z = (int)Math.floor((p-y)/16f);
				x = p-y-z;
				return new Vector(x,y,z);
			}
		} else {
			y = (int)Math.floor(p/16f/16f);
			z = (int)Math.floor((p-y)/16f);
			x = p-y-z;
			return new Vector(x,y,z);
		}
	}
	
	private Vector increaseConstructionProgress() {
		constructionBlockProgress++;
		int totalVolume = structure.getStructureVolume();
		Vector loc = getConstructingBlockLocation();
		if(loc == null) {
			constructionStage = constructionStage.getNextStage(constructionStage);
			loc = getConstructingBlockLocation();
		}
		return loc;
	}
	
	private void processBlockForStage(Zone zone, int x, int y, int z, BlockData data) {
		int underground = StructureFactory.undergroundLayers;
		ChunkPosition chunk = zone.pos;
		Block b = zone.world.getBlockAt(chunk.getBlockX() + x, zone.averageTerrainLevel - underground + y, chunk.getBlockZ() + z);
	}

	private void setBlockAt(Block b, BlockData data) {
		b.setType(Material.AIR);
		b.setBlockData(data);

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
}
