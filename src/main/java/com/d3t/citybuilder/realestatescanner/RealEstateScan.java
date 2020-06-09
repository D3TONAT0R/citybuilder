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
import org.bukkit.entity.Player;

import com.d3t.citybuilder.framework.CBMain;
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
		
		public BlockPos add(BlockPos other) {
			return new BlockPos(x+other.x, y+other.y, z+other.z);
		}
		
		@Override
		public int hashCode() {
			return x*32768+z*256+y;
		}
		
		public BlockPos getAbove() {
			return Direction.YPos.apply(this);
		}
		
		public BlockPos getBelow() {
			return Direction.YNeg.apply(this);
		}
		
		@Override
		public BlockPos clone() {
			return new BlockPos(x,y,z);
		}
	}
	
	public enum Direction {
		XPos,
		XNeg,
		YPos,
		YNeg,
		ZPos,
		ZNeg;
		
		public BlockPos apply(BlockPos bp) {
			bp = bp.clone();
			if(this == XPos) bp.x++;
			else if(this == XNeg) bp.x--;
			else if(this == YPos) bp.y++;
			else if(this == YNeg) bp.y--;
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
	
	public enum RoomStatus {
		UNKNOWN,
		VALID,
		EXTERIOR,
		EXTRA_ROOM,
		NOT_AIRTIGHT_NOR_EXTERIOR,
		TOO_DARK_SL,
		TOO_DARK_BL,
		TOO_SMALL,
		TOO_LARGE;
		
		public boolean isInterior() {
			return this == VALID || this == TOO_DARK_BL || this == TOO_DARK_SL || this == TOO_SMALL || this == TOO_LARGE;
		}
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
	
	public enum LogLevel {
		INFO,
		WARNING,
		ERROR
	}
	
	public class ScanLogMessage {
		public LogLevel level;
		public String message;
		
		public ScanLogMessage(LogLevel l, String msg) {
			level = l;
			message = msg;
		}
		
		public void logToPlayer(Player p) {
			String msg = "§3<ImmoScanner>";
			if(level == LogLevel.INFO) msg += " [INFO] ";
			else if(level == LogLevel.WARNING) msg += " §e[WARN] ";
			else if(level == LogLevel.ERROR) msg += " §c[ERROR] ";
			msg += message;
			p.sendMessage(msg);
		}
	}
	
	public static final int maxFloorBlocks = 1000;
	public static final int maxVolume = 3000;
	public static final int maxExtentsFromOrigin = 32;
	public static final float minAvgSkylightForBalcony = 12;
	public static final float minAvgSkylightForInterior = 7.5F;
	public static final float minAvgBlocklightForInterior = 9.5F;
	public static final int minLightPerBlock = 6;
	public static final int minRoomSize = 10;
	public static final int maxRoomsize = 400;
	public static final Material requiredKitchenBlock = Material.CRAFTING_TABLE;
	public static final Material[] extraKitchenBlocks = new Material[] {Material.FURNACE,Material.SMOKER,Material.CAULDRON,Material.IRON_BLOCK,Material.BARREL,Material.CHEST};
	public static final int minKitchenBlocks = 4;
	public static final Material requiredBathroomBlock = Material.HOPPER;
	public static final Material[] extraBathroomBlocks = new Material[] {Material.CAULDRON,Material.TRIPWIRE_HOOK,Material.WHITE_BANNER,Material.QUARTZ_STAIRS,Material.SMOOTH_QUARTZ_STAIRS,Material.DIORITE_STAIRS};
	public static final int minBathroomBlocks = 3;
	
	public static final Direction[] allPlanarDirections = new Direction[] {Direction.XPos, Direction.XNeg, Direction.ZPos, Direction.ZNeg};
	public static final Direction[] allDirections = new Direction[] {Direction.XPos, Direction.XNeg, Direction.YPos, Direction.YNeg, Direction.ZPos, Direction.ZNeg};
	
	World world;
	BlockPos origin;
	int originX;
	int originY;
	int originZ;
	boolean useChunkBoundaries;
	int chunkX;
	int chunkZ;
	
	HashMap<BlockPos, Integer> scannedBlocks = new HashMap<BlockPos, Integer>();
	boolean[] roomAirtightness;

	ArrayList<BlockPos> currentRoomScannedAir;
	boolean currentRoomNotAirtight;
	
	float[] averageSkylightPerRoom;
	float[] averageBlocklightPerRoom;
	int[] tooDarkFloorSpacesPerRoom;
	int[] roomAreas;
	RoomStatus[] roomStatus;
	
	ScanStatus status;
	
	public int interiorRoomCount = 0;
	public int interiorArea = 0;
	public int balconyAndTerraceCount = 0;
	public int exteriorArea = 0;
	public boolean hasKitchen;
	public boolean hasBathroom;
	
	public float qualityRating;
	public float suggestedQualityClass;

	private ArrayList<ScanLogMessage> logMessages = new ArrayList<ScanLogMessage>();
	
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
		roomAirtightness = new boolean[roomNum];
		for(int i = 0; i < roomNum; i++) {
			currentRoomScannedAir = new ArrayList<BlockPos>();
			//Get the first position registered in room i
			BlockPos pos = null;
			for(BlockPos bp : scannedBlocks.keySet()) {
				if(scannedBlocks.get(bp) == i) pos = bp; 
			}
			if(pos != null) {
				currentRoomNotAirtight = false;
				scanAir(pos);
				roomAirtightness[i] = !currentRoomNotAirtight;
				String msg = "Room # "+i+": "+currentRoomScannedAir.size()+" air volume, "+(currentRoomNotAirtight?"NOT":"")+" airtight";
				logMessage(LogLevel.INFO, msg);
				CBMain.log.info(msg);
			} else {
				CBMain.log.warning("Failed to check air in room "+i+"! No floor space was found.");
			}
		}
		//Do an illumination scan (light sources and skylight) and count the room areas
		averageSkylightPerRoom = new float[roomNum];
		averageBlocklightPerRoom = new float[roomNum];
		tooDarkFloorSpacesPerRoom = new int[roomNum];
		roomAreas = new int[roomNum];
		for(int i = 0; i < roomNum; i++) {
			float bl = 0;
			float sl = 0;
			BlockPos[] floor = getAllScannedFloorBlocksInRoom(i);
			for(BlockPos pos : floor) {
				Block b = world.getBlockAt(pos.x, pos.y, pos.z);
				bl += b.getLightFromBlocks();
				sl += b.getLightFromSky();
				if(sl+bl <= minLightPerBlock) {
					tooDarkFloorSpacesPerRoom[i]++;
				}
				roomAreas[i]++;
			}
			averageSkylightPerRoom[i] = sl/floor.length;
			averageBlocklightPerRoom[i] = bl/floor.length;
		}
		//count actual rooms
		roomStatus = new RoomStatus[roomNum];
		for(int i = 0; i < roomStatus.length; i++) {
			roomStatus[i] = getRoomStatus(i);
		}
		//check for bathrooms and kitchen
		for(int i = 0; i < roomNum; i++) {
			if(roomStatus[i].isInterior()) {
				if(!hasKitchen) {
					if(containsBlocks(i, requiredKitchenBlock, extraKitchenBlocks, minKitchenBlocks)) {
						hasKitchen = true;
						if(i != 0) {
							//If the room is small enough, consider it a dedicated kitchen (not part of any actual room)
							if(roomStatus[i] == RoomStatus.VALID) {
								if(roomAreas[i] < 20) {
									roomStatus[i] = RoomStatus.EXTRA_ROOM;
									logMessage(LogLevel.INFO, "Kitchen was not accounted as an actual room.");
								}
							}
						}
					}
				}
				if(!hasBathroom) {
					if(containsBlocks(i, requiredBathroomBlock, extraBathroomBlocks, minBathroomBlocks)) {
						hasBathroom = true;
						if(i == 0) {
							//The bathroom is interconnected with the main room, this should not happen
							logMessage(LogLevel.ERROR, "Bathroom was located in main room. Bathroom was ignored.");
						} else {
							//If the room is small enough, consider it a dedicated bathroom (not part of any actual room)
							if(roomStatus[i] == RoomStatus.VALID) {
								if(roomAreas[i] < 20) {
									roomStatus[i] = RoomStatus.EXTRA_ROOM;
								} else {
									logMessage(LogLevel.INFO, "Bathroom was accounted as part of another room.");
								}
							}
						}
					}
				}
			}
		}
		//Calculate the apartment's specs
		for(int i = 0; i < roomNum; i++) {
			RoomStatus rs = roomStatus[i];
			if(rs == RoomStatus.VALID) interiorRoomCount++;
			if(rs.isInterior()) interiorArea += roomAreas[i];
			if(rs == RoomStatus.EXTERIOR) {
				balconyAndTerraceCount++;
				exteriorArea += roomAreas[i];
			}
		}
		//determine the apartment's quality class
		int maxClass = 4;
		qualityRating = interiorArea;
		qualityRating *= 1F+Math.max((interiorRoomCount-1f)/5f, 0);
		if(exteriorArea > 8) {
			float ext = Math.min(exteriorArea*1.4F, 60);
			ext *= 1+Math.max((balconyAndTerraceCount-1f)/3f, 0);
			qualityRating += ext;
		}
		if(!hasBathroom) {
			qualityRating *= 0.75F;
			maxClass = 3;
		}
		if(!hasKitchen) {
			qualityRating *= 0.6F;
			maxClass = 2;
		}
		suggestedQualityClass = Math.min(qualityRating/120F, maxClass);
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
		for(Direction d : allPlanarDirections) {
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
	
	private boolean scanAir(BlockPos pos) {
		//If there are more than 16 blocks of air above or below, consider this room to be not airtight
		int limit = 16;
		if(getAllAirspacesInDirection(pos, Direction.YPos, limit).length >= limit || getAllAirspacesInDirection(pos, Direction.YNeg, limit).length >= limit) {
			return false;
		}
		for(BlockPos n : findSpaceNeighbors(pos, true, false)) {
			if(!scanAir(n)) return false;
		}
		return true;
	}
	
	//Returns all spaces near the position
	private BlockPos[] findSpaceNeighbors(BlockPos pos, boolean mustBeAir, boolean horizontalOnly) {
		ArrayList<BlockPos> list = new ArrayList<BlockPos>();
		Direction[] dirSet = horizontalOnly ? allPlanarDirections : allDirections;
		for(Direction d : allDirections) {
			BlockPos n = d.apply(pos);
			if((isAir(n) && mustBeAir) && !currentRoomScannedAir.contains(n)) list.add(n);
		}
		BlockPos[] arr = new BlockPos[list.size()];
		return list.toArray(arr);
	}
	
	//Expands the air scan horizontally to contain it's walls 
	private void expandCurrentAirScanIntoWalls() {
		for(BlockPos bp : currentRoomScannedAir) {
			for(BlockPos n : findSpaceNeighbors(bp, false, true))
			currentRoomScannedAir.add(n);
		}
	}
	
	private RoomStatus getRoomStatus(int i) {
		if(!roomAirtightness[i]) {
			if(averageSkylightPerRoom[i] >= minAvgSkylightForBalcony) {
				return RoomStatus.EXTERIOR;
			} else {
				return RoomStatus.NOT_AIRTIGHT_NOR_EXTERIOR;
			}
		}
		if(roomAreas[i] < minRoomSize) {
			return RoomStatus.TOO_SMALL;
		}
		if(roomAreas[i] > maxRoomsize) {
			return RoomStatus.TOO_LARGE;
		}
		if(averageSkylightPerRoom[i] < minAvgSkylightForInterior) {
			return RoomStatus.TOO_DARK_SL;
		}
		if(averageBlocklightPerRoom[i] < minAvgBlocklightForInterior) {
			return RoomStatus.TOO_DARK_BL;
		}
		return RoomStatus.VALID;
	}
	
	private boolean containsBlocks(int room, Material mustInclude, Material[] optionalBlocks, int minCount) {
		currentRoomScannedAir = new ArrayList<BlockPos>();
		scanAir(getFirstScannedFloorBlockInRoom(room));
		expandCurrentAirScanIntoWalls();
		boolean includesRequiredBlock = mustInclude == null;
		int blockNum = 0;
		for(BlockPos bp : currentRoomScannedAir) {
			Material mat = world.getBlockAt(bp.x, bp.y, bp.z).getType();
			if(!includesRequiredBlock && mat == mustInclude) {
				includesRequiredBlock = true;
				blockNum++;
			}
			for(Material m2 : optionalBlocks) {
				if(mat == m2) {
					blockNum++;
				}
			}
		}
		return includesRequiredBlock && blockNum >= minCount;
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
	
	private BlockPos[] getAllAirspacesInDirection(BlockPos pos, Direction dir, int maxDistance) {
		ArrayList<BlockPos> list = new ArrayList<BlockPos>();
		for(int i = 0; i < maxDistance; i++) {
			pos = dir.apply(pos);
			if(isAir(pos)) {
				list.add(pos);
			} else {
				break;
			}
		}
		BlockPos[] arr = new BlockPos[list.size()];
		arr = list.toArray(arr);
		return arr;
	}
	
	private BlockPos getFirstScannedFloorBlockInRoom(int roomNum) {
		ArrayList<BlockPos> list = new ArrayList<BlockPos>();
		for(BlockPos bp : scannedBlocks.keySet()) {
			if(scannedBlocks.get(bp) == roomNum) return bp;
		}
		return null;
	}
	
	private BlockPos[] getAllScannedFloorBlocksInRoom(int roomNum) {
		ArrayList<BlockPos> list = new ArrayList<BlockPos>();
		for(BlockPos bp : scannedBlocks.keySet()) {
			if(scannedBlocks.get(bp) == roomNum) list.add(bp);
		}
		BlockPos[] arr = new BlockPos[list.size()];
		arr = list.toArray(arr);
		return arr;
	}
	
	private void logMessage(LogLevel l, String msg) {
		logMessages.add(new ScanLogMessage(l, msg));
	}
}
