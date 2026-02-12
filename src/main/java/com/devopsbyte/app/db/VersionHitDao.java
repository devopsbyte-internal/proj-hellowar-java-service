package com.devopsbyte.app.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Best-effort telemetry logging for version hits.
 *
 * Never throws to callers; instead returns DbWriteResult so API responses can include warnings.
 */
public class VersionHitDao {

    private static final String INSERT_SQL =
            "INSERT INTO version_hit (version, app_version, release_number, request_id, user_agent) " +
            "VALUES (?, ?, ?, ?, ?)";

    public DbWriteResult logVersionHit(int version,
                                      String appVersion,
                                      int releaseNumber,
                                      String requestId,
                                      String userAgent) {

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

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {

            ps.setInt(1, version);
            ps.setString(2, appVersion != null ? appVersion : "");
            ps.setInt(3, releaseNumber);
            ps.setString(4, requestId != null ? requestId : "");
            ps.setString(5, userAgent != null ? userAgent : "");
            ps.executeUpdate();
            return DbWriteResult.ok();

        } catch (SQLException e) {
            System.err.println("[VersionHitDao] Failed to insert version_hit row: " + e.getMessage());
            e.printStackTrace(System.err);
            return DbWriteResult.warn("DB write failed: " + safeMsg(e.getMessage()));
        } catch (RuntimeException e) {
            System.err.println("[VersionHitDao] Unexpected runtime exception while logging: " + e.getMessage());
            e.printStackTrace(System.err);
            return DbWriteResult.warn("DB runtime error: " + safeMsg(e.getMessage()));
        }
    }

    private String safeMsg(String msg) {
        if (msg == null) {
            return "unknown";
        }
        String m = msg.replaceAll("[\r\n\t]+", " ").trim();
        return m.length() > 160 ? m.substring(0, 160) + "..." : m;
    }
}
