package vg.civcraft.mc.cbanman;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.BanList.Type;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import vg.civcraft.mc.cbanman.ban.Ban;
import vg.civcraft.mc.cbanman.ban.BanLevel;
import vg.civcraft.mc.cbanman.ban.CBanList;
import vg.civcraft.mc.cbanman.database.SqlManager;
import vg.civcraft.mc.cbanman.listeners.MercuryMessageListener;
import vg.civcraft.mc.cbanman.listeners.PlayerListener;
import vg.civcraft.mc.cbanman.handler.CommandHandler;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.annotations.CivConfig;
import vg.civcraft.mc.civmodcore.annotations.CivConfigType;
import vg.civcraft.mc.mercury.MercuryAPI;
import vg.civcraft.mc.namelayer.NameAPI;

public class CBanManagement extends ACivMod {
	private static CBanManagement plugin;
	private Map<UUID, CBanList> bannedPlayers;
	private SqlManager sqlman;
	private PlayerListener plyr;
	private CommandHandler cmdHandler;
	private MercuryMessageListener mercury;
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
	}
	
	@CivConfig(name = "import_native_bans", def = "false", type = CivConfigType.Bool)
	private void importBans() {
		if (!plugin.GetConfig().get("import_native_bans").getBool()){return;}
		
		BanList banned = this.getServer().getBanList(Type.NAME);
		if (banned.getBanEntries().size() == 0){return;}
		plugin.getLogger().info("Importing "+banned.getBanEntries().size()+" native ban(s)...");
		int counter = 0;
		Ban newban = new Ban(
				BanLevel.HIGH.fromInt(plugin.GetConfig().get("adminban.banlevel").getInt()),
				plugin.GetConfig().get("adminban.pluginname").getString(),
				plugin.GetConfig().get("adminban.banmessage").getString()				
				);
		for (BanEntry ban : banned.getBanEntries()){
			if (banPlayer(ban.getTarget().toLowerCase(),newban)){
				banned.pardon(ban.getTarget());
				counter++;
			}

		}
		plugin.getLogger().info("Imported "+counter+" Native Ban(s)!");
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
	
	public boolean banPlayer(String name, byte banlevel, String pluginname, String message){
		return banPlayer(name, BanLevel.HIGH.fromByte(banlevel), pluginname, message);
	}
	
	@SuppressWarnings("deprecation")
	public boolean banPlayer(String name, BanLevel banlevel, String pluginname, String message){
		if (name == null || banlevel == null){return false;}
		if (name.isEmpty()){return false;}
		UUID uuid = null;
		if (isNameLayerEnabled){
			uuid = NameAPI.getUUID(name);
		} else {
			OfflinePlayer p = plugin.getServer().getOfflinePlayer(name);
			if (p != null)
				uuid = p.getUniqueId();
		}
		if (uuid == null){
			banPlayer(UUID.fromString(name.toLowerCase()),banlevel, pluginname, message);
		} else {
			banPlayer(uuid, banlevel, pluginname, message);
		}
		return true;
	}
	
	public void banPlayer(Player player, BanLevel banlevel, String pluginname, String message){
		if (player == null){return;}
		banPlayer(player.getUniqueId(), banlevel, pluginname, message);
	}
	
	public void banPlayer(UUID uuid, BanLevel banlevel, String pluginname, String message){
		Ban ban = new Ban(banlevel, pluginname, message);
		banPlayer(uuid, ban);
	}
	
	@SuppressWarnings("deprecation")
	public boolean banPlayer(String name, Ban ban){
		if (name == null){return false;}
		if (name.isEmpty()){return false;}
		UUID uuid = null;
		if (isNameLayerEnabled){
			uuid = NameAPI.getUUID(name);
		} else {
			OfflinePlayer p = plugin.getServer().getOfflinePlayer(name);
			if (p != null)
				uuid = p.getUniqueId();
		}
		if (uuid == null){
			banPlayer(UUID.nameUUIDFromBytes(name.toLowerCase().getBytes()), ban);
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
		if (isBanned(uuid)){
			CBanList banlist = bannedPlayers.get(uuid);
			Ban previous = null;
			for (Ban ban : banlist.getList()){
				if (ban.getPluginName().equals(newban.getPluginName())){
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
			CBanList banlist = new CBanList();
			banlist.addBan(newban);
			bannedPlayers.put(uuid, banlist);
			sqlman.banPlayer(uuid, newban);
		}
		if (isMercuryEnabled){
			MercuryAPI.instance.sendMessage("all", 
					"ban~"+uuid.toString()+"~"+newban.getBanLevel().value()+"~"+newban.getPluginName()+"~"+newban.getMessage(),
					"banman");
		}
		for (Player p : plugin.getServer().getOnlinePlayers()){
			if (p.getUniqueId().equals(uuid)){
				p.kickPlayer(newban.getMessage());
				break;
			}
		}
	}
	
	
	@SuppressWarnings("deprecation")
	public boolean isBanned(String name){
		if (name == null){return false;}
		if (name.isEmpty()){return false;}
		UUID uuid = null;
		if (isNameLayerEnabled){
			uuid = NameAPI.getUUID(name);
		} else {
			OfflinePlayer p = plugin.getServer().getOfflinePlayer(name);
			if (p != null)
				uuid = p.getUniqueId();
		}
		if (uuid == null){
			return isBanned(UUID.nameUUIDFromBytes(name.toLowerCase().getBytes()));
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
	
	@SuppressWarnings("deprecation")
	public boolean unbanPlayer(String name, String pluginname){
		if (name == null || pluginname == null){return false;}
		if (name.isEmpty() || pluginname.isEmpty()){return false;}
		UUID uuid = null;
		if (isNameLayerEnabled){
			uuid = NameAPI.getUUID(name);
		} else {
			OfflinePlayer p = plugin.getServer().getOfflinePlayer(name);
			if (p != null)
				uuid = p.getUniqueId();
		}
		if (uuid == null){
			unbanPlayer(UUID.nameUUIDFromBytes(name.toLowerCase().getBytes()),pluginname);
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
		if (!isBanned(uuid)){return;}
		pluginname = pluginname.toLowerCase();
		CBanList banlist = bannedPlayers.get(uuid);
		Ban rem = null;
		for (Ban ban : banlist.getList()){
			if (ban.getPluginName().equals(pluginname)){
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
	
	@SuppressWarnings("deprecation")
	public boolean unbanPlayerAll(String name){
		if (name == null){return false;}
		if (name.isEmpty()){return false;}
		UUID uuid = null;
		if (isNameLayerEnabled){
			uuid = NameAPI.getUUID(name);
		} else {
			OfflinePlayer p = plugin.getServer().getOfflinePlayer(name);
			if (p != null)
				uuid = p.getUniqueId();
		}
		if (uuid == null){
			unbanPlayerAll(UUID.nameUUIDFromBytes(name.toLowerCase().getBytes()));
		} else {
			unbanPlayerAll(uuid);
			unbanPlayerAll(UUID.nameUUIDFromBytes(name.toLowerCase().getBytes()));
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
}
