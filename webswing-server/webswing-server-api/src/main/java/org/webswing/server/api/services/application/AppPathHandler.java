package org.webswing.server.api.services.application;

import java.io.File;

import org.webswing.model.common.in.ConnectionHandshakeMsgIn;
import org.webswing.server.api.base.UrlHandler;
import org.webswing.server.api.model.ApplicationInfoMsg;
import org.webswing.server.api.services.datastore.DataStoreHandler;
import org.webswing.server.api.services.security.login.SecuredPathHandler;
import org.webswing.server.api.services.swinginstance.SwingInstanceInfo;
import org.webswing.server.api.services.websocket.BrowserWebSocketConnection;

public interface AppPathHandler extends SecuredPathHandler, UrlHandler, DataStoreHandler {
	
	ApplicationInfoMsg getApplicationInfoMsg();

	void connectView(ConnectionHandshakeMsgIn handshake, BrowserWebSocketConnection r);

	boolean isUserAuthorized();
	
	File resolveFile(String name);

	void disable();
	
	byte[] getIconAsBytes();

	void initDataStore();

	SwingInstanceInfo createSwingInstanceInfo();
	
}
