package org.webswing.sessionpool.api.service.swingprocess.impl;

import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.Constants;
import org.webswing.server.common.model.AppletLauncherConfig;
import org.webswing.server.common.model.DesktopLauncherConfig;
import org.webswing.server.common.model.SwingConfig.DockMode;
import org.webswing.server.common.model.SwingConfig.LauncherType;
import org.webswing.server.common.service.swingprocess.ProcessStartupParams;
import org.webswing.server.common.util.CommonUtil;
import org.webswing.server.common.util.FontUtils;
import org.webswing.server.model.exception.WsInitException;
import org.webswing.sessionpool.api.service.swingprocess.SwingProcess;
import org.webswing.sessionpool.api.service.swingprocess.SwingProcessConfig;
import org.webswing.sessionpool.api.service.swingprocess.SwingProcessService;
import org.webswing.util.ClasspathUtil;
import org.webswing.util.NamedThreadFactory;

import com.google.common.base.Joiner;
import com.google.inject.Singleton;

import main.Main;

@Singleton
public class SwingProcessServiceImpl implements SwingProcessService {
	
	private static final Logger log = LoggerFactory.getLogger(SwingProcessServiceImpl.class);
	
	private static final String LAUNCHER_CONFIG = "launcherConfig";
	private static final String WEB_API_CLASS_NAME = "org.webswing.toolkit.api.WebswingApi";
	private static final String WEB_TOOLKIT_CLASS_NAME = "org.webswing.toolkit.WebToolkit";
	private static final String WEB_GRAPHICS_ENV_CLASS_NAME = "org.webswing.toolkit.ge.WebGraphicsEnvironment";
	private static final String WEB_PRINTER_JOB_CLASS_NAME = "org.webswing.toolkit.WebPrinterJobWrapper";
	private static final String SHELL_FOLDER_MANAGER = "sun.awt.shell.PublicShellFolderManager";
	private static final String JAVA9_PATCHED_JSOBJECT_MODULE_MARKER = "netscape.javascript.WebswingPatchedJSObjectJarMarker";
	private static final String JAVA_FX_PATH = System.getProperty("java.home") + "/lib/ext/jfxrt.jar";
	private static final String JAVA_FX_TOOLKIT_CLASS_NAME = "org.webswing.javafx.toolkit.WebsinwgFxToolkitFactory";
	private static final String JACCESS_JAR_PATH = System.getProperty("java.home") + "/lib/ext/jaccess.jar";
	
	private Map<String, SwingProcess> processMap = Collections.synchronizedMap(new HashMap<>());
	private Map<String, List<String>> pathInstanceMap = Collections.synchronizedMap(new HashMap<>());
	
	private ScheduledExecutorService processHandlerThread;
	
	@Override
	public void start() throws WsInitException {
		processHandlerThread = Executors.newSingleThreadScheduledExecutor(NamedThreadFactory.getInstance("Webswing Process Handler"));
	}

	@Override
	public void stop() {
		processHandlerThread.shutdown();
	}
	
	@Override
	public void kill(String instanceId, int delayMs) {
		synchronized (processMap) {
			if (!processMap.containsKey(instanceId)) {
				return;
			}
			
			processMap.get(instanceId).destroy(delayMs);
		}
	}
	
	@Override
	public void killAll(String path) {
		List<String> instanceIds = null;
		synchronized (pathInstanceMap) {
			if (pathInstanceMap.containsKey(path)) {
				instanceIds = pathInstanceMap.remove(path);
			}
		}
		if (instanceIds != null) {
			instanceIds.forEach(instanceId -> kill(instanceId, 0));
		}
	}

	@Override
	public void killAll() {
		getAll().forEach((process) -> kill(process.getInstanceId(), 0));
	}

	@Override
	public SwingProcess getByInstanceId(String instanceId) {
		synchronized (processMap) {
			return processMap.get(instanceId);
		}
	}
	
	@Override
	public List<SwingProcess> getAll() {
		synchronized (processMap) {
			return new ArrayList<>(processMap.values());
		}
	}
	
