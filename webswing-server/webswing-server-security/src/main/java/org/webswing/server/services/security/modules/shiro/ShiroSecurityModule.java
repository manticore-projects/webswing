package org.webswing.server.services.security.modules.shiro;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.server.common.service.security.AuthenticatedWebswingUser;
import org.webswing.server.services.security.api.WebswingAuthenticationException;
import org.webswing.server.services.security.modules.AbstractUserPasswordSecurityModule;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.regex.Matcher;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.util.Factory;
import org.webswing.Constants;

public class ShiroSecurityModule extends AbstractUserPasswordSecurityModule<ShiroSecurityModuleConfig> {

    private final static Logger log = LoggerFactory.getLogger(ShiroSecurityModule.class);

    private org.apache.shiro.mgt.SecurityManager securityManager;

    public ShiroSecurityModule(ShiroSecurityModuleConfig config) {
        super(config);
    }

    @Override
    public void init() {
        super.init();

        String homePath = new File(System.getProperty("user.home")).toURI().getPath();
        String iniFileName = getConfig().getFile();
        iniFileName = iniFileName.replaceFirst("~", Matcher.quoteReplacement(homePath));
        iniFileName = iniFileName.replaceFirst("\\$\\{" + Constants.ROOT_DIR_PATH + "\\}" , System.getProperty(Constants.ROOT_DIR_PATH));
        iniFileName = iniFileName.replaceFirst("\\$\\{user.home\\}", Matcher.quoteReplacement(homePath));

        File file = new File(iniFileName);
        Ini ini = new Ini();

        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(file);
            ini.load(fileInputStream);

            Factory<org.apache.shiro.mgt.SecurityManager> factory = new IniSecurityManagerFactory(ini);
            securityManager = factory.getInstance();
            log.info("Initiated Security Manager successfully from " + file.getAbsolutePath());
        } catch (FileNotFoundException ex) {
            log.error("Configuration file not found: " + file.getAbsolutePath(), ex);
        }
    }

    @Override
    public AuthenticatedWebswingUser verifyUserPassword(String user, String password) throws WebswingAuthenticationException {
        // only when init was successful and a security manager was established
        if (securityManager != null) {
            // shiro.conf can define many realms, so test one by one until found a succeeding realm
            Collection<Realm> realms = ((RealmSecurityManager) securityManager).getRealms();
            UsernamePasswordToken token = new UsernamePasswordToken(user, password);
            AuthenticationInfo authtInfo;
            for (Realm realm : realms) {
                if (realm instanceof AuthorizingRealm && realm.supports(token)) {
                    try {
                        authtInfo = realm.getAuthenticationInfo(token);
                        ShiroWebswingUser shiroWebswingUser = new ShiroWebswingUser((AuthorizingRealm) realm, authtInfo);

                        return shiroWebswingUser;
                    } catch (AuthenticationException e) {
                    }
                }
            }
            throw new WebswingAuthenticationException("Invalid username or password!",
                    WebswingAuthenticationException.INVALID_USER_OR_PASSWORD);
        }
        throw new WebswingAuthenticationException("No valid Authorzing Realm found!",
                WebswingAuthenticationException.CONFIG_ERROR);

    }

}
