package vg.civcraft.mc.cbanman.ban;

import java.util.Map;
import java.util.UUID;

import org.bukkit.BanList;
import org.bukkit.BanList.Type;
import org.bukkit.command.CommandSender;

import vg.civcraft.mc.cbanman.CBanManagement;

public class SyncBan implements Runnable {
	private final CBanManagement plugin;
	private final Map<String, UUID> names;
	private final Ban ban;
	private final CommandSender sender;
	
	public SyncBan(CBanManagement plugin, Map<String, UUID> names, Ban ban, CommandSender sender){
		this.plugin = plugin;
		this.names = names;
		this.ban = ban;
		this.sender = sender;
	}
	
	@Override
	public void run() {
		BanList banlist = plugin.getServer().getBanList(Type.NAME);
		for (String name : names.keySet()){
			UUID uuid = names.get(name);
			plugin.banPlayer(uuid, ban);
			if (plugin.isBanned(uuid)){
				if (banlist.isBanned(name)){
					banlist.pardon(name);
				}
				sender.sendMessage("Banned: "+name);
			}
		}
	}

}
