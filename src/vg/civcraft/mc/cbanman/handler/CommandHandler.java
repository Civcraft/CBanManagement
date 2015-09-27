package vg.civcraft.mc.cbanman.handler;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import vg.civcraft.mc.cbanman.CBanManagement;
import vg.civcraft.mc.cbanman.ban.AsyncBan;
import vg.civcraft.mc.cbanman.ban.AsyncUnban;
import vg.civcraft.mc.cbanman.ban.Ban;
import vg.civcraft.mc.cbanman.ban.BanLevel;
import vg.civcraft.mc.cbanman.ban.CBanList;
import vg.civcraft.mc.civmodcore.annotations.CivConfig;
import vg.civcraft.mc.civmodcore.annotations.CivConfigType;
import vg.civcraft.mc.civmodcore.annotations.CivConfigs;
import vg.civcraft.mc.namelayer.NameAPI;

public class CommandHandler implements CommandExecutor {
	private CBanManagement plugin;
	private boolean isNameLayer = false;
	
	public CommandHandler(CBanManagement plugin, boolean nl) {
		this.plugin = plugin;
		isNameLayer = nl;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if (!sender.hasPermission("CBanMan.*")){return false;}
		switch (label.toLowerCase()){
			case "ban":
				return handleBan(sender, args);
			case "unban":
			case "uban":
			case "pardon":
				return handleUnban(sender, args);
			case "checkban":
			case "chkban":
			case "cban":
				return handleCheckban(sender, args);
			case "banrecache":
				return handleRecache(sender, args);
		}
		return false;
	}

	private boolean handleRecache(CommandSender sender, String[] args) {
		if (args.length != 0){return false;}
		if (plugin.reload()){
			sender.sendMessage("Bans Reloaded from DB!");
		} else{
			sender.sendMessage("Failed to reload bans from DB :(.");
		}
		return true;
	}

	private boolean handleCheckban(CommandSender sender, String[] args) {
		if (args.length < 1 || args.length > 2){ return false;}
		UUID uuid = null;
		if (isNameLayer){
			uuid = NameAPI.getUUID(args[0]);
		} else{
			@SuppressWarnings("deprecation")
			OfflinePlayer p = plugin.getServer().getOfflinePlayer(args[0]);
			if (p != null)
				uuid = p.getUniqueId();
		}
		
		if (uuid != null){
			if (plugin.isBanned(uuid)){
				int selected = -1, count = 0;
				if (args.length == 2){
					try{
						selected = Integer.valueOf(args[1].substring(0, 1));
					} catch (NumberFormatException e){
						selected = -1;
					}
				}
				sender.sendMessage("----------------------------");
				sender.sendMessage("'"+args[0]+"' is banned:");
				CBanList banlist = plugin.getBannedPlayers().get(uuid);
				if (selected != -1  && selected > banlist.getSize()){selected = -1;}
				for (Ban ban : banlist.getList()){
					count++;
					if (selected != -1){
						if (count != selected){continue;}
						sender.sendMessage("Lv:"+ban.getBanLevel().toString()+" | Plugin: "+ban.getPluginName());
						sender.sendMessage(ban.getMessage());
						break;
					}
					sender.sendMessage(
							"<"+count+"> Lv:"+ban.getBanLevel().toString()+" | Plugin: "+ban.getPluginName()
							);
				}
				sender.sendMessage("----------------------------");
			} else {
				sender.sendMessage("'"+args[0]+"' is not banned.");
			}
		} else {
			sender.sendMessage("'"+args[0]+"' is an unknown player.");
		}
		
		return true;
	}

	private boolean handleUnban(CommandSender sender, String[] args) {
		if (args.length == 0){return false;}
		ArrayList<String> namelist = new ArrayList<String>();
		for (String name : args){
			if (plugin.unbanPlayerAll(name)){
				sender.sendMessage("Unbanned: "+name);
			} else {
				sender.sendMessage("Attempting to AsyncUnban: "+name);
				namelist.add(name);
			}
		}
		if (!namelist.isEmpty()){
			AsyncUnban aban = new AsyncUnban(plugin, namelist, sender);
			plugin.getServer().getScheduler().runTaskAsynchronously(plugin, aban);
		}
		return true;
	}
	
	@CivConfigs({
		@CivConfig(name = "adminban.pluginname", def = "admin", type = CivConfigType.String),
		@CivConfig(name = "adminban.banmessage", def = "You have been banned by an administrator", type = CivConfigType.String),
		@CivConfig(name = "adminban.banlevel", def = "3", type = CivConfigType.Int),
	})
	private boolean handleBan(CommandSender sender, String[] args) {
		if (args.length == 0){return false;}
		
		Ban ban = new Ban(
				BanLevel.HIGH.fromInt(plugin.GetConfig().get("adminban.banlevel").getInt()),
				plugin.GetConfig().get("adminban.pluginname").getString(),
				plugin.GetConfig().get("adminban.banmessage").getString()				
				);
		ArrayList<String> namelist = new ArrayList<String>();
		for (String name : args){
			if (plugin.banPlayer(name, ban)){
				sender.sendMessage("Banned: "+name);
			} else {
				sender.sendMessage("Attempting AsyncBan for: "+name);
				namelist.add(name);
			}
		}
		if (!namelist.isEmpty()){
			AsyncBan aban = new AsyncBan(plugin, namelist, ban, sender);
			plugin.getServer().getScheduler().runTaskAsynchronously(plugin, aban);
		}
		
		return true;
	}

}
