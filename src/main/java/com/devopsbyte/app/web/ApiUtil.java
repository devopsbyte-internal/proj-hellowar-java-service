package com.devopsbyte.app.web;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

/**
 * Small utility for consistent API responses without adding JSON dependencies.
 */
public final class ApiUtil {

    private ApiUtil() {}

    public static String newRequestId() {
        return UUID.randomUUID().toString();
    }

    public static String nowIso() {
        return Instant.now().toString();
    }

    public static void setJsonHeaders(HttpServletResponse resp, String requestId) {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-store");
        if (requestId != null && !requestId.isBlank()) {
            resp.setHeader("X-Request-Id", requestId);
        }
    }

    public static void writeJson(HttpServletResponse resp, int status, String jsonBody) throws IOException {
        resp.setStatus(status);
        resp.getWriter().write(jsonBody);
    }

    /** Minimal JSON string escaper. */
    public static String j(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"': sb.append("\\""); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int)c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}
