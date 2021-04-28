package fr.iban.lands.listeners;

import java.util.Iterator;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.Action;
import fr.iban.lands.enums.Flag;
import fr.iban.lands.objects.Land;

public class EntityExplodeListener implements Listener {

	private LandManager landmanager;

	public EntityExplodeListener(LandsPlugin landsPlugin) {
		this.landmanager = landsPlugin.getLandManager();
	}
	
	@EventHandler
	public void onExplode(EntityExplodeEvent e) {
		Player player = getTargetPlayer(e);
		
		Iterator<Block> it = e.blockList().iterator();

		while(it.hasNext()) {
			Block block = it.next();
			Chunk chunk = block.getChunk();
			Land land = landmanager.getLandAt(chunk);

			if(player != null) {
				if(land.isBypassing(player, Action.BLOCK_BREAK)) {
					continue;
				}
			}
			
			if(!land.hasFlag(Flag.EXPLOSIONS)) {
				it.remove();
			}
		}
	}
	
	private Player getTargetPlayer(EntityExplodeEvent e) {
		if(e.getEntity() instanceof Creeper) {
			Creeper creeper = (Creeper) e.getEntity();
			return (Player) creeper.getTarget();
		}
		return null;
	}
}
