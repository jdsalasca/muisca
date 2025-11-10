package com.muisca.combat;

/**
 * Simple passive talents applied when an entity spawns.
 */
public enum TalentId {
    CENIZA_DISCIPLINE(stats -> {
        stats.addAttack(4f);
        stats.addResistance(2f);
        stats.setFocusRegen(4f);
    }),
    CENIZA_PYRE(stats -> stats.addAttack(6f)),
    JURAMENTO_WARD(stats -> {
        stats.addDefense(4f);
        stats.addResistance(3f);
    }),
    JURAMENTO_ANCHOR(stats -> stats.addVitality(3f)),
    VANGUARD_TRAINING(stats -> {
        stats.addDefense(2f);
        stats.addAttack(2f);
    });

    private final TalentEffect effect;

    TalentId(TalentEffect effect) {
        this.effect = effect;
    }

    public void apply(CombatStats stats) {
        effect.apply(stats);
    }

    @FunctionalInterface
    public interface TalentEffect {
        void apply(CombatStats stats);
    }
}
