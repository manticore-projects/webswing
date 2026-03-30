package org.webswing.theme;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.webswing.Constants;
import org.webswing.common.WindowActionType;
import org.webswing.common.WindowDecoratorTheme;
import org.webswing.model.appframe.out.AccessibilityMsgOut;
import org.webswing.toolkit.api.component.Dockable;
import org.webswing.toolkit.util.Util;
import org.webswing.util.AppLogger;

public class DefaultWindowDecoratorTheme implements WindowDecoratorTheme {
  private static final String DEFAULT_THEME = "Murrine";

  Insets insets = new Insets(0, 0, 0, 0);

  private String theme = System.getProperty(Constants.SWING_START_SYS_PROP_THEME, DEFAULT_THEME);

  private Properties themeProperties;
  private ImageSet active;
  private ImageSet inactive;

  public int BUTTON_OFFSET = 7;
  public int BUTTON_SPACING = 4;
  public int TITLE_HORIZONTAL_OFFSET = 0;

  private abstract class ImageSet {
    BufferedImage TOP_LEFT;
    BufferedImage TITLE;
    BufferedImage MENU;
    BufferedImage DOCK;
    BufferedImage UNDOCK;
    BufferedImage HIDE;
    BufferedImage MAXIMIZE;
    BufferedImage CLOSE;
    BufferedImage TOP_RIGHT;

    BufferedImage LEFT;
    BufferedImage RIGHT;

    BufferedImage BOTTOM_LEFT;
    BufferedImage BOTTOM;
    BufferedImage BOTTOM_RIGHT;

    Color TEXT_COLOR;
    Color TEXT_SHADOW_COLOR;

    boolean TITLE_SHADOW;
    int TITLE_VERTICAL_OFFSET;
  };

  public DefaultWindowDecoratorTheme() {
    try {
      init();
    } catch (Exception e) {
      AppLogger.error(
          "Could not load theme " + theme + ". Falling back to default theme " + DEFAULT_THEME, e);
      theme = DEFAULT_THEME;
      try {
        init();
      } catch (IOException e1) {
        // should never happen
        AppLogger.fatal("Could not load default theme:", e1);
      }
    }
  }

  private void init() throws IOException {
    themeProperties = readProperties();
    active = new ImageSet() {
      {
        TOP_LEFT = readImage("top-left-active");
        TITLE = readImage("title-1-active");
        MENU = readImage("menu-active");
        UNDOCK = readImage("shade-active");
        DOCK = readImage("stick-active");
        HIDE = readImage("hide-active");
        MAXIMIZE = readImage("maximize-active");
        CLOSE = readImage("close-active");
        TOP_RIGHT = readImage("top-right-active");

        LEFT = readImage("left-active");
        RIGHT = readImage("right-active");

        BOTTOM_LEFT = readImage("bottom-left-active");
        BOTTOM = readImage("bottom-active");
        BOTTOM_RIGHT = readImage("bottom-right-active");

        TEXT_COLOR = Color.decode(themeProperties.getProperty("active_text_color", "#000000"));
        TEXT_SHADOW_COLOR =
            Color.decode(themeProperties.getProperty("active_text_shadow_color", "#111111"));
      }

    };
    inactive = new ImageSet() {
      {
        TOP_LEFT = readImage("top-left-inactive");
        TITLE = readImage("title-1-inactive");
        MENU = readImage("menu-inactive");
        UNDOCK = readImage("shade-inactive");
        DOCK = readImage("stick-inactive");
        HIDE = readImage("hide-inactive");
        MAXIMIZE = readImage("maximize-inactive");
        CLOSE = readImage("close-inactive");
        TOP_RIGHT = readImage("top-right-inactive");

        LEFT = readImage("left-inactive");
        RIGHT = readImage("right-inactive");

        BOTTOM_LEFT = readImage("bottom-left-inactive");
        BOTTOM = readImage("bottom-inactive");
        BOTTOM_RIGHT = readImage("bottom-right-inactive");

        TEXT_COLOR = Color.decode(themeProperties.getProperty("inactive_text_color", "#000000"));
        TEXT_SHADOW_COLOR =
            Color.decode(themeProperties.getProperty("inactive_text_shadow_color", "#111111"));
      }

    };
    insets = new Insets(active.TITLE.getHeight(), active.LEFT.getWidth(), active.BOTTOM.getHeight(),
        active.RIGHT.getWidth());
  }

