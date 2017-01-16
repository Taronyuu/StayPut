package nl.zandervdm.stayput.Listeners;

import com.onarandombox.MultiverseCore.event.MVTeleportEvent;
import javafx.geometry.Pos;
import nl.zandervdm.stayput.Main;
import nl.zandervdm.stayput.Models.Position;
import org.bukkit.Location;
import org.bukkit.World;
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
            //There is a location, and the player should teleport, so teleport him
            if (Main.config.getBoolean("debug"))
                this.plugin.getLogger().info("Teleporting player to his previous location");
            event.setCancelled(true);
            player.teleport(previousLocation);
        }
    }
}
