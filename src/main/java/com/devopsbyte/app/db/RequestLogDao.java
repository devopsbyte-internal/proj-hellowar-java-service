package com.devopsbyte.app.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Very small DAO used to optionally log /hello requests when an external DB is configured.
 *
 * Important behaviour:
 *  - If the DB is not usable, all methods return immediately (no-op).
 *  - SQL failures are logged to stderr but never propagated back to the servlet,
 *    so HTTP responses are not impacted by DB issues.
 *
 * Schema suggested for the backing table (PostgreSQL):
 *
 *   CREATE TABLE request_log (
 *       id BIGSERIAL PRIMARY KEY,
 *       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 *       path VARCHAR(255),
 *       remote_addr VARCHAR(64),
 *       app_env VARCHAR(64),
 *       message TEXT
 *   );
 */
public class RequestLogDao {

    private static final String INSERT_SQL =
            "INSERT INTO request_log (path, remote_addr, app_env, message) VALUES (?, ?, ?, ?)";

    public void logHelloRequest(String path,
                                String remoteAddr,
                                String appEnv,
                                String message) {
        if (!DatabaseManager.isDbUsable()) {
            // External DB is not configured or not available; fail silently.
            return;
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {

            ps.setString(1, path);
            ps.setString(2, remoteAddr);
            ps.setString(3, appEnv != null ? appEnv : "");
            ps.setString(4, message != null ? message : "");
            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[RequestLogDao] Failed to insert request_log row: " + e.getMessage());
            e.printStackTrace(System.err);
        } catch (RuntimeException e) {
            System.err.println("[RequestLogDao] Unexpected runtime exception while logging request: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}
