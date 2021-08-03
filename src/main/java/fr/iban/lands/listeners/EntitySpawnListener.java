package fr.iban.lands.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.Flag;
import fr.iban.lands.objects.Land;

public class EntitySpawnListener implements Listener {

	private LandManager landmanager;

	public EntitySpawnListener(LandsPlugin landsPlugin) {
		this.landmanager = landsPlugin.getLandManager();
	}

	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent e){
		if(e.getSpawnReason() != SpawnReason.SPAWNER) {
			Land land = landmanager.getLandAt(e.getLocation());
			if(land != null && land.hasFlag(Flag.NO_MOB_SPAWNING)) {
				e.setCancelled(true);
			}
		}
	}

}
