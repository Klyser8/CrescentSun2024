package it.crescentsun.jumpwarps;

import it.crescentsun.crescentcore.api.data.DataType;
import it.crescentsun.crescentcore.api.data.plugin.DatabaseColumn;
import it.crescentsun.crescentcore.api.data.plugin.DatabaseTable;
import it.crescentsun.crescentcore.api.data.plugin.PluginData;
import it.crescentsun.jumpwarps.warphandling.JumpWarpScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings("unused")
@DatabaseTable(tableName = "jumpwarps", plugin = JumpWarps.class)
public class JumpWarpData extends PluginData {

    @DatabaseColumn(columnName = "uuid", dataType = DataType.VARCHAR_36, order = 1, isPrimaryKey = true)
    private UUID uuid;

    @DatabaseColumn(columnName = "name", dataType = DataType.VARCHAR_36, order = 2)
    private String warpName;

    @DatabaseColumn(columnName = "server", dataType = DataType.VARCHAR_36, order = 3)
    private String server;

    @DatabaseColumn(columnName = "world_uuid", dataType = DataType.VARCHAR_36, order = 4)
    private UUID worldUUID;

    @DatabaseColumn(columnName = "x", dataType = DataType.INT, order = 5)
    private int x;

    @DatabaseColumn(columnName = "y", dataType = DataType.INT, order = 6)
    private int y;

    @DatabaseColumn(columnName = "z", dataType = DataType.INT, order = 7)
    private int z;

    @DatabaseColumn(columnName = "server_destination", dataType = DataType.VARCHAR_255, order = 8)
    private String destinationServer;

    transient private JumpWarps plugin;
    transient private Consumer<BukkitTask> bukkitTask;

    /**
     * Constructor for creating a new JumpWarpData instance.
     * With this constructor, you will be creating a same-server Jump Warp.
     *
     * @param uuid The UUID of the Jump Warp.
     * @param warpName The name of the Jump Warp.
     * @param server The server the Jump Warp is on.
     * @param location The location of the Jump Warp.
     * @param destination The destination of the Jump Warp.
     */
    public JumpWarpData(UUID uuid, String warpName, String server, Location location, Location destination) {
        super();
        this.uuid = uuid;
        this.warpName = warpName;
        this.server = server;
        this.worldUUID = location.getWorld().getUID();
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.destinationServer = this.server;
    }

    public JumpWarpData(UUID uuid, String warpName, String server, Location location, String destinationServer) {
        super();
        this.uuid = uuid;
        this.warpName = warpName;
        this.server = server;
        this.worldUUID = location.getWorld().getUID();
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.destinationServer = destinationServer;
    }

    // Required for reflection
    private JumpWarpData() {
        super();
    }

    @Override
    public void init() {
        super.init();
        this.plugin = JumpWarps.getInstance();
        this.bukkitTask = new JumpWarpScheduledTask(plugin, this);

        Bukkit.getScheduler().runTaskTimer(plugin, bukkitTask, 0, 2);
    }

    @Override
    public boolean shouldInit() {
        if (server == null) {
            crescentCore.getLogger().warning("JumpWarpData " + uuid + " has a null server. Skipping initialization.");
            return false;
        }
        return crescentCore.getServerName().equalsIgnoreCase(server); //Destination server should be checked instead of the originating server, as the method is run on all servers but the originating one.
    }

    public Location getLocation() {
        World world = Bukkit.getWorld(worldUUID);
        if (world == null) {
//            crescentCore.getLogger().warning("World with UUID " + worldUUID + " not found while trying to fetch location for JumpWarpData " + this);FIXME: This gets called at every. single. jump.
        }
        return new Location(world, x, y, z);
    }

    public String getDestinationServer() {
        return destinationServer;
    }

    public void setDestinationServer(String destinationServer) {
        this.destinationServer = destinationServer;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public UUID getWorldUUID() {
        return worldUUID;
    }

    public void setWorldUUID(UUID worldUUID) {
        this.worldUUID = worldUUID;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getWarpName() {
        return warpName;
    }

    public void setWarpName(String warpName) {
        this.warpName = warpName;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String toString() {
        return "JumpWarpData{" +
                "uuid=" + uuid +
                ", warpName='" + warpName + '\'' +
                ", server='" + server + '\'' +
                ", worldUUID=" + worldUUID +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", destinationServer='" + destinationServer + '\'' +
                '}';
    }
}
