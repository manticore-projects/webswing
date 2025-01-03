package org.webswing.toolkit;

import java.applet.Applet;
import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.CheckboxMenuItem;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Insets;
import java.awt.JobAttributes;
import java.awt.KeyboardFocusManager;
import java.awt.Label;
import java.awt.List;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.PageAttributes;
import java.awt.Panel;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.PrintJob;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.ScrollPane;
import java.awt.Scrollbar;
import java.awt.SystemTray;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.TrayIcon;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.dnd.MouseDragGestureRecognizer;
import java.awt.dnd.peer.DragSourceContextPeer;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.awt.font.TextAttribute;
import java.awt.im.InputMethodHighlight;
import java.awt.im.spi.InputMethodDescriptor;
import java.awt.image.ColorModel;
import java.awt.peer.ButtonPeer;
import java.awt.peer.CheckboxMenuItemPeer;
import java.awt.peer.CheckboxPeer;
import java.awt.peer.ChoicePeer;
import java.awt.peer.ComponentPeer;
import java.awt.peer.DesktopPeer;
import java.awt.peer.DialogPeer;
import java.awt.peer.FileDialogPeer;
import java.awt.peer.FontPeer;
import java.awt.peer.FramePeer;
import java.awt.peer.KeyboardFocusManagerPeer;
import java.awt.peer.LabelPeer;
import java.awt.peer.ListPeer;
import java.awt.peer.MenuBarPeer;
import java.awt.peer.MenuItemPeer;
import java.awt.peer.MenuPeer;
import java.awt.peer.MouseInfoPeer;
import java.awt.peer.PanelPeer;
import java.awt.peer.PopupMenuPeer;
import java.awt.peer.RobotPeer;
import java.awt.peer.ScrollPanePeer;
import java.awt.peer.ScrollbarPeer;
import java.awt.peer.SystemTrayPeer;
import java.awt.peer.TextAreaPeer;
import java.awt.peer.TextFieldPeer;
import java.awt.peer.TrayIconPeer;
import java.awt.peer.WindowPeer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.webswing.Constants;
import org.webswing.dispatch.CwmPaintDispatcher;
import org.webswing.dispatch.EventDispatcher;
import org.webswing.dispatch.PaintDispatcher;
import org.webswing.dispatch.SessionWatchdog;
import org.webswing.dispatch.WebEventDispatcher;
import org.webswing.dispatch.WebPaintDispatcher;
import org.webswing.dispatch.WebSessionWatchdog;
import org.webswing.model.Msg;
import org.webswing.model.common.in.MirroringStatusEnum;
import org.webswing.model.common.in.RecordingStatusEnum;
import org.webswing.toolkit.api.WebswingApi;
import org.webswing.toolkit.api.WebswingApiProvider;
import org.webswing.toolkit.api.lifecycle.ShutdownReason;
import org.webswing.toolkit.extra.WindowManager;
import org.webswing.toolkit.listener.WebToolkitStartupListener;
import org.webswing.toolkit.util.EvaluationProperties;
import org.webswing.toolkit.util.Util;
import org.webswing.util.AppLogger;
import org.webswing.util.NamedThreadFactory;
import org.webswing.util.SessionMirror;
import org.webswing.util.SessionRecorder;

import sun.awt.SunToolkit;
import sun.awt.image.SurfaceManager;
import sun.java2d.SurfaceData;
import sun.print.PrintJob2D;

@SuppressWarnings("restriction")
public abstract class WebToolkit extends SunToolkit implements WebswingApiProvider {
	public static final Font defaultFont = new Font("Dialog", 0, 12);

	public static final String BACKGROUND_WINDOW_ID = "BG";
	private static final int DEFAULT_SCREEN_RESOLUTION = Integer.getInteger("webswing.screenResolution", 96);
	private static Object TREELOCK = null;

	private EventDispatcher eventDispatcher;
	private PaintDispatcher paintDispatcher;
	private SessionWatchdog sessionWatchdog;
	private WebswingApiImpl api = new WebswingApiImpl();

	private WindowManager windowManager = WindowManager.getInstance();
	private ClassLoader swingClassLoader;
	private SessionRecorder sessionRecorder;
	private SessionMirror sessionMirror;
	private java.util.List<WebToolkitStartupListener> startupListeners = Collections.synchronizedList(new ArrayList<>());

	public void init() {
		try {
			if (!System.getProperty("os.name", "").startsWith("Windows") && !System.getProperty("os.name", "").startsWith("Mac")) {
				Class<?> c = ClassLoader.getSystemClassLoader().loadClass("sun.awt.X11GraphicsEnvironment");
				Method initDisplayMethod = c.getDeclaredMethod("initDisplay", Boolean.TYPE);
				initDisplayMethod.setAccessible(true);
				initDisplayMethod.invoke(null, false);
			}
		} catch (Exception e) {
			AppLogger.error("Failed to init X11 display: ", e.getMessage());
		}
		if (System.getProperty("os.name", "").startsWith("Windows")) {
			String path = System.getenv("USERPROFILE") + "\\Desktop";
			File desktopFolder = new File(path);
			if (!desktopFolder.exists() && !desktopFolder.mkdir()) {
				AppLogger.error("Failed to create Desktop folder: " + path);
			}
		}

		installFonts();
	}

