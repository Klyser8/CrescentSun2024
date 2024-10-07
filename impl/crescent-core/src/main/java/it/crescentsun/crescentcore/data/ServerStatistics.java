package it.crescentsun.crescentcore.data;

import it.crescentsun.api.crescentcore.data.DataType;
import it.crescentsun.api.crescentcore.data.plugin.DatabaseColumn;
import it.crescentsun.api.crescentcore.data.plugin.DatabaseTable;
import it.crescentsun.api.crescentcore.data.plugin.SingletonPluginData;
import it.crescentsun.crescentcore.CrescentCore;

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

    @Override
    public boolean isProxyDependent() {
        return false;
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
        this.lastRestart = lastRestart;
        saveAndSync();
    }
}
