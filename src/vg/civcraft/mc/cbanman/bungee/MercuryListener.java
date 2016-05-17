package vg.civcraft.mc.cbanman.bungee;

import vg.civcraft.mc.mercury.MercuryAPI;
import vg.civcraft.mc.mercury.events.EventListener;

public class MercuryListener implements EventListener {
	private CBanManBungee plugin;

	public MercuryListener(CBanManBungee plugin) {
		this.plugin = plugin;
		MercuryAPI.registerListener(this, "banman");
		MercuryAPI.registerPluginMessageChannel("banman");
		if (plugin.GetConfig().getBoolean("bungee.master",true)){
			MercuryAPI.registerPluginMessageChannel("banmaster");
		}
	}

	@Override
	public void receiveMessage(String origin, String channel, String msg) {
		if (!channel.equalsIgnoreCase("banman") || !channel.equalsIgnoreCase("banmaster"))
			return;
		//TODO: React to mercury messages
	}

}