  protected Properties readProperties() throws IOException {
    Properties properies = new Properties();

    ClassLoader classLoader = DefaultWindowDecoratorTheme.class.getClassLoader();
    URL resource = classLoader.getResource("themes/" + theme + "/xfwm4/themerc");
    if (resource == null) {
      resource = new File(theme + "/themerc").toURI().toURL();
    }
    if (resource != null) {
      properies.load(resource.openStream());
    } else {
      throw new IOException("File " + theme + "/themerc does not exist.");
    }
    return properies;
  }

  protected BufferedImage readImage(String key) throws IOException {
    ClassLoader classLoader = DefaultWindowDecoratorTheme.class.getClassLoader();

    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice gs = ge.getDefaultScreenDevice();
    GraphicsConfiguration gc = gs.getDefaultConfiguration();
    Graphics2D g = null;
    BufferedImage bimage = null;

    InputStream is =
        classLoader.getResourceAsStream("themes/" + theme + "/xfwm4/" + key + "_xpm.png");
    if (is == null) {
      File localFile = new File(theme + "/" + key + "_xpm.png");
      if (localFile.exists()) {
        is = new FileInputStream(localFile);
      }
    }

    if (is != null) {
      try {
        Image xpm = ImageIO.read(is);
        bimage =
            gc.createCompatibleImage(xpm.getWidth(null), xpm.getHeight(null), Transparency.BITMASK);
        g = bimage.createGraphics();
        g.drawImage(xpm, 0, 0, null);
      } catch (Exception e) {
        AppLogger.debug("Exception while reading theme file " + key + " for theme " + theme, e);
      } finally {
        is.close();
      }
    }

    is = classLoader.getResourceAsStream("themes/" + theme + "/xfwm4/" + key + ".png");
    if (is == null) {
      File localFile = new File(theme + "/" + key + ".png");
      if (localFile.exists()) {
        is = new FileInputStream(localFile);
      }
    }

    if (is != null) {
      try {
        Image png = ImageIO.read(is);
        if (g == null) {
          bimage = gc.createCompatibleImage(png.getWidth(null), png.getHeight(null),
              Transparency.BITMASK);
          g = bimage.createGraphics();
        }
        g.drawImage(png, 0, 0, null);
      } catch (Exception e) {
        AppLogger.debug("Exception while reading theme file " + key + " for theme " + theme, e);
      } finally {
        is.close();
      }
    }
    return bimage;
  }

  @Override
  public Insets getInsets() {
    return (Insets) insets.clone();
  }

