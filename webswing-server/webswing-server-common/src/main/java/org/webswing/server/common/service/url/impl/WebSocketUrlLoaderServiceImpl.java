package org.webswing.server.common.service.url.impl;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.Constants;
import org.webswing.server.common.service.url.WebSocketUrlLoader;
import org.webswing.server.common.service.url.WebSocketUrlLoaderEvent;
import org.webswing.server.common.service.url.WebSocketUrlLoaderListener;
import org.webswing.server.common.service.url.WebSocketUrlLoaderService;
import org.webswing.server.common.service.url.WebSocketUrlLoaderType;

public class WebSocketUrlLoaderServiceImpl implements WebSocketUrlLoaderService {
	
	private static final Logger log = LoggerFactory.getLogger(WebSocketUrlLoaderServiceImpl.class);

	private WebSocketUrlLoaderType loaderType;
	private long reloadInterval;
	private WebSocketUrlLoader loader;
	
	private List<WebSocketUrlLoaderListener> listeners = new ArrayList<>();
	
	private final Set<String> lastValue = Collections.synchronizedSet(new HashSet<>());
	
	private Timer reloadTimer = new Timer(true);
	
	public WebSocketUrlLoaderServiceImpl() {
	}
	
	@Override
	public void init() {
		reloadInterval = Long.parseLong(System.getProperty(Constants.WEBSOCKET_URL_LOADER_INTERVAL, Constants.WEBSOCKET_URL_LOADER_INTERVAL_DEFAULT + ""));
		
		String loaderProp = System.getProperty(Constants.WEBSOCKET_URL_LOADER_TYPE, WebSocketUrlLoaderType.propertyFile.name());
		try {
			loaderType = WebSocketUrlLoaderType.valueOf(loaderProp);
		} catch (IllegalArgumentException e) {
			log.warn("Unknown loader type [" + loaderProp + "]! Using default [" + WebSocketUrlLoaderType.propertyFile + "].", e);
			loaderType = WebSocketUrlLoaderType.propertyFile;
		}
		
		switch (loaderType) {
			case propertyFile_noReload:
				loader = new StaticWebSocketUrlLoader();
				break;
			case propertyFile: {
				File propFile = new File(URI.create(System.getProperty(Constants.PROPERTIES_FILE_PATH)));
				loader = new PropertyWebSocketUrlLoader(propFile);
				break;
			}
			case script: {
				String script = System.getProperty(Constants.WEBSOCKET_URL_LOADER_SCRIPT);
				loader = new ScriptWebSocketUrlLoader(script);
				break;
			}
		}
		
		if (loaderType != WebSocketUrlLoaderType.propertyFile_noReload) {
			reloadTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					loadAndValidateUrls();
				}
			}, TimeUnit.SECONDS.toMillis(reloadInterval), TimeUnit.SECONDS.toMillis(reloadInterval));
		}
		
		loadAndValidateUrls();
	}
	
	private void loadAndValidateUrls() {
		synchronized (lastValue) {
			Set<String> urls = loader.reload();
			Iterator<String> it = urls.iterator();
			
			while (it.hasNext()) {
				String url = it.next();
				
				if (StringUtils.isBlank(url)) {
					it.remove();
				}
				
				try {
					URI.create(url);
				} catch (Exception e) {
					log.warn("Could not validate webswocket URL [" + url + "]!", e);
					it.remove();
				}
			}
			
			if (!lastValue.equals(urls)) {
				log.info("Websocket URLs reloaded.");
				lastValue.clear();
				if (urls != null) {
					lastValue.addAll(urls);
				}
				for (WebSocketUrlLoaderListener listener : listeners) {
					listener.urlsChanged(new WebSocketUrlLoaderEvent(lastValue));
				}
			}
		}
	}
	
	@Override
	public void addListener(WebSocketUrlLoaderListener listener) {
		listeners.add(listener);
		
		new Thread(() -> {
			for (WebSocketUrlLoaderListener l : listeners) {
				l.urlsChanged(new WebSocketUrlLoaderEvent(lastValue));
			}
		}).start();
	}
	
	@Override
	public void removeListener(WebSocketUrlLoaderListener listener) {
		listeners.remove(listener);
	}
	
}