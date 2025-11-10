package com.muisca.combat;

import com.muisca.ecs.components.StatusComponent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DamageCalculatorTest {

    @Test
    void fireDamageRespectsResistance() {
        CombatStats attacker = CombatStats.enemyBaseline(10f, 10f, 15f, 5f, 5f);
        CombatStats defender = CombatStats.enemyBaseline(10f, 10f, 10f, 8f, 8f);
        SpellDefinition spell = new SpellDefinition("test_flame", "Test Flame", SpellSchool.CENIZA,
                DamageType.FIRE, 20f, 0.5f, 0f, 0f, 200f, 1f,
                null, 0f, 0f, 1f, 0f);
        float damage = DamageCalculator.computeSpellDamage(spell, attacker, defender, null);
        assertEquals(20.3f, damage, 0.01f);
    }

    @Test
    void bondStatusAmplifiesIncomingDamage() {
        CombatStats attacker = CombatStats.enemyBaseline(10f, 10f, 15f, 5f, 5f);
        CombatStats defender = CombatStats.enemyBaseline(10f, 10f, 10f, 8f, 8f);
        SpellDefinition spell = new SpellDefinition("test_bind", "Test Bind", SpellSchool.JURAMENTO,
                DamageType.FIRE, 20f, 0.5f, 0f, 0f, 200f, 1f,
                StatusEffect.BOND, 5f, 0.25f, 2f, 0f);
        StatusComponent status = new StatusComponent();
        status.put(StatusEffect.BOND, 5f, 0.25f, 1f, "tester");
        float damage = DamageCalculator.computeSpellDamage(spell, attacker, defender, status);
        assertEquals(25.375f, damage, 0.01f);
    }
}
