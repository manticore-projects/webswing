package org.webswing.server.api.services.security.modules.impl;

import java.util.Map;

import org.webswing.server.api.services.security.modules.SecurityModuleFactory;
import org.webswing.server.api.services.security.modules.SecurityModuleWrapper;
import org.webswing.server.common.util.ConfigUtil;
import org.webswing.server.services.security.api.BuiltInModules;
import org.webswing.server.services.security.api.SecurityContext;
import org.webswing.server.services.security.api.WebswingSecurityConfig;
import org.webswing.server.services.security.api.WebswingSecurityModule;
import org.webswing.server.services.security.api.WebswingSecurityModuleConfig;
import org.webswing.server.services.security.modules.noaccess.NoAccessSecurityModule;

import com.google.inject.Singleton;

@Singleton
public class DefaultSecurityModuleFactoryImpl implements SecurityModuleFactory {

  public DefaultSecurityModuleFactoryImpl() {}

  public WebswingSecurityModule create(SecurityContext context, WebswingSecurityConfig config) {
    if (BuiltInModules.getSecurityModuleClassName(config.getModule()) != null) {
      return new SecurityModuleWrapper(context, config);
    } else {
      return null;
    }
  }

  public WebswingSecurityModule createNoAccess(String msgKey, SecurityContext context,
      WebswingSecurityConfig config) {
    Map<String, Object> map = config != null ? config.getConfig() : null;
    WebswingSecurityModuleConfig c =
        ConfigUtil.instantiateConfig(map, WebswingSecurityModuleConfig.class, context);
    return new NoAccessSecurityModule(msgKey, c);
  }

}
