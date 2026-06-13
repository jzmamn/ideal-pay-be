package com.payroll.util.dbdiagnostic;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Standalone, read-only verifier for the table_registry table.
 *
 * <p>This class is intentionally not a Spring component and cannot be called
 * through the application. Run it manually when registry validation is
 * required.</p>
 */
public final class TableRegistryVerifier {

    private static final String ENABLE_PROPERTY = "table.registry.verifier.enabled";
    private static final Set<String> ALLOWED_TYPES = Set.of("CON", "MAS", "TXN", "TRL", "LOG");
    private static final Set<String> EXCLUDED_TABLES =
            Set.of("flyway_schema_history", "table_registry");

    private TableRegistryVerifier() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void main(String[] args) throws SQLException {
        if (!Boolean.getBoolean(ENABLE_PROPERTY)) {
            throw new IllegalStateException(
                    "TableRegistryVerifier is disabled. Run manually with -D"
                            + ENABLE_PROPERTY + "=true");
        }
        if (args.length != 3) {
            throw new IllegalArgumentException(
                    "Usage: TableRegistryVerifier <jdbcUrl> <username> <password>");
        }

        try (Connection connection = DriverManager.getConnection(args[0], args[1], args[2])) {
            connection.setReadOnly(true);
            VerificationResult result = verify(connection);
            result.print();

            if (!result.isValid()) {
                throw new IllegalStateException("table_registry verification failed");
            }
        }
    }

    static VerificationResult verify(Connection connection) throws SQLException {
        String schema = connection.getCatalog();
        ensureRegistryExists(connection, schema);

        List<String> missingPhysicalTables = queryNames(connection, """
                SELECT tr.table_name
                FROM table_registry tr
                LEFT JOIN information_schema.tables t
                       ON t.table_schema = ?
                      AND t.table_name = tr.table_name
                      AND t.table_type = 'BASE TABLE'
                WHERE t.table_name IS NULL
                ORDER BY tr.table_name
                """, schema);

        List<String> unregisteredTables = queryNames(connection, """
                SELECT t.table_name
                FROM information_schema.tables t
                LEFT JOIN table_registry tr ON tr.table_name = t.table_name
                WHERE t.table_schema = ?
                  AND t.table_type = 'BASE TABLE'
                  AND tr.table_name IS NULL
                  AND t.table_name NOT IN ('flyway_schema_history', 'table_registry')
                ORDER BY t.table_name
                """, schema);

        List<String> invalidTypes = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT table_name, table_type
                FROM table_registry
                ORDER BY table_name
                """);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String type = resultSet.getString("table_type");
                if (type == null || !ALLOWED_TYPES.contains(type.trim().toUpperCase())) {
                    invalidTypes.add(resultSet.getString("table_name") + "=" + type);
                }
            }
        }

        return new VerificationResult(schema, missingPhysicalTables, unregisteredTables, invalidTypes);
    }

    private static void ensureRegistryExists(Connection connection, String schema) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        try (ResultSet tables = metadata.getTables(schema, null, "table_registry", new String[]{"TABLE"})) {
            if (!tables.next()) {
                throw new IllegalStateException("table_registry does not exist in schema " + schema);
            }
        }
    }

    private static List<String> queryNames(Connection connection, String sql, String schema)
            throws SQLException {
        List<String> names = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, schema);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String tableName = resultSet.getString(1);
                    if (!EXCLUDED_TABLES.contains(tableName)) {
                        names.add(tableName);
                    }
                }
            }
        }
        return names;
    }

    record VerificationResult(
            String schema,
            List<String> missingPhysicalTables,
            List<String> unregisteredTables,
            List<String> invalidTypes) {

        boolean isValid() {
            return missingPhysicalTables.isEmpty()
                    && unregisteredTables.isEmpty()
                    && invalidTypes.isEmpty();
        }

        void print() {
            System.out.println("table_registry verification for schema: " + schema);
            printItems("Registered names without physical tables", missingPhysicalTables);
            printItems("Physical tables missing from registry", unregisteredTables);
            printItems("Entries with invalid table types", invalidTypes);
            System.out.println(isValid() ? "Result: VALID" : "Result: INVALID");
        }

        private void printItems(String label, List<String> items) {
            System.out.println(label + ": " + items.size());
            items.forEach(item -> System.out.println("  - " + item));
        }
    }
}
