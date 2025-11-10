package com.muisca.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Array;
import com.muisca.combat.CombatStats;
import com.muisca.combat.TalentId;

public class TalentComponent implements Component {

    public final Array<TalentId> talents = new Array<>();

    public void addTalent(TalentId talent) {
        if (!talents.contains(talent, true)) {
            talents.add(talent);
        }
    }

    public void applyTalents(CombatStats stats) {
        for (TalentId talent : talents) {
            talent.apply(stats);
        }
    }
}
