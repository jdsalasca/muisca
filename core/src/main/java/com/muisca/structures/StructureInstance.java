package com.muisca.structures;

import com.badlogic.gdx.math.Rectangle;

public class StructureInstance {
    private final StructureBlueprint blueprint;
    private final Rectangle bounds;

    public StructureInstance(StructureBlueprint blueprint, float x, float y) {
        this.blueprint = blueprint;
        this.bounds = new Rectangle(
                x - blueprint.getWidth() / 2f,
                y - blueprint.getHeight() / 2f,
                blueprint.getWidth(),
                blueprint.getHeight());
    }

    public StructureBlueprint getBlueprint() {
        return blueprint;
    }

    public Rectangle getBounds() {
        return bounds;
    }
}
