package com.d3t.citybuilder.userinteractive;

import java.util.ArrayList;

import org.bukkit.Particle;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import com.d3t.citybuilder.framework.CBMain;

public class GUIHandler {

	static int test = 0;
	static BossBar bar;
	
	static ArrayList<Player> chunkGridUsers = new ArrayList<Player>();

	public static void update(Player p) {
		updateProgressBars(p);
		updateParticles(p);
	}
	
	static void updateProgressBars(Player p) {
		if(bar == null) bar = CBMain.getServerInstance().createBossBar("Test", BarColor.BLUE, BarStyle.SEGMENTED_10, new BarFlag[] {});
		if (CBMain.getEditModeForPlayer(p) > 0) {
			test = (test+1) % 100;
			bar.setProgress(test/100d);
			if(!bar.getPlayers().contains(p)) {
				bar.setVisible(true);
				bar.addPlayer(p);
			}
		} else {
			bar.removePlayer(p);
		}
	}
	
	static void updateParticles(Player p) {
		if (CBMain.getEditModeForPlayer(p) > 0) {
			int x = p.getLocation().getBlockX();
			int y = p.getLocation().getBlockY();
			int z = p.getLocation().getBlockZ();
			int amount = 5;
			p.getWorld().spawnParticle(Particle.SPELL_INSTANT, x, y-4, z+(16-(z%16)), amount, 12f, 0f, 0f, 0);
			p.getWorld().spawnParticle(Particle.SPELL_INSTANT, x, y-4, z-(z%16), amount, 12f, 0f, 0f, 0);
			p.getWorld().spawnParticle(Particle.SPELL_INSTANT, x+(16-(x%16)), y-4, z, amount, 0f, 0f, 12f, 0);
			p.getWorld().spawnParticle(Particle.SPELL_INSTANT, x-(x%16), y-4, z, amount, 0f, 0f, 12f, 0);
		}
	}
}
