package org.webswing.ext.services;

import java.awt.Image;
import java.awt.image.BufferedImage;

import org.webswing.common.WindowDecoratorTheme;

public interface ImageService {

  byte[] getPngImage(BufferedImage image);

  WindowDecoratorTheme getWindowDecorationTheme();

  Image readFromDataUrl(String img);
}
