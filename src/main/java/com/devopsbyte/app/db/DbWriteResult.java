package com.devopsbyte.app.db;

/** Small value object to report DB write outcome back to API layers. */
public final class DbWriteResult {
    private final boolean enabled;
    private final boolean ok;
    private final String warning; // nullable, short

    private DbWriteResult(boolean enabled, boolean ok, String warning) {
        this.enabled = enabled;
        this.ok = ok;
        this.warning = warning;
    }

    public static DbWriteResult disabled() {
        return new DbWriteResult(false, true, null);
    }

    public static DbWriteResult ok() {
        return new DbWriteResult(true, true, null);
    }

    public static DbWriteResult warn(String warning) {
        return new DbWriteResult(true, false, warning);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isOk() {
        return ok;
    }

    public String getWarning() {
        return warning;
    }
}
