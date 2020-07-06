package org.webswing.server.services.swinginstance;

import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.FileSize;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.action.Action;
import org.apache.logging.log4j.core.appender.rolling.action.DeleteAction;
import org.apache.logging.log4j.core.appender.rolling.action.IfAccumulatedFileSize;
import org.apache.logging.log4j.core.appender.rolling.action.IfFileName;
import org.apache.logging.log4j.core.appender.rolling.action.PathCondition;
import org.apache.logging.log4j.core.appender.rolling.action.PathSortByModificationTime;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.Constants;
import org.webswing.model.MsgIn;
import org.webswing.model.MsgInternal;
import org.webswing.model.MsgOut;
import org.webswing.model.c2s.ConnectionHandshakeMsgIn;
import org.webswing.model.c2s.ParamMsg;
import org.webswing.model.c2s.SimpleEventMsgIn;
import org.webswing.model.c2s.SimpleEventMsgIn.SimpleEventType;
import org.webswing.model.c2s.TimestampsMsgIn;
import org.webswing.model.internal.ApiCallMsgInternal;
import org.webswing.model.internal.ApiEventMsgInternal;
import org.webswing.model.internal.ApiEventMsgInternal.ApiEventType;
import org.webswing.model.internal.ExitMsgInternal;
import org.webswing.model.internal.JvmStatsMsgInternal;
import org.webswing.model.internal.OpenFileResultMsgInternal;
import org.webswing.model.internal.PrinterJobResultMsgInternal;
import org.webswing.model.internal.ThreadDumpMsgInternal;
import org.webswing.model.internal.ThreadDumpRequestMsgInternal;
import org.webswing.model.s2c.AppFrameMsgOut;
import org.webswing.model.s2c.CursorChangeEventMsg;
import org.webswing.model.s2c.LinkActionMsg;
import org.webswing.model.s2c.LinkActionMsg.LinkActionType;
import org.webswing.model.s2c.SimpleEventMsgOut;
import org.webswing.server.common.model.AppletLauncherConfig;
import org.webswing.server.common.model.DesktopLauncherConfig;
import org.webswing.server.common.model.SwingConfig;
import org.webswing.server.common.model.SwingConfig.DockMode;
import org.webswing.server.common.model.SwingConfig.LauncherType;
import org.webswing.server.common.util.CommonUtil;
import org.webswing.server.common.util.VariableSubstitutor;
import org.webswing.server.model.EncodedMessage;
import org.webswing.server.model.exception.WsException;
import org.webswing.server.services.files.FileTransferHandler;
import org.webswing.server.services.jvmconnection.JvmConnection;
import org.webswing.server.services.jvmconnection.JvmConnectionService;
import org.webswing.server.services.jvmconnection.JvmListener;
import org.webswing.server.services.recorder.SessionRecorder;
import org.webswing.server.services.recorder.SessionRecorderService;
import org.webswing.server.model.SwingInstanceStatus;
import org.webswing.server.services.rest.resources.model.SwingSession;
import org.webswing.server.services.security.api.AbstractWebswingUser;
import org.webswing.server.services.security.modules.AbstractSecurityModule;
import org.webswing.server.services.stats.StatisticsLogger;
import org.webswing.server.services.stats.StatisticsReader;
import org.webswing.server.services.swingmanager.SwingInstanceManager;
import org.webswing.server.services.swingprocess.ProcessExitListener;
import org.webswing.server.services.swingprocess.SwingProcess;
import org.webswing.server.services.swingprocess.SwingProcessConfig;
import org.webswing.server.services.swingprocess.SwingProcessImpl;
import org.webswing.server.services.swingprocess.SwingProcessService;
import org.webswing.server.services.websocket.WebSocketConnection;
import org.webswing.server.services.websocket.WebSocketUserInfo;
import org.webswing.server.util.FontUtils;
import org.webswing.server.util.LogReaderUtil;
import org.webswing.server.util.ServerUtil;
import org.webswing.toolkit.api.WebswingApi;
import org.webswing.toolkit.api.WebswingMessagingApi;

import main.Main;
import org.webswing.toolkit.util.ClasspathUtil;

import com.google.common.base.Joiner;

public class SwingInstanceImpl implements Serializable, SwingInstance, JvmListener {

	private static final long serialVersionUID = -4640770499863974871L;

	private static final String LAUNCHER_CONFIG = "launcherConfig";
	private static final String WEB_TOOLKIT_CLASS_NAME = "org.webswing.toolkit.WebToolkit";
	private static final String WEB_GRAPHICS_ENV_CLASS_NAME = "org.webswing.toolkit.ge.WebGraphicsEnvironment";
	private static final String WEB_PRINTER_JOB_CLASS_NAME = "org.webswing.toolkit.WebPrinterJobWrapper";
	private static final String SHELL_FOLDER_MANAGER = "sun.awt.shell.PublicShellFolderManager";
	private static final String JAVA9_PATCHED_JSOBJECT_MODULE_MARKER = "netscape.javascript.WebswingPatchedJSObjectJarMarker";
	private static final String JAVA_FX_PATH = System.getProperty("java.home") + "/lib/ext/jfxrt.jar";
	private static final String JAVA_FX_TOOLKIT_CLASS_NAME = "org.webswing.javafx.toolkit.WebsinwgFxToolkitFactory";
	private static final long DEFAULT_LOG_SIZE = 10 * 1024 * 1024; // 10 MB
	private static final String JACCESS_JAR_PATH = System.getProperty("java.home") + "/lib/ext/jaccess.jar";

