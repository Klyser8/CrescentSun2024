package it.crescentsun.crystals.data;

import it.crescentsun.api.crescentcore.data.DataType;
import it.crescentsun.api.crescentcore.data.plugin.DatabaseColumn;
import it.crescentsun.api.crescentcore.data.plugin.DatabaseTable;
import it.crescentsun.api.crescentcore.data.plugin.SingletonPluginData;
import it.crescentsun.crystals.Crystals;

import java.util.UUID;

@DatabaseTable(tableName = "settings", plugin = Crystals.class)
public class CrystalsSettings extends SingletonPluginData {

    @DatabaseColumn(columnName = "uuid", dataType = DataType.VARCHAR_36, isPrimaryKey = true, order = 1)
    private final UUID UUID = Crystals.SETTINGS_UUID;

    @DatabaseColumn(columnName = "non_owned_crystal_pickup_delay", dataType = DataType.UNSIGNED_INT, order = 2)
    private int nonOwnedCrystalPickupDelay = 400;

    @DatabaseColumn(columnName = "drop_lifetime", dataType = DataType.UNSIGNED_INT, order = 3)
    private int dropLifetime = 12000;

    @Override
    public UUID getUuid() {
        return UUID;
    }

    @Override
    public boolean isProxyDependent() {
        return false;
    }

    public int getNonOwnedCrystalPickupDelay() {
        return nonOwnedCrystalPickupDelay;
    }

    public void setNonOwnedCrystalPickupDelay(int delay) {
        nonOwnedCrystalPickupDelay = delay;
    }

    public int getDropLifetime() {
        return dropLifetime;
    }

    public void setDropLifetime(int dropLifetime) {
        this.dropLifetime = dropLifetime;
    }
}
