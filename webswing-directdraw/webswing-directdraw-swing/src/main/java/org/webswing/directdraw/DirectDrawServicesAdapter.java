package org.webswing.directdraw;

import java.awt.Font;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.webswing.directdraw.util.ImageConsumerAdapter;

public class DirectDrawServicesAdapter {

  private Map<String, String> fontFileMap;

  public byte[] getPngImage(BufferedImage imageContent) {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(imageContent, "png", baos);
      return baos.toByteArray();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public long getSignature(byte[] data) {
    return Arrays.hashCode(data);
  }

  public String encodeBytes(byte[] bytes) {
    return Base64.encodeBase64String(bytes);
  }

  public long computeHash(Image subImage) {
    ImageConsumerAdapter ic = new ImageConsumerAdapter() {
      @Override
      public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pixels, int off,
          int scansize) {
        for (int i = off; i < off + scansize; i++) {
          hash = hash * 31 + pixels[i];
        }
      }

      @Override
      public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pixels, int off,
          int scansize) {
        for (int i = off; i < off + scansize; i++) {
          hash = hash * 31 + pixels[i];
        }
      }

      @Override
      public void setDimensions(int width, int height) {
        hash = hash * 31 + width;
        hash = hash * 31 + height;
      }
    };
    subImage.getSource().startProduction(ic);
    return ic.getHash();
  }

  public String getFileForFont(Font font) {
    if (font != null) {
      Map<String, String> map = getFontFileMap();
      String fontName = font.getFontName().toLowerCase();
      String file = map.get(fontName);
      return file;
    }
    return null;
  }

  private Map<String, String> getFontFileMap() {
    if (fontFileMap == null) {
      fontFileMap = new HashMap<String, String>();
      try {
        String customFontConfigFile = System.getProperty("sun.awt.fontconfig");
        if (customFontConfigFile != null && new File(customFontConfigFile).canRead()) {
          File f = new File(customFontConfigFile);
          try (Scanner s = new Scanner(f)) {
            while (s.hasNextLine()) {
              String line = s.nextLine();
              if (line.startsWith("#@@")) {
                String[] values = line.substring(3).split("=");
                if (values.length == 2) {
                  fontFileMap.put(values[0].toLowerCase(), values[1]);
                }
              }

            }
          }
        }
      } catch (FileNotFoundException e) {
        System.err.println("Failed to initialize font file map for DirectDraw rendering.");
        e.printStackTrace();
      }
    }
    return fontFileMap;
  }

}
