package org.webswing.toolkit;

import java.awt.Container;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JComponent;

import org.webswing.Constants;
import org.webswing.component.HtmlPanelImpl;
import org.webswing.dispatch.PaintDispatcher;
import org.webswing.model.Msg;
import org.webswing.model.c2s.ActionEventMsgIn;
import org.webswing.model.c2s.ActionEventMsgIn.ActionEventType;
import org.webswing.model.c2s.ConnectionHandshakeMsgIn;
import org.webswing.model.internal.ApiCallMsgInternal;
import org.webswing.model.internal.ApiCallMsgInternal.ApiMethod;
import org.webswing.model.internal.ApiEventMsgInternal;
import org.webswing.model.internal.ApiEventMsgInternal.ApiEventType;
import org.webswing.toolkit.api.WebswingApi;
import org.webswing.toolkit.api.WebswingApiException;
import org.webswing.toolkit.api.action.WebActionEvent;
import org.webswing.toolkit.api.action.WebActionListener;
import org.webswing.toolkit.api.action.WebWindow;
import org.webswing.toolkit.api.clipboard.BrowserTransferable;
import org.webswing.toolkit.api.clipboard.PasteRequestContext;
import org.webswing.toolkit.api.clipboard.WebswingClipboardData;
import org.webswing.toolkit.api.component.Dockable;
import org.webswing.toolkit.api.component.HtmlPanel;
import org.webswing.toolkit.api.lifecycle.OnBeforeShutdownEvent;
import org.webswing.toolkit.api.lifecycle.ShutdownReason;
import org.webswing.toolkit.api.lifecycle.WebswingShutdownListener;
import org.webswing.toolkit.api.security.UserEvent;
import org.webswing.toolkit.api.security.WebswingUser;
import org.webswing.toolkit.api.security.WebswingUserListener;
import org.webswing.toolkit.api.url.WebswingUrlState;
import org.webswing.toolkit.api.url.WebswingUrlStateChangeEvent;
import org.webswing.toolkit.api.url.WebswingUrlStateChangeListener;
import org.webswing.toolkit.util.DeamonThreadFactory;
import org.webswing.toolkit.util.GitRepositoryState;
import org.webswing.toolkit.util.Logger;
import org.webswing.toolkit.util.Services;
import org.webswing.toolkit.util.Util;

public class WebswingApiImpl implements WebswingApi {
	private final List<WebswingShutdownListener> shutdownListeners = Collections.synchronizedList(new ArrayList<WebswingShutdownListener>());
	private final List<WebswingUserListener> userConnectionListeners = Collections.synchronizedList(new ArrayList<WebswingUserListener>());
	private final List<WebswingUrlStateChangeListener> urlStateChangeListeners = Collections.synchronizedList(new ArrayList<WebswingUrlStateChangeListener>());
	private final List<WebActionListener> browserActionListeners = Collections.synchronizedList(new ArrayList<WebActionListener>());
	private ExecutorService apiProcessor = Executors.newSingleThreadExecutor(DeamonThreadFactory.getInstance("Webswing API Processor"));
	private WebswingUser primaryUser;
	private WebswingUser mirrorUser;
	private WebswingUrlState state = parseState(System.getProperty(Constants.SWING_START_SYS_PROP_INITIAL_URL));

	@Override
	public WebswingUser getPrimaryUser() {
		return primaryUser;
	}

	@Override
	public WebswingUser getMirrorViewUser() {
		return mirrorUser;
	}

	@Override
	public Boolean primaryUserHasRole(String role) throws WebswingApiException {
		try {
			return apiCall(ApiMethod.HasRole, role);
		} catch (WebswingApiException e) {
			Logger.error("Failed to resolve role assignemnt.", e);
			throw e;
		}
	}

	@Override
	public Boolean primaryUserIsPermitted(String permission) throws WebswingApiException {
		try {
			return apiCall(ApiMethod.IsPermitted, permission);
		} catch (WebswingApiException e) {
			Logger.error("Failed to resolve role assignemnt.", e);
			throw e;
		}
	}

