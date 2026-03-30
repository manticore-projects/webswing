package org.webswing.toolkit.ge;

import org.webswing.Constants;
import sun.awt.FontConfiguration;

@SuppressWarnings("restriction")
public class WebGraphicsEnvironment11 extends WebGraphicsEnvironment {

  public WebGraphicsEnvironment11() {
    if (hasFontConfiguration()) {
      System.setProperty("sun.awt.fontconfig",
          System.getProperty(Constants.SWING_START_SYS_PROP_FONT_CONFIG));
      System.setProperty("sun.font.fontmanager", WebFontManager.class.getName());
    }

  }

  public FontConfiguration createFontConfiguration(boolean b1, boolean b2) {
    return null; // not used in java8 (see WebFontManager)
  }
}
