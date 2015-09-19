package vg.civcraft.mc.cbanman;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import vg.civcraft.mc.cbanman.ban.Ban;
import vg.civcraft.mc.cbanman.ban.BanLevel;
import vg.civcraft.mc.cbanman.database.SqlManager;
import vg.civcraft.mc.cbanman.listeners.PlayerListener;
import vg.civcraft.mc.civmodcore.ACivMod;

public class CBanManagement extends ACivMod {
	private static CBanManagement plugin;
	private Map<UUID, Ban> bannedPlayers;
	private SqlManager sqlman;
	private PlayerListener plyr;

	@Override
	public void onEnable() {
		super.onEnable();
		plugin = this;
		bannedPlayers = new HashMap<UUID, Ban>();
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
	
	public Map<UUID, Ban> getBannedPlayers(){
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
	
	public void banPlayer(UUID uuid, Ban ban){
		if (uuid == null || ban == null){return;}
		if (isBanned(uuid)){
			sqlman.updateBan(uuid, ban);
		} else {
			sqlman.banPlayer(uuid, ban);
		}
		bannedPlayers.put(uuid, ban);
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
	
	public void unbanPlayer(Player player){
		if (player == null){return;}
		unbanPlayer(player.getUniqueId());
	}
	
	public void unbanPlayer(UUID uuid){
		if (uuid == null){return;}
		if (!isBanned(uuid)){return;}
		bannedPlayers.remove(uuid);
		sqlman.unbanPlayer(uuid);
	}
	
}
