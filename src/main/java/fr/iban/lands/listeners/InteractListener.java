package fr.iban.lands.listeners;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.Bed;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;

import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.Action;
import fr.iban.lands.enums.Flag;
import fr.iban.lands.enums.LandType;
import fr.iban.lands.objects.Land;
import fr.iban.lands.objects.SystemLand;

public class InteractListener implements Listener {

	private LandManager landmanager;
	private LandsPlugin plugin;

	public InteractListener(LandsPlugin landsPlugin) {
		this.plugin = landsPlugin;
		this.landmanager = landsPlugin.getLandManager();
	}

	@EventHandler
	public void onPhysics(PlayerInteractEvent e) {
		Block block = e.getClickedBlock();
		if(block == null)
			return;

		Chunk chunk = block.getChunk();
		Land land = landmanager.getLandAt(chunk);

		if(land == null)
			return;

		if(e.getAction() == org.bukkit.event.block.Action.PHYSICAL && block.getType() == Material.FARMLAND && !land.hasFlag(Flag.FARMLAND_GRIEF)) {
			e.setCancelled(true);
			return;
		}

		if(e.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
			Player player = e.getPlayer();

			if(land.getType() == LandType.SYSTEM && (e.getClickedBlock().getType().toString().endsWith("_DOOR") || e.getClickedBlock().getType().toString().endsWith("_GATE"))) {
				if(block.getBlockData() instanceof Openable) {
					Openable openable = (Openable) block.getBlockData();
					if(!openable.isOpen() && !land.isBypassing(e.getPlayer(), Action.ALL)) {
						new BukkitRunnable() {
							@Override
							public void run() {
								Openable o = (Openable) block.getBlockData();
								if(o.isOpen()) {
									o.setOpen(false);
									block.setBlockData(o);
									block.getState().update();
								}
							}
						}.runTaskLater(plugin, 60L);
					}
				}
				return;
			}

			if((block.getType() == Material.ANVIL && !land.isBypassing(player, Action.USE_ANVIL))
					|| (block.getType() == Material.BREWING_STAND && !land.isBypassing(player, Action.BREWING_STAND_INTERACT))
					|| (block.getBlockData() instanceof Powerable && !land.isBypassing(player, Action.USE)) 
					|| (block.getBlockData() instanceof Bed && !land.isBypassing(player, Action.USE_BED))
					|| ((block.getState() instanceof InventoryHolder || block.getType() == Material.JUKEBOX) && !land.isBypassing(player, Action.OPEN_CONTAINER))
					|| (player.getInventory().getItemInMainHand().getType().toString().endsWith("SPAWN_EGG") || player.getInventory().getItemInOffHand().getType().toString().endsWith("SPAWN_EGG") && !land.isBypassing(player, Action.ALL))) {
				e.setCancelled(true);
			}

		}

	}

	@EventHandler
	public void onEntityInteract(EntityInteractEvent e) {
		Block block = e.getBlock();

		Chunk chunk = block.getChunk();
		Land land = landmanager.getLandAt(chunk);

		if(land != null && e.getEntityType() != EntityType.VILLAGER) {
			e.setCancelled(true);
		}

	}


	@EventHandler
	public void onFerilize(BlockFertilizeEvent e) {
		Block block = e.getBlock();

		Chunk chunk = block.getChunk();
		Land land = landmanager.getLandAt(chunk);

		if(!(land instanceof SystemLand))
			return;

		//TODO param

		e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onInteractAtEntity(PlayerInteractEntityEvent e) {
		Chunk chunk = e.getRightClicked().getChunk();
		Land land = landmanager.getLandAt(chunk);

		if(land == null) {
			return;
		}
		if(!land.isBypassing(e.getPlayer(), Action.ENTITY_INTERACT)) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onInteractAtEntity(PlayerInteractAtEntityEvent e) {
		Chunk chunk = e.getRightClicked().getChunk();
		Land land = landmanager.getLandAt(chunk);
		if(land == null) {
			return;
		}
		if(!land.isBypassing(e.getPlayer(), Action.ENTITY_INTERACT)) {
			e.setCancelled(true);
		}
	}
}
