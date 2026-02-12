package com.devopsbyte.app.db;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Performs a lightweight DB validation check for API health reporting.
 * This is best-effort and should never break HTTP responses.
 */
public final class DbHealth {

    private DbHealth() {}

    public static DbWriteResult check() {
        if (!DbConfig.isEnabled()) {
            return DbWriteResult.disabled();
        }

        String configWarn = DbConfig.getConfigWarningIfAny();
        if (configWarn != null) {
            return DbWriteResult.warn(configWarn);
        }

        if (!DatabaseManager.isDbUsable()) {
            return DbWriteResult.warn("DB is enabled but not usable (driver/credentials missing).");
        }

        try (Connection conn = DatabaseManager.getConnection()) {
            boolean valid = false;
            try {
                valid = conn.isValid(2);
            } catch (SQLException ignored) {
                // fall through
            }
            if (valid) {
                return DbWriteResult.ok();
            }
            return DbWriteResult.warn("DB connection opened but isValid() returned false.");
        } catch (SQLException | RuntimeException e) {
            return DbWriteResult.warn("DB connectivity check failed: " + safeMsg(e.getMessage()));
        }
    }

    private static String safeMsg(String msg) {
        if (msg == null) {
            return "unknown";
        }
        // avoid returning overly long/unsafe messages
        String m = msg.replaceAll("[\r\n\t]+", " ").trim();
        return m.length() > 160 ? m.substring(0, 160) + "..." : m;
    }
}
