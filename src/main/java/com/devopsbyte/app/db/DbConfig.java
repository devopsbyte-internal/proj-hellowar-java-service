package com.devopsbyte.app.db;

/**
 * Simple holder for external database configuration sourced from environment variables.
 *
 * The presence of all three variables:
 *   - DB_URL
 *   - DB_USER
 *   - DB_PASSWORD
 *
 * is treated as the switch that turns "external DB mode" ON.
 *
 * If any of them is missing or blank, the DB is considered disabled and the
 * rest of the application must continue to work without touching the database.
 */
public final class DbConfig {

    private static final String url;
    private static final String user;
    private static final String password;
    private static final boolean enabled;

    static {
        url = trimOrNull(System.getenv("DB_URL"));
        user = trimOrNull(System.getenv("DB_USER"));
        password = trimOrNull(System.getenv("DB_PASSWORD"));

        enabled = url != null && user != null && password != null;
    }

    private DbConfig() {
        // utility
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static String getUrl() {
        return url;
    }

    public static String getUser() {
        return user;
    }

    public static String getPassword() {
        return password;
    }

    private static String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
