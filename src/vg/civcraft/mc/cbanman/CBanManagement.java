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

public class CBanManagement extends ACivMod {
	private static CBanManagement plugin;
	private Map<UUID, CBanList> bannedPlayers;
	private SqlManager sqlman;
	private PlayerListener plyr;

	@Override
	public void onEnable() {
		super.onEnable();
		plugin = this;
		bannedPlayers = new HashMap<UUID, CBanList>();
		sqlman = new SqlManager(plugin);
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
	
	public void banPlayer(Player player, BanLevel banlevel, String pluginname, String message){
		if (player == null){return;}
		banPlayer(player.getUniqueId(), banlevel, pluginname, message);
	}
	
	public void banPlayer(UUID uuid, BanLevel banlevel, String pluginname, String message){
		Ban ban = new Ban(banlevel, pluginname, message);
		banPlayer(uuid, ban);
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
	
	
	//TODO isBanned() using playername, grabbing uuid from namelayer
	public boolean isBanned(Player player){
		if (player == null){return false;}
		return isBanned(player.getUniqueId());
	}

	public boolean isBanned(UUID uuid) {
		if (uuid == null){return false;}
		return bannedPlayers.containsKey(uuid);
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
