package vg.civcraft.mc.cbanman.ban;


public class Ban {
	private final BanLevel level;
	private final String pluginname;
	private final String message;
	
	public Ban(BanLevel lv, String plugin, String msg){
		if (lv == null)
			lv = BanLevel.MEDIUM;
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
		this.pluginname = plugin;
		this.message = msg;
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

}
