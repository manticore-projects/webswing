package org.webswing.server.api.services.config.impl;

import java.lang.reflect.Constructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.Constants;
import org.webswing.server.api.base.WebswingService;
import org.webswing.server.common.extension.ExtensionClassLoader;
import org.webswing.server.common.model.SecuredPathConfig;
import org.webswing.server.common.service.config.AbstractConfigurationService;
import org.webswing.server.common.service.config.ConfigurationProvider;
import org.webswing.server.common.service.config.ConfigurationUpdateHandler;
import org.webswing.server.model.exception.WsInitException;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DefaultConfigurationServiceImpl extends AbstractConfigurationService<SecuredPathConfig> implements WebswingService {
	private static final Logger log = LoggerFactory.getLogger(DefaultConfigurationServiceImpl.class);

	private final ExtensionClassLoader extensionLoader;
	private ConfigurationProvider<SecuredPathConfig> provider;

	@Inject
	public DefaultConfigurationServiceImpl(ExtensionClassLoader extensionLoader) {
		this.extensionLoader = extensionLoader;
	}

	@Override
	public void start() throws WsInitException {
		String providerClassName = System.getProperty(Constants.CONFIG_PROVIDER, ServerConfigurationProvider.class.getName());
		try {
			Class<?> providerClass = extensionLoader.loadClass(providerClassName);
			try {
				Constructor<?> constructor = providerClass.getDeclaredConstructor(ConfigurationUpdateHandler.class);
				provider = (ConfigurationProvider<SecuredPathConfig>) constructor.newInstance(this);
			} catch (NoSuchMethodException e) {
				provider = (ConfigurationProvider<SecuredPathConfig>) providerClass.newInstance();
			}
		} catch (Exception e) {
			throw new WsInitException("Could not instantiate configuration provider " + providerClassName, e);
		}

	}

	@Override
	public void stop() {
		clearChangeListeners();
		if (provider != null) {
			try {
				provider.dispose();
				provider = null;
			} catch (Exception e) {
				log.error("Failed to dispose config provider", e);
			}
		}
	}

	@Override
	protected ConfigurationProvider<SecuredPathConfig> getProvider() {
		return provider;
	}
	
	@Override
	protected ExtensionClassLoader getExtensionClassLoader() {
		return extensionLoader;
	}
	
}
