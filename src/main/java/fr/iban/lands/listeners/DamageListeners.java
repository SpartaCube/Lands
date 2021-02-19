package fr.iban.lands.listeners;

import java.util.Set;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.Action;
import fr.iban.lands.enums.Flag;
import fr.iban.lands.objects.Land;
import fr.iban.lands.utils.MobUtils;

public class DamageListeners implements Listener {


	private LandManager landmanager;
	private Set<EntityType> mobs = MobUtils.mobsList;

	public DamageListeners(LandsPlugin landsPlugin) {
		this.landmanager = landsPlugin.getLandManager();
	}

	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		Land land = landmanager.getLandAt(e.getEntity().getLocation().getChunk());

		if(land == null) {
			return;
		}

		if(e.getEntity() instanceof Player) {
			if(land.hasFlag(Flag.INVINCIBLE)) {
				e.setCancelled(true);
				return;
			}
			if(land.hasFlag(Flag.PVP)) {
				e.setCancelled(false);
				return;
			}
		}


		if(e instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent event = (EntityDamageByEntityEvent)e;
			Player player = getPlayerDamager(event);
			if(player != null) {
				if(!mobs.contains(e.getEntityType()) && !land.isBypassing(player, Action.PASSIVE_KILL)) {
					e.setCancelled(true);
					return;
				}
				if(e.getEntity().getCustomName() != null && !land.isBypassing(player, Action.TAGGED_KILL)) {
					e.setCancelled(true);
					return;
				}
				if(e.getEntityType() == EntityType.ITEM_FRAME && !land.isBypassing(player, Action.BLOCK_BREAK)) {
					e.setCancelled(true);
				}
			}
		}
	}

	private Player getPlayerDamager(EntityDamageByEntityEvent event) {
		Player player = null;
		if(event.getCause() == DamageCause.PROJECTILE) {
			if(event.getDamager() instanceof Arrow) {
				Arrow arrow = (Arrow) event.getDamager();
				if(arrow.getShooter() instanceof Player) {
					player = (Player)arrow.getShooter();
				}
			}else if(event.getDamager() instanceof Trident) {
				Trident trident = (Trident)event.getDamager();
				if(trident.getShooter() instanceof Player) {
					player = (Player)trident.getShooter();
				}
			}else if(event.getDamager() instanceof Firework) {
				Firework firework = (Firework)event.getDamager();
				if(firework.getShooter() instanceof Player) {
					player = (Player)firework.getShooter();
				}
			}else if(event.getDamager() instanceof EnderPearl) {
				EnderPearl enderpearl = (EnderPearl)event.getDamager();
				if(enderpearl.getShooter() instanceof Player) {
					player = (Player)enderpearl.getShooter();
				}
			}
		}
		if(event.getDamager() instanceof Player) {
			player = (Player) event.getDamager();
		}
		return player;
	}
}
