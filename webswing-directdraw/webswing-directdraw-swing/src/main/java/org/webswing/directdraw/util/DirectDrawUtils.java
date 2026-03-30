package org.webswing.directdraw.util;

import org.webswing.directdraw.DirectDraw;
import org.webswing.directdraw.model.DrawConstant;
import org.webswing.directdraw.model.DrawInstruction;
import org.webswing.directdraw.model.TransformConst;
import org.webswing.directdraw.proto.Directdraw.DrawInstructionProto.InstructionProto;
import sun.java2d.SunGraphics2D;
import sun.java2d.loops.FontInfo;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.File;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.*;
import java.util.List;

@SuppressWarnings("restriction")
public class DirectDrawUtils {

  public static final Map<String, String> webFonts = Map.of("Dialog", "sans-serif", "DialogInput",
      "monospace", "Serif", "serif", "SansSerif", "sans-serif", "Monospaced", "monospace");
  private static final String DELIMITER = "|";
  private static SunGraphics2D sgHelper;

  static {
    sgHelper = (SunGraphics2D) new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).getGraphics();
    sgHelper.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
  }

  public static FontInfo getFontInfo(Font font) {
    sgHelper.setFont(font);
    return sgHelper.getFontInfo();
  }

  /**
   * there is a bug in the jdk 1.6 which makes Font.getAttributes() not work correctly. The method
   * does not return all values. What we dow here is using the old JDK 1.5 method.
   *
   * @param font font
   *
   * @return Attributes of font
   */
  // @SuppressWarnings({ "rawtypes", "unchecked" })
  public static Map<? extends Attribute, ?> getAttributes(Font font) {
    Map<Attribute, Object> result = new HashMap<Attribute, Object>(7, (float) 0.9);
    result.put(TextAttribute.TRANSFORM, font.getTransform());
    result.put(TextAttribute.FAMILY, font.getName());
    result.put(TextAttribute.SIZE, font.getSize2D());
    result.put(TextAttribute.WEIGHT, (font.getStyle() & Font.BOLD) != 0 ? TextAttribute.WEIGHT_BOLD
        : TextAttribute.WEIGHT_REGULAR);
    result.put(TextAttribute.POSTURE,
        (font.getStyle() & Font.ITALIC) != 0 ? TextAttribute.POSTURE_OBLIQUE
            : TextAttribute.POSTURE_REGULAR);
    result.put(TextAttribute.SUPERSCRIPT, 0);
    result.put(TextAttribute.WIDTH, 1f);
    return result;
  }

  public static BufferedImage createBufferedImage(Image image, ImageObserver observer, Color bkg) {
    if ((bkg == null) && (image instanceof BufferedImage bufferedImage)) {
      return bufferedImage;
    }
    BufferedImage bufferedImage =
        new BufferedImage(image.getWidth(observer), image.getHeight(observer),
            bkg == null ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);

    Graphics g = bufferedImage.getGraphics();
    if (bkg == null) {
      g.drawImage(image, 0, 0, observer);
    } else {
      g.drawImage(image, 0, 0, bkg, observer);
    }
    return bufferedImage;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static BufferedImage createBufferedImage(RenderedImage img) {
    if (img instanceof BufferedImage image) {
      return image;
    }
    ColorModel cm = img.getColorModel();
    int width = img.getWidth();
    int height = img.getHeight();
    WritableRaster raster = cm.createCompatibleWritableRaster(width, height);
    boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
    Hashtable properties = new Hashtable();
    String[] keys = img.getPropertyNames();
    if (keys != null) {
      for (int i = 0; i < keys.length; i++) {
        properties.put(keys[i], img.getProperty(keys[i]));
      }
    }
    BufferedImage result = new BufferedImage(cm, raster, isAlphaPremultiplied, properties);
    img.copyData(raster);
    return result;
  }

  public static int findFirstVisibleIndex(String s, double x, double y, Shape clip,
      FontMetrics fm) {
    if (clip == null) {
      return 0;
    }
    int idx = 0;
    int idxMin = 0;
    int idxMax = s.length();
    while (true) {
      VisibilityHints hints = getVisibilityHintsForIndex(idx, s, x, y, clip, fm);
      if (hints.leftVisible) {
        idxMax = idx - 1;
      } else {
        if (hints.indexVisible) {
          return idx;
        } else {
          if (hints.rightVisible) {
            idxMin = idx + 1;
          } else {
            return s.length();// invalid option
          }
        }
      }
      idx = idxMin + (idxMax - idxMin) / 2;
    }
  }

  public static int findLastVisibleIndex(int firstIndex, String s, double x, double y, Shape clip,
      FontMetrics fm) {
    if (clip == null || firstIndex == s.length()) {
      return s.length();
    }
    int idx = s.length();
    int idxMin = firstIndex;
    int idxMax = s.length();

    while (true) {
      VisibilityHints hints = getVisibilityHintsForIndex(idx, s, x, y, clip, fm);
      if (hints.rightVisible) {
        idxMin = idx + 1;
      } else {
        if (hints.indexVisible) {
          return Math.min(idx + 1, s.length());
        } else {
          if (hints.leftVisible) {
            idxMax = idx - 1;
          } else {
            return s.length();// invalid option
          }
        }
      }
      idx = idxMin + (idxMax - idxMin) / 2;
    }
  }

  private static VisibilityHints getVisibilityHintsForIndex(int index, String s, double x, double y,
      Shape clip, FontMetrics fm) {
    VisibilityHints result = new VisibilityHints();
    y = y - fm.getAscent();
    double h = fm.getDescent() + fm.getAscent();

    String txtL = s.substring(0, index);
    String txtI = s.substring(index, Math.min(index + 1, s.length()));
    String txtR = s.substring(Math.min(index + 1, s.length()));

    double wL = fm.stringWidth(txtL);
    double xL = x;
    if (clip.contains(xL, y, wL, h) || clip.intersects(xL, y, wL, h)) {
      result.leftVisible = true;
    }

    double wI = fm.stringWidth(txtI);
    wI = wI == 0 ? 0.0001 : wI;// clip.contains always returns false if wI is 0 (causing accent thai
                               // chars not render on top of last char)
    double xI = xL + wL;
    if (clip.contains(xI, y, wI, h) || clip.intersects(xI, y, wI, h)) {
      result.indexVisible = true;
    }

    double wR = fm.stringWidth(txtR);
    double xR = xI + wI;
    if (clip.contains(xR, y, wR, h) || clip.intersects(xR, y, wR, h)) {
      result.rightVisible = true;
    }
    return result;
  }

  static class VisibilityHints {
    public boolean leftVisible;
    public boolean rightVisible;
    public boolean indexVisible;
  }

  private static class GraphicsStatus {
    AffineTransform tx;
    DrawConstant<?> stroke;
    DrawConstant<?> composite;
    DrawConstant<?> paint;
    DrawConstant<?> font;

    void reset() {
      tx = null;
      stroke = null;
      composite = null;
      paint = null;
      font = null;
    }
  }

  public static void optimizeInstructions(DirectDraw ctx, List<DrawInstruction> instructions) {

    // step 1. group consequent transformations
    final GraphicsStatus mergedStatus = new GraphicsStatus();
    DrawInstruction graphicsCreate = null;
    Map<Integer, DrawInstruction> graphicsCreateMap = new HashMap<Integer, DrawInstruction>();
    List<DrawInstruction> newInstructions = new ArrayList<DrawInstruction>();
    for (DrawInstruction current : instructions) {
      if (current.getInstruction().equals(InstructionProto.TRANSFORM)) {
        if (mergedStatus.tx == null) {
          mergedStatus.tx = ((TransformConst) current.getArg(0)).getValue();
        } else {
          mergedStatus.tx.concatenate(((TransformConst) current.getArg(0)).getValue());
        }
      } else if (current.getInstruction().equals(InstructionProto.SET_STROKE)) {
        mergedStatus.stroke = current.getArg(0);
      } else if (current.getInstruction().equals(InstructionProto.SET_COMPOSITE)) {
        mergedStatus.composite = current.getArg(0);
      } else if (current.getInstruction().equals(InstructionProto.SET_PAINT)) {
        mergedStatus.paint = current.getArg(0);
      } else if (current.getInstruction().equals(InstructionProto.SET_FONT)) {
        mergedStatus.font = current.getArg(0);
      } else if (current.getInstruction().equals(InstructionProto.GRAPHICS_CREATE)
          || current.getInstruction().equals(InstructionProto.GRAPHICS_SWITCH)) {
        boolean isGraphicsCreateInst =
            current.getInstruction().equals(InstructionProto.GRAPHICS_CREATE);
        if (graphicsCreate != null) {
          if (!equalStatus(graphicsCreate, mergedStatus)) {
            graphicsCreate = createGraphics(ctx, graphicsCreate.getArg(0), mergedStatus);
            mergedStatus.reset();
          }
          graphicsCreateMap.put(graphicsCreate.getArg(0).getId(), graphicsCreate);
        } else if (isGraphicsCreateInst) {
          setGraphicsStatus(ctx, newInstructions, mergedStatus);
          mergedStatus.reset();
        }
        graphicsCreate =
            isGraphicsCreateInst ? current : graphicsCreateMap.get(current.getArg(0).getId());
        if (graphicsCreate != null) {
          mergedStatus.tx = ((TransformConst) graphicsCreate.getArg(1)).getValue();
          mergedStatus.stroke = graphicsCreate.getArg(2);
          mergedStatus.composite = graphicsCreate.getArg(3);
          mergedStatus.paint = graphicsCreate.getArg(4);
          mergedStatus.font = graphicsCreate.getArg(5);
        } else {// if graphics switch instruction and the create instruction already in result array
          // then add all status change for old graphics and add the switch inst
          setGraphicsStatus(ctx, newInstructions, mergedStatus);
          mergedStatus.reset();
          newInstructions.add(current);
        }
      } else if (current.getInstruction().equals(InstructionProto.GRAPHICS_DISPOSE)) {
        mergedStatus.reset();
        if (graphicsCreate == null) {
          newInstructions.add(current);
        } else {
          graphicsCreate = null;
        }
      } else {
        if (graphicsCreate != null) {
          if (!equalStatus(graphicsCreate, mergedStatus)) {
            graphicsCreate = createGraphics(ctx, graphicsCreate.getArg(0), mergedStatus);
          }
          newInstructions.add(graphicsCreate);
          graphicsCreateMap.remove(graphicsCreate.getArg(0).getId());
          graphicsCreate = null;
        } else {
          setGraphicsStatus(ctx, newInstructions, mergedStatus);
        }
        mergedStatus.reset();
        newInstructions.add(current);
      }
    }
    // if transform is last instruction, it will be omitted from the result
    instructions.clear();
    instructions.addAll(newInstructions);
  }

  private static boolean equalStatus(DrawInstruction graphicsCreate, GraphicsStatus status) {
    return equals(((TransformConst) graphicsCreate.getArg(1)).getValue(), status.tx)
        && equals(graphicsCreate.getArg(2), status.stroke)
        && equals(graphicsCreate.getArg(3), status.composite)
        && equals(graphicsCreate.getArg(4), status.paint)
        && equals(graphicsCreate.getArg(5), status.font);
  }

  private static boolean equals(Object a, Object b) {
    return (a == b) || (a != null && a.equals(b));
  }

  private static DrawInstruction createGraphics(DirectDraw ctx, DrawConstant<?> id,
      GraphicsStatus status) {
    return ctx.getInstructionFactory().createGraphics(id, new TransformConst(ctx, status.tx),
        status.stroke, status.composite, status.paint, status.font);
  }

  private static void setGraphicsStatus(DirectDraw ctx, List<DrawInstruction> instructions,
      GraphicsStatus status) {
    if (status.tx != null && !status.tx.isIdentity()) {
      instructions.add(ctx.getInstructionFactory().transform(status.tx));
    }
    if (status.stroke != null) {
      instructions.add(new DrawInstruction(InstructionProto.SET_STROKE, status.stroke));
    }
    if (status.composite != null) {
      instructions.add(new DrawInstruction(InstructionProto.SET_COMPOSITE, status.composite));
    }
    if (status.paint != null) {
      instructions.add(new DrawInstruction(InstructionProto.SET_PAINT, status.paint));
    }
    if (status.font != null) {
      instructions.add(new DrawInstruction(InstructionProto.SET_FONT, status.font));
    }
  }

  public static int hashCode(double value) {
    long bits = Double.doubleToLongBits(value);
    return (int) (bits ^ (bits >>> 32));
  }

  public static int hashCode(float value) {
    return Float.floatToIntBits(value);
  }

  public static int hashCode(boolean value) {
    return value ? 1231 : 1237;
  }

  public static String fontInfoDescriptor(Font f, FontInfo fi) {
    StringBuilder sb = new StringBuilder(f.getFontName());
    sb.append(DELIMITER).append(f.getStyle());
    sb.append(DELIMITER).append(Arrays.toString(fi.devTx));
    sb.append(DELIMITER).append(Arrays.toString(fi.glyphTx));
    sb.append(DELIMITER).append(fi.pixelHeight);
    sb.append(DELIMITER);
    return sb.toString();
  }

  public static byte[] toPNG(DirectDraw ctx, byte[] gray, int w, int h) {
    int[] imagePixels = new int[w * h];
    for (int i = 0; i < imagePixels.length; i++) {
      imagePixels[i] = gray[i] << 24;
    }
    BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    image.setRGB(0, 0, w, h, imagePixels, 0, w);
    image.getRGB(0, 0, w, h, null, 0, w);
    return ctx.getServices().getPngImage(image);

  }

  public static String fontNameFromFile(String fileName, Font font) {
    if (fileName != null) {
      File f = new File(fileName);
      if (f.exists()) {
        if (Boolean.getBoolean(DirectDraw.FONTS_PROVIDED)) {
          return new File(fileName).getName();
        } else {
          String name = fileName.hashCode() + new File(fileName).getName();
          name = name.length() > 20 ? name.substring(0, 20) : name; // IE will ignore the font if
                                                                    // name is longer than 31 chars
          return name;
        }
      } else {
        return fileName;
      }
    } else {
      return DirectDrawUtils.webFonts.get(font.getFamily());
    }
  }

  private DirectDrawUtils() {}
}
