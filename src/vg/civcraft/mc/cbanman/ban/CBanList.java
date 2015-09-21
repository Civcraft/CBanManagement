package vg.civcraft.mc.cbanman.ban;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;

public class CBanList {
	private List<Ban> banlist;
	
	public CBanList(){
		banlist = new ArrayList<Ban>();
	}
	
	public List<Ban> getBanList(){
		return banlist;
	}
	
	public void addBan(Ban ban){
		Ban rem = null;
		for (Ban oldban : banlist){
			if (oldban.getPluginName().equals(ban.getPluginName())){
				rem = oldban;
				break;
			}
		}
		if (rem != null){
			banlist.remove(rem);
		}
		banlist.add(ban);
	}
	
	public void removeBan(String pluginname){
		Ban rem = null;
		for (Ban ban : banlist){
			if (ban.getPluginName().equalsIgnoreCase(pluginname)){
				rem = ban;
				break;
			}
		}
		if (rem != null){
			removeBan(rem);
			
		}
	}
	
	public void removeBan(Ban ban){
		banlist.remove(ban);
	}
	
	public int getSize(){
		return banlist.size();
	}
	
	public Ban getHighestLevelBan(){
		Ban ret = null;
		for (Ban ban : banlist){
			Bukkit.getLogger().info(ban.getMessage());
			if (ret == null){
				ret = ban;
			} else if (ban.getBanLevel().value() > ret.getBanLevel().value()){
				ret = ban;
			}
		}
		return ret;
	}
	
}
