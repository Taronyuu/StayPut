package nl.zandervdm.stayput.Repositories;

import com.google.common.collect.*;
import com.j256.ormlite.stmt.UpdateBuilder;
import nl.zandervdm.stayput.StayPut;
import nl.zandervdm.stayput.Models.Position;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.*;

public class PositionRepository {

    private StayPut plugin;

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
        String worldName = location.getWorld().getName();
        position.setWorldName(worldName);
        position.setPlayerName(player.getName());
        position.setUuid(player.getUniqueId().toString());
        position.setCoordinateX(location.getX());
        position.setCoordinateY(location.getY());
        position.setCoordinateZ(location.getZ());
        position.setYaw(location.getYaw());
        position.setPitch(location.getPitch());
        // Reset dimension last location.
        position.setDimensionName(updateDimension(player, position.getDimensionName(), worldName));
        position.setDimensionLastLocation(true);
        try {
            this.plugin.getPositionMapper().createOrUpdate(position);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Location getPreviousLocationIgnoringDimension(Player player, World world){
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
        double coordinateX = position.getCoordinateX();
        double coordinateY = position.getCoordinateY();
        double coordinateZ = position.getCoordinateZ();
        float yaw = position.getYaw();
        float pitch = position.getPitch();

        return new Location(world, coordinateX, coordinateY, coordinateZ, yaw, pitch);
    }

    private String updateDimension(Player player, String dimensionName, String worldName) {
        if(dimensionName == null || dimensionName.isEmpty()) {
            dimensionName = this.plugin.getDimensionManager().getDimension(worldName);
        }
        if(dimensionName != null && !dimensionName.isEmpty()) {
            List<Position> dimensionPositions = null;
            UpdateBuilder<Position, Integer> updater = null;
            try {
                updater = this.plugin.getPositionMapper().updateBuilder();
                updater.updateColumnValue("dimension_last_location", false)
                        .where()
                        .eq("dimension_name", dimensionName)
                        .and()
                        .eq("uuid", player.getUniqueId().toString());
                updater.update();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return dimensionName;
    }

    // Need to check and update all locations to new dimensions as necessary.
    public void updateDimensionOfPositions(ImmutableMultimap<String,String> dimensions) {
        if(dimensions != null && !dimensions.isEmpty()) {
            for (String dimensionName : dimensions.keySet()) {
                ImmutableCollection<String> worlds = dimensions.get(dimensionName);
                if (worlds == null || worlds.isEmpty()) {
                    plugin.getLogger().info("Dimension " + dimensionName + " has no worlds. Skipping.");
                } else {
                    if (plugin.getConfig().getBoolean("debug")) plugin.getLogger().info("Updating positions for dimension: " + dimensionName);
                    List<Position> worldPositions = null;
                    UpdateBuilder<Position, Integer> updater = null;
                    try {
                        worldPositions = this.plugin.getPositionMapper()
                                .queryBuilder()
                                .where().in("world_name", worlds)
                                .query();
                        updater = this.plugin.getPositionMapper()
                                .updateBuilder();
                        updater.updateColumnValue("dimension_name", dimensionName)
                                .where()
                                .in("world_name", worlds);
                        updater.update();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    /*
                    if (plugin.getConfig().getBoolean("debug")) plugin.getLogger().info("Found: " + worldPositions.size() + " worlds");
                    for(Position position : worldPositions) {
                        position.setDimensionName(dimensionName);
                        try{
                            this.plugin.getPositionMapper().createOrUpdate(position);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                    */
                }
                removeDimensionalOverlap(dimensionName);
            }
        }
    }

    // Occasionally check if an update to dimensions has caused an overlap of lastLocations.
    // Don't use an updater as we want to only execute if we have more than 2 records.
    private void removeDimensionalOverlap(String dimensionName) {
        List<Position> dimensionLastPositions = null;
        try {
            dimensionLastPositions = this.plugin.getPositionMapper()
                .queryBuilder()
                .where()
                .eq("dimension_name", dimensionName)
                .and()
                .eq("dimension_last_location", true)
                .query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if( dimensionLastPositions != null && dimensionLastPositions.size() >= 2) {
            for(Position position : dimensionLastPositions) {
                position.setDimensionLastLocation(false);
                try {
                    this.plugin.getPositionMapper().createOrUpdate(position);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Location getPreviousLocation(Player player, World intendedWorld) {
        String worldName = intendedWorld.getName();
        Position intendedPosition = null;
        Position actualPosition;
        String dimensionName = null;
        try {
            intendedPosition = this.plugin.getPositionMapper()
                .queryBuilder()
                .where()
                .eq("uuid", player.getUniqueId().toString())
                .and()
                .eq("world_name", worldName)
                .queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(intendedPosition == null) {
            dimensionName = this.plugin.getDimensionManager().getDimension(worldName);
        } else {
            dimensionName = intendedPosition.getDimensionName();
        }

        // Stop and just go to the intended location.
        if(intendedPosition != null && intendedPosition.getDimensionLastLocation()) {
            if(plugin.getConfig().getBoolean("debug")) plugin.getLogger().info("Dimension - intended location is last location.");
            return getPreviousLocationIgnoringDimension(player, intendedWorld);
        } else if(dimensionName == null || dimensionName.isEmpty()) {
            return getPreviousLocationIgnoringDimension(player, intendedWorld);
        } else {
            try {
                actualPosition = this.plugin.getPositionMapper()
                        .queryBuilder()
                        .where()
                        .eq("dimension_name", dimensionName)
                        .and()
                        .eq("dimension_last_location", true)
                        .queryForFirst();
                World actualWorld = this.plugin.getMultiverseCore().getMVWorldManager().getMVWorld(actualPosition.getWorldName()).getCBWorld();

                if(plugin.getConfig().getBoolean("debug") && intendedPosition == null) plugin.getLogger().info("Intended to go to " + intendedWorld.getName());
                if(plugin.getConfig().getBoolean("debug") && intendedPosition != null) plugin.getLogger().info("Intended to go to " + intendedWorld.getName() + " X: " + intendedPosition.getCoordinateX() + " Y: " + intendedPosition.getCoordinateY() + " Z: " + intendedPosition.getCoordinateZ() + " Dimension: " + intendedPosition.getDimensionName());
                if(plugin.getConfig().getBoolean("debug")) plugin.getLogger().info("Going to " + actualWorld.getName() + " X: " + actualPosition.getCoordinateX() + " Y: " + actualPosition.getCoordinateY() + " Z: " + actualPosition.getCoordinateZ() + " in dimension " + actualPosition.getDimensionName());
                return new Location(actualWorld, actualPosition.getCoordinateX(), actualPosition.getCoordinateY(), actualPosition.getCoordinateZ(), actualPosition.getYaw(), actualPosition.getPitch());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if(plugin.getConfig().getBoolean("debug")) plugin.getLogger().info("getLastWorldInDimension - We should never get here. This is a problem.");
        return null;
    }

}
