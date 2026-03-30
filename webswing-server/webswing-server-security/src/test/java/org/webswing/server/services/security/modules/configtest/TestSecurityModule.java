package org.webswing.server.services.security.modules.configtest;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.webswing.server.common.service.security.AuthenticatedWebswingUser;
import org.webswing.server.services.security.api.WebswingAuthenticationException;
import org.webswing.server.services.security.modules.AbstractSecurityModule;

public class TestSecurityModule extends AbstractSecurityModule<TestSecurityModuleConfig> {

  public TestSecurityModule(TestSecurityModuleConfig config) {
    super(config);
  }

  @Override
  protected AuthenticatedWebswingUser authenticate(HttpServletRequest request)
      throws WebswingAuthenticationException {
    return null;
  }

  @Override
  protected void serveLoginPartial(HttpServletRequest request, HttpServletResponse response,
      WebswingAuthenticationException exception) throws IOException {

  }

}
