package com.muisca.telemetry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.TimeUtils;
import java.util.Locale;

/**
 * CSV logger for inventory events: add and consume of items.
 * Columns: timestamp,action,item,amount,context
 */
public final class InventoryTelemetry {

    private final FileHandle logFile;
    private final long sessionStartMillis;
    private boolean headerWritten = false;

    public InventoryTelemetry(FileHandle logFile) {
        this.logFile = logFile;
        this.sessionStartMillis = TimeUtils.millis();
    }

    private float nowSeconds() {
        return (TimeUtils.millis() - sessionStartMillis) / 1000f;
    }

    private void ensureHeader() {
        if (logFile == null || headerWritten) return;
        if (!logFile.exists() || logFile.length() == 0) {
            logFile.writeString("timestamp,action,item,amount,context\n", true, "UTF-8");
            try {
                Gdx.app.log("Inventory", "CSV path: " + logFile.file().getAbsolutePath());
            } catch (Throwable t) {
                Gdx.app.log("Inventory", "CSV path: " + logFile.path());
            }
        }
        headerWritten = true;
    }

    public void logAdd(String itemId, int amount) {
        logAdd(itemId, amount, "");
    }

    public void logAdd(String itemId, int amount, String context) {
        ensureHeader();
        float ts = nowSeconds();
        String line = String.format(Locale.US, "%.3f,add,%s,%d,%s\n",
                ts, sanitize(itemId), amount, sanitize(context));
        write(line);
        Gdx.app.log("Inventory", "add " + itemId + " x" + amount + (context == null || context.isEmpty() ? "" : " (" + context + ")"));
    }

    public void logConsume(String itemId, int amount) {
        logConsume(itemId, amount, "");
    }

    public void logConsume(String itemId, int amount, String context) {
        ensureHeader();
        float ts = nowSeconds();
        String line = String.format(Locale.US, "%.3f,consume,%s,%d,%s\n",
                ts, sanitize(itemId), amount, sanitize(context));
        write(line);
        Gdx.app.log("Inventory", "consume " + itemId + " x" + amount + (context == null || context.isEmpty() ? "" : " (" + context + ")"));
    }

    private void write(String line) {
        if (logFile != null) {
            logFile.writeString(line, true, "UTF-8");
        }
    }

    private String sanitize(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}