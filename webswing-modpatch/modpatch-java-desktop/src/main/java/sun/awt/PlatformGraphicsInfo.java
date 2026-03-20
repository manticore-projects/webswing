/*
 * Patched PlatformGraphicsInfo for WebSwing on JDK 21+.
 *
 * In JDK 12+, GraphicsEnvironment.getLocalGraphicsEnvironment() no longer
 * reads the java.awt.graphicsenv system property. Instead it calls
 * PlatformGraphicsInfo.createGE() which hardcodes X11GraphicsEnvironment
 * on Linux. This patched version restores the system property behavior.
 *
 * Deployed via: --patch-module java.desktop=modpatch-java-desktop.jar
 */
package sun.awt;

import java.awt.AWTError;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;

public class PlatformGraphicsInfo {

    public static GraphicsEnvironment createGE() {
        String geName = System.getProperty("java.awt.graphicsenv");
        if (geName == null) {
            geName = "sun.awt.X11GraphicsEnvironment";
        }
        try {
            // Use 1-arg Class.forName which uses the caller's classloader.
            // Since this class is in java.desktop (bootstrap loader via --patch-module),
            // it can see both JDK classes and -Xbootclasspath/a: classes.
            Class<?> geClass = Class.forName(geName);
            return (GraphicsEnvironment) geClass.getDeclaredConstructor().newInstance();
        } catch (Throwable e) {
            System.err.println("[WebSwing] PlatformGraphicsInfo.createGE() failed for: " + geName);
            e.printStackTrace(System.err);
            throw new AWTError("Could not create graphics environment: " + geName + " - " + e);
        }
    }

    public static boolean getDefaultHeadlessProperty() {
        return false;
    }

    public static String getDefaultHeadlessMessage() {
        return null;
    }

    public static Toolkit createToolkit() {
        String toolkitName = System.getProperty("awt.toolkit");
        if (toolkitName == null) {
            toolkitName = "sun.awt.X11.XToolkit";
        }
        try {
            Class<?> tkClass = Class.forName(toolkitName);
            return (Toolkit) tkClass.getDeclaredConstructor().newInstance();
        } catch (Throwable e) {
            System.err.println("[WebSwing] PlatformGraphicsInfo.createToolkit() failed for: " + toolkitName);
            e.printStackTrace(System.err);
            throw new AWTError("Could not create toolkit: " + toolkitName + " - " + e);
        }
    }
}