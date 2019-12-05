package nl.zandervdm.stayput.Utils;

import com.google.common.collect.*;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import nl.zandervdm.stayput.StayPut;
import nl.zandervdm.stayput.Repositories.PositionRepository;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class DimensionManager {

    private StayPut plugin;
    private ImmutableMultimap<String,String> dimensions;
    private static String SECTION = "dimensions";

    public DimensionManager(StayPut plugin) {
        this.plugin = plugin;
    }

    public void loadDimensions() {
        this.ensureConfigIsPrepared();
        this.checkDimensionalDuplication();
        ImmutableMultimap.Builder<String,String> builder = ImmutableMultimap.builder();
        ConfigurationSection dimensionsSection = this.plugin.getConfig().getConfigurationSection(SECTION);
        Set<String> dimensionSectionKeys = this.plugin.getConfig().getConfigurationSection(SECTION).getKeys(false);
        for(String dimensionKey : dimensionSectionKeys) {
            if(StayPut.config.getBoolean("debug")) this.plugin.getLogger().info("Loading dimension " + dimensionKey + ":");
            if(dimensionsSection.isList(dimensionKey)) {
                List<String> subDimension = dimensionsSection.getStringList(dimensionKey);
                for (String worldName : subDimension) {
                    MultiverseWorld world = this.plugin.getMultiverseCore().getMVWorldManager().getMVWorld(worldName);
                    if (world != null) {
                        if (StayPut.config.getBoolean("debug")) this.plugin.getLogger().info("--- Loading world " + worldName);
                        builder.put(dimensionKey, worldName);
                    } else {
                        this.plugin.getLogger().info("    world " + worldName + " could not be found for dimension " + dimensionKey);
                    }
                }
            } else {
                if(StayPut.config.getBoolean("debug")) this.plugin.getLogger().info("Dimension " + dimensionKey + " is not a list.");
            }
        }
        this.dimensions = builder.build();
        if(StayPut.config.getBoolean("debug") && dimensionSectionKeys.isEmpty()) this.plugin.getLogger().info("Dimensions list is empty.");
    }

    public ImmutableMultimap<String,String> getDimensions() { return this.dimensions; }

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

    public String getDimension(String worldName) {
        String dimension = null;
        ImmutableList<String> invertedDimensions = this.dimensions.inverse().get(worldName).asList();

        if(invertedDimensions.size() > 0) {
            dimension = invertedDimensions.get(0);
        }

        return dimension;
    }
}
