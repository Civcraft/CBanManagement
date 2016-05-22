package vg.civcraft.mc.cbanman.bungee;

import java.util.UUID;

import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import vg.civcraft.mc.cbanman.ban.Ban;

public class BungeeListener implements Listener{
	private CBanManBungee plugin;
	
	public BungeeListener(CBanManBungee plugin) {
		this.plugin = plugin;
		plugin.getProxy().getPluginManager().registerListener(plugin, this);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void postLogin(LoginEvent event){
		UUID uuid = event.getConnection().getUniqueId();
		if (uuid == null){
			// Something is wrong if the player tries to connect without a uuid. 
			//   Fail fast if thats the case.
			event.setCancelled(true);
			event.setCancelReason("Failed to Authenticate");
			return;
		}
		if (!BanAPI.getInstance().isBanned(uuid)){
			return;
		}
		Ban ban = plugin.getBannedPlayers().get(uuid).getHighestLevelBan();
		event.setCancelled(true);
		event.setCancelReason(ban.getMessage());
		
	}
}
