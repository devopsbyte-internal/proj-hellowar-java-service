package com.devopsbyte.app.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Central place to obtain JDBC connections when the external DB is enabled.
 *
 * This class is defensive by design:
 *  - If DbConfig.isEnabled() is false, any attempt to get a Connection will
 *    throw an IllegalStateException.
 *  - If the JDBC driver is missing or misconfigured, we log to stderr and
 *    effectively treat the DB as disabled, so that the webapp can still run.
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
            // Be loud in logs, but do NOT break application startup.
            System.err.println("[DatabaseManager] PostgreSQL JDBC driver not found on classpath. " +
                    "External DB will be treated as disabled.");
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
     * Whether the DB is both configured (DbConfig.isEnabled) and the driver is available.
     */
    public static boolean isDbUsable() {
        ensureInitialized();
        return DbConfig.isEnabled() && driverAvailable;
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
            throw new IllegalStateException("Database is not enabled or driver not available.");
        }

        return DriverManager.getConnection(
                DbConfig.getUrl(),
                DbConfig.getUser(),
                DbConfig.getPassword()
        );
    }
}
