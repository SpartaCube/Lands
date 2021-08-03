package fr.iban.lands.listeners;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.Flag;
import fr.iban.lands.events.LandEnterEvent;
import fr.iban.lands.objects.Land;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class LandListeners implements Listener {

	private LandsPlugin plugin;

	public LandListeners(LandsPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onEnter(LandEnterEvent e) {
		Player player = e.getPlayer();
		Land lto = e.getToLand();
		Land lfrom = e.getFromLand();

		if(lfrom.equals(lto)) return;

		if(lto.isBanned(player.getUniqueId()) && !plugin.isBypassing(player) && !player.hasPermission("group.support")) {
			player.sendMessage("§cVous ne pouvez pas entrer dans ce territoire, le propriétaire vous a banni.");
			e.setCancelled(true);
			return;
		}
		
		Component fromName = Component.text(lfrom.getName()).color(TextColor.fromHexString("#F57C00"));
		UUID ownerFrom = lfrom.getOwner();
		if(ownerFrom != null) {
			String ownerName = Bukkit.getOfflinePlayer(ownerFrom).getName();
			fromName = fromName.append(Component.text(" ☗ "+ownerName).color(TextColor.fromHexString("#FFA726")));
		}

		Component toName = Component.text(lto.getName()).color(TextColor.fromHexString("#AFB42B"));
		UUID ownerTo = lto.getOwner();
		if(ownerTo != null) {
			String ownerName = Bukkit.getOfflinePlayer(ownerTo).getName();
			toName = toName.append(Component.text(" ☗ "+ownerName).color(TextColor.fromHexString("#D4E157")));
		}
		
		Component finalComponent = fromName.append(Component.text(" §8➜ ").append(toName)).decorate(TextDecoration.BOLD);

		player.sendActionBar(finalComponent);

		if(lfrom.hasFlag(Flag.INVISIBLE)) {
			player.removePotionEffect(PotionEffectType.INVISIBILITY);
		}
		
		if(lto.hasFlag(Flag.INVISIBLE)) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
		}
		
	}

}
