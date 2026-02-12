package com.devopsbyte.app.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Central place to obtain JDBC connections when the external DB is enabled.
 *
 * Defensive by design:
 *  - If DbConfig.isEnabled() is false, DB operations are treated as disabled.
 *  - If DB is enabled but credentials are missing, the DB is treated as unusable.
 *  - If the JDBC driver is missing, we log loudly but do not break the webapp.
 *
 * Callers should use isDbUsable() to decide if DB operations should be attempted.
 */
public final class DatabaseManager {

    private static volatile boolean initialized = false;
    private static boolean driverAvailable = false;

    private DatabaseManager() {
        // utility
    }

    private static synchronized void init() {
        if (initialized) {
            return;
        }

        if (!DbConfig.isEnabled()) {
            // Nothing to do; DB is logically disabled.
            initialized = true;
            driverAvailable = false;
            return;
        }

        try {
            // PostgreSQL driver; if not on the classpath this will fail.
            Class.forName("org.postgresql.Driver");
            driverAvailable = true;
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseManager] PostgreSQL JDBC driver not found on classpath. " +
                    "External DB will be treated as unusable.");
            e.printStackTrace(System.err);
            driverAvailable = false;
        } finally {
            initialized = true;
        }
    }

    private static void ensureInitialized() {
        if (!initialized) {
            init();
        }
    }

    /**
     * Whether DB operations can be attempted:
     *  - DB is enabled
     *  - credentials are present
     *  - driver is available
     */
    public static boolean isDbUsable() {
        ensureInitialized();
        return DbConfig.isEnabled() && DbConfig.areCredentialsPresent() && driverAvailable;
    }

    /**
     * Obtain a new JDBC connection to the configured database.
     *
     * @throws IllegalStateException if the DB is not considered usable.
     * @throws SQLException          if the underlying DriverManager cannot open a connection.
     */
    public static Connection getConnection() throws SQLException {
        ensureInitialized();

        if (!isDbUsable()) {
            String warn = DbConfig.getConfigWarningIfAny();
            if (warn != null) {
                throw new IllegalStateException(warn);
            }
            throw new IllegalStateException("Database is not enabled, not configured, or driver not available.");
        }

        return DriverManager.getConnection(
                DbConfig.getUrl(),
                DbConfig.getUser(),
                DbConfig.getPassword()
        );
    }
}
