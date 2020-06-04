package com.d3t.citybuilder.realestatescanner;

import java.rmi.UnexpectedException;
import java.util.ArrayList;
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
		
		@Override
		public int hashCode() {
			return x*32768+z*256+y;
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
	
	public enum ScanStatus {
		SCANNING_FLOOR,
		SCANNING_ROOMS,
		SCANNING_LIGHT,
		SCANNING_AIRTIGHTNESS,
		SCANNING_FEATURES,
		SCANNING_ACCESS,
		ERROR_TOO_SMALL,
		ERROR_TOO_LARGE,
		ERROR_OUT_OF_BOUNDS,
		ERROR_NOT_AIRTIGHT,
		ERROR_TOO_DARK,
		ERROR_NOT_ACCESSIBLE;
		
		public boolean isError() {
			return this.name().startsWith("ERROR");
		}
	}
	
	public class NeighborData {
		public NeighborCheckResult result;
		public Direction direction;
		public BlockPos pos;
		
		public NeighborData(NeighborCheckResult r, Direction d, BlockPos p) {
			result = r;
			direction = d;
			pos = p;
		}
	}
	
	public static final int maxFloorBlocks = 1000;
	public static final int maxVolume = 3000;
	public static final int maxExtentsFromOrigin = 32;
	
	public static final Direction[] allDirections = new Direction[] {Direction.XPos, Direction.XNeg, Direction.ZPos, Direction.ZNeg};
	
	World world;
	BlockPos origin;
	int originX;
	int originY;
	int originZ;
	boolean useChunkBoundaries;
	int chunkX;
	int chunkZ;
	
	HashMap<BlockPos, Integer> scannedBlocks = new HashMap<BlockPos, Integer>();
	int roomCounter = 0;
	
	ScanStatus status;
	
	public RealEstateScan(World w, int x, int y, int z, boolean chunkBound) throws Exception {
		world = w;
		originX = x;
		originY = y;
		originZ = z;
		if(chunkBound) {
			useChunkBoundaries = true;
			int[] chunk = getChunk(new BlockPos(x,y,z));
			chunkX = chunk[0];
			chunkZ = chunk[1];
		}
		//Start a recurive scan across the whole apartment
		status = ScanStatus.SCANNING_FLOOR;
		scanFloorPosition(new BlockPos(x,y,z));
		//Assign a room number for each separated room
		int attempts = 32;
		int roomNum = 0;
		while(attempts > 0) {
			attempts--;
			if(scannedBlocks.containsValue(-1)) {
				//Find the first non-assigned space, then do a recursive check for the whole room
				BlockPos pos = null;
				for(BlockPos bp : scannedBlocks.keySet()) {
					if(scannedBlocks.get(bp) == -1) {
						pos = bp;
						break;
					}
				}
				if(pos != null) {
					scanRoom(pos, roomNum);
					roomNum++;
				} else {
					throw new UnexpectedException("An unassigned space was located but could not be feteched!");
				}
			} else {
				//All rooms have been assigned
				attempts = 0;
			}
		}
		if(scannedBlocks.containsValue(-1)) {
			throw new UnexpectedException("An unassigned space was located after the assignment!");
		}
		//Check airtightness in each room
		for(int i = 0; i < roomNum; i++) {
			
		}
		//TODO: count actual rooms
		//TODO: do an illumination scan (light sources and skylight)
		//TODO: check for bathrooms and kitchen
		//TODO: determine the apartment's quality class
	}
	
	private void scanFloorPosition(BlockPos pos) {
		if(status.isError()) return;
		if(canStand(pos)) {
			if(scannedBlocks.size() < maxFloorBlocks) {
				scannedBlocks.put(pos, -1);
				//roomSize[roomNum]++;
				NeighborData[] neighbors = findFloorNeighbors(pos);
				for(NeighborData n : neighbors) {
					if(isWithinBoundaries(n.pos)) {
						scanFloorPosition(n.pos);
					} else {
						status = ScanStatus.ERROR_OUT_OF_BOUNDS;
					}
				}
			} else {
				//Maximum reached, abort
				status = ScanStatus.ERROR_TOO_LARGE;
			}
		} else {
			//The area is not valid, this should not happen
		}
	}
	
	private NeighborData[] findFloorNeighbors(BlockPos from) {
		ArrayList<NeighborData> list = new ArrayList<NeighborData>();
		for(Direction d : allDirections) {
			NeighborCheckResult result = checkNeighbor(from, d);
			if(result != NeighborCheckResult.Invalid) {
				BlockPos pos = d.apply(from);
				//A valid neighbor was found, continue the recursion
				if(result == NeighborCheckResult.Ascending) pos = pos.getAbove();
				else if(result == NeighborCheckResult.Descending) pos = pos.getBelow();
				if(result == NeighborCheckResult.Door) {
					if(crossDoor(pos, d)) {
						//The door was successfully crossed, move through the door
						pos = d.apply(pos);
						if(!isChecked(pos)) list.add(new NeighborData(result,d,pos));
					}
				} else {
					//It is a regular neighbor spot
					if(!isChecked(pos)) list.add(new NeighborData(result,d,pos));
				}
			}
		}
		NeighborData[] arr = new NeighborData[list.size()];
		return list.toArray(arr);
	}
	
	private NeighborCheckResult checkNeighbor(BlockPos from, Direction dir) {
		BlockPos pos = dir.apply(from);
		if(world.getBlockAt(pos.x, pos.y, pos.z).getBlockData() instanceof Door && world.getBlockAt(pos.x, pos.y+1, pos.z).getBlockData() instanceof Door) {
			//The neighbor contains a door
			return NeighborCheckResult.Door;
		}
		if(isWall(pos)) return NeighborCheckResult.Invalid;
		//TODO: only account correctly facing stairs and slabs for acending/descending
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
	
	private void scanRoom(BlockPos pos, int room) {
		scannedBlocks.put(pos, room);
		NeighborData[] neighbors = findFloorNeighbors(pos);
		for(NeighborData n : neighbors) {
			if(n.result != NeighborCheckResult.Door && n.result != NeighborCheckResult.Invalid) {
				if(scannedBlocks.get(n.pos) == -1) scanRoom(n.pos, room);
			}
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
	
	private int[] getChunk(BlockPos pos) {
		int[] i = new int[2];
		i[0] = (int)Math.floor(pos.x/16);
		i[1] = (int)Math.floor(pos.z/16);
		return i;
	}
	
	private boolean isWithinBoundaries(BlockPos pos) {
		if(useChunkBoundaries) {
			int[] c = getChunk(pos);
			if(c[0] != chunkX || c[1] != chunkZ) {
				//Not within the origin chunk
				return false;
			} else {
				int icx = pos.x%16;
				int icy = pos.z%16;
				if(icx == 0 || icy == 0 || icx == 15 || icy == 15) {
					//At the edge of the chunk: There is no way there will be a wall inside the same chunk
					return false;
				}
			}
			return true;
		} else {
			if(Math.abs(pos.x-originX) > maxExtentsFromOrigin || Math.abs(pos.z-originZ) > maxExtentsFromOrigin) {
				//Too far from origin point
				return false;
			}
			return true;
		}
	}
}
