package fr.iban.lands.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;

public class TeleportListener implements Listener {
	
	private LandsPlugin plugin;
	private LandManager manager;

	public TeleportListener(LandsPlugin plugin) {
		this.manager = plugin.getLandManager();
		this.plugin = plugin;
	}
	
	
	@EventHandler
	public void onTeleport(PlayerTeleportEvent e) {
		Location from = e.getFrom();
		manager.getLandAtAsync(e.getTo().getChunk()).thenAccept(land -> {
			Player player = e.getPlayer();
			if(land.isBanned(player.getUniqueId()) && !plugin.isBypassing(player) && !player.hasPermission("group.support")) {
				Bukkit.getScheduler().runTask(plugin, () -> {
					player.teleportAsync(from);
					player.sendMessage("§cVous ne pouvez pas entrer dans ce territoire, le propriétaire vous a banni.");
				});
				return;
			}
		});
	}
	

}