  @Override
  public void paintWindowDecoration(Graphics g, Object window, int w, int h) {
    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
        RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

    ImageSet is =
        (window != null && window.equals(Util.getWebToolkit().getWindowManager().getActiveWindow()))
            ? active
            : inactive;

    int xOffset = 0;
    int yOffset = 0;

    int x = xOffset;
    int y = yOffset + is.TITLE.getHeight();
    int effectiveH = h - is.TOP_LEFT.getHeight() - is.BOTTOM_LEFT.getHeight();
    g.drawImage(is.LEFT, x, y, is.LEFT.getWidth(), effectiveH, null);
    g.drawImage(is.RIGHT, xOffset + w - is.RIGHT.getWidth(), y, is.RIGHT.getWidth(), effectiveH,
        null);

    x = xOffset + is.TOP_LEFT.getWidth();
    y = yOffset;
    int effectiveW = w - is.TOP_LEFT.getWidth() - is.TOP_RIGHT.getWidth();
    g.drawImage(is.TITLE, x, y, effectiveW, is.TITLE.getHeight(), null);

    x = xOffset + is.BOTTOM_LEFT.getWidth();
    y = yOffset + h;
    effectiveW = w - is.BOTTOM_LEFT.getWidth() - is.BOTTOM_RIGHT.getWidth();
    g.drawImage(is.BOTTOM, x, y - is.BOTTOM.getHeight(), effectiveW, is.BOTTOM.getHeight(), null);

    x = xOffset;
    y = yOffset;
    g.drawImage(is.TOP_LEFT, x, y, null);
    g.drawImage(is.TOP_RIGHT, xOffset + w - is.TOP_RIGHT.getWidth(), y, null);

    x += is.TOP_LEFT.getWidth() + BUTTON_SPACING;
    g.drawImage(is.MENU, x, y + (is.TITLE.getHeight() - is.MENU.getHeight()) / 2, null);
    Rectangle menuRect = new Rectangle(x, y + (is.TITLE.getHeight() - is.MENU.getHeight()) / 2,
        is.MENU.getWidth(), is.MENU.getHeight());
    g.drawImage(getIcon(window), menuRect.x, menuRect.y, menuRect.width, menuRect.height, null);

    String title = getTitle(window) == null ? "" : getTitle(window);
    g.setFont(UIManager.getFont("TitledBorder.font").deriveFont(Font.PLAIN, 12));
    float lineMetricsHeight = g.getFontMetrics().getLineMetrics(title, g).getHeight();

    x += is.MENU.getWidth() + BUTTON_SPACING;
    g.setColor(is.TEXT_COLOR);
    g.drawString(title, x,
        y + (is.TITLE.getHeight() - (int) lineMetricsHeight) / 2 + (int) lineMetricsHeight);

    x = xOffset + w - BUTTON_SPACING;

    boolean firstButton = true;

    if (isCloseButtonVisible(window)) {
      x -= is.CLOSE.getWidth();
      g.drawImage(is.CLOSE, x, y + (is.TITLE.getHeight() - is.CLOSE.getHeight()) / 2, null);
      firstButton = false;
    }

    // Dialogs can be RESIZABLE too, at least on Linux/Unix
    if (isMinMaxButtonVisible(window)) {
      x -= (firstButton ? 0 : BUTTON_SPACING) + is.MAXIMIZE.getWidth();
      g.drawImage(is.MAXIMIZE, x, y + (is.TITLE.getHeight() - is.MAXIMIZE.getHeight()) / 2, null);

      x -= BUTTON_SPACING + is.HIDE.getWidth();
      g.drawImage(is.HIDE, x, y + (is.TITLE.getHeight() - is.HIDE.getHeight()) / 2, null);

      firstButton = false;
    }

    if (isDockButtonVisible(window) && window instanceof Window window1) {
      boolean undocked = Util.isWindowUndocked(window1);
      BufferedImage img = undocked ? is.DOCK : is.UNDOCK;
      x -= (firstButton ? 0 : BUTTON_SPACING) + img.getWidth();

      g.drawImage(img, x, y + (is.TITLE.getHeight() - img.getHeight()) / 2, null);
      firstButton = false;
    }

    x = xOffset;
    y = yOffset + h;
    g.drawImage(is.BOTTOM_LEFT, x, y - is.BOTTOM_LEFT.getHeight(), null);
    g.drawImage(is.BOTTOM_RIGHT, xOffset + w - is.BOTTOM_RIGHT.getWidth(),
        y - is.BOTTOM_RIGHT.getHeight(), null);

    insets = new Insets(is.TITLE.getHeight(), is.LEFT.getWidth(), is.BOTTOM.getHeight(),
        is.RIGHT.getWidth());
  }

