package com.d3t.citybuilder.realestatescanner;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;

import com.d3t.citybuilder.structures.BlockCategories;

public class RealEstateScan {
	
	public class BlockPos {

		public int x;
		public int y;
		public int z;
		
		public BlockPos(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		@Override
		public boolean equals(Object other) {
			if(other instanceof BlockPos) {
				BlockPos bp = (BlockPos)other;
				return x == bp.x && y == bp.y && z == bp.z;
			} else {
				return false;
			}
		}
		
		public BlockPos getAbove() {
			BlockPos bp = clone();
			bp.y++;
			return bp;
		}
		
		public BlockPos getBelow() {
			BlockPos bp = clone();
			bp.y--;
			return bp;
		}
		
		@Override
		public BlockPos clone() {
			return new BlockPos(x,y,z);
		}
	}
	
	public enum Direction {
		XPos,
		XNeg,
		ZPos,
		ZNeg;
		
		public BlockPos apply(BlockPos bp) {
			bp = bp.clone();
			if(this == XPos) bp.x++;
			else if(this == XNeg) bp.x--;
			else if(this == ZPos) bp.z++;
			else if(this == ZNeg) bp.z--;
			return bp;
		}
	}
	
	public enum NeighborCheckResult {
		Invalid,
		Valid,
		Ascending,
		Descending,
		Door
	}
	
	public static final int maxBlocksToScan = 2000;
	public static final int maxExtentsFromOrigin = 32;
	
	World world;
	BlockPos origin;
	int originX;
	int originY;
	int originZ;
	
	HashMap<BlockPos, Integer> scannedBlocks = new HashMap<BlockPos, Integer>();
	int roomCounter = 0;
	
	public RealEstateScan(World w, int x, int y, int z) {
		world = w;
		originX = x;
		originY = y;
		originZ = z;
		//Start a recurive scan across the whole apartment
		scanPosition(new BlockPos(x,y,z), (byte)0);
		//TODO: do a illumination scan (light sources and skylight)
		//TODO: double check room detection and check for airtightness
		//TODO: check for bathrooms and kitchen
		//TODO: determine the apartment's quality class
	}
	
	private void scanPosition(BlockPos pos, int roomNum) {
		if(canStand(pos)) {
			if(scannedBlocks.size() < maxBlocksToScan) {
				scannedBlocks.put(pos, roomNum);
				//roomSize[roomNum]++;
				findNeighbors(pos,roomNum);
			} else {
				//Maximum reached, abort
				
			}
		} else {
			//The area is not valid, this should not happen
		}
	}
	
	private void findNeighbors(BlockPos from, int roomNum) {
		Direction[] dirs = new Direction[] {
			Direction.XPos,
			Direction.XNeg,
			Direction.ZPos,
			Direction.ZNeg
		};
		for(Direction d : dirs) {
			NeighborCheckResult result = checkNeighbor(from, d);
			if(result != NeighborCheckResult.Invalid) {
				BlockPos pos = d.apply(from);
				//A valid neighbor was found, continue the recursion
				if(result == NeighborCheckResult.Ascending) pos = pos.getAbove();
				else if(result == NeighborCheckResult.Descending) pos = pos.getBelow();
				else if(result == NeighborCheckResult.Door) {
					if(crossDoor(pos, d)) {
						roomCounter++;
						roomNum = roomCounter;
					} else {
						return;
					}
				}
				if(!isChecked(pos)) scanPosition(pos, roomNum);
			}
		}
	}
	
	private NeighborCheckResult checkNeighbor(BlockPos from, Direction dir) {
		BlockPos pos = dir.apply(from);
		if(world.getBlockAt(pos.x, pos.y, pos.z).getBlockData() instanceof Door && world.getBlockAt(pos.x, pos.y+1, pos.z).getBlockData() instanceof Door) {
			//The neighbor contains a door
			return NeighborCheckResult.Door;
		}
		if(isWall(pos)) return NeighborCheckResult.Invalid;
		if(canStand(pos)) {
			//The position is valid without any elevation changes
			return NeighborCheckResult.Valid;
		} else if(canStand(pos.getAbove()) && canStandAndJump(from)) {
			//The position is above and can be climbed from the previous position
			return NeighborCheckResult.Ascending;
		} else if(canStandAndJump(pos.getBelow())) {
			//The position is below and can be reached from the previous position
			return NeighborCheckResult.Descending;
		} else {
			//None of the above apply
			return NeighborCheckResult.Invalid;
		}
	}
	
	//Checks if the block is a valid floor block. The block must be solid (Exceptions: glass, stairs, slabs)
	private boolean isValidFloor(BlockPos pos) {
		Block b = world.getBlockAt(pos.x, pos.y, pos.z);
		Material m = b.getType();
		if(m.isSolid() || BlockCategories.isGlassBlock(m) || b instanceof Stairs || b instanceof Slab) {
			return true;
		} else {
			return false;
		}
	}
	
	//Checks if the bottom block where the "player" stands is a valid airy block (e.g. air or carpet)
	private boolean isValidGroundBlock(BlockPos pos) {
		return BlockCategories.isNonObstructingBlock(world.getBlockAt(pos.x, pos.y, pos.z).getType());
	}
	
	private boolean isAir(BlockPos pos) {
		Material m = world.getBlockAt(pos.x, pos.y, pos.z).getType();
		return !BlockCategories.isCarpet(m) && BlockCategories.isNonObstructingBlock(m);
	}
	
	//Performs a stand check at the specified location, if a player can stand at that location without obstructions
	private boolean canStand(BlockPos pos) {
		return isValidFloor(pos.getBelow()) && isValidGroundBlock(pos) && isAir(pos.getAbove());
	}
	
	//Checks if a player can "jump" in that spot. This is used to determine wether a player can climb onto a higher elevation from that position
	private boolean canStandAndJump(BlockPos pos) {
		return canStand(pos) && canStand(pos.getAbove());
	}
	
	//Checks if the specified location contains an unpassable wall
	private boolean isWall(BlockPos pos) {
		return !isValidGroundBlock(pos) && !isValidGroundBlock(pos.getAbove());
	}
	
	private boolean isChecked(BlockPos pos) {
		return scannedBlocks.containsKey(pos);
	}
	
	private boolean crossDoor(BlockPos pos, Direction crossDir) {
		//A door will only be crossed if it faces the right direction
		Door d = (Door)world.getBlockAt(pos.x, pos.y, pos.z).getBlockData();
		BlockFace facing = d.getFacing();
		if((facing == BlockFace.NORTH && crossDir == Direction.ZPos) ||
			(facing == BlockFace.EAST && crossDir == Direction.XNeg) ||
			(facing == BlockFace.SOUTH && crossDir == Direction.ZNeg) ||
			(facing == BlockFace.WEST && crossDir == Direction.XPos)) {
			pos = crossDir.apply(pos);
			return canStand(pos) && !isChecked(pos);
		} else {
			//The door is not facing the correct direction
			return false;
		}
	}
}
