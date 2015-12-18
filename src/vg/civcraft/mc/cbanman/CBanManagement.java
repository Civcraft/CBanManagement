package vg.civcraft.mc.cbanman;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.BanList.Type;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import vg.civcraft.mc.cbanman.ban.AsyncBan;
import vg.civcraft.mc.cbanman.ban.Ban;
import vg.civcraft.mc.cbanman.ban.BanLevel;
import vg.civcraft.mc.cbanman.ban.CBanList;
import vg.civcraft.mc.cbanman.database.SqlManager;
import vg.civcraft.mc.cbanman.listeners.MercuryMessageListener;
import vg.civcraft.mc.cbanman.listeners.PlayerListener;
import vg.civcraft.mc.cbanman.listeners.TempBanListener;
import vg.civcraft.mc.cbanman.handler.CommandHandler;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.annotations.CivConfig;
import vg.civcraft.mc.civmodcore.annotations.CivConfigType;
import vg.civcraft.mc.civmodcore.annotations.CivConfigs;
import vg.civcraft.mc.mercury.MercuryAPI;
import vg.civcraft.mc.namelayer.NameAPI;

public class CBanManagement extends ACivMod {
	private static CBanManagement plugin;
	private Map<UUID, CBanList> bannedPlayers;
	private SqlManager sqlman;
	private PlayerListener plyr;
	private CommandHandler cmdHandler;
	private MercuryMessageListener mercury;
	private TempBanListener tempListener;
	private boolean isNameLayerEnabled = false;
	private boolean isMercuryEnabled = false;

