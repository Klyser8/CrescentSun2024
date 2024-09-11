package it.crescentsun.crystals;

import it.crescentsun.crescentcore.api.data.DataType;
import it.crescentsun.crescentcore.api.data.plugin.DatabaseColumn;
import it.crescentsun.crescentcore.api.data.plugin.DatabaseTable;
import it.crescentsun.crescentcore.api.data.plugin.PluginData;

import java.util.UUID;

@DatabaseTable(plugin = Crystals.class, tableName = "crystals")
public class CrystalsData extends PluginData {

    @DatabaseColumn(columnName = "uuid", dataType = DataType.VARCHAR_36, isPrimaryKey = true, order = 1)
    public static final UUID UUID = java.util.UUID.fromString("d6d3909a-0fae-4d1f-a124-f564783256ee"); //There will only ever be one instance of this data

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

    public int getCrystalsInCirculation() {
        return crystalsGenerated - crystalsSpent - crystalsLost;
    }

    public int getCrystalsGenerated() {
        return crystalsGenerated;
    }

    public void setCrystalsGenerated(int crystalsGenerated) {
        this.crystalsGenerated = crystalsGenerated;
    }

    public int getCrystalsSpent() {
        return crystalsSpent;
    }

    public void setCrystalsSpent(int crystalsSpent) {
        this.crystalsSpent = crystalsSpent;
    }

    public int getCrystalsLost() {
        return crystalsLost;
    }

    public void setCrystalsLost(int crystalsLost) {
        this.crystalsLost = crystalsLost;
    }
}
