package fr.iban.lands.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import fr.iban.bukkitcore.menu.Menu;
import fr.iban.bukkitcore.utils.Head;
import fr.iban.bukkitcore.utils.ItemBuilder;
import fr.iban.lands.LandManager;
import fr.iban.lands.objects.Land;

public class LandManageMenu extends Menu {

	private Land land;
	private LandManager manager;
	private LandMainMenu mainMenu;

	public LandManageMenu(Player player, LandManager manager, Land land) {
		super(player);
		this.land = land;
		this.manager = manager;
	}

	public LandManageMenu(Player player, LandManager manager, Land land, LandMainMenu mainmenu) {
		this(player, manager, land);
		this.mainMenu = mainmenu;
	}

	@Override
	public String getMenuName() {
		return "§2§l" + land.getName();
	}

	@Override
	public int getRows() {
		return 4;
	}

	@Override
	public void handleMenu(InventoryClickEvent e) {
		ItemStack current = e.getCurrentItem();

		if(mainMenu != null && current.getType() == Material.RED_STAINED_GLASS_PANE) {
			mainMenu.open();
		}
		
		if(displayNameEquals(current, "§2Permissions")) {
			new TrustsManageMenu(player, manager, land, this).open();
		}else if(displayNameEquals(current, "§2Tronçons protégés")) {
			new ClaimsManageMenu(player, land, manager, this).open();
		}else if(displayNameEquals(current, "§2Paramètres")) {
			new LandSettingsMenu(player, land, manager, this).open();
		}else if(displayNameEquals(current, "§2Bannissements")) {
			new BansManageMenu(player, manager, land, this).open();
		}

	}

	@Override
	public void setMenuItems() {
		inventory.setItem(10, new ItemBuilder(Head.CHEST_DIRT.get()).setDisplayName("§2Tronçons protégés")
				.addLore("§aPermet de gérer les tronçons que le territoire inclut.")
				.build());
		inventory.setItem(12, new ItemBuilder(Head.HAL.get()).setDisplayName("§2Permissions")
				.addLore("§aPermet de gérer les permissions du territoire.")
				.build());
		inventory.setItem(14, new ItemBuilder(Head.FIREBALL.get()).setDisplayName("§2Paramètres")
				.addLore("§aPermet de modifier les paramètres du territoire")
				.build());
		inventory.setItem(16, new ItemBuilder(Head.NO_ENTRY.get()).setDisplayName("§2Bannissements")
				.addLore("§aPermet de gérer les bannissements.")
				.build());
		if(mainMenu != null) {
			inventory.setItem(31, new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setDisplayName("§4Retour")
					.addLore("§cRetourner au menu")
					.build());
		}
		fillWithGlass();
	}

}
