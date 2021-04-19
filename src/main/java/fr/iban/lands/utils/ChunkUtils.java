package fr.iban.lands.utils;

import org.bukkit.Chunk;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.lands.objects.SChunk;

public class ChunkUtils {

	public static String serialize(Chunk chunk) {
		return serialize(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
	}

	public static String serialize(String server, String world, int x, int z) {
		StringBuilder sb = new StringBuilder();
		sb.append(server);
		sb.append(":");
		sb.append(world);
		sb.append(":");
		sb.append(x);
		sb.append(":");
		sb.append(z);
		return sb.toString();
	}

	public static String serialize(String world, int x, int z) {
		String server = CoreBukkitPlugin.getInstance().getServerName();
		if(server == null) {
			server = "null";
		}
		return serialize(server, world, x, z);
	}
	
	public static SChunk getSChunkFromString(String string) {
		String[] cutted = string.split(":");
		return new SChunk(cutted[0], cutted[1], Integer.parseInt(cutted[2]), Integer.parseInt(cutted[3]));
	}

}