	@Override
	public void onEnable() {
		super.onEnable();
		plugin = this;
		sqlman = new SqlManager(plugin);
		isNameLayerEnabled = getServer().getPluginManager().isPluginEnabled("NameLayer");
		isMercuryEnabled = getServer().getPluginManager().isPluginEnabled("Mercury");
		if (reload() == false)
			return;
		plyr = new PlayerListener(plugin);
		this.getServer().getPluginManager().registerEvents(plyr, plugin);
		cmdHandler = new CommandHandler(this, isNameLayerEnabled);
		for (String command : getDescription().getCommands().keySet()) {
			getCommand(command).setExecutor(cmdHandler);
		}
		if (isMercuryEnabled){
			mercury = new MercuryMessageListener(plugin);
			this.getServer().getPluginManager().registerEvents(mercury, plugin);
			MercuryAPI.instance.registerPluginMessageChannel("banman");
		}
		importBans();
		if (plugin.GetConfig().get("tempbans.scan_enabled").getBool()){
			tempListener = new TempBanListener(plugin);
			plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, tempListener
					, 0L, 20L*60*plugin.GetConfig().get("tempbans.minutes_between_scan").getInt());
			plugin.getLogger().info("Scheduled temp ban scan");
		}
	}

	@CivConfigs({
		@CivConfig(name = "tempbans.scan_enabled", def = "false", type = CivConfigType.Bool),
		@CivConfig(name = "tempbans.minutes_between_scan", def= "1", type = CivConfigType.Int),
		@CivConfig(name = "import_native_bans", def = "false", type = CivConfigType.Bool)
	})
	private void importBans() {
		if (!plugin.GetConfig().get("import_native_bans").getBool()){return;}

		BanList banned = this.getServer().getBanList(Type.NAME);
		if (banned.getBanEntries().size() == 0){return;}
		plugin.getLogger().info("Importing "+banned.getBanEntries().size()+" native ban(s)...");
		int counter = 0;
		ArrayList<String> async = new ArrayList<String>();
		Ban newban = new Ban(
				BanLevel.HIGH.fromInt(plugin.GetConfig().get("adminban.banlevel").getInt()),
				plugin.GetConfig().get("adminban.pluginname").getString(),
				plugin.GetConfig().get("adminban.banmessage").getString()				
				);
		for (BanEntry ban : banned.getBanEntries()){
			if (banPlayer(ban.getTarget().toLowerCase(),newban)){
				banned.pardon(ban.getTarget());
				counter++;
			} else {
				async.add(ban.getTarget().toLowerCase());
			}

		}
		plugin.getLogger().info("Imported "+counter+" native Ban(s)!");
		if (!async.isEmpty()){
			plugin.getLogger().info("Attempting to AsyncBan "+async.size()+" native bans...");
			AsyncBan aban = new AsyncBan(plugin, async, newban, plugin.getServer().getConsoleSender());
			plugin.getServer().getScheduler().runTaskAsynchronously(plugin, aban);
		}
	}

	@Override
	protected String getPluginName() {
		return "CBanManagement";
	}

	public boolean reload(){
		bannedPlayers = new HashMap<UUID, CBanList>();
		return sqlman.load();
	}

	public Map<UUID, CBanList> getBannedPlayers(){
		return bannedPlayers;
	}


	public boolean banPlayer(String name, Ban ban){
		if (name == null){return false;}
		if (name.isEmpty()){return false;}
		UUID uuid = this.grabUUID(name);
		if (uuid == null){
			return false;
		} else {
			banPlayer(uuid, ban);
		}
		return true;
	}



	public void banPlayer(Player player, Ban ban){
		if (player == null){return;}
		banPlayer(player.getUniqueId(), ban);
	}

	public void banPlayer(UUID uuid, Ban newban){
		if (uuid == null || newban == null){return;}
		CBanList banlist = bannedPlayers.get(uuid);
		if (banlist != null){

			Ban previous = null;
			for (Ban ban : banlist.getList()){
				if (ban.isPluginName(newban.getPluginName())){
					previous = ban;
					break;
				}
			}
			if (previous != null){
				banlist.addBan(newban);
				sqlman.updateBan(uuid, newban);
			} else {
				banlist.addBan(newban);
				sqlman.banPlayer(uuid, newban);
			}
		} else {
			banlist = new CBanList();
			banlist.addBan(newban);
			bannedPlayers.put(uuid, banlist);
			sqlman.banPlayer(uuid, newban);
		}
		if (isMercuryEnabled){
			MercuryAPI.instance.sendMessage("all", 
					"ban~"+uuid.toString()+"~"+newban.getBanLevel().value()+"~"+newban.getPluginName()+"~"+newban.getMessage(),
					"banman");
		}
		Player p = Bukkit.getPlayer(uuid);
		if (p != null)
			p.kickPlayer(newban.getMessage());
	}


	public boolean isBanned(String name){
		if (name == null){return false;}
		if (name.isEmpty()){return false;}
		UUID uuid = this.grabUUID(name);
		if (uuid == null){
			return false;
		} else {
			return isBanned(uuid);
		}
	}

	public boolean isBanned(Player player){
		if (player == null){return false;}
		return isBanned(player.getUniqueId());
	}

	public boolean isBanned(UUID uuid) {
		if (uuid == null){return false;}
		return bannedPlayers.containsKey(uuid);
	}

	public boolean unbanPlayer(String name, String pluginname){
		if (name == null || pluginname == null){return false;}
		if (name.isEmpty() || pluginname.isEmpty()){return false;}
		UUID uuid = this.grabUUID(name);
		if (uuid == null){
			return false;
		} else {
			unbanPlayer(uuid, pluginname);
		}
		return true;
	}

	public void unbanPlayer(Player player, String pluginname){
		if (player == null || pluginname == null){return;}
		if (pluginname.isEmpty()){return;}
		unbanPlayer(player.getUniqueId(), pluginname);
	}

	public void unbanPlayer(UUID uuid, String pluginname){
		if (uuid == null || pluginname == null){return;}
		if (pluginname.isEmpty()){return;}
		CBanList banlist = bannedPlayers.get(uuid);
		if (banlist == null){return;}
		Ban rem = null;
		for (Ban ban : banlist.getList()){
			if (ban.isPluginName(pluginname)){
				rem = ban;
				break;
			}
		}
		if (rem != null){
			banlist.removeBan(rem);
			if (banlist.getSize() == 0){
				bannedPlayers.remove(uuid);
			}
			sqlman.unbanPlayer(uuid, pluginname);
			if (isMercuryEnabled){
				MercuryAPI.instance.sendMessage("all", "unban~"+uuid.toString()+"~"+pluginname, "banman");
			}
		}
	}

	public boolean unbanPlayerAll(String name){
		if (name == null){return false;}
		if (name.isEmpty()){return false;}
		UUID uuid = this.grabUUID(name);
		if (uuid == null){
			return false;
		} else {
			unbanPlayerAll(uuid);
		}
		return true;
	}


	public void unbanPlayerAll(Player p){
		if (p == null){return;}
		unbanPlayerAll(p.getUniqueId());
	}

	public void unbanPlayerAll(UUID uuid){
		if (uuid == null){return;}
		if (!bannedPlayers.containsKey(uuid)){return;}
		bannedPlayers.remove(uuid);
		sqlman.unbanPlayerAll(uuid);
		if (isMercuryEnabled){
			MercuryAPI.instance.sendMessage("all", "unban~"+uuid.toString()+"~all", "banman");
		}
	}

	@SuppressWarnings("deprecation")
	private UUID grabUUID(String name) {
		UUID ret = null;
		if (isNameLayerEnabled){
			ret = NameAPI.getUUID(name);
		} else {
			OfflinePlayer p = plugin.getServer().getOfflinePlayer(name);
			if (p != null)
				ret = p.getUniqueId();
		}
		return ret;
	}
	
	public static CBanManagement getInstance() {
		return plugin;
	}
}
