package com.muisca.ecs.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Array;
import com.muisca.combat.SpellDefinition;

public class SpellbookComponent implements Component {

    public final Array<SpellSlot> slots = new Array<>();

    public void addSpell(SpellDefinition definition) {
        slots.add(new SpellSlot(definition));
    }

    public SpellSlot getSlot(int index) {
        if (index < 0 || index >= slots.size) {
            return null;
        }
        return slots.get(index);
    }

    public SpellSlot findBySpellId(String spellId) {
        if (spellId == null) {
            return null;
        }
        for (SpellSlot slot : slots) {
            if (slot.spell != null && slot.spell.id.equals(spellId)) {
                return slot;
            }
        }
        return null;
    }

    public static final class SpellSlot {
        public final SpellDefinition spell;
        public float cooldownRemaining = 0f;

        public SpellSlot(SpellDefinition spell) {
            this.spell = spell;
        }

        public boolean ready() {
            return cooldownRemaining <= 0f;
        }
    }
}
