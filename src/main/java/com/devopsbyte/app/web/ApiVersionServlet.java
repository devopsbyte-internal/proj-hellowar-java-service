package com.devopsbyte.app.web;

import com.devopsbyte.app.ReleaseInfo;
import com.devopsbyte.app.db.DbWriteResult;
import com.devopsbyte.app.db.VersionHitDao;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Stable JSON version endpoint intended to be fronted by Nginx (/api/version/{n} -> /hellowar/api/version/{n}).
 */
@WebServlet(urlPatterns = {"/api/version/*"})
public class ApiVersionServlet extends HttpServlet {

    private final VersionHitDao versionHitDao = new VersionHitDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestId = ApiUtil.newRequestId();
        ApiUtil.setJsonHeaders(resp, requestId);

        String pathInfo = req.getPathInfo(); // "/1"
        int requestedVersion = parseVersion(pathInfo);

        if (requestedVersion < 1 || requestedVersion > 5) {
            String body = "{"
                    + "\"error\":\"Invalid version. Use /api/version/1..5\","
                    + "\"route\":\"" + ApiUtil.j(req.getRequestURI()) + "\","
                    + "\"timestamp\":\"" + ApiUtil.j(ApiUtil.nowIso()) + "\","
                    + "\"requestId\":\"" + ApiUtil.j(requestId) + "\""
                    + "}";
            ApiUtil.writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, body);
            return;
        }

        String route = req.getRequestURI();
        String timestamp = ApiUtil.nowIso();

        String appVersion = ReleaseInfo.getAppVersion();
        int releaseNumber = ReleaseInfo.getReleaseNumber();

        boolean activeHere = (releaseNumber == requestedVersion);
        boolean notYetDeployed = (releaseNumber < requestedVersion);
        boolean olderRelease = (releaseNumber > requestedVersion);

        String status;
        if (activeHere) status = "ACTIVE_HERE";
        else if (notYetDeployed) status = "NOT_YET_DEPLOYED";
        else status = "OLDER_RELEASE";

        String userAgent = req.getHeader("User-Agent");

        DbWriteResult db = versionHitDao.logVersionHit(
                requestedVersion,
                appVersion,
                releaseNumber,
                requestId,
                userAgent
        );

        String body = "{"
                + "\"requestedVersion\":" + requestedVersion + ","
                + "\"status\":\"" + ApiUtil.j(status) + "\","
                + "\"route\":\"" + ApiUtil.j(route) + "\","
                + "\"appVersion\":\"" + ApiUtil.j(appVersion) + "\","
                + "\"releaseNumber\":" + releaseNumber + ","
                + "\"timestamp\":\"" + ApiUtil.j(timestamp) + "\","
                + "\"requestId\":\"" + ApiUtil.j(requestId) + "\","
                + "\"db\":" + dbJson(db) + ","
                + "\"warnings\":" + warningsArray(db)
                + "}";

        ApiUtil.writeJson(resp, HttpServletResponse.SC_OK, body);
    }

    private int parseVersion(String pathInfo) {
        if (pathInfo == null) return -1;
        String v = pathInfo;
        if (v.startsWith("/")) v = v.substring(1);
        if (v.isBlank()) return -1;
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String dbJson(DbWriteResult db) {
        return "{"
                + "\"enabled\":" + (db.isEnabled() ? "true" : "false") + ","
                + "\"ok\":" + (db.isOk() ? "true" : "false") + ","
                + "\"warning\":" + (db.getWarning() == null ? "null" : ("\"" + ApiUtil.j(db.getWarning()) + "\""))
                + "}";
    }

    private String warningsArray(DbWriteResult db) {
        if (db.isEnabled() && !db.isOk() && db.getWarning() != null) {
            return "[\"" + ApiUtil.j(db.getWarning()) + "\"]";
        }
        return "[]";
    }
}