  private Rectangle getDockUndockRect(Window w) {
    ImageSet is =
        (w != null && w.equals(Util.getWebToolkit().getWindowManager().getActiveWindow())) ? active
            : inactive;
    boolean undocked = Util.isWindowUndocked(w);
    BufferedImage img = undocked ? is.DOCK : is.UNDOCK;

    int x = w.getWidth() - BUTTON_SPACING - img.getWidth();
    if (isCloseButtonVisible(w)) {
      x = x - BUTTON_SPACING - is.CLOSE.getWidth();
    }
    if (isMinMaxButtonVisible(w)) {
      x = x - BUTTON_SPACING - is.MAXIMIZE.getWidth() - BUTTON_SPACING - is.HIDE.getWidth();
    }
    return new Rectangle(x, 0 + (is.TITLE.getHeight() - img.getHeight()) / 2, img.getWidth(),
        img.getHeight());
  }

  private Rectangle getHideRect(Window w) {
    ImageSet is =
        (w != null && w.equals(Util.getWebToolkit().getWindowManager().getActiveWindow())) ? active
            : inactive;
    int x = w.getWidth() - BUTTON_SPACING - is.MAXIMIZE.getWidth() - BUTTON_SPACING
        - is.HIDE.getWidth();
    if (isCloseButtonVisible(w)) {
      x = x - BUTTON_SPACING - is.CLOSE.getWidth();
    }
    return new Rectangle(x, 0 + (is.TITLE.getHeight() - is.HIDE.getHeight()) / 2,
        is.HIDE.getWidth(), is.HIDE.getHeight());
  }

  private Rectangle getMaximizeRect(Window w) {
    ImageSet is =
        (w != null && w.equals(Util.getWebToolkit().getWindowManager().getActiveWindow())) ? active
            : inactive;
    int x = w.getWidth() - BUTTON_SPACING - is.MAXIMIZE.getWidth();
    if (isCloseButtonVisible(w)) {
      x = x - BUTTON_SPACING - is.CLOSE.getWidth();
    }
    return new Rectangle(x, 0 + (is.TITLE.getHeight() - is.MAXIMIZE.getHeight()) / 2,
        is.MAXIMIZE.getWidth(), is.MAXIMIZE.getHeight());
  }

  private Rectangle getCloseRect(Window w) {
    ImageSet is =
        (w != null && w.equals(Util.getWebToolkit().getWindowManager().getActiveWindow())) ? active
            : inactive;
    int x = w.getWidth() - BUTTON_SPACING - is.CLOSE.getWidth();
    return new Rectangle(x, 0 + (is.TITLE.getHeight() - is.CLOSE.getHeight()) / 2,
        is.CLOSE.getWidth(), is.CLOSE.getHeight());
  }

  private static String getTitle(Object o) {
    if (o instanceof Frame frame) {
      return frame.getTitle();
    } else if (o instanceof Dialog dialog) {
      return dialog.getTitle();
    } else {
      return null;
    }
  }

  private static Image getIcon(Object o) {
    if (o instanceof Frame frame) {
      return frame.getIconImage();
    } else if (o instanceof Dialog dialog) {
      List<Image> images = dialog.getIconImages();
      if (images.size() > 0) {
        return images.get(0);
      }
    }
    return null;
  }

