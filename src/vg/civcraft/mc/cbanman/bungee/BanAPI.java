package vg.civcraft.mc.cbanman.bungee;

import java.util.UUID;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import vg.civcraft.mc.cbanman.ban.Ban;
import vg.civcraft.mc.cbanman.ban.CBanList;
import vg.civcraft.mc.mercury.MercuryAPI;

public class BanAPI {
	private static BanAPI instance;
	private CBanManBungee plugin;
	private SqlManager sqlman;
	
	public BanAPI (CBanManBungee plugin, SqlManager sqlman){
		instance = this;
		this.plugin = plugin;
		this.sqlman = sqlman;
	}

	public boolean isBanned(UUID uuid) {
		CBanList bans = plugin.getBannedPlayers().get(uuid);
		if (bans == null){
			return false;
		} else {
			return true;
		}
	}
	
	public void ban(UUID uuid, Ban newban){
		if (uuid == null || newban == null){return;}
		CBanList banlist = plugin.getBannedPlayers().get(uuid);
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
			plugin.getBannedPlayers().put(uuid, banlist);
			sqlman.banPlayer(uuid, newban);
		}
		MercuryAPI.sendGlobalMessage(
				"ban~"+uuid.toString()+"~"+newban.getBanLevel().value()+"~"+newban.getPluginName()+"~"+newban.getMessage(),
				"banman");
		ProxiedPlayer p = plugin.getProxy().getPlayer(uuid);
		if (p != null)
			p.disconnect(new TextComponent(newban.getMessage()));
		
	}
	
	public void unban(UUID uuid){
		if (uuid == null){return;}
		if (!plugin.getBannedPlayers().containsKey(uuid)){return;}
		plugin.getBannedPlayers().remove(uuid);
		sqlman.unbanPlayerAll(uuid);
		MercuryAPI.sendGlobalMessage("unban~"+uuid.toString()+"~all", "banman");
	}

	public static BanAPI getInstance() {
		return instance;
	}
	
	
	
}
