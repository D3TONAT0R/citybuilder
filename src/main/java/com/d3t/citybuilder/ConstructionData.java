package com.d3t.citybuilder;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
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
			if(vec != null) {
				processBlockForStage(zone, vec.getBlockX(), vec.getBlockY(), vec.getBlockZ(), structure.getBlockForOrientation(vec.getBlockX(), vec.getBlockY(), vec.getBlockZ(), orientation));
			}
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
	
	private boolean processBlockForStage(Zone zone, int x, int y, int z, BlockData target) {
		int underground = StructureFactory.undergroundLayers;
		ChunkPosition chunk = zone.pos;
		Block source = zone.world.getBlockAt(chunk.getBlockX() + x, zone.averageTerrainLevel - underground + y, chunk.getBlockZ() + z);
		if(source == null) return false;
		if(constructionStage == ConstructionStage.DEMOLITION) {
			//Remove everything above ground
			if(y >= zone.averageTerrainLevel) {
				return setAir(source);
			}
		} else if(constructionStage == ConstructionStage.EXCAVATION) {
			//Dig holes where blocks should be placed later
			if(!source.getBlockData().matches(target)) {
				return setAir(source);
			}
		} else if(constructionStage == ConstructionStage.BASE_CONSTRUCTION) {
			//Place substitute blocks during base construction stage
			if(BlockCategories.isBaseConstructionBlock(target.getMaterial())) {
				return setBlockAt(source, BlockCategories.getSubstituteBlock(target));
			}
		} else if(constructionStage == ConstructionStage.INTERIOR_FINISHING) {
			//place interior blocks & replace substitute blocks with the final blocks
			if(BlockCategories.isBaseConstructionBlock(target.getMaterial()) || BlockCategories.isInteriorStageBlock(target.getMaterial())) {
				return setBlockAt(source, target);
			}
		} else if(constructionStage == ConstructionStage.DECORATION) {
			//Place the rest of the blocks 
			return setBlockAt(source, target);
		}
		return false;
	}

	private boolean setBlockAt(Block b, BlockData data) {
		//setAir(b);
		if(!b.getBlockData().matches(data)) {
			b.setBlockData(data);
			return true;
		} else {
			return false;
		}
	}
	
	private boolean setAir(Block b) {
		if(b.getType() != Material.AIR) {
			b.setType(Material.AIR);
			return true;
		} else {
			return false;
		}
	}
}
