package fr.iban.lands.listeners;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;

import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.objects.Land;

public class PistonListeners implements Listener {

	private LandManager landmanager;

	public PistonListeners(LandsPlugin landsPlugin) {
		this.landmanager = landsPlugin.getLandManager();
	}
	
	@EventHandler
	public void onPiston(BlockPistonExtendEvent e) {

		Land pistonLand= landmanager.getLandAt(e.getBlock().getChunk());

		for(Block block : e.getBlocks()) {
			Chunk chunk = block.getRelative(e.getDirection()).getChunk();
			Land land = landmanager.getLandAt(chunk);

			if(land == null || land == pistonLand) 
				continue;

			e.setCancelled(true);
		}

	}

}
