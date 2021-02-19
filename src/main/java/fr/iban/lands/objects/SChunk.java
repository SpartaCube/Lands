package fr.iban.lands.objects;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;

import fr.iban.bukkitcore.CoreBukkitPlugin;

public class SChunk {

	private String server;
	private String world;
	private int x;
	private int z;

	public SChunk(String server, String world, int x, int z) {
		this.server = server;
		this.world = world;
		this.x = x;
		this.z = z;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getWorld() {
		return world;
	}

	public void setWorld(String world) {
		this.world = world;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getZ() {
		return z;
	}

	public void setZ(int z) {
		this.z = z;
	}

	public boolean equalsChunk(Chunk chunk) {
		return chunk.getWorld().getName().equals(world) && chunk.getX() == x && chunk.getZ() == z && CoreBukkitPlugin.getInstance().getServerName().equals(server);
	}

	public boolean equalsChunk(SChunk schunk) {
		return schunk.getWorld().equals(world) && schunk.getX() == x && schunk.getZ() == z && CoreBukkitPlugin.getInstance().getServerName().equals(server);
	}

	public Chunk getChunk() {
		return Bukkit.getWorld(world).getChunkAt(x, z);
	}

	@Override
	public String toString() {
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

}
