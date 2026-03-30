package org.webswing.server.api.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.Constants;
import org.webswing.server.api.services.security.login.SecuredPathHandler;
import org.webswing.server.api.util.SecurityUtil;
import org.webswing.server.api.util.ServerApiUtil;
import org.webswing.server.common.model.security.WebswingAction;
import org.webswing.server.common.service.security.AbstractWebswingUser;
import org.webswing.server.common.service.security.SecurableService;
import org.webswing.server.common.util.CommonUtil;
import org.webswing.server.model.exception.WsException;

public abstract class AbstractUrlHandler implements UrlHandler, SecurableService {
	private static final Logger log = LoggerFactory.getLogger(AbstractUrlHandler.class);

	private final UrlHandler parent;
	private final List<UrlHandler> childHandlers = Collections.synchronizedList(new LinkedList<UrlHandler>());

	public AbstractUrlHandler(UrlHandler parent) {
		this.parent = parent;
	}

	@Override
	public void init() {
		synchronized (childHandlers) {
			for (UrlHandler handler : childHandlers) {
				try {
					handler.init();
				} catch (Exception e) {
					log.error("Failed to initialize child handler: " + handler.getClass().getName(), e);
				}
			}
		}
	}

	@Override
	public void destroy() {
		synchronized (childHandlers) {
			for (UrlHandler handler : childHandlers) {
				try {
					handler.destroy();
				} catch (Exception e) {
					log.error("Failed to destroy child handler: " + handler.getClass().getName(), e);
				}
			}
			childHandlers.clear();
		}
	}

