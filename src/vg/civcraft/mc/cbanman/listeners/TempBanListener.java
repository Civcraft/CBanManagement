package vg.civcraft.mc.cbanman.listeners;

import java.util.Map;
import java.util.UUID;

import vg.civcraft.mc.cbanman.CBanManagement;
import vg.civcraft.mc.cbanman.ban.Ban;
import vg.civcraft.mc.cbanman.ban.CBanList;

public class TempBanListener implements Runnable{
	private CBanManagement plugin;
	
	public TempBanListener(CBanManagement plugin){
		this.plugin = plugin;
	}

	public void run(){
		Map<UUID, CBanList> banned = plugin.getBannedPlayers();
		for (final UUID uuid : banned.keySet()){
			CBanList banlist = banned.get(uuid);
			for (final Ban ban : banlist.getList()){
				if (ban.isExpired()){
					plugin.getServer().getScheduler().runTask(plugin, new Runnable(){
						@Override
						public void run(){
							plugin.getLogger().info("Ban Expired for: "+uuid.toString());
							plugin.unbanPlayer(uuid, ban.getPluginName());
						}
					});
				}
			}
		}
	}

	
	

}
