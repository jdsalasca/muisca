package com.muisca.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.muisca.MuiscaGame;

public final class DesktopLauncher {

    private DesktopLauncher() {
        // no-op
    }

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Muisca v0.0.1");
        config.useVsync(true);
        config.setWindowedMode(1280, 720);
        config.setForegroundFPS(60);
        new Lwjgl3Application(new MuiscaGame(), config);
    }
}
