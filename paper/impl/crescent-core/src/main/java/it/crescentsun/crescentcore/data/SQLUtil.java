package it.crescentsun.crescentcore.data;

import java.util.Map;

/**
 * Utility class for building common SQL queries used by the data managers.
 */
public final class SQLUtil {

    private SQLUtil() { }

    /**
     * Builds an INSERT ... ON DUPLICATE KEY UPDATE query for the given table.
     * The map provided should contain the column name as key and whether the
     * column is the primary key as value. The order of the map entries is
     * preserved when constructing the query, therefore a LinkedHashMap is
     * recommended.
     */
    public static String buildInsertOrUpdateQuery(String tableName, Map<String, Boolean> columns) {
        StringBuilder columnsPart = new StringBuilder();
        StringBuilder valuesPart = new StringBuilder();
        StringBuilder updatePart = new StringBuilder();

        for (Map.Entry<String, Boolean> entry : columns.entrySet()) {
            String column = entry.getKey();
            boolean isPrimary = entry.getValue();

            columnsPart.append(column).append(", ");
            valuesPart.append("?, ");
            if (!isPrimary) {
                updatePart.append(column).append(" = VALUES(").append(column).append(")").append(", ");
            }
        }

        if (columnsPart.length() > 0) columnsPart.setLength(columnsPart.length() - 2);
        if (valuesPart.length() > 0) valuesPart.setLength(valuesPart.length() - 2);
        if (updatePart.length() > 0) updatePart.setLength(updatePart.length() - 2);

        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO ").append(tableName).append(" (")
                .append(columnsPart).append(") VALUES (")
                .append(valuesPart).append(")");
        if (updatePart.length() > 0) {
            query.append(" ON DUPLICATE KEY UPDATE ").append(updatePart);
        }
        return query.toString();
    }

    /**
     * Builds a simple SELECT query using the given table name and primary key column.
     */
    public static String buildSelectQuery(String tableName, String primaryKeyColumn) {
        return "SELECT * FROM " + tableName + " WHERE " + primaryKeyColumn + " = ?";
    }
}