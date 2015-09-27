package vg.civcraft.mc.cbanman.ban;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.command.CommandSender;

import vg.civcraft.mc.cbanman.CBanManagement;
import vg.civcraft.mc.cbanman.utils.UUIDFetcher;

public class AsyncBan implements Runnable {
	private final CBanManagement plugin;
	private final List<String> names;
	private final Ban ban;
	private final CommandSender sender;
	
	public AsyncBan(CBanManagement plugin, List<String> names, Ban ban){
		this.plugin = plugin;
		this.names = names;
		this.ban = ban;
		this.sender = null;
	}
	public AsyncBan(CBanManagement plugin, List<String> names, Ban ban, CommandSender sender){
		this.plugin = plugin;
		this.names = names;
		this.ban = ban;
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
				sender.sendMessage("Failed to async ban");
			return;
		}
		SyncBan sban = new SyncBan(plugin, responce, ban, sender);
		plugin.getServer().getScheduler().runTask(plugin, sban);
	}

}
