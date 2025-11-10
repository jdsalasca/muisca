package com.muisca.town;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

public class TownLifeComponent implements Component {
    public final Vector2 homeAnchor = new Vector2();
    public final Vector2 plazaAnchor = new Vector2();
}