	private void installFonts() {
		try {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			Properties fontsProp = new Properties();
			String fontConfig = System.getProperty("sun.awt.fontconfig");
			if (fontConfig != null) {
				fontsProp.load(new FileInputStream(new File(fontConfig)));
				for (String name : fontsProp.stringPropertyNames()) {
					if (name.startsWith("filename.")) {
						String file = fontsProp.getProperty(name);
						Font font = Font.createFont(Font.TRUETYPE_FONT, new File(file));
						ge.registerFont(font);
					}
				}
			}
		} catch (Exception e) {
			AppLogger.error("Failed to install fonts", e);
		}
	}

	public void startDispatchers() {
		eventDispatcher = Util.instantiateClass(EventDispatcher.class, Constants.SWING_START_SYS_PROP_EVENT_DISPATCHER_CLASS, WebEventDispatcher.class.getName());
		if(eventDispatcher==null){
			AppLogger.fatal("EventDispatcher not initialized. Exiting.");
			System.exit(1);
		}

		Class<? extends PaintDispatcher> defaultPaintDispatcher = Util.isCompositingWM() ? CwmPaintDispatcher.class : WebPaintDispatcher.class;
		paintDispatcher = Util.instantiateClass(PaintDispatcher.class, Constants.SWING_START_SYS_PROP_PAINT_DISPATCHER_CLASS, defaultPaintDispatcher.getName());
		if(paintDispatcher==null){
			AppLogger.fatal("PaintDispatcher not initialized. Exiting.");
			System.exit(1);
		}

		sessionWatchdog = Util.instantiateClass(SessionWatchdog.class, Constants.SWING_START_SYS_PROP_SESSION_WATCHDOG_CLASS, WebSessionWatchdog.class.getName());
		if (sessionWatchdog == null) {
			AppLogger.fatal("SessionWatchdog not initialized. Exiting.");
			System.exit(1);
		}

		AppLogger.info("Webswing Event Dispatcher: "+eventDispatcher.getClass().getName());
		AppLogger.info("Webswing Paint Dispatcher: "+paintDispatcher.getClass().getName());
		AppLogger.info("Session Watchdog: " + sessionWatchdog.getClass().getName());
		
		for (WebToolkitStartupListener l : startupListeners) {
			l.dispatchersStarted();
		}
		
		initSessionRecorder();
		sessionMirror = new SessionMirror();
		getPaintDispatcher().notifySessionDataChanged();
	}
	
	private void initSessionRecorder() {
		sessionRecorder = new SessionRecorder(System.getProperty(Constants.SWING_START_SYS_PROP_INSTANCE_ID));

		// Start recording if recording=true is in app url
		if (Boolean.getBoolean(Constants.SWING_START_SYS_PROP_RECORDING_FLAGGED)) {
			try {
				sessionRecorder.startRecording();
			} catch (Exception e) {
				AppLogger.error("Could not initialize recording!", e);
			}
		}
	}

