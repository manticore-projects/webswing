package org.webswing.toolkit.ge;

import java.awt.GraphicsDevice;

import org.webswing.Constants;
import org.webswing.toolkit.WebToolkit;

import sun.awt.FontConfiguration;

@SuppressWarnings("restriction")
public abstract class WebGraphicsEnvironment extends sun.java2d.SunGraphicsEnvironment {

  protected int getNumScreens() {
    return 1;
  }

  protected GraphicsDevice makeScreenDevice(int paramInt) {
    return WebGraphicsConfig.getWebGraphicsConfig(WebToolkit.screenWidth, WebToolkit.screenHeight)
        .getDevice();
  }

  protected FontConfiguration createFontConfiguration() {
    return createFontConfiguration(false, false);
  }

  abstract public FontConfiguration createFontConfiguration(boolean b1, boolean b2);

  public void displayChanged() {
    screens = null;
    super.displayChanged();
  }

  public boolean isDisplayLocal() {
    return true;
  }

  @Override
  public void preferLocaleFonts() {
    // do nothing (prevents CompositeFont initialization)
  }

  @Override
  public void preferProportionalFonts() {
    // do nothing (prevents CompositeFont initialization)
  }

  public static boolean hasFontConfiguration() {
    if (System.getProperty(Constants.SWING_START_SYS_PROP_FONT_CONFIG) != null) {
      return true;
    } else {
      return false;
    }
  }
}
