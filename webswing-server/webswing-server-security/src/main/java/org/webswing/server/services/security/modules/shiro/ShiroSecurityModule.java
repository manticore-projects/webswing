package org.webswing.server.services.security.modules.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.config.Ini;
import org.apache.shiro.env.BasicIniEnvironment;
import org.apache.shiro.env.Environment;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.Constants;
import org.webswing.server.common.service.security.AuthenticatedWebswingUser;
import org.webswing.server.services.security.api.WebswingAuthenticationException;
import org.webswing.server.services.security.modules.AbstractUserPasswordSecurityModule;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;

public class ShiroSecurityModule
    extends AbstractUserPasswordSecurityModule<ShiroSecurityModuleConfig> {
  private final static Logger log = LoggerFactory.getLogger(ShiroSecurityModule.class);

  /* OLD ROLE STORAGE (Commented out)
  private final TreeSet<String> definedRoles = new TreeSet<>();
  */

  private volatile org.apache.shiro.mgt.SecurityManager securityManager;
  private ShiroConfigMonitor configMonitor;

  // Reflection helper to access protected Shiro methods for dynamic discovery
  private static Method getAuthorizationInfoMethod;

  static {
    try {
      getAuthorizationInfoMethod = AuthorizingRealm.class.getDeclaredMethod("getAuthorizationInfo",
          PrincipalCollection.class);
      getAuthorizationInfoMethod.setAccessible(true);
    } catch (NoSuchMethodException e) {
      log.error("Failed to setup dynamic role discovery: getAuthorizationInfo method not found.",
          e);
    }
  }

  public ShiroSecurityModule(ShiroSecurityModuleConfig config) {
    super(config);
  }

  @Override
  public void init() {
    super.init();
    String iniFileName = resolvePath(getConfig().getFile());

    internalInit(iniFileName);

    if (getConfig().isHotReload() && iniFileName != null) {
      // Monitor uses a pool of 4 threads for background watching and reload tasks
      this.configMonitor = new ShiroConfigMonitor(iniFileName, this::internalInit);
      this.configMonitor.startWatching();
    }
  }

  private void internalInit(String iniFileName) {
    try {
      Ini ini = new Ini();
      ini.loadFromPath(iniFileName);

      // Modern Shiro Environment setup (Non-deprecated)
      Environment env = new BasicIniEnvironment(ini);
      org.apache.shiro.mgt.SecurityManager newManager = env.getSecurityManager();

      /* OLD [roles] SCRAPING LOGIC (Commented out)
      
      Section rolesSection = ini.getSection("roles");
      synchronized (definedRoles) {
          definedRoles.clear();
          if (rolesSection != null) {
              definedRoles.addAll(rolesSection.keySet());
              log.info("INI file provided roles: {}", Arrays.deepToString(definedRoles.toArray()));
          } else {
              applyDefaultRoles();
          }
      }
      */

      // Atomic swap for thread safety
      this.securityManager = newManager;
      SecurityUtils.setSecurityManager(newManager);

      log.info("Shiro configuration (re)loaded from {}. Dynamic role discovery active.",
          iniFileName);
    } catch (Exception ex) {
      log.error("Failed to load Shiro configuration from {}. Previous state remains active.",
          iniFileName, ex);
    }
  }

  @Override
  public AuthenticatedWebswingUser verifyUserPassword(String user, String password)
      throws WebswingAuthenticationException {
    String originalPassword = deobfuscate(password);
    org.apache.shiro.mgt.SecurityManager currentMgr = this.securityManager;

    if (currentMgr != null) {
      UsernamePasswordToken token = new UsernamePasswordToken(user, originalPassword);

      AuthenticationInfo authInfo;
      try {
        authInfo = currentMgr.authenticate(token);
      } catch (UnknownAccountException e) {
        log.warn("Authentication failed for user '{}': unknown account.", user);
        throw new WebswingAuthenticationException(
            "Unknown user '" + user + "'. Please check your username.",
            WebswingAuthenticationException.INVALID_USER_OR_PASSWORD);
      } catch (IncorrectCredentialsException e) {
        log.warn("Authentication failed for user '{}': incorrect credentials.", user);
        throw new WebswingAuthenticationException("Incorrect password for user '" + user + "'.",
            WebswingAuthenticationException.INVALID_USER_OR_PASSWORD);
      } catch (LockedAccountException e) {
        log.warn("Authentication failed for user '{}': account locked.", user);
        throw new WebswingAuthenticationException(
            "Account '" + user + "' is locked. Please contact your administrator.",
            WebswingAuthenticationException.NO_ACCESS);
      } catch (DisabledAccountException e) {
        log.warn("Authentication failed for user '{}': account disabled.", user);
        throw new WebswingAuthenticationException(
            "Account '" + user + "' is disabled. Please contact your administrator.",
            WebswingAuthenticationException.NO_ACCESS);
      } catch (ExcessiveAttemptsException e) {
        log.warn("Authentication failed for user '{}': too many attempts.", user);
        throw new WebswingAuthenticationException(
            "Too many failed login attempts for '" + user + "'. Please try again later.",
            WebswingAuthenticationException.FAILED_TO_AUTHENTICATE);
      } catch (AuthenticationException e) {
        String detail = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
        log.error("Authentication failed for user '{}': {}", user, detail);
        throw new WebswingAuthenticationException(
            "Authentication failed for '" + user + "': " + detail,
            WebswingAuthenticationException.FAILED_TO_AUTHENTICATE);
      }

      PrincipalCollection principals = authInfo.getPrincipals();

      Set<String> authorizedRoles = new HashSet<>();

      // NEW DYNAMIC LOGIC: Querying realms directly for assigned roles
      if (currentMgr instanceof RealmSecurityManager manager) {
        for (Realm realm : manager.getRealms()) {
          if (realm instanceof AuthorizingRealm) {
            try {
              AuthorizationInfo info =
                  (AuthorizationInfo) getAuthorizationInfoMethod.invoke(realm, principals);
              if (info != null && info.getRoles() != null) {
                authorizedRoles.addAll(info.getRoles());
              }
            } catch (Exception e) {
              log.debug("Could not extract roles from realm: {}", realm.getName());
            }
          }
        }
      }

      /*
      OLD ROLE CHECKING LOOP (Commented out)
      
      ArrayList<String> rolesToCheck;
      synchronized (definedRoles) {
          rolesToCheck = new ArrayList<>(definedRoles);
      }
      
      for (Realm realm : ((RealmSecurityManager) currentMgr).getRealms()) {
          if (realm instanceof AuthorizingRealm && realm.supports(token)) {
              AuthorizingRealm authorizingRealm = (AuthorizingRealm) realm;
              try {
                  boolean[] results = authorizingRealm.hasRoles(principals, rolesToCheck);
                  for (int j = 0; j < results.length; j++) {
                      if (results[j]) authorizedRoles.add(rolesToCheck.get(j));
                  }
              } catch (Exception ex) {
                  log.trace("Auth check failed against {}", realm.getName());
              }
          }
      }
      */

      // Apply default roles if no roles were discovered
      if (authorizedRoles.isEmpty()) {
        authorizedRoles.addAll(getDefaultRoles());
      }

      log.info("User {} authenticated. Discovered roles: {}", user, authorizedRoles);
      return new ShiroWebswingUser(user, password, new ArrayList<>(authorizedRoles));
    }

    throw new WebswingAuthenticationException("Security Manager not initialized.",
        WebswingAuthenticationException.CONFIG_ERROR);
  }

  private List<String> getDefaultRoles() {
    List<String> defaults = new ArrayList<>();
    for (String a : new String[] {"IFRSBOX", "ETLBOX", "RISKBOX", "PROFITBOX", "IMPALA",
        "ALMBOX"}) {
      for (String r : new String[] {"OFFICER", "MANAGER", "ADMIN", "OPERATOR", "REPORTER",
          "AUDITOR", "GUEST"}) {
        defaults.add(a + "_" + r);
      }
    }
    return defaults;
  }

  private String resolvePath(String path) {
    if (path == null)
      return null;
    String homePath = new File(System.getProperty("user.home")).toURI().getPath();
    String resolved = path.replaceFirst("~", Matcher.quoteReplacement(homePath));
    resolved = resolved.replaceFirst("\\$\\{" + Constants.ROOT_DIR_PATH + "}",
        System.getProperty(Constants.ROOT_DIR_PATH, ""));
    return resolved.replaceFirst("\\$\\{user.home}", Matcher.quoteReplacement(homePath));
  }

  @Override
  public void destroy() {
    if (this.configMonitor != null) {
      this.configMonitor.shutdown();
    }
    super.destroy();
  }

  String deobfuscate(String encoded) {
    try {
      byte[] decodedBytes = Base64.getDecoder().decode(encoded);
      String decoded = new String(decodedBytes, StandardCharsets.UTF_8);
      if (decoded.length() < 8 || !decoded.matches(".*\\d{8}$"))
        return encoded;
      String digits = decoded.substring(decoded.length() - 8);
      String front = decoded.substring(0, decoded.length() - 8);
      int seed = Integer.parseInt(digits);
      Mulberry32 rng = new Mulberry32(seed);
      List<Integer> indices = new ArrayList<>();
      for (int i = 0; i < front.length(); i++)
        indices.add(i);
      for (int i = indices.size() - 1; i > 0; i--) {
        float rand = rng.next();
        int j = (int) Math.floor(rand * (i + 1));
        Collections.swap(indices, i, j);
      }
      char[] shuffled = front.toCharArray();
      char[] unshuffled = new char[shuffled.length];
      for (int i = 0; i < indices.size(); i++)
        unshuffled[indices.get(i)] = shuffled[i];
      return new String(unshuffled) + digits;
    } catch (IllegalArgumentException ex) {
      return encoded;
    }
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
