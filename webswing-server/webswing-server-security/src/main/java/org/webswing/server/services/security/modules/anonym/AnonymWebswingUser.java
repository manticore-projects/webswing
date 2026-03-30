package org.webswing.server.services.security.modules.anonym;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.webswing.server.common.model.security.WebswingAction;
import org.webswing.server.common.service.security.AuthenticatedWebswingUser;

import com.google.common.collect.Lists;

public class AnonymWebswingUser extends AuthenticatedWebswingUser {

  @Serial
  private static final long serialVersionUID = 1157215452456795382L;

  private String userId;
  private List<String> roles = Lists.newArrayList(WebswingAction.AccessType.basic.name());

  public AnonymWebswingUser(String userIdParam) {
    this.userId = userIdParam;
  }

  @Override
  public String getUserId() {
    return userId;
  }

  @Override
  public List<String> getUserRoles() {
    return roles;
  }

  @Override
  public Map<String, Serializable> getUserAttributes() {
    return Collections.emptyMap();
  }

  @Override
  public Map<String, Serializable> getUserSessionAttributes() {
    return Collections.emptyMap();
  }

  @Override
  public boolean hasRole(String role) {
    return WebswingAction.AccessType.basic.name().equals(role);
  }

  public boolean isAuthenticated() {
    return true;
  }

};
