package com.devopsbyte.app;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Central place to expose:
 *  - app version (from app.properties, injected by Maven)
 *  - release number (from env var RELEASE_NUMBER, with a safe default)
 *
 * This is read once at class-load time and then cached.
 */
public final class ReleaseInfo {

    private static final String DEFAULT_VERSION = "UNKNOWN";
    private static final int DEFAULT_RELEASE = 1;
    private static final String PROPERTIES_FILE = "app.properties";

    private static final String appVersion;
    private static final int releaseNumber;

    static {
        appVersion = loadVersion();
        releaseNumber = loadReleaseNumber();
    }

    private ReleaseInfo() {
        // utility class, no instances
    }

    private static String loadVersion() {
        Properties props = new Properties();
        try (InputStream in = ReleaseInfo.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (in != null) {
                props.load(in);
                String v = props.getProperty("app.version");
                if (v != null && !v.isBlank()) {
                    return v.trim();
                }
            }
        } catch (IOException e) {
            // swallow, use default
        }
        return DEFAULT_VERSION;
    }

    private static int loadReleaseNumber() {
        String fromEnv = System.getenv("RELEASE_NUMBER");
        if (fromEnv != null) {
            try {
                int parsed = Integer.parseInt(fromEnv.trim());
                if (parsed > 0) {
                    return parsed;
                }
            } catch (NumberFormatException ignored) {
                // fall through to default
            }
        }
        return DEFAULT_RELEASE;
    }

    public static String getAppVersion() {
        return appVersion;
    }

    public static int getReleaseNumber() {
        return releaseNumber;
    }
}
