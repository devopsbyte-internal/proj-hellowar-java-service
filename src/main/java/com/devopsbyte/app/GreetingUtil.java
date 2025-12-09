
package com.devopsbyte.app;

public class GreetingUtil {
    /**
     * Compose a greeting using optional prefix (e.g., from env).
     */
    public static String greet(String name, String prefix) {
        String p = (prefix == null || prefix.isBlank()) ? "Hello" : prefix.trim();
        String n = (name == null || name.isBlank()) ? "world" : name.trim();
        return p + ", " + n + "!";
    }
}
