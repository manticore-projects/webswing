package org.webswing.server.api.services.rest;

import java.io.File;

import org.webswing.server.api.base.AbstractUrlHandler;
import org.webswing.server.api.base.UrlHandler;
import org.webswing.server.model.exception.WsException;

public abstract class AbstractAppRestHandler extends AbstractUrlHandler implements UrlHandler {

  public AbstractAppRestHandler(UrlHandler parent) {
    super(parent);
  }

  protected abstract String getVersion() throws WsException;

  protected abstract File getAppIcon() throws WsException;

  protected abstract void ping() throws WsException;

}
