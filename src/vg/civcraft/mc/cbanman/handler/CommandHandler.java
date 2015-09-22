package vg.civcraft.mc.cbanman.handler;

import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import vg.civcraft.mc.cbanman.CBanManagement;
import vg.civcraft.mc.cbanman.ban.Ban;
import vg.civcraft.mc.cbanman.ban.BanLevel;
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
				return handleCkban(sender, args);
		}
		return false;
	}

	private boolean handleCkban(CommandSender sender, String[] args) {
		if (args.length != 1){ return false;}
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
				sender.sendMessage("----------------------------");
				sender.sendMessage("'"+args[0]+"' is banned:");
				for (Ban ban : plugin.getBannedPlayers().get(uuid).getList()){
					sender.sendMessage(
							"Lv:"+ban.getBanLevel().toString()+" | Plugin: "+ban.getPluginName()
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
		
		for (String name : args){
			if (plugin.unbanPlayerAll(name)){
				sender.sendMessage("Unbanned: "+name);
			} else {
				sender.sendMessage("Unable to unban: "+name);
			}

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
		for (String name : args){
			if (plugin.banPlayer(name, ban)){
				sender.sendMessage("Banned: "+name);
			} else {
				sender.sendMessage("Unable to ban: "+name);
			}
		}
		
		return true;
	}

}
