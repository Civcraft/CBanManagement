package vg.civcraft.mc.cbanman.ban;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Ban {
	private final BanLevel level;
	private final String pluginname;
	private final String message;
	private Date expiry;
	
	public Ban(BanLevel lv, String plugin, String msg){
		if (lv == null)
			lv = BanLevel.LOW;
		if (plugin == null){
			plugin = "none";
		} else if (plugin.trim().isEmpty()){
			plugin = "none";
		}
		if (msg == null){
			msg = "You are banned from this server.";
		} else if (msg.trim().isEmpty()){
			msg = "You are banned from this server.";
		}
		this.level = lv;
		this.pluginname = plugin.toLowerCase();
		this.message = msg;
		this.expiry = null;
	}
	
	@SuppressWarnings("deprecation")
	public Ban(BanLevel lv, String plugin, String msg, Date expiry){
		if (lv == null)
			lv = BanLevel.LOW;
		if (plugin == null){
			plugin = "none";
		} else if (plugin.isEmpty()){
			plugin = "none";
		}
		if (msg == null){
			msg = "You are banned from this server.";
		} else if (msg.isEmpty()){
			msg = "You are banned from this server.";
		}
		this.level = lv;
		this.pluginname = plugin.toLowerCase();
		this.message = msg;
		this.expiry = expiry;
		if (expiry != null){expiry.setSeconds(0);}
	}
	
	
	public BanLevel getBanLevel(){
		return this.level;
	}
	public String getPluginName(){
		return this.pluginname;
	}
	public String getMessage(){
		return this.message;
	}
	
	public String getExpiryString(){
		if (expiry != null){
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return dateFormat.format(expiry);
		} else {
			return null;
		}
	}
	
	public boolean isExpired(){
		if (expiry == null){return false;}
		Date now = new Date();
		int compare = expiry.compareTo(now);
		if (compare < 0){
			return true;
		} else if (compare > 0 ){
			return false;
		} else{
			return false;
		}
	}
	
	public boolean isPluginName(String name){
		if (name == null){return false;}
		return this.pluginname.equalsIgnoreCase(name);
	}

}
