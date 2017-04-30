package nl.zandervdm.stayput.Utils;

import nl.zandervdm.stayput.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;

public class RuleManager {

    protected Main plugin;

    public RuleManager(Main plugin) {
        this.plugin = plugin;
    }

    public boolean shouldUpdateLocation(Player player, Location fromLocation, Location toLocation){
        World fromWorld = fromLocation.getWorld();
        World toWorld = toLocation.getWorld();

        if(Main.config.getBoolean("debug")) this.plugin.getLogger().info("Player teleporting: " + player.getName());
        if(Main.config.getBoolean("debug")) this.plugin.getLogger().info("Player teleporting from world: " + fromWorld.getName());
        if(Main.config.getBoolean("debug")) this.plugin.getLogger().info("Player teleporting to world: " + toWorld.getName());

        //If the worlds are the same, ignore
        if(fromWorld.getName().equals(toWorld.getName())){
            if(Main.config.getBoolean("debug"))  this.plugin.getLogger().info("Ignoring player " + player.getName() + " because he did not switch worlds");
            return false;
        }

        //If the player does not have the use permission, just ignore it and do nothing
        if(!player.hasPermission("stayput.use")){
            if(Main.config.getBoolean("debug"))  this.plugin.getLogger().info("Ignoring player " + player.getName() + " because he does not have permission");
            return false;
        }

        //        //However if the player has the override permission, also ignore
//        if(player.hasPermission("stayput.override") && !player.hasPermission("-stayput.override")){
//            if(Main.config.getBoolean("debug")) this.plugin.getLogger().info("Ignore player " + player.getName() + " because he has the override permission");
//                return;
//        }

        return true;
    }

    public Location shouldTeleportPlayer(Player player, Location fromLocation, Location toLocation){
        World fromWorld = fromLocation.getWorld();
        World toWorld = toLocation.getWorld();

        //If this world is inside the configs blacklist, ignore
        if(this.worldIsBlacklisted(toWorld)){
            if(Main.config.getBoolean("debug")) this.plugin.getLogger().info("Not teleport player because this world is blacklisted");
            return null;
        }

        //In any other case, find the previous spot of the user in this world
        Location previousLocation = this.plugin.getPositionRepository().getPreviousLocation(player, toWorld);

        //If there is no previous location for this world, just ignore it
        if(previousLocation == null){
            if(Main.config.getBoolean("debug")) this.plugin.getLogger().info("Not teleporting player because there is no previous location found");
            return null;
        }

        return previousLocation;
    }

    protected boolean shouldTeleport(PlayerTeleportEvent.TeleportCause cause){
        return cause.name().equals(PlayerTeleportEvent.TeleportCause.COMMAND.name())
                || cause.name().equals(PlayerTeleportEvent.TeleportCause.PLUGIN.name());
    }

    public boolean worldIsBlacklisted(World world) {
        List<String> blacklistedWorlds = Main.config.getStringList("blacklisted-worlds");
        return blacklistedWorlds.size() != 0 && blacklistedWorlds.contains(world.getName());
    }
}
