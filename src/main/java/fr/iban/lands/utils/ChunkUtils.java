package fr.iban.lands.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.lands.objects.SChunk;

public class ChunkUtils {

	public static List<SChunk> getChunksAround(Player player, int rangeX, int rangeZ) {

		List<SChunk> schunks = new ArrayList<>();
		String servername = CoreBukkitPlugin.getInstance().getServerName();
		final Chunk center = player.getChunk();
		final World world = center.getWorld();

		final int startX = center.getX() - rangeX;
		final int startZ = center.getZ() - rangeZ;
		final int endX = center.getX() + rangeX+1;
		final int endZ = center.getZ() + rangeZ+1;



		for (int x = startX; x < endX; x++) {

			for (int z = startZ; z < endZ; z++) {

				SChunk schunk = new SChunk(servername, world.getName(), x, z);
				schunks.add(schunk);

			}

		}

		return schunks;
	}

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
