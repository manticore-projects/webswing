package org.webswing.ext.services;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDPixelMap;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.webswing.common.ImageServiceIfc;
import org.webswing.common.WindowDecoratorThemeIfc;

import com.objectplanet.image.PngEncoder;

public class ImageService implements ImageServiceIfc {

    private static ImageService impl;
    private PngEncoder encoder;
    private WindowDecoratorThemeIfc windowDecorationTheme;

    public static ImageService getInstance() {
        if (impl == null) {
            impl = new ImageService();
        }
        return impl;
    }

    public ImageService() {
        try {
            encoder = new PngEncoder(PngEncoder.COLOR_TRUECOLOR_ALPHA, PngEncoder.BEST_COMPRESSION);
        } catch (Exception e) {
            System.out.println("Library for fast image encoding not found. Download the library from http://objectplanet.com/pngencoder/");
        }
    }

    public String encodeImage(BufferedImage window) {
        return Base64.encodeBase64String(getPngImage(window));
    }

    public byte[] getPngImage(BufferedImage imageContent) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (encoder != null) {
                encoder.encode(imageContent, baos);
            } else {
                ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
                ImageIO.write(imageContent, "png", ios);
            }
            byte[] result = baos.toByteArray();
            baos.close();
            return result;
        } catch (IOException e) {
            System.out.println("Writing image interupted:" + e.getMessage());
        }
        return null;
    }

    public WindowDecoratorThemeIfc getWindowDecorationTheme() {
        if (windowDecorationTheme == null) {
            String implClassName = System.getProperty(WindowDecoratorThemeIfc.DECORATION_THEME_IMPL_PROP, WindowDecoratorThemeIfc.DECORATION_THEME_IMPL_DEFAULT);
            Class<?> implclass = null;
            try {
                implclass = ImageService.class.getClassLoader().loadClass(implClassName);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                try {
                    implclass = ImageService.class.getClassLoader().loadClass(WindowDecoratorThemeIfc.DECORATION_THEME_IMPL_DEFAULT);
                } catch (ClassNotFoundException e1) {
                    System.err.println("Fatal error:Default decoration theme not found.");
                    System.exit(1);
                }
            }
            if (WindowDecoratorThemeIfc.class.isAssignableFrom(implclass)) {
                try {
                    WindowDecoratorThemeIfc theme = (WindowDecoratorThemeIfc) implclass.newInstance();
                    this.windowDecorationTheme = theme;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.err.println("Fatal error: Decoration theme not instance of WindowDecoratorThemeIfc:" + implclass.getCanonicalName());
                System.exit(1);
            }
        }
        return windowDecorationTheme;
    }

    @Override
    public byte[] generatePDF(List<BufferedImage> pages) {
        ByteArrayOutputStream os= new ByteArrayOutputStream();
        PDDocument doc = null;
        try {
            doc = new PDDocument();
            for (BufferedImage image : pages) {
                PDPage page = new PDPage();
                PDPageContentStream content = new PDPageContentStream(doc, page);
                PDXObjectImage ximage = new PDPixelMap(doc, image);
                content.drawImage(ximage, 0, 0);
                content.close();
                doc.addPage(page);
            }
            doc.save(os);
            doc.close();
        } catch (Exception ie) {
            ie.printStackTrace();
        }
        return os.toByteArray();
    }

}
