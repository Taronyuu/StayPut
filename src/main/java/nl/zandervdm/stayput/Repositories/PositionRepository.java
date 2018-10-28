package nl.zandervdm.stayput.Repositories;

import com.google.common.collect.HashMultimap;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import nl.zandervdm.stayput.StayPut;
import nl.zandervdm.stayput.Models.Position;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.*;

public class PositionRepository {

    private StayPut plugin;
    private MVWorldManager worldManager;

    public PositionRepository(StayPut plugin) {
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
        String world_name = location.getWorld().getName();
        position.setWorld_name(world_name);
        position.setPlayer_name(player.getName());
        position.setUuid(player.getUniqueId().toString());
        position.setCoordinate_x(location.getX());
        position.setCoordinate_y(location.getY());
        position.setCoordinate_z(location.getZ());
        position.setYaw(location.getYaw());
        position.setPitch(location.getPitch());
        // Reset dimension last location.
        updateDimension(position.getDimension_name(), world_name);
        position.setDimensionLastLocation(true);
        try {
            this.plugin.getPositionMapper().createOrUpdate(position);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Location getPreviousLocationIgnoringDimension(Player player, World world){
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
        double coordinate_x = position.getCoordinate_x();
        double coordinate_y = position.getCoordinate_y();
        double coordinate_z = position.getCoordinate_z();
        float yaw = position.getYaw();
        float pitch = position.getPitch();

        return new Location(world, coordinate_x, coordinate_y, coordinate_z, yaw, pitch);
    }

    private void updateDimension(String dimension_name_passed, String world_name) {
        // if world is null, quickly check if it has a dimension.
        String dimension_name = dimension_name_passed;
        if(world_name == null || world_name.isEmpty()) {
            dimension_name = this.plugin.getDimensionManager().getDimension(world_name);
        }
        if(dimension_name != null && !dimension_name.isEmpty()) {
            List<Position> dimension_positions = null;
            try {
                dimension_positions = this.plugin.getPositionMapper()
                        .queryBuilder()
                        .where()
                        .eq("dimension_name", dimension_name)
                        .query();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            for (Position position : dimension_positions) {
                position.setDimensionLastLocation(false);
                try {
                    this.plugin.getPositionMapper().createOrUpdate(position);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Need to check and update all locations to new dimensions as necessary.
    public void updateDimensionOfPositions(HashMultimap<String,String> dimensions) {
        if(dimensions != null && !dimensions.isEmpty()) {
            for (String dimension_name : dimensions.keySet()) {
                Set<String> worlds = dimensions.get(dimension_name);
                if (worlds == null || worlds.isEmpty()) {
                    plugin.getLogger().info("Dimension " + dimension_name + " has no worlds. Skipping.");
                } else {
                    if (plugin.getConfig().getBoolean("debug")) plugin.getLogger().info("Updating positions for dimension: " + dimension_name);
                    List<Position> world_positions = null;
                    try {
                        world_positions = this.plugin.getPositionMapper()
                                .queryBuilder()
                                .where().in("world_name", worlds)
                                .query();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    if (plugin.getConfig().getBoolean("debug")) plugin.getLogger().info("Found: " + world_positions.size() + " worlds");
                    for(Position position : world_positions) {
                        try{
                            this.plugin.getPositionMapper().createOrUpdate(position);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
                removeDimensionalOverlap(dimension_name);
            }
        }
    }

    // Occasionally check if an update to dimensions has caused an overlap of last_locations.
    private void removeDimensionalOverlap(String dimension_name) {
        List<Position> dimension_last_positions = null;
        try {
            dimension_last_positions = this.plugin.getPositionMapper()
                .queryBuilder()
                .where()
                .eq("dimension_name", dimension_name)
                .and()
                .eq("dimension_last_location", true)
                .query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if( dimension_last_positions != null && dimension_last_positions.size() >= 2) {
            for(Position position : dimension_last_positions) {
                position.setDimensionLastLocation(false);
                try {
                    this.plugin.getPositionMapper().createOrUpdate(position);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Location getPreviousLocation(Player player, World intended_world) {
        String world_name = intended_world.getName();
        Position intended_position = null;
        Position actual_position;
        try {
            intended_position = this.plugin.getPositionMapper()
                .queryBuilder()
                .where()
                .eq("uuid", player.getUniqueId().toString())
                .and()
                .eq("world_name", world_name)
                .queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Stop and just go to the intended location.
        if(intended_position != null && intended_position.getDimensionLastLocation()) {
            if(plugin.getConfig().getBoolean("debug")) plugin.getLogger().info("Dimension - intended location is last location.");
            return getPreviousLocationIgnoringDimension(player, intended_world);
        } else {
            if(intended_position.getDimension_name() == null) return null;
            try {
                actual_position = this.plugin.getPositionMapper()
                        .queryBuilder()
                        .where()
                        .eq("dimension_name", intended_position.getDimension_name())
                        .and()
                        .eq("dimension_last_location", true)
                        .queryForFirst();
                World actual_world = this.plugin.getMultiverseCore().getMVWorldManager().getMVWorld(actual_position.getWorld_name()).getCBWorld();

                if(plugin.getConfig().getBoolean("debug")) plugin.getLogger().info("Intended to go to " + intended_world.getName() + " going to " + actual_world.getName() + " in dimension " + intended_position.getDimension_name());
                return new Location(actual_world, actual_position.getCoordinate_x(), actual_position.getCoordinate_y(), actual_position.getCoordinate_z(), actual_position.getYaw(), actual_position.getPitch());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if(plugin.getConfig().getBoolean("debug")) plugin.getLogger().info("getLastWorldInDimension - We should never get here. This is a problem.");
        return null;
    }

}