	private static final Logger log = LoggerFactory.getLogger(SwingInstance.class);
	private final String instanceId;
	private final String ownerId;

	private WebSocketConnection webConnection;
	private WebSocketConnection mirroredWebConnection;

	private final SwingInstanceManager manager;
	private final FileTransferHandler fileHandler;
	private SwingProcess process;
	private JvmConnection jvmConnection;
	private SessionRecorder sessionRecorder;

	private SwingConfig config;
	private VariableSubstitutor subs;

	private final Date startedAt = new Date();
	private WebSocketUserInfo lastConnection = null;

	//finished instances only
	private Date endedAt = null;
	private List<String> warningHistoryLog;
	private Map<Long, ThreadDumpMsgInternal> threadDumps = new ConcurrentHashMap<>();
	
	private Boolean statisticsLoggingEnabled; // unset if null -> take default value from config

	public SwingInstanceImpl(SwingInstanceManager manager, FileTransferHandler fileHandler, SwingProcessService processService, JvmConnectionService connectionService, SessionRecorderService recorderService, ConnectionHandshakeMsgIn h, SwingConfig config, WebSocketConnection websocket) throws WsException {
		this.manager = manager;
		this.fileHandler = fileHandler;
		this.webConnection = websocket;
		this.config = config;
		this.ownerId = ServerUtil.resolveOwnerIdForSessionMode(websocket, h, config);
		this.instanceId = ServerUtil.generateInstanceId(websocket, h, manager.getPathMapping());
		AbstractWebswingUser user = websocket.getUser();
		WebSocketUserInfo info = websocket.getUserInfo();
		subs = VariableSubstitutor.forSwingInstance(manager.getConfig(), user.getUserId(), user.getUserAttributes(), this.getInstanceId(), info.getUserIp(), h.getLocale(), h.getTimeZone(), info.getCustomArgs());
		this.sessionRecorder = recorderService.create(this, manager);

		try {
			this.jvmConnection = connectionService.connect(this.instanceId, this);
			process = start(processService, config, h, websocket);
			notifyUserConnected();
		} catch (Exception e) {
			notifyExiting();
			throw new WsException("Failed to create App instance.", e);
		}
		if (ServerUtil.isRecording(websocket.getRequest())) {
			sessionRecorder.startRecording();
		}
		logStatValue(StatisticsLogger.WEBSOCKET_CONNECTED, websocket.isWebsocketTransport() ? 1 : 2);
	}

	public void connectSwingInstance(WebSocketConnection r, ConnectionHandshakeMsgIn h) {
		if (h.isMirrored()) {// connect as mirror viewer
			connectMirroredWebSession(r);
		} else {// continue old session?
			if (h.getConnectionId() != null && h.getConnectionId().equals(getConnectionId())) {
				sendToSwing(r, h);
			} else {
				boolean result = connectPrimaryWebSession(r);
				if (result) {
					r.broadcastMessage(SimpleEventMsgOut.continueOldSession.buildMsgOut());
				} else {
					r.broadcastMessage(SimpleEventMsgOut.applicationAlreadyRunning.buildMsgOut());
				}
			}
		}
	}

	private boolean connectPrimaryWebSession(WebSocketConnection resource) {
		if (resource != null) {
			if (this.mirroredWebConnection != null && StringUtils.equals(resource.uuid(), this.mirroredWebConnection.uuid())) {
				disconnectMirroredWebSession(); // prevent same connection to be primary and mirrored at the same time
			}
			if (this.webConnection != null && config.isAllowStealSession()) {
				synchronized (this.webConnection) {
					this.webConnection.broadcastMessage(SimpleEventMsgOut.sessionStolenNotification.buildMsgOut());
				}
				notifyUserDisconnected();
				this.webConnection = null;
			}
			if (this.webConnection == null) {
				this.webConnection = resource;
				logStatValue(StatisticsLogger.WEBSOCKET_CONNECTED, resource.isWebsocketTransport() ? 1 : 2);
				notifyUserConnected();
				return true;
			}
		}
		return false;
	}

	private void disconnectPrimaryWebSession() {
		if (this.webConnection != null) {
			notifyUserDisconnected();
			this.lastConnection = this.webConnection.getUserInfo();
			this.lastConnection.setDisconnected();
			this.webConnection = null;
			logStatValue(StatisticsLogger.WEBSOCKET_CONNECTED, 0);
		}
	}

	public void shutdown(boolean force) {
		if (force) {
			kill(0);
		} else {
			SimpleEventMsgIn simpleEventMsgIn = new SimpleEventMsgIn();
			simpleEventMsgIn.setType(SimpleEventType.killSwing);
			sendToSwing(null, simpleEventMsgIn);
		}
	}

