package org.webswing;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

// Jetty 12 core
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.ssl.SslContextFactory;

// Jetty 12 EE8
import org.eclipse.jetty.ee8.webapp.WebAppContext;

import org.webswing.util.AppLogger;

public class ServerMain {

	static Server server;

	public static void main(String[] args) throws Exception {
		Configuration config = ConfigurationImpl.parse(args);
		System.out.println(config.toString());
		System.setProperty(Constants.SERVER_EMBEDED_FLAG, "true");
		System.setProperty(Constants.SERVER_PORT, config.getHttpPort());
		System.setProperty(Constants.SERVER_HOST, config.getHost());
		System.setProperty(Constants.SERVER_CONTEXT_PATH, config.getContextPath());
		boolean isHttpsOnly = config.isHttps() && !config.isHttp();
		System.setProperty(Constants.HTTPS_ONLY, System.getProperty(Constants.HTTPS_ONLY, "" + isHttpsOnly));
		System.setProperty(Constants.SERVER_WEBSOCKET_URL, buildWebsocketUrl(config));
		if (config.getServerId() != null) {
			System.setProperty(Constants.WEBSWING_SERVER_ID, config.getServerId());
		}

		if (config.getConfigFile() != null) {
			File configFile = new File(config.getConfigFile());
			if (configFile.exists()) {
				System.setProperty(Constants.CONFIG_FILE_PATH, configFile.toURI().toString());
			} else {
				AppLogger.error("Webswing configuration file " + config.getConfigFile() + " not found. Using default location.");
			}
		}
		if (config.getPropertiesFile() != null) {
			File propFile = new File(config.getPropertiesFile());
			if (propFile.exists()) {
				System.setProperty(Constants.PROPERTIES_FILE_PATH, propFile.toURI().toString());
			} else {
				AppLogger.error("Webswing properties file " + config.getPropertiesFile() + " not found. Using default location.");
			}
		}

		server = new Server();

		List<Connector> connectors = new ArrayList<Connector>();
		if (config.isHttp()) {
			HttpConfiguration http_config = new HttpConfiguration();
			http_config.setSendServerVersion(false);
			http_config.setRequestHeaderSize(Integer.getInteger(Constants.JETTY_REQUEST_HEADER_SIZE, Constants.JETTY_REQUEST_HEADER_SIZE_DEFAULT));
			http_config.setResponseHeaderSize(Integer.getInteger(Constants.JETTY_REQUEST_HEADER_SIZE, Constants.JETTY_REQUEST_HEADER_SIZE_DEFAULT));
			if (config.isHttps()) {
				http_config.setSecurePort(Integer.parseInt(config.getHttpsPort()));
			}
			ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(http_config));
			http.setPort(Integer.parseInt(config.getHttpPort()));
			http.setHost(config.getHost());
			connectors.add(http);
		}
		if (config.isHttps()) {
			if (config.getTruststore() != null && !config.getTruststore().isEmpty() && config.getKeystore() != null && config.getKeystore().isEmpty()) {
				AppLogger.error("SSL configuration is invalid. Please specify the location of truststore and keystore files.");
			} else {
				File keyStoreFile = config.resolveConfigFile(config.getKeystore());
				File trustStoreFile = config.resolveConfigFile(config.getTruststore());
				if (!trustStoreFile.exists()) {
					AppLogger.error("SSL configuration is invalid. Truststore file " + trustStoreFile.getAbsolutePath() + " does not exist.");
				} else if (!keyStoreFile.exists()) {
					AppLogger.error("SSL configuration is invalid. Keystore file " + keyStoreFile.getAbsolutePath() + " does not exist.");
				} else {
					SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
					sslContextFactory.setKeyStorePath(keyStoreFile.getAbsolutePath());
					sslContextFactory.setKeyStorePassword(config.getKeystorePassword());
					sslContextFactory.setTrustStorePath(trustStoreFile.getAbsolutePath());
					sslContextFactory.setTrustStorePassword(config.getTruststorePassword());
					sslContextFactory.setNeedClientAuth(config.isClientAuthEnabled());

					HttpConfiguration https_config = new HttpConfiguration();
					https_config.setSendServerVersion(false);
					https_config.setRequestHeaderSize(Integer.getInteger(Constants.JETTY_REQUEST_HEADER_SIZE, Constants.JETTY_REQUEST_HEADER_SIZE_DEFAULT));
					https_config.setResponseHeaderSize(Integer.getInteger(Constants.JETTY_REQUEST_HEADER_SIZE, Constants.JETTY_REQUEST_HEADER_SIZE_DEFAULT));

					SecureRequestCustomizer src = new SecureRequestCustomizer();
					https_config.addCustomizer(src);

					// Jetty 12: SSL requires explicit SslConnectionFactory wrapping the HTTP factory
					SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(sslContextFactory, "http/1.1");
					ServerConnector https = new ServerConnector(server, sslConnectionFactory, new HttpConnectionFactory(https_config));
					https.setPort(Integer.parseInt(config.getHttpsPort()));
					https.setHost(config.getHost());
					connectors.add(https);
				}
			}
		}

		server.setConnectors(connectors.toArray(new Connector[0]));

		WebAppContext webapp = new WebAppContext();
		webapp.setContextPath(System.getProperty(Constants.SERVER_CONTEXT_PATH, "/"));
		webapp.setWar(System.getProperty(Constants.WAR_FILE_LOCATION));
		webapp.setTempDirectory(new File(URI.create(System.getProperty(Constants.TEMP_DIR_PATH))));
		webapp.setPersistTempDirectory(true);
		webapp.setAttribute("org.eclipse.jetty.server.webapp.WebInfIncludeJarPattern", ".*/webswing-server-api-[^/]*\\.jar$");

		server.setHandler(webapp);

		// Hide server version in error pages (pen test requirement)
		ErrorHandler errorHandler = new ErrorHandler();
		errorHandler.setShowStacks(false);
		errorHandler.setShowMessageInTitle(false);
		server.setErrorHandler(errorHandler);

		try {
			server.start();
			server.join();
		} catch (Exception e) {
			AppLogger.error("Webswing Server initialization failed. Stopping the server.", e);
			server.stop();
		}
		server = null;
	}

	private static String buildWebsocketUrl(Configuration config) {
		boolean isHttpsOnly = config.isHttps() && !config.isHttp();
		String host = config.getHost();
		if ("0.0.0.0".equals(host)) {
			host = "127.0.0.1";
		}
		return (isHttpsOnly ? "wss://" : "ws://") + host + getPortString(config) + config.getContextPath();
	}

	private static String getPortString(Configuration config) {
		boolean isHttpsOnly = config.isHttps() && !config.isHttp();
		String port = isHttpsOnly ? config.getHttpsPort() : config.getHttpPort();
		return StringUtil.isBlank(port) ? "" : ":" + port;
	}

	public static void stopServer() {
		if (server != null) {
			try {
				server.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}