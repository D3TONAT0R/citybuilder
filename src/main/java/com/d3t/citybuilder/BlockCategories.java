package com.d3t.citybuilder;

import org.bukkit.Material;

public class BlockCategories {

	public static final Material[] naturalBlocks = new Material[] {
		Material.GRASS_BLOCK, Material.DIRT, Material.SAND, Material.PODZOL, Material.COARSE_DIRT, Material.GRAVEL, Material.WATER
	};
	
	public static final Material[] editDenyingBlocks = new Material[] {
		Material.COMMAND_BLOCK, Material.REPEATING_COMMAND_BLOCK, Material.CHAIN_COMMAND_BLOCK, Material.STRUCTURE_BLOCK, Material.JIGSAW
	};
	
	public static final Material[] interiorFinishingStageBlocks = new Material[] {
		Material.GLASS, Material.GLASS_PANE,
	};
	
	private static boolean contains(Material[] arr, Material block) {
		for(Material m : arr) {
			if(block == m) return true;
		}
		return false;
	}
	
	public static boolean isNaturalBlock(Material block) {
		return contains(naturalBlocks, block);
	}
	
	public static boolean isEditDenyingBlock(Material block) {
		return contains(editDenyingBlocks, block);
	}
	
	public static boolean isInteriorStageBlock(Material block) {
		return contains(interiorFinishingStageBlocks, block);
	}
}
