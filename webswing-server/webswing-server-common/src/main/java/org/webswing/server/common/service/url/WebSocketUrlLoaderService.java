package org.webswing.server.common.service.url;

public interface WebSocketUrlLoaderService {
	
	void addListener(WebSocketUrlLoaderListener listener);
	
	void removeListener(WebSocketUrlLoaderListener listener);
	
}