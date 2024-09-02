package it.crescentsun.crescentcore.core.data;

import it.crescentsun.crescentcore.CrescentCore;
import it.crescentsun.crescentcore.api.data.DataType;
import it.crescentsun.crescentcore.api.data.plugin.DatabaseColumn;
import it.crescentsun.crescentcore.api.data.plugin.DatabaseTable;
import it.crescentsun.crescentcore.api.data.plugin.PluginData;

import java.sql.Timestamp;

@DatabaseTable(tableName = "server_stats", plugin = CrescentCore.class)
public class ServerStats extends PluginData {

    @DatabaseColumn(columnName = "server", dataType = DataType.VARCHAR_16, isPrimaryKey = true)
    private String server = "server";

    @DatabaseColumn(columnName = "last_restart", dataType = DataType.TIMESTAMP)
    private Timestamp lastRestart;

    @DatabaseColumn(columnName = "total_players", dataType = DataType.UNSIGNED_INT)
    private int totalPlayers;

    @DatabaseColumn(columnName = "most_players_online", dataType = DataType.UNSIGNED_INT)
    private int mostPlayersOnline;

    public ServerStats() {
        this.lastRestart = new Timestamp(System.currentTimeMillis());
        this.totalPlayers = 0;
        this.mostPlayersOnline = 0;
    }

    public Timestamp getLastRestart() {
        return lastRestart;
    }

    public void setLastRestart(Timestamp lastRestart) {
        this.lastRestart = lastRestart;
    }

    public int getTotalPlayers() {
        return totalPlayers;
    }

    public void setTotalPlayers(int totalPlayers) {
        this.totalPlayers = totalPlayers;
    }

    public int getMostPlayersOnline() {
        return mostPlayersOnline;
    }

}
