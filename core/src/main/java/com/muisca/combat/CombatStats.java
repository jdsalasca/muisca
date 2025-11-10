package com.muisca.combat;

import com.badlogic.gdx.math.MathUtils;

/**
 * Lightweight holder for base combat stats and derived pools.
 */
public class CombatStats implements Cloneable {

    private float vitality;
    private float stamina;
    private float attack;
    private float defense;
    private float resistance;

    private float maxHealth;
    private float maxStamina;
    private float maxFocus;

    private float health;
    private float staminaPool;
    private float focusPool;

    private float staminaRegen = 6f;
    private float focusRegen = 3f;

    public CombatStats(float vitality, float stamina, float attack, float defense, float resistance) {
        this.vitality = vitality;
        this.stamina = stamina;
        this.attack = attack;
        this.defense = defense;
        this.resistance = resistance;
        recalcDerived();
        refillAll();
    }

    public static CombatStats colonistBaseline() {
        return new CombatStats(14f, 12f, 10f, 8f, 8f);
    }

    public static CombatStats enemyBaseline(float vitality, float stamina, float attack, float defense, float resistance) {
        return new CombatStats(vitality, stamina, attack, defense, resistance);
    }

    public void recalcDerived() {
        maxHealth = 60f + vitality * 12f;
        maxStamina = 30f + stamina * 7f;
        maxFocus = 15f + resistance * 4.5f;
        health = MathUtils.clamp(health, 0f, maxHealth);
        staminaPool = MathUtils.clamp(staminaPool, 0f, maxStamina);
        focusPool = MathUtils.clamp(focusPool, 0f, maxFocus);
    }

    public void refillAll() {
        health = maxHealth;
        staminaPool = maxStamina;
        focusPool = maxFocus;
    }

    public void regenerate(float delta) {
        staminaPool = MathUtils.clamp(staminaPool + staminaRegen * delta, 0f, maxStamina);
        focusPool = MathUtils.clamp(focusPool + focusRegen * delta, 0f, maxFocus);
    }

    public boolean spendStamina(float amount) {
        if (staminaPool < amount) {
            return false;
        }
        staminaPool -= amount;
        return true;
    }

    public boolean spendFocus(float amount) {
        if (focusPool < amount) {
            return false;
        }
        focusPool -= amount;
        return true;
    }

    public boolean canPay(float staminaCost, float focusCost) {
        return staminaPool >= staminaCost && focusPool >= focusCost;
    }

    public boolean spend(float staminaCost, float focusCost) {
        if (!canPay(staminaCost, focusCost)) {
            return false;
        }
        staminaPool -= staminaCost;
        focusPool -= focusCost;
        return true;
    }

    public void recoverFocus(float amount) {
        focusPool = MathUtils.clamp(focusPool + amount, 0f, maxFocus);
    }

    public void recoverStamina(float amount) {
        staminaPool = MathUtils.clamp(staminaPool + amount, 0f, maxStamina);
    }

    public void heal(float amount) {
        health = MathUtils.clamp(health + amount, 0f, maxHealth);
    }

    public void applyDamage(float amount) {
        health = MathUtils.clamp(health - amount, 0f, maxHealth);
    }

    public void setHealth(float value) {
        health = MathUtils.clamp(value, 0f, maxHealth);
    }

    public void setStaminaPool(float value) {
        staminaPool = MathUtils.clamp(value, 0f, maxStamina);
    }

    public void setFocusPool(float value) {
        focusPool = MathUtils.clamp(value, 0f, maxFocus);
    }

    public void setPools(float health, float stamina, float focus) {
        setHealth(health);
        setStaminaPool(stamina);
        setFocusPool(focus);
    }

    public boolean isAlive() {
        return health > 0f;
    }

    public float getMitigation(DamageType damageType) {
        if (damageType == DamageType.PHYSICAL) {
            return defense * 0.8f;
        }
        if (damageType == DamageType.TRUE) {
            return 0f;
        }
        return resistance * 0.9f;
    }

    public float getHealthRatio() {
        return health / maxHealth;
    }

    public float getStaminaRatio() {
        return staminaPool / maxStamina;
    }

    public float getFocusRatio() {
        return focusPool / maxFocus;
    }

    public float getHealth() {
        return health;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public float getStaminaPool() {
        return staminaPool;
    }

    public float getMaxStamina() {
        return maxStamina;
    }

    public float getFocusPool() {
        return focusPool;
    }

    public float getMaxFocus() {
        return maxFocus;
    }

    public float getAttack() {
        return attack;
    }

    public float getVitality() {
        return vitality;
    }

    public float getStamina() {
        return stamina;
    }

    public float getDefense() {
        return defense;
    }

    public float getResistance() {
        return resistance;
    }

    public void addVitality(float delta) {
        vitality += delta;
        recalcDerived();
    }

    public void addAttack(float delta) {
        attack += delta;
    }

    public void addDefense(float delta) {
        defense += delta;
    }

    public void addResistance(float delta) {
        resistance += delta;
    }

    public void addStamina(float delta) {
        stamina += delta;
        recalcDerived();
    }

    public void setStaminaRegen(float staminaRegen) {
        this.staminaRegen = staminaRegen;
    }

    public void setFocusRegen(float focusRegen) {
        this.focusRegen = focusRegen;
    }

    @Override
    public CombatStats clone() {
        try {
            CombatStats copy = (CombatStats) super.clone();
            copy.health = health;
            copy.staminaPool = staminaPool;
            copy.focusPool = focusPool;
            copy.recalcDerived();
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}
