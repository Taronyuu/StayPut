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

    public String getPlayer_name() {
        return player_name;
    }
    public void setPlayer_name(String player_name) {
        this.player_name = player_name;
    }

    public String getWorld_name() {
        return world_name;
    }
    public void setWorld_name(String world_name) {
        this.world_name = world_name;
    }

    public Double getCoordinate_x() {
        return coordinate_x;
    }
    public void setCoordinate_x(Double coordinate_x) {  this.coordinate_x = coordinate_x; }

    public Double getCoordinate_y() {
        return coordinate_y;
    }
    public void setCoordinate_y(Double coordinate_y) {
        this.coordinate_y = coordinate_y;
    }

    public Double getCoordinate_z() {
        return coordinate_z;
    }
    public void setCoordinate_z(Double coordinate_z) {
        this.coordinate_z = coordinate_z;
    }

    public Float getYaw() { return yaw; }
    public void setYaw(Float yaw) {
        this.yaw = yaw;
    }

    public String getDimension_name() { return dimension_name; }
    public void setDimension_name(String dimension_name) { this.dimension_name = dimension_name; }

    public boolean getDimensionLastLocation() { return dimension_last_location; }
    public void setDimensionLastLocation(Boolean dimension_last_location) { this.dimension_last_location = dimension_last_location; }
}