package fr.iban.lands.listeners;

import org.bukkit.Chunk;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
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
		
		Player player = getPlayerDamager(e);
		
		if(player != null) {			
			
			Land land = landmanager.getLandAt(chunk);

			if(land != null) {
				if(land.isBypassing(player, Action.BLOCK_BREAK)) {
					return;
				}
				e.setCancelled(true);
			}
		}

	}

	@EventHandler
	public void onHangingBreak(HangingPlaceEvent e) {	
		Chunk chunk = e.getEntity().getChunk();
		Player player = e.getPlayer();
		Land land = landmanager.getLandAt(chunk);


		if(land != null && !land.isBypassing(player, Action.BLOCK_PLACE)) {
			e.setCancelled(true);
		}

	}
	
	private Player getPlayerDamager(HangingBreakByEntityEvent event) {
		Player player = null;
		if(event.getCause() == RemoveCause.ENTITY) {
			if(event.getRemover() instanceof Arrow) {
				Arrow arrow = (Arrow) event.getRemover();
				if(arrow.getShooter() instanceof Player) {
					player = (Player)arrow.getShooter();
				}
			}else if(event.getRemover() instanceof Trident) {
				Trident trident = (Trident)event.getRemover();
				if(trident.getShooter() instanceof Player) {
					player = (Player)trident.getShooter();
				}
			}else if(event.getRemover() instanceof Firework) {
				Firework firework = (Firework)event.getRemover();
				if(firework.getShooter() instanceof Player) {
					player = (Player)firework.getShooter();
				}
			}else if(event.getRemover() instanceof EnderPearl) {
				EnderPearl enderpearl = (EnderPearl)event.getRemover();
				if(enderpearl.getShooter() instanceof Player) {
					player = (Player)enderpearl.getShooter();
				}
			}
		}
		if(event.getRemover() instanceof Player) {
			player = (Player) event.getRemover();
		}
		return player;
	}

}
