package nl.zandervdm.stayput.Repositories;

import nl.zandervdm.stayput.Main;
import nl.zandervdm.stayput.Models.Position;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class PositionRepository {

    protected Main plugin;

    public PositionRepository(Main plugin) {
        this.plugin = plugin;
    }

    public void updateLocationForPlayer(Player player, Location location){
        Position position = null;
        try {
            position = this.plugin.getPositionMapper()
                    .queryBuilder()
                    .where()
                    .eq("uuid", player.getUniqueId().toString())
                    .and()
                    .eq("world_name", location.getWorld().getName())
                    .queryForFirst();
        } catch (SQLException e) {
            //
        }

        if(position == null) {
            position = new Position();
        }
        position.setWorld_name(location.getWorld().getName());
        position.setPlayer_name(player.getName());
        position.setUuid(player.getUniqueId().toString());
        position.setCoordinate_x(location.getX() * 1.0);
        position.setCoordinate_y(location.getY() * 1.0);
        position.setCoordinate_z(location.getZ() * 1.0);
        position.setYaw(location.getYaw());
        position.setPitch(location.getPitch());
        try {
            this.plugin.getPositionMapper().createOrUpdate(position);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Location getPreviousLocation(Player player, World world){
        Position position = null;
        try {
            position = this.plugin.getPositionMapper()
                    .queryBuilder()
                    .where()
                    .eq("uuid", player.getUniqueId().toString())
                    .and()
                    .eq("world_name", world.getName())
                    .queryForFirst();
        } catch (SQLException e) {
            return null;
        }
        if(position == null) return null;
        double coordX = position.getCoordinate_x();
        double coordY = position.getCoordinate_y();
        double coordZ = position.getCoordinate_z();
        float yaw = position.getYaw();
        float pitch = position.getPitch();

        return new Location(world, coordX, coordY, coordZ, yaw, pitch);
    }
}
