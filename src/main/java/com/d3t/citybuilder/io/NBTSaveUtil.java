package com.d3t.citybuilder.io;

import org.bukkit.block.BlockState;

import de.tr7zw.nbtapi.NBTTileEntity;

public class NBTSaveUtil {

	public static String getSkullNBTString(BlockState b) {
		NBTTileEntity te = new NBTTileEntity(b);
		return te.toString();
	}
	
	public static void setSkullNBT(BlockState b, String nbt) {
		//TODO: how to convert string to NBT?
		/*NBTContainer c = new NBTContainer(nbt);
		Entity e;
		new NBTEntity().mergeCompound(c);
		NBTTileEntity blockEntity = new NBTTileEntity(b);
		blockEntity.mergeCompound(te);*/
	}
}