	@Override
	public SwingProcess startProcess(ProcessStartupParams startupParams) throws Exception {
		SwingProcess swing = null;
		try {
			SwingProcessConfig processConfig = new SwingProcessConfig();
			processConfig.setPath(startupParams.getPathMapping());
			processConfig.setName(startupParams.getInstanceId());
			processConfig.setApplicationName(startupParams.getAppName());
			String java = getAbsolutePath(startupParams.getSubs().replace(startupParams.getAppConfig().getJreExecutable()), false, startupParams.getFileResolver());
			processConfig.setJreExecutable(java);
			String homeDir = getAbsolutePath(startupParams.getSubs().replace(startupParams.getAppConfig().getUserDir()), true, startupParams.getFileResolver());
			processConfig.setBaseDir(homeDir);
			processConfig.setMainClass(Main.class.getName());
			processConfig.setClassPath(new File(URI.create(CommonUtil.getWarFileLocation())).getAbsolutePath());
			String javaVersion = startupParams.getSubs().replace(startupParams.getAppConfig().getJavaVersion());
			boolean useJFX = startupParams.getAppConfig().isJavaFx();
			String webToolkitClass = WEB_TOOLKIT_CLASS_NAME;
			String webFxToolkitFactory = JAVA_FX_TOOLKIT_CLASS_NAME;
			String javaFxBootClasspath = "";
			String webGraphicsEnvClass = WEB_GRAPHICS_ENV_CLASS_NAME;
			String j9modules = "";
			if (javaVersion.startsWith("1.8")) {
				webToolkitClass += "8";
				webFxToolkitFactory += "8";
				webGraphicsEnvClass += "8";
				if (useJFX) {
					File file = new File(JAVA_FX_PATH);
					if (!file.exists()) {

						//try resolve javafx path from jre executable
						File jreRelative = new File(java, "../../lib/ext/jfxrt.jar");
						File jdkRelative = new File(java, "../../jre/lib/ext/jfxrt.jar");
						if (jreRelative.exists()) {
							file = jreRelative;
						} else if (jdkRelative.exists()) {
							file = jdkRelative;
						} else {
							log.warn("JavaFx library not found in '" + file.getCanonicalPath() + "'. ");
							useJFX = false;
						}

					}
					javaFxBootClasspath += File.pathSeparator + CommonUtil.getBootClassPathForClass(JAVA_FX_TOOLKIT_CLASS_NAME) + File.pathSeparator + CommonUtil.getBootClassPathForClass(webFxToolkitFactory) + File.pathSeparator + "\"" + file.getCanonicalPath() + "\"";
				}
			} else if (javaVersion.startsWith("11") || javaVersion.startsWith("17") || javaVersion.startsWith("21")) {
				webToolkitClass += "11";
				webFxToolkitFactory += "11";
				webGraphicsEnvClass += "11";
				if (useJFX) {
					String javaFxToolkitCP = CommonUtil.getBootClassPathForClass(JAVA_FX_TOOLKIT_CLASS_NAME, false) + ";" + CommonUtil.getBootClassPathForClass(webFxToolkitFactory, false) + ";";
					String jfxCp = startupParams.getSubs().replace(CommonUtil.generateClassPathString(startupParams.getAppConfig().getJavaFxClassPathEntries()));
					URL[] urls = ClasspathUtil.populateClassPath(processConfig.getClassPath() + ";" + javaFxToolkitCP + ";" + jfxCp, homeDir);
					processConfig.setClassPath(Arrays.stream(urls).map(url -> {
						try {
							return new File(url.toURI()).getAbsolutePath();
						} catch (URISyntaxException e) {
							return url.getFile();
						}
					}).collect(Collectors.joining(File.pathSeparator)));
				}
				j9modules = " --patch-module jdk.jsobject=" + CommonUtil.getBootClassPathForClass(JAVA9_PATCHED_JSOBJECT_MODULE_MARKER);
				j9modules += " --patch-module java.desktop=" + CommonUtil.getBootClassPathForClass(SHELL_FOLDER_MANAGER);
				j9modules += " --add-reads jdk.jsobject=ALL-UNNAMED ";
				j9modules += " --add-opens java.base/java.net=ALL-UNNAMED "; // URLStreamHandler reflective access from SwingClassloader
				j9modules += " --add-opens java.desktop/java.awt=ALL-UNNAMED "; // EventQueue reflective access from SwingMain
				j9modules += " --add-opens java.desktop/sun.awt.windows=ALL-UNNAMED "; // sun.awt.windows.ThemeReader reflective access from WebToolkit
				j9modules += " --add-opens java.desktop/java.awt.event=ALL-UNNAMED "; // ava.awt.event.KeyEvent.extendedKeyCode reflective access from Util
				j9modules += " --add-opens java.desktop/javax.swing=ALL-UNNAMED "; // field javax.swing.PopupFactory.popupType reflective access from org.webswing.dispatch.CwmPaintDispatcher$1

				j9modules += " --add-exports=java.desktop/java.awt=ALL-UNNAMED ";
				j9modules += " --add-exports=java.desktop/java.awt.peer=ALL-UNNAMED ";
				j9modules += " --add-exports=java.desktop/sun.awt.image=ALL-UNNAMED ";
				j9modules += " --add-exports=java.desktop/sun.java2d=ALL-UNNAMED ";
				j9modules += " --add-exports=java.desktop/java.awt.dnd.peer=ALL-UNNAMED ";
				j9modules += " --add-exports=java.desktop/sun.awt=ALL-UNNAMED ";
				j9modules += " --add-exports=java.desktop/sun.awt.event=ALL-UNNAMED ";
				j9modules += " --add-exports=java.desktop/sun.awt.datatransfer=ALL-UNNAMED ";
				j9modules += " --add-exports=java.base/sun.security.action=ALL-UNNAMED ";

				j9modules += " --add-opens=java.base/java.util=ALL-UNNAMED ";
				//j9modules += " --add-opens=java.desktop/java.awt=ALL-UNNAMED ";
				j9modules += " --add-opens=java.desktop/sun.java2d=ALL-UNNAMED ";
				j9modules += " --add-opens=java.base/java.lang.reflect=ALL-UNNAMED ";



			} else {
				log.error("Java version " + javaVersion + " not supported in this version of Webswing.");
				throw new RuntimeException("Java version not supported. (Versions starting with 1.8 and 11 are supported.)");
			}
			String webSwingToolkitApiJarPath = CommonUtil.getBootClassPathForClass(WEB_API_CLASS_NAME);
			String webSwingCommonJarPath = CommonUtil.getBootClassPathForClass(Constants.class.getName());
			String webSwingToolkitJarPath = CommonUtil.getBootClassPathForClass(WEB_TOOLKIT_CLASS_NAME);
			String webSwingToolkitJarPathSpecific = CommonUtil.getBootClassPathForClass(webToolkitClass);
			String shellFolderMgrJarPath = (File.pathSeparator + CommonUtil.getBootClassPathForClass(SHELL_FOLDER_MANAGER));

			String bootCp = "-Xbootclasspath/a:" + webSwingToolkitApiJarPath 
					+ File.pathSeparatorChar + webSwingCommonJarPath 
					+ File.pathSeparatorChar + webSwingToolkitJarPathSpecific 
					+ File.pathSeparatorChar + webSwingToolkitJarPath + shellFolderMgrJarPath;

			if (useJFX) {
				bootCp += javaFxBootClasspath;
			}

			if (javaVersion.startsWith("1.8")) {
				if (!new File(JACCESS_JAR_PATH).exists()) {
					log.warn("Java access.jar not found in '" + new File(JACCESS_JAR_PATH).getCanonicalPath() + "'. ");
				} else {
					bootCp += File.pathSeparatorChar + "\"" + new File(JACCESS_JAR_PATH).getCanonicalPath() + "\"";
				}
			}
			
			String debug = startupParams.getAppConfig().isDebug() && (startupParams.getDebugPort() != 0) ? " -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=" + startupParams.getDebugPort() + ",server=y,suspend=y " : "";
			String vmArgs = startupParams.getAppConfig().getVmArgs() == null ? "" : startupParams.getSubs().replace(startupParams.getAppConfig().getVmArgs());
			processConfig.setJvmArgs(j9modules + bootCp + debug + " -Djavax.sound.sampled.Clip=org.webswing.audio.AudioMixerProvider " + " -noverify " + vmArgs);
			processConfig.addProperty(Constants.SWING_START_SYS_PROP_INSTANCE_ID, startupParams.getInstanceId());
			processConfig.addProperty(Constants.SWING_START_SYS_PROP_SESSION_POOL_ID, System.getProperty(Constants.SESSION_POOL_ID));
			processConfig.addProperty(Constants.SWING_START_SYS_PROP_USER_ID, startupParams.getUserId());
			processConfig.addProperty(Constants.SWING_START_SYS_PROP_APP_ID, startupParams.getPathMapping());
			processConfig.addProperty(Constants.SWING_START_SYS_PROP_JMS_ID, startupParams.getInstanceId());
			processConfig.addProperty(Constants.SWING_START_SYS_PROP_APP_HOME, getAbsolutePath(".", false, startupParams.getFileResolver()));
			processConfig.addProperty(Constants.SWING_START_SYS_PROP_CLASS_PATH, startupParams.getSubs().replace(CommonUtil.generateClassPathString(startupParams.getAppConfig().getClassPathEntries())));
			processConfig.addProperty(Constants.ROOT_DIR_PATH, System.getProperty(Constants.ROOT_DIR_PATH));
			processConfig.addProperty(Constants.TEMP_DIR_PATH, System.getProperty(Constants.TEMP_DIR_PATH));
			processConfig.addProperty(Constants.SWING_START_SYS_PROP_WEBSOCKET_URL, startupParams.getWebsocketUrl());
			processConfig.addProperty(Constants.WEBSWING_CONNECTION_SECRET, serializeAppConnectionSecret(startupParams.getAppConnectionSecret()));			
			processConfig.addProperty(Constants.SWING_START_SYS_PROP_DATA_STORE_CONFIG, startupParams.getDataStoreConfig());

			processConfig.addProperty(Constants.SWING_START_SYS_PROP_THEME, startupParams.getSubs().replace(startupParams.getAppConfig().getTheme()));
			processConfig.addProperty(Constants.SWING_START_SYS_PROP_ISOLATED_FS, startupParams.getAppConfig().isIsolatedFs());
			processConfig.addProperty(Constants.SWING_START_SYS_PROP_TRANSFER_DIR, getAbsolutePaths(startupParams.getSubs().replace(startupParams.getAppConfig().getTransferDir()), false, startupParams.getFileResolver()));
			processConfig.addProperty(Constants.SWING_START_SYS_PROP_ALLOW_DOWNLOAD, startupParams.getAppConfig().isAllowDownload());
			processConfig.addProperty(Constants.SWING_START_SYS_PROP_ALLOW_AUTO_DOWNLOAD, startupParams.getAppConfig().isAllowAutoDownload());
			processConfig.addProperty(Constants.SWING_START_SYS_PROP_ALLOW_UPLOAD, startupParams.getAppConfig().isAllowUpload());
			processConfig.addProperty(Constants.SWING_START_SYS_PROP_TRANSPARENT_FILE_OPEN, startupParams.getAppConfig().isTransparentFileOpen());
			processConfig.addProperty(Constants.SWING_START_SYS_PROP_TRANSPARENT_FILE_SAVE, startupParams.getAppConfig().isTransparentFileSave());
			processConfig.addProperty(Constants.SWING_START_SYS_PROP_ALLOW_DELETE, startupParams.getAppConfig().isAllowDelete());
			processConfig.addProperty(Constants.SWING_START_SYS_PROP_ALLOW_LOCAL_CLIPBOARD, startupParams.getAppConfig().isAllowLocalClipboard());
			processConfig.addProperty(Constants.SWING_START_SYS_PROP_ALLOW_JSLINK, startupParams.getAppConfig().isAllowJsLink());
			processConfig.addProperty(Constants.SWING_START_SYS_PROP_JSLINK_WHITELIST, Joiner.on(',').join(startupParams.getAppConfig().getJsLinkWhitelist()));
			processConfig.addProperty(Constants.SWING_START_SYS_PROP_INITIAL_URL, startupParams.getHandshakeUrl());
			processConfig.addProperty(Constants.SWING_START_SYS_PROP_DOCK_MODE, startupParams.isDockingSupported() ? startupParams.getAppConfig().getDockMode().name() : DockMode.NONE.name());
			processConfig.addProperty(Constants.SWING_START_SYS_PROP_TOUCH_MODE, startupParams.isTouchModeEnabled());
			processConfig.addProperty(Constants.SWING_START_SYS_PROP_ACCESSIBILITY_ENABLED, startupParams.isAccessiblityEnabled());
			processConfig.addProperty(Constants.SWING_START_SYS_PROP_STATISTICS_LOGGING_ENABLED, startupParams.getAppConfig().isAllowStatisticsLogging());
			processConfig.addProperty(Constants.SWING_START_SYS_PROP_RECORDING_FLAGGED, startupParams.isRecordingFlagged());
			processConfig.addProperty(Constants.SWING_START_SYS_PROP_RECORDING_ASK_NEEDED, startupParams.isAskForRecordingNeeded());
			processConfig.addProperty(Constants.SWING_START_SYS_PROP_MIRRORING_ASK_NEEDED, startupParams.isAskForMirroringNeeded());
			processConfig.addProperty(Constants.SWING_START_SYS_PROP_IS_APPLET, LauncherType.Applet.equals(startupParams.getAppConfig().getLauncherType()));
			processConfig.addProperty(Constants.SWING_START_SYS_PROP_SESSION_LOGGING_ENABLED, startupParams.getAppConfig().isSessionLogging());

			processConfig.addProperty(Constants.SWING_START_SYS_PROP_DIRECTDRAW, startupParams.getAppConfig().isDirectdraw());
			processConfig.addProperty(Constants.SWING_START_SYS_PROP_DIRECTDRAW_SUPPORTED, startupParams.isDirectDrawSupported());
			processConfig.addProperty(Constants.SWING_START_SYS_PROP_COMPOSITING_WM, startupParams.getAppConfig().isCompositingWinManager());
			processConfig.addProperty(Constants.SWING_START_SYS_PROP_TEST_MODE, startupParams.getAppConfig().isTestMode());
			processConfig.addProperty(Constants.SWING_SESSION_TIMEOUT_SEC, startupParams.getAppConfig().getSwingSessionTimeout());
			processConfig.addProperty(Constants.SWING_SESSION_TIMEOUT_IF_INACTIVE, startupParams.getAppConfig().isTimeoutIfInactive());
			processConfig.addProperty("awt.toolkit", webToolkitClass);
			processConfig.addProperty("java.awt.headless", false);
			processConfig.addProperty("java.awt.graphicsenv", webGraphicsEnvClass);
			processConfig.addProperty("java.awt.printerjob", WEB_PRINTER_JOB_CLASS_NAME);
			processConfig.addProperty(Constants.PRINTER_JOB_CLASS, startupParams.getAppConfig().isAllowServerPrinting() ? PrinterJob.getPrinterJob().getClass().getCanonicalName() : "org.webswing.toolkit.WebPrinterJob");
			processConfig.addProperty(Constants.SWING_START_SYS_PROP_FONT_CONFIG, FontUtils.createFontConfiguration(startupParams.getAppConfig(), startupParams.getSubs()));
			processConfig.addProperty(Constants.SWING_SCREEN_WIDTH, ((startupParams.getScreenWidth() == null) ? Constants.SWING_SCREEN_WIDTH_MIN : startupParams.getScreenWidth()));
			processConfig.addProperty(Constants.SWING_SCREEN_HEIGHT, ((startupParams.getScreenHeight() == null) ? Constants.SWING_SCREEN_HEIGHT_MIN : startupParams.getScreenHeight()));
			processConfig.addProperty(Constants.WEBSOCKET_MESSAGE_SIZE, System.getProperty(Constants.WEBSOCKET_MESSAGE_SIZE, "" + Constants.WEBSOCKET_MESSAGE_SIZE_DEFAULT_VALUE));
			processConfig.addProperty(Constants.WEBSOCKET_MESSAGE_TIMEOUT, System.getProperty(Constants.WEBSOCKET_MESSAGE_TIMEOUT, "" + Constants.WEBSOCKET_MESSAGE_TIMEOUT_DEFAULT));

			copyProperties(processConfig,Constants.WEBSOCKET_CLIENT_TRUSTSTORE,Constants.WEBSOCKET_CLIENT_TRUSTSTORE_TYPE,Constants.WEBSOCKET_CLIENT_TRUSTSTORE_PWD,Constants.WEBSOCKET_CLIENT_HOSTNAME_VERIFIER_DISABLED,Constants.WEBSOCKET_CLIENT_PROXY_URI);

			if (useJFX) {
				processConfig.addProperty(Constants.SWING_FX_TOOLKIT_FACTORY, webFxToolkitFactory);
				processConfig.addProperty(Constants.SWING_START_SYS_PROP_JFX_TOOLKIT, Constants.SWING_START_SYS_PROP_JFX_TOOLKIT_WEB);
				processConfig.addProperty(Constants.SWING_START_SYS_PROP_JFX_PRISM, "web");//PrismSettings
				processConfig.addProperty("prism.text", "t2k");//PrismFontFactory
				processConfig.addProperty("prism.lcdtext", "false");//PrismFontFactory
				processConfig.addProperty("javafx.live.resize", "false");//QuantumToolkit
			}

			if (startupParams.getAppConfig().isSessionLogging()) {
				String singleSize = startupParams.getSubs().replace(startupParams.getAppConfig().getSessionLogFileSize());
				String maxSize = startupParams.getSubs().replace(startupParams.getAppConfig().getSessionLogMaxFileSize());
				processConfig.setSessionLogAppenderParams(new SessionLogAppenderParams(singleSize, maxSize, startupParams.getInstanceId(), startupParams.getPathMapping(), getSessionLogDir()));
			}

			switch (startupParams.getAppConfig().getLauncherType()) {
			case Applet:
				AppletLauncherConfig applet = startupParams.getAppConfig().getValueAs(LAUNCHER_CONFIG, AppletLauncherConfig.class);
				processConfig.addProperty(Constants.SWING_START_SYS_PROP_APPLET_DOCUMENT_BASE, startupParams.getDocumentBase());
				processConfig.addProperty(Constants.SWING_START_SYS_PROP_APPLET_CLASS, applet.getAppletClass());
				for (String key : applet.getParameters().keySet()) {
					processConfig.addProperty(Constants.SWING_START_STS_PROP_APPLET_PARAM_PREFIX + startupParams.getSubs().replace(key), startupParams.getSubs().replace(applet.getParameters().get(key)));
				}
				if (startupParams.getParams() != null) {
					for (Entry<String, String> p : startupParams.getParams().entrySet()) {
						processConfig.addProperty(Constants.SWING_START_STS_PROP_APPLET_PARAM_PREFIX + p.getKey(), p.getValue());
					}
				}
				break;
			case Desktop:
				DesktopLauncherConfig desktop = startupParams.getAppConfig().getValueAs(LAUNCHER_CONFIG, DesktopLauncherConfig.class);
				processConfig.setArgs(startupParams.getSubs().replace(desktop.getArgs()));
				processConfig.addProperty(Constants.SWING_START_SYS_PROP_MAIN_CLASS, startupParams.getSubs().replace(desktop.getMainClass()));
				break;
			default:
				throw new IllegalStateException("Launcher type not recognized.");
			}

			swing = new SwingProcessImpl(startupParams.getInstanceId(), processConfig, startupParams.getAppConfig(), processHandlerThread);
			swing.execute();
			
			synchronized (processMap) {
				processMap.put(startupParams.getInstanceId(), swing);
			}
			synchronized (pathInstanceMap) {
				String path = startupParams.getPathMapping();
				if (!pathInstanceMap.containsKey(path)) {
					pathInstanceMap.put(path, new ArrayList<>());
				}
				pathInstanceMap.get(path).add(startupParams.getInstanceId());
			}
		} catch (Exception e) {
			throw new Exception(e);
		}
		return swing;
	}

