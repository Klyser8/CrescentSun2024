package it.crescentsun.crystals.data;

import it.crescentsun.crescentcore.api.data.DataType;
import it.crescentsun.crescentcore.api.data.plugin.DatabaseColumn;
import it.crescentsun.crescentcore.api.data.plugin.DatabaseTable;
import it.crescentsun.crescentcore.api.data.plugin.SingletonPluginData;
import it.crescentsun.crystals.Crystals;

import java.util.UUID;

@DatabaseTable(tableName = "settings", plugin = Crystals.class)
public class CrystalsSettings extends SingletonPluginData {

    @DatabaseColumn(columnName = "uuid", dataType = DataType.VARCHAR_36, isPrimaryKey = true, order = 1)
    private final UUID UUID = Crystals.SETTINGS_UUID;

    @DatabaseColumn(columnName = "non_owned_crystal_pickup_delay", dataType = DataType.UNSIGNED_INT, order = 2)
    private static int nonOwnedCrystalPickupDelay = 400;

    @Override
    public UUID getUuid() {
        return UUID;
    }

    public int getNonOwnedCrystalPickupDelay() {
        return nonOwnedCrystalPickupDelay;
    }

    public void setNonOwnedCrystalPickupDelay(int delay) {
        nonOwnedCrystalPickupDelay = delay;
    }
}
