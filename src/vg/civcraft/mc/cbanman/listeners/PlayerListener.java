package vg.civcraft.mc.cbanman.listeners;

import java.util.UUID;

import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.BanList.Type;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerKickEvent;
import vg.civcraft.mc.cbanman.CBanManagement;
import vg.civcraft.mc.cbanman.ban.Ban;
import vg.civcraft.mc.cbanman.ban.BanLevel;
import vg.civcraft.mc.civmodcore.annotations.CivConfig;
import vg.civcraft.mc.civmodcore.annotations.CivConfigType;
import vg.civcraft.mc.civmodcore.annotations.CivConfigs;

public class PlayerListener implements Listener {
	private CBanManagement plugin;
	
	public PlayerListener(CBanManagement plugin){
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onConnect(AsyncPlayerPreLoginEvent event){
		UUID uuid = event.getUniqueId(), uuid2;
		if (plugin.isBanned(uuid)){
			
		} else if (plugin.isBanned(uuid2 = UUID.nameUUIDFromBytes(event.getName().toLowerCase().getBytes()))) {
			uuid = uuid2;
		} else {
			return;
		}
		Ban ban = plugin.getBannedPlayers().get(uuid).getHighestLevelBan();
		event.disallow(Result.KICK_BANNED, ban.getMessage());
	}
	
	@CivConfigs({
		@CivConfig(name = "thirdpartyban.pluginname", def = "3rdparty", type = CivConfigType.String),
		@CivConfig(name = "thirdpartyban.banmessageifnotdefined", def = "You have been banned from this server.", type = CivConfigType.String),
		@CivConfig(name = "thirdpartyban.banlevel", def = "2", type = CivConfigType.Int),
	})
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onThirdPartyBan(PlayerKickEvent event){
		Player p = event.getPlayer();
		if (p.isBanned() && !plugin.isBanned(p)){
			BanList banlist = plugin.getServer().getBanList(Type.NAME);
			BanEntry ban = banlist.getBanEntry(p.getName());
			String reason = ban.getReason();
			banlist.pardon(p.getName());
			if (reason == null){reason = plugin.GetConfig().get("thirdpartyban.banmessageifnotdefined").getString();}
			Ban newban = new Ban(
					BanLevel.HIGH.fromInt(plugin.GetConfig().get("thirdpartyban.banlevel").getInt()),
					plugin.GetConfig().get("thirdpartyban.pluginname").getString(),
					reason);
			plugin.banPlayer(p, newban);
		}
	}
	
}
