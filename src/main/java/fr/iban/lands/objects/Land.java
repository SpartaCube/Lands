package fr.iban.lands.objects;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.Action;
import fr.iban.lands.enums.Flag;
import fr.iban.lands.enums.LandType;
import fr.iban.lands.enums.Link;

public class Land {

	protected int id;
	protected String name;
	protected Trust globalTrust = new Trust();
	protected LandType type;
	protected Map<UUID, Trust> trusts = new HashMap<>();
	protected Set<Flag> flags;
	protected Set<UUID> bans;
	private Map<Link, Land> links;

	public Land(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String getName() {
		if(name == null) {
			return "default";
		}
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LandType getType() {
		return type;
	}

	public void setType(LandType type) {
		this.type = type;
	}
	
	
	/*
	 * TRUSTS
	 */

	public Trust getGlobalTrust() {
		Land linkedLand = getLinkedLand(Link.GLOBALTRUST);
		if(linkedLand != null) {
			return linkedLand.getGlobalTrust();
		}
		return globalTrust;
	}

	public void setGlobalTrust(Trust globalTrust) {
		this.globalTrust = globalTrust;
	}

	public Map<UUID, Trust> getTrusts() {
		Land linkedLand = getLinkedLand(Link.TRUSTS);
		if(linkedLand != null) {
			return linkedLand.getTrusts();
		}
		return trusts;
	}

	public Trust getTrust(UUID uuid) {
		return getTrusts().getOrDefault(uuid, new Trust());
	}

	public void trust(UUID uuid, Action action) {
		if(!getTrusts().containsKey(uuid)) {
			getTrusts().put(uuid, new Trust());
		}
		getTrust(uuid).addPermission(action);
	}
	
	public void untrust(UUID uuid, Action action) {
		if(!getTrusts().containsKey(uuid)) {
			getTrusts().put(uuid, new Trust());
		}
		getTrust(uuid).removePermission(action);
		if(getTrust(uuid).getPermissions().isEmpty()) {
			getTrusts().remove(uuid);
		}
	}
	
	public void trust(Action action) {
		getGlobalTrust().addPermission(action);
	}
	
	public void untrust(Action action) {
		getGlobalTrust().removePermission(action);
	}
	
	public boolean isTrusted(UUID uuid, Action action) {
		return getTrusts().containsKey(uuid) && getTrust(uuid).hasPermission(action);
	}
	
	public boolean isBypassing(Player player, Action action) {
		UUID uuid = player.getUniqueId();
		boolean bypass = getGlobalTrust().hasPermission(action) || isTrusted(uuid, action) || LandsPlugin.getInstance().isBypassing(player);
		if(!bypass && this instanceof PlayerLand)
			player.sendActionBar("§cVous n'avez pas la permission de faire cela dans ce claim.");
		return bypass;
	}

	public void setTrusts(Map<UUID, Trust> trusts) {
		this.trusts = trusts;
	}
	
	/*
	 * FLAGS
	 */
	
	public Set<Flag> getFlags() {
		return flags;
	}

	public void setFlags(Set<Flag> flags) {
		this.flags = flags;
	}
	
	public boolean hasFlag(Flag flag) {
		return getFlags().contains(flag);
	}

	public Set<UUID> getBans() {
		Land linkedLand = getLinkedLand(Link.BANS);
		if(linkedLand != null) {
			return linkedLand.getBans();
		}
		return bans;
	}

	public void setBans(Set<UUID> bans) {
		this.bans = bans;
	}
	
	public boolean isBanned(UUID uuid) {
		return getBans().contains(uuid);
	}

	public Map<Link, Land> getLinks() {
		if(links == null) {
			links = new EnumMap<>(Link.class);
		}
		return links;
	}

	public void addLink(Link link, Land with) {
		getLinks().put(link, with);
	}
	
	public void removeLink(Link link) {
		getLinks().remove(link);
	}
	
	public Land getLinkedLand(Link link) {
		Land land = getLinks().get(link);
		if(land == null) {
			removeLink(link);
		}
		return land;
	}

}