	@Override
	public boolean serve(HttpServletRequest req, HttpServletResponse res) throws WsException {
		String pathinfo = getPathInfo(req);
		List<UrlHandler> localHandlerList = new LinkedList<UrlHandler>(childHandlers);
		for (UrlHandler child : localHandlerList) {
			if (isSubPath(toPath(child.getPathMapping()), pathinfo)) {
				boolean served = child.serve(req, res);
				if (served) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Object secureServe(HttpServletRequest req, HttpServletResponse res) throws WsException {
		return serve(req, res);
	}

	protected void handleCorsHeaders(HttpServletRequest req, HttpServletResponse res) throws WsException {
		// Read Origin once into a local variable to avoid TOCTOU and
		// to make it clear to static analyzers that the value is validated
		// by isOriginAllowed() before being reflected in the response.
		String origin = req.getHeader("Origin");
		if (origin != null && isOriginAllowed(origin)) {
			res.setHeader("Access-Control-Allow-Origin", origin);
			res.setHeader("Access-Control-Allow-Credentials", "true");
			res.setHeader("Access-Control-Expose-Headers", Constants.HTTP_ATTR_ARGS + ", " + Constants.HTTP_ATTR_RECORDING_FLAG + ", X-Cache-Date, X-Atmosphere-tracking-id, X-Requested-With");
			// Indicate that the response varies by Origin so caches don't
			// serve one origin's CORS headers to another origin
			res.setHeader("Vary", "Origin");

			if ("OPTIONS".equals(req.getMethod())) {
				res.setHeader("Access-Control-Allow-Methods", "OPTIONS, GET, POST, DELETE");
				res.setHeader("Access-Control-Allow-Headers", "Authorization, " + Constants.HTTP_ATTR_ARGS + ", " + Constants.HTTP_ATTR_RECORDING_FLAG + ", X-Requested-With, Origin, Content-Type, Content-Range, Content-Disposition, Content-Description, X-Atmosphere-Framework, X-Cache-Date, X-Atmosphere-tracking-id, X-Atmosphere-Transport");
				res.setHeader("Access-Control-Max-Age", "-1");
			}
		}
	}

	protected boolean isOriginAllowed(String header) {
		return false;
	}

	protected boolean isSameOrigin(HttpServletRequest req) {
		String origin = req.getHeader("Origin");
		String host = req.getHeader("X-Forwarded-Host");
		if(origin==null){
			return true; //IE11 on Win7 does not send Origin header
		}
		if (host == null) {
			host = req.getHeader("Host");
		}
		if (origin != null && host != null) {
			String originHost = origin.indexOf("://") >= 0 ? origin.substring(origin.indexOf("://") + 3) : origin;
			if (StringUtils.equals(originHost, host)) {
				return true;
			}
		}
		return false;
	}

	public String getFullPathMapping() {
		String handlerPath = toPath(getPathMapping());
		if (this.parent != null) {
			String parentMapping = parent.getFullPathMapping();
			handlerPath = parentMapping + handlerPath;
		} else {
			handlerPath = ServerApiUtil.getContextPath(getServletContext()) + handlerPath;
		}
		return handlerPath;
	}

	public String getPathInfo(HttpServletRequest req) {
		String fullHandlerPath = getFullPathMapping();
		String requestPath = toPath(ServerApiUtil.getContextPath(getServletContext()) + req.getPathInfo());
		if (isSubPath(fullHandlerPath, requestPath)) {
			return toPath(requestPath.substring(fullHandlerPath.length()));
		} else {
			return "/";
		}
	}

	protected abstract String getPath();

	public String getPathMapping() {
		String path = toPath(getPath());
		return path;
	}

	public boolean isSubPath(String subpath, String path) {
		return CommonUtil.isSubPath(subpath, path);
	}

	public static String toPath(String path) {
		return CommonUtil.toPath(path);
	}

	public void registerFirstChildUrlHandler(UrlHandler handler) {
		synchronized (childHandlers) {
			childHandlers.add(0, handler);
		}
	}

	@Override
	public void registerChildUrlHandler(UrlHandler handler) {
		synchronized (childHandlers) {
			childHandlers.add(handler);
		}
	}

	@Override
	public void removeChildUrlHandler(UrlHandler handler) {
		synchronized (childHandlers) {
			if (childHandlers.contains(handler)) {
				childHandlers.remove(handler);
				handler.destroy();
			}
		}
	}

	public ServletContext getServletContext() {
		return parent.getServletContext();
	}

	public String getSecuredPath() {
		if (SecuredPathHandler.class.isAssignableFrom(this.getClass())) {
			SecuredPathHandler provider = (SecuredPathHandler) this;
			if (provider.get() != null) {
				return getFullPathMapping();
			}
		}
		if (parent == null) {
			return getFullPathMapping();
		} else {
			return parent.getSecuredPath();
		}
	}

	public SecuredPathHandler getSecurityProvider() {
		if (SecuredPathHandler.class.isAssignableFrom(this.getClass())) {
			SecuredPathHandler provider = (SecuredPathHandler) this;
			if (provider.get() != null) {
				return provider;
			}
		}
		if (parent == null) {
			return (SecuredPathHandler) this;
		} else {
			return parent.getSecurityProvider();
		}
	}

	public long getLastModified(HttpServletRequest req) {
		return -1;
	}

	public AbstractWebswingUser getUser() {
		return SecurityUtil.getUser(this);
	}

	public AbstractWebswingUser getMasterUser() {
		return SecurityUtil.getUser(getRootHandler());
	}

	@Override
	public UrlHandler getRootHandler() {
		if (parent != null) {
			return parent.getRootHandler();
		} else {
			return this;
		}
	}

	@Override
	public void checkPermission(WebswingAction action) throws WsException {
		AbstractWebswingUser user = getUser();
		checkPermission(user, action);
	}

	@Override
	public void checkMasterPermission(WebswingAction action) throws WsException {
		AbstractWebswingUser user = getMasterUser();
		checkPermission(user, action);
	}

	public void checkPermissionLocalOrMaster(WebswingAction a) throws WsException {
		try {
			checkPermission(a);
		} catch (WsException e) {
			checkMasterPermission(a);
		}
	}

	private void checkPermission(AbstractWebswingUser user, WebswingAction action) throws WsException {
		if (user != null) {
			if (user.isPermitted(action.name())) {
				return;
			}
		}
		throw new WsException("User '" + user + "' is not allowed to execute action '" + action + "'", HttpServletResponse.SC_UNAUTHORIZED);
	}

	protected void sendContent(HttpServletResponse res, String content) throws WsException {
		try (PrintWriter writer = res.getWriter()) {
			writer.write(content);
			writer.flush();
		} catch (IOException e) {
			throw new WsException(e);
		}
	}

	protected void sendFile(HttpServletResponse res, File file) throws WsException {
		try (FileInputStream fis = new FileInputStream(file)) {
			IOUtils.copy(fis, res.getOutputStream());
		} catch (IOException e) {
			throw new WsException(e);
		}
	}

}