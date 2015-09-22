package vg.civcraft.mc.cbanman.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.Bukkit;

import vg.civcraft.mc.cbanman.CBanManagement;
import vg.civcraft.mc.cbanman.ban.Ban;
import vg.civcraft.mc.cbanman.ban.BanLevel;
import vg.civcraft.mc.cbanman.ban.CBanList;
import vg.civcraft.mc.civmodcore.Config;
import vg.civcraft.mc.civmodcore.annotations.CivConfig;
import vg.civcraft.mc.civmodcore.annotations.CivConfigType;
import vg.civcraft.mc.civmodcore.annotations.CivConfigs;

public class SqlManager {

	private CBanManagement plugin;
	private Config config;
	private Database db;
	
	private String insertData, updateData, removeData, getAllData;
	

	
	public SqlManager(CBanManagement plugin) {
		this.plugin = plugin;
		config = plugin.GetConfig();
		initializeStrings();
	}
	
	private void initializeStrings() {
		insertData = "insert into ban_list(uuid, ban_flag, plugin_name, message) values (?,?,?,?);";
		removeData = "delete from ban_list where uuid=";
		updateData = "update ban_list set ";
		getAllData = "select * from ban_list;";
	}
	
	@CivConfigs({
		@CivConfig(name = "mysql.username", def = "bukkit", type = CivConfigType.String),
		@CivConfig(name = "mysql.password", def = "", type = CivConfigType.String),
		@CivConfig(name = "mysql.host", def = "localhost", type = CivConfigType.String),
		@CivConfig(name = "mysql.dbname", def = "bukkit", type = CivConfigType.String),
		@CivConfig(name = "mysql.port", def = "3306", type = CivConfigType.Int)
	})
	private boolean loadDB() {
		String username = config.get("mysql.username").getString();
		String password = config.get("mysql.password").getString();
		String host = config.get("mysql.host").getString();
		String dbname = config.get("mysql.dbname").getString();
		int port = config.get("mysql.port").getInt();
		db = new Database(host, port, dbname, username, password, plugin.getLogger());
		if (!db.connect()) {
			plugin.getLogger().warning("MySql could not connect, shutting down.");
			Bukkit.getPluginManager().disablePlugin(plugin);
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
				+ "primary key (uuid, ban_flag, plugin_name, message));");
	}

	public boolean load() {
		plugin.getLogger().info("Connecting to database...");
		if (loadDB() == false)
			return false;
		PreparedStatement playerData = db.prepareStatement(getAllData);
		ResultSet set;
		try {
			set = playerData.executeQuery();
			while (set.next()) {
				UUID uuid = UUID.fromString(set.getString("uuid"));
				BanLevel banlevel = BanLevel.HIGH.fromByte(set.getByte("ban_flag"));
				String pluginname = set.getString("plugin_name");
				String message = set.getString("message");
				Ban ban = new Ban(banlevel, pluginname, message);
				CBanList banlist = plugin.getBannedPlayers().get(uuid);
				if (banlist == null){
					banlist = new CBanList();
					banlist.addBan(ban);
					plugin.getBannedPlayers().put(uuid, banlist);
				} else {
					banlist.addBan(ban);
				}
			}
			plugin.getLogger().info("Loaded from database!");
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	
	public void updateBan(UUID uuid, Ban ban){
		PreparedStatement updateBan = db.prepareStatement(updateData
				+ "ban_flag='"+ban.getBanLevel().value()+"',"
				+ "plugin_name='"+ban.getPluginName()+"',"
				+ "message='"+ban.getMessage()+"' "
				+ "where uuid='"+uuid.toString()+"' AND "
				+ "plugin_name='"+ban.getPluginName()+"'");
		try {
			updateBan.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void banPlayer(UUID uuid, Ban ban) {
		PreparedStatement banPlayer = db.prepareStatement(insertData);
		try {
			banPlayer.setString(1, uuid.toString());
			banPlayer.setByte(2, ban.getBanLevel().value());
			banPlayer.setString(3, ban.getPluginName());
			banPlayer.setString(4, ban.getMessage());
			banPlayer.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void unbanPlayer(UUID uuid, String pluginname){
		PreparedStatement unbanPlayer = db.prepareStatement(removeData+
				"'"+uuid.toString()+"' AND "
				+ "plugin_name='"+pluginname+"';");
		try {
			unbanPlayer.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void unbanPlayerAll(UUID uuid) {
		PreparedStatement unbanPlayer = db.prepareStatement(removeData+
				"'"+uuid.toString()+"';");
		try {
			unbanPlayer.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}