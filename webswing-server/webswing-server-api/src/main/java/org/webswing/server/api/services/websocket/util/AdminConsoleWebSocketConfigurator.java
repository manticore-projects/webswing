package org.webswing.server.api.services.websocket.util;

import javax.websocket.server.ServerEndpointConfig;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class AdminConsoleWebSocketConfigurator extends ServerEndpointConfig.Configurator {

  @Inject
  private static Injector injector;

  @Override
  public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
    return injector.getInstance(endpointClass);
  }

}
