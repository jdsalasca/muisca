package com.muisca.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration.GLEmulation;
import com.muisca.MuiscaGame;

import java.util.Locale;

public final class DesktopLauncher {

    private DesktopLauncher() {
        // no-op
    }

    public static void main(String[] args) {
        boolean angle = Boolean.parseBoolean(System.getProperty("muisca.angle", "false"));
        launchWithConfig(angle, /*allowFallback=*/true);
    }

    private static void launchWithConfig(boolean angle, boolean allowFallback) {
        Lwjgl3ApplicationConfiguration config = createConfig(angle);
        System.out.println("Launching Muisca on Java " + System.getProperty("java.version")
                + " (" + System.getProperty("java.vendor") + ")");
        try {
            new Lwjgl3Application(new MuiscaGame(), config);
        } catch (Exception ex) {
            if (angle && allowFallback && isAngleMissingLibrary(ex)) {
                System.err.println("[DesktopLauncher] ANGLE solicitado pero libEGL/libGLESv2 no se encontraron en PATH.");
                System.err.println("[DesktopLauncher] Usa tools\\run-desktop.ps1 -UseAngle solo si copias esas DLL a tools\\angle "
                        + "o defines ANGLE_LIB_DIR. Reintentando con GL30...");
                System.clearProperty("muisca.angle");
                launchWithConfig(false, /*allowFallback=*/false);
                return;
            }
            throwUnchecked(ex);
        }
    }

    private static Lwjgl3ApplicationConfiguration createConfig(boolean angle) {
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

        if (angle) {
            config.setOpenGLEmulation(GLEmulation.ANGLE_GLES20, 0, 0);
            System.out.println("[DesktopLauncher] Using ANGLE GLES20 emulation (Windows/Metal friendly)");
        } else {
            // GL ES 3.0 emulado por OpenGL 3.2 (más estricto y estable que GL2 en algunos drivers)
            config.setOpenGLEmulation(GLEmulation.GL30, 3, 2);
            System.out.println("[DesktopLauncher] Using GL30 emulation via OpenGL 3.2");
        }
        return config;
    }

    private static boolean isAngleMissingLibrary(Throwable throwable) {
        Throwable cursor = throwable;
        while (cursor != null) {
            String message = cursor.getMessage();
            if (message != null) {
                String normalized = message.toLowerCase(Locale.US);
                if (normalized.contains("egl: library not found")
                        || normalized.contains("egl library not found")
                        || normalized.contains("glfw_api_unavailable")) {
                    return true;
                }
            }
            cursor = cursor.getCause();
        }
        return false;
    }

    private static void throwUnchecked(Exception ex) {
        if (ex instanceof RuntimeException) {
            throw (RuntimeException) ex;
        }
        throw new RuntimeException(ex);
    }
}
