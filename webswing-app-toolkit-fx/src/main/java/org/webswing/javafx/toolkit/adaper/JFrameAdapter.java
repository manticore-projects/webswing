package org.webswing.javafx.toolkit.adaper;

import java.awt.Window;

import javax.swing.JFrame;
import javax.swing.JRootPane;

public class JFrameAdapter extends JFrame implements WindowAdapter {
  public JFrameAdapter(boolean titled) {
    super();
    if (!titled) {
      setUndecorated(true);
      getRootPane().setWindowDecorationStyle(JRootPane.NONE);
    }
  }

  @Override
  public Window getThis() {
    return this;
  }

  @Override
  public void setModal(boolean b) {
    // do nothing
  }
}
