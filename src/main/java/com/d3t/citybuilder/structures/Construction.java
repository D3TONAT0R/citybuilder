package com.d3t.citybuilder.structures;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.Sign;
import org.bukkit.util.Vector;

import com.d3t.citybuilder.framework.CBMain;
import com.d3t.citybuilder.io.StructureSaveUtil;
import com.d3t.citybuilder.zones.Zone;

public class Construction {
	
	public static final int maxProgressIterations = 256;
	
	public Zone zone;
	
	//public String structureName;
	
	public Structure structure;
	public Orientation orientation;
	
	public ConstructionStage constructionStage = ConstructionStage.IDLE;
	public float constructionProgress = 1f;
	public int constructionBlockProgress = -1;
	
	public Construction(Zone z, Structure structure, Orientation orient, BuildTask task) {
		zone = z;
		this.structure = structure;
		orientation = orient;
		constructionBlockProgress = 0;
		if(task == BuildTask.NORMAL) {
			z.city.registerConstruction(this);
		} else if(task == BuildTask.INSTANT) {
			constructAll();
		}
	}
	
	public String getSaveString() {
		String s =  structure.structureName+"@"+orientation;
		if(constructionStage != ConstructionStage.DONE) {
			s += "@"+constructionStage.name()+":"+constructionBlockProgress;
		}
		return s;
	}
	
	public boolean updateConstruction() {
		if(constructionStage == ConstructionStage.DONE) return false;
		construct();
		return true;
	}
	
	private void onFinishConstruction() {
		updateConnectionsToRoad();
		CBMain.log.info("CONSTRUCTION DONE!");
	}
	
	public void updateConnectionsToRoad() {
		for(int i = 0; i < 16; i++) {
			if(structure.frontline[i] == StructureFrontline.MAIN_PATH || structure.frontline[i] == StructureFrontline.SECONDARY_PATH) {
				joinPath(i);
			}
		}
	}
	
