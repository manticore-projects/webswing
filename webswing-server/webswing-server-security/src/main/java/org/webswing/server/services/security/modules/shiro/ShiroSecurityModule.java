package org.webswing.server.services.security.modules.shiro;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.server.common.service.security.AuthenticatedWebswingUser;
import org.webswing.server.services.security.api.WebswingAuthenticationException;
import org.webswing.server.services.security.modules.AbstractUserPasswordSecurityModule;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;
import java.util.regex.Matcher;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.Ini.Section;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.util.Factory;
import org.webswing.Constants;

public class ShiroSecurityModule extends AbstractUserPasswordSecurityModule<ShiroSecurityModuleConfig> {
    private final static Logger log = LoggerFactory.getLogger(ShiroSecurityModule.class);
    private org.apache.shiro.mgt.SecurityManager securityManager;
    
    private final TreeSet<String> definedRoles = new TreeSet<>();

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
            
            Section rolesSection = ini.getSection("roles");
            if (rolesSection!=null) {
               definedRoles.addAll(rolesSection.keySet());
               log.info( "INI file provided roles: " + Arrays.deepToString(definedRoles.toArray()));
            } else {
                for (String a: new String[] {"IFRSBOX", "ETLBOX", "RISKBOX", "PROFITBOX", "IMPALA", "ALMBOX"} ) {
                    for (String r : new String[] {"OFFICER", "MANAGER", "ADMIN", "OPERATOR", "REPORTER", "AUDITOR", "GUEST"} ) {
                        definedRoles.add( a + "_" + r);
                    }
                }
                log.info( "Did not find roles in INI and applied pre-defined roles instead: "  + Arrays.deepToString(definedRoles.toArray()) );
            }

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
                        AuthorizingRealm authorizingRealm = (AuthorizingRealm) realm;
                        
                        authtInfo = realm.getAuthenticationInfo(token);
                        String userId = authtInfo.getPrincipals().getPrimaryPrincipal().toString();
                        ArrayList<String> roles = new ArrayList<>();
                        for (String role: definedRoles) {
                            if (authorizingRealm.hasRole(authtInfo.getPrincipals(), role)) {
                                roles.add(role);
                            }
                        }
                        ShiroWebswingUser shiroWebswingUser = new ShiroWebswingUser(userId, password, roles);
                        log.info("User " + user + " authorized for roles: " + Arrays.deepToString(roles.toArray()) );

                        return shiroWebswingUser;
                    } catch (AuthenticationException e) {
                        log.error("User " + user + " not authenticated for realm " + realm.getName(), e);
                    }
                }
            }
            throw new WebswingAuthenticationException("Invalid username or password!",
                    WebswingAuthenticationException.INVALID_USER_OR_PASSWORD);
        }
        throw new WebswingAuthenticationException("No valid Authorizing Realm found!",
                WebswingAuthenticationException.CONFIG_ERROR);

    }

}