	private void connectMirroredWebSession(WebSocketConnection resource) {
		if (resource != null) {
			if (this.webConnection != null && StringUtils.equals(resource.uuid(), this.webConnection.uuid())) {
				disconnectPrimaryWebSession(); // prevent same connection to be primary and mirrored at the same time
			}
			if (this.mirroredWebConnection != null) {
				synchronized (this.mirroredWebConnection) {
					this.mirroredWebConnection.broadcastMessage(SimpleEventMsgOut.sessionStolenNotification.buildMsgOut());
				}
				notifyMirrorViewDisconnected();
			}
			this.mirroredWebConnection = resource;
			notifyMirrorViewConnected();
		}
	}

	private void disconnectMirroredWebSession() {
		if (this.mirroredWebConnection != null) {
			notifyMirrorViewDisconnected();
			this.mirroredWebConnection = null;
		}
	}

	public void sendToWeb(MsgOut o) {
		EncodedMessage serialized = new EncodedMessage(o);
		if (sessionRecorder != null && sessionRecorder.isRecording()) {
			sessionRecorder.saveFrame(serialized.getProtoMessage());
		}
		if (webConnection != null) {
			synchronized (webConnection) {
				webConnection.broadcastMessage(serialized);
				if (isStatisticsLoggingEnabled()) {
					int length = serialized.getLength(webConnection.isBinary());
					logStatValue(StatisticsLogger.OUTBOUND_SIZE_METRIC, length);
				}
			}
		}
		if (mirroredWebConnection != null) {
			synchronized (mirroredWebConnection) {
				mirroredWebConnection.broadcastMessage(serialized);
			}
		}
	}

	public boolean sendToSwing(WebSocketConnection r, MsgIn h) {
		if (isRunning()) {
			if (h instanceof SimpleEventMsgIn) {
				SimpleEventMsgIn m = (SimpleEventMsgIn) h;
				if (m.getType().equals(SimpleEventMsgIn.SimpleEventType.paintAck)) {
					if (((webConnection != null && r.uuid().equals(webConnection.uuid())) || (webConnection == null && mirroredWebConnection != null && r.uuid().equals(mirroredWebConnection.uuid())))) {
						jvmConnection.send(h);
					}
				} else if (m.getType().equals(SimpleEventMsgIn.SimpleEventType.unload)) {
					if (webConnection != null && r.uuid().equals(webConnection.uuid())) {
						jvmConnection.send(h);
					}
					disconnectPrimaryWebSession();
					disconnectMirroredWebSession();
				} else {
					jvmConnection.send(h);
				}
			} else if (h instanceof TimestampsMsgIn) {
				processTimestampMessage((TimestampsMsgIn) h);
				jvmConnection.send(h);
			} else {
				jvmConnection.send(h);
			}
			return true;
		} else {
			return false;
		}
	}

	private void processTimestampMessage(TimestampsMsgIn h) {
		if (!isStatisticsLoggingEnabled()) {
			return;
		}
		
		if (StringUtils.isNotEmpty(h.getSendTimestamp())) {
			long currentTime = System.currentTimeMillis();
			long sendTime = Long.parseLong(h.getSendTimestamp());
			if (StringUtils.isNotEmpty(h.getRenderingTime()) && StringUtils.isNotEmpty(h.getStartTimestamp())) {
				long renderingTime = Long.parseLong(h.getRenderingTime());
				long startTime = Long.parseLong(h.getStartTimestamp());
				
				logStatValue(StatisticsLogger.LATENCY_SERVER_RENDERING, sendTime - startTime);
				logStatValue(StatisticsLogger.LATENCY_CLIENT_RENDERING, renderingTime);
				logStatValue(StatisticsLogger.LATENCY, currentTime - startTime);
				logStatValue(StatisticsLogger.LATENCY_NETWORK_TRANSFER, currentTime - sendTime - renderingTime);
			}
		}
		if (h.getPing() > 0) {
			logStatValue(StatisticsLogger.LATENCY_PING, h.getPing());
		}
	}

