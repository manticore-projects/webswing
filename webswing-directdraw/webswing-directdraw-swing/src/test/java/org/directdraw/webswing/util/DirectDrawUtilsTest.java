package org.directdraw.webswing.util;

import org.junit.Before;
import org.junit.Test;
import org.webswing.directdraw.DirectDraw;
import org.webswing.directdraw.model.DrawInstruction;
import org.webswing.directdraw.toolkit.DrawInstructionFactory;
import org.webswing.directdraw.toolkit.WebGraphics;
import org.webswing.directdraw.toolkit.WebImage;
import org.webswing.directdraw.util.DirectDrawUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DirectDrawUtilsTest {

  DirectDraw dd;
  private DrawInstructionFactory f;

  @Before
  public void setUp() throws Exception {
    dd = new DirectDraw();
    f = dd.getInstructionFactory();
  }

  @Test
  public void testGroupedGraphicsCreate() {
    WebImage webImage = new WebImage(dd, 10, 10);
    List<DrawInstruction> inst = new ArrayList<DrawInstruction>();

    inst.add(f.createGraphics((WebGraphics) webImage.getGraphics()));// merged in one graphics
                                                                     // create
    inst.add(f.transform(new AffineTransform(1, 0, 0, 1, 10, 10)));
    inst.add(f.transform(new AffineTransform(1, 0, 0, 1, 10, 10)));
    inst.add(f.setPaint(Color.black));
    inst.add(f.setPaint(Color.blue));
    inst.add(f.setComposite(AlphaComposite.Dst));
    inst.add(f.setComposite(AlphaComposite.Src));
    inst.add(f.setStroke(new BasicStroke(1)));
    inst.add(f.setStroke(new BasicStroke(2)));

    inst.add(f.draw(new Rectangle(), null));// draw

    inst.add(f.transform(new AffineTransform(1, 0, 0, 1, 10, 10)));// ignored
    inst.add(f.setPaint(Color.black));
    inst.add(f.setComposite(AlphaComposite.Dst));
    inst.add(f.setStroke(new BasicStroke(1)));

    DirectDrawUtils.optimizeInstructions(dd, inst);
    assertEquals("Count not valid", 2, inst.size());
    assertEquals("Transform not expected", new AffineTransform(1, 0, 0, 1, 20, 20),
        inst.get(0).getArg(1).getValue());
    assertEquals("Stroke not expected", new BasicStroke(2), inst.get(0).getArg(2).getValue());
    assertEquals("Composite not expected", AlphaComposite.Src, inst.get(0).getArg(3).getValue());
    assertEquals("Color not expected", Color.blue, inst.get(0).getArg(4).getValue());
  }

  @Test
  public void testUnusedGraphicsCreate() {
    WebImage webImage = new WebImage(dd, 10, 10);
    List<DrawInstruction> inst = new ArrayList<DrawInstruction>();

    inst.add(f.createGraphics((WebGraphics) webImage.getGraphics()));// ignored because not used
    inst.add(f.transform(new AffineTransform(1, 0, 0, 1, 10, 10)));
    inst.add(f.transform(new AffineTransform(1, 0, 0, 1, 10, 10)));
    inst.add(f.setPaint(Color.black));
    inst.add(f.setPaint(Color.blue));
    inst.add(f.setComposite(AlphaComposite.Dst));
    inst.add(f.setComposite(AlphaComposite.Src));
    inst.add(f.setStroke(new BasicStroke(1)));
    inst.add(f.setStroke(new BasicStroke(2)));

    inst.add(f.createGraphics((WebGraphics) webImage.getGraphics()));// merged in one graphics
                                                                     // create
    inst.add(f.transform(new AffineTransform(1, 0, 0, 1, 10, 10)));
    inst.add(f.setPaint(Color.blue));
    inst.add(f.setComposite(AlphaComposite.Dst));
    inst.add(f.setStroke(new BasicStroke(2)));

    inst.add(f.draw(new Rectangle(), null));// draw

    inst.add(f.createGraphics((WebGraphics) webImage.getGraphics()));// ingored
    inst.add(f.transform(new AffineTransform(1, 0, 0, 1, 10, 10)));
    inst.add(f.setPaint(Color.black));
    inst.add(f.setComposite(AlphaComposite.Dst));
    inst.add(f.setStroke(new BasicStroke(1)));

    DirectDrawUtils.optimizeInstructions(dd, inst);
    assertEquals("Count not valid", 2, inst.size());
    assertEquals("Transform not expected", new AffineTransform(1, 0, 0, 1, 10, 10),
        inst.get(0).getArg(1).getValue());
    assertEquals("Stroke not expected", new BasicStroke(2), inst.get(0).getArg(2).getValue());
    assertEquals("Composite not expected", AlphaComposite.Dst, inst.get(0).getArg(3).getValue());
    assertEquals("Color not expected", Color.blue, inst.get(0).getArg(4).getValue());
  }

  @Test
  public void testMergedPaints() {
    WebImage webImage = new WebImage(dd, 10, 10);
    List<DrawInstruction> inst = new ArrayList<DrawInstruction>();

    inst.add(f.createGraphics((WebGraphics) webImage.getGraphics()));// graphics

    inst.add(f.draw(new Rectangle(), null));// draw

    inst.add(f.setPaint(new TexturePaint(new BufferedImage(10, 10, BufferedImage.TYPE_3BYTE_BGR),
        new Rectangle())));// merged
    inst.add(f.setPaint(Color.green));

    inst.add(f.draw(new Rectangle(), null));// draw

    DirectDrawUtils.optimizeInstructions(dd, inst);
    assertEquals("Count not valid", 4, inst.size());
    assertEquals("Color not expected", Color.green, inst.get(2).getArg(0).getValue());
  }

  @Test
  public void testMergedTransforms() {
    WebImage webImage = new WebImage(dd, 10, 10);
    List<DrawInstruction> inst = new ArrayList<DrawInstruction>();

    inst.add(f.createGraphics((WebGraphics) webImage.getGraphics()));// graphics

    inst.add(f.draw(new Rectangle(), null));// draw

    inst.add(f.transform(new AffineTransform(1, 0, 0, 1, 10, 10)));
    inst.add(f.transform(new AffineTransform(1, 0, 0, 1, 10, 60)));

    inst.add(f.draw(new Rectangle(), null));// draw

    DirectDrawUtils.optimizeInstructions(dd, inst);
    assertEquals("Count not valid", 4, inst.size());
    assertEquals("Transform not expected", new AffineTransform(1, 0, 0, 1, 20, 70),
        inst.get(2).getArg(0).getValue());
  }

  @Test
  public void testMergedComposites() {
    WebImage webImage = new WebImage(dd, 10, 10);
    List<DrawInstruction> inst = new ArrayList<DrawInstruction>();

    inst.add(f.createGraphics((WebGraphics) webImage.getGraphics()));// graphics

    inst.add(f.draw(new Rectangle(), null));// draw

    inst.add(f.setComposite(AlphaComposite.Dst));
    inst.add(f.setComposite(AlphaComposite.SrcAtop));

    inst.add(f.draw(new Rectangle(), null));// draw

    DirectDrawUtils.optimizeInstructions(dd, inst);
    assertEquals("Count not valid", 4, inst.size());
    assertEquals("Composite not expected", AlphaComposite.SrcAtop,
        inst.get(2).getArg(0).getValue());
  }

  @Test
  public void testCreateAfterTransform() {
    WebImage webImage = new WebImage(dd, 10, 10);
    List<DrawInstruction> inst = new ArrayList<DrawInstruction>();

    WebGraphics g1 = (WebGraphics) webImage.getGraphics();
    inst.add(f.createGraphics(g1));
    inst.add(f.draw(new Rectangle(0, 0, 1, 1), null));
    inst.add(f.transform(new AffineTransform(1, 0, 0, 1, 5, 3)));

    WebGraphics g2 = (WebGraphics) webImage.getGraphics();
    inst.add(f.createGraphics(g2));
    inst.add(f.draw(new Rectangle(0, 0, 1, 1), null));
    inst.add(f.disposeGraphics(g2));

    inst.add(f.switchGraphics(g1));
    inst.add(f.transform(new AffineTransform(1, 0, 0, 1, -5, -3)));
    inst.add(f.draw(new Rectangle(0, 0, 1, 1), null));

    DirectDrawUtils.optimizeInstructions(dd, inst);

    List<DrawInstruction> originalInstructions = new ArrayList<DrawInstruction>(inst);
    assertEquals(9, inst.size());
    assertEquals("Create graphics 1", originalInstructions.get(0), inst.get(0));
    assertEquals("Draw rectangle 1", originalInstructions.get(1), inst.get(1));
    assertEquals("Transform 1", new AffineTransform(1, 0, 0, 1, 5, 3),
        inst.get(2).getArg(0).getValue());

    assertEquals("Create graphics 2", originalInstructions.get(3), inst.get(3));
    assertEquals("Draw rectangle 2", originalInstructions.get(4), inst.get(4));
    assertEquals("Dispose graphics 2", originalInstructions.get(5), inst.get(5));

    assertEquals("Switch graphics", originalInstructions.get(6), inst.get(6));
    assertEquals("Transform 2", new AffineTransform(1, 0, 0, 1, -5, -3),
        inst.get(7).getArg(0).getValue());
    assertEquals("Draw rectangle 2", originalInstructions.get(8), inst.get(8));
  }

  @Test
  public void testCreateGraphicsWithSingleTransform() {
    WebImage webImage = new WebImage(dd, 10, 10);
    List<DrawInstruction> inst = new ArrayList<DrawInstruction>();

    WebGraphics g1 = (WebGraphics) webImage.getGraphics();
    inst.add(f.createGraphics(g1));
    inst.add(f.draw(new Rectangle(0, 0, 1, 1), null));

    WebGraphics g2 = (WebGraphics) webImage.getGraphics();
    inst.add(f.createGraphics(g2));
    inst.add(f.transform(new AffineTransform(1, 0, 0, 1, -11, -11)));

    inst.add(f.switchGraphics(g1));
    inst.add(f.draw(new Rectangle(0, 0, 1, 1), null));

    inst.add(f.switchGraphics(g2));
    inst.add(f.disposeGraphics(g2));

    DirectDrawUtils.optimizeInstructions(dd, inst);

    List<DrawInstruction> originalInstructions = new ArrayList<DrawInstruction>(inst);
    assertEquals(4, inst.size());
    assertEquals("Create graphics 1", originalInstructions.get(0), inst.get(0));
    assertEquals("Draw rectangle 1", originalInstructions.get(1), inst.get(1));

    assertEquals("Switch graphics", originalInstructions.get(2), inst.get(2));
    assertEquals("Draw rectangle 2", originalInstructions.get(3), inst.get(3));
  }
}