	public void initSize(final Integer width, final Integer height) {
		final Integer desktopWidth = Math.max(Constants.SWING_SCREEN_WIDTH_MIN, width);
		final Integer desktopHeight = Math.max(Constants.SWING_SCREEN_HEIGHT_MIN, height);
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(() -> initSize(desktopWidth, desktopHeight));
		} else {
			int oldWidht = screenWidth;
			int oldHeight = screenHeight;
			screenWidth = desktopWidth;
			screenHeight = desktopHeight;
			displayChanged();
			resetGC();
			Util.resetWindowsGC(screenWidth, screenHeight);
			getPaintDispatcher().notifyScreenSizeChanged(oldWidht, oldHeight,screenWidth, screenHeight);
		}
	}
	
	public void addStartupListener(WebToolkitStartupListener listener) {
		startupListeners.add(listener);
	}
	
	public void removeStartupListener(WebToolkitStartupListener listener) {
		startupListeners.remove(listener);
	}

	public WindowManager getWindowManager() {
		return windowManager;
	}

	public EventDispatcher getEventDispatcher() {
		return eventDispatcher;
	}

	public PaintDispatcher getPaintDispatcher() {
		return paintDispatcher;
	}

	public SessionWatchdog getSessionWatchdog() {
		return sessionWatchdog;
	}

	public MirroringStatusEnum getMirroringStatus() {
		if (sessionMirror == null) {
			return null;
		}

		return sessionMirror.getMirroringStatus();
	}

	public void startMirroring() {
		try {
			if (sessionMirror != null && (
					sessionMirror.getMirroringStatus() == MirroringStatusEnum.NOT_MIRRORING ||
							sessionMirror.getMirroringStatus() == MirroringStatusEnum.DENIED_MIRRORING_BY_USER
			)) {
				sessionMirror.startMirroring();
				getPaintDispatcher().notifySessionDataChanged();
			}
		} catch (Exception e) {
			AppLogger.error("Failed to start mirroring!", e);
		}
	}

	public void stopMirroring() {
		try {
			if (sessionMirror != null && (
					sessionMirror.getMirroringStatus() == MirroringStatusEnum.MIRRORING ||
							sessionMirror.getMirroringStatus() == MirroringStatusEnum.WAITING_FOR_MIRRORING_APPROVAL
			)) {
				sessionMirror.stopMirroring();
				getPaintDispatcher().notifySessionDataChanged();
			}
		} catch (Exception e) {
			AppLogger.warn("Failed to stop mirroring!", e);
		}
	}

	public RecordingStatusEnum getRecordingStatus() {
		if (sessionRecorder == null) {
			return null;
		}

		return sessionRecorder.getRecordingStatus();
	}

	public void recordFrame(byte[] appFrameMsgOut) {
		if (sessionRecorder != null && sessionRecorder.getRecordingStatus() == RecordingStatusEnum.RECORDING) {
			sessionRecorder.saveFrame(appFrameMsgOut);
		}
	}

	public void startRecording() {
		try {
			if (sessionRecorder != null && (
					sessionRecorder.getRecordingStatus() == RecordingStatusEnum.NOT_RECORDING ||
							sessionRecorder.getRecordingStatus() == RecordingStatusEnum.DENIED_RECORDING_BY_USER
			)) {
				sessionRecorder.startRecording();
				getPaintDispatcher().notifyWindowRepaintAll();
				getPaintDispatcher().notifySessionDataChanged();
			}
		} catch (Exception e) {
			AppLogger.error("Failed to start recording!", e);
		}
	}

	public void stopRecording() {
		try {
			if (sessionRecorder != null && (
					sessionRecorder.getRecordingStatus() == RecordingStatusEnum.RECORDING ||
							sessionRecorder.getRecordingStatus() == RecordingStatusEnum.WAITING_FOR_RECORDING_APPROVAL
			)) {
				sessionRecorder.stopRecording();
				getPaintDispatcher().notifySessionDataChanged();
			}
		} catch (Exception e) {
			AppLogger.error("Failed to stop recording!", e);
		}
	}

	public String getRecordingFileName() {
		if (sessionRecorder == null) {
			return null;
		}
		return sessionRecorder.getFileName();
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////
	// /////////////////// Toolkit Implementation//////////////////////////////////////////////////
	private static WebMouseInfoPeer mPeer;

	public WebToolkit() {
		if (System.getProperty("java.version").startsWith("1.6") || System.getProperty("java.version").startsWith("1.7")) {
			try {
				Method m = SunToolkit.class.getDeclaredMethod("setDataTransfererClassName", String.class);
				m.setAccessible(true);
				m.invoke(null, "org.webswing.toolkit.WebDataTransfer");
			} catch (Exception e) {
				// do nothing
			}
		}
	}

	private static GraphicsConfiguration config;
	private Hashtable<String, FontPeer> cacheFontPeer;
	private WebClipboard clipboard;
	private Clipboard selectionClipboard;

	private boolean exiting = false;

	public static int screenWidth = Math.max(Constants.SWING_SCREEN_WIDTH_MIN,Integer.parseInt(System.getProperty(Constants.SWING_SCREEN_WIDTH, Constants.SWING_SCREEN_WIDTH_MIN + "")));
	public static int screenHeight = Math.max(Constants.SWING_SCREEN_HEIGHT_MIN,Integer.parseInt(System.getProperty(Constants.SWING_SCREEN_HEIGHT, Constants.SWING_SCREEN_HEIGHT_MIN + "")));

	public static final Object targetToPeer(Object paramObject) {
		return SunToolkit.targetToPeer(paramObject);
	}

	public static final void targetDisposedPeer(Object paramObject1, Object paramObject2) {
		SunToolkit.targetDisposedPeer(paramObject1, paramObject2);
	}

	@Override
	protected void initializeDesktopProperties() {
		if (Boolean.getBoolean(Constants.SWING_START_SYS_PROP_ISOLATED_FS)) {
			this.desktopProperties.put("Shell.shellFolderManager", "org.webswing.toolkit.extra.IsolatedFsShellFolderManager");
		} else {
			if (System.getProperty("os.name", "").startsWith("Windows")) {
				this.desktopProperties.put("Shell.shellFolderManager", "sun.awt.shell.Win32ShellFolderManager2");
			}
		}

		this.desktopProperties.put("DnD.gestureMotionThreshold", 2);
		this.desktopProperties.put("DnD.Autoscroll.initialDelay", 100);
		this.desktopProperties.put("DnD.Autoscroll.interval", 100);
		this.desktopProperties.put("DnD.Autoscroll.cursorHysteresis", 10);
		this.desktopProperties.put("awt.dynamicLayoutSupported", true);
		this.desktopProperties.put("awt.file.showAttribCol", false);
		this.desktopProperties.put("awt.file.showHiddenFiles", false);
		this.desktopProperties.put("awt.mouse.numButtons", 5);
		this.desktopProperties.put("awt.multiClickInterval", 500);
		this.desktopProperties.put("awt.wheelMousePresent", true);
		this.desktopProperties.put("win.3d.backgroundColor", new java.awt.Color(240, 240, 240));
		this.desktopProperties.put("win.3d.darkShadowColor", new java.awt.Color(105, 105, 105));
		this.desktopProperties.put("win.3d.highlightColor", new java.awt.Color(255, 255, 255));
		this.desktopProperties.put("win.3d.lightColor", new java.awt.Color(227, 227, 227));
		this.desktopProperties.put("win.3d.shadowColor", new java.awt.Color(160, 160, 160));
		this.desktopProperties.put("win.ansiFixed.font", Font.decode("Monospaced 0 13"));
		this.desktopProperties.put("win.ansiFixed.font.height", 13);
		this.desktopProperties.put("win.ansiVar.font", Font.decode("Dialog 0 11"));
		this.desktopProperties.put("win.ansiVar.font.height", 11);
		this.desktopProperties.put("win.button.textColor", new java.awt.Color(0, 0, 0));
		this.desktopProperties.put("win.caret.width", 1);
		this.desktopProperties.put("win.defaultGUI.font", Font.decode("Dialog 0 11"));
		this.desktopProperties.put("win.defaultGUI.font.height", 11);
		this.desktopProperties.put("win.desktop.backgroundColor", new java.awt.Color(0, 0, 0));
		this.desktopProperties.put("win.deviceDefault.font", Font.decode("Dialog 1 13"));
		this.desktopProperties.put("win.deviceDefault.font.height", 13);
		this.desktopProperties.put("win.drag.height", 4);
		this.desktopProperties.put("win.drag.width", 4);
		this.desktopProperties.put("win.frame.activeBorderColor", new java.awt.Color(180, 180, 180));
		this.desktopProperties.put("win.frame.activeCaptionColor", new java.awt.Color(153, 180, 209));
		this.desktopProperties.put("win.frame.activeCaptionGradientColor", new java.awt.Color(185, 209, 234));
		this.desktopProperties.put("win.frame.backgroundColor", new java.awt.Color(255, 255, 255));
		this.desktopProperties.put("win.frame.captionButtonHeight", 22);
		this.desktopProperties.put("win.frame.captionButtonWidth", 36);
		this.desktopProperties.put("win.frame.captionFont", Font.decode("Dialog 0 12"));
		this.desktopProperties.put("win.frame.captionFont.height", 12);
		this.desktopProperties.put("win.frame.captionGradientsOn", true);
		this.desktopProperties.put("win.frame.captionHeight", 22);
		this.desktopProperties.put("win.frame.captionTextColor", new java.awt.Color(0, 0, 0));
		this.desktopProperties.put("win.frame.color", new java.awt.Color(100, 100, 100));
		this.desktopProperties.put("win.frame.fullWindowDragsOn", true);
		this.desktopProperties.put("win.frame.inactiveBorderColor", new java.awt.Color(244, 247, 252));
		this.desktopProperties.put("win.frame.inactiveCaptionColor", new java.awt.Color(191, 205, 219));
		this.desktopProperties.put("win.frame.inactiveCaptionGradientColor", new java.awt.Color(215, 228, 242));
		this.desktopProperties.put("win.frame.inactiveCaptionTextColor", new java.awt.Color(0, 0, 0));
		this.desktopProperties.put("win.frame.sizingBorderWidth", 5);
		this.desktopProperties.put("win.frame.smallCaptionButtonHeight", 22);
		this.desktopProperties.put("win.frame.smallCaptionButtonWidth", 22);
		this.desktopProperties.put("win.frame.smallCaptionFont", Font.decode("Dialog 0 12"));
		this.desktopProperties.put("win.frame.smallCaptionFont.height", 12);
		this.desktopProperties.put("win.frame.smallCaptionHeight", 22);
		this.desktopProperties.put("win.frame.textColor", new java.awt.Color(0, 0, 0));
		this.desktopProperties.put("win.highContrast.on", false);
		this.desktopProperties.put("win.icon.font", Font.decode("Dialog 0 12"));
		this.desktopProperties.put("win.icon.font.height", 12);
		this.desktopProperties.put("win.icon.hspacing", 75);
		this.desktopProperties.put("win.icon.titleWrappingOn", true);
		this.desktopProperties.put("win.icon.vspacing", 75);
		this.desktopProperties.put("win.item.highlightColor", new java.awt.Color(51, 153, 255));
		this.desktopProperties.put("win.item.highlightTextColor", new java.awt.Color(255, 255, 255));
		this.desktopProperties.put("win.item.hotTrackedColor", new java.awt.Color(0, 102, 204));
		this.desktopProperties.put("win.item.hotTrackingOn", true);
		this.desktopProperties.put("win.mdi.backgroundColor", new java.awt.Color(171, 171, 171));
		this.desktopProperties.put("win.menu.backgroundColor", new java.awt.Color(240, 240, 240));
		this.desktopProperties.put("win.menu.buttonWidth", 19);
		this.desktopProperties.put("win.menu.font", Font.decode("Dialog 0 12"));
		this.desktopProperties.put("win.menu.font.height", 12);
		this.desktopProperties.put("win.menu.height", 19);
		this.desktopProperties.put("win.menu.keyboardCuesOn", false);
		this.desktopProperties.put("win.menu.textColor", new java.awt.Color(0, 0, 0));
		this.desktopProperties.put("win.menubar.backgroundColor", new java.awt.Color(240, 240, 240));
		this.desktopProperties.put("win.messagebox.font", Font.decode("Dialog 0 12"));
		this.desktopProperties.put("win.messagebox.font.height", 12);
		this.desktopProperties.put("win.oemFixed.font", Font.decode("Dialog 0 12"));
		this.desktopProperties.put("win.oemFixed.font.height", 12);
		this.desktopProperties.put("win.properties.version", 3);
		this.desktopProperties.put("win.scrollbar.backgroundColor", new java.awt.Color(200, 200, 200));
		this.desktopProperties.put("win.scrollbar.height", 17);
		this.desktopProperties.put("win.scrollbar.width", 17);
		this.desktopProperties.put("win.status.font", Font.decode("Dialog 0 12"));
		this.desktopProperties.put("win.status.font.height", 12);
		this.desktopProperties.put("win.system.font", Font.decode("Dialog 1 13"));
		this.desktopProperties.put("win.system.font.height", 13);
		this.desktopProperties.put("win.systemFixed.font", Font.decode("Dialog 0 12"));
		this.desktopProperties.put("win.systemFixed.font.height", 12);
		this.desktopProperties.put("win.text.fontSmoothingContrast", 1200);
		this.desktopProperties.put("win.text.fontSmoothingOn", true);
		this.desktopProperties.put("win.text.fontSmoothingOrientation", 1);
		this.desktopProperties.put("win.text.fontSmoothingType", 2);
		this.desktopProperties.put("win.text.grayedTextColor", new java.awt.Color(109, 109, 109));
		this.desktopProperties.put("win.tooltip.backgroundColor", new java.awt.Color(255, 255, 225));
		this.desktopProperties.put("win.tooltip.font", Font.decode("Dialog 0 12"));
		this.desktopProperties.put("win.tooltip.font.height", 12);
		this.desktopProperties.put("win.tooltip.textColor", new java.awt.Color(0, 0, 0));
		this.desktopProperties.put("win.xpstyle.colorName", "NormalColor");
		this.desktopProperties.put("win.xpstyle.dllName", "C:\\WINDOWS\\resources\\themes\\Aero\\Aero.msstyles");
		this.desktopProperties.put("win.xpstyle.sizeName", "NormalSize");
		this.desktopProperties.put("win.xpstyle.themeActive", true);
//		if (System.getProperty("os.name", "").startsWith("Windows")) {
//			try {
//				Field xpStyleEnabledField = sun.awt.windows.ThemeReader.class.getDeclaredField("xpStyleEnabled");
//				xpStyleEnabledField.setAccessible(true);
//				xpStyleEnabledField.setBoolean(null, true);
//			} catch (Exception e) {
//				AppLogger.debug("Failed to set xpStyleEnabled to true", e);
//			}
//		}

		if(Util.isDD()){
			RenderingHints hints= new RenderingHints(RenderingHints.KEY_FRACTIONALMETRICS,RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
			this.desktopProperties.put("awt.font.desktophints",hints);
		}


		System.setProperty("swing.disablevistaanimation","true");//See:com.sun.java.swing.plaf.windows.AnimationController.VISTA_ANIMATION_DISABLED

		int repaintInterval = 400;
		UIManager.put("ProgressBar.repaintInterval", repaintInterval);
		UIManager.put("ProgressBar.cycleTime", repaintInterval*60);
	}

	public boolean needUpdateWindow() {
		return true;
	}

	public boolean isTranslucencyCapable(GraphicsConfiguration paramGraphicsConfiguration) {
		return true;
	}

	public boolean isFrameStateSupported(int state) throws HeadlessException {
		return true;
	}

	public KeyboardFocusManagerPeer createKeyboardFocusManagerPeer(KeyboardFocusManager paramKeyboardFocusManager) throws HeadlessException {
		return new WebKeyboardFocusManagerPeer();
	}

	public FramePeer createFrame(Frame frame) throws HeadlessException {
		WebFramePeer localWFramePeer = createWebFramePeer(frame);
		return localWFramePeer;
	}

	public static void registerPeer(Component target, ComponentPeer peer){
		targetCreatedPeer(target,peer);
	}

	abstract WebFramePeer createWebFramePeer(Frame frame) throws HeadlessException;

	public DialogPeer createDialog(Dialog paramDialog) throws HeadlessException {
		WebDialogPeer localdialogPeer = createWebDialogPeer(paramDialog);
		return localdialogPeer;
	}

	abstract  WebDialogPeer createWebDialogPeer(Dialog paramDialog);

	public boolean isModalityTypeSupported(ModalityType mt) {
		return true;
	}

	public WindowPeer createWindow(Window paramWindow) throws HeadlessException {
		WebWindowPeer localwindowPeer = createWebWindowPeer(paramWindow);
		return localwindowPeer;
	}

	abstract WebWindowPeer createWebWindowPeer(Window paramWindow);

	public PanelPeer createPanel(Panel panel) {
		if (panel instanceof Applet) {
			return super.createPanel(panel);
		}

		WebPanelPeer localpanelPeer = createWebPanelPeer(panel);
		return localpanelPeer;
	}

	abstract WebPanelPeer createWebPanelPeer(Panel panel);

	public FileDialogPeer createFileDialog(FileDialog paramFileDialog) throws HeadlessException{
		WebFileDialogPeer localFileDialogPeer= createWebFileDialogPeer(paramFileDialog);
		return localFileDialogPeer;
	};

	abstract WebFileDialogPeer createWebFileDialogPeer(FileDialog paramFileDialog);


	@Override
	public synchronized MouseInfoPeer getMouseInfoPeer() {
		{
			if (mPeer == null) {
				mPeer = new WebMouseInfoPeer();
			}
			return mPeer;
		}
	}

	public FontPeer getFontPeer(String paramString, int paramInt) {
		FontPeer localObject = null;
		String str = paramString.toLowerCase();
		if (null != this.cacheFontPeer) {
			localObject = this.cacheFontPeer.get(str + paramInt);
			if (null != localObject) {
				return localObject;
			}
		}
		localObject = new WebFontPeer(paramString, paramInt);
		if (localObject != null) {
			if (null == this.cacheFontPeer) {
				this.cacheFontPeer = new Hashtable<String, FontPeer>(5, 0.9F);
			}
			if (null != this.cacheFontPeer) {
				this.cacheFontPeer.put(str + paramInt, localObject);
			}
		}
		return ((FontPeer) localObject);
	}

	public Clipboard getSystemClipboard() throws HeadlessException {
		return getWebswingClipboard();
	}

	public WebClipboard getWebswingClipboard() {
		synchronized (this) {
			if (this.clipboard == null) {
				this.clipboard = new WebClipboard("default", true);
			}
		}
		return this.clipboard;
	}

	public Clipboard getSystemSelection() throws HeadlessException {
		synchronized (this) {
			if (this.selectionClipboard == null) {
				this.selectionClipboard = new WebClipboard("selection", false);
			}
		}
		return this.selectionClipboard;
	}

	@Override
	protected Object lazilyLoadDesktopProperty(String name) {
		if ("awt.font.desktophints".equals(name)) {
			return SunToolkit.getDesktopFontHints();
		}

		return super.lazilyLoadDesktopProperty(name);
	}

	@Override
	protected RenderingHints getDesktopAAHints() {
		RenderingHints hints = new RenderingHints(null);
		hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		hints.put(RenderingHints.KEY_TEXT_LCD_CONTRAST, 140);
		return hints;

	}

	public DragSourceContextPeer createDragSourceContextPeer(DragGestureEvent paramDragGestureEvent) throws InvalidDnDOperationException {
		return WebDragSourceContextPeer.createDragSourceContextPeer(paramDragGestureEvent);
	}

	@SuppressWarnings("unchecked")
	public <T extends DragGestureRecognizer> T createDragGestureRecognizer(Class<T> type, DragSource dragSource, Component component, int act, DragGestureListener listener) {
		if (MouseDragGestureRecognizer.class.equals(type)) {
			return (T) new WebMouseDragGestureRecognizer(dragSource, component, act, listener);
		}
		return null;
	}

	protected int getScreenWidth() {
		return screenWidth;
	}

	protected int getScreenHeight() {
		return screenHeight;
	}

	public int getScreenResolution() throws HeadlessException {
		return DEFAULT_SCREEN_RESOLUTION;
	}

	public ColorModel getColorModel() throws HeadlessException {
		if (config == null) {
			resetGC();
		}
		return config.getColorModel();
	}

	public void sync() {
	}

	public Map<TextAttribute, ?> mapInputMethodHighlight(InputMethodHighlight paramInputMethodHighlight) throws HeadlessException {
		return null;
	}

	public static void resetGC() {
		config = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
	}

	public abstract void displayChanged();

	// EventQueue.invokeLater(new Runnable() {
	//
	// public void run() {
	// ((WebGraphicsEnvironment) GraphicsEnvironment.getLocalGraphicsEnvironment()).displayChanged();
	// }
	// });
	// }

	public InputMethodDescriptor getInputMethodAdapterDescriptor() throws AWTException {
		return new WebInputMethodDescriptor();
	}

	protected boolean syncNativeQueue() {
		return true;
	}

	public void grab(Window paramWindow) {
	}

	public void ungrab(Window paramWindow) {
	}

	public ButtonPeer createButton(Button paramButton) throws HeadlessException {
		throw new UnsupportedOperationException();
	}

	public TextFieldPeer createTextField(TextField paramTextField) throws HeadlessException {
		throw new UnsupportedOperationException();
	}

	public ChoicePeer createChoice(Choice paramChoice) throws HeadlessException {
		throw new UnsupportedOperationException();
	}

	public LabelPeer createLabel(Label paramLabel) throws HeadlessException {
		throw new UnsupportedOperationException();
	}

	public ListPeer createList(List paramList) throws HeadlessException {
		throw new UnsupportedOperationException();
	}

	public CheckboxPeer createCheckbox(Checkbox paramCheckbox) throws HeadlessException {
		throw new UnsupportedOperationException();
	}

	public ScrollbarPeer createScrollbar(Scrollbar paramScrollbar) throws HeadlessException {
		throw new UnsupportedOperationException();
	}

	public ScrollPanePeer createScrollPane(ScrollPane paramScrollPane) throws HeadlessException {
		throw new UnsupportedOperationException();
	}

	public TextAreaPeer createTextArea(TextArea paramTextArea) throws HeadlessException {
		throw new UnsupportedOperationException();
	}

	public MenuBarPeer createMenuBar(MenuBar paramMenuBar) throws HeadlessException {
		throw new UnsupportedOperationException();
	}

	public MenuPeer createMenu(Menu paramMenu) throws HeadlessException {
		throw new UnsupportedOperationException();
	}

	public PopupMenuPeer createPopupMenu(PopupMenu paramPopupMenu) throws HeadlessException {
		throw new UnsupportedOperationException();
	}

	public MenuItemPeer createMenuItem(MenuItem paramMenuItem) throws HeadlessException {
		throw new UnsupportedOperationException();
	}

	public CheckboxMenuItemPeer createCheckboxMenuItem(CheckboxMenuItem paramCheckboxMenuItem) throws HeadlessException {
		throw new UnsupportedOperationException();
	}

	public TrayIconPeer createTrayIcon(TrayIcon paramTrayIcon) throws HeadlessException, AWTException {
		throw new UnsupportedOperationException();
	}

	public SystemTrayPeer createSystemTray(SystemTray paramSystemTray) {
		return new WebSystemTrayPeer();
	}

	public boolean isTraySupported() {
		return false;
	}

	public RobotPeer createRobot(Robot robot, GraphicsDevice device) throws AWTException {
		return new WebRobotPeer(robot, device);
	}

	public boolean isDesktopSupported() {
		return true;
	}

	public boolean isWindowOpacityControlSupported() {
		return true;
	}

	public boolean isWindowShapingSupported() {
		return false;
	}

	public boolean isWindowTranslucencySupported() {
		return true;
	}

	public DesktopPeer createDesktopPeer(Desktop paramDesktop) throws HeadlessException {
		return new WebDesktopPeer(paramDesktop);
	}

	public PrintJob getPrintJob(Frame frame, String jobtitle, JobAttributes jobAttributes, PageAttributes pageAttributes) {
		PrintJob2D localPrintJob2D = new PrintJob2D(frame, jobtitle, jobAttributes, pageAttributes);

		if (!localPrintJob2D.printDialog()) {
			localPrintJob2D = null;
		}

		return localPrintJob2D;
	}

	public PrintJob getPrintJob(Frame frame, String jobtitle, Properties paramProperties) {
		return getPrintJob(frame, jobtitle, null, null);
	}

	public void beep() {

	}

	@Override
	public Dimension getBestCursorSize(int preferredWidth, int preferredHeight) throws HeadlessException {
		return new Dimension(preferredWidth, preferredHeight);
	}

	@Override
	public int getMaximumCursorColors() throws HeadlessException {
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean getLockingKeyState(int keyCode) throws UnsupportedOperationException {
		return false;
	}

	public GraphicsConfiguration getGraphicsConfig() {
		if (config == null) {
			resetGC();
		}
		return config;
	}

	public boolean areExtraMouseButtonsEnabled() throws HeadlessException {
		return true;
	}

	// update system colors to win7 default theme (light)
	protected void loadSystemColors(int[] systemColors) throws HeadlessException {
		if (systemColors != null && systemColors.length == 26) {
			systemColors[0] = 0xff000000;
			systemColors[1] = 0xff99b4d1;
			systemColors[2] = 0xff000000;
			systemColors[3] = 0xffb4b4b4;
			systemColors[4] = 0xffbfcddb;
			systemColors[5] = 0xff434e54;
			systemColors[6] = 0xfff4f7fc;
			systemColors[7] = 0xffffffff;
			systemColors[8] = 0xff646464;
			systemColors[9] = 0xff000000;
			systemColors[10] = 0xfff0f0f0;
			systemColors[11] = 0xff000000;
			systemColors[12] = 0xffffffff;
			systemColors[13] = 0xff000000;
			systemColors[14] = 0xff3399ff;
			systemColors[15] = 0xffffffff;
			systemColors[16] = 0xff6d6d6d;
			systemColors[17] = 0xfff0f0f0;
			systemColors[18] = 0xff000000;
			systemColors[19] = 0xffe3e3e3;
			systemColors[20] = 0xffffffff;
			systemColors[21] = 0xffa0a0a0;
			systemColors[22] = 0xff696969;
			systemColors[23] = 0xffc8c8c8;
			systemColors[24] = 0xffffffe1;
			systemColors[25] = 0xff000000;
		}
	}

	@Override
	public boolean isModalExclusionTypeSupported(Dialog.ModalExclusionType exclusionType) {
		return true;
	}

	abstract public boolean webConpoenentPeerUpdateGraphicsData();

	abstract public SurfaceData webComponentPeerReplaceSurfaceData(SurfaceManager mgr);

	abstract public int shouldNativelyFocusHeavyweight(Component heavyweight, Component descendant, boolean temporary, boolean focusedWindowChangeAllowed, long time, FocusEventCause cause) ;

	abstract public boolean deliverFocus(Component heavyweight, Component descendant, boolean temporary, boolean focusedWindowChangeAllowed, long time, FocusEventCause cause) ;

	public synchronized int executeOnBeforeShutdownListeners(final ShutdownReason reason){
		ExecutorService executor = Executors.newSingleThreadExecutor(NamedThreadFactory.getInstance("Webswing pre-shutdown thread"));
		try {
			long wait = Long.getLong(Constants.SWING_START_SYS_PROP_SYNC_TIMEOUT, Constants.SWING_START_SYS_PROP_SYNC_TIMEOUT_DEFAULT_VALUE);
			Integer delay = executor.submit(new Callable<Integer>() {
				@Override
				public Integer call() throws Exception {
					return api.fireBeforeShutdownListeners(reason);
				}
			}).get(wait, TimeUnit.MILLISECONDS);
			return delay;
		} catch (Exception e) {
			AppLogger.error("Failed to execute before-shutdown listeners",e);
		}finally {
			executor.shutdownNow();
		}
		return 0;
	}

	public synchronized void exitSwing(final int i) {
		if (!exiting) {
			exiting = true;
			Thread shutdownThread = new Thread(new Runnable() {
				@Override
				public void run() {
					//tell server to kill this application after defined time
					try {
						stopRecording();
						getSessionWatchdog().notifyExit();
						getPaintDispatcher().notifyApplicationExiting();
						api.fireShutdownListeners();
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
			});
			shutdownThread.setName("Webswing shutdown thread");
			shutdownThread.setDaemon(true);
			shutdownThread.start();
		}
	}

	public void defaultShutdownProcedure() {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				//first send windows closing event to all windows
				for (Window w : Window.getWindows()) {
					w.dispatchEvent(new WindowEvent(w, WindowEvent.WINDOW_CLOSING));
				}
			}
		});
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				//make sure we close windows created by window close listeners executed above
				for (Window w : Window.getWindows()) {
					w.setVisible(false);
					w.dispose();
				}
			}
		});
	}

	public Object getTreeLock() {
		if (TREELOCK == null) {
			TREELOCK = new JPanel().getTreeLock();
		}
		return TREELOCK;
	}

	@Override
	public WebswingApi getApi() {
		return api;
	}

	public void processApiEvent(Msg event) {
		api.processEvent(event);
	}

	@Override
	public Cursor createCustomCursor(Image cursor, Point hotSpot, String name) throws IndexOutOfBoundsException, HeadlessException {
		return new WebCursor(cursor, hotSpot, name);
	}

	public void setSwingClassLoader(ClassLoader swingClassLoader) {
		this.swingClassLoader = swingClassLoader;
	}

	public ClassLoader getSwingClassLoader() {
		return swingClassLoader;
	}

	public boolean isStatisticsLoggingEnabled() {
		return Boolean.getBoolean(Constants.SWING_START_SYS_PROP_STATISTICS_LOGGING_ENABLED);
	}

	public void setStatisticsLoggingEnabled(boolean enabled) {
		System.setProperty(Constants.SWING_START_SYS_PROP_STATISTICS_LOGGING_ENABLED, String.valueOf(enabled));
		getPaintDispatcher().notifySessionDataChanged();
	}

	public void showEvaluationWindow() {
		EvaluationProperties props = Util.getEvaluationProps();
		
		long timeout = props.getTimeout();
		String url = props.getLinkUrl();
		boolean hasUrl = url != null && url.trim().length() > 0;
		boolean hasDismissText = props.getDismissText() != null && props.getDismissText().trim().length() > 0;
		
		JFrame evalWin = new JFrame();
		evalWin.setAlwaysOnTop(true);
		evalWin.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		evalWin.setSize(getDefaultToolkit().getScreenSize().width, props.getHeight());
		evalWin.setLocation(0, 0);
		evalWin.setUndecorated(true);
		evalWin.getContentPane().setBackground(new Color(247, 215, 218));
		evalWin.getRootPane().setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.RED));
		
		Timer resizeTimer = new Timer(true);
		resizeTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				evalWin.setSize(getDefaultToolkit().getScreenSize().width, props.getHeight());
			}
		}, 250, 250);
		
		JLabel label = new JLabel(props.getMainText());
		label.setHorizontalAlignment(JLabel.CENTER);
		label.setFont(label.getFont().deriveFont(16f));
		label.setForeground(new Color(102,38,48));
		
		JLabel counter = new JLabel();
		counter.setHorizontalAlignment(JLabel.CENTER);
		counter.setText(TimeUnit.MILLISECONDS.toSeconds(timeout) + "");
		counter.setFont(label.getFont().deriveFont(20f));
		counter.setForeground(new Color(102, 38, 48));
		
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.X_AXIS));
		leftPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
		leftPanel.setOpaque(false);
		leftPanel.add(label);
		
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.X_AXIS));
		rightPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
		rightPanel.setOpaque(false);
		rightPanel.add(counter);
		
		if (hasUrl) {
			JButton linkButton = new JButton();
		    linkButton.setText(props.getLinkText());
		    linkButton.setHorizontalAlignment(SwingConstants.LEFT);
		    linkButton.setFont(label.getFont().deriveFont(16f));
		    linkButton.setForeground(new Color(102, 38, 48));
		    linkButton.setBorderPainted(false);
		    linkButton.setMargin(new Insets(0, 5, 0, 5));
		    linkButton.setOpaque(false);
		    linkButton.setContentAreaFilled(false);
		    linkButton.setFocusPainted(false);
	    	linkButton.setToolTipText(url);
	    	linkButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	    	linkButton.addActionListener((event) -> {
	    		if (Desktop.isDesktopSupported()) {
	    			try {
	    				Desktop.getDesktop().browse(URI.create(url));
	    			} catch (IOException e) {
	    				// ignore
	    			}
	    		}
	    	});
	    	
	    	leftPanel.add(linkButton);
	    }
		
		JButton dismissButton = new JButton();
		
		if (hasDismissText) {
			dismissButton.setText(props.getDismissText());
			dismissButton.setHorizontalAlignment(SwingConstants.LEFT);
			dismissButton.setFont(label.getFont().deriveFont(16f));
			dismissButton.setForeground(new Color(102, 38, 48));
			dismissButton.setBorderPainted(false);
			dismissButton.setMargin(new Insets(0, 5, 0, 5));
			dismissButton.setOpaque(false);
			dismissButton.setContentAreaFilled(false);
			dismissButton.setFocusPainted(false);
			dismissButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			dismissButton.addActionListener((event) -> {
				evalWin.dispose();
				resizeTimer.cancel();
			});
			dismissButton.setVisible(false);
			
			rightPanel.add(dismissButton);
		}
		
		evalWin.getContentPane().setLayout(new BorderLayout());
		evalWin.getContentPane().add(leftPanel, BorderLayout.WEST);
		evalWin.getContentPane().add(rightPanel, BorderLayout.EAST);
		
		evalWin.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				long end = System.currentTimeMillis() + timeout;
				Timer timer = new Timer(true);
				timer.scheduleAtFixedRate(new TimerTask() {
					@Override
					public void run() {
						counter.setText((TimeUnit.MILLISECONDS.toSeconds(end - System.currentTimeMillis()) + 1) + "");
						
						if (System.currentTimeMillis() > end) {
							timer.cancel();
							counter.setVisible(false);
							if (hasDismissText) {
								dismissButton.setVisible(true);
							}
						}
					}
				}, 250, 250);
			}
		});
		
		evalWin.setVisible(true);
	}

}
