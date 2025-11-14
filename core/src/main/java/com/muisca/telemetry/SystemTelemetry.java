package com.muisca.telemetry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.TimeUtils;
import com.muisca.economy.Season;
import java.util.Locale;

/**
 * Lightweight CSV logger for system-level telemetry events used in diagnostics and balancing.
 * Writes lines with the format:
 *   timestamp,event,details
 * Where timestamp is seconds since session start.
 */
public final class SystemTelemetry {

    private final FileHandle logFile;
    private final long sessionStartMillis;
    private boolean headerWritten = false;
    private float lastRegenScale = 1f;
    private boolean lastRaining = false;
    private float lastRainIntensity = 0f;
    private Season lastFarmSeason = null;
    private int lastFarmFallow = -1;
    private int lastFarmGrowing = -1;
    private int lastFarmReady = -1;
    private float lastFarmAvg = -1f;

    public SystemTelemetry(FileHandle logFile) {
        this.logFile = logFile;
        this.sessionStartMillis = TimeUtils.millis();
    }

    private float nowSeconds() {
        return (TimeUtils.millis() - sessionStartMillis) / 1000f;
    }

    private void ensureHeader() {
        if (logFile == null || headerWritten) return;
        if (!logFile.exists() || logFile.length() == 0) {
            logFile.writeString("timestamp,event,details\n", true, "UTF-8");
            try {
                Gdx.app.log("System", "CSV path: " + logFile.file().getAbsolutePath());
            } catch (Throwable t) {
                Gdx.app.log("System", "CSV path: " + logFile.path());
            }
        }
        headerWritten = true;
    }

    public void log(String event, String details) {
        ensureHeader();
        float ts = nowSeconds();
        String line = String.format(Locale.US, "%.3f,%s,%s\n", ts, sanitize(event), sanitize(details));
        if (logFile != null) {
            logFile.writeString(line, true, "UTF-8");
        }
        Gdx.app.log("System", event + ": " + details);
    }

    public void logWeatherToggle(boolean raining, float intensity) {
        // avoid duplicate logs if toggle called repeatedly
        if (raining != lastRaining || Math.abs(intensity - lastRainIntensity) > 0.01f) {
            lastRaining = raining;
            lastRainIntensity = intensity;
            log("weather_toggle", "raining=" + raining + ", intensity=" + String.format(Locale.US, "%.2f", intensity));
        }
    }

    public void logRegenScale(float dayIntensity, boolean raining, float rainIntensity, float regenScale, float regrowMultiplier) {
        // only log on meaningful change to avoid spam
        if (Math.abs(regenScale - lastRegenScale) < 0.03f && raining == lastRaining && Math.abs(rainIntensity - lastRainIntensity) < 0.03f) {
            return;
        }
        lastRegenScale = regenScale;
        lastRaining = raining;
        lastRainIntensity = rainIntensity;
        String details = String.format(Locale.US,
                "day=%.2f, raining=%s, rain=%.2f, regenScale=%.3f, regrowX=%.2f",
                dayIntensity, raining, rainIntensity, regenScale, regrowMultiplier);
        log("regen_env_update", details);
    }

    public void logFarmStatus(Season season, int fallow, int growing, int ready, float avgProgress) {
        Season safeSeason = season == null ? Season.TEMPERATE : season;
        if (safeSeason == lastFarmSeason
                && fallow == lastFarmFallow
                && growing == lastFarmGrowing
                && ready == lastFarmReady
                && Math.abs(avgProgress - lastFarmAvg) < 0.02f) {
            return;
        }
        lastFarmSeason = safeSeason;
        lastFarmFallow = fallow;
        lastFarmGrowing = growing;
        lastFarmReady = ready;
        lastFarmAvg = avgProgress;
        String details = String.format(Locale.US,
                "season=%s, fallow=%d, growing=%d, ready=%d, avg=%.3f",
                safeSeason.name(), fallow, growing, ready, avgProgress);
        log("farm_status", details);
    }

    private String sanitize(String s) {
        if (s == null) return "";
        // escape quotes for CSV, and wrap if contains commas
        if (s.contains(",") || s.contains("\"")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}