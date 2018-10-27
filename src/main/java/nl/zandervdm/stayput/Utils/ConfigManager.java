package nl.zandervdm.stayput.Utils;

import nl.zandervdm.stayput.StayPut;

import java.io.File;

public class ConfigManager {

    private StayPut plugin;

    public ConfigManager(StayPut plugin) {
        this.plugin = plugin;
    }

    public void createConfig(){
        if(!this.plugin.getDataFolder().exists()){
            this.plugin.getDataFolder().mkdirs();
        }

        File file = new File(this.plugin.getDataFolder(), "config.yml");
        if(!file.exists()){
            this.plugin.getLogger().info("Config.yml not found, creating!");
            this.plugin.saveDefaultConfig();
        }
    }

}
