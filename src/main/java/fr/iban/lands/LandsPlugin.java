package fr.iban.lands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import fr.iban.lands.commands.LandCMD;
import fr.iban.lands.commands.LandsCMD;
import fr.iban.lands.commands.MaxClaimsCMD;
import fr.iban.lands.listeners.BlockBreakListener;
import fr.iban.lands.listeners.BlockPlaceListener;
import fr.iban.lands.listeners.CommandListener;
import fr.iban.lands.listeners.DamageListeners;
import fr.iban.lands.listeners.DropListener;
import fr.iban.lands.listeners.EntityBlockDamageListener;
import fr.iban.lands.listeners.EntityExplodeListener;
import fr.iban.lands.listeners.EntitySpawnListener;
import fr.iban.lands.listeners.HangingListeners;
import fr.iban.lands.listeners.HeadDatabaseListener;
import fr.iban.lands.listeners.InteractListener;
import fr.iban.lands.listeners.LandListeners;
import fr.iban.lands.listeners.PistonListeners;
import fr.iban.lands.listeners.PlayerMoveListener;
import fr.iban.lands.listeners.PlayerTakeLecternBookListener;
import fr.iban.lands.listeners.PortalListeners;
import fr.iban.lands.listeners.ShopCreateListener;
import fr.iban.lands.listeners.TeleportListener;
import fr.iban.lands.storage.DbTables;
import fr.iban.lands.storage.Storage;
import fr.iban.lands.utils.Head;

public final class LandsPlugin extends JavaPlugin {

	private LandManager landManager;
	private static LandsPlugin instance;
	private List<UUID> bypass;


	@Override
	public void onEnable() {
		instance = this;
		saveDefaultConfig();
		this.bypass = new ArrayList<>();

		DbTables tables = new DbTables();
		tables.create();
		Storage storage = new Storage();
		landManager = new LandManager(this, storage);
		landManager.loadData();


		getCommand("land").setExecutor(new LandCMD(this));
		getCommand("land").setTabCompleter(new LandCMD(this));

		getCommand("lands").setExecutor(new LandsCMD(this));
		getCommand("lands").setTabCompleter(new LandsCMD(this));

		getCommand("addmaxclaim").setExecutor(new MaxClaimsCMD());
		getCommand("removemaxclaim").setExecutor(new MaxClaimsCMD());
		getCommand("getmaxclaim").setExecutor(new MaxClaimsCMD());

		registerListeners(
				new PlayerMoveListener(this),
				new PlayerTakeLecternBookListener(this),
				new BlockPlaceListener(this),
				new BlockBreakListener(this),
				new PistonListeners(this),
				new InteractListener(this),
				new EntitySpawnListener(this),
				new EntityExplodeListener(this),
				new DamageListeners(this),
				new EntityBlockDamageListener(this),
				new CommandListener(this),
				new HangingListeners(this),
				new TeleportListener(this),
				new DropListener(this),
				new LandListeners(this),
				new HeadDatabaseListener(),
				new PortalListeners(this)
				);

		if(getServer().getPluginManager().getPlugin("QuickShop") != null) {
			getServer().getPluginManager().registerEvents(new ShopCreateListener(this), this);
		}
		
		Head.loadAPI();

	}

	@Override
	public void onDisable() {
		// Plugin shutdown logic
	}


	public LandManager getLandManager() {
		return landManager;
	}

	public static LandsPlugin getInstance() {
		return instance;
	}

	private void registerListeners(Listener... listeners) {

		PluginManager pm = Bukkit.getPluginManager();

		for (Listener listener : listeners) {
			pm.registerEvents(listener, this);
		}

	}

	public List<UUID> getBypass() {
		return bypass;
	}

	public boolean isBypassing(Player player) {
		return getBypass().contains(player.getUniqueId());
	}

}
