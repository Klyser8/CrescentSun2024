package it.crescentsun.crystals.vault;

import it.crescentsun.api.common.DatabaseNamespacedKeys;
import it.crescentsun.api.crescentcore.data.DataType;
import it.crescentsun.api.crescentcore.data.plugin.DatabaseColumn;
import it.crescentsun.api.crescentcore.data.plugin.DatabaseTable;
import it.crescentsun.api.crescentcore.data.plugin.PluginData;
import it.crescentsun.api.crescentcore.data.plugin.PluginDataIdentifier;
import it.crescentsun.api.crystals.VaultService;
import it.crescentsun.crescentmsg.api.CrescentHexCodes;
import it.crescentsun.crystals.Crystals;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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

    transient private VaultScheduledTask bukkitTask;

    /// The key used to identify vaults in the crescent sun network
    public static final NamespacedKey VAULT_KEY = Crystals.id("vault");

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

    public UUID getOwnerUUID() {
        return ownerUuid;
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
            this.bukkitTask = new VaultScheduledTask((Crystals) owningPlugin, ownerUuid, this);
            Bukkit.getScheduler().runTaskTimer(owningPlugin, bukkitTask, 0, 1);
            refreshVaultNameTag();
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

    public void refreshVaultNameTag() {
        Optional<Integer> inVault = owningPlugin.getPlayerDataService()
                .getData(ownerUuid)
                .getDataValue(DatabaseNamespacedKeys.PLAYER_CRYSTALS_IN_VAULT);
        bukkitTask.textDisplay.text(MiniMessage.miniMessage().deserialize(
                CrescentHexCodes.ICE_CITADEL + Bukkit.getOfflinePlayer(ownerUuid).getName() + "'s Crystal Vault" +
                        CrescentHexCodes.WHITE + " - " +
                        CrescentHexCodes.DROPLET + inVault.orElse(0)));
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

    public OfflinePlayer getOwner() {
        return Bukkit.getOfflinePlayer(ownerUuid);
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public String getServerName() {
        return server;
    }

    public void setServerName(String server) {
        this.server = server;
    }

    public UUID getWorldUuid() {
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

    @Override
    public <T extends PluginData> CompletableFuture<PluginDataIdentifier<T>> deleteAndSync() {
        CompletableFuture<PluginDataIdentifier<T>> pluginDataIdentifierCompletableFuture = super.deleteAndSync();
        // Cancel the scheduled task if it exists
        if (bukkitTask != null) {

        }
        return pluginDataIdentifierCompletableFuture;
    }

    /**
     * Gets the locations of the vault structure blocks.
     * The first in the array is always the block underneath the vault, while the remaining four are the cross-shaped diamond blocks around it.
     * @return the array of locations related to this vault
     */
    public Location[] getVaultStructureLocations() {
        Location[] locations = new Location[] {
                getLocation().clone().add(0, -1, 0),
                getLocation().clone().add(1, -1, 0),
                getLocation().clone().add(-1, -1, 0),
                getLocation().clone().add(0, -1, 1),
                getLocation().clone().add(0, -1, -1),
        };
        return locations;
    }
}
