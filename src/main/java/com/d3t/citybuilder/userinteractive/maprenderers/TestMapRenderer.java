package com.d3t.citybuilder.userinteractive.maprenderers;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import java.awt.Color;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class TestMapRenderer extends MapRenderer {

	@Override
	public void render(MapView mv, MapCanvas mc, Player p) {
		mv.setLocked(true);
		mv.setTrackingPosition(false);
		
		BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		for(int x = 0; x < 128; x++) {
			for(int y = 0; y < 128; y++) {
				Color c = new Color(2*x, 2*y, 0);
				image.setRGB(x, y, c.getRGB());
			}
		}
		mc.drawImage(0, 0, image);
		p.sendMap(mv);
	}
}
