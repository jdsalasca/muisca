package com.muisca.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

public class InputControlComponent implements Component {
    public final Vector2 direction = new Vector2();
    public boolean selected = false;
    public float intendedSpeed = 0f;
}
