package nl.zandervdm.stayput.Models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "stayput_position")
public class Position {

    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField()
    private String uuid;

    @DatabaseField()
    private String player_name;

    @DatabaseField()
    private String world_name;

    @DatabaseField()
    private Double coordinate_x;

    @DatabaseField()
    private Double coordinate_y;

    @DatabaseField()
    private Double coordinate_z;

    @DatabaseField()
    private Float yaw;

    @DatabaseField()
    private Float pitch;

    @DatabaseField
    private String dimension_name;

    @DatabaseField
    private boolean dimension_last_location;

    public Float getPitch() {
        return pitch;
    }

    public void setPitch(Float pitch) {
        this.pitch = pitch;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPlayerName() {
        return player_name;
    }

    public void setPlayerName(String playerName) {
        this.player_name = playerName;
    }

    public String getWorldName() {
        return world_name;
    }

    public void setWorldName(String worldName) {
        this.world_name = worldName;
    }

    public Double getCoordinateX() {
        return coordinate_x;
    }

    public void setCoordinateX(Double coordinateX) {  this.coordinate_x = coordinateX; }

    public Double getCoordinateY() {
        return coordinate_y;
    }

    public void setCoordinateY(Double coordinateY) {
        this.coordinate_y = coordinateY;
    }

    public Double getCoordinateZ() {
        return coordinate_z;
    }

    public void setCoordinateZ(Double coordinateZ) {
        this.coordinate_z = coordinateZ;
    }

    public Float getYaw() { return yaw; }

    public void setYaw(Float yaw) {
        this.yaw = yaw;
    }

    public String getDimensionName() { return dimension_name; }

    public void setDimensionName(String dimensionName) { this.dimension_name = dimensionName; }

    public boolean getDimensionLastLocation() { return dimension_last_location; }

    public void setDimensionLastLocation(Boolean dimension_last_location) { this.dimension_last_location = dimension_last_location; }
}