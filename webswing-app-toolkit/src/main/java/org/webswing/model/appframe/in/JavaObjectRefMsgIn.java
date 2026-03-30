package org.webswing.model.appframe.in;

import java.io.Serial;
import java.util.List;

import org.webswing.model.MsgIn;

public class JavaObjectRefMsgIn implements MsgIn {
  @Serial
  private static final long serialVersionUID = -1260785304443300962L;
  private String id;
  private List<String> methods;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<String> getMethods() {
    return methods;
  }

  public void setMethods(List<String> methods) {
    this.methods = methods;
  }

}
