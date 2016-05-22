package vg.civcraft.mc.cbanman.bungee;

import java.util.UUID;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import vg.civcraft.mc.cbanman.ban.Ban;
import vg.civcraft.mc.cbanman.ban.BanLevel;
import vg.civcraft.mc.cbanman.ban.CBanList;
import vg.civcraft.mc.mercury.MercuryAPI;
import vg.civcraft.mc.mercury.events.EventListener;

public class MercuryListener implements EventListener {
	private CBanManBungee plugin;

	public MercuryListener(CBanManBungee plugin) {
		this.plugin = plugin;
		MercuryAPI.registerListener(this, "banman");
		MercuryAPI.registerPluginMessageChannel("banman");
	}

	@Override
	public void receiveMessage(String origin, String channel, String mess) {
		if (!channel.equalsIgnoreCase("banman"))
			return;
		String[] message = mess.split("~");
		String reason = message[0];
		UUID uuid = UUID.fromString(message[1]);
		if (reason.equals("ban")){
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
			ProxiedPlayer p = ProxyServer.getInstance().getPlayer(uuid);
			if (p != null)
				p.disconnect(new TextComponent(msg));
		} else if (reason.equals("unban")){
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
		}
	}

}
