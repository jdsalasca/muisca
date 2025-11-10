package com.muisca.world;

import com.badlogic.gdx.graphics.Color;

/**
 * Represents the major surface categories in the prototype world.
 */
public enum TileType {
    BOSQUE_TEMPLADO("Bosque templado", new Color(0.32f, 0.46f, 0.28f, 1f)),
    PARAMO_SOLAR("Páramo solar", new Color(0.62f, 0.58f, 0.38f, 1f)),
    ESTEPA_CENICIENTA("Estepa cenicienta", new Color(0.49f, 0.42f, 0.31f, 1f)),
    PANTANO_AZUFRADO("Pantano azufrado", new Color(0.21f, 0.32f, 0.29f, 1f)),
    BOSQUE_HUESOS("Bosque de huesos", new Color(0.44f, 0.35f, 0.42f, 1f)),
    LAGUNA_SAGRADA("Laguna sagrada", new Color(0.22f, 0.38f, 0.52f, 1f));

    private final String description;
    private final Color color;

    TileType(String description, Color color) {
        this.description = description;
        this.color = color;
    }

    public String getDescription() {
        return description;
    }

    public Color getColor() {
        return color;
    }

    public static TileType fromSample(float elevation, float humidity) {
        if (elevation < 0.2f) {
            return LAGUNA_SAGRADA;
        }
        if (humidity > 0.65f && elevation < 0.45f) {
            return PANTANO_AZUFRADO;
        }
        if (elevation > 0.8f) {
            return BOSQUE_HUESOS;
        }
        if (elevation > 0.6f) {
            return PARAMO_SOLAR;
        }
        if (humidity < 0.35f) {
            return ESTEPA_CENICIENTA;
        }
        return BOSQUE_TEMPLADO;
    }
}
