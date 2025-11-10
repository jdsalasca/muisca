package com.muisca.enemies;

import com.badlogic.gdx.utils.Array;
import com.muisca.combat.CombatStats;
import com.muisca.combat.TalentId;

public enum EnemyArchetype {
    CENIZA_ACOLYTE("Acolito Ceniza", CombatStats.enemyBaseline(12f, 14f, 16f, 6f, 10f),
            new String[]{"ember_burst"}, new TalentId[]{TalentId.CENIZA_DISCIPLINE},
            220f, 120f, 90f),
    JURAMENTO_SENTINEL("Centinela de Juramento", CombatStats.enemyBaseline(18f, 10f, 12f, 12f, 14f),
            new String[]{"oath_bind", "warding_sigils"}, new TalentId[]{TalentId.JURAMENTO_WARD},
            200f, 90f, 80f),
    CENIZA_REVENANT("Apóstol de Ceniza", CombatStats.enemyBaseline(20f, 16f, 18f, 10f, 16f),
            new String[]{"cinder_chain"}, new TalentId[]{TalentId.CENIZA_PYRE},
            240f, 110f, 95f),
    WARDEN_OF_UNITY("Guardia del Vínculo", CombatStats.enemyBaseline(28f, 18f, 22f, 16f, 20f),
            new String[]{"ashen_nova", "oath_bind", "warding_sigils"}, new TalentId[]{TalentId.VANGUARD_TRAINING, TalentId.JURAMENTO_ANCHOR},
            260f, 130f, 70f);

    public final String displayName;
    private final CombatStats template;
    public final Array<String> spellIds = new Array<>();
    public final TalentId[] talents;
    public final float engagementRange;
    public final float preferredDistance;
    public final float movementSpeed;
    public final float meleeRange = 55f;

    EnemyArchetype(String displayName,
                   CombatStats template,
                   String[] spellIds,
                   TalentId[] talents,
                   float engagementRange,
                   float preferredDistance,
                   float movementSpeed) {
        this.displayName = displayName;
        this.template = template;
        this.spellIds.addAll(spellIds);
        this.talents = talents;
        this.engagementRange = engagementRange;
        this.preferredDistance = preferredDistance;
        this.movementSpeed = movementSpeed;
    }

    public CombatStats createStats() {
        CombatStats stats = template.clone();
        stats.refillAll();
        return stats;
    }
}
