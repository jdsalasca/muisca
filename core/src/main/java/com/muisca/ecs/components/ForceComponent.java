package com.muisca.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

public class ForceComponent implements Component {
    public final Vector2 velocity = new Vector2();
}
