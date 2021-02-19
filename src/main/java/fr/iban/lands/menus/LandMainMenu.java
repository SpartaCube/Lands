package fr.iban.lands.menus;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.menu.ConfirmMenu;
import fr.iban.bukkitcore.menu.PaginatedMenu;
import fr.iban.bukkitcore.utils.Head;
import fr.iban.bukkitcore.utils.ItemBuilder;
import fr.iban.lands.LandManager;
import fr.iban.lands.LandsPlugin;
import fr.iban.lands.objects.Land;

public class LandMainMenu extends PaginatedMenu {

	private LandManager manager;
	private List<Land> lands;
	private LandsPlugin plugin;
	private boolean system = false;

	public LandMainMenu(Player player, LandsPlugin plugin, List<Land> lands) {
		super(player);
		this.manager = plugin.getLandManager();
		this.lands = lands;
		this.plugin = plugin;
	}

	public LandMainMenu(Player player, LandsPlugin plugin, List<Land> lands, boolean system) {
		this(player, plugin, lands);
		this.system = system;
	}

	@Override
	public String getMenuName() {
		return "§2§lVos territoires :";
	}

	@Override
	public int getRows() {
		return 4;
	}

	@Override
	public int getElementAmount() {
		return lands.size();
	}

	@Override
	public void handleMenu(InventoryClickEvent e) {
		Player player = (Player) e.getWhoClicked();
		ItemStack current = e.getCurrentItem();

		if(e.getClickedInventory() != e.getView().getTopInventory()) {
			return;
		}

		checkBottonsClick(current, player);

		if(displayNameEquals(current, "§2Créer")) {
			CoreBukkitPlugin core = CoreBukkitPlugin.getInstance();
			player.closeInventory();
			player.sendMessage("§2§lVeuillez entrer le nom du territoire souhaité :");
			core.getTextInputs().put(player.getUniqueId(), texte -> {
				if(!system) {
					manager.createLand(player, texte).thenAccept(land -> 
					Bukkit.getScheduler().runTask(plugin, () -> {
						lands.add(land);
						open();
					}));
				}else {
					manager.createSystemLand(player, texte).thenAccept(land -> 
					Bukkit.getScheduler().runTask(plugin, () -> {
						lands.add(land);
						open();
					}));				
				}
				core.getTextInputs().remove(player.getUniqueId());
			});
		}

		if(current.getType() == Material.PLAYER_HEAD) {
			Land land = getLandByName(current.getItemMeta().getDisplayName());

			if(land == null) {
				return;
			}

			ClickType click = e.getClick();
			if(click == ClickType.RIGHT) {
				new ConfirmMenu(player, "§cConfirmation de suppression", "§eVoulez-vous supprimer le territoire " + land.getName() + " ?", result -> {
					if(result) {
						if(!system) {
							manager.deleteLand(player, land.getName());
						}else {
							manager.deleteSystemLand(player, land.getName());
						}
						lands.remove(land);

					}
					super.open();
				}).open();
			}else if(click == ClickType.LEFT) {
				new LandManageMenu(player, manager, land, this).open();
			}

		}		

	}

	@Override
	public void setMenuItems() {
		addMenuBorder();

		if(lands != null && !lands.isEmpty()) {
			for(int i = 0; i < getMaxItemsPerPage(); i++) {
				index = getMaxItemsPerPage() * page + i;
				if(index >= lands.size()) break;
				if (lands.get(index) != null){
					Land land = lands.get(index);
					inventory.addItem(getLandItem(land));
				}
			}
		}

		if(!system) {
			inventory.setItem(27, new ItemBuilder(Head.OAK_INFO.get()).setName("§2Informations").addLore("§cChargement...").build());
			getInfoItem().thenAccept(item -> {
				inventory.setItem(27, item);
			});
		}

		inventory.setItem(35, new ItemBuilder(Head.OAK_PLUS.get()).setName("§2Créer").addLore("§aPermet de créer un nouveau territoire").build());
	}

	private ItemStack getLandItem(Land land) {
		return new ItemBuilder(Head.GRASS.get())
				.setDisplayName("§2" + land.getName())
				.addLore("§aClic gauche pour gérer ce territoire.")
				.addLore("§cClic droit pour supprimer ce territoire.")
				.addLore("")
				.addLore("§fTronçons : " + manager.getChunks(land).size())
				.addLore("§fJoueurs trust : " + land.getTrusts().size())
				.addLore("§fBannissements : " + land.getBans().size())
				.build();
	}

	private Land getLandByName(String string) {
		for (Land land : lands) {
			if(string.equals("§2"+land.getName())) {
				return land;
			}
		}
		return null;
	}

	private CompletableFuture<ItemStack> getInfoItem(){
		return manager.future(() -> {
			return new ItemBuilder(Head.OAK_INFO.get()).setName("§2Informations").addLore("§f§lVos tronçons : §a" + manager.getChunkCount(player).get() + "/" + manager.getMaxChunkCount(player)).build();
		});
	}

}
