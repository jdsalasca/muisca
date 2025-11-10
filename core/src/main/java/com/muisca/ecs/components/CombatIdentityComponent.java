package com.muisca.ecs.components;

import com.badlogic.ashley.core.Component;

public class CombatIdentityComponent implements Component {

    public enum Faction {
        PLAYER,
        ENEMY
    }

    public String name;
    public Faction faction;

    public CombatIdentityComponent(String name, Faction faction) {
        this.name = name;
        this.faction = faction;
    }
}
