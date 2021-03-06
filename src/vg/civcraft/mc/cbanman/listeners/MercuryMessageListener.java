package vg.civcraft.mc.cbanman.listeners;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import vg.civcraft.mc.cbanman.CBanManagement;
import vg.civcraft.mc.cbanman.ban.Ban;
import vg.civcraft.mc.cbanman.ban.BanLevel;
import vg.civcraft.mc.cbanman.ban.CBanList;
import vg.civcraft.mc.mercury.events.AsyncPluginBroadcastMessageEvent;

public class MercuryMessageListener implements Listener{
	private CBanManagement plugin;
	
	public MercuryMessageListener(CBanManagement plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onMercuryMessage(AsyncPluginBroadcastMessageEvent event){
		if (!event.getChannel().equalsIgnoreCase("banman"))
			return;
		String[] message = event.getMessage().split("~");
		String reason = message[0];
		if (reason.equals("ban")){
			UUID uuid = UUID.fromString(message[1]);
			BanLevel banlevel = BanLevel.LOW.fromByte(Byte.valueOf(message[2]));
			String pluginname = message[3];
			String msg = message[4];
			Ban ban = new Ban(banlevel,pluginname,msg);
			CBanList list = plugin.getBannedPlayers().get(uuid);
			if (list != null){
				list.addBan(ban);
			} else{
				list = new CBanList();
				list.addBan(ban);
				plugin.getBannedPlayers().put(uuid, list);
			}
			Player p = Bukkit.getPlayer(uuid);
			if (p != null)
				p.kickPlayer(msg);
		} else if (reason.equals("unban")){
			UUID uuid = UUID.fromString(message[1]);
			String pluginname = message[2];
			CBanList list = plugin.getBannedPlayers().get(uuid);
			if (pluginname.equals("all")){
				if (list != null){
					plugin.getBannedPlayers().remove(uuid);
				}
			} else {
				if (list != null){
					Ban rem = null;
					for (Ban ban : list.getList()){
						if (ban.getPluginName().equals(pluginname)){
							rem = ban;
							break;
						}
					}
					if (rem != null){
						list.removeBan(rem);
						if (list.getSize() == 0){
							plugin.getBannedPlayers().remove(uuid);
						}
					}
				}
			}
		} else if (reason.equals("disable")) {
			//plugin.unregisterPlayerListener();
			//plugin.disableCommands();
			// TODO: Reconsider this. The only listener that should be disabled is the
			//   join listener. The Kick listener is good; that detects if a ban occurs
			//   outside of CBan that should be monitored. 
			//  So, refactor this to not be silly. See issue #21
		}
	}
	
}
