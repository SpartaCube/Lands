package fr.iban.lands.listeners;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;

import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.Action;
import fr.iban.lands.objects.Land;

public class PlayerTakeLecternBookListener implements Listener {

	private LandManager landManager;

	public PlayerTakeLecternBookListener(LandsPlugin plugin) {
		//this.plugin = plugin;
		this.landManager = plugin.getLandManager();
	}

	@EventHandler
	public void onTakeBook(PlayerTakeLecternBookEvent e) {
		Player player = e.getPlayer();
		Chunk chunk = e.getLectern().getChunk();
		Land land = landManager.getLandAt(chunk);
		if(land != null && !land.isBypassing(player, Action.LECTERN_TAKE)) {
			e.setCancelled(true);
		}
	}


}