	@Override
	public void onJvmMessage(Serializable o) {
		if (o instanceof MsgInternal) {
			if (o instanceof ApiCallMsgInternal) {
				ApiCallMsgInternal query = (ApiCallMsgInternal) o;
				AbstractWebswingUser currentUser = webConnection != null ? webConnection.getUser() : null;
				Serializable result;
				switch (query.getMethod()) {
				case HasRole:
					if (currentUser == null) {
						query.setResult(null);
					} else {
						result = currentUser.hasRole((String) query.getArgs()[0]);
						query.setResult(result);
					}
					jvmConnection.send(query);
					break;
				case IsPermitted:
					if (currentUser == null) {
						query.setResult(null);
					} else {
						result = currentUser.isPermitted((String) query.getArgs()[0]);
						query.setResult(result);
					}
					jvmConnection.send(query);
					break;
				default:
					break;
				}
			} else if (o instanceof PrinterJobResultMsgInternal) {
				PrinterJobResultMsgInternal pj = (PrinterJobResultMsgInternal) o;
				boolean success = fileHandler.registerFile(pj.getPdfFile(), pj.getId(), 30, TimeUnit.MINUTES, getUserId(), getInstanceId(), pj.isTempFile(), false, null);
				if (success) {
					AppFrameMsgOut f = new AppFrameMsgOut();
					LinkActionMsg linkAction = new LinkActionMsg(LinkActionType.print, pj.getId());
					f.setLinkAction(linkAction);
					sendToWeb(f);
				}
			} else if (o instanceof OpenFileResultMsgInternal) {
				OpenFileResultMsgInternal fr = (OpenFileResultMsgInternal) o;
				String extension = getFileExtension(fr.getFile());
				String id = UUID.randomUUID().toString() + extension;
				boolean success = fileHandler.registerFile(fr.getFile(), id, 30, TimeUnit.MINUTES, getUserId(), getInstanceId(), false, fr.isWaitForFile(), fr.getOverwriteDetails());
				if (success) {
					AppFrameMsgOut f = new AppFrameMsgOut();
					LinkActionType action = extension.equalsIgnoreCase(".pdf") && fr.isPreview() ? LinkActionType.print : LinkActionType.file;
					LinkActionMsg linkAction = new LinkActionMsg(action, id);
					f.setLinkAction(linkAction);
					sendToWeb(f);
				}
			} else if (o instanceof JvmStatsMsgInternal) {
				JvmStatsMsgInternal s = (JvmStatsMsgInternal) o;

				if (isStatisticsLoggingEnabled()) {
					double cpuUsage = s.getCpuUsage();
					
					logStatValue(StatisticsLogger.MEMORY_ALLOCATED_METRIC, s.getHeapSize());
					logStatValue(StatisticsLogger.MEMORY_USED_METRIC, s.getHeapSizeUsed());
					logStatValue(StatisticsLogger.CPU_UTIL_METRIC, cpuUsage);
					logStatValue(StatisticsLogger.CPU_UTIL_SESSION_METRIC, cpuUsage);
					logStatValue(StatisticsLogger.EDT_BLOCKED_SEC_METRIC, s.getEdtPingSeconds());
				}
				if (getAppConfig().isMonitorEdtEnabled()) {
					if (s.getEdtPingSeconds() > Math.max(2, getAppConfig().getLoadingAnimationDelay())) {
						sendToWeb(SimpleEventMsgOut.applicationBusy.buildMsgOut());
					}
				}
			} else if (o instanceof ExitMsgInternal) {
				close();
				ExitMsgInternal e = (ExitMsgInternal) o;
				kill(e.getWaitForExit());
			} else if (o instanceof ThreadDumpMsgInternal) {
				ThreadDumpMsgInternal e = (ThreadDumpMsgInternal) o;
				threadDumps.put(e.getTimestamp(), e);
			}
		} else if (o instanceof AppFrameMsgOut && ((AppFrameMsgOut) o).getCursorChange() != null) {
			CursorChangeEventMsg cmsg = ((AppFrameMsgOut) o).getCursorChange();
			if (cmsg.getCurFile() != null) {
				File cur = new File(cmsg.getCurFile());
				boolean success = fileHandler.registerFile(cur, cur.getName(), 1, TimeUnit.DAYS, getUserId(), getInstanceId(), false, false, null);
				cmsg.setCurFile(cur.getName());
			}
			sendToWeb((MsgOut) o);
		} else if (o instanceof MsgOut) {
			sendToWeb((MsgOut) o);
		}
	}

