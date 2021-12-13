package org.webswing.server.services.security.modules.shiro;

import org.webswing.server.common.service.security.AuthenticatedWebswingUser;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.realm.AuthorizingRealm;

public class ShiroWebswingUser extends AuthenticatedWebswingUser {

	private static final long serialVersionUID = 4579855803170690448L;
	
	private final AuthorizingRealm authzrealm;
	private final AuthenticationInfo authtInfo;

	public ShiroWebswingUser(AuthorizingRealm authzrealm, AuthenticationInfo authtInfo) {
		super();
		this.authzrealm = authzrealm;
		this.authtInfo = authtInfo;
	}

	@Override
	public String getUserId() {
		return (String) this.authtInfo.getPrincipals().getPrimaryPrincipal();
	}

	@Override
	public List<String> getUserRoles() {
		return Collections.emptyList();
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
		return authzrealm.hasRole(this.authtInfo.getPrincipals(), role);
	}

	public String getPassword() {
		return null;
	}
}