package com.d3t.citybuilder.structures;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import com.d3t.citybuilder.framework.ChunkPosition;
import com.d3t.citybuilder.io.StructureSaveUtil;
import com.d3t.citybuilder.zones.RealEstateData;

public class StructureFactory {

	public static final Material infolineBaseBlock = Material.BLACK_CONCRETE;
	public static final Material infolinePathBlock = Material.EMERALD_BLOCK;
	public static final Material infolineSecondaryPathBlock = Material.LAPIS_BLOCK;

	public static final int structureHeightTreshold = 8;
	public static final int undergroundLayers = 8;

	public World world;
	public ChunkPosition chunk;
	public String name;
	public Material[] infoline = new Material[16];
	private BlockData[][][] blocks;
	private String[][][] tileStates;
	private Structure structure;
	

	public StructureFactory(World w, ChunkPosition pos, String n, String category, boolean requireInfoline, Player creator) {
		world = w;
		if(category.length() < 2) category = "";
		chunk = pos;
		name = n;
		if (requireInfoline) {
			readInfoline();
			if (!hasValidInfoline()) {
				if (creator != null)
					creator.sendMessage("No valid Infoline found!");
				return;
			}
		}
		int totalHeight = getStructureHeightPeak();
		int lawfulHeight = getLawfulStructureHeight(totalHeight);
		fetchBlocksFromChunk();
		structure = new Structure(blocks, tileStates, name, category, creator.getName(), 1, 1, lawfulHeight, new RealEstateData[0]);
		for(int i = 0; i < 16; i++) {
			Material m = infoline[i];
			if(m == infolinePathBlock) structure.frontline[i] = StructureFrontline.MAIN_PATH;
			else if(m == infolinePathBlock) structure.frontline[i] = StructureFrontline.SECONDARY_PATH;
			else structure.frontline[i] = StructureFrontline.NOTHING;
		}
	}

	public static void onBeginCreateNewStructure(Player p, String name, String category, boolean requireInfoline) {
		ChunkPosition pos = new ChunkPosition(p.getLocation());
		StructureFactory sf = new StructureFactory(p.getWorld(), pos, name, category, requireInfoline, p);

		// Done
		StructureLibrary.registerStructure(sf.structure, category);
		if (StructureSaveUtil.saveStructure(sf.structure)) {
			p.sendMessage("Structure saved as: " + name);
		} else {
			p.sendMessage("Failed to save structure: " + name);
		}
	}

	private void readInfoline() {
		for (int i = 0; i < 16; i++) {
			infoline[i] = world.getBlockAt(i + chunk.getBlockX(), 63, chunk.getBlockZ() + 16).getType();
		}
	}

	private boolean hasValidInfoline() {
		for (Material m : infoline) {
			if (m == Material.GRASS_BLOCK || m == Material.DIRT || m == Material.STONE)
				return false;
		}
		return true;
	}

	private void fetchBlocksFromChunk() {
		int height = Math.max(getStructureHeightPeak() + undergroundLayers, undergroundLayers+4);
		blocks = new BlockData[16][height][16];
		tileStates = new String[16][height][16];
		int dataX = 0;
		for (int x = chunk.getBlockX(); x < chunk.getBlockX() + 16; x++) {
			int dataZ = 0;
			for (int z = chunk.getBlockZ(); z < chunk.getBlockZ() + 16; z++) {
				int dataY = 0;
				for (int y = 64 - undergroundLayers; y < 64 + height - undergroundLayers; y++) {
					Block b = world.getBlockAt(x, y, z);
					blocks[dataX][dataY][dataZ] = b.getBlockData();
					BlockState state = b.getState();
					if(state instanceof TileState && shouldSaveTileState((TileState)state)) {
						tileStates[dataX][dataY][dataZ] = StructureSaveUtil.getTileStateSaveString((TileState)state);
					}
					dataY++;
				}
				dataZ++;
			}
			dataX++;
		}
	}

	private boolean shouldSaveTileState(TileState state) {
		if(state instanceof Banner || state instanceof Sign || state instanceof Skull) {
			return true;
		} else {
			return false;
		}
	}
	
	private int getStructureHeightPeak() {
		int highest = 0;
		for (int x = chunk.getBlockX(); x < chunk.getBlockX() * +16; x++) {
			for (int z = chunk.getBlockZ(); z < chunk.getBlockZ() + 16; z++) {
				int h2 = getHighestBlock(x, z);
				if (h2 > highest)
					highest = h2;
			}
		}
		return highest - 63;
	}

	private int getHighestBlock(int x, int z) {
		for (int y = 128; y > 64; y--) {
			Material mat = world.getBlockAt(x, y, z).getType();
			if (isVisibleBlock(mat))
				return y;
		}
		return 63;
	}

	private int getLawfulStructureHeight(int highest) {
		for (int y = highest; y > 63; y--) {
			int visibleBlocks = 0;
			for (int x = chunk.getBlockX(); x < chunk.getBlockX() * +16; x++) {
				for (int z = chunk.getBlockZ(); z < chunk.getBlockZ() + 16; z++) {
					if (isVisibleBlock(world.getBlockAt(x, y, z).getType()))
						visibleBlocks++;
					if (visibleBlocks >= structureHeightTreshold)
						return y - 63;
				}
			}
		}
		return 0;
	}

	private boolean isVisibleBlock(Material mat) {
		return !(mat == Material.AIR || mat == Material.CAVE_AIR || mat == Material.STRUCTURE_VOID
				|| mat == Material.BARRIER);
	}
}
