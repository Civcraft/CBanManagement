package vg.civcraft.mc.cbanman.bungee;

import java.util.UUID;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import vg.civcraft.mc.cbanman.ban.Ban;
import vg.civcraft.mc.cbanman.ban.BanLevel;
import vg.civcraft.mc.cbanman.utils.UUIDFetcher;
import vg.civcraft.mc.namelayer.bungee.NameLayerBungee;

public final class BanCommand extends Command {

	public BanCommand() {
		super("ban");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		String name;
		UUID uuid;
		Ban ban;
		switch (args.length){
		case 0:
			sender.sendMessage(new TextComponent("You need to give a players name to ban"));
			break;
		case 1:
			name = args[0];
			try{
				uuid = CBanManBungee.isNameLayerEnabled() ? NameLayerBungee.getUUIDFromPlayerName(name) : UUIDFetcher.getUUIDOf(name);
				if (uuid == null){
					throw new Exception();
				}
			} catch (Exception e){
				sender.sendMessage(new TextComponent("Ban Failed for "+name+": Unable to fetch UUID"));
				return;
			}
			ban = new Ban(BanLevel.HIGH, "admin", "You have been banned by an Administrator");
			BanAPI.getInstance().ban(uuid, ban);
			sender.sendMessage(new TextComponent("Banned: "+name+"|"+(String)uuid.toString()));
			break;
		default:
			name = args[0];
			try{
				uuid = CBanManBungee.isNameLayerEnabled() ? NameLayerBungee.getUUIDFromPlayerName(name) : UUIDFetcher.getUUIDOf(name);
				if (uuid == null){
					throw new Exception();
				}
			} catch (Exception e){
				sender.sendMessage(new TextComponent("Ban Failed for "+name+": Unable to fetch UUID"));
				return;
			}
			String msg = "";
			for (int x =1; x<args.length; x++){
				msg+=args[x];
			}
			ban = new Ban(BanLevel.HIGH, "admin", msg);
			BanAPI.getInstance().ban(uuid, ban);
			sender.sendMessage(new TextComponent("Banned: "+name+"|"+(String)uuid.toString()));
			break;
		}

	}

}
