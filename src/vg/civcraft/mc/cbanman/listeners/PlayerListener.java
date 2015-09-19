package vg.civcraft.mc.cbanman.listeners;

import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

import vg.civcraft.mc.cbanman.CBanManagement;
import vg.civcraft.mc.cbanman.ban.Ban;

public class PlayerListener implements Listener {
	private CBanManagement plugin;
	
	public PlayerListener(CBanManagement plugin){
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onConnect(AsyncPlayerPreLoginEvent event){
		UUID uuid = event.getUniqueId();
		if (!plugin.isBanned(uuid)){
			return;
		}
		Ban ban = plugin.getBannedPlayers().get(uuid);
		event.disallow(Result.KICK_BANNED, ban.getMessage());
	}
	
}