	public static Construction loadFromSaveString(Zone z, String data) {
		String[] dataSplit = data.split("@");
		Construction cdata = new Construction(z, StructureLibrary.allStructures.get(dataSplit[0]), Orientation.valueOf(dataSplit[1]), BuildTask.NOTHING);
		if(dataSplit.length > 2) {
			String[] constructionSplit = dataSplit[2].split(":");
			ConstructionStage stage = ConstructionStage.valueOf(constructionSplit[0]);
			int progress = Integer.parseInt(constructionSplit[1]);
			cdata.constructionStage = stage;
			cdata.constructionBlockProgress = progress;
			z.city.registerConstruction(cdata);
		} else {
			cdata.constructionStage = ConstructionStage.DONE;
		}
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
				if(constructionStage != ConstructionStage.DEMOLITION) {
					data = structure.getBlockForOrientation(vec.getBlockX(), vec.getBlockY(), vec.getBlockZ(), orientation);
				}
				b = processBlockForStage(zone, vec.getBlockX(), vec.getBlockY(), vec.getBlockZ(), data);
			}
			//if(vec != null) CBMain.log.info(String.format("Block %s/%s, x%s y%s z%s, stage: %s", constructionBlockProgress, structure.getStructureVolume(), vec.getBlockX(), vec.getBlockY(), vec.getBlockZ(), constructionStage));
		}
	}
	
	private void constructAll() {
		for(int y = 0; y < structure.getTotalHeight(); y++) {
			for(int z = 0; z < structure.sizeZ*16; z++) {
				for(int x = 0; x < structure.sizeX*16; x++) {
					Block source = zone.world.getBlockAt(zone.pos.getBlockX() + x, zone.averageTerrainLevel - StructureFactory.undergroundLayers + y, zone.pos.getBlockZ() + z);
					setAir(source);
					setBlockAt(source, structure.getBlockForOrientation(x,y,z,orientation), structure.blockTiles[x][y][z]);
				}
			}
		}
		constructionStage = ConstructionStage.DONE;
		onFinishConstruction();
	}
	
	public Vector getConstructingBlockLocation() {
		int x,y,z = 0;
		int p = constructionBlockProgress;
		if(constructionStage == ConstructionStage.IDLE || constructionStage == ConstructionStage.DONE) return null;
		if(constructionStage == ConstructionStage.DEMOLITION) {
			y = 128-(int)Math.floor(p/16f/16f);
			z = (int)Math.floor((p/16f))%16;
			x = p % 16;
			if(y < 0) return null;
			return new Vector(x,y-zone.averageTerrainLevel+StructureFactory.undergroundLayers,z);
		} else if(constructionStage == ConstructionStage.EXCAVATION) {
			y = (int)Math.floor(p/16f/16f);
			z = (int)Math.floor((p/16f))%16;
			x = p % 16;
			y = StructureFactory.undergroundLayers-y;
			if(y < 0) return null;
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
			CBMain.log.info("Construction works advanced to stage "+constructionStage.toString());
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
		Block source = getBlockAt(x,y,z);
		int worldY = zone.averageTerrainLevel - StructureFactory.undergroundLayers + y;
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
				return setBlockAt(source, BlockCategories.getSubstituteBlock(target), null);
			}
		} else if(constructionStage == ConstructionStage.INTERIOR_FINISHING) {
			//place interior blocks & replace substitute blocks with the final blocks
			if(BlockCategories.isBaseConstructionBlock(target.getMaterial()) || BlockCategories.isInteriorStageBlock(target.getMaterial())) {
				return setBlockAt(source, target, null);
			}
		} else if(constructionStage == ConstructionStage.DECORATION) {
			//Place the rest of the blocks 
			return setBlockAt(source, target, structure.blockTiles[x][y][z]);
		}
		return false;
	}
	
	private Block getBlockAt(int x, int y, int z) {
		int worldX = zone.pos.getBlockX() + x;
		int worldY = zone.averageTerrainLevel - StructureFactory.undergroundLayers + y;
		int worldZ = zone.pos.getBlockZ() + z;
		return zone.world.getBlockAt(worldX, worldY, worldZ);
	}
	
	private void joinPath(int x) {
		int z = 0;
		int dirX = 0;
		int dirZ = 0;
		switch (orientation) {
		case NONE:
		case SOUTH:
		case SOUTH_WEST:
			dirZ = -1;
			break;
		case WEST:
		case NORTH_WEST:
			z = 15-x;
			x = 0;
			dirX = -1;
			break;
		case NORTH:
		case NORTH_EAST:
			x = 15-x;
			z = 15-z;
			dirZ = 1;
			break;
		case EAST:
		case SOUTH_EAST:
			z = x;
			x = 15;
			dirX = 1;
			break;
		}
		Block b = getBlockAt(x, StructureFactory.undergroundLayers-1, z);
		for(int i = 1; i < 6; i++) {
			Block b2 = getBlockAt(x+dirX*i, StructureFactory.undergroundLayers-1, z+dirZ*i);
			if(!BlockCategories.isNaturalBlock(b2.getType())) break;
			b2.setType(b.getType());
		}
	}

	private boolean setBlockAt(Block b, BlockData data, String tileState) {
		//setAir(b);
		if(!b.getBlockData().matches(data)) {
			b.setBlockData(data);
			if(tileState != null && tileState.length() > 0) setTileStateForBlock(b, tileState);
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
	
	private void setTileStateForBlock(Block b, String fullStateStr) {
		String stateStr = fullStateStr.substring(5);
		if(fullStateStr.startsWith("SIGN:")) {
			if(BlockCategories.isSignBlock(b.getType())) {
				Sign sign = (Sign)b.getState();
				String[] split = stateStr.split(StructureSaveUtil.signLineDelimiter);
				for(int i = 0; i < 4; i++) sign.setLine(0, split[i]);
			} else {
				System.out.println("not a sign block!");
			}
		} else if(fullStateStr.startsWith("BANR:")) {
			//TODO make banners from data
		} else if(fullStateStr.startsWith("SKUL:")) {
			//TODO make skulls from data
		}
	}
}
