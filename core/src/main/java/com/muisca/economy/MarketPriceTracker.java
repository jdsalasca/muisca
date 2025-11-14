package com.muisca.economy;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.StringBuilder;
import com.muisca.inventory.Inventory;
import com.muisca.reputation.ReputationTracker;
import java.util.Locale;

/**
 * Tiny helper that models scarcity-based prices modified by reputation.
 */
public class MarketPriceTracker {

    private static class MarketGood {
        final String itemId;
        final float basePrice;
        final int idealStock;
        final float scarcityWeight;
        final float reputationWeight;

        MarketGood(String itemId, float basePrice, int idealStock,
                   float scarcityWeight, float reputationWeight) {
            this.itemId = itemId;
            this.basePrice = basePrice;
            this.idealStock = Math.max(1, idealStock);
            this.scarcityWeight = scarcityWeight;
            this.reputationWeight = reputationWeight;
        }
    }

    private final ObjectMap<String, MarketGood> goods = new ObjectMap<>();
    private final Array<MarketGood> ordered = new Array<>();

    public void registerGood(String itemId, float basePrice, int idealStock,
                             float scarcityWeight, float reputationWeight) {
        MarketGood good = new MarketGood(itemId, basePrice, idealStock, scarcityWeight, reputationWeight);
        goods.put(itemId, good);
        ordered.add(good);
    }

    public float getSellPrice(String itemId, int stock, float reputationValue) {
        MarketGood good = goods.get(itemId);
        if (good == null) {
            return 0f;
        }
        float scarcity = MathUtils.clamp((good.idealStock - stock) / (float) good.idealStock, -1f, 1f);
        float scarcityMultiplier = 1f + scarcity * good.scarcityWeight;
        float repMultiplier = 1f - MathUtils.clamp(reputationValue * good.reputationWeight, -0.35f, 0.35f);
        return good.basePrice * scarcityMultiplier * repMultiplier;
    }

    public float getSellPrice(String itemId, Inventory inventory, ReputationTracker reputation, String faction) {
        int stock = inventory.getAmount(itemId);
        float repValue = reputation.get(faction);
        return getSellPrice(itemId, stock, repValue);
    }

    public String summarize(Inventory inventory, ReputationTracker reputation, String factionId) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < ordered.size; i++) {
            MarketGood good = ordered.get(i);
            if (i > 0) {
                builder.append(" | ");
            }
            int stock = inventory.getAmount(good.itemId);
            float price = getSellPrice(good.itemId, stock, reputation.get(factionId));
            builder.append(good.itemId)
                    .append(":")
                    .append(stock)
                    .append("u @")
                    .append(String.format(Locale.US, "%.1f", price));
        }
        return builder.toString();
    }
}