	private void copyProperties(SwingProcessConfig processConfig, String... propNames) {
		for(String prop:propNames){
			if(System.getProperty(prop)!=null){
				processConfig.addProperty(prop,System.getProperty(prop));
			}
		}
	}

	@Override
	public void closeProcess(String instanceId) {
		SwingProcess process = getByInstanceId(instanceId);
		
		if (process == null) {
			return;
		}
		
		if (process.isRunning()) {
			process.setProcessExitListener(null);
		}
		
		if (process.getSwingConfig().isIsolatedFs() && process.getSwingConfig().isClearTransferDir()) {
			String transferDir = process.getConfig().getProperties().get(Constants.SWING_START_SYS_PROP_TRANSFER_DIR);
			try {
				if (transferDir.indexOf(File.pathSeparator) != -1) {
					throw new IOException("Can not clear upload folder if multiple roots are defined. Turn off the option in Webswing config. [" + transferDir + "]");
				} else if (transferDir != null) {
					FileUtils.deleteDirectory(new File(transferDir));
					log.info("Transfer dir for session [" + process.getConfig().getName() + "] cleared. [" + transferDir + "]");
				}
			} catch (IOException e) {
				log.error("Failed to delete transfer dir " + transferDir, e);
			}
		}
		
		synchronized (processMap) {
			if (processMap.containsKey(instanceId)) {
				processMap.remove(instanceId);
			}
		}
		synchronized (pathInstanceMap) {
			if (pathInstanceMap.containsKey(process.getConfig().getPath())) {
				pathInstanceMap.get(process.getConfig().getPath()).remove(instanceId);
			}
		}
	}
	
