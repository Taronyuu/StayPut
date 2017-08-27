package nl.zandervdm.stayput.Listeners;

import com.onarandombox.MultiverseCore.event.MVTeleportEvent;
import javafx.geometry.Pos;
import nl.zandervdm.stayput.Main;
import nl.zandervdm.stayput.Models.Position;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.sql.SQLException;
import java.util.List;

public class PlayerTeleportEventListener implements Listener {

    protected Main plugin;

    public PlayerTeleportEventListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerTeleportEvent(MVTeleportEvent event){
        if(Main.config.getBoolean("debug")) this.plugin.getLogger().info("PlayerTeleportEvent activated");
        Player player = event.getTeleportee();
        World fromWorld = event.getFrom().getWorld();
        World toWorld = event.getDestination().getLocation(player).getWorld();

        if(!this.plugin.getRuleManager().shouldUpdateLocation(player, event.getFrom(), event.getDestination().getLocation(player))){
            return;
        }

        //We should always update the previous location for the previous world for this player because at this point
        //he left the previous world
        this.plugin.getPositionRepository().updateLocationForPlayer(player, event.getFrom());

        Location previousLocation = this.plugin.getRuleManager().shouldTeleportPlayer(player, event.getFrom(), event.getDestination().getLocation(player));

        if(previousLocation != null) {
            if(this.isPressureplate(previousLocation)){
                // Find a valid spot around the location
                Location newLocation = this.findAvailableLocation(previousLocation);
                if(newLocation != null) previousLocation = newLocation;
            }

            //There is a location, and the player should teleport, so teleport him
            if (Main.config.getBoolean("debug"))
                this.plugin.getLogger().info("Teleporting player to his previous location");
            event.setCancelled(true);
            player.teleport(previousLocation);
        }
    }

    protected boolean isPressureplate(Location toLocation) {
        Location blockBelow = new Location(toLocation.getWorld(), toLocation.getX(), toLocation.getY()-1, toLocation.getZ());
        if(blockBelow.getBlock().getType().equals(Material.STONE_PLATE)) return true;
        if(blockBelow.getBlock().getType().equals(Material.WOOD_PLATE)) return true;
        if(blockBelow.getBlock().getType().equals(Material.IRON_PLATE)) return true;
        if(blockBelow.getBlock().getType().equals(Material.GOLD_PLATE)) return true;
        if(toLocation.getBlock().getRelative(BlockFace.DOWN).getType().equals(Material.STONE_PLATE)) return true;
        if(toLocation.getBlock().getRelative(BlockFace.DOWN).getType().equals(Material.GOLD_PLATE)) return true;
        if(toLocation.getBlock().getRelative(BlockFace.DOWN).getType().equals(Material.IRON_PLATE)) return true;
        if(toLocation.getBlock().getRelative(BlockFace.DOWN).getType().equals(Material.WOOD_PLATE)) return true;
        if(toLocation.getBlock().getType().equals(Material.STONE_PLATE)) return true;
        if(toLocation.getBlock().getType().equals(Material.WOOD_PLATE)) return true;
        if(toLocation.getBlock().getType().equals(Material.IRON_PLATE)) return true;
        if(toLocation.getBlock().getType().equals(Material.GOLD_PLATE)) return true;
        return false;
    }

    protected Location findAvailableLocation(Location location) {
        //The current given location isn't valid (most likely it is a pressure plate)
        //Get the location in front, back, left and right of it and check if it is air.
        //Also check the block above it to make sure the block can't suffocate.
        Location block1Down = new Location(location.getWorld(), location.getX()-1, location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        Location block2Down = new Location(location.getWorld(), location.getX()+1, location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        Location block3Down = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ()-1, location.getYaw(), location.getPitch());
        Location block4Down = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ()+1, location.getYaw(), location.getPitch());

        if(block1Down.getBlock().getType().equals(Material.AIR)){
            Location block1Up = new Location(location.getWorld(), location.getX()-1, location.getY()+1, location.getZ());
            if(block1Up.getBlock().getType().equals(Material.AIR)) return block1Down;
        }else if(block2Down.getBlock().getType().equals(Material.AIR)){
            Location block2Up = new Location(location.getWorld(), location.getX()+1, location.getY()+1, location.getZ());
            if(block2Up.getBlock().getType().equals(Material.AIR)) return block2Down;
        }else if(block3Down.getBlock().getType().equals(Material.AIR)){
            Location block3Up = new Location(location.getWorld(), location.getX(), location.getY()+1, location.getZ()-1);
            if(block3Up.getBlock().getType().equals(Material.AIR)) return block3Down;
        }else if(block4Down.getBlock().getType().equals(Material.AIR)){
            Location block4Up = new Location(location.getWorld(), location.getX(), location.getY()+1, location.getZ()+1);
            if(block4Up.getBlock().getType().equals(Material.AIR)) return block4Down;
        }
        return null;
    }
}
