package fr.iban.lands.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.events.LandEnterEvent;
import fr.iban.lands.objects.Land;

public class TeleportListener implements Listener {

	private LandManager manager;
	private LandsPlugin plugin;

	public TeleportListener(LandsPlugin plugin) {
		this.manager = plugin.getLandManager();
		this.plugin = plugin;
	}


	@EventHandler
	public void onTeleport(PlayerTeleportEvent e) {
		Location from = e.getFrom();
		Location to = e.getTo();
		manager.future(() -> {
			Land lfrom = manager.getLandAt(from);
			Land lto = manager.getLandAt(to);
			Player player = e.getPlayer();

			LandEnterEvent enter = new LandEnterEvent(player, lfrom, lto);
			
			Bukkit.getScheduler().runTask(plugin, () -> {
				Bukkit.getPluginManager().callEvent(enter);
				
				if(enter.isCancelled()) {
					player.teleportAsync(from);
					return;
				}
			});
		});
	}


}
