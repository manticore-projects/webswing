package org.webswing.server.common.service.url;

public interface WebSocketUrlLoaderService {
	
	void init();
	
	void addListener(WebSocketUrlLoaderListener listener);
	
	void removeListener(WebSocketUrlLoaderListener listener);
	
}