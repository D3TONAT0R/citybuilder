package com.d3t.citybuilder.util;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;

import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.TileEntity;

public class SkullDataUtil {
	public static NBTTagCompound getSkullData(Block block) {
		CraftWorld cw = (CraftWorld)block.getWorld();
		Location loc = block.getLocation();
		TileEntity tileEntity = cw.getHandle().getTileEntity(new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
		NBTTagCompound nbt = new NBTTagCompound();
		tileEntity.save(nbt);
		return nbt;
	}
	
	public static void setSkullData(Block block, NBTTagCompound compound) {
		NBTTagCompound nbt = compound.clone();
		Location loc = block.getLocation();
		nbt.setInt("x", loc.getBlockX());
		nbt.setInt("y", loc.getBlockY());
		nbt.setInt("z", loc.getBlockZ());
		CraftWorld cw = (CraftWorld)block.getWorld();
		TileEntity te = cw.getHandle().getTileEntity(new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
		te.load(nbt);
		te.update();
	}
}
