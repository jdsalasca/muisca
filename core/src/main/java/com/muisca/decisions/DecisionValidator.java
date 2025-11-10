package com.muisca.decisions;

import com.badlogic.gdx.utils.Array;

public final class DecisionValidator {

    private DecisionValidator() {
    }

    public static void validate(DecisionGraph graph) {
        Array<String> nodeIds = graph.getNodeIds();
        for (String nodeId : nodeIds) {
            DecisionNode node = graph.getNode(nodeId);
            if (node == null) {
                throw new IllegalStateException("Nodo inexistente: " + nodeId);
            }
            for (int i = 0; i < node.options.size; i++) {
                DecisionOption option = node.options.get(i);
                if (option.nextNode != null && graph.getNode(option.nextNode) == null) {
                    throw new IllegalStateException("La opción '" + option.label + "' apunta a nodo inexistente: " + option.nextNode);
                }
            }
        }
    }
}
