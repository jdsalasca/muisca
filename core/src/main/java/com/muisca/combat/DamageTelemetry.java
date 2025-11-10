package com.muisca.combat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import java.util.Locale;

/**
 * Stores recent combat damage samples for HUD overlays and logging.
 */
public final class DamageTelemetry {

    private static final int MAX_EVENTS = 32;
    private final Array<DamageEvent> events = new Array<>(false, MAX_EVENTS);
    private float clock = 0f;
    private float outgoingSum = 0f;
    private float incomingSum = 0f;
    private float windowSeconds = 10f;
    private final StringBuilder scratch = new StringBuilder();
    private final StringBuilder csvBuilder = new StringBuilder();
    private FileHandle logFile;

    public DamageTelemetry() {
        this(null);
    }

    public DamageTelemetry(FileHandle logFile) {
        this.logFile = logFile;
    }

    public void setLogFile(FileHandle logFile) {
        this.logFile = logFile;
    }

    public void update(float delta) {
        clock += delta;
        for (int i = events.size - 1; i >= 0; i--) {
            DamageEvent event = events.get(i);
            if (clock - event.timestamp > windowSeconds) {
                adjustRollingSums(event, -1f);
                events.removeIndex(i);
            }
        }
    }

    public void record(String source, String target, float amount, DamageType type, String ability, boolean outgoing) {
        DamageEvent event = new DamageEvent(source, target, amount, type, ability, clock, outgoing);
        if (events.size == MAX_EVENTS) {
            DamageEvent removed = events.removeIndex(0);
            adjustRollingSums(removed, -1f);
        }
        events.add(event);
        adjustRollingSums(event, 1f);
        Gdx.app.log("Damage", event.toString());
        appendEventToLog(event);
    }

    private void adjustRollingSums(DamageEvent event, float sign) {
        if (event.outgoing) {
            outgoingSum = Math.max(0f, outgoingSum + event.amount * sign);
        } else {
            incomingSum = Math.max(0f, incomingSum + event.amount * sign);
        }
    }

    public String buildOverlayText() {
        scratch.setLength(0);
        scratch.append("Daño (últimos ").append((int) windowSeconds).append("s)")
                .append(" | DPS Out: ").append(formatDps(outgoingSum))
                .append(" | DPS In: ").append(formatDps(incomingSum)).append('\n');
        int lines = Math.min(events.size, 6);
        for (int i = events.size - lines; i < events.size; i++) {
            if (i < 0) continue;
            DamageEvent event = events.get(i);
            scratch.append(event.outgoing ? "→ " : "← ")
                    .append(event.source).append(" vs ").append(event.target)
                    .append(" : ").append(Math.round(event.amount))
                    .append(" ").append(event.type)
                    .append(" (").append(event.ability).append(")\n");
        }
        return scratch.toString();
    }

    private String formatDps(float sum) {
        return String.format(Locale.US, "%.1f", sum / windowSeconds);
    }

    private void appendEventToLog(DamageEvent event) {
        if (logFile == null) {
            return;
        }
        csvBuilder.setLength(0);
        csvBuilder.append(String.format(Locale.US, "%.3f", event.timestamp)).append(',');
        csvBuilder.append(event.outgoing ? "out" : "in").append(',');
        csvBuilder.append(escapeCsv(event.source)).append(',');
        csvBuilder.append(escapeCsv(event.target)).append(',');
        csvBuilder.append(String.format(Locale.US, "%.2f", event.amount)).append(',');
        csvBuilder.append(event.type).append(',');
        csvBuilder.append(escapeCsv(event.ability));
        csvBuilder.append('\n');
        logFile.writeString(csvBuilder.toString(), true, "UTF-8");
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    public static final class DamageEvent {
        public final String source;
        public final String target;
        public final float amount;
        public final DamageType type;
        public final String ability;
        public final float timestamp;
        public final boolean outgoing;

        public DamageEvent(String source, String target, float amount, DamageType type,
                           String ability, float timestamp, boolean outgoing) {
            this.source = source;
            this.target = target;
            this.amount = amount;
            this.type = type;
            this.ability = ability;
            this.timestamp = timestamp;
            this.outgoing = outgoing;
        }

        @Override
        public String toString() {
            return (outgoing ? "[OUT]" : "[IN]") + " " + source + " -> " + target + " "
                    + amount + " " + type + " via " + ability;
        }
    }
}
