package com.devopsbyte.app.db;

/**
 * External database configuration sourced from environment variables.
 *
 * Backward compatible behaviour:
 *  - If DB_ENABLED is NOT set, DB is enabled when all DB_URL/DB_USER/DB_PASSWORD are present (legacy).
 *  - If DB_ENABLED IS set, its boolean value becomes the ON/OFF switch.
 *
 * Even when DB_ENABLED=true, the application must remain resilient:
 *  - Missing credentials or connectivity issues must not break HTTP responses.
 *  - Callers can inspect status via helper methods for warnings/diagnostics.
 */
public final class DbConfig {

    private static final String url;
    private static final String user;
    private static final String password;

    private static final Boolean enabledFlag; // null when DB_ENABLED is absent
    private static final boolean enabled;
    private static final boolean credentialsPresent;

    static {
        url = trimOrNull(System.getenv("DB_URL"));
        user = trimOrNull(System.getenv("DB_USER"));
        password = trimOrNull(System.getenv("DB_PASSWORD"));

        credentialsPresent = (url != null && user != null && password != null);

        String rawEnabled = trimOrNull(System.getenv("DB_ENABLED"));
        if (rawEnabled == null) {
            enabledFlag = null; // legacy mode
            enabled = credentialsPresent;
        } else {
            enabledFlag = parseBooleanLenient(rawEnabled);
            enabled = Boolean.TRUE.equals(enabledFlag);
        }
    }

    private DbConfig() {
        // utility
    }

    /** Whether DB mode is logically enabled (may still be unusable due to creds/driver/connectivity). */
    public static boolean isEnabled() {
        return enabled;
    }

    /** Whether DB credentials are present and non-blank. */
    public static boolean areCredentialsPresent() {
        return credentialsPresent;
    }

    /** Whether DB_ENABLED was explicitly set in the environment. */
    public static boolean isEnabledFlagExplicit() {
        return enabledFlag != null;
    }

    /** If DB is enabled but credentials are missing, return a short warning message; otherwise null. */
    public static String getConfigWarningIfAny() {
        if (enabled && !credentialsPresent) {
            return "DB_ENABLED=true but DB_URL/DB_USER/DB_PASSWORD are missing.";
        }
        return null;
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

    /**
     * Accepts: true/false (case-insensitive), 1/0, yes/no, y/n.
     * Anything else is treated as false to avoid accidental enabling.
     */
    private static Boolean parseBooleanLenient(String raw) {
        String v = raw.trim().toLowerCase();
        if (v.equals("true") || v.equals("1") || v.equals("yes") || v.equals("y")) {
            return Boolean.TRUE;
        }
        if (v.equals("false") || v.equals("0") || v.equals("no") || v.equals("n")) {
            return Boolean.FALSE;
        }
        return Boolean.FALSE;
    }
}
