package org.webswing.ext.services;

import java.awt.Window;
import java.util.List;

import org.webswing.model.appframe.out.ComponentTreeMsgOut;

public interface ToolkitFXService {

  public List<ComponentTreeMsgOut> requestNodeTree(Object node);

  public void registerStage(Object stage);

  public boolean isFXWindow(Window window);

}
