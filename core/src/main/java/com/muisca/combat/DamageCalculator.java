package com.muisca.combat;

import com.muisca.ecs.components.StatusComponent;

public final class DamageCalculator {

    private DamageCalculator() {
    }

    public static float computeSpellDamage(SpellDefinition spell,
                                           CombatStats caster,
                                           CombatStats defender,
                                           StatusComponent targetStatus) {
        float raw = spell.baseDamage + caster.getAttack() * spell.attackScaling;
        float mitigation = defender.getMitigation(spell.damageType);
        float damage = Math.max(1f, raw - mitigation);
        float multiplier = 1f;
        if (targetStatus != null) {
            multiplier += targetStatus.getDamageTakenBonus();
        }
        return damage * multiplier;
    }
}
