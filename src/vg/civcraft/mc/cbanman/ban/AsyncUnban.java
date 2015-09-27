package vg.civcraft.mc.cbanman.ban;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.command.CommandSender;

import vg.civcraft.mc.cbanman.CBanManagement;
import vg.civcraft.mc.cbanman.utils.UUIDFetcher;

public class AsyncUnban implements Runnable {
	private final CBanManagement plugin;
	private final List<String> names;
	private final CommandSender sender;
	
	public AsyncUnban(CBanManagement plugin, List<String> names){
		this.plugin = plugin;
		this.names = names;
		this.sender = null;
	}
	public AsyncUnban(CBanManagement plugin, List<String> names, CommandSender sender){
		this.plugin = plugin;
		this.names = names;
		this.sender = sender;
	}
	
	@Override
	public void run() {
		Map<String, UUID> responce = null;
		UUIDFetcher fetcher = new UUIDFetcher(names);
		try{
			responce = fetcher.call();
		} catch (Exception e){
			responce = null;
		}
		if (responce == null){
			if (sender !=null)
				sender.sendMessage("Failed to async unban");
			return;
		}
		SyncUnban suban = new SyncUnban(plugin, responce, sender);
		plugin.getServer().getScheduler().runTask(plugin, suban);
	}

}
