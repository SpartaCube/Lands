package fr.iban.lands.listeners;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;

import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.Action;
import fr.iban.lands.objects.Land;

public class HangingListeners implements Listener {
	
	private LandManager landmanager;

	public HangingListeners(LandsPlugin landsPlugin) {
		this.landmanager = landsPlugin.getLandManager();
	}
	
	@EventHandler
	public void onHangingBreak(HangingBreakByEntityEvent e) {	
		Chunk chunk = e.getEntity().getChunk();
		Land land = landmanager.getLandAt(chunk);

		if(land != null) {
			if(e.getRemover() instanceof Player && land.isBypassing((Player) e.getRemover(), Action.BLOCK_BREAK)) {
				return;
			}
			e.setCancelled(true);
		}
		
	}
	
	@EventHandler
	public void onHangingBreak(HangingPlaceEvent e) {	
		Chunk chunk = e.getEntity().getChunk();
		Land land = landmanager.getLandAt(chunk);


		if(land != null && !land.isBypassing(e.getPlayer(), Action.BLOCK_PLACE)) {
			e.setCancelled(true);
		}
		
	}

}
