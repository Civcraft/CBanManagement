package vg.civcraft.mc.cbanman.ban;

import java.util.Map;
import java.util.UUID;

import org.bukkit.command.CommandSender;

import vg.civcraft.mc.cbanman.CBanManagement;

public class SyncUnban implements Runnable {
	private final CBanManagement plugin;
	private final Map<String, UUID> names;
	private final CommandSender sender;
	
	public SyncUnban(CBanManagement plugin, Map<String, UUID> names, CommandSender sender){
		this.plugin = plugin;
		this.names = names;
		this.sender = sender;
	}
	
	@Override
	public void run() {
		for (String name : names.keySet()){
			UUID uuid = names.get(name);
			plugin.unbanPlayerAll(uuid);
			if (!plugin.isBanned(uuid)){
				if (sender != null)
					sender.sendMessage("Unbanned: "+name);
			}
		}
	}

}
