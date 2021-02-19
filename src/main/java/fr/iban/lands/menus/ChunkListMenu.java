package fr.iban.lands.menus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import fr.iban.bukkitcore.menu.Menu;
import fr.iban.bukkitcore.menu.PaginatedMenu;
import fr.iban.bukkitcore.utils.ItemBuilder;
import fr.iban.lands.LandManager;
import fr.iban.lands.objects.Land;
import fr.iban.lands.objects.SChunk;
import fr.iban.lands.utils.ChunkUtils;

public class ChunkListMenu extends PaginatedMenu {

	private Land land;
	private Collection<SChunk> chunks = new ArrayList<>();
	private Map<Integer, SChunk> chunkAtSlot;
	private LandManager manager;
	private Menu previousMenu;


	public ChunkListMenu(Player player, LandManager manager, Land land) {
		super(player);
		this.manager = manager;
		this.land = land;
		for(String string : manager.getChunks(land)) {
			chunks.add(ChunkUtils.getSChunkFromString(string));
		}
	}

	public ChunkListMenu(Player player, LandManager manager, Land land, Menu previousMenu) {
		this(player, manager, land);
		this.previousMenu = previousMenu;
	}

	@Override
	public String getMenuName() {
		return "§2§l" + land.getName() + " §8> §2tronçons";
	}

	@Override
	public int getRows() {
		return 4;
	}

	@Override
	public void handleMenu(InventoryClickEvent e) {
		Player player = (Player) e.getWhoClicked();
		ItemStack item = e.getCurrentItem();

		if(e.getClickedInventory() != e.getView().getTopInventory()) {
			return;
		}

		if(previousMenu != null && displayNameEquals(item, "§4Retour")) {
			previousMenu.open();
		}

		checkBottonsClick(item, player);


		SChunk schunk = chunkAtSlot.get(e.getSlot());

		if(schunk == null) {
			return;
		}

		manager.unclaim(schunk);
		chunks.remove(schunk);
		open();

	}

	@Override
	public void setMenuItems() {
		addMenuBorder();
		chunkAtSlot = new HashMap<>();
		if(chunks != null && !chunks.isEmpty()) {

			int count = 0;
			for(SChunk schunk : chunks) {
				index = getMaxItemsPerPage() * page + count;

				if(index <= chunks.size() && count < maxItemsPerPage) {
					final int slot = inventory.firstEmpty();
					inventory.setItem(slot, new ItemBuilder(Material.PLAYER_HEAD).setDisplayName("§cChargement...").build());
					chunkAtSlot.put(slot, schunk);
					getChunkItem(schunk).thenAccept(item -> inventory.setItem(slot, item));
				}else {
					break;
				}

				count++;

			}
		}

		if(previousMenu != null) {
			inventory.setItem(31, new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setDisplayName("§4Retour")
					.addLore("§cRetourner au menu précédent")
					.build());
		}
	}

	@Override
	public int getElementAmount() {
		return chunks.size();
	}

	private CompletableFuture<ItemStack> getChunkItem(SChunk chunk) {
		return CompletableFuture.supplyAsync(() -> {
			return new ItemBuilder(Material.DIRT).setDisplayName("§2§lTronçon")
					.addLore("§fServeur : §a§l" + chunk.getServer())
					.addLore("§fMonde : §a§l" + chunk.getWorld())
					.addLore("§fCoordonnées (en chunk) : §a§lX : " + chunk.getX() + " Z : " + chunk.getZ())
					.addLore("§fCoordonées (en bloc) : §a§lX : " + (chunk.getX()*16) + " Z : " + (chunk.getZ()*16))
					.addLore("§cCliquez pour supprimer.")
					.build();
		});
	}


}
