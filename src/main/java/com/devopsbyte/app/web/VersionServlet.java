package com.devopsbyte.app.web;

import com.devopsbyte.app.ReleaseInfo;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Exposes 5 endpoints:
 *   /hello/version1
 *   /hello/version2
 *   /hello/version3
 *   /hello/version4
 *   /hello/version5
 *
 * Each endpoint uses:
 *   - ReleaseInfo.getReleaseNumber()
 *   - ReleaseInfo.getAppVersion()
 *
 * to decide whether this version is ACTIVE, NOT YET DEPLOYED, or an OLDER release.
 */
@WebServlet(
        name = "VersionServlet",
        urlPatterns = {
                "/version1",
                "/version2",
                "/version3",
                "/version4",
                "/version5"
        }
)
public class VersionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException {

        String servletPath = req.getServletPath(); // e.g. "/hello/version2"
        int version = extractVersionNumber(servletPath);

        if (version < 1 || version > 5) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("text/plain;charset=UTF-8");
            resp.getWriter().println("Invalid version endpoint.");
            return;
        }

        int releaseNumber = ReleaseInfo.getReleaseNumber();
        String appVersion = ReleaseInfo.getAppVersion();

        boolean activeHere = (releaseNumber == version);
        boolean notYetDeployed = (releaseNumber < version);
        boolean olderRelease = (releaseNumber > version);

        resp.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.println("<!doctype html>");
            out.println("<html>");
            out.println("<head>");
            out.println("  <meta charset=\"UTF-8\">");
            out.println("  <title>Hello WAR - Version " + version + "</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Hello WAR — Version " + version + "</h1>");
            out.println("<p>Artifact version: <strong>" + escape(appVersion) + "</strong></p>");
            out.println("<p>Current release number: <strong>" + releaseNumber + "</strong></p>");

            if (activeHere) {
                out.println("<p style=\"color: green; font-weight: bold;\">");
                out.println("This version is <strong>ACTIVE HERE</strong>.");
                out.println("</p>");
            } else if (notYetDeployed) {
                out.println("<p style=\"color: orange; font-weight: bold;\">");
                out.println("This version is <strong>NOT YET DEPLOYED</strong>.");
                out.println("</p>");
            } else if (olderRelease) {
                out.println("<p style=\"color: gray; font-weight: bold;\">");
                out.println("This version belongs to an <strong>OLDER RELEASE</strong>.");
                out.println("</p>");
            }

            out.println("<hr>");
            out.println("<p><a href=\"" + req.getContextPath() + "/\">Back to index</a></p>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    private int extractVersionNumber(String servletPath) {
        if (servletPath == null) {
            return -1;
        }
        // Expect patterns like "/hello/version1"
        int idx = servletPath.lastIndexOf("version");
        if (idx == -1) {
            return -1;
        }
        String suffix = servletPath.substring(idx + "version".length()); // "1", "2", ...
        try {
            return Integer.parseInt(suffix);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String escape(String value) {
        if (value == null) return "";
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
