package fr.iban.lands.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.sql.DataSource;

import fr.iban.common.data.sql.DbAccess;
import fr.iban.lands.LandManager;
import fr.iban.lands.enums.Action;
import fr.iban.lands.enums.Flag;
import fr.iban.lands.enums.LandType;
import fr.iban.lands.enums.Link;
import fr.iban.lands.objects.Land;
import fr.iban.lands.objects.PlayerLand;
import fr.iban.lands.objects.SChunk;
import fr.iban.lands.objects.SystemLand;

public class Storage implements AbstractStorage {

	private DataSource ds = DbAccess.getDataSource();


	@Override
	public Map<SChunk, Integer> getChunks() {
		Map<SChunk, Integer> chunks = new HashMap<>();

		try(Connection connection = ds.getConnection()){
			try(PreparedStatement ps = connection.prepareStatement(
					"SELECT * " +
					"FROM sc_chunks;")){
				//ps.setString(1, CoreBukkitPlugin.getInstance().getServerName());
				try(ResultSet rs = ps.executeQuery()){
					while(rs.next()) {
						int id = rs.getInt("idL");
						String server = rs.getString("server");
						String world = rs.getString("world");
						int x = rs.getInt("x");
						int z = rs.getInt("z");
						chunks.put(new SChunk(server, world, x, z), id);
					}
				}
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return chunks;
	}


	@Override
	public Map<Integer, Land> getLands() {
		Map<Integer, Land> lands = new HashMap<>();
		try(Connection connection = ds.getConnection()){
			try(PreparedStatement ps = connection.prepareStatement(
					"SELECT L.idL, L.libelleL, TL.libelleTL, L.uuid " +
							"FROM sc_lands L"
							+ " JOIN sc_land_types TL ON L.idTL=TL.idTL;")){
				try(ResultSet rs = ps.executeQuery()){
					while(rs.next()) {
						Land land;
						int id = rs.getInt("idL");
						LandType type = LandType.valueOf(rs.getString("libelleTL"));
						String name = rs.getString("libelleL");
						if(type == LandType.PLAYER) {
							UUID uuid = UUID.fromString(rs.getString("uuid"));
							land = new PlayerLand(id, uuid, name);
						}else if(type == LandType.SYSTEM){
							land = new SystemLand(id, name);
						}else {
							land = new Land(id, name);
						}
						land.setId(id);
						land.setName(name);
						loadTrusts(land);
						land.setFlags(getFlags(land));
						land.setBans(getBans(land));
						lands.put(land.getId(), land);
					}
				}
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}		
		return lands;
	}

	@Override
	public void addPlayerLand(PlayerLand land) {
		String sql = "INSERT INTO sc_lands (libelleL, idTL, uuid) VALUES(?, (SELECT idTL FROM sc_land_types WHERE libelleTL LIKE ?), ?);";
		try (Connection connection = ds.getConnection()) {
			try(PreparedStatement ps = connection.prepareStatement(sql)){
				ps.setString(1, land.getName());
				ps.setString(2, land.getType().toString());
				ps.setString(3, land.getOwner().toString());

				ps.executeUpdate();
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void addSystemLand(SystemLand land) {
		String sql = "INSERT INTO sc_lands (libelleL, idTL) VALUES(?, (SELECT idTL FROM sc_land_types WHERE libelleTL LIKE ?));";
		try (Connection connection = ds.getConnection()) {
			try(PreparedStatement ps = connection.prepareStatement(sql)){
				ps.setString(1, land.getName());
				ps.setString(2, LandType.SYSTEM.toString());

				ps.executeUpdate();
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}	

	@Override
	public void deleteLand(Land land) {
		String trustsSql = "DELETE FROM sc_trusts WHERE idL=?";
		try (Connection connection = ds.getConnection()) {
			try(PreparedStatement ps = connection.prepareStatement(trustsSql)){
				ps.setInt(1, land.getId());
				ps.executeUpdate();
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		String chunksSql = "DELETE FROM sc_chunks WHERE idL=?";
		try (Connection connection = ds.getConnection()) {
			try(PreparedStatement ps = connection.prepareStatement(chunksSql)){
				ps.setInt(1, land.getId());
				ps.executeUpdate();
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		String flagsSql = "DELETE FROM sc_flags WHERE idL=?";
		try (Connection connection = ds.getConnection()) {
			try(PreparedStatement ps = connection.prepareStatement(flagsSql)){
				ps.setInt(1, land.getId());
				ps.executeUpdate();
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		String bansSql = "DELETE FROM sc_lands_bans WHERE idL=?";
		try (Connection connection = ds.getConnection()) {
			try(PreparedStatement ps = connection.prepareStatement(bansSql)){
				ps.setInt(1, land.getId());
				ps.executeUpdate();
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		String linksSql = "DELETE FROM sc_lands_links WHERE idL=?";
		try (Connection connection = ds.getConnection()) {
			try(PreparedStatement ps = connection.prepareStatement(linksSql)){
				ps.setInt(1, land.getId());
				ps.executeUpdate();
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		String sql = "DELETE FROM sc_lands WHERE idL=?";
		try (Connection connection = ds.getConnection()) {
			try(PreparedStatement ps = connection.prepareStatement(sql)){
				ps.setInt(1, land.getId());
				ps.executeUpdate();
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getLandID(LandType type, UUID uuid, String name) {
		String sql = "SELECT idL FROM sc_lands L JOIN sc_land_types TL ON TL.idTL=L.idTL WHERE TL.libelleTL=? AND L.uuid=? AND L.libelleL=? LIMIT 1;";  
		int id = 0;
		try(Connection connection = ds.getConnection()){
			try(PreparedStatement ps = connection.prepareStatement(sql)){
				ps.setString(1, type.toString());
				ps.setString(2, uuid.toString());
				ps.setString(3, name);
				try(ResultSet rs = ps.executeQuery()){
					while(rs.next()) {
						id = rs.getInt("idL");
					}
				}
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}	
		return id;
	}

	@Override
	public int getSystemLandID(String name) {
		String sql = "SELECT idL FROM sc_lands L JOIN sc_land_types TL ON TL.idTL=L.idTL WHERE TL.libelleTL=? AND L.libelleL=? LIMIT 1;";  
		int id = 0;
		try(Connection connection = ds.getConnection()){
			try(PreparedStatement ps = connection.prepareStatement(sql)){
				ps.setString(1, LandType.SYSTEM.toString());
				ps.setString(2, name);
				try(ResultSet rs = ps.executeQuery()){
					while(rs.next()) {
						id = rs.getInt("idL");
					}
				}
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}	
		return id;
	}

	@Override
	public void setChunk(Land land, SChunk chunk) {
		try(Connection connection = ds.getConnection()){
			try(PreparedStatement ps = connection.prepareStatement(
					"INSERT INTO sc_chunks (server, world, x, z, idL) " +
					"VALUES(?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE idL=VALUES(idL);")){
				ps.setString(1, chunk.getServer());
				ps.setString(2, chunk.getWorld());
				ps.setInt(3, chunk.getX());
				ps.setInt(4, chunk.getZ());
				ps.setInt(5, land.getId());
				ps.executeUpdate();
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}		
	}

	@Override
	public void removeChunk(SChunk chunk) {
		String sql = "DELETE FROM sc_chunks WHERE server LIKE ? AND world LIKE ? AND x=? AND z=?;";
		try (Connection connection = ds.getConnection()) {
			try(PreparedStatement ps = connection.prepareStatement(sql)){
				ps.setString(1, chunk.getServer());
				ps.setString(2, chunk.getWorld());
				ps.setInt(3, chunk.getX());
				ps.setInt(4, chunk.getZ());
				ps.executeUpdate();
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void loadTrusts(Land land) {
		String sql = "SELECT T.uuid, LP.libelleLP FROM sc_trusts T JOIN sc_land_permissions LP ON T.idLP=LP.idLP WHERE T.idL=?;";
		try(Connection connection = ds.getConnection()){
			try(PreparedStatement ps = connection.prepareStatement(sql)){
				ps.setInt(1, land.getId());
				try(ResultSet rs = ps.executeQuery()){
					while(rs.next()) {
						Action action = Action.valueOf(rs.getString("libelleLP"));
						String id = rs.getString("uuid");
						if(id.equals("GLOBAL")) {
							land.trust(action);
						}else {
							UUID uuid = UUID.fromString(id);
							land.trust(uuid, action);
						}
					}
				}
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}	
	}

	//	@Override
	//	public Trust loadGlobalTrust(Land land) {
	//		String sql = "SELECT LP.libelleLP FROM sc_trusts T JOIN sc_land_permissions LP ON T.idLP=LP.idLP " + 
	//				"WHERE T.idL=? AND T.uuid LIKE 'GLOBAL' LIMIT 1;";
	//		Trust trust = new Trust();
	//		try(Connection connection = ds.getConnection()){
	//			try(PreparedStatement ps = connection.prepareStatement(sql)){
	//				ps.setInt(1, land.getId());
	//				try(ResultSet rs = ps.executeQuery()){
	//					if(rs.next()) {
	//						Action action = Action.valueOf(rs.getString("libelleLP"));
	//						trust.addPermission(action);
	//					}
	//				}
	//			}
	//		}catch (SQLException e) {
	//			e.printStackTrace();
	//		}
	//		return trust;
	//	}

	@Override
	public void addTrust(Land land, UUID uuid, Action action) {
		try (Connection connection = ds.getConnection()) {
			try(PreparedStatement ps = connection.prepareStatement(
					"INSERT INTO sc_trusts (idL, uuid, idLP) VALUES("
							+ "?, "
							+ "?,"
							+ " (SELECT idLP FROM sc_land_permissions WHERE libelleLP LIKE ?));")){
				ps.setInt(1, land.getId());
				ps.setString(2, uuid.toString());
				ps.setString(3, action.toString());
				ps.executeUpdate();
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void removeTrust(Land land, UUID uuid, Action action) {
		try (Connection connection = ds.getConnection()) {
			try(PreparedStatement ps = connection.prepareStatement(
					"DELETE FROM sc_trusts "
							+ "WHERE idL=? "
							+ "AND uuid=? "
							+ "AND idLP=(SELECT idLP FROM sc_land_permissions WHERE libelleLP LIKE ?);")){
				ps.setInt(1, land.getId());
				ps.setString(2, uuid.toString());
				ps.setString(3, action.toString());
				ps.executeUpdate();
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void addGlobalTrust(Land land, Action action) {
		try (Connection connection = ds.getConnection()) {
			try(PreparedStatement ps = connection.prepareStatement(
					"INSERT INTO sc_trusts (idL, uuid, idLP) VALUES("
							+ "?, "
							+ "'GLOBAL', "
							+ " (SELECT idLP FROM sc_land_permissions WHERE libelleLP LIKE ?));")){
				ps.setInt(1, land.getId());
				ps.setString(2, action.toString());
				ps.executeUpdate();
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void removeGlobalTrust(Land land, Action action) {
		try (Connection connection = ds.getConnection()) {
			try(PreparedStatement ps = connection.prepareStatement(
					"DELETE FROM sc_trusts "
							+ "WHERE idL=? "
							+ "AND uuid LIKE 'GLOBAL' "
							+ "AND idLP=(SELECT idLP FROM sc_land_permissions WHERE libelleLP LIKE ?);")){
				ps.setInt(1, land.getId());
				ps.setString(2, action.toString());
				ps.executeUpdate();
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Map<SChunk, Integer> getChunks(UUID uuid) {
		String sql = "SELECT * FROM `sc_chunks` HAVING idL IN (SELECT DISTINCT idL FROM sc_lands WHERE uuid LIKE '?'";
		Map<SChunk, Integer> chunks = new HashMap<>();
		try(Connection connection = ds.getConnection()){
			try(PreparedStatement ps = connection.prepareStatement(sql)){
				ps.setString(1, uuid.toString());
				try(ResultSet rs = ps.executeQuery()){
					while(rs.next()) {
						int id = rs.getInt("idL");
						String server = rs.getString("server");
						String world = rs.getString("world");
						int x = rs.getInt("x");
						int z = rs.getInt("z");
						chunks.put(new SChunk(server, world, x, z), id);
					}
				}
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return chunks;
	}

	@Override
	public int getChunkCount(UUID uuid) {
		String sql = "SELECT COUNT(*) FROM sc_chunks WHERE idL IN (SELECT DISTINCT idL FROM sc_lands WHERE uuid LIKE ?);";
		int count = 0;
		try(Connection connection = ds.getConnection()){
			try(PreparedStatement ps = connection.prepareStatement(sql)){
				ps.setString(1, uuid.toString());
				try(ResultSet rs = ps.executeQuery()){
					while(rs.next()) {
						count = rs.getInt("COUNT(*)");
					}
				}
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return count;
	}

	@Override
	public Map<SChunk, Integer> getChunks(Land land) {
		String sql = "SELECT * FROM `sc_chunks` WHERE idL=?";
		Map<SChunk, Integer> chunks = new HashMap<>();
		try(Connection connection = ds.getConnection()){
			try(PreparedStatement ps = connection.prepareStatement(sql)){
				ps.setInt(1, land.getId());
				try(ResultSet rs = ps.executeQuery()){
					while(rs.next()) {
						int id = rs.getInt("idL");
						String server = rs.getString("server");
						String world = rs.getString("world");
						int x = rs.getInt("x");
						int z = rs.getInt("z");
						chunks.put(new SChunk(server, world, x, z), id);
					}
				}
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return chunks;
	}

	@Override
	public Set<Flag> getFlags(Land land) {
		Set<Flag> flags = new HashSet<>();
		String sql = "SELECT LF.libelleTF FROM sc_flags F JOIN sc_land_flags LF ON F.idTF=LF.idTF WHERE F.idL=?;";
		try(Connection connection = ds.getConnection()){
			try(PreparedStatement ps = connection.prepareStatement(sql)){
				ps.setInt(1, land.getId());
				try(ResultSet rs = ps.executeQuery()){
					while(rs.next()) {
						flags.add(Flag.valueOf(rs.getString("libelleTF")));

					}
				}
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}			
		return flags;
	}

	@Override
	public void addFlag(Land land, Flag flag) {
		try (Connection connection = ds.getConnection()) {
			try(PreparedStatement ps = connection.prepareStatement(
					"INSERT INTO sc_flags (idL, idTF) VALUES("
							+ "?, "
							+ " (SELECT idTF FROM sc_land_flags WHERE libelleTF LIKE ?));")){
				ps.setInt(1, land.getId());
				ps.setString(2, flag.toString());
				ps.executeUpdate();
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}		
	}

	@Override
	public void removeFlag(Land land, Flag flag) {
		try (Connection connection = ds.getConnection()) {
			try(PreparedStatement ps = connection.prepareStatement(
					"DELETE FROM sc_flags "
							+ "WHERE idL=? "
							+ "AND idTF=(SELECT idTF FROM sc_land_flags WHERE libelleTF LIKE ?);")){
				ps.setInt(1, land.getId());
				ps.setString(2, flag.toString());
				ps.executeUpdate();
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Set<UUID> getBans(Land land) {
		Set<UUID> bans = new HashSet<>();
		String sql = "SELECT uuid FROM sc_lands_bans WHERE idL=?;";
		try(Connection connection = ds.getConnection()){
			try(PreparedStatement ps = connection.prepareStatement(sql)){
				ps.setInt(1, land.getId());
				try(ResultSet rs = ps.executeQuery()){
					while(rs.next()) {
						bans.add(UUID.fromString(rs.getString("uuid")));

					}
				}
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}			
		return bans;
	}

	@Override
	public void addBan(Land land, UUID uuid) {
		try (Connection connection = ds.getConnection()) {
			try(PreparedStatement ps = connection.prepareStatement(
					"INSERT INTO sc_lands_bans VALUES(?,?);")){
				ps.setInt(1, land.getId());
				ps.setString(2, uuid.toString());
				ps.executeUpdate();
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}			
	}

	@Override
	public void removeBan(Land land, UUID uuid) {
		try (Connection connection = ds.getConnection()) {
			try(PreparedStatement ps = connection.prepareStatement(
					"DELETE FROM sc_lands_bans "
							+ "WHERE idL=? "
							+ "AND uuid=?;")){
				ps.setInt(1, land.getId());
				ps.setString(2, uuid.toString());
				ps.executeUpdate();
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void loadLinks(LandManager manager) {
		String sql = "SELECT * FROM sc_lands_links;";
		try(Connection connection = ds.getConnection()){
			try(PreparedStatement ps = connection.prepareStatement(sql)){
				try(ResultSet rs = ps.executeQuery()){
					while(rs.next()) {
						int idL = rs.getInt("idL");
						int idLW = rs.getInt("idLW");

						Land land = manager.getLands().get(idL);
						Land landwith = manager.getLands().get(idLW);
						Link link = Link.valueOf(rs.getString("LinkType"));

						if(landwith != null) {
							land.addLink(link, landwith);
						}else {
							removeLink(land, link);
						}
					}
				}
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void addLink(Land land, Link link, Land with) {
		try (Connection connection = ds.getConnection()) {
			try(PreparedStatement ps = connection.prepareStatement(
					"INSERT INTO sc_lands_links VALUES(?,?,?);")){
				ps.setInt(1, land.getId());
				ps.setInt(2, with.getId());
				ps.setString(3, link.toString());
				ps.executeUpdate();
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}	
	}

	@Override
	public void removeLink(Land land, Link link) {
		try (Connection connection = ds.getConnection()) {
			try(PreparedStatement ps = connection.prepareStatement(
					"DELETE FROM sc_lands_links "
							+ "WHERE idL=? "
							+ "AND LinkType LIKE ?;")){
				ps.setInt(1, land.getId());
				ps.setString(2, link.toString());
				ps.executeUpdate();
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void renameLand(Land land, String name) {
		String sql = "UPDATE sc_lands SET libelleL=? WHERE idL=?;";
		try (Connection connection = ds.getConnection()) {
			try(PreparedStatement ps = connection.prepareStatement(sql)){
				ps.setString(1, name);
				ps.setInt(2, land.getId());
				ps.executeUpdate();
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}	
	}


}
