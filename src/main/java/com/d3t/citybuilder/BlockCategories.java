package com.d3t.citybuilder;

import org.bukkit.Material;

public class BlockCategories {

	public static final Material[] naturalBlocks = new Material[] {
		Material.GRASS_BLOCK, Material.DIRT, Material.SAND, Material.PODZOL, Material.COARSE_DIRT, Material.GRAVEL, Material.WATER
	};
	
	public static final Material[] editDenyingBlock = new Material[] {
		Material.COMMAND_BLOCK, Material.REPEATING_COMMAND_BLOCK, Material.CHAIN_COMMAND_BLOCK, Material.STRUCTURE_BLOCK, Material.JIGSAW
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
		return contains(editDenyingBlock, block);
	}
}
