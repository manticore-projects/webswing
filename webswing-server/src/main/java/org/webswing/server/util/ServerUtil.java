package org.webswing.server.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.http.HttpServletRequest;

import main.Main;

import org.apache.commons.codec.binary.Base64;
import org.apache.shiro.realm.text.PropertiesRealm;
import org.apache.shiro.subject.Subject;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.FrameworkConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.Constants;
import org.webswing.model.admin.c2s.JsonApplyConfiguration;
import org.webswing.model.admin.s2c.JsonAdminConsoleFrame;
import org.webswing.model.admin.s2c.JsonMessage;
import org.webswing.model.admin.s2c.JsonMessage.Type;
import org.webswing.model.admin.s2c.JsonSwingSession;
import org.webswing.model.c2s.JsonConnectionHandshake;
import org.webswing.model.c2s.JsonEventKeyboard;
import org.webswing.model.c2s.JsonEventMouse;
import org.webswing.model.c2s.JsonEventPaste;
import org.webswing.model.s2c.JsonApplication;
import org.webswing.model.server.SwingApplicationDescriptor;
import org.webswing.model.server.WebswingConfiguration;
import org.webswing.server.SwingInstance;
import org.webswing.server.handler.ApplicationSelectorServlet;
import org.webswing.server.handler.FileServlet;
import org.webswing.server.handler.LoginServlet;
import org.webswing.server.stats.SessionRecorder;

public class ServerUtil {

	private static final String DEFAULT = "default";
	private static final Logger log = LoggerFactory.getLogger(ServerUtil.class);
	private static final Map<String, String> iconMap = new HashMap<String, String>();
	private static final ObjectMapper mapper = new ObjectMapper();

	public static String encode(Serializable m) {
		try {
			if (m instanceof String) {
				return (String) m;
			}
			return mapper.writeValueAsString(m);
		} catch (IOException e) {
			log.error("Encoding object failed: " + m, e);
			return null;
		}
	}

	public static Object decode(String s) {
		try {
			return mapper.readValue(s, JsonEventMouse.class);
		} catch (IOException e) {
			try {
				return mapper.readValue(s, JsonEventKeyboard.class);
			} catch (IOException e1) {
				try {
					return mapper.readValue(s, JsonConnectionHandshake.class);
				} catch (IOException e2) {
					try {
						return mapper.readValue(s, JsonEventPaste.class);
					} catch (IOException e3) {
						try {
							return mapper.readValue(s, JsonApplyConfiguration.class);
						} catch (IOException e4) {
							return null;
						}
					}
				}
			}
		}
	}

	public static List<JsonApplication> createApplicationJsonInfo(AtmosphereResource r, Map<String, SwingApplicationDescriptor> applications, boolean includeAdminApp) {
		List<JsonApplication> apps = new ArrayList<JsonApplication>();
		if (applications.size() == 0) {
			return null;
		} else {
			for (String name : applications.keySet()) {
				SwingApplicationDescriptor descriptor = applications.get(name);
				if (!isUserAuthorizedForApplication(r, descriptor)) {
					continue;
				}
				JsonApplication app = new JsonApplication();
				app.name = name;
				if (descriptor.getIcon() == null) {
					app.base64Icon = loadImage(null);
				} else {
					if (new File(descriptor.getIcon()).exists()) {
						app.base64Icon = loadImage(descriptor.getIcon());
					} else if (new File(descriptor.getHomeDir() + File.separator + descriptor.getIcon()).exists()) {
						app.base64Icon = loadImage(descriptor.getHomeDir() + File.separator + descriptor.getIcon());
					} else {
						log.error("Icon loading failed. File " + descriptor.getIcon() + " or " + descriptor.getHomeDir() + File.separator + descriptor.getIcon() + " does not exist.");
						app.base64Icon = loadImage(null);
					}
				}
				apps.add(app);
			}
			if (includeAdminApp) {
				JsonApplication adminConsole = new JsonApplication();
				adminConsole.name = Constants.ADMIN_CONSOLE_APP_NAME;
				apps.add(adminConsole);
			}
		}
		return apps;
	}

	public static boolean isUserAuthorizedForApplication(AtmosphereResource r, SwingApplicationDescriptor app) {
		if ((app.isAuthentication() || app.isAuthorization()) && isUserAnonymous(r)) {
			return false;
		}
		if (app.isAuthorization()) {
			if (isUserinRole(r, app.getName()) || isUserinRole(r, Constants.ADMIN_ROLE)) {
				return true;
			}
			return false;
		} else {
			return true;
		}
	}