  @Override
  public WindowActionType getAction(Window w, Point e) {
    Rectangle eventPoint = new Rectangle((int) e.getX(), (int) e.getY(), 1, 1);
    Insets i = w.getInsets();

    if (isDockButtonVisible(w)
        && SwingUtilities.isRectangleContainingRectangle(getDockUndockRect(w), eventPoint)) {
      return Util.isWindowUndocked(w) ? WindowActionType.dock : WindowActionType.undock;
    }
    // Dialogs can be RESIZABLE too, at least on Linux/Unix
    if (isMinMaxButtonVisible(w)) {
      if (SwingUtilities.isRectangleContainingRectangle(getHideRect(w), eventPoint)) {
        return WindowActionType.minimize;
      }
      if (SwingUtilities.isRectangleContainingRectangle(getMaximizeRect(w), eventPoint)) {
        return WindowActionType.maximize;
      }
    }
    if (isCloseButtonVisible(w)
        && SwingUtilities.isRectangleContainingRectangle(getCloseRect(w), eventPoint)) {
      return WindowActionType.close;
    }

    // resize
    if (canResize(w)) {
      if (e.getX() < 10 && e.getY() < 10) {
        return WindowActionType.resizeUniTopLeft;
      }
      if (e.getX() > (w.getWidth() - 10) && e.getY() < 10) {
        return WindowActionType.resizeUniTopRight;
      }
      if (e.getX() < 10 && e.getY() > (w.getHeight() - 10)) {
        return WindowActionType.resizeUniBottomLeft;
      }
      if (e.getX() > (w.getWidth() - 10) && e.getY() > (w.getHeight() - 10)) {
        return WindowActionType.resizeUniBottomRight;
      }
      if (e.getX() > (w.getWidth() - i.right)) {
        return WindowActionType.resizeRight;
      }
      if (e.getX() < i.left) {
        return WindowActionType.resizeLeft;
      }
      if (e.getY() > (w.getHeight() - i.bottom)) {
        return WindowActionType.resizeBottom;
      }
      if (e.getY() < i.bottom) {
        return WindowActionType.resizeTop;
      }
    }

    if (e.getY() < i.top) {
      // move
      return WindowActionType.move;
    }

    return WindowActionType.cursorChanged;
  }

  @Override
  public AccessibilityMsgOut getAccessible(Window window, WindowActionType action,
      Point mousePointer) {
    AccessibilityMsgOut result = new AccessibilityMsgOut();

    if (!action.isButtonActionType()) {
      return null;
    }

    result.setId(System.identityHashCode(window) + "-" + action.name());
    result.setRole("decorationbutton"); // not a real ARIA role

    List<String> states = new ArrayList<>();
    states.add("ENABLED");
    result.setStates(states);

    Rectangle rect = null;

    switch (action) {
      case dock:
        rect = getDockUndockRect(window);
        result.setText("accessibility.window.button.toggleDock");
        break;
      case undock:
        rect = getDockUndockRect(window);
        result.setText("accessibility.window.button.toggleDock");
        break;
      case close:
        rect = getCloseRect(window);
        result.setText("accessibility.window.button.close");
        break;
      case maximize:
        rect = getMaximizeRect(window);
        if (window instanceof Frame frame) {
          if ((frame.getExtendedState() & Frame.MAXIMIZED_BOTH) != 0) {
            // maximized
            result.setText("accessibility.window.button.restore");
          } else {
            // not maximized
            result.setText("accessibility.window.button.maximize");
          }
        }
        break;
      case minimize:
        rect = getHideRect(window);
        result.setText("accessibility.window.button.minimize");
        break;
      default:
        break;
    }

    if (rect != null) {
      Point loc = window.getLocationOnScreen();
      result.setScreenX(loc.x + rect.x);
      result.setScreenY(loc.y + rect.y);
      result.setWidth(rect.width);
      result.setHeight(rect.height);
    }

    return result;
  }

  public boolean isMinMaxButtonVisible(Object w) {
    if (w instanceof Window window && Util.isWindowUndocked(window)) {
      return false;
    }
    return (w instanceof Frame f) && f.isResizable();
  }

  public boolean isCloseButtonVisible(Object w) {
    if (w instanceof Window window && Util.isWindowUndocked(window)) {
      return false;
    }
    return true;
  }

  public boolean canResize(Window w) {
    if (Util.isWindowUndocked(w)) {
      return false;
    }
    return (w instanceof Dialog d && d.isResizable()) || (w instanceof Frame f) && f.isResizable();
  }

  private boolean isDockButtonVisible(Object w) {
    switch (Util.getDockMode()) {
      case "NONE":
        return false;
      case "ALL":
        return true;
      case "MARKED":
        return w instanceof Dockable;
      default:
        return false;
    }
  }

}
