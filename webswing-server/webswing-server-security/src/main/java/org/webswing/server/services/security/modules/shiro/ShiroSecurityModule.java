package org.webswing.server.services.security.modules.shiro;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.Ini.Section;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.Constants;
import org.webswing.server.common.service.security.AuthenticatedWebswingUser;
import org.webswing.server.services.security.api.WebswingAuthenticationException;
import org.webswing.server.services.security.modules.AbstractUserPasswordSecurityModule;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;
import java.util.regex.Matcher;

public class ShiroSecurityModule extends AbstractUserPasswordSecurityModule<ShiroSecurityModuleConfig> {
    private final static Logger log = LoggerFactory.getLogger(ShiroSecurityModule.class);
    private final TreeSet<String> definedRoles = new TreeSet<>();
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
        iniFileName = iniFileName.replaceFirst(
                "\\$\\{" + Constants.ROOT_DIR_PATH + "}",
                System.getProperty(Constants.ROOT_DIR_PATH)
        );
        iniFileName = iniFileName.replaceFirst("\\$\\{user.home}", Matcher.quoteReplacement(homePath));

        File file = new File(iniFileName);
        Ini ini = new Ini();

        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(file);
            ini.load(fileInputStream);

            Section rolesSection = ini.getSection("roles");
            if (rolesSection != null) {
                definedRoles.addAll(rolesSection.keySet());
                log.info("INI file provided roles: {}", Arrays.deepToString(definedRoles.toArray()));
            }
            else {
                for (String a : new String[]{"IFRSBOX", "ETLBOX", "RISKBOX", "PROFITBOX", "IMPALA", "ALMBOX"}) {
                    for (String r : new String[]{
                            "OFFICER",
                            "MANAGER",
                            "ADMIN",
                            "OPERATOR",
                            "REPORTER",
                            "AUDITOR",
                            "GUEST"
                    }) {
                        definedRoles.add(a + "_" + r);
                    }
                }
                log.info(
                        "Did not find roles in INI and applied pre-defined roles instead: {}",
                        Arrays.deepToString(definedRoles.toArray())
                );
            }

            Factory<org.apache.shiro.mgt.SecurityManager> factory = new IniSecurityManagerFactory(ini);
            securityManager = factory.getInstance();
            log.info("Initiated Security Manager successfully from {}", file.getAbsolutePath());
        } catch (FileNotFoundException ex) {
            log.error("Configuration file not found: {}", file.getAbsolutePath(), ex);
        }
    }

    String deobfuscate(String encoded) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encoded);
            String decoded = new String(decodedBytes, StandardCharsets.UTF_8);

            if (decoded.length() < 8 || !decoded.matches(".*\\d{8}$")) {
                return encoded; // Doesn't match obfuscated pattern
            }

            String digits = decoded.substring(decoded.length() - 8);
            String front = decoded.substring(0, decoded.length() - 8);

            int seed = Integer.parseInt(digits);
            Mulberry32 rng = new Mulberry32(seed);

            // Rebuild original character positions
            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < front.length(); i++) {
                indices.add(i);
            }

            // Generate same shuffle as JS
            for (int i = indices.size() - 1; i > 0; i--) {
                float rand = rng.next();
                int j = (int) Math.floor(rand * (i + 1));
                Collections.swap(indices, i, j);
            }

            // Reverse the shuffle
            char[] shuffled = front.toCharArray();
            char[] unshuffled = new char[shuffled.length];
            for (int i = 0; i < indices.size(); i++) {
                unshuffled[indices.get(i)] = shuffled[i];
            }

            return new String(unshuffled) + digits;
        } catch (IllegalArgumentException ex) {
            return encoded;
        }
    }

    @Override
    public AuthenticatedWebswingUser verifyUserPassword(String user,
                                                        String password) throws WebswingAuthenticationException {
        String originalPassword = deobfuscate(password);

        // only when init was successful and a security manager was established
        if (securityManager != null) {
            // shiro.conf can define many realms, so test one by one until found a succeeding realm
            Collection<Realm> realms = ((RealmSecurityManager) securityManager).getRealms();
            UsernamePasswordToken token = new UsernamePasswordToken(user, originalPassword);
            Optional<ShiroWebswingUser> authenticatedUser
                    = realms.parallelStream()
                              .filter(realm -> realm instanceof AuthorizingRealm
                                               && realm.supports(token))
                              .map(realm -> {
                                  try {
                                      AuthorizingRealm authorizingRealm = (AuthorizingRealm) realm;
                                      AuthenticationInfo authtInfo = realm.getAuthenticationInfo(
                                              token);
                                      String userId = authtInfo.getPrincipals()
                                                               .getPrimaryPrincipal()
                                                               .toString();
                                      ArrayList<String> roles = new ArrayList<>();

                                      final PrincipalCollection principals = authtInfo.getPrincipals();
                                      for (String role : definedRoles) {
                                          if (log.isTraceEnabled()) {
                                              log.trace(
                                                      "User {} checking role {}",
                                                      user,
                                                      role
                                              );
                                          }
                                          if (authorizingRealm.hasRole(
                                                  principals,
                                                  role
                                          )) {
                                              roles.add(role);
                                          }
                                      }
                                      log.info(
                                              "User {} authorized for roles: {}",
                                              user,
                                              Arrays.deepToString(roles.toArray())
                                      );
                                      return new ShiroWebswingUser(
                                              userId,
                                              password,
                                              roles
                                      );
                                  } catch (AuthenticationException e) {
                                      if (log.isTraceEnabled()) {
                                          log.trace(
                                                  "User {} not authenticated for realm {}",
                                                  user,
                                                  realm.getName(),
                                                  e
                                          );
                                      }
                                      return null;
                                  }
                              })
                              .filter(Objects::nonNull) // Filter out the null results from failed authentications
                              .findFirst();

            // If authenticatedUser is present, return it. Otherwise, throw the exception.
            return authenticatedUser.orElseThrow(() ->
                 new WebswingAuthenticationException(
                         "Invalid username or password!"
                         ,
                         WebswingAuthenticationException.INVALID_USER_OR_PASSWORD
                 )
            );
        }
        throw new WebswingAuthenticationException(
                "No valid Authorizing Realm found!",
                WebswingAuthenticationException.CONFIG_ERROR
        );

    }

    static class Mulberry32 {
        private int state;

        public Mulberry32(int seed) {
            this.state = seed;
        }

        public float next() {
            int t = state += 0x6D2B79F5;
            t = (t ^ (t >>> 15)) * (t | 1);
            t ^= t + ((t ^ (t >>> 7)) * (t | 61));
            t = t ^ (t >>> 14);
            return ((long) t & 0xFFFFFFFFL) / (float) (1L << 32);
        }
    }

}