	public static class SessionLogAppenderParams {
		public String singleSize;
		public String maxSize;
		public String instanceId;
		public String pathMapping;
		public String sessionLogDir;

		public SessionLogAppenderParams(String singleSize, String maxSize, String instanceId, String pathMapping, String sessionLogDir) {
			super();
			this.singleSize = singleSize;
			this.maxSize = maxSize;
			this.instanceId = instanceId;
			this.pathMapping = pathMapping;
			this.sessionLogDir = sessionLogDir;
		}
	}
	
	private String serializeAppConnectionSecret(String secret) {
		return new String(Base64.getEncoder().encode(secret.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
	}
	
	private String getSessionLogDir() {
		String logDir = System.getProperty(Constants.LOGS_DIR_PATH, "logs/");
		if (!logDir.endsWith("/") && !logDir.endsWith("\\")) {
			logDir = logDir + "/";
		}
		
		return logDir + "session/";
	}
	
	private String getAbsolutePaths(String paths, boolean b, Function<String, File> fileResolver) throws IOException {
		String result = "";
		for (String s : paths.split(File.pathSeparator)) {
			result += getAbsolutePath(s, b, fileResolver) + File.pathSeparator;
		}
		return result.substring(0, Math.max(0, result.length() - 1));
	}
	
	private String getAbsolutePath(String path, boolean create, Function<String, File> fileResolver) throws IOException {
		if (StringUtils.isBlank(path)) {
			path = ".";
		}
		File f = fileResolver.apply(path);
		if (f == null || !f.exists()) {
			path = path.replaceAll("\\\\", "/");
			String[] pathSegs = path.split("/");
			boolean absolute = pathSegs[0].length() == 0 || pathSegs[0].contains(":");
			if (!absolute) {
				File home = fileResolver.apply(".");
				f = new File(home, path);
			} else {
				f = new File(path);
			}
			if (create) {
				boolean done = f.mkdirs();
				if (!done) {
					throw new IOException("Unable to create path. " + f.getAbsolutePath());
				}
			}
		}
		return f.getCanonicalPath();
	}
	
}