	private static String loadImage(String icon) {
		try {
			if (icon == null) {
				if (iconMap.containsKey(DEFAULT)) {
					return iconMap.get(DEFAULT);
				} else {
					BufferedImage defaultIcon = ImageIO.read(ServerUtil.class.getClassLoader().getResourceAsStream("images/java.png"));
					String b64icon = Base64.encodeBase64String(getPngImage(defaultIcon));
					iconMap.put(DEFAULT, b64icon);
					return b64icon;
				}
			} else {
				if (iconMap.containsKey(icon)) {
					return iconMap.get(icon);
				} else {
					BufferedImage defaultIcon = ImageIO.read(new File(icon));
					String b64icon = Base64.encodeBase64String(getPngImage(defaultIcon));
					iconMap.put(icon, b64icon);
					return b64icon;
				}
			}
		} catch (IOException e) {
			log.error("Failed to load image " + icon, e);
			return null;
		}
	}

	private static byte[] getPngImage(BufferedImage imageContent) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
			ImageIO.write(imageContent, "png", ios);
			byte[] result = baos.toByteArray();
			baos.close();
			return result;
		} catch (IOException e) {
			log.error("Writing image interupted:" + e.getMessage(), e);
		}
		return null;
	}

	public static String getUserPropsFileName() {
		String userFile = System.getProperty(Constants.USER_FILE_PATH);
		if (userFile == null) {
			String war = ServerUtil.getWarFileLocation();
			userFile = war.substring(0, war.lastIndexOf("/") + 1) + Constants.DEFAULT_USER_FILE_NAME;
			System.setProperty(userFile, Constants.USER_FILE_PATH);
		}
		return userFile;
	}

	public static String getWarFileLocation() {
		String warFile = System.getProperty(Constants.WAR_FILE_LOCATION);
		if (warFile == null) {
			ProtectionDomain domain = Main.class.getProtectionDomain();
			URL location = domain.getCodeSource().getLocation();
			String locationString = location.toExternalForm();
			if (locationString.endsWith("/WEB-INF/classes/")) {
				locationString = locationString.substring(0, locationString.length() - "/WEB-INF/classes/".length());
			}
			System.setProperty(Constants.WAR_FILE_LOCATION, locationString);
			return locationString;
		}
		return warFile;
	}

	public static JsonSwingSession composeSwingInstanceStatus(SwingInstance si) {
		JsonSwingSession result = new JsonSwingSession();
		result.setId(si.getClientId());
		result.setApplication(si.getApplicationName());
		result.setConnected(si.getSessionId() != null);
		if (!result.getConnected()) {
			result.setDisconnectedSince(si.getDisconnectedSince());
		}
		result.setStartedAt(si.getStartedAt());
		result.setUser(si.getUser());
		result.setEndedAt(si.getEndedAt());
		result.setState(si.getStats());
		return result;
	}

	public static String getUserName(AtmosphereResource resource) {
		Subject sub = (Subject) resource.getRequest().getAttribute(FrameworkConfig.SECURITY_SUBJECT);
		if (sub != null) {
			return sub.getPrincipal() + "";
		}
		return null;
	}

	public static boolean isUserinRole(AtmosphereResource resource, String role) {
		Subject sub = (Subject) resource.getRequest().getAttribute(FrameworkConfig.SECURITY_SUBJECT);
		if (sub != null) {
			return sub.hasRole(role);
		}
		return false;
	}

	public static boolean isUserAnonymous(AtmosphereResource resource) {
		if (LoginServlet.anonymUserName.equals(getUserName(resource))) {
			return true;
		}
		return false;
	}

	public static boolean validateConfigFile(byte[] content) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		mapper.readValue(content, WebswingConfiguration.class);
		return true;
	}

	public static boolean validateUserFile(byte[] content) throws IOException {
		PropertiesRealm r = new PropertiesRealm();
		String tmpFileName = FileServlet.registerFile(content, UUID.randomUUID().toString(), 10, TimeUnit.SECONDS, "");
		r.setResourcePath(tmpFileName);
		r.init();
		return true;
	}

	public static String composeAdminErrorReply(Exception e) {
		return createJsonMessageFrame(Type.danger, e.getMessage());
	}

	public static String composeAdminSuccessReply(String s) {
		return createJsonMessageFrame(Type.success, s);
	}

	private static String createJsonMessageFrame(Type t, String text) {
		JsonAdminConsoleFrame response = new JsonAdminConsoleFrame();
		JsonMessage message = new JsonMessage();
		message.setType(t);
		message.setText(text);
		message.setTime(new Date());
		response.setMessage(message);
		return encode(response);
	}

	public static String getPreSelectedApplication(HttpServletRequest r, boolean reset) {
		String application = (String) r.getSession().getAttribute(ApplicationSelectorServlet.SELECTED_APPLICATION);
		if (reset) {
			r.getSession().removeAttribute(ApplicationSelectorServlet.SELECTED_APPLICATION);
		}
		return application;
	}

	public static boolean isRecording(HttpServletRequest r) {
		String recording = (String) r.getAttribute(SessionRecorder.RECORDING_FLAG);
		return Boolean.parseBoolean(recording);
	}

	public static String getCustomArgs(HttpServletRequest r) {
		String args = (String) r.getAttribute(ApplicationSelectorServlet.APPLICATION_CUSTOM_ARGS);
		return args != null ? args : "";
	}
}
