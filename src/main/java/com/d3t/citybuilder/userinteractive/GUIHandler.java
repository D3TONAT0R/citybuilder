package com.d3t.citybuilder.userinteractive;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import com.d3t.citybuilder.framework.CBMain;

public class GUIHandler {

	static int test = 0;

	public static void update(Player p) {
		if (CBMain.getEditModeForPlayer(p) > 0) {
			test = (test+1) % 100;
			BossBar bar = CBMain.getServerInstance().createBossBar("Test", BarColor.BLUE, BarStyle.SEGMENTED_10, new BarFlag[] {});
			bar.setProgress(test/100d);
			bar.setVisible(true);
		}
	}
}
