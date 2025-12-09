package com.devopsbyte.app.web;

import com.devopsbyte.app.GreetingUtil;
import com.devopsbyte.app.db.RequestLogDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Simple servlet demonstrating Jakarta API and externalized config readiness.
 *
 * Behaviour:
 *  - Reads optional env var APP_GREETING as a prefix.
 *  - Returns a plain-text greeting.
 *  - Optionally logs the request to an external DB if DB_* env vars are present.
 *
 * The logging is completely fire-and-forget: if the DB is not configured or is
 * unavailable, the HTTP response is still returned successfully.
 */
@WebServlet(urlPatterns = {"/hello"})
public class HelloServlet extends HttpServlet {

    private final RequestLogDao logDao = new RequestLogDao();

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException {

        String name = req.getParameter("name");
        String prefix = System.getenv("APP_GREETING");
        String message = GreetingUtil.greet(name, prefix);

        resp.setContentType("text/plain;charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.println(message);
        }

        // Fire-and-forget logging to external DB (if configured)
        String path = req.getRequestURI();
        String remoteAddr = req.getRemoteAddr();
        String appEnv = System.getenv("APP_ENV"); // optional; may be null

        logDao.logHelloRequest(path, remoteAddr, appEnv, message);
    }
}
