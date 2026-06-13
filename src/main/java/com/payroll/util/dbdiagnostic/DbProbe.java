package com.payroll.util.dbdiagnostic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.Set;

/**
 * Standalone, read-only database diagnostic utility.
 *
 * <p>This class is intentionally not a Spring component and is not exposed by
 * any service or controller. Run it manually only when database inspection is
 * required.</p>
 */
public final class DbProbe {

    private static final String ENABLE_PROPERTY = "db.probe.enabled";
    private static final Set<String> ALLOWED_STATEMENTS =
            Set.of("SELECT", "SHOW", "DESCRIBE", "DESC", "EXPLAIN");

    private DbProbe() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void main(String[] args) throws SQLException {
        if (!Boolean.getBoolean(ENABLE_PROPERTY)) {
            throw new IllegalStateException(
                    "DbProbe is disabled. Run manually with -D" + ENABLE_PROPERTY + "=true");
        }
        if (args.length != 4) {
            throw new IllegalArgumentException(
                    "Usage: DbProbe <jdbcUrl> <username> <password> <readOnlySql>");
        }

        String sql = args[3].trim();
        validateReadOnlySql(sql);

        try (Connection connection = DriverManager.getConnection(args[0], args[1], args[2])) {
            connection.setReadOnly(true);
            printQuery(connection, sql);
        }
    }

    private static void validateReadOnlySql(String sql) {
        if (sql.isBlank()) {
            throw new IllegalArgumentException("SQL is required");
        }

        String command = sql.split("\\s+", 2)[0].toUpperCase(Locale.ROOT);
        if (!ALLOWED_STATEMENTS.contains(command)) {
            throw new IllegalArgumentException(
                    "Only read-only diagnostic SQL is allowed: " + ALLOWED_STATEMENTS);
        }
    }

    private static void printQuery(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            ResultSetMetaData metadata = resultSet.getMetaData();

            while (resultSet.next()) {
                for (int column = 1; column <= metadata.getColumnCount(); column++) {
                    System.out.printf("%s: %s%n",
                            metadata.getColumnLabel(column), resultSet.getString(column));
                }
                System.out.println();
            }
        }
    }
}
