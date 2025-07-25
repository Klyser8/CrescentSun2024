package it.crescentsun.crystals.vault;

import it.crescentsun.api.crescentcore.data.DataType;
import it.crescentsun.api.crescentcore.data.plugin.DatabaseColumn;
import it.crescentsun.api.crescentcore.data.plugin.DatabaseTable;
import it.crescentsun.api.crescentcore.data.plugin.PluginData;
import it.crescentsun.crystals.Crystals;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;
import java.util.function.Consumer;

@DatabaseTable(tableName = "vaults", plugin = Crystals.class)
public class VaultData extends PluginData {

    @DatabaseColumn(columnName = "uuid", dataType = DataType.VARCHAR_36, order = 0, isPrimaryKey = true)
    private UUID uuid;

    @DatabaseColumn(columnName = "owner", dataType = DataType.VARCHAR_36, order = 1)
    private UUID ownerUuid;

    @DatabaseColumn(columnName = "is_public", dataType = DataType.BOOLEAN, order = 2)
    private boolean isPublic;   // Whether the vault is accessible by anyone or not

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

    transient private Consumer<BukkitTask> bukkitTask;

    public VaultData(Crystals plugin, UUID uuid, UUID ownerUuid, boolean isPublic, String server, Location location) {
        super();
        this.uuid = uuid;
        this.ownerUuid = ownerUuid;
        this.isPublic = isPublic;
        this.server = server;
        this.worldUUID = location.getWorld().getUID();
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
    }

    public VaultData() {
        super();
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    protected boolean shouldInit() {
        if (server == null) {
            return false;
        }
        String serverName = owningPlugin.getCrescentCoreAPI().getServerName();
        if (serverName == null) {
            return false;
        }
        return serverName.equalsIgnoreCase(server);
    }

    @Override
    public boolean tryInit() {
        if (super.tryInit()) {
            Player owner = Bukkit.getPlayer(ownerUuid);
            if (owner == null) {
                owningPlugin.getLogger().warning("Owner with UUID " + ownerUuid + " not found while trying to initialize VaultData " + this);
                return false;
            }
            this.bukkitTask = new VaultScheduledTask((Crystals) owningPlugin, owner, this);
            Bukkit.getScheduler().runTaskTimer(owningPlugin, bukkitTask, 0, 1);
        }
        return initialized;
    }

    @Override
    public boolean isProxyDependent() {
        return false;
    }

    public Location getLocation() {
        World world = Bukkit.getWorld(worldUUID);
        if (world == null) {
            owningPlugin.getLogger().warning("World with UUID " + worldUUID + " not found while trying to fetch location for VaultData " + this);
        }
        return new Location(world, x, y, z);
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public void setOwnerUuid(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public UUID getWorldUUID() {
        return worldUUID;
    }

    public void setWorldUUID(UUID worldUUID) {
        this.worldUUID = worldUUID;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }
}
