package it.crescentsun.crystals.data;

import it.crescentsun.api.crescentcore.data.DataType;
import it.crescentsun.api.crescentcore.data.plugin.DatabaseColumn;
import it.crescentsun.api.crescentcore.data.plugin.DatabaseTable;
import it.crescentsun.api.crescentcore.data.plugin.SingletonPluginData;
import it.crescentsun.crystals.Crystals;

import java.util.UUID;

@DatabaseTable(plugin = Crystals.class, tableName = "statistics")
public class CrystalsStatistics extends SingletonPluginData {

    @DatabaseColumn(columnName = "uuid", dataType = DataType.VARCHAR_36, isPrimaryKey = true, order = 1)
    private final UUID UUID = Crystals.STATISTICS_UUID; //There will only ever be one instance of this data

    @DatabaseColumn(columnName = "crystals_generated", dataType = DataType.INT, order = 2)
    private int crystalsGenerated;

    @DatabaseColumn(columnName = "crystals_spent", dataType = DataType.INT, order = 3)
    private int crystalsSpent;

    @DatabaseColumn(columnName = "crystals_lost", dataType = DataType.INT, order = 4)
    private int crystalsLost;

    @Override
    public UUID getUuid() {
        return UUID;
    }

    @Override
    public boolean isProxyDependent() {
        return false;
    }

    public int getCrystalsInCirculation() {
        return crystalsGenerated - crystalsSpent - crystalsLost;
    }

    public int getCrystalsGenerated() {
        return crystalsGenerated;
    }

    public void setCrystalsGenerated(int crystalsGenerated) {
        this.crystalsGenerated = crystalsGenerated;
        saveAndSync();
    }

    public int getCrystalsSpent() {
        return crystalsSpent;
    }

    public void setCrystalsSpent(int crystalsSpent) {
        this.crystalsSpent = crystalsSpent;
        saveAndSync();
    }

    public int getCrystalsLost() {
        return crystalsLost;
    }

    public void setCrystalsLost(int crystalsLost) {
        this.crystalsLost = crystalsLost;
        saveAndSync();
    }
}
