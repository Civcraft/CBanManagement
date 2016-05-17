package vg.civcraft.mc.cbanman.bungee;

import java.util.UUID;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import vg.civcraft.mc.cbanman.utils.UUIDFetcher;

public final class UnBanCommand extends Command {

	public UnBanCommand() {
		super("unban");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		String name;
		UUID uuid;
		switch (args.length){
		case 0:
			sender.sendMessage(new TextComponent("You need to give a name"));
			break;
		case 1:
			name = args[0];
			try {
				uuid = UUIDFetcher.getUUIDOf(name);
			} catch (Exception e) {
				sender.sendMessage(new TextComponent("Player '"+name+"' does not exist"));
				return;
			}
			if (BanAPI.getInstance().isBanned(uuid) == true){
				BanAPI.getInstance().unban(uuid);
				sender.sendMessage(new TextComponent("Player '"+name+"' has been unbanned"));
			} else {
				sender.sendMessage(new TextComponent("Player '"+name+"' is not banned"));
			}
			break;
		default:
			sender.sendMessage(new TextComponent("You can only give one name at a time"));
			break;
		}
	}

}
