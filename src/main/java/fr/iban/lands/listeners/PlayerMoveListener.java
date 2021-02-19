package fr.iban.lands.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.objects.Land;
import fr.iban.lands.objects.PlayerLand;
import fr.iban.lands.utils.HexColor;

public class PlayerMoveListener implements Listener {


	private LandManager landmanager;
	private LandsPlugin plugin;

	public PlayerMoveListener(LandsPlugin plugin) {
		this.landmanager = plugin.getLandManager();
		this.plugin = plugin;
	}


	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		Player player = e.getPlayer();
		final Location from = e.getFrom();
		final Location to = e.getTo();


		int x = Math.abs(from.getBlockX() - to.getBlockX());
		int y = Math.abs(from.getBlockY() - to.getBlockY());
		int z = Math.abs(from.getBlockZ() - to.getBlockZ());

		if (x == 0 && y == 0 && z == 0) return;


		//onMoveBlock :

		Chunk cfrom = from.getBlock().getChunk();
		Chunk cto = to.getBlock().getChunk();

		int cx = cfrom.getX() - cto.getX();
		int cz = cfrom.getZ() - cto.getZ();

		if(cx == 0 && cz == 0) return;


		//onMoveChunk

		landmanager.future(() -> {
			Land lfrom = landmanager.getLandAt(cfrom);
			Land lto= landmanager.getLandAt(cto);

			if(lfrom == null && lto == null) return;

			if(landmanager.isWilderness(lfrom) && landmanager.isWilderness(lto)) return;

			
			if(lto != null && lfrom != lto && !landmanager.isWilderness(lto)) {
				if(lto.isBanned(player.getUniqueId()) && !plugin.isBypassing(player)) {
					Bukkit.getScheduler().runTask(plugin, () -> {
						player.teleportAsync(from);
						player.sendMessage("§cVous ne pouvez pas entrer dans ce territoire, le propriétaire vous a banni.");
					});
					return;
				}
				if(lto instanceof PlayerLand) {
					PlayerLand pland = (PlayerLand)lto;
					if(player.getUniqueId().equals(pland.getOwner())) {
						player.sendActionBar("§8≫ " + HexColor.OLIVE.getColor() + "§lVous entrez dans votre territoire.");
						return;
					}else {
						player.sendActionBar("§8≫ " + HexColor.OLIVE.getColor() + "§lVous entrez dans le territoire de " + Bukkit.getOfflinePlayer(pland.getOwner()).getName() + ".");
					}
				}else {
					player.sendActionBar("§8≫ " + HexColor.OLIVE.getColor() + "§lVous entrez dans le territoire " + lto.getName());
				}
			}else if(lfrom != null && landmanager.isWilderness(lto)) {
				if(lfrom instanceof PlayerLand) {
					PlayerLand pland = (PlayerLand)lfrom;
					if(player.getUniqueId().equals(pland.getOwner())) {
						player.sendActionBar("§8≫ " + HexColor.MARRON.getColor() + "§lVous quittez votre territoire.");
						return;
					}else {
						player.sendActionBar("§8≫ " + HexColor.MARRON.getColor() + "§lVous quittez le territoire de " + Bukkit.getOfflinePlayer(pland.getOwner()).getName() + ".");
					}
				}else {
					player.sendActionBar("§8≫ " + HexColor.MARRON.getColor() + "§lVous quittez le territoire " + lfrom.getName());
				}
			}
		});
	}
}
