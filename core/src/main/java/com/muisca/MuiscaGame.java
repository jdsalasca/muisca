package com.muisca;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.muisca.screens.SettlementScreen;

/**
 * Root libGDX game for the Muisca prototype.
 */
public class MuiscaGame extends Game {

    private SpriteBatch sharedBatch;

    @Override
    public void create() {
        sharedBatch = new SpriteBatch();
        setScreen(new SettlementScreen(this));
    }

    public SpriteBatch getSharedBatch() {
        return sharedBatch;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (getScreen() != null) {
            getScreen().dispose();
        }
        if (sharedBatch != null) {
            sharedBatch.dispose();
        }
    }
}
