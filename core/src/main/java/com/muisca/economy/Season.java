package com.muisca.economy;

/**
 * Lightweight representation of the agricultural seasons used in 0.0.7.
 */
public enum Season {
    TEMPERATE("templada"),
    RAINY("lluviosa"),
    DRY("seca");

    private final String id;

    Season(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static Season fromString(String value) {
        if (value == null) {
            return TEMPERATE;
        }
        String normalized = value.trim().toLowerCase();
        for (Season season : values()) {
            if (season.id.equals(normalized)) {
                return season;
            }
        }
        return TEMPERATE;
    }
}
