package com.muisca.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration.GLEmulation;
import com.muisca.MuiscaGame;

public final class DesktopLauncher {

    private DesktopLauncher() {
        // no-op
    }

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Muisca v0.0.6");
        config.useVsync(true);
        config.setWindowedMode(1280, 720);
        // Backbuffer consistente para evitar pantallas negras por formatos raros
        config.setBackBufferConfig(8, 8, 8, 8, 16, 0, 0);
        // Usar FPS del monitor si está disponible
        try {
            config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        } catch (Throwable t) {
            config.setForegroundFPS(60);
        }

        // Permitir elegir emulación GL mediante propiedad del sistema: -Dmuisca.angle=true
        boolean angle = Boolean.parseBoolean(System.getProperty("muisca.angle", "false"));
        if (angle) {
            config.setOpenGLEmulation(GLEmulation.ANGLE_GLES20, 0, 0);
            System.out.println("[DesktopLauncher] Using ANGLE GLES20 emulation (Windows/Metal friendly)");
        } else {
            // GL ES 3.0 emulado por OpenGL 3.2 (más estricto y estable que GL2 en algunos drivers)
            config.setOpenGLEmulation(GLEmulation.GL30, 3, 2);
            System.out.println("[DesktopLauncher] Using GL30 emulation via OpenGL 3.2");
        }

        System.out.println("Launching Muisca on Java " + System.getProperty("java.version")
                + " (" + System.getProperty("java.vendor") + ")");
        new Lwjgl3Application(new MuiscaGame(), config);
    }
}
