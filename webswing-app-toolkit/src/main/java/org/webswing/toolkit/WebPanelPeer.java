package org.webswing.toolkit;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.peer.PanelPeer;

import javax.swing.SwingUtilities;

import org.webswing.common.GraphicsWrapper;
import org.webswing.toolkit.util.Util;

abstract public class WebPanelPeer extends WebContainerPeer implements PanelPeer {

  private Insets insets;

  public WebPanelPeer(Container t) {
    super(t);
    insets = new Insets(0, 0, 0, 0);
  }

  @Override
  public String getGuid() {
    return getParentWindowPeer().getGuid();
  }

  @Override
  public Insets getInsets() {
    return insets;
  }

  @SuppressWarnings("deprecation")
  WebWindowPeer getParentWindowPeer() {
    Panel target = (Panel) getTarget();
    Window w = SwingUtilities.windowForComponent(target);
    return (WebWindowPeer) Util.getPeer(w);
  }

  @Override
  public void show() {
    getParentWindowPeer().addHwLayer(this);
  }

  @Override
  public void hide() {
    getParentWindowPeer().removeHwLayer(this);
  }

  @Override
  public Graphics getGraphics() {
    GraphicsWrapper g = (GraphicsWrapper) super.getGraphics();
    g.setOffset(SwingUtilities.convertPoint((Component) target, new Point(0, 0),
        (Component) getParentWindowPeer().getTarget()));
    return g;
  }

  @Override
  protected void notifyWindowClosed() {
    // do nothing here
  }

  @Override
  protected void notifyWindowBoundsChanged(Rectangle rectangle) {
    // do nothing here
  }
}
