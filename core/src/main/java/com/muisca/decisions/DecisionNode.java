package com.muisca.decisions;

import com.badlogic.gdx.utils.Array;

public class DecisionNode {
    public String id;
    public String title;
    public String description;
    public final Array<DecisionOption> options = new Array<>();
}
