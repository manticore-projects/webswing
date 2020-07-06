package org.webswing.model.c2s;

import java.util.List;

import org.webswing.model.MsgIn;

public class ConnectionHandshakeMsgIn implements MsgIn {

	private static final long serialVersionUID = -7143564320373144470L;

	private String clientId;
	private String browserId;
	private String viewId;
	private String tabId;
	private String connectionId;
	private Integer desktopWidth;
	private Integer desktopHeight;
	private String applicationName;
	private String documentBase;
	private String locale;
	private String timeZone;
	private String url;
	private boolean mirrored;
	private boolean directDrawSupported;
	private boolean dockingSupported;
	private boolean touchMode;
	private boolean accessiblityEnabled;
	private List<ParamMsg> params;

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getBrowserId() {
		return browserId;
	}

	public void setBrowserId(String browserId) {
		this.browserId = browserId;
	}

	public String getConnectionId() {
		return connectionId;
	}

	public void setConnectionId(String connectionId) {
		this.connectionId = connectionId;
	}

	public Integer getDesktopWidth() {
		return desktopWidth;
	}

	public void setDesktopWidth(Integer desktopWidth) {
		this.desktopWidth = desktopWidth;
	}

	public Integer getDesktopHeight() {
		return desktopHeight;
	}

	public void setDesktopHeight(Integer desktopHeight) {
		this.desktopHeight = desktopHeight;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public boolean isMirrored() {
		return mirrored;
	}

	public void setMirrored(boolean mirrored) {
		this.mirrored = mirrored;
	}

	public boolean isDirectDrawSupported() {
		return directDrawSupported;
	}

	public void setDirectDrawSupported(boolean directDrawSupported) {
		this.directDrawSupported = directDrawSupported;
	}

	public String getDocumentBase() {
		return documentBase;
	}

	public void setDocumentBase(String documentBase) {
		this.documentBase = documentBase;
	}

	public List<ParamMsg> getParams() {
		return params;
	}

	public void setParams(List<ParamMsg> params) {
		this.params = params;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public String getViewId() {
		return viewId;
	}

	public void setViewId(String viewId) {
		this.viewId = viewId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isDockingSupported() {
		return dockingSupported;
	}

	public void setDockingSupported(boolean dockingSupported) {
		this.dockingSupported = dockingSupported;
	}

	public boolean isTouchMode() {
		return touchMode;
	}

	public void setTouchMode(boolean touchMode) {
		this.touchMode = touchMode;
	}

	public boolean isAccessiblityEnabled() {
		return accessiblityEnabled;
	}

	public void setAccessiblityEnabled(boolean accessiblityEnabled) {
		this.accessiblityEnabled = accessiblityEnabled;
	}

	public String getTabId() {
		return tabId;
	}

	public void setTabId(String tabId) {
		this.tabId = tabId;
	}
}
