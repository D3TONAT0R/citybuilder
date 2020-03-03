package com.d3t.citybuilder;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;

public class ConstructionData {
	
	public static final int maxProgressIterations = 256;
	
	public Zone zone;
	
	//public String structureName;
	
	public Structure structure;
	public Orientation orientation;
	
	public ConstructionStage constructionStage = ConstructionStage.IDLE;
	public float constructionProgress = 1f;
	public int constructionBlockProgress = -1;
	
	public ConstructionData(Zone z, Structure structure, Orientation orient, boolean buildInstantly) {
		zone = z;
		this.structure = structure;
		orientation = orient;
		constructionBlockProgress = 0;
		if(buildInstantly) constructAll();
	}
	
	public String getSaveString() {
		return structure.structureName+"@"+orientation;
	}
	
	public boolean updateConstruction() {
		if(constructionStage == ConstructionStage.DONE) return false;
		construct();
		return true;
	}
	
	private void onFinishConstruction() {
		System.out.println("CONSTRUCTION DONE!");
	}
	
	public static ConstructionData loadFromSaveString(Zone z, String data) {
		String[] dataSplit = data.split("@");
		int progress = Integer.parseInt(dataSplit[1]);
		ConstructionData cdata = new ConstructionData(z, StructureLibrary.allStructures.get(dataSplit[0]), Orientation.valueOf(dataSplit[1]), progress >= 1);
		cdata.constructionBlockProgress = progress; 
		return cdata;
	}
	
	private void construct() {
		int i = 0;
		boolean b = false;
		Vector vec = null;
		while(!b && i < maxProgressIterations) {
			i++;
			if(constructionStage == ConstructionStage.DONE) return;
			vec = increaseConstructionProgress();
			if(vec != null) {
				BlockData data = Material.AIR.createBlockData();
				if(constructionStage != ConstructionStage.DEMOLITION && constructionStage != ConstructionStage.EXCAVATION) {
					data = structure.getBlockForOrientation(vec.getBlockX(), vec.getBlockY(), vec.getBlockZ(), orientation);
				}
				b = processBlockForStage(zone, vec.getBlockX(), vec.getBlockY(), vec.getBlockZ(), data);
			}
			System.out.println(String.format("Block %s/%s, x%s y%s z%s, stage: %s", constructionBlockProgress, structure.getStructureVolume(), vec.getBlockX(), vec.getBlockY(), vec.getBlockZ(), constructionStage));
		}
	}
	
	private void constructAll() {
		for(int y = 0; y < structure.getTotalHeight(); y++) {
			for(int z = 0; z < structure.sizeZ*16; z++) {
				for(int x = 0; x < structure.sizeX*16; x++) {
					Block source = zone.world.getBlockAt(zone.pos.getBlockX() + x, zone.averageTerrainLevel - StructureFactory.undergroundLayers + y, zone.pos.getBlockZ() + z);
					setAir(source);
					setBlockAt(source,structure.getBlockForOrientation(x,y,z,orientation));
				}
			}
		}
		constructionStage = ConstructionStage.DONE;
	}
	
	public Vector getConstructingBlockLocation() {
		int x,y,z = 0;
		int p = constructionBlockProgress;
		if(constructionStage == ConstructionStage.IDLE || constructionStage == ConstructionStage.DONE) return null;
		if(constructionStage == ConstructionStage.DEMOLITION) {
			y = 128-(int)Math.floor(p/16f/16f);
			z = (int)Math.floor((p/16f))%16;
			x = p % 16;
			if(y <= zone.averageTerrainLevel-StructureFactory.undergroundLayers) return null;
			return new Vector(x,y-zone.averageTerrainLevel+StructureFactory.undergroundLayers,z);
		} else if(constructionStage == ConstructionStage.EXCAVATION) {
			y = StructureFactory.undergroundLayers-(int)Math.floor(p/16f/16f);
			z = (int)Math.floor((p/16f))%16;
			x = p % 16;
			if(y > StructureFactory.undergroundLayers) return null;
			return new Vector(x,y,z);
		} else {
			y = (int)Math.floor(p/16f/16f);
			z = (int)Math.floor((p/16f))%16;
			x = p % 16;
			if(y >= structure.blocks[0].length) return null;
			return new Vector(x,y,z);
		}
	}
	
	private Vector increaseConstructionProgress() {
		constructionBlockProgress++;
		//int totalVolume = structure.getStructureVolume();
		Vector loc = getConstructingBlockLocation();
		if(loc == null) {
			constructionStage = constructionStage.getNextStage(constructionStage);
			System.out.println("Construction works advanced to stage "+constructionStage.toString());
			constructionBlockProgress = 0;
			loc = getConstructingBlockLocation();
		}
		if(constructionStage == ConstructionStage.DONE) {
			onFinishConstruction();
			return null;
		}
		return loc;
	}
	
	private boolean processBlockForStage(Zone zone, int x, int y, int z, BlockData target) {
		int underground = StructureFactory.undergroundLayers;
		ChunkPosition chunk = zone.pos;
		int worldX = chunk.getBlockX() + x;
		int worldY = zone.averageTerrainLevel - underground + y;
		int worldZ = chunk.getBlockZ() + z;
		Block source = zone.world.getBlockAt(worldX, worldY, worldZ);
		if(source == null) return false;
		if(constructionStage == ConstructionStage.DEMOLITION) {
			//Remove everything above ground
			if(worldY >= zone.averageTerrainLevel) {
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
