package vg.civcraft.mc.cbanman.bungee;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import net.md_5.bungee.config.Configuration;
import vg.civcraft.mc.cbanman.ban.Ban;
import vg.civcraft.mc.cbanman.ban.BanLevel;
import vg.civcraft.mc.cbanman.ban.CBanList;
import vg.civcraft.mc.cbanman.bungee.Database;

public class SqlManager {

	private CBanManBungee plugin;
	private Configuration config;
	private Database db;
	
	private String insertData, updateData, removeData, getAllData, removeAllData;
	
	public SqlManager(CBanManBungee plugin) {
		this.plugin = plugin;
		config = plugin.GetConfig();
		initializeStrings();
	}
	
	private void initializeStrings() {
		insertData = "insert into ban_list(uuid, ban_flag, plugin_name, message, expires) values (?,?,?,?,?);";
		removeData = "delete from ban_list where uuid=? and plugin_name=?;";
		updateData = "update ban_list set ban_flag=?, plugin_name=?, message=?, expiry=? where uuid=? and plugin_name=?;";
		getAllData = "select * from ban_list;";
		removeAllData = "delete from ban_list where uuid=?;";
	}
	
	private boolean loadDB() {
		String username = config.getString("mysql.username");
		String password = config.getString("mysql.password");
		String host = config.getString("mysql.host");
		String dbname = config.getString("mysql.dbname");
		int port = config.getInt("mysql.port");
		db = new Database(host, port, dbname, username, password, plugin.getLogger());
		if (!db.connect()) {
			plugin.getLogger().warning("MySql could not connect, shutting down.");
			plugin.getProxy().stop(); // Want to stop the server if bans aren't working.
			return false;
		}
		createTables();
		return true;
	}
	
	private void createTables() {
		db.execute("create table if not exists ban_list("
				+ "uuid varchar(36) not null,"
				+ "ban_flag tinyint not null,"
				+ "plugin_name varchar(255) not null,"
				+ "message varchar(255) not null,"
				+ "expires datetime,"
				+ "primary key (uuid, ban_flag, plugin_name, message));");
	}

	public boolean load() {
		plugin.getLogger().info("Connecting to database...");
		if (loadDB() == false)
			return false;
		if (!db.isConnected()){db.connect();}
		PreparedStatement playerData = db.prepareStatement(getAllData);
		ResultSet set;
		try {
			set = playerData.executeQuery();
			int loadCount = 0;
			while (set.next()) {
				loadCount++;
				UUID uuid = UUID.fromString(set.getString("uuid"));
				BanLevel banlevel = BanLevel.HIGH.fromByte(set.getByte("ban_flag"));
				String pluginname = set.getString("plugin_name");
				String message = set.getString("message");
				String date = set.getString("expires");
				Date expiry = null;
				if (date != null){
					if (!date.isEmpty()){
						DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						try {
							expiry = dateFormat.parse(date);
						} catch (ParseException e){
							;
						}
					}
				}
				Ban ban = new Ban(banlevel, pluginname, message, expiry);
				CBanList banlist = plugin.getBannedPlayers().get(uuid);
				if (banlist == null){
					banlist = new CBanList();
					banlist.addBan(ban);
					plugin.getBannedPlayers().put(uuid, banlist);
				} else {
					banlist.addBan(ban);
				}
			}
			plugin.getLogger().info(loadCount + " Loaded from database!");
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public void updateBan(UUID uuid, Ban ban){
		if (!db.isConnected()){db.connect();}
		PreparedStatement updateBan = db.prepareStatement(updateData);
		try {
			updateBan.setInt(1, ban.getBanLevel().value());
			updateBan.setString(2, ban.getPluginName());
			updateBan.setString(3, ban.getMessage());
			updateBan.setString(4, ban.getExpiryString());
			updateBan.setString(5, uuid.toString());
			updateBan.setString(6, ban.getPluginName());
			updateBan.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void banPlayer(UUID uuid, Ban ban) {
		if (!db.isConnected()){db.connect();}
		PreparedStatement banPlayer = db.prepareStatement(insertData);
		try {
			banPlayer.setString(1, uuid.toString());
			banPlayer.setByte(2, ban.getBanLevel().value());
			banPlayer.setString(3, ban.getPluginName());
			banPlayer.setString(4, ban.getMessage());
			String expiry = ban.getExpiryString();
			if (expiry != null){
				banPlayer.setString(5, expiry);
			} else {
				banPlayer.setNull(5, Types.NULL);
			}
			banPlayer.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void unbanPlayer(UUID uuid, String pluginname){
		if (!db.isConnected()){db.connect();}
		PreparedStatement unbanPlayer = db.prepareStatement(removeData);
		try {
			unbanPlayer.setString(1, uuid.toString());
			unbanPlayer.setString(2, pluginname);
			unbanPlayer.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void unbanPlayerAll(UUID uuid) {
		if (!db.isConnected()){db.connect();}
		PreparedStatement unbanPlayer = db.prepareStatement(removeAllData);
		try {
			unbanPlayer.setString(1, uuid.toString());
			unbanPlayer.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
