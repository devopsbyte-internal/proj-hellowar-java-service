package com.devopsbyte.app.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;

@WebServlet(urlPatterns = {"/health"})
public class HealthServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setStatus(HttpServletResponse.SC_OK);
        String body = String.format(
            "{\"status\":\"UP\",\"app\":\"hello-war\",\"timestamp\":\"%s\"}",
            Instant.now().toString()
        );
        resp.getWriter().write(body);
    }
}
