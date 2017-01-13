package nl.zandervdm.stayput;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import nl.zandervdm.stayput.Listeners.PlayerTeleportEventListener;
import nl.zandervdm.stayput.Models.Position;
import nl.zandervdm.stayput.Utils.ConfigManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.javalite.activejdbc.Base;

import java.sql.SQLException;

public class Main extends JavaPlugin {

    public static FileConfiguration config;

    protected ConfigManager configManager;
    protected ConnectionSource connectionSource;

    protected Dao<Position, Integer> positionMapper;

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
        setupDao();
        setupTables();
    }

    @Override
    public void onDisable(){
        Base.close();
    }

    public Dao<Position, Integer> getPositionMapper(){
        return this.positionMapper;
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
        String datasource  = "jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true";
        connectionSource   = null;
        try {
            connectionSource = new JdbcConnectionSource(datasource, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(Main.config.getBoolean("debug")) getLogger().info("Setting up database");
    }

    protected void setupDao(){
        positionMapper = null;
        try {
            positionMapper = DaoManager.createDao(connectionSource, Position.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void setupTables() {
        try {
            TableUtils.createTableIfNotExists(connectionSource, Position.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(Main.config.getBoolean("debug")) getLogger().info("Setting up tables");
    }

}
