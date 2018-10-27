package nl.zandervdm.stayput;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import nl.zandervdm.stayput.Commands.StayputCommand;
import nl.zandervdm.stayput.Listeners.PlayerTeleportEventListener;
import nl.zandervdm.stayput.Models.Position;
import nl.zandervdm.stayput.Repositories.PositionRepository;
import nl.zandervdm.stayput.Utils.ConfigManager;
import nl.zandervdm.stayput.Utils.DimensionManager;
import nl.zandervdm.stayput.Utils.RuleManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;
import java.util.Collection;

public class StayPut extends JavaPlugin {

    public static FileConfiguration config;

    //Util classes
    private ConfigManager configManager;
    private DimensionManager dimensionManager;
    private RuleManager ruleManager;

    //Database connection stuff
    private ConnectionSource connectionSource;

    //Data mappers
    private Dao<Position, Integer> positionMapper;

    //Repositories
    private PositionRepository positionRepository;

    /**
     * Permissions:
     * stayput.use
     * stayput.override
     * stayput.admin
     */

    @Override
    public void onEnable(){
        setupClasses();
        setupConfig();
        setupListeners();
        setupCommands();
        setupDatabase();
        setupDao();
        setupTables();
        setupDimensions();
    }

    @Override
    public void onDisable(){
        syncPlayersToDatabase();
    }

    public ConnectionSource getConnectionSource() {
        return this.connectionSource;
    }

    public Dao<Position, Integer> getPositionMapper(){
        return this.positionMapper;
    }

    public PositionRepository getPositionRepository(){
        return this.positionRepository;
    }

    public DimensionManager getDimensionManager() { return this.dimensionManager; }

    public RuleManager getRuleManager(){
        return this.ruleManager;
    }

    public MultiverseCore getMultiverseCore() {
        Plugin plugin = getServer().getPluginManager().getPlugin("Multiverse-Core");

        if (plugin instanceof MultiverseCore) {
            return (MultiverseCore) plugin;
        }

        // Get the name of all plugins.
        Plugin[] pluginList =  getServer().getPluginManager().getPlugins();
        for(Plugin plug : pluginList) {
            getLogger().info("Plugin: " + plug.getName() );
        }

        throw new RuntimeException("MultiVerse not found!");
    }

    private void setupClasses(){
        this.configManager = new ConfigManager(this);
        this.dimensionManager = new DimensionManager(this);
        this.ruleManager = new RuleManager(this);

        this.positionRepository = new PositionRepository(this);

        if(getMultiverseCore().getMVWorldManager().getMVWorlds().isEmpty()) {
            getLogger().info("No worlds found !!!!!!!!! fuck");
        }
    }

    public void setupConfig(){
        this.configManager.createConfig();
        config = getConfig();
        if(StayPut.config.getBoolean("debug")) getLogger().info("Setting up config");
        if(config.getBoolean("rebuild-db")) {
            rebuildTables();
            config.set("rebuild-db", "false");
        }
    }

    private void setupDimensions() {
        this.dimensionManager.loadDimensions();
        this.positionRepository.updateDimensionOfPositions(this.dimensionManager.getDimensions());
        if(StayPut.config.getBoolean("debug")) getLogger().info("Setting up dimensions");
    }

    private void setupListeners(){
        getServer().getPluginManager().registerEvents(new PlayerTeleportEventListener(this), this);
        if(StayPut.config.getBoolean("debug")) getLogger().info("Setting up listeners");
    }

    private void setupCommands(){
       this.getCommand("stayput").setExecutor(new StayputCommand(this));
    }

    private void setupDatabase(){
        if(StayPut.config.getString("type").equals("sqlite")){
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            File file = new File(this.getDataFolder(), "database.db");
            String data_source = "jdbc:sqlite:" + file;
            connectionSource = null;
            try {
                connectionSource = new JdbcConnectionSource(data_source);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }else if(StayPut.config.getString("type").equals("mysql")) {
            String host = StayPut.config.getString("mysql.host");
            Integer port = StayPut.config.getInt("mysql.port");
            String database = StayPut.config.getString("mysql.database");
            String username = StayPut.config.getString("mysql.username");
            String password = StayPut.config.getString("mysql.password");
            String data_source = "jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true";
            connectionSource = null;
            try {
                connectionSource = new JdbcConnectionSource(data_source, username, password);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }else{
            getLogger().warning("Invalid database connection type chosen!");
        }
        if(StayPut.config.getBoolean("debug")) getLogger().info("Setting up database");
    }

    private void setupDao(){
        this.positionMapper = null;
        try {
            this.positionMapper = DaoManager.createDao(connectionSource, Position.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupTables() {
        try {
            TableUtils.createTableIfNotExists(connectionSource, Position.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(StayPut.config.getBoolean("debug")) getLogger().info("Setting up tables");
    }

    public void rebuildTables() {
        try {
            TableUtils.dropTable(connectionSource, Position.class, false);
            setupTables();
            if(StayPut.config.getBoolean("debug")) getLogger().info("Rebuilding Stayput tables");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void syncPlayersToDatabase(){
        Collection<? extends Player> players = getServer().getOnlinePlayers();
        for(Player player : players){
            if(player.hasPermission("stayput.use")) {
                this.getPositionRepository().updateLocationForPlayer(player, player.getLocation());
            }
        }
    }

}
