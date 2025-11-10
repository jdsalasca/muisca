package com.muisca.decisions;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectFloatMap;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.muisca.inventory.Inventory;
import com.muisca.reputation.ReputationTracker;

public class DecisionEngine {

    public static class Result {
        public boolean success;
        public String message;
    }

    private final DecisionGraph graph;
    private final Inventory inventory;
    private final ReputationTracker reputation;
    private final DecisionState state;

    private DecisionNode currentNode;

    public DecisionEngine(DecisionGraph graph, Inventory inventory, ReputationTracker reputation, DecisionState state) {
        this.graph = graph;
        this.inventory = inventory;
        this.reputation = reputation;
        this.state = state;
        this.currentNode = graph.getEntryNode();
    }

    public DecisionNode getCurrentNode() {
        return currentNode;
    }

    public void openNode(String nodeId) {
        DecisionNode node = graph.getNode(nodeId);
        if (node != null) {
            currentNode = node;
        }
    }

    public Result chooseOption(int index) {
        Result result = new Result();
        if (currentNode == null || index < 0 || index >= currentNode.options.size) {
            result.success = false;
            result.message = "Opción inválida.";
            return result;
        }
        DecisionOption option = currentNode.options.get(index);
        if (!meetsReputation(option.reputationMin)) {
            result.success = false;
            result.message = "Reputación insuficiente.";
            return result;
        }
        if (!inventory.hasAll(option.inventoryCost)) {
            result.success = false;
            result.message = "Recursos insuficientes.";
            return result;
        }
        inventory.consumeAll(option.inventoryCost);
        addInventory(option.inventoryReward);
        applyReputation(option.reputationDelta);
        applyFlags(option.flagSet);
        if (option.nextNode != null) {
            openNode(option.nextNode);
        } else {
            currentNode = null;
        }
        result.success = true;
        result.message = "Decisión registrada.";
        return result;
    }

    private boolean meetsReputation(ObjectFloatMap<String> minMap) {
        for (ObjectFloatMap.Entry<String> entry : minMap.entries()) {
            if (reputation.get(entry.key) < entry.value) {
                return false;
            }
        }
        return true;
    }

    private void applyReputation(ObjectFloatMap<String> deltaMap) {
        for (ObjectFloatMap.Entry<String> entry : deltaMap.entries()) {
            reputation.add(entry.key, entry.value);
        }
    }

    private void applyFlags(ObjectMap<String, Boolean> flagMap) {
        for (ObjectMap.Entry<String, Boolean> entry : flagMap.entries()) {
            state.setFlag(entry.key, entry.value);
        }
    }

    private void addInventory(ObjectIntMap<String> rewardMap) {
        for (ObjectIntMap.Entry<String> entry : rewardMap.entries()) {
            inventory.add(entry.key, entry.value);
        }
    }

    public DecisionState getState() {
        return state;
    }

    public Array<String> getNodeIds() {
        return graph.getNodeIds();
    }

    public void resetToEntry() {
        currentNode = graph.getEntryNode();
    }
}