	private void close() {
		try {
			if (config.isAutoLogout()) {
				sendToWeb(SimpleEventMsgOut.shutDownAutoLogoutNotification.buildMsgOut());
				if (webConnection != null) {
					webConnection.logoutUser();
				} else if (lastConnection != null) {
					lastConnection.logoutUser();
				}
			}
			if (StringUtils.isNotBlank(config.getGoodbyeUrl())) {
				String url = subs.replace(config.getGoodbyeUrl());
				if (url.startsWith("/")) {
					url = AbstractSecurityModule.getContextPath(manager.getServletContext()) + url;
				}
				AppFrameMsgOut result = new AppFrameMsgOut();
				result.setLinkAction(new LinkActionMsg(LinkActionType.redirect, url));
				sendToWeb(result);
			} else {
				sendToWeb(SimpleEventMsgOut.shutDownNotification.buildMsgOut());
			}
			jvmConnection.close();
		} catch (Throwable e) {
			log.error("Unexpected error while closing instance", e);
		} finally {
			notifyExiting();
		}

		if (process != null && config.isIsolatedFs() && config.isClearTransferDir()) {
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
	}

	public void notifyExiting() {
		endedAt = new Date();
		if (isRunning()) {
			process.setProcessExitListener(null);
		}
		try {
			if (sessionRecorder != null && sessionRecorder.isRecording()) {
				sessionRecorder.stopRecording();
			}
		} catch (WsException e) {
			log.error("Stop Recording:", e);
		}
		manager.notifySwingClose(this);
	}

	@Override
	public void startRecording() throws WsException {
		sessionRecorder.startRecording();
		sendToSwing(webConnection, new SimpleEventMsgIn(SimpleEventType.repaint));
	}

	@Override
	public void stopRecording() throws WsException {
		sessionRecorder.stopRecording();
	}

	public SwingSession toSwingSession(boolean stats) {
		SwingSession session = new SwingSession();
		session.setId(getInstanceId());
		session.setApplet(LauncherType.Applet.equals(getAppConfig().getLauncherType()));
		session.setApplication(getAppConfig().getName());
		session.setApplicationUrl(manager.getFullPathMapping());
		session.setApplicationPath(manager.getPathMapping());
		session.setConnected(getConnectionId() != null);
		WebSocketUserInfo info;
		if (webConnection == null) {
			info = lastConnection;
		} else {
			info = webConnection.getUserInfo();
		}
		if(info.getDisconnectedSince() != null)
			session.setDisconnectedSince(info.getDisconnectedSince().getTime());
		session.setUser(info.getUserId());
		session.setUserIp(info.getUserIp());
		session.setUserOs(info.getUserOs());
		session.setUserBrowser(info.getUserBrowser());

		if(getStartedAt() != null)
			session.setStartedAt(getStartedAt().getTime());
		if(getEndedAt() != null)
			session.setEndedAt(getEndedAt().getTime());
		session.setStatus(SwingSession.StatusEnum.valueOf(getStatus().toString()));
		StatisticsReader statReader = manager.getStatsReader();
		if (stats) {
			session.setStats((Map<String, Object>) statReader.getInstanceStats(this.getInstanceId()));
		}
		session.setMetrics((Map<String, BigDecimal>) statReader.getInstanceMetrics(this.getInstanceId()));
		session.setWarnings(statReader.getInstanceWarnings(this.getInstanceId()));
		if (isRunning()) {
			session.setWarningHistory(statReader.getInstanceWarningHistory(this.getInstanceId()));
		} else {
			session.setWarningHistory(warningHistoryLog);
		}
		session.setRecorded(isRecording());
		session.setRecordingFile(getRecordingFile());
		session.setThreadDumps(toMap(threadDumps));
		session.setLoggingEnabled(getAppConfig().isSessionLogging());
		session.setStatisticsLoggingEnabled(isStatisticsLoggingEnabled());

		return session;
	}

	public void kill(int delayMs) {
		if (process != null) {
			process.destroy(delayMs);
		}
	}

	private SwingProcess start(SwingProcessService processService, final SwingConfig appConfig, final ConnectionHandshakeMsgIn handshake, WebSocketConnection websocket) throws Exception {
		final Integer screenWidth = handshake.getDesktopWidth();
		final Integer screenHeight = handshake.getDesktopHeight();
		SwingProcess swing = null;
		try {
			SwingProcessConfig swingConfig = new SwingProcessConfig();
			swingConfig.setName(this.getInstanceId());
			swingConfig.setApplicationName(getAppConfig().getName());
			String java = getAbsolutePath(subs.replace(appConfig.getJreExecutable()), false);
			swingConfig.setJreExecutable(java);
			String homeDir = getAbsolutePath(subs.replace(appConfig.getUserDir()), true);
			swingConfig.setBaseDir(homeDir);
			swingConfig.setMainClass(Main.class.getName());
			swingConfig.setClassPath(new File(URI.create(CommonUtil.getWarFileLocation())).getAbsolutePath());
			String javaVersion = subs.replace(appConfig.getJavaVersion());
			boolean useJFX = config.isJavaFx();
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
			} else if (javaVersion.startsWith("11")) {
				webToolkitClass += "11";
				webFxToolkitFactory += "11";
				webGraphicsEnvClass += "11";
				if (useJFX) {
					String javaFxToolkitCP = CommonUtil.getBootClassPathForClass(JAVA_FX_TOOLKIT_CLASS_NAME, false) + ";" + CommonUtil.getBootClassPathForClass(webFxToolkitFactory, false) + ";";
					String jfxCp = subs.replace(CommonUtil.generateClassPathString(config.getJavaFxClassPathEntries()));
					URL[] urls = ClasspathUtil.populateClassPath(swingConfig.getClassPath() + ";" + javaFxToolkitCP + ";" + jfxCp, homeDir);
					swingConfig.setClassPath(Arrays.stream(urls).map(url -> {
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
			} else {
				log.error("Java version " + javaVersion + " not supported in this version of Webswing.");
				throw new RuntimeException("Java version not supported. (Versions starting with 1.8 and 11 are supported.)");
			}
			String webSwingToolkitApiJarPath = CommonUtil.getBootClassPathForClass(WebswingApi.class.getName());
			String webSwingToolkitJarPath = CommonUtil.getBootClassPathForClass(WEB_TOOLKIT_CLASS_NAME);
			String webSwingToolkitJarPathSpecific = CommonUtil.getBootClassPathForClass(webToolkitClass);
			String shellFolderMgrJarPath = (File.pathSeparator + CommonUtil.getBootClassPathForClass(SHELL_FOLDER_MANAGER));

			String bootCp = "-Xbootclasspath/a:" + webSwingToolkitApiJarPath + File.pathSeparatorChar + webSwingToolkitJarPathSpecific + File.pathSeparatorChar + webSwingToolkitJarPath + shellFolderMgrJarPath;

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
			
			int debugPort = websocket.getUserInfo().getDebugPort();
			String debug = appConfig.isDebug() && (debugPort != 0) ? " -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=" + debugPort + ",server=y,suspend=y " : "";
			String vmArgs = appConfig.getVmArgs() == null ? "" : subs.replace(appConfig.getVmArgs());
			swingConfig.setJvmArgs(j9modules + bootCp + debug + " -Djavax.sound.sampled.Clip=org.webswing.audio.AudioMixerProvider " + " -noverify " + vmArgs);
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_CLIENT_ID, this.getInstanceId());
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_APP_ID, manager.getPathMapping());
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_JMS_ID, this.instanceId);
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_APP_HOME, getAbsolutePath(".", false));
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_CLASS_PATH, subs.replace(CommonUtil.generateClassPathString(appConfig.getClassPathEntries())));
			swingConfig.addProperty(Constants.TEMP_DIR_PATH, System.getProperty(Constants.TEMP_DIR_PATH));
			swingConfig.addProperty(Constants.JMS_URL, System.getProperty(Constants.JMS_URL, Constants.JMS_URL_DEFAULT));

			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_THEME, subs.replace(appConfig.getTheme()));
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_ISOLATED_FS, appConfig.isIsolatedFs());
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_TRANSFER_DIR, getAbsolutePaths(subs.replace(appConfig.getTransferDir()), false));
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_ALLOW_DOWNLOAD, appConfig.isAllowDownload());
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_ALLOW_AUTO_DOWNLOAD, appConfig.isAllowAutoDownload());
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_ALLOW_UPLOAD, appConfig.isAllowUpload());
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_TRANSPARENT_FILE_OPEN, appConfig.isTransparentFileOpen());
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_TRANSPARENT_FILE_SAVE, appConfig.isTransparentFileSave());
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_ALLOW_DELETE, appConfig.isAllowDelete());
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_ALLOW_LOCAL_CLIPBOARD, appConfig.isAllowLocalClipboard());
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_ALLOW_JSLINK, appConfig.isAllowJsLink());
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_JSLINK_WHITELIST, Joiner.on(',').join(appConfig.getJsLinkWhitelist()));
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_INITIAL_URL, handshake.getUrl());
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_MSG_API_TOPIC, WebswingMessagingApi.MSG_API_SHARED_TOPIC + manager.getFullPathMapping());
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_DOCK_MODE, handshake.isDockingSupported() ? appConfig.getDockMode().name() : DockMode.NONE.name());
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_TOUCH_MODE, handshake.isTouchMode());
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_ACCESSIBILITY_ENABLED, handshake.isAccessiblityEnabled());
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_STATISTICS_LOGGING_ENABLED, isStatisticsLoggingEnabled());

			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_DIRECTDRAW, appConfig.isDirectdraw());
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_DIRECTDRAW_SUPPORTED, handshake.isDirectDrawSupported());
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_COMPOSITING_WM, appConfig.isCompositingWinManager());
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_TEST_MODE, appConfig.isTestMode());
			swingConfig.addProperty(Constants.SWING_SESSION_TIMEOUT_SEC, appConfig.getSwingSessionTimeout());
			swingConfig.addProperty(Constants.SWING_SESSION_TIMEOUT_IF_INACTIVE, appConfig.isTimeoutIfInactive());
			swingConfig.addProperty("awt.toolkit", webToolkitClass);
			swingConfig.addProperty("java.awt.headless", false);
			swingConfig.addProperty("java.awt.graphicsenv", webGraphicsEnvClass);
			swingConfig.addProperty("java.awt.printerjob", WEB_PRINTER_JOB_CLASS_NAME);
			swingConfig.addProperty(Constants.PRINTER_JOB_CLASS, appConfig.isAllowServerPrinting() ? PrinterJob.getPrinterJob().getClass().getCanonicalName() : "org.webswing.toolkit.WebPrinterJob");
			swingConfig.addProperty(Constants.SWING_START_SYS_PROP_FONT_CONFIG, FontUtils.createFontConfiguration(appConfig, subs));
			swingConfig.addProperty(Constants.SWING_SCREEN_WIDTH, ((screenWidth == null) ? Constants.SWING_SCREEN_WIDTH_MIN : screenWidth));
			swingConfig.addProperty(Constants.SWING_SCREEN_HEIGHT, ((screenHeight == null) ? Constants.SWING_SCREEN_HEIGHT_MIN : screenHeight));

			if (useJFX) {
				swingConfig.addProperty(Constants.SWING_FX_TOOLKIT_FACTORY, webFxToolkitFactory);
				swingConfig.addProperty(Constants.SWING_START_SYS_PROP_JFX_TOOLKIT, Constants.SWING_START_SYS_PROP_JFX_TOOLKIT_WEB);
				swingConfig.addProperty(Constants.SWING_START_SYS_PROP_JFX_PRISM, "web");//PrismSettings
				swingConfig.addProperty("prism.text", "t2k");//PrismFontFactory
				swingConfig.addProperty("prism.lcdtext", "false");//PrismFontFactory
				swingConfig.addProperty("javafx.live.resize", "false");//QuantumToolkit
			}

			if (config.isSessionLogging()) {
				swingConfig.setLogAppender(createSessionLogAppender());
			}

			switch (appConfig.getLauncherType()) {
			case Applet:
				AppletLauncherConfig applet = appConfig.getValueAs(LAUNCHER_CONFIG, AppletLauncherConfig.class);
				swingConfig.addProperty(Constants.SWING_START_SYS_PROP_APPLET_DOCUMENT_BASE, handshake.getDocumentBase());
				swingConfig.addProperty(Constants.SWING_START_SYS_PROP_APPLET_CLASS, applet.getAppletClass());
				for (String key : applet.getParameters().keySet()) {
					swingConfig.addProperty(Constants.SWING_START_STS_PROP_APPLET_PARAM_PREFIX + subs.replace(key), subs.replace(applet.getParameters().get(key)));
				}
				if (handshake.getParams() != null) {
					for (ParamMsg p : handshake.getParams()) {
						swingConfig.addProperty(Constants.SWING_START_STS_PROP_APPLET_PARAM_PREFIX + p.getName(), p.getValue());
					}
				}
				break;
			case Desktop:
				DesktopLauncherConfig desktop = appConfig.getValueAs(LAUNCHER_CONFIG, DesktopLauncherConfig.class);
				swingConfig.setArgs(subs.replace(desktop.getArgs()));
				swingConfig.addProperty(Constants.SWING_START_SYS_PROP_MAIN_CLASS, subs.replace(desktop.getMainClass()));
				break;
			default:
				throw new IllegalStateException("Launcher type not recognized.");
			}

			swing = processService.create(swingConfig);
			swing.execute();
			swing.setProcessExitListener(new ProcessExitListener() {

				@Override
				public void onClose() {
					close();
				}
			});
		} catch (Exception e1) {
			close();
			throw new Exception(e1);
		}
		return swing;
	}

	private String getAbsolutePaths(String paths, boolean b) throws IOException {
		String result = "";
		for (String s : paths.split(File.pathSeparator)) {
			result += getAbsolutePath(s, b) + File.pathSeparator;
		}
		return result.substring(0, Math.max(0, result.length() - 1));
	}

	private String getAbsolutePath(String path, boolean create) throws IOException {
		if (StringUtils.isBlank(path)) {
			path = ".";
		}
		File f = manager.resolveFile(path);
		if (f == null || !f.exists()) {
			path = path.replaceAll("\\\\", "/");
			String[] pathSegs = path.split("/");
			boolean absolute = pathSegs[0].length() == 0 || pathSegs[0].contains(":");
			if (!absolute) {
				File home = manager.resolveFile(".");
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

	private Appender createSessionLogAppender() {
		String logDir = LogReaderUtil.getSessionLogDir(subs, config);

		if (StringUtils.isNotBlank(logDir)) {
			// make path relative for logger
			logDir = new File("").toURI().relativize(new File(logDir).toURI()).getPath();
		}

		String appUrlNormalized = LogReaderUtil.normalizeForFileName(manager.getApplicationInfoMsg().getUrl());
		String sessionIdNormalized = LogReaderUtil.normalizeForFileName(this.getInstanceId());
		String logFileName = logDir + "/webswing-" + sessionIdNormalized + "-" + appUrlNormalized + ".session.log";
		String globPattern = "webswing-*-" + appUrlNormalized + ".session.log*";

		BuiltConfiguration logConfig = ConfigurationBuilderFactory.newConfigurationBuilder().build();

		String singleSize = subs.replace(config.getSessionLogFileSize());
		long maxLogRollingSize = FileSize.parse(singleSize, DEFAULT_LOG_SIZE) / 2;
		SizeBasedTriggeringPolicy sizeBasedPolicy = SizeBasedTriggeringPolicy.createPolicy(maxLogRollingSize + " B");
		String maxSize = subs.replace(config.getSessionLogMaxFileSize());

		RollingFileAppender appender = RollingFileAppender.newBuilder().withName(SwingProcessImpl.class.getName()).withFileName(logFileName).withFilePattern(logFileName + ".%i").withAppend(true).withLayout(PatternLayout.newBuilder().withPattern(Constants.SESSION_LOG_PATTERN).build()).withPolicy(sizeBasedPolicy).withStrategy(
				DefaultRolloverStrategy.newBuilder().withMax("1").withConfig(logConfig)
						.withCustomActions(new Action[] { DeleteAction.createDeleteAction(logDir, false, 1, false, PathSortByModificationTime.createSorter(true), new PathCondition[] { IfFileName.createNameCondition(globPattern, null, IfAccumulatedFileSize.createFileSizeCondition(maxSize)) }, null, logConfig) }).build()).build();
		appender.start();

		return appender;
	}

	@Override
	public String getOwnerId() {
		return ownerId;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public SwingConfig getAppConfig() {
		return config;
	}

	public String getConnectionId() {
		if (webConnection != null) {
			return webConnection.uuid();
		}
		return null;
	}

	public String getMirroredSessionId() {
		if (mirroredWebConnection != null) {
			return mirroredWebConnection.uuid();
		}
		return null;
	}

	public boolean isRunning() {
		return (process != null && process.isRunning());
	}

	public Date getStartedAt() {
		return startedAt;
	}

	public Date getEndedAt() {
		return endedAt;
	}

	public boolean isRecording() {
		return sessionRecorder.isRecording();
	}

	public String getRecordingFile() {
		return sessionRecorder.getFileName();
	}

	public SwingInstanceStatus getStatus() {
		if (process == null) {
			return SwingInstanceStatus.NOT_STARTED;
		} else {
			if (isRunning()) {
				if (getEndedAt() == null) {
					return SwingInstanceStatus.RUNNING;
				} else {
					return SwingInstanceStatus.EXITING;
				}
			} else {
				if (process.isForceKilled()) {
					return SwingInstanceStatus.FORCE_KILLED;
				} else {
					return SwingInstanceStatus.FINISHED;
				}
			}
		}
	}

	@Override
	public void webSessionDisconnected(String connectionId) {
		if (getConnectionId() != null && getConnectionId().equals(connectionId)) {
			disconnectPrimaryWebSession();
		} else if (getMirroredSessionId() != null && getMirroredSessionId().equals(connectionId)) {
			disconnectMirroredWebSession();
		}
	}

	@Override
	public String getMirrorConnectionId() {
		return mirroredWebConnection != null ? mirroredWebConnection.uuid() : null;
	}

	public void logStatValue(String name, Number value) {
		if (!isStatisticsLoggingEnabled()) {
			return;
		}
		if (StringUtils.isNotEmpty(name)) {
			manager.logStatValue(this.getInstanceId(), name, value);
		}
	}

	@Override
	public void logWarningHistory() {
		StatisticsReader statReader = manager.getStatsReader();
		List<String> current = statReader.getInstanceWarnings(this.getInstanceId());
		if (current != null) {
			current.addAll(statReader.getInstanceWarningHistory(this.getInstanceId()));
		}
		warningHistoryLog = current;
	}

	private Map<String, String> toMap(Map<Long, ThreadDumpMsgInternal> dumps) {
		LinkedHashMap<String, String> result = new LinkedHashMap<>();
		for (ThreadDumpMsgInternal dump : dumps.values()) {
			result.put(Long.toString(dump.getTimestamp()), dump.getReason());
		}
		return result;
	}

	@Override
	public String getThreadDump(String id) {
		try {
			ThreadDumpMsgInternal dump = threadDumps.get(Long.parseLong(id));
			if (dump != null) {
				return FileUtils.readFileToString(new File(dump.getDump()));
			}
			return null;
		} catch (Exception e) {
			log.error("Failed to load threaddump", e);
			return null;
		}
	}

	@Override
	public void requestThreadDump() {
		if (isRunning()) {
			jvmConnection.send(new ThreadDumpRequestMsgInternal());
		}
	}

	private void notifyUserConnected() {
		sendUserApiEventMsg(ApiEventType.UserConnected, webConnection);
	}

	private void notifyUserDisconnected() {
		sendUserApiEventMsg(ApiEventType.UserDisconnected, webConnection);
	}

	private void notifyMirrorViewConnected() {
		sendUserApiEventMsg(ApiEventType.MirrorViewConnected, mirroredWebConnection);
	}

	private void notifyMirrorViewDisconnected() {
		sendUserApiEventMsg(ApiEventType.MirrorViewDisconnected, mirroredWebConnection);
	}

	private void sendUserApiEventMsg(ApiEventType type, WebSocketConnection r) {
		ApiEventMsgInternal event;
		if (r != null && r.getUser() != null) {
			AbstractWebswingUser connectedUser = r.getUser();
			event = new ApiEventMsgInternal(type, connectedUser.getUserId(), new HashMap<String, Serializable>(connectedUser.getUserAttributes()));
		} else {
			event = new ApiEventMsgInternal(type, null, null);
		}
		jvmConnection.send(event);
	}

	public String getUserId() {
		return webConnection == null ? lastConnection.getUserId() : webConnection.getUserId();
	}

	private String getFileExtension(File file) {
		String name = file.getName();
		int lastIndexOf = name.lastIndexOf(".");
		if (lastIndexOf == -1) {
			return "";
		}
		return name.substring(lastIndexOf);
	}

	@Override
	public boolean isStatisticsLoggingEnabled() {
		if (statisticsLoggingEnabled != null) {
			return statisticsLoggingEnabled;
		}
		return config.isAllowStatisticsLogging();
	}
	
	@Override
	public void toggleStatisticsLogging(boolean enabled) {
		if (statisticsLoggingEnabled == null && enabled == isStatisticsLoggingEnabled()) {
			// ignore when toggle to same as current value
			return;
		}
		
		statisticsLoggingEnabled = enabled;
		
		SimpleEventMsgIn simpleEventMsgIn = new SimpleEventMsgIn();
		simpleEventMsgIn.setType(enabled ? SimpleEventType.enableStatisticsLogging : SimpleEventType.disableStatisticsLogging);
		sendToSwing(null, simpleEventMsgIn);
	}
	
}
