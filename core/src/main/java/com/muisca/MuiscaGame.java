package com.muisca;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Timer;
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
        scheduleAutoQuit();
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

    private void scheduleAutoQuit() {
        String raw = System.getProperty("muisca.autoQuitSeconds", "");
        if (raw.isEmpty()) {
            return;
        }
        try {
            float delay = Float.parseFloat(raw);
            if (delay <= 0f) {
                return;
            }
            Gdx.app.log("Muisca", "Auto-quit en " + delay + "s (muisca.autoQuitSeconds)");
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    Gdx.app.log("Muisca", "Auto-quit disparado");
                    Gdx.app.exit();
                }
            }, delay);
        } catch (NumberFormatException ignored) {
            // ignore invalid value
        }
    }
}
