package it.klynet.klynetcore.core.data;

import it.klynet.klynetcore.KlyNetCore;
import it.klynet.klynetcore.plugindata.DataType;

import java.sql.*;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static it.klynet.klynetcore.api.registry.KlyNetNamespaceKeys.SERVER_KEY;

public class ServerDataManager {

    private final Map<String, DataType> columns = Map.of(
            "crystals_generated", DataType.INT,
            "players_joined", DataType.INT,
            "last_launch", DataType.TIMESTAMP
    );

    private final KlyNetCore klyNetCore;
    private final DatabaseManager dbManager;
    private boolean tableCreated = false;
    private boolean tableUpdated = false;

    public ServerDataManager(KlyNetCore klyNetCore, DatabaseManager dbManager) {
        this.klyNetCore = klyNetCore;
        this.dbManager = dbManager;
    }

    protected void createServerTable() {
        if (tableCreated) {
            klyNetCore.getLogger().warning("Second attempt to create the server table. Ignoring...");
            return;
        }
        try (Connection connection = dbManager.getConnection()) {
            StringBuilder query = new StringBuilder("CREATE TABLE IF NOT EXISTS " + SERVER_KEY + " (");
            query.append(SERVER_KEY).append(" VARCHAR(16) NOT NULL PRIMARY KEY, ");
            for (String columnName : columns.keySet()) {
                query.append(columnName).append(" ").append(columns.get(columnName).getSqlType()).append(", ");
            }
            query.deleteCharAt(query.length() - 2).append(");");
            System.out.println("QUIERY: " + query);
            connection.createStatement().executeUpdate(query.toString());
            // Check if the table is empty and insert the default row if necessary
            ResultSet rs = connection.createStatement().executeQuery("SELECT COUNT(*) AS rowcount FROM " + SERVER_KEY);
            rs.next();
            int count = rs.getInt("rowcount");
            if (count == 0) {
                // Assuming you have a method to build the default values insertion query
                String insertQuery = "INSERT INTO " + SERVER_KEY + " (server) VALUE ('server')";
                connection.createStatement().executeUpdate(insertQuery);
            }

            klyNetCore.getLogger().info("Created " + SERVER_KEY + " table");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void updateServerTable() {
        if (tableUpdated) {
            klyNetCore.getLogger().warning("Second attempt to update the server table caught. Ignoring...");
            return;
        }
        try (Connection connection = dbManager.getConnection()) {
            DatabaseMetaData dbMetaData = connection.getMetaData();

            for (String columnName : columns.keySet()) {
                try (ResultSet rs = dbMetaData.getColumns(null, null, SERVER_KEY, columnName)) {
                    if (rs.next()) {
                        continue;
                    }
                    String alterQuery = "ALTER TABLE " + SERVER_KEY + " ADD COLUMN " + columnName + " " +
                            columns.get(columnName).getSqlType();
                    connection.createStatement().executeUpdate(alterQuery);
                }
            }
            tableUpdated = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public CompletableFuture<Integer> getCrystalsGenerated() {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dbManager.getConnection()) {
                try (ResultSet rs = connection.createStatement().executeQuery("SELECT crystals_generated FROM " + SERVER_KEY)) {
                    if (rs.next()) {
                        return rs.getInt("crystals_generated");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return -1;
        });
    }

    public CompletableFuture<Void> incrementCrystalsGenerated(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dbManager.getConnection()) {
                String query = "UPDATE " + SERVER_KEY + " SET crystals_generated = crystals_generated + ?";
                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.setInt(1, amount);
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<Integer> getTotalPlayersJoined() {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dbManager.getConnection()) {
                try (ResultSet rs = connection.createStatement().executeQuery("SELECT players_joined FROM " + SERVER_KEY)) {
                    if (rs.next()) {
                        return rs.getInt("players_joined");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return -1;
        });
    }

    public CompletableFuture<Void> incrementPlayersJoined() {
        System.out.println("Incrementing...");
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dbManager.getConnection()) {
                String query = "UPDATE " + SERVER_KEY + " SET players_joined = players_joined + 1";
                connection.createStatement().executeUpdate(query);
                klyNetCore.getLogger().info("Hurray! A new player has joined the server!");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<Timestamp> getLastLaunch() {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dbManager.getConnection()) {
                try (ResultSet rs = connection.createStatement().executeQuery("SELECT last_launch FROM " + SERVER_KEY)) {
                    if (rs.next()) {
                        return rs.getTimestamp("last_launch");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public CompletableFuture<Void> setLastLaunch(Timestamp timestamp) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dbManager.getConnection()) {
                String query = "UPDATE " + SERVER_KEY + " SET last_launch = ?";
                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.setTimestamp(1, timestamp);
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

}
