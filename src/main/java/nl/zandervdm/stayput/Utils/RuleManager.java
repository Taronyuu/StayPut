package nl.zandervdm.stayput.Utils;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import nl.zandervdm.stayput.StayPut;
import nl.zandervdm.stayput.Utils.DimensionManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;

public class RuleManager {

    private StayPut plugin;
    private MVWorldManager worldManager;

    public RuleManager(StayPut plugin) {
        this.plugin = plugin;
    }

    public boolean shouldUpdateLocation(Player player, Location fromLocation, Location toLocation){
        World fromWorld = fromLocation.getWorld();
        World toWorld = toLocation.getWorld();

        if(StayPut.config.getBoolean("debug")) this.plugin.getLogger().info("Player teleporting: " + player.getName());
        if(StayPut.config.getBoolean("debug")) this.plugin.getLogger().info("Player teleporting from world: " + fromWorld.getName());
        if(StayPut.config.getBoolean("debug")) this.plugin.getLogger().info("Player teleporting to world: " + toWorld.getName());

        //If the worlds are the same, ignore
        if(fromWorld.getName().equals(toWorld.getName())){
            if(StayPut.config.getBoolean("debug"))  this.plugin.getLogger().info("Ignoring player " + player.getName() + " because he did not switch worlds");
            return false;
        }

        //If the player does not have the use permission, just ignore it and do nothing
        if(!player.hasPermission("stayput.use")){
            if(StayPut.config.getBoolean("debug"))  this.plugin.getLogger().info("Ignoring player " + player.getName() + " because he does not have permission");
            return false;
        }

        return true;
    }

    public Location shouldTeleportPlayer(Player player, Location toLocation){
        World toWorld = toLocation.getWorld();

        //If this world is inside the configs blacklist, ignore
        if(this.worldIsBlackListed(toWorld)){
            if(StayPut.config.getBoolean("debug")) this.plugin.getLogger().info("Not StayPut this player because this world is blacklisted");
            return null;
        }

        if(!this.worldIsWhiteListed(toWorld)) {
            if(StayPut.config.getBoolean("debug")) this.plugin.getLogger().info("Not StayPut this player because this world is not whitelisted");
            return null;
        }

        //In any other case, find the previous spot of the user in this world
        Location previousLocation = this.plugin.getPositionRepository().getPreviousLocation(player, toWorld);

        //If there is no previous location for this world, just ignore it
        if(previousLocation == null){
            if(StayPut.config.getBoolean("debug")) this.plugin.getLogger().info("Not teleporting player because there is no previous location found");
            return null;
        }

        return previousLocation;
    }

    private boolean worldIsBlackListed(World world) {
        List<String> blacklistedWorlds = StayPut.config.getStringList("blacklisted-worlds");
        return blacklistedWorlds.size() != 0 && blacklistedWorlds.contains(world.getName());
    }

    //
    private boolean worldIsWhiteListed(World world) {
        List<String> white_listed_worlds = this.plugin.getConfig().getStringList("whitelisted-worlds");
        return white_listed_worlds.size() == 0 || white_listed_worlds.contains(world.getName());
    }
}
