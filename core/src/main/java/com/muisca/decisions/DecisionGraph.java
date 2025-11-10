package com.muisca.decisions;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectFloatMap;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectMap;

import java.util.HashMap;
import java.util.Map;

public class DecisionGraph {

    private final Map<String, DecisionNode> nodes = new HashMap<>();
    private final String entryNodeId;

    public static DecisionGraph load(FileHandle handle) {
        JsonValue root = new JsonReader().parse(handle);
        DecisionGraph graph = new DecisionGraph(root);
        DecisionValidator.validate(graph);
        return graph;
    }

    private DecisionGraph(JsonValue root) {
        for (JsonValue nodeValue : root.get("nodes")) {
            DecisionNode node = new DecisionNode();
            node.id = nodeValue.getString("id");
            node.title = nodeValue.getString("title", node.id);
            node.description = nodeValue.getString("description", "");
            JsonValue options = nodeValue.get("options");
            if (options != null) {
                for (JsonValue optionValue : options) {
                    DecisionOption option = new DecisionOption();
                    option.label = optionValue.getString("label", "Opción");
                    option.inventoryCost = parseIntMap(optionValue.get("inventoryCost"));
                    option.inventoryReward = parseIntMap(optionValue.get("inventoryReward"));
                    option.reputationMin = parseFloatMap(optionValue.get("reputationMin"));
                    option.reputationDelta = parseFloatMap(optionValue.get("reputationDelta"));
                    option.flagSet = parseFlagMap(optionValue.get("setFlag"));
                    option.nextNode = optionValue.getString("next", null);
                    node.options.add(option);
                }
            }
            nodes.put(node.id, node);
        }
        this.entryNodeId = root.getString("entryNode", nodes.keySet().stream().findFirst().orElse(null));
    }

    private static ObjectIntMap<String> parseIntMap(JsonValue node) {
        ObjectIntMap<String> map = new ObjectIntMap<>();
        if (node == null) return map;
        for (JsonValue child : node) {
            map.put(child.name, child.asInt());
        }
        return map;
    }

    private static ObjectFloatMap<String> parseFloatMap(JsonValue node) {
        ObjectFloatMap<String> map = new ObjectFloatMap<>();
        if (node == null) return map;
        for (JsonValue child : node) {
            map.put(child.name, child.asFloat());
        }
        return map;
    }

    private static ObjectMap<String, Boolean> parseFlagMap(JsonValue node) {
        ObjectMap<String, Boolean> map = new ObjectMap<>();
        if (node == null) return map;
        for (JsonValue child : node) {
            map.put(child.name, child.asBoolean());
        }
        return map;
    }

    public DecisionNode getNode(String id) {
        return nodes.get(id);
    }

    public DecisionNode getEntryNode() {
        return nodes.get(entryNodeId);
    }

    public Array<String> getNodeIds() {
        return new Array<>(nodes.keySet().toArray(new String[0]));
    }
}
