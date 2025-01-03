package org.webswing;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.webswing.ext.services.ToolkitFXService;
import org.webswing.toolkit.util.Services;
import org.webswing.toolkit.util.Util;
import org.webswing.util.AppLogger;
import org.webswing.util.ClasspathUtil;

public class SwingMain {

	public static ClassLoader swingLibClassLoader;
//	private static final VarHandle MODIFIERS;
//
//	static {
//		try {
//			ByteBuddyAgent.install();
//
//			var lookup = MethodHandles.privateLookupIn(Field.class, MethodHandles.lookup());
//			MODIFIERS = lookup.findVarHandle(Field.class, "modifiers", int.class);
//		} catch (IllegalAccessException | NoSuchFieldException ex) {
//			throw new RuntimeException(ex);
//		}
//	}
//
//	static {
//		try {
//			injectCTCGraphicsEnvironment();
//
//			Field toolkit = Toolkit.class.getDeclaredField("toolkit");
//			toolkit.setAccessible(true);
//			toolkit.set(null, new WebToolkit11());
//
//			Field defaultHeadlessField = java.awt.GraphicsEnvironment.class.getDeclaredField("defaultHeadless");
//			defaultHeadlessField.setAccessible(true);
//			defaultHeadlessField.set(null, Boolean.FALSE);
//			Field headlessField = java.awt.GraphicsEnvironment.class.getDeclaredField("headless");
//			headlessField.setAccessible(true);
//			headlessField.set(null, Boolean.FALSE);
//
//			Class<?> smfCls = Class.forName("sun.java2d.SurfaceManagerFactory");
//			Field smf = smfCls.getDeclaredField("instance");
//			smf.setAccessible(true);
//			smf.set(null, null);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		System.setProperty("swing.defaultlaf", MetalLookAndFeel.class.getName());
//	}
//
//	public static void injectCTCGraphicsEnvironment() throws ClassNotFoundException, IOException {
//		/*
//		 * ByteBuddy is used to intercept the methods that return the graphics environment in use
//		 * (java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment() and
//		 *  sun.awt.PlatformGraphicsInfo.createGE())
//		 *
//		 * Since java.awt.GraphicsEnvironment is loaded by the bootstrap class loader,
//		 * all classes used by CTCGraphicsEnvironment also need to be available to the bootstrap class loader,
//		 * as that class loader also loads the CTCInterceptor class, which will instantiate CTCGraphicsEnvironment.
//		 */
//		injectClassIntoBootstrapClassLoader(
//				WebGraphicsEnvironment.class,
//				WebGraphicsConfig.class,
//				WebPlatformFactory.class);
//
//		ByteBuddy byteBuddy = new ByteBuddy();
//
//		byteBuddy
//				.redefine(
//						TypePool.Default.ofSystemLoader().describe("java.awt.GraphicsEnvironment").resolve(),
//						ClassFileLocator.ForClassLoader.ofSystemLoader())
//				.method(ElementMatchers.named("getLocalGraphicsEnvironment"))
//				.intercept(
//						MethodDelegation.to(CTCInterceptor.class))
//				.make()
//				.load(
//						Object.class.getClassLoader(),
//						ClassReloadingStrategy.fromInstalledAgent());
//
//		TypeDescription platformGraphicInfosType;
//		platformGraphicInfosType = TypePool.Default.ofSystemLoader().describe("sun.awt.PlatformGraphicsInfo").resolve();
//		ClassFileLocator locator = ClassFileLocator.ForClassLoader.ofSystemLoader();
//
//		byteBuddy
//				.redefine(
//						platformGraphicInfosType,
//						locator)
//				.method(
//						nameStartsWith("createGE"))
//				.intercept(
//						MethodDelegation.to(GraphicsEnvironmentInterceptor.class))
//				.make()
//				.load(
//						Thread.currentThread().getContextClassLoader(),
//						ClassReloadingStrategy.fromInstalledAgent());
//
//	}
//
//	public static class GraphicsEnvironmentInterceptor {
//		@RuntimeType
//		public static Object intercept(@Origin Method method, @AllArguments final Object[] args) throws Exception {
//			return new WebGraphicsEnvironment11();
//		}
//	}
//
//	public static class CTCInterceptor {
//		@RuntimeType
//		public static GraphicsEnvironment intercept() {
//			return new WebGraphicsEnvironment11();
//		}
//	}
//
//	private static void injectClassIntoBootstrapClassLoader(Class<?>... classes) throws IOException {
//		for (Class<?> clazz: classes) {
//			final byte[] buffer = clazz.getClassLoader().getResourceAsStream(clazz.getName().replace('.', '/').concat(".class")).readAllBytes();
//			ClassInjector.UsingUnsafe injector = new ClassInjector.UsingUnsafe(null);
//			injector.injectRaw(Map.of(clazz.getName(), buffer));
//		}
//	}

