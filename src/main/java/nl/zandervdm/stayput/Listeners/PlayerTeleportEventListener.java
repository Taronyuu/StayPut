package nl.zandervdm.stayput.Listeners;

import nl.zandervdm.stayput.Main;
import nl.zandervdm.stayput.Models.Position;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerTeleportEventListener implements Listener {

    protected Main plugin;

    public PlayerTeleportEventListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerTeleportEvent(PlayerTeleportEvent event){
        if(Main.config.getBoolean("debug")) this.plugin.getLogger().info("PlayerTeleportEvent activated");
        Player player = event.getPlayer();
        World fromWorld = event.getFrom().getWorld();
        World toWorld = event.getTo().getWorld();
        PlayerTeleportEvent.TeleportCause cause = event.getCause();

        //If the player does not have the use permission, just ignore it and do nothing
        if(!player.hasPermission("stayput.use")){
            if(Main.config.getBoolean("debug"))  this.plugin.getLogger().info("Ignoring player " + player.getName() + " because he does not have permission");
                return;
        }

        //However if the player has the override permission, also ignore
        if(player.hasPermission("stayput.override")){
            if(Main.config.getBoolean("debug")) this.plugin.getLogger().info("Ignore player " + player.getName() + " because he has the override permission");
                return;
        }

        //We should always update the previous location for the previous world for this player because at this point
        //he left the previous world
        this.updateLocationForPlayer(player, event.getFrom());

        //Only teleport the player if he is using a command or if it is a plugin
        if(!this.shouldTeleport(cause)){
            if(Main.config.getBoolean("debug")) this.plugin.getLogger().info("Not teleporting player because he is teleported by something else than a plugin or command");
            return;
        }

        //In any other case, find the previous spot of the user in this world
        Location previousLocation = this.getPreviousLocation(player, toWorld);

        //If there is no previous location for this world, just ignore it
        if(previousLocation == null){
            if(Main.config.getBoolean("debug")) this.plugin.getLogger().info("Not teleporting player because there is no previous location found");
            return;
        }

        //There is a location, and the player should teleport, so teleport him
        if(Main.config.getBoolean("debug")) this.plugin.getLogger().info("Teleporting player to his previous location");
        player.teleport(previousLocation);
    }

    protected boolean shouldTeleport(PlayerTeleportEvent.TeleportCause cause){
        return cause.name().equals(PlayerTeleportEvent.TeleportCause.COMMAND.name())
                || cause.name().equals(PlayerTeleportEvent.TeleportCause.PLUGIN.name());
    }

    protected void updateLocationForPlayer(Player player, Location location){
        Position position = Position.findFirst("uuid = ? AND world_name = ?", player.getUniqueId().toString(), location.getWorld().getName());
        if(position == null) {
            position = new Position();
        }
        position.set("player_name", player.getName());
        position.set("uuid", player.getUniqueId().toString());
        position.set("coordinate_x", location.getBlockX());
        position.set("coordinate_y", location.getBlockY());
        position.set("coordinate_z", location.getBlockZ());
        position.set("yaw", location.getYaw());
        position.set("pitch", location.getPitch());
        position.saveIt();
    }

    protected Location getPreviousLocation(Player player, World world){
        Position position = Position.findFirst("uuid = ? AND world_name = ?", player.getUniqueId().toString(), world.getName());
        if(position == null) return null;
        double coordX = position.getDouble("coordinate_x");
        double coordY = position.getDouble("coordinate_y");
        double coordZ = position.getDouble("coordinate_z");
        float yaw = position.getFloat("yaw");
        float pitch = position.getFloat("pitch");

        Location location = new Location(world, coordX, coordY, coordZ, yaw, pitch);
        return location;
    }

}
