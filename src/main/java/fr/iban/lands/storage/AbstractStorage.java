package fr.iban.lands.storage;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import fr.iban.lands.LandManager;
import fr.iban.lands.enums.Action;
import fr.iban.lands.enums.Flag;
import fr.iban.lands.enums.LandType;
import fr.iban.lands.enums.Link;
import fr.iban.lands.objects.Land;
import fr.iban.lands.objects.PlayerLand;
import fr.iban.lands.objects.SChunk;
import fr.iban.lands.objects.SystemLand;

public interface AbstractStorage {
		
	Map<String, Integer> getChunks();
	
	Map<SChunk, Integer> getChunks(UUID uuid);
	
	Map<SChunk, Integer> getChunks(Land land);
	
	int getChunkCount(UUID uuid);
	
	Map<Integer, Land> getLands();
	
	void addPlayerLand(PlayerLand land);
	
	void addSystemLand(SystemLand land);
	
	void deleteLand(Land land);
	
	void renameLand(Land land, String name);
	
	int getLandID(LandType type, UUID uuid, String name);
	
	int getSystemLandID(String name);
	
	void setChunk(Land land, SChunk chunk);
	
	void removeChunk(SChunk chunk);
	
	void loadTrusts(Land land);

	void addTrust(Land land, UUID uuid, Action action);
	
	void removeTrust(Land land, UUID uuid, Action action);
	
	void addGlobalTrust(Land land, Action action);
	
	void removeGlobalTrust(Land land, Action action);
	
	Set<Flag> getFlags(Land land);
	
	void addFlag(Land land, Flag flag);
	
	void removeFlag(Land land, Flag flag);
	
	Set<UUID> getBans(Land land);
	
	void addBan(Land land, UUID uuid);
	
	void removeBan(Land land, UUID uuid);
	
	void loadLinks(LandManager manager);
	
	void addLink(Land land, Link link, Land with);
	
	void removeLink(Land land, Link link);
	
}
