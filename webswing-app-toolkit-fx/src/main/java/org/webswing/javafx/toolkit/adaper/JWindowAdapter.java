package org.webswing.javafx.toolkit.adaper;

import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JRootPane;

public class JWindowAdapter extends JDialog implements WindowAdapter {
  public JWindowAdapter(Window parent) {
    super(parent);
    setUndecorated(true);
    getRootPane().setWindowDecorationStyle(JRootPane.NONE);
    setFocusableWindowState(false);
    setType(java.awt.Window.Type.POPUP);
  }

  @Override
  public Window getThis() {
    return this;
  }

}