	public static void main(String[] args) {
		try {

			String relativeBase = System.getProperty(Constants.SWING_START_SYS_PROP_APP_HOME);
			String cp = System.getProperty(Constants.SWING_START_SYS_PROP_CLASS_PATH);
			URL[] urls = ClasspathUtil.populateClassPath(cp, relativeBase);
			/*
			 * wrap into additional URLClassLoader with class path urls because
			 * some resources may contain classes from packages that should be loaded
			 * with parent class loader that otherwise would not have a classpath
			 */
			ClassLoader wrapper = new URLClassLoader(urls, SwingMain.class.getClassLoader());
			swingLibClassLoader = Services.getClassLoaderService().createSwingClassLoader(urls, wrapper);
			initTempFolder();

			startSwingApp(args);

		} catch (Exception e) {
			AppLogger.fatal("SwingMain:main", e);
			System.exit(1);
		}
	}

	private static void startSwingApp(String[] args) throws Exception {
		setupContextClassLoader(swingLibClassLoader);
		Class<?> clazz = swingLibClassLoader.loadClass(System.getProperty(Constants.SWING_START_SYS_PROP_MAIN_CLASS));
		Class<?>[] mainArgType = {String[].class};
		java.lang.reflect.Method main = clazz.getMethod("main", mainArgType);
		Util.getWebToolkit().startDispatchers();
		initializeJavaFX();
		Object[] argsArray = { args };
		main.invoke(null, argsArray);
	}

	private static void startApplet() throws Exception {
		AppLogger.error("Error in SwingMain: Applets have been removed");
	}

	public static void initializeJavaFX() throws InvocationTargetException, InterruptedException {
		if (Constants.SWING_START_SYS_PROP_JFX_TOOLKIT_WEB.equals(System.getProperty(Constants.SWING_START_SYS_PROP_JFX_TOOLKIT))) {

			// Fix jvm crash starting javafx app on windows - See: https://bugs.openjdk.java.net/browse/JDK-8201539
			if (System.getProperty("os.name", "").startsWith("Windows")) {
				try {
					System.load("C:\\Windows\\System32\\WindowsCodecs.dll");
				} catch (UnsatisfiedLinkError e) {
					System.err.println("Native code library failed to load.\n" + e);
				}
			}

			//start swing dispatch thread
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					//nothing to do
				}
			});
			//start JavaFx platform
			try {
				Class<?> clazz = swingLibClassLoader.loadClass("com.sun.javafx.application.PlatformImpl");
				Class<?> startupAttrType[] = { Runnable.class };
				java.lang.reflect.Method startup = clazz.getMethod("startup", startupAttrType);
				startup.invoke(null,new Runnable() {
					@Override
					public void run() {
						try {
							Class<?> toolkitFXServiceImplClass = swingLibClassLoader.loadClass("org.webswing.javafx.toolkit.ToolkitFXServiceImpl");
							Method m = toolkitFXServiceImplClass.getMethod("getInstance");
							Object instance = m.invoke(null);
							ToolkitFXService toolkitFXServiceImpl = (ToolkitFXService) instance;
							Services.initializeToolkitFXService(toolkitFXServiceImpl);
						} catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
							AppLogger.error("Failed to initialize ToolkitFXServiceImpl", e);
						}
					}
				});
			} catch (IllegalAccessException |ClassNotFoundException|NoSuchMethodException  e) {
				AppLogger.error("Failed to initialize Javafx Platform",e);
			}
		}
	}

	private static void setupContextClassLoader(ClassLoader swingClassLoader) {
		Util.getWebToolkit().setSwingClassLoader(swingLibClassLoader);
		Thread.currentThread().setContextClassLoader(swingClassLoader);
		try {
			EventQueue q = Toolkit.getDefaultToolkit().getSystemEventQueue();
			Class<?> systemQueue = q.getClass();
			Field cl = systemQueue.getDeclaredField("classLoader");
			cl.setAccessible(true);
			cl.set(q, Thread.currentThread().getContextClassLoader());
		} catch (Exception e) {
			AppLogger.error("Error in SwingMain: EventQueue thread - setting context class loader failed.", e);
		}
	}

	private static Map<String, String> resolveProps() {
		HashMap<String, String> result = new HashMap<String, String>();
		for (Object keyObj : System.getProperties().keySet()) {
			String key = (String) keyObj;
			if (key.startsWith(Constants.SWING_START_STS_PROP_APPLET_PARAM_PREFIX)) {
				String paramKey = key.substring(Constants.SWING_START_STS_PROP_APPLET_PARAM_PREFIX.length());
				result.put(paramKey, System.getProperty(key));
			}
		}
		return result;
	}

	private static boolean isApplet() {
		return false;
	}

	private static void initTempFolder() {
		//try to create java.io.tmpdir if does not exist
		try {
			File f = new File(System.getProperty("java.io.tmpdir", ".")).getAbsoluteFile();
			if (!f.exists()) {
				f.mkdirs();
			}
		} catch (Exception e) {
			//ignore if not possible to create
		}
	}
}
