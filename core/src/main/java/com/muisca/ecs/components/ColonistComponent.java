package com.muisca.ecs.components;

import com.badlogic.ashley.core.Component;
import com.muisca.colony.Colonist;

public class ColonistComponent implements Component {

    public final Colonist colonist;

    public ColonistComponent(Colonist colonist) {
        this.colonist = colonist;
    }
}
