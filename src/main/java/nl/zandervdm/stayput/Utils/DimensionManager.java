package nl.zandervdm.stayput.Utils;

import com.google.common.collect.HashMultimap;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import nl.zandervdm.stayput.StayPut;
import nl.zandervdm.stayput.Repositories.PositionRepository;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class DimensionManager {

    private StayPut plugin;
    private HashMultimap<String,String> dimensions;
    private static String SECTION = "dimensions";

    public DimensionManager(StayPut plugin) {
        this.plugin = plugin;
        this.dimensions = HashMultimap.create();
    }

    public void loadDimensions() {
        this.ensureConfigIsPrepared();
        this.checkDimensionalDuplication();
        ConfigurationSection dimensions_section = this.plugin.getConfig().getConfigurationSection(SECTION);
        Set<String> dimension_section_keys = this.plugin.getConfig().getConfigurationSection(SECTION).getKeys(false);
        for(String dimension_key : dimension_section_keys) {
            if(StayPut.config.getBoolean("debug")) this.plugin.getLogger().info("Loading dimension " + dimension_key + ":");
            if(dimensions_section.isList(dimension_key)) {
                List<String> sub_dimension = dimensions_section.getStringList(dimension_key);
                for (String world_name : sub_dimension) {
                    MultiverseWorld world = this.plugin.getMultiverseCore().getMVWorldManager().getMVWorld(world_name);
                    if (world != null) {
                        if (StayPut.config.getBoolean("debug")) this.plugin.getLogger().info("--- Loading world " + world_name);
                        this.dimensions.put(dimension_key, world_name);
                    } else {
                        this.plugin.getLogger().info("    world " + world_name + " could not be found for dimension " + dimension_key);
                    }
                }
            } else {
                if(StayPut.config.getBoolean("debug")) this.plugin.getLogger().info("Dimension " + dimension_key + " is not a list.");
            }
        }
        if(StayPut.config.getBoolean("debug") && dimension_section_keys.isEmpty()) this.plugin.getLogger().info("Dimensions list is empty.");
    }

    public HashMultimap<String,String> getDimensions() { return this.dimensions; }

    private void ensureConfigIsPrepared() {
        if (this.plugin.getConfig().getConfigurationSection(SECTION) == null) {
            if(StayPut.config.getBoolean("debug")) this.plugin.getLogger().info("Adding dimensions to config.");
            this.plugin.getConfig().createSection(SECTION);
        }
    }

    private void checkDimensionalDuplication() {
        final Set<String> duplicateDimensions = new HashSet<>();
        final Set<String> intermediateSet = new HashSet<>();
        List<String> dimensionsList = this.plugin.getConfig().getStringList(SECTION);
        for (String dimension : dimensionsList) {
            if (!intermediateSet.add(dimension)) {
                duplicateDimensions.add(dimension);
            }
        }
        if(!duplicateDimensions.isEmpty()) {
            this.plugin.getLogger().info("Duplicate dimensions detected: " + String.join(", ", duplicateDimensions + ". Only the first dimension will be loaded."));
            // return true;
        }
         //return false;
    }

    public Location dimensionCheck(Location fromLocation, Location toLocation) {
        String locationName = toLocation.getWorld().getName();
        ConfigurationSection dimensions = StayPut.config.getConfigurationSection("dimensions");
        dimensions.getKeys(false).forEach(key -> {
            // I don't like mixing plural with singular, too easy to mix up. So we get sub_dimension.
            List<String> sub_dimension = dimensions.getStringList("dimensions." + key);
            // Now go to the next dimension if you can't find it or the world doesn't belong to the dimension (sub_dimension).
            if (sub_dimension == null || !sub_dimension.contains(locationName)) return;

        });
        return toLocation;
    }
}
