package it.crescentsun.crescentcraft.artifact.data;

import it.crescentsun.api.crescentcore.data.DataType;
import it.crescentsun.api.crescentcore.data.plugin.DatabaseColumn;
import it.crescentsun.api.crescentcore.data.plugin.DatabaseTable;
import it.crescentsun.api.crescentcore.data.plugin.PluginData;
import it.crescentsun.crescentcraft.CrescentCraft;
import it.crescentsun.crescentcraft.artifact.DetonationOrbManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

@DatabaseTable(tableName = "detonation_orbs", plugin = CrescentCraft.class)
public class DetonationOrbData extends PluginData {

    @DatabaseColumn(columnName = "uuid", dataType = DataType.VARCHAR_36, order = 1, isPrimaryKey = true)
    private UUID uuid;

    @DatabaseColumn(columnName = "owner", dataType = DataType.VARCHAR_36, order = 2)
    private UUID ownerUuid;

    @DatabaseColumn(columnName = "server", dataType = DataType.VARCHAR_36, order = 3)
    private String server;

    @DatabaseColumn(columnName = "world_uuid", dataType = DataType.VARCHAR_36, order = 4)
    private UUID worldUuid;

    @DatabaseColumn(columnName = "x", dataType = DataType.INT, order = 5)
    private int x;

    @DatabaseColumn(columnName = "y", dataType = DataType.INT, order = 6)
    private int y;

    @DatabaseColumn(columnName = "z", dataType = DataType.INT, order = 7)
    private int z;

    @DatabaseColumn(columnName = "placed_at", dataType = DataType.BIG_INT, order = 8)
    private long placedAt;

    public DetonationOrbData(UUID uuid, UUID ownerUuid, String server, Location location, long placedAt) {
        super();
        this.uuid = uuid;
        this.ownerUuid = ownerUuid;
        this.server = server;
        this.worldUuid = location.getWorld().getUID();
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.placedAt = placedAt;
    }

    // Required for reflection
    private DetonationOrbData() {
        super();
    }

    @Override
    public boolean tryInit() {
        DetonationOrbManager manager = ((CrescentCraft) owningPlugin).getDetonationOrbManager();
        if (!shouldInit()) {
            return false;
        }
        if (manager.handleExpiry(this)) {
            return false;
        }
        if (super.tryInit()) {
            manager.startOrbTask(this);
        }
        return initialized;
    }

    @Override
    public boolean shouldInit() {
        String serverName = owningPlugin.getCrescentCoreAPI().getServerName();
        if (serverName == null || !serverName.equalsIgnoreCase(server)) {
            return false;
        }
        return Bukkit.getWorld(worldUuid) != null;
    }

    public Location getLocation() {
        World world = Bukkit.getWorld(worldUuid);
        return new Location(world, x, y, z);
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public UUID getWorldUuid() {
        return worldUuid;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public long getPlacedAt() {
        return placedAt;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public boolean isProxyDependent() {
        return true;
    }
}
