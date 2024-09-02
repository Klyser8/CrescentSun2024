package it.crescentsun.crescentcore.core.data;

import it.crescentsun.crescentcore.CrescentCore;
import it.crescentsun.crescentcore.api.data.DataType;
import it.crescentsun.crescentcore.api.data.plugin.DatabaseColumn;
import it.crescentsun.crescentcore.api.data.plugin.DatabaseTable;
import it.crescentsun.crescentcore.api.data.plugin.PluginData;

import java.util.*;

@DatabaseTable(tableName = "jumpwarps", plugin = CrescentCore.class)
public class JumpWarp extends PluginData {

    @DatabaseColumn(columnName = "id", dataType = DataType.AUTO_INCREMENT_INT, isPrimaryKey = true)
    private int id;

    @DatabaseColumn(columnName = "name", dataType = DataType.VARCHAR_36)
    private String warpName;

    @DatabaseColumn(columnName = "server", dataType = DataType.VARCHAR_36)
    private String server;

    @DatabaseColumn(columnName = "world_uuid", dataType = DataType.VARCHAR_36)
    private UUID worldUUID;

    @DatabaseColumn(columnName = "x", dataType = DataType.INT)
    private int x;

    @DatabaseColumn(columnName = "y", dataType = DataType.INT)
    private int y;

    @DatabaseColumn(columnName = "z", dataType = DataType.INT)
    private int z;

    @DatabaseColumn(columnName = "server_destination", dataType = DataType.VARCHAR_255)
    private String destinationServer;

    @DatabaseColumn(columnName = "world_uuid_destination", dataType = DataType.VARCHAR_36)
    private UUID destinationWorldUUID;

    // If destination X, Y, and Z are 123456789: destination is player's last location.
    @DatabaseColumn(columnName = "destination_x", dataType = DataType.INT)
    private int destinationX;

    @DatabaseColumn(columnName = "destination_y", dataType = DataType.INT)
    private int destinationY;

    @DatabaseColumn(columnName = "destination_z", dataType = DataType.INT)
    private int destinationZ;

    public JumpWarp(int id, String warpName, String server, UUID worldUUID, int x, int y, int z, String destinationServer, UUID destinationWorldUUID, int destinationX, int destinationY, int destinationZ) {
        this.id = id;
        this.warpName = warpName;
        this.server = server;
        this.worldUUID = worldUUID;
        this.x = x;
        this.y = y;
        this.z = z;
        this.destinationServer = destinationServer;
        this.destinationWorldUUID = destinationWorldUUID;
        this.destinationX = destinationX;
        this.destinationY = destinationY;
        this.destinationZ = destinationZ;
    }

    // Empty constructor for reflection loading
    public JumpWarp() {

    }

    public int getId() {
        return id;
    }

}
