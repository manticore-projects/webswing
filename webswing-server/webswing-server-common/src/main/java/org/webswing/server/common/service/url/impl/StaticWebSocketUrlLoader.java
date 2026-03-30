package org.webswing.server.common.service.url.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.webswing.Constants;
import org.webswing.server.common.service.url.WebSocketUrlLoader;

import com.google.common.base.Splitter;

public class StaticWebSocketUrlLoader implements WebSocketUrlLoader {

  private Set<String> webSocketUrls = Collections.synchronizedSet(new HashSet<>());

  @Override
  public Set<String> reload() {
    synchronized (webSocketUrls) {
      webSocketUrls.clear();
      String urls = System.getProperty(Constants.SERVER_WEBSOCKET_URL, "");
      Splitter.on(',').trimResults().omitEmptyStrings().split(urls).forEach(url -> {
        if (url.endsWith("/")) {
          url = url.substring(0, url.length() - 1);
        }
        webSocketUrls.add(url);
      });
      return webSocketUrls;
    }
  }

}
