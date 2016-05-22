package vg.civcraft.mc.cbanman.bungee;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.io.ByteStreams;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import vg.civcraft.mc.cbanman.ban.CBanList;
import vg.civcraft.mc.cbanman.bungee.SqlManager;
import vg.civcraft.mc.mercury.MercuryAPI;

public class CBanManBungee extends Plugin {
	private static CBanManBungee plugin;
	private Map<UUID, CBanList> bannedPlayers;
	private SqlManager sqlman;
	@SuppressWarnings("unused")
	private MercuryListener mercury;
	@SuppressWarnings("unused")
	private BungeeListener bungee;
	private Configuration config;
	
	private static boolean isNameLayer = false;
	
	@Override
	public void onEnable() {
		if (this.getProxy().getPluginManager().getPlugin("Mercury") == null){
			this.getLogger().warning("[FATALITY]CBanManagement is unable to load, Mercury not found!");
			return;
		}
		plugin = this;
		this.bannedPlayers = new HashMap<UUID, CBanList>();
		this.loadConfig();
		this.sqlman = new SqlManager(plugin);
		if (this.sqlman.load() == false)
			return;
		new BanAPI(this, this.sqlman);
		this.mercury = new MercuryListener(plugin);
		this.bungee = new BungeeListener(plugin);
		this.registerCommands();
		isNameLayer = getProxy().getPluginManager().getPlugin("NameLayer") != null;
		// This method will tell bukkit servers to disable their commands and login listeners.
		this.getProxy().getScheduler().schedule(this, new Runnable() {

			@Override
			public void run() {
				MercuryAPI.sendGlobalMessage("disable", "banman");
			}

		}, 5, TimeUnit.MINUTES);
	}

	private void registerCommands() {
		this.getProxy().getPluginManager().registerCommand(this, new BanCommand());
		this.getProxy().getPluginManager().registerCommand(this, new UnBanCommand());
		
	}

	private void loadConfig() {
		final File configFile;
		configFile = new File(this.getDataFolder(), "config.yml");
		if (!getDataFolder().exists())
			getDataFolder().mkdir();
		ConfigurationProvider configManager = ConfigurationProvider.getProvider(YamlConfiguration.class);
		if (!configFile.exists()) {
			try {
                configFile.createNewFile();
                try (InputStream is = getResourceAsStream("config.yml");
                     OutputStream os = new FileOutputStream(configFile)) {
                    ByteStreams.copy(is, os);
                    is.close();
                    os.close();
                }
            } catch (IOException e) {
                throw new RuntimeException("Unable to create configuration file", e);
            }
        }
		try {
			this.config = configManager.load(configFile);
		} catch (IOException e) {
			this.config = new Configuration();
		}
	}

	@Override
	public void onDisable() {
		this.bannedPlayers = null;
	}

	public synchronized Map<UUID, CBanList> getBannedPlayers() {
		return this.bannedPlayers;
	}

	public Configuration GetConfig() {
		return this.config;
	}
	
	public BanAPI getBanAPI(){
		return BanAPI.getInstance();
	}
	
	public static CBanManBungee getInstance(){
		return plugin;
	}
	
	public static boolean isNameLayerEnabled() {
		return isNameLayer;
	}
}
