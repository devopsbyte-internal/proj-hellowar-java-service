package com.devopsbyte.app.web;

import com.devopsbyte.app.ReleaseInfo;
import com.devopsbyte.app.db.DbHealth;
import com.devopsbyte.app.db.DbWriteResult;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Stable JSON health endpoint intended to be fronted by Nginx (/api/health -> /hellowar/api/health).
 */
@WebServlet(urlPatterns = {"/api/health"})
public class ApiHealthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestId = ApiUtil.newRequestId();
        ApiUtil.setJsonHeaders(resp, requestId);

        String route = req.getRequestURI();
        String timestamp = ApiUtil.nowIso();

        String appVersion = ReleaseInfo.getAppVersion();
        int releaseNumber = ReleaseInfo.getReleaseNumber();

        DbWriteResult db = DbHealth.check();

        String warningsJson = warningsArray(db);

        String body = "{"
                + "\"status\":\"UP\","
                + "\"route\":\"" + ApiUtil.j(route) + "\","
                + "\"appVersion\":\"" + ApiUtil.j(appVersion) + "\","
                + "\"releaseNumber\":" + releaseNumber + ","
                + "\"timestamp\":\"" + ApiUtil.j(timestamp) + "\","
                + "\"requestId\":\"" + ApiUtil.j(requestId) + "\","
                + "\"db\":" + dbJson(db) + ","
                + "\"warnings\":" + warningsJson
                + "}";

        ApiUtil.writeJson(resp, HttpServletResponse.SC_OK, body);
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
