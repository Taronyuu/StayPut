package nl.zandervdm.stayput;

import nl.zandervdm.stayput.Listeners.PlayerTeleportEventListener;
import nl.zandervdm.stayput.Utils.ConfigManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.javalite.activejdbc.Base;

import java.sql.SQLException;

public class Main extends JavaPlugin {

    public static FileConfiguration config;

    protected ConfigManager configManager;

    /**
     * Permissions:
     * stayput.use
     * stayput.override
     */

    @Override
    public void onEnable(){
        setupClasses();
        setupConfig();
        setupListeners();
        setupDatabase();
        setupTables();
    }

    @Override
    public void onDisable(){
        Base.close();
    }

    protected void setupClasses(){
        this.configManager = new ConfigManager(this);
    }

    protected void setupConfig(){
        this.configManager.createConfig();
        config = getConfig();
        if(Main.config.getBoolean("debug")) getLogger().info("Setting up config");
    }

    protected void setupListeners(){
        getServer().getPluginManager().registerEvents(new PlayerTeleportEventListener(this), this);
        if(Main.config.getBoolean("debug")) getLogger().info("Setting up listeners");
    }

    protected void setupDatabase(){
        String host        = Main.config.getString("mysql.host");
        Integer port       = Main.config.getInt("mysql.port");
        String database    = Main.config.getString("mysql.database");
        String username    = Main.config.getString("mysql.username");
        String password    = Main.config.getString("mysql.password");
        String datasource  = "jdbc:mysql://" + host + ":" + port + "/" + database;
        Base.open("com.mysql.jdbc.Driver", datasource, username, password);
        if(Main.config.getBoolean("debug")) getLogger().info("Setting up database");
    }

    protected void setupTables() {
        String createPositionsTable = "CREATE TABLE IF NOT EXISTS `positions` (" +
                "  `id` int(11) unsigned NOT NULL AUTO_INCREMENT," +
                "  `player_name` varchar(255) DEFAULT NULL," +
                "  `uuid` varchar(255) DEFAULT NULL," +
                "  `world_name` varchar(255) DEFAULT NULL," +
                "  `coordinate_x` double DEFAULT NULL," +
                "  `coordinate_y` double DEFAULT NULL," +
                "  `coordinate_z` double DEFAULT NULL," +
                "  `yaw` float DEFAULT NULL," +
                "  `pitch` float DEFAULT NULL," +
                "  PRIMARY KEY (`id`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
        try {
            Base.connection().nativeSQL(createPositionsTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(Main.config.getBoolean("debug")) getLogger().info("Setting up tables");
    }

}
