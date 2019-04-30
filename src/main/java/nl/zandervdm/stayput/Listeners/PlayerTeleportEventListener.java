package nl.zandervdm.stayput.Listeners;

import com.onarandombox.MultiverseCore.event.MVTeleportEvent;
import com.onarandombox.MultiversePortals.event.MVPortalEvent;
import nl.zandervdm.stayput.StayPut;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Arrays;

public class PlayerTeleportEventListener implements Listener {

    private StayPut plugin;

    public PlayerTeleportEventListener(StayPut plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerTeleportEvent(MVTeleportEvent event){
        if(StayPut.config.getBoolean("debug")) this.plugin.getLogger().info(ChatColor.GREEN + "PlayerTeleportEvent activated");
        if(StayPut.config.getBoolean("debug")) this.plugin.getLogger().info(event.toString());
        if(StayPut.config.getBoolean("skipTeleportEvent")) return;
        Player player = event.getTeleportee();

        if(!this.plugin.getRuleManager().shouldUpdateLocation(player, event.getFrom(), event.getDestination().getLocation(player))){
            return;
        }

        //We should always update the previous location for the previous world for this player because at this point
        //he left the previous world
        this.plugin.getPositionRepository().updateLocationForPlayer(player, event.getFrom());

        Location previousLocation = this.plugin.getRuleManager().shouldTeleportPlayer(player, event.getDestination().getLocation(player));

        if(previousLocation != null) {
            if(this.isPressurePlate(previousLocation)){
                // Find a valid spot around the location
                Location newLocation = this.findAvailableLocation(previousLocation);
                if(newLocation != null) previousLocation = newLocation;
            }

            //There is a location, and the player should teleport, so teleport him
            if (StayPut.config.getBoolean("debug"))
                this.plugin.getLogger().info("Teleporting player to his previous location");
            event.setCancelled(true);
            player.teleport(previousLocation);
        }
    }

    @EventHandler
    public void onPlayerPortalEvent(MVPortalEvent event) {
        if(StayPut.config.getBoolean("debug")) this.plugin.getLogger().info(ChatColor.GREEN + "PlayerPortalEvent activated");
        if(StayPut.config.getBoolean("debug")) this.plugin.getLogger().info(event.toString());
        if(StayPut.config.getBoolean("skipPortalEvent")) return;
        Player player = event.getTeleportee();

        if(!this.plugin.getRuleManager().shouldUpdateLocation(player, event.getFrom(), event.getDestination().getLocation(player))){
            return;
        }

        //We should always update the previous location for the previous world for this player because at this point
        //he left the previous world
        this.plugin.getPositionRepository().updateLocationForPlayer(player, event.getFrom());

        Location previousLocation = this.plugin.getRuleManager().shouldTeleportPlayer(player, event.getDestination().getLocation(player));

        if(previousLocation != null) {
            if(this.isPressurePlate(previousLocation)){
                // Find a valid spot around the location
                Location newLocation = this.findAvailableLocation(previousLocation);
                if(newLocation != null) previousLocation = newLocation;
            }

            //There is a location, and the player should teleport, so teleport him
            if (StayPut.config.getBoolean("debug"))
                this.plugin.getLogger().info("Teleporting player to his previous location");
            event.setCancelled(true);
            player.teleport(previousLocation);
        }
    }

    private boolean isPressurePlate(Location toLocation) {
        Location blockBelow = new Location(toLocation.getWorld(), toLocation.getX(), toLocation.getY()-1, toLocation.getZ());
        Material[] pressurePlates = {Material.ACACIA_PRESSURE_PLATE, Material.BIRCH_PRESSURE_PLATE, Material.DARK_OAK_PRESSURE_PLATE, Material.JUNGLE_PRESSURE_PLATE,
            Material.OAK_PRESSURE_PLATE, Material.SPRUCE_PRESSURE_PLATE, Material.STONE_PRESSURE_PLATE, Material.HEAVY_WEIGHTED_PRESSURE_PLATE, Material.LIGHT_WEIGHTED_PRESSURE_PLATE};

        if(Arrays.stream(pressurePlates).anyMatch(x -> x.equals(blockBelow.getBlock().getType()))) return true;
        if(Arrays.stream(pressurePlates).anyMatch(x -> x.equals(toLocation.getBlock().getRelative(BlockFace.DOWN).getType()))) return true;

        return false;
    }

    private Location findAvailableLocation(Location location) {
        //The current given location isn't valid (most likely it is a pressure plate)
        //Get the location in front, back, left and right of it and check if it is air or water.
        //Also check the block above it to make sure the block can't suffocate.
        Location location1Down = new Location(location.getWorld(), location.getX()-1, location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        Location location2Down = new Location(location.getWorld(), location.getX()+1, location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        Location location3Down = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ()-1, location.getYaw(), location.getPitch());
        Location location4Down = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ()+1, location.getYaw(), location.getPitch());
        Material block1Down = location1Down.getBlock().getType();
        Material block2Down = location2Down.getBlock().getType();
        Material block3Down = location3Down.getBlock().getType();
        Material block4Down = location4Down.getBlock().getType();

        if(block1Down.equals(Material.AIR) || block1Down.equals(Material.WATER)){
            Location location1Up = new Location(location.getWorld(), location.getX()-1, location.getY()+1, location.getZ(), location.getYaw(), location.getPitch());
            Material block1Up = location1Up.getBlock().getType();
            if(block1Up.equals(Material.AIR) || block1Up.equals(Material.WATER)) return location1Down;
        }else if(block2Down.equals(Material.AIR) || block2Down.equals(Material.WATER)){
            Location location2Up = new Location(location.getWorld(), location.getX()+1, location.getY()+1, location.getZ(), location.getYaw(), location.getPitch());
            Material block2Up = location2Up.getBlock().getType();
            if(block2Up.equals(Material.AIR) || block2Up.equals(Material.WATER)) return location2Down;
        }else if(block3Down.equals(Material.AIR) || block3Down.equals(Material.WATER)){
            Location location3Up = new Location(location.getWorld(), location.getX(), location.getY()+1, location.getZ()-1, location.getYaw(), location.getPitch());
            Material block3Up = location3Up.getBlock().getType();
            if(block3Up.equals(Material.AIR) || block3Up.equals(Material.WATER)) return location3Down;
        }else if(block4Down.equals(Material.AIR) || block4Down.equals(Material.WATER)){
            Location location4Up = new Location(location.getWorld(), location.getX(), location.getY()+1, location.getZ()+1, location.getYaw(), location.getPitch());
            Material block4Up = location4Up.getBlock().getType();
            if(block4Up.equals(Material.AIR) || block4Up.equals(Material.WATER)) return location4Down;
        }
        return null;
    }
}
