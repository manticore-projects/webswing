package org.webswing.server.services.security.modules.shiro;

import org.webswing.server.common.model.meta.ConfigField;
import org.webswing.server.services.security.extension.api.WebswingExtendableSecurityModuleConfig;

import org.webswing.Constants;
import org.webswing.server.common.model.meta.ConfigFieldDefaultValueString;
import org.webswing.server.common.model.meta.ConfigFieldVariables;
import org.webswing.server.common.model.meta.VariableSetName;

public interface ShiroSecurityModuleConfig extends WebswingExtendableSecurityModuleConfig {
        @ConfigField(label="File", description="Path pointing to Shiro INI file.")
	@ConfigFieldDefaultValueString("${" + Constants.ROOT_DIR_PATH + "}/shiro.ini")
	@ConfigFieldVariables(VariableSetName.SwingApp)
	String getFile();
}
