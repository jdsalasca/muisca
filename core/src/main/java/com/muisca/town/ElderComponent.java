package com.muisca.town;

import com.badlogic.ashley.core.Component;

public class ElderComponent implements Component {
    public final ElderProfile profile;
    public float ritualTimer = 0f;

    public ElderComponent(ElderProfile profile) {
        this.profile = profile;
    }
}
