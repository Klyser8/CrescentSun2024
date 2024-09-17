package it.crescentsun.crescentcore.core;

import it.crescentsun.crescentcore.CrescentCore;
import it.crescentsun.crescentcore.api.data.DataType;
import it.crescentsun.crescentcore.api.data.plugin.DatabaseColumn;
import it.crescentsun.crescentcore.api.data.plugin.DatabaseTable;
import it.crescentsun.crescentcore.api.data.plugin.SingletonPluginData;

import java.sql.Timestamp;
import java.util.UUID;

@DatabaseTable(tableName = "server_statistics", plugin = CrescentCore.class)
public class ServerStatistics extends SingletonPluginData {

    @DatabaseColumn(columnName = "uuid", dataType = DataType.VARCHAR_36, isPrimaryKey = true, order = 1)
    public final UUID UUID = CrescentCore.STATISTICS_UUID;

    @DatabaseColumn(columnName = "max_concurrent_players", dataType = DataType.INT, order = 2)
    private int maxConcurrentPlayers = 0;

    @DatabaseColumn(columnName = "total_players", dataType = DataType.INT, order = 3)
    private int totalPlayers = 0;

    @DatabaseColumn(columnName = "last_restart", dataType = DataType.TIMESTAMP, order = 4)
    private Timestamp lastRestart = new Timestamp(System.currentTimeMillis());

    @Override
    public UUID getUuid() {
        return UUID;
    }

    public int getMaxConcurrentPlayers() {
        return maxConcurrentPlayers;
    }

    public void setMaxConcurrentPlayers(int maxConcurrentPlayers) {
        this.maxConcurrentPlayers = maxConcurrentPlayers;
        saveAndSync();
    }

    public int getTotalPlayers() {
        return totalPlayers;
    }

    public void setTotalPlayers(int totalPlayers) {
        this.totalPlayers = totalPlayers;
        saveAndSync();
    }

    public Timestamp getLastRestart() {
        return lastRestart;
    }

    public void setLastRestart(Timestamp lastRestart) {
        this .lastRestart = lastRestart;
        saveAndSync();
    }
}
