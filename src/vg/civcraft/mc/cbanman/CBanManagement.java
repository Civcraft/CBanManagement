package vg.civcraft.mc.cbanman;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import vg.civcraft.mc.cbanman.ban.Ban;
import vg.civcraft.mc.cbanman.ban.BanLevel;
import vg.civcraft.mc.cbanman.ban.CBanList;
import vg.civcraft.mc.cbanman.database.SqlManager;
import vg.civcraft.mc.cbanman.listeners.PlayerListener;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.namelayer.NameAPI;

public class CBanManagement extends ACivMod {
	private static CBanManagement plugin;
	private Map<UUID, CBanList> bannedPlayers;
	private SqlManager sqlman;
	private PlayerListener plyr;
	private boolean isNameLayerEnabled = false;

	@Override
	public void onEnable() {
		super.onEnable();
		plugin = this;
		bannedPlayers = new HashMap<UUID, CBanList>();
		sqlman = new SqlManager(plugin);
		isNameLayerEnabled = getServer().getPluginManager().isPluginEnabled("NameLayer");
		if (sqlman.load() == false)
			return;
		plyr = new PlayerListener(plugin);
		this.getServer().getPluginManager().registerEvents(plyr, plugin);
	}

	@Override
	protected String getPluginName() {
		return "CBanManagement";
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
			uuid = plugin.getServer().getOfflinePlayer(name).getUniqueId();
		}
		if (uuid == null){
			return false;
		} else {
			banPlayer(uuid, banlevel, pluginname, message);
			return true;
		}
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
			uuid = plugin.getServer().getOfflinePlayer(name).getUniqueId();
		}
		if (uuid == null){
			return false;
		} else {
			banPlayer(uuid, ban);
			return true;
		}
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
			for (Ban ban : banlist.getBanList()){
				if (ban.getPluginName().equals(newban.getPluginName())){
					previous = ban;
					break;
				}
			}
			if (previous != null){
				banlist.removeBan(previous);
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
			uuid = plugin.getServer().getOfflinePlayer(name).getUniqueId();
		}
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
	
	@SuppressWarnings("deprecation")
	public boolean unbanPlayer(String name, String pluginname){
		if (name == null || pluginname == null){return false;}
		if (name.isEmpty() || pluginname.isEmpty()){return false;}
		UUID uuid = null;
		if (isNameLayerEnabled){
			uuid = NameAPI.getUUID(name);
		} else {
			uuid = plugin.getServer().getOfflinePlayer(name).getUniqueId();
		}
		if (uuid == null){
			return false;
		} else {
			unbanPlayer(uuid, pluginname);
			return true;
		}
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
		for (Ban ban : banlist.getBanList()){
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
			uuid = plugin.getServer().getOfflinePlayer(name).getUniqueId();
		}
		if (uuid == null){
			return false;
		} else {
			unbanPlayerAll(uuid);
			return true;
		}
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
	}
}
