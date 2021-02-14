package fr.iban.lands.listeners;

import java.util.Iterator;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.Flag;
import fr.iban.lands.objects.Land;

public class EntityExplodeListener implements Listener {

	private LandManager landmanager;

	public EntityExplodeListener(LandsPlugin landsPlugin) {
		this.landmanager = landsPlugin.getLandManager();
	}
	
	@EventHandler
	public void onExplode(EntityExplodeEvent e) {
		Iterator<Block> it = e.blockList().iterator();

		while(it.hasNext()) {
			Block block = it.next();
			Chunk chunk = block.getChunk();
			Land land = landmanager.getLandAt(chunk);

			if(land == null) continue;

			if(!land.hasFlag(Flag.EXPLOSIONS)) {
				it.remove();
			}
		}
	}
}
