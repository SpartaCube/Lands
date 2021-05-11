package fr.iban.lands;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import fr.iban.common.data.AccountProvider;
import fr.iban.lands.enums.Action;
import fr.iban.lands.enums.Flag;
import fr.iban.lands.enums.Link;
import fr.iban.lands.objects.Land;
import fr.iban.lands.objects.PlayerLand;
import fr.iban.lands.objects.SChunk;
import fr.iban.lands.objects.SystemLand;
import fr.iban.lands.storage.AbstractStorage;
import fr.iban.lands.utils.LandMap;

public class LandManager {

	private AbstractStorage storage;
	private boolean loaded = false;

	private Map<Integer, Land> lands = new ConcurrentHashMap<>();
	private Map<SChunk, Land> chunks = new ConcurrentHashMap<>();
	private LandMap landMap;
	private LandsPlugin plugin;
	private SystemLand wilderness = new SystemLand(-1, "Zone sauvage");

	public LandManager(LandsPlugin plugin, AbstractStorage storage) {
		this.storage = storage;
		this.plugin = plugin;
		this.landMap = new LandMap(this);
	}

	/*
	 * Charge les données depuis la bdd
	 */
	public void loadData() {
		final long start = System.currentTimeMillis();
		plugin.getLogger().log(Level.INFO, "Chargement des données :.");
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			
			plugin.getLogger().log(Level.INFO, "Chargement des lands...");
			getLands().putAll(storage.getLands());
			
			plugin.getLogger().log(Level.INFO, "Chargement des chunks...");
			Map<SChunk, Integer> savedchunks = storage.getChunks();
			for(Entry<SChunk, Integer> entry : savedchunks.entrySet()) {
				getChunks().put(entry.getKey(), getLands().get(entry.getValue()));
			}
			plugin.getLogger().log(Level.INFO, getChunks().size() + " chunks chargées");

			plugin.getLogger().log(Level.INFO, "Chargement des liens...");
			storage.loadLinks(this);
			getSystemLand("Zone sauvage").thenAccept(wild -> {
				if(wild == null) {
					saveWilderness(wilderness);
				}else {
					wilderness = wild;
				}
			});
			loaded = true;
			plugin.getLogger().log(Level.INFO, "Chargement des données terminé en " + (System.currentTimeMillis() - start) + " ms.");
		});
	}

	/*
	 * Retourne la liste de toutes les territoires.
	 */
	public Map<Integer, Land> getLands() {
		return lands;
	}

	/*
	 * Retourne la liste de toutes les territoires d'un joueur.
	 */
	public List<PlayerLand> getLands(Player player) {
		UUID uuid = player.getUniqueId();
		return getLands().values().stream()
				.filter(l -> l instanceof PlayerLand)
				.map(l -> (PlayerLand) l)
				.filter(l -> l.getOwner().equals(uuid))
				.collect(Collectors.toList());
	}

	/*
	 * Retourne la liste de toutes les territoires d'un joueur. (ASYNC)
	 */
	public CompletableFuture<List<Land>> getLandsAsync(Player player) {
		return future(() ->  getLands(player).stream().collect(Collectors.toList()));
	}

	/*
	 * Retourne la liste de toutes les territoires d'un joueur.
	 */
	public List<SystemLand> getSystemLands() {
		return getLands().values().stream()
				.filter(l -> l instanceof SystemLand)
				.map(l -> (SystemLand) l)
				.collect(Collectors.toList());
	}

	/*
	 * Retourne la liste de toutes les territoires d'un joueur. (ASYNC)
	 */
	public CompletableFuture<List<Land>> getSystemLandsAsync() {
		return future(() ->  getSystemLands().stream().collect(Collectors.toList()));

	}

	//Retourne le territoire du nom donné pour le joueur donné.
	public CompletableFuture<PlayerLand> getPlayerLand(Player player, String name) {
		return future(() ->  {
			for(PlayerLand land : getLands(player)) {
				if(land.getName().equalsIgnoreCase(name)) {
					return land;
				}
			}
			return null;
		});
	}

	//Retourne le territoire du nom donné pour le joueur donné.
	public CompletableFuture<SystemLand> getSystemLand(String name) {
		return future(() ->  {
			for(SystemLand land : getSystemLands()) {
				if(land.getName().equalsIgnoreCase(name)) {
					return land;
				}
			}
			return null;
		});
	}
	
	public boolean isWilderness(Land land) {
		return land instanceof SystemLand && land.getName().equals("Zone sauvage");
	}

	//Retourne le territoire du nom donné pour le joueur donné.
	public CompletableFuture<PlayerLand> getPlayerFirstLand(Player player) {
		return future(() ->  {
			List<PlayerLand> plands = getLands(player);
			if(!plands.isEmpty()) {
				return plands.get(0);
			}
			player.sendMessage("§cVous n'avez pas de territoire.");
			return null;
		});
	}

	/*
	 * Permet de créer un nouveau territoire en tant que joueur.
	 */
	public CompletableFuture<PlayerLand> createLand(Player player, String name) {
		return future(() -> {
			if(getLands(player).size() > 50) {
				player.sendMessage("§cVous pouvez avoir 50 territoires maximum.");
				return null;
			}
			if(getPlayerLand(player, name).get() != null) {
				player.sendMessage("§cVous avez déjà un territoire à ce nom.");
				return null;
			}
			if(name.length() > 16) {
				player.sendMessage("§cLe nom du territoire ne doit pas dépasser 16 caractères.");
				return null;
			}
			PlayerLand land = new PlayerLand(-1, player.getUniqueId(), name);
			storage.addPlayerLand(land);
			int id = storage.getLandID(land.getType(), land.getOwner(), land.getName());
			land.setId(id);
			getLands().put(id, land);
			land.setBans(new HashSet<>());
			land.setFlags(new HashSet<>());
			player.sendMessage("§aLe territoire au nom de " + name + " a été créée.");
			return land;
		});
	}

	/*
	 * Permet de créer un nouveau territoire système.
	 */
	public CompletableFuture<SystemLand> createSystemLand(Player player, String name) {
		return future(() -> {

			if(getSystemLand(name).get() != null) {
				player.sendMessage("§cIl y a déjà un territoire à ce nom.");
				return null;
			}

			if(name.length() > 16) {
				player.sendMessage("§cLe nom du territoire ne doit pas dépasser 16 caractères.");
				return null;
			}
			SystemLand land = new SystemLand(-1, name);
			storage.addSystemLand(land);
			int id = storage.getSystemLandID(name);
			land.setId(id);
			getLands().put(id, land);
			land.setBans(new HashSet<>());
			land.setFlags(new HashSet<>());
			player.sendMessage("§aLe territoire au nom de " + name + " a été créée.");
			return land;
		});
	}
	
	public CompletableFuture<Void> saveWilderness(SystemLand land) {
		return future(() -> {
			getLands().put(-1, land);
			storage.addSystemLand(land);
		});
	}

	/*
	 * Permet de créer un nouveau territoire.
	 */
	public CompletableFuture<PlayerLand> createLand(UUID uuid, String name) {
		return future(() -> {
			PlayerLand land = new PlayerLand(-1, uuid, name);
			storage.addPlayerLand(land);
			int id = storage.getLandID(land.getType(), land.getOwner(), land.getName());
			land.setId(id);
			getLands().put(id, land);
			land.setBans(new HashSet<>());
			land.setFlags(new HashSet<>());
			Bukkit.broadcastMessage("§aLe territoire au nom de " + name + " pour " + Bukkit.getOfflinePlayer(uuid).getName() + " a été créée.");
			return land;
		});
	}

	/*
	 * Permet de supprimer une territoire
	 */
	public CompletableFuture<Void> deleteLand(Player player, String name) {
		return getPlayerLand(player, name).thenAcceptAsync(l -> {
			if(l == null) {
				player.sendMessage("§cVous n'avez pas de territoire à ce nom.");
			}
			storage.deleteLand(l);
			getChunks(l).forEach(schunk -> {
				getChunks().remove(schunk);
				chunksCache.invalidate(schunk);
			});
			getLands().remove(l.getId());
			player.sendMessage("§cLe territoire au nom de " + name + " a bien été supprimée.");
		});
	}

	/*
	 * Permet de supprimer une territoire
	 */
	public CompletableFuture<Void> deleteSystemLand(Player player, String name) {
		return getSystemLand(name).thenAcceptAsync(l -> {
			if(l == null) {
				player.sendMessage("§cIl n'y a pas de territoire à ce nom.");
			}
			storage.deleteLand(l);
			getChunks(l).forEach(schunk -> {
				getChunks().remove(schunk);
				chunksCache.invalidate(schunk);
			});
			getLands().remove(l.getId());
			player.sendMessage("§cLe territoire au nom de " + name + " a bien été supprimée.");
		});
	}

	/*
	 * Permet de renommer un territoire
	 */
	public CompletableFuture<Void> renameLand(Land land, Player player, String name) {
		return future(() -> {
			try {
				if(getPlayerLand(player, name).get() != null) {
					player.sendMessage("§cVous avez déjà un territoire à ce nom.");
					return;
				}
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
			if(name.length() > 16) {
				player.sendMessage("§cLe nom du territoire ne doit pas dépasser 16 caractères.");
				return;
			}
			land.setName(name);
			storage.renameLand(land, name);
			player.sendMessage("§aLe nom du territoire a bien été modifié.");
		});
	}

	/*
	 * Retourne le nombre de chunks détenus par un joueur.
	 */
	public CompletableFuture<Integer> getChunkCount(Player player) {
		return future(() -> storage.getChunkCount(player.getUniqueId()));
	}

	public int getMaxChunkCount(Player player) {
		if(plugin.isBypassing(player)) {
			return 1000000;
		}
		AccountProvider ap = new AccountProvider(player.getUniqueId());
		return ap.getAccount().getMaxClaims();
	}

	public CompletableFuture<Integer> getRemainingChunkCount(Player player) {
		return future(() -> getMaxChunkCount(player) - getChunkCount(player).get());
	}


	/*
	 * Retourne la liste des chunks d'un territoire.
	 */
	public Collection<SChunk> getChunks(Land land) {
		Set<SChunk> chunksSet = new HashSet<>();
		for(Entry<SChunk, Land> entry : getChunks().entrySet()) {
			if(land.equals(entry.getValue())) {
				chunksSet.add(entry.getKey());
			}
		}
		return chunksSet;
	}

	/*
	 * Retourne tous les chunks claim du serveur.
	 */
	public Map<SChunk, Land> getChunks() {
		return chunks;
	}



	/*
	 * TRUST / UNTRUST
	 */


	public void addTrust(Land land, UUID uuid, Action action) {
		land.trust(uuid, action);
		future(() -> storage.addTrust(land, uuid, action));
	}

	public void removeTrust(Land land, UUID uuid, Action action) {
		land.untrust(uuid, action);
		future(() -> storage.removeTrust(land, uuid, action));
	}

	public void addGlobalTrust(Land land, Action action) {
		land.trust(action);
		future(() -> storage.addGlobalTrust(land, action));
	}

	public void removeGlobalTrust(Land land, Action action) {
		land.untrust(action);
		future(() -> storage.removeGlobalTrust(land, action));
	}


	/* 
	 * CLAIM / UNCLAIM
	 */

	private Cache<SChunk, Land> chunksCache = Caffeine.newBuilder()
			.expireAfterAccess(10, TimeUnit.MINUTES)
			.maximumSize(1000)
			.build();

	public Land getLandAt(Chunk chunk) {
		return getLandAt(new SChunk(chunk));
	}

	public Land getLandAt(SChunk schunk) {
		if(!loaded) {
			return wilderness;
		}
		return chunksCache.get(schunk, land -> getChunks().getOrDefault(schunk, wilderness));
	}



	public CompletableFuture<Land> getLandAtAsync(Chunk chunk) {
		return future(() ->  getLandAt(chunk));
	}


	/*
	 * Ajouter un chunk à un territoire :
	 */
	public void claim(SChunk chunk, Land land) {
		getChunks().put(chunk, land);
		chunksCache.invalidate(chunk);
		future(() -> storage.setChunk(land, chunk));
	}

	public void claim(Chunk chunk, Land land) {
		claim(new SChunk(chunk), land);
	}

	public CompletableFuture<Void> claim(Player player, Chunk chunk, Land land, boolean verbose) {
		return future(() ->  {
			try {
				if(getRemainingChunkCount(player).get() < 1) {
					player.sendMessage("§cVous n'avez pas de tronçon disponnible.");
					return;
				}
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
				return;
			}

			if(getLandAt(chunk).equals(wilderness)) {
				claim(chunk, land);
				if(verbose) {
					player.sendActionBar("§a§lLe tronçon a bien été claim.");
				}
			}else if(verbose){
				player.sendActionBar("§c§lCe tronçon est déjà claim.");
			}
		});
	}

	public CompletableFuture<Void> unclaim(Player player, Chunk chunk, boolean verbose) {
		return future(() -> {
			Land land = getLandAt(chunk);
			if(land instanceof PlayerLand) {
				PlayerLand pland = (PlayerLand) land;
				if(pland.getOwner().equals(player.getUniqueId()) || plugin.isBypassing(player)) {
					unclaim(chunk);
					if(verbose) {
						player.sendActionBar("§a§lLe tronçon a bien été unclaim.");
					}
				}else if(verbose){
					player.sendActionBar("§c§lCe tronçon ne vous appartient pas !");
				}
			}else if(verbose){
				player.sendActionBar("§c§lImpossible d'unclaim ce tronçon.");
			}
		});
	}

	public CompletableFuture<Void> unclaim(Player player, Chunk chunk, Land land, boolean verbose) {
		return future(() -> {
			Land l = getLandAt(chunk);
			if(l == null) {
				if(verbose)
					player.sendActionBar("§c§lCe tronçon n'est pas claim !");
				return;
			}

			if(l.equals(land)) {
				unclaim(player, chunk, true);
			}else if(verbose){
				player.sendActionBar("§c§lImpossible d'unclaim ce tronçon, il n'appartient pas au territoire " + land.getName());
			}
		});
	}

	public CompletableFuture<Void> unclaim(Chunk chunk) {
		return unclaim(new SChunk(chunk));
	}

	public CompletableFuture<Void> unclaim(SChunk schunk) {
		return future(() -> {
			Land land = getLandAt(schunk);
			if(land == null) {
				return;
			}
			chunks.remove(schunk);
			chunksCache.invalidate(schunk);
			storage.removeChunk(schunk);
		});
	}

	/*
	 * FLAGS
	 */

	public void addFlag(Land land, Flag flag) {
		if(!land.getFlags().contains(flag)) {
			land.getFlags().add(flag);
		}
		future(() -> storage.addFlag(land, flag));
	}

	public void removeFlag(Land land, Flag flag) {
		if(land.getFlags().contains(flag)) {
			land.getFlags().remove(flag);
		}	
		future(() -> storage.removeFlag(land, flag));
	}

	/*
	 * BANS
	 */

	public void ban(Player sender, Land land, UUID uuid) {
		if(sender.getUniqueId().equals(uuid)) {
			sender.sendMessage("§cVous ne pouvez pas vous ban vous même !");
			return;
		}
		if(land.isBanned(uuid)){
			sender.sendMessage("§cCe joueur est déjà banni.");
			return;
		}
		land.getBans().add(uuid);
		storage.addBan(land, uuid);
		sender.sendMessage("§aLe joueur a bien été banni.");

		Player player = Bukkit.getPlayer(uuid);
		if(player != null) {
			Land landat = getLandAt(player.getChunk());
			if(landat instanceof PlayerLand) {
				PlayerLand pland = (PlayerLand)landat;
				if(pland.getOwner().equals(sender.getUniqueId())) {
					player.teleportAsync(Bukkit.getWorld("world").getSpawnLocation());
				}
			}
			player.sendMessage("§aVous avez été banni du territoire " + land.getName() + " par " + sender.getName() + ".");
		}
	}

	public void unban(Player sender, Land land, UUID uuid) {
		if(!land.isBanned(uuid)){
			sender.sendMessage("§cCe joueur n'est pas banni.");
			return;
		}
		land.getBans().remove(uuid);
		storage.removeBan(land, uuid);
		sender.sendMessage("§aLe joueur a bien été débanni.");
		Player player = Bukkit.getPlayer(uuid);
		if(player != null) {
			player.sendMessage("§aVous avez été débanni du territoire " + land.getName() + " par " + sender.getName() + ".");
		}
	}

	/*
	 * LINKS
	 */

	public void addLink(Land land, Link link, Land with) {
		land.addLink(link, with);
		future(() -> storage.addLink(land, link, with));
	}

	public void removeLink(Land land, Link link) {
		land.removeLink(link);
		future(() -> storage.removeLink(land, link));
	}


	public <T> CompletableFuture<T> future(Callable<T> supplier) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return supplier.call();
			} catch (Exception e) {
				if (e instanceof RuntimeException) {
					throw (RuntimeException) e;
				}
				throw new CompletionException(e);
			}
		});
	}

	public CompletableFuture<Void> future(Runnable runnable) {
		return CompletableFuture.runAsync(() -> {
			try {
				runnable.run();
			} catch (Exception e) {
				if (e instanceof RuntimeException) {
					throw (RuntimeException) e;
				}
				throw new CompletionException(e);
			}
		});
	}

	public LandMap getLandMap() {
		return landMap;
	}


}
