package org.webswing.directdraw.toolkit;

import sun.awt.image.SurfaceManager;
import sun.java2d.SurfaceData;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.ImageCapabilities;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.VolatileImage;

@SuppressWarnings("restriction")
public class VolatileWebImageWrapper extends VolatileImage {

  private WebImage webImage;
  private ImageCapabilities caps;

  public VolatileWebImageWrapper(ImageCapabilities caps, WebImage webImage) {
    this.webImage = webImage;
    this.caps = caps;
    SurfaceManager.setManager(this, new SurfaceManager() {

      @SuppressWarnings("unused")
      // java 1.6
      public SurfaceData getSourceSurfaceData(sun.java2d.SurfaceData s,
          sun.java2d.loops.CompositeType c, Color color, boolean b) {
        BufferedImage snapshot = VolatileWebImageWrapper.this.getSnapshot();
        SurfaceManager m = SurfaceManager.getManager(snapshot);
        try {
          return (SurfaceData) m.getClass()
              .getDeclaredMethod("getSourceSurfaceData", sun.java2d.SurfaceData.class,
                  sun.java2d.loops.CompositeType.class, Color.class, Boolean.TYPE)
              .invoke(m, s, c, color, b);
        } catch (Exception e) {
          e.printStackTrace();
          return null;
        }
      }

      @SuppressWarnings("unused")
      // java 1.6
      public SurfaceData getDestSurfaceData() {
        BufferedImage snapshot = VolatileWebImageWrapper.this.getSnapshot();
        SurfaceManager m = SurfaceManager.getManager(snapshot);
        try {
          return (SurfaceData) m.getClass().getDeclaredMethod("getDestSurfaceData").invoke(m);
        } catch (Exception e) {
          e.printStackTrace();
          return null;
        }
      }

      public SurfaceData getPrimarySurfaceData() {// java 1.7
        BufferedImage snapshot = VolatileWebImageWrapper.this.getSnapshot();
        SurfaceManager m = SurfaceManager.getManager(snapshot);
        try {
          return (SurfaceData) m.getClass().getDeclaredMethod("getPrimarySurfaceData").invoke(m);
        } catch (Exception e) {
          e.printStackTrace();
          return null;
        }
      }

      public SurfaceData restoreContents() {// java 1.7
        BufferedImage snapshot = VolatileWebImageWrapper.this.getSnapshot();
        SurfaceManager m = SurfaceManager.getManager(snapshot);
        try {
          return (SurfaceData) m.getClass().getDeclaredMethod("restoreContents").invoke(m);
        } catch (Exception e) {
          e.printStackTrace();
          return null;
        }
      }

    });
  }

  @Override
  public BufferedImage getSnapshot() {
    return this.webImage.getSnapshot();
  }

  @Override
  public int getWidth() {
    return this.webImage.getWidth(null);
  }

  @Override
  public int getHeight() {
    return this.webImage.getHeight(null);
  }

  @Override
  public Graphics2D createGraphics() {
    return (Graphics2D) this.webImage.getGraphics();
  }

  @Override
  public int validate(GraphicsConfiguration gc) {
    this.webImage.reset();
    return VolatileImage.IMAGE_RESTORED;
  }

  @Override
  public boolean contentsLost() {
    return false;
  }

  @Override
  public ImageCapabilities getCapabilities() {
    return this.caps;
  }

  @Override
  public int getWidth(ImageObserver observer) {
    return this.webImage.getWidth(observer);
  }

  @Override
  public int getHeight(ImageObserver observer) {
    return this.webImage.getHeight(observer);
  }

  @Override
  public Object getProperty(String name, ImageObserver observer) {
    return this.webImage.getProperty(name, observer);
  }

  public WebImage getWebImage() {
    return webImage;
  }
}