	public void addUserConnectionListener(WebswingUserListener listener) {
		if (listener != null) {
			userConnectionListeners.add(listener);
		}
	}

	public void removeUserConnectionListener(WebswingUserListener listener) {
		if (listener != null) {
			userConnectionListeners.remove(listener);
		}
	}

	@Override
	public void notifyShutdown(int forceKillTimeout) {
		Util.getWebToolkit().getPaintDispatcher().notifyApplicationExiting(forceKillTimeout);
	}

	@Override
	public void addShutdownListener(WebswingShutdownListener listener) {
		if (listener != null) {
			shutdownListeners.add(listener);
		}
	}

	@Override
	public void removeShutdownListener(WebswingShutdownListener listener) {
		if (listener != null) {
			shutdownListeners.remove(listener);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T apiCall(ApiMethod m, Serializable... args) throws WebswingApiException {
		ApiCallMsgInternal msg = new ApiCallMsgInternal();
		msg.setMethod(m);
		msg.setArgs(args);
		try {
			ApiCallMsgInternal result = (ApiCallMsgInternal) Services.getConnectionService().sendObjectSync(msg, msg.getCorrelationId());
			return ((T) result.getResult());
		} catch (Exception e) {
			throw new WebswingApiException("API call failed.", e);
		}
	}

	void processEvent(final Msg msg) {
		if (msg instanceof ConnectionHandshakeMsgIn) {
			final ConnectionHandshakeMsgIn event = (ConnectionHandshakeMsgIn) msg;
			WebswingUrlState state = parseState(event.getUrl());
			if (this.state == null || !this.state.equals(state)) {
				WebswingUrlState oldState = this.state;
				this.state = state;
				final WebswingUrlStateChangeEvent stateChangeEvent = createEvent(event.getUrl(), state, oldState);
				apiProcessor.submit(new Runnable() {
					@Override
					public void run() {
						synchronized (urlStateChangeListeners) {
							try {
								WebswingApiImpl.this.fireUrlChangeListener(stateChangeEvent);
							} catch (Exception e) {
								Logger.error("Processing URL change api event failed.", e);
							}
						}
					}
				});
			}
		} else if (msg instanceof ApiEventMsgInternal) {
			final ApiEventMsgInternal event = (ApiEventMsgInternal) msg;
			apiProcessor.submit(new Runnable() {
				@Override
				public void run() {
					synchronized (userConnectionListeners) {
						try {
							UserEvent e = new UserEvent(new WebswingUserInfo(event.getArgs()));
							switch (event.getEvent()) {
							case UserConnected:
								primaryUser = e.getUser();
								break;
							case UserDisconnected:
								primaryUser = null;
								break;
							case MirrorViewConnected:
								mirrorUser = e.getUser();
								break;
							case MirrorViewDisconnected:
								mirrorUser = null;
								break;
							default:
								break;
							}
							WebswingApiImpl.this.fireUserListener(event.getEvent(), e);
						} catch (Exception e) {
							Logger.error("Processing User api event failed.", e);
						}
					}
				}
			});
		} else if (msg instanceof ActionEventMsgIn) {
			ActionEventMsgIn action = (ActionEventMsgIn) msg;
			if (action != null) {
				apiProcessor.submit(() -> {
					if (action.getWindowId() != null) {
						Window w = Util.findWindowById(action.getWindowId());
						HtmlPanel hp = Util.findHtmlPanelById(action.getWindowId());
						
						if (w != null && w instanceof WebWindow) {
							if (action.getEventType() == ActionEventType.init) {
								((WebWindow) w).handleWindowInitialized();
							} else {
								((WebWindow) w).handleWebActionEvent(new WebActionEvent(action.getActionName(), action.getData(), action.getBinaryData()));
							}
						} else if (hp != null) {
							if (action.getEventType() == ActionEventType.init) {
								hp.handleWindowInitialized();
							} else {
								hp.handleWebActionEvent(new WebActionEvent(action.getActionName(), action.getData(), action.getBinaryData()));
							}
						} else if (action.getEventType() != ActionEventType.init) {
							// fire the general listeners
							// don't fire for init event type
							fireBrowserActionListener(new WebActionEvent(action.getActionName(), action.getData(), action.getBinaryData()));
						}
					} else {
						fireBrowserActionListener(new WebActionEvent(action.getActionName(), action.getData(), action.getBinaryData()));
					}
				});
			}
		}
	}

	private static WebswingUrlState parseState(String url) {
		if (url.contains("#")) {
			String hash = url.substring(url.indexOf("#") + 1);
			try {
				if (hash.contains("&")) {
					List<String> params = Arrays.asList(hash.split("&"));
					Iterator<String> i = params.iterator();
					String path = URLDecoder.decode(i.next(), "UTF-8");
					Map<String, String> parameters = new LinkedHashMap<String, String>();
					for (; i.hasNext(); ) {
						String param = i.next();
						String key = URLDecoder.decode(param, "UTF-8");
						String value = null;
						if (param.contains("=")) {
							int eq = param.indexOf("=");
							key = URLDecoder.decode(param.substring(0, eq), "UTF-8");
							value = URLDecoder.decode(param.substring(eq + 1), "UTF-8");
						}
						parameters.put(key, value);
					}
					return new WebswingUrlState(path, parameters);
				} else {
					String path = URLDecoder.decode(hash, "UTF-8");
					return new WebswingUrlState(path);
				}
			} catch (UnsupportedEncodingException e) {
				Logger.error("Failed to decode url", e);
			}
		}
		return new WebswingUrlState();
	}

	private WebswingUrlStateChangeEvent createEvent(String url, WebswingUrlState state, WebswingUrlState oldState) {
		return new WebswingUrlStateChangeEventImpl(url, new WebswingUrlState(state), oldState);
	}

	void fireShutdownListeners() {
		synchronized (shutdownListeners) {
			if (shutdownListeners.size() == 0) {
				Logger.info("No shutdown listener found. Using default shutdown procedure.");
				Util.getWebToolkit().defaultShutdownProcedure();
			} else {
				for (WebswingShutdownListener l : shutdownListeners) {
					try {
						l.onShutdown();
					} catch (Exception e) {
						Logger.error("Shutdown Listener failed.", e);
					}
				}
			}
		}
	}

	int fireBeforeShutdownListeners(ShutdownReason reason) {
		synchronized (shutdownListeners) {
			int maxDelay=0;
			for (WebswingShutdownListener l : shutdownListeners) {
				try {
					maxDelay= Math.max(l.onBeforeShutdown(new OnBeforeShutdownEvent(reason)),maxDelay);
				} catch (Exception e) {
					Logger.error("Before Shutdown Listener failed.", e);
				}
			}
			return maxDelay;
		}
	}

	private void fireUserListener(ApiEventType type, UserEvent event) {
		synchronized (userConnectionListeners) {
			for (WebswingUserListener l : userConnectionListeners) {
				try {
					switch (type) {
					case UserConnected:
						l.onPrimaryUserConnected(event);
						break;
					case UserDisconnected:
						l.onPrimaryUserDisconnected(event);
						break;
					case MirrorViewConnected:
						l.onMirrorViewConnected(event);
						break;
					case MirrorViewDisconnected:
						l.onMirrorViewDisconnected(event);
						break;
					default:
						break;
					}
				} catch (Exception e) {
					Logger.error("User Listener failed.", e);
				}
			}
		}
	}

	private void fireUrlChangeListener(WebswingUrlStateChangeEvent event) {
		synchronized (urlStateChangeListeners) {
			for (WebswingUrlStateChangeListener l : urlStateChangeListeners) {
				try {
					l.onUrlStateChange(event);
				} catch (Exception e) {
					Logger.error("Url change Listener failed.", e);
				}
			}
		}
	}

	private void fireBrowserActionListener(WebActionEvent actionEvent) {
		synchronized (browserActionListeners) {
			for (WebActionListener l : browserActionListeners) {
				try {
					l.actionPerformed(actionEvent);
				} catch (Exception e) {
					Logger.error("Browser action listener failed.", e);
				}
			}
		}
	}

	@Override
	public String getWebswingVersion() {
		return GitRepositoryState.getInstance().getDescribe();
	}

	@Override
	public void setUrlState(WebswingUrlState state) {
		this.setUrlState(state, false);
	}

	@Override
	public void setUrlState(WebswingUrlState state, boolean fireChangeEvent) {
		if (state == null) {
			state = new WebswingUrlState();
		}
		String url = "#";
		try {
			if (state.getPath() != null) {
				url += URLEncoder.encode(state.getPath(), "UTF-8");
			}
			if (state.getParameters() != null && state.getParameters().size() > 0) {

				for (String key : state.getParameters().keySet()) {
					url += "&" + URLEncoder.encode(key, "UTF-8");

					String value = state.getParameters().get(key);
					if (value != null) {
						url += "=" + URLEncoder.encode(value, "UTF-8");
					}
				}
			}
		} catch (UnsupportedEncodingException e) {
			Logger.error("Failed to encode URL state.", e);
		}
		if (!fireChangeEvent) {
			this.state = state;
		}
		Util.getWebToolkit().getPaintDispatcher().notifyUrlRedirect(url);
	}

	@Override
	public WebswingUrlState getUrlState() {
		return state == null ? null : new WebswingUrlState(state);
	}

	@Override
	public void addUrlStateChangeListener(WebswingUrlStateChangeListener listener) {
		if (listener != null) {
			urlStateChangeListeners.add(listener);
		}
	}

	@Override
	public void resetInactivityTimeout() {
		Services.getConnectionService().resetInactivityTimers();
	}

	@Override
	public void removeUrlStateChangeListener(WebswingUrlStateChangeListener listener) {
		if (listener != null) {
			urlStateChangeListeners.remove(listener);
		}
	}

	@Override
	public BrowserTransferable getBrowserClipboard() {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		if (clipboard instanceof WebClipboard) {
			return ((WebClipboard) clipboard).getBrowserClipboard();
		}
		return null;
	}

	@Override
	public BrowserTransferable getBrowserClipboard(PasteRequestContext ctx) {
		WebClipboard webclipboard = Util.getWebToolkit().getWebswingClipboard();
		return webclipboard.requestClipboard(ctx);
	}

	@Override
	public void sendClipboard(WebswingClipboardData content) {
		if (content != null) {
			PaintDispatcher paintDispatcher = Util.getWebToolkit().getPaintDispatcher();
			if (paintDispatcher != null) {
				paintDispatcher.notifyCopyEvent(content);
			}
		}
	}

	@Override
	public void sendClipboard() {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable transferable = clipboard.getContents(null);
		WebswingClipboardData content = WebClipboard.toWebswingClipboardData(transferable);
		if (content != null) {
			PaintDispatcher paintDispatcher = Util.getWebToolkit().getPaintDispatcher();
			if (paintDispatcher != null) {
				paintDispatcher.notifyCopyEvent(content);
			}
		}
	}

	@Override
	public void addBrowserActionListener(WebActionListener listener) {
		if (listener != null) {
			browserActionListeners.add(listener);
		}
	}

	@Override
	public void removeBrowserActionListener(WebActionListener listener) {
		if (listener != null) {
			browserActionListeners.remove(listener);
		}
	}

	@Override
	public void sendActionEvent(String actionName, String data, byte[] binaryData) {
		sendActionEvent((String) null, actionName, data, binaryData);
	}

	@Override
	public void sendActionEvent(WebWindow webWindow, String actionName, String data, byte[] binaryData) {
		if (webWindow == null || !(webWindow instanceof Window || webWindow instanceof HtmlPanel)) {
			sendActionEvent(actionName, data, binaryData);
			return;
		}

		String windowId = null;
		if (webWindow instanceof HtmlPanel) {
			windowId = System.identityHashCode(webWindow) + "";
		}
		if (webWindow instanceof Window) {
			windowId = ((WebWindowPeer) WebToolkit.targetToPeer(webWindow)).getGuid();
		}

		if (windowId == null) {
			sendActionEvent(actionName, data, binaryData);
			return;
		}

		sendActionEvent(windowId, actionName, data, binaryData);
	}

	private void sendActionEvent(String windowId, String actionName, String data, byte[] binaryData) {
		PaintDispatcher paintDispatcher = Util.getWebToolkit().getPaintDispatcher();
		if (paintDispatcher != null) {
			paintDispatcher.notifyActionEvent(windowId, actionName, data, binaryData);
		}
	}

	@Override
	public HtmlPanel createHtmlPanel() {
		if (!isCompositingWindowManager()) {
			throw new IllegalArgumentException("Not allowed to create HtmlPanel! Enable compositing window manager.");
		}
		HtmlPanel htmlPanel = new HtmlPanelImpl();
		htmlPanel.setFocusable(true);
		Util.getWebToolkit().getPaintDispatcher().registerHtmlPanel(htmlPanel);
		return htmlPanel;
	}
	
	@Override
	public HtmlPanel createHtmlPanelForWebContainerComponent(Container webContainer, JComponent componentParentOfHtmlPanel) {
		if (!isCompositingWindowManager()) {
			throw new IllegalArgumentException("Not allowed to create HtmlPanel! Enable compositing window manager.");
		}
		HtmlPanel htmlPanel = new HtmlPanelImpl(webContainer, componentParentOfHtmlPanel);
		Util.getWebToolkit().getPaintDispatcher().registerHtmlPanel(htmlPanel);
		return htmlPanel;
	}
	
	@Override
	public void registerWebContainer(Container container) {
		if (!isCompositingWindowManager()) {
			throw new IllegalArgumentException("Not allowed to create web container! Enable compositing window manager.");
		}
		Util.getWebToolkit().getPaintDispatcher().registerWebContainer(container);
	}
	
	@Override
	public boolean isCompositingWindowManager() {
		return Util.isCompositingWM();
	}
	
	@Override
	public boolean isDockingEnabled(Window window) {
		if (!isCompositingWindowManager()) {
			return false;
		}
		
		switch (Util.getDockMode()) {
			case "NONE":
				return false;
			case "ALL":
				return true;
			case "MARKED":
				return window instanceof Dockable;
			default: return false;
		}
	}
	
	@Override
	public void toggleWindowDock(Window window) {
		if (!isDockingEnabled(window)) {
			throw new IllegalArgumentException("Not allowed to change dock state! Enable compositing window manager and docking mode in config.");
		}
		
		String winId = Util.getPeerForTarget(window) == null ? null : Util.getPeerForTarget(window).getGuid();
		Util.getWebToolkit().getPaintDispatcher().notifyWindowDockAction(winId);
	}
	
	@Override
	public boolean isTouchMode() {
		return Util.isTouchMode();
	}
	
	public class WebswingUrlStateChangeEventImpl implements WebswingUrlStateChangeEvent {

		private String url;
		private WebswingUrlState state;
		private WebswingUrlState oldState;

		WebswingUrlStateChangeEventImpl(String url, WebswingUrlState state, WebswingUrlState oldState) {
			this.url = url;
			this.state = state;
			this.oldState = oldState;
		}

		@Override
		public String getFullUrl() {
			return url;
		}

		@Override
		public WebswingUrlState getState() {
			return state;
		}

		@Override
		public WebswingUrlState getOldState() {
			return oldState;
		}
	}

	public class WebswingUserInfo implements WebswingUser {
		private String userId;
		private HashMap<String, Serializable> userAttributes;

		@SuppressWarnings("unchecked")
		WebswingUserInfo(Serializable... attribs) {
			super();
			this.userId = (String) attribs[0];
			this.userAttributes = (HashMap<String, Serializable>) attribs[1];
		}

		@Override
		public String getUserId() {
			return userId;
		}

		@Override
		public Map<String, Serializable> getUserAttributes() {
			return userAttributes;
		}

		@Override
		public String toString() {
			return getUserId();
		}
	}
}
