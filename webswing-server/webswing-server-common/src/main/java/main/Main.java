package main;

import java.awt.Toolkit;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.ProtectionDomain;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.webswing.Constants;
import org.webswing.toolkit.WebToolkit;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.*;

public class Main {

	private static ClassLoader defaultCL;

	@SuppressWarnings("restriction")
	public static void main(String[] args) {
		boolean client = System.getProperty(Constants.SWING_START_SYS_PROP_CLIENT_ID) != null;

		try {
			initializeDefaultSystemProperties();

			System.setProperty(Constants.CREATE_NEW_TEMP, getCreateNewTemp(args));
			System.setProperty(Constants.CLEAN_TEMP, getBoolParam(args, "-tc", true));

			ProtectionDomain domain = Main.class.getProtectionDomain();
			URL location = domain.getCodeSource().getLocation();
			System.setProperty(Constants.WAR_FILE_LOCATION, location.toExternalForm());

			List<URL> urls = new ArrayList<URL>();
			if (client) {
				populateClasspathFromDir("WEB-INF/swing-lib", urls);
				initializeExtLibServices(urls);
				retainOnlyLauncherUrl(urls);
			} else {
				initTempDirPath(args);
				populateClasspathFromDir("WEB-INF/server-lib", urls);
			}
			defaultCL = new URLClassLoader(urls.toArray(new URL[urls.size()]), ClassLoader.getSystemClassLoader());
			Thread.currentThread().setContextClassLoader(defaultCL);
			Class<?> mainClass;
			if (client) {
				mainClass = defaultCL.loadClass("org.webswing.SwingMain");
			} else {
				try {
					mainClass = defaultCL.loadClass("org.webswing.ServerMain");
				} catch (ClassNotFoundException e) {
					InputStream readme = Main.class.getClassLoader().getResourceAsStream("WEB-INF/server-lib/README.txt");
					if (readme != null) {
						Scanner s = new Scanner(readme).useDelimiter("\\A");
						String result = s.hasNext() ? s.next() : "";
						throw new Exception(result, e);
					} else {
						throw new Exception("Unexpected error.", e);
					}
				}
			}

			Method method = mainClass.getMethod("main", args.getClass());
			method.setAccessible(true);
			try {
				method.invoke(null, new Object[] { args });
			} catch (IllegalAccessException e) {
				// This should not happen, as we have
				// disabled access checks
			}
		} catch (Exception e) {
			System.err.println("Uncaught exception.");
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void initializeDefaultSystemProperties() {
		try {
			InputStream propFile = Main.class.getClassLoader().getResourceAsStream("WEB-INF/classes/webswing.properties");
			Properties p = new Properties(System.getProperties());
			p.load(propFile);

			for (Map.Entry<Object, Object> prop : p.entrySet()) {
				if (!System.getProperties().containsKey(prop.getKey()))
					System.getProperties().put(prop.getKey(), prop.getValue());
			}

		} catch (Exception e) {
			//file does not exist, do nothing
		}
	}

	public static String getCreateNewTemp(String[] args) {
		// create the command line parser
		return getBoolParam(args, "-d", false);
	}

	public static String getBoolParam(String[] args, String param, Boolean def) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals(param) && i + 1 < args.length) {
				return args[i + 1];
			}
		}
		return def.toString();
	}

	private static void retainOnlyLauncherUrl(List<URL> urls) {
		for (Iterator<URL> i = urls.iterator(); i.hasNext(); ) {
			if (!i.next().getFile().contains("webswing-app-launcher")) {
				i.remove();
			}
		}

	}

	private static void initializeExtLibServices(List<URL> urls) throws Exception {
		// sets up Services class providing jms connection and other services in separated classloader to prevent classpath pollution of swing application.

		//JAVA9 needs to set parent classloader to ClassLoader.getPlatformClassLoader(), otherwise the parent is the boot classloader which only contains the java.base module
		//we use reflection to be backwards compatible with java8
		ClassLoader parent = null;
		try {
			parent = (ClassLoader) ClassLoader.class.getDeclaredMethod("getPlatformClassLoader").invoke(null);
		} catch (Exception e) {
			//ignore
		}

		ClassLoader extLibClassLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]), parent);
		Class<?> classLoaderUtilClass = extLibClassLoader.loadClass("org.webswing.util.ClassLoaderUtil");
		Method initializeServicesMethod = classLoaderUtilClass.getMethod("initializeServices");
		initializeServicesMethod.invoke(null);
		((WebToolkit) Toolkit.getDefaultToolkit()).init();
	}

	private static void populateClasspathFromDir(String dir, List<URL> urls) throws IOException {
		for (URL f : getFilesFromPath(Main.class.getClassLoader().getResource(dir))) {
			urls.add(f);
		}
	}

	public static List<URL> getFilesFromPath(URL r) throws IOException {
		List<URL> urls = new ArrayList<URL>();
		String tempDirPath = getTempDir().getAbsolutePath();
		if (r.getPath().contains("!")) {
			String[] splitPath = r.getPath().split("\\!/");
			String jar = splitPath[0];
			String path = splitPath[1];
			JarFile jarFile = new JarFile(new File(URI.create(jar)));
			Enumeration<JarEntry> jarEntries = jarFile.entries();
			while (jarEntries.hasMoreElements()) {
				JarEntry jarEntry = jarEntries.nextElement();
				if (!jarEntry.isDirectory() && jarEntry.getName().endsWith(".jar") && jarEntry.getName().startsWith(path)) {
					urls.add(jarEntryAsFile(jarFile, jarEntry, tempDirPath).toURI().toURL());
				}
			}
		} else {
			File dir;
			try {
				dir = new File(r.toURI());
			} catch (URISyntaxException e) {
				dir = new File(r.getPath());
			}
			if (dir.isDirectory()) {
				for (File f : dir.listFiles()) {
					if (f.isFile() && f.getName().endsWith(".jar")) {
						urls.add(f.toURI().toURL());
					}
				}
			}
		}
		return urls;
	}

	private static File jarEntryAsFile(JarFile jarFile, JarEntry jarEntry, String tempDirPath) throws IOException {
		InputStream input = null;
		OutputStream output = null;
		try {
			String name = jarEntry.getName();
			if (name.contains("/")) {
				name = name.replace('/', '_');
				int i = name.lastIndexOf(".");
				String extension = i > -1 ? name.substring(i) : "";
				name = name.substring(0, name.length() - extension.length()) + extension;
			}
			File file = new File(tempDirPath + File.separator + name).getAbsoluteFile();
			if (!file.exists()) {
				file.createNewFile();
				input = jarFile.getInputStream(jarEntry);
				output = new FileOutputStream(file);
				int readCount;
				byte[] buffer = new byte[4096];
				while ((readCount = input.read(buffer)) != -1) {
					output.write(buffer, 0, readCount);
				}
			}
			return file;
		} finally {
			close(input);
			close(output);
		}
	}

	private static void close(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static File getTempDir() {
		if (System.getProperty(Constants.TEMP_DIR_PATH) == null) {
			File baseDir = new File(System.getProperty(Constants.TEMP_DIR_PATH_BASE, System.getProperty("java.io.tmpdir"))).getAbsoluteFile();
			if (!baseDir.exists()) {
				baseDir.mkdirs();
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd-HH-mm-ss");
			String baseName;
			if (Boolean.parseBoolean(System.getProperty(Constants.CREATE_NEW_TEMP, ""))) {
				baseName = sdf.format(new Date()) + "-";
				for (int counter = 0; counter < 10; counter++) {
					File tempDir = new File(baseDir, baseName + counter);
					if (tempDir.mkdir()) {
						System.setProperty(Constants.TEMP_DIR_PATH, tempDir.toURI().toString());
						return tempDir;
					}
				}
			} else {
				baseName = "release";
				File tempDir = new File(baseDir, baseName).getAbsoluteFile();
				if (!tempDir.exists()) {
					tempDir.mkdir();
				} else if (Boolean.parseBoolean(System.getProperty(Constants.CLEAN_TEMP, "true"))) {
					for (File f : tempDir.listFiles()) {
						if (!delete(f)) {
							throw new IllegalStateException("Not possible to clean the temp folder. Make sure no other instance of webswing is running or use '-d true' option to create a new temp folder.");
						}
					}
				}
				System.setProperty(Constants.TEMP_DIR_PATH, tempDir.toURI().toString());
				return tempDir;
			}
			throw new IllegalStateException("Failed to create directory within " + 10 + " attempts (tried " + baseName + " to " + baseName + (100 - 1) + ')');
		} else {
			return new File(URI.create(System.getProperty(Constants.TEMP_DIR_PATH)));
		}
	}

	public static File getRootDir() {
		if (System.getProperty(Constants.ROOT_DIR_PATH) == null) {
			File defaultRoot = new File(System.getProperty("user.dir"));
			System.setProperty(Constants.ROOT_DIR_URI, defaultRoot.toURI().toString());
			System.setProperty(Constants.ROOT_DIR_PATH, defaultRoot.getAbsolutePath());
			return defaultRoot;
		} else {
			String pathOrUri = System.getProperty(Constants.ROOT_DIR_PATH);
			try {
				File file = new File(URI.create(pathOrUri));
				if (file.exists()) {
					System.setProperty(Constants.ROOT_DIR_URI, file.toURI().toString());
					System.setProperty(Constants.ROOT_DIR_PATH, file.getAbsolutePath());
					return file;
				} else {
					throw new IllegalArgumentException("File " + file.getAbsolutePath() + "not found.");
				}
			} catch (IllegalArgumentException e) {
				File absoluteConfigFile = new File(pathOrUri).getAbsoluteFile();
				if (absoluteConfigFile.exists()) {
					System.setProperty(Constants.ROOT_DIR_URI, absoluteConfigFile.toURI().toString());
					System.setProperty(Constants.ROOT_DIR_PATH, absoluteConfigFile.getAbsolutePath());
					return absoluteConfigFile;
				} else {
					throw new IllegalArgumentException("File " + absoluteConfigFile.getAbsolutePath() + " not found.");
				}
			}
		}
	}

	public static File getConfigProfileDir() {
		if (System.getProperty(Constants.CONFIG_PATH) == null) {
			System.setProperty(Constants.CONFIG_PATH, getRootDir().getAbsolutePath());
			return getRootDir();
		} else {
			try {
				String configProfile = System.getProperty(Constants.CONFIG_PATH);
				File relative = new File(getRootDir(), configProfile);
				if (relative.exists()) {
					return relative;
				} else {
					File absolute = new File(configProfile);
					if (absolute.exists()) {
						return absolute;
					} else {
						throw new IOException("Failed to resolve configuration profile path for '" + configProfile + "'");
					}
				}
			} catch (IOException e) {
				System.out.println("Ignoring Config profile setting due to following exception:");
				e.printStackTrace();
				System.setProperty(Constants.CONFIG_PATH, getRootDir().getAbsolutePath());
				return getRootDir();
			}
		}
	}

	private static boolean delete(File f) {
		if (f.isDirectory()) {
			for (File fx : f.listFiles()) {
				if (!delete(fx)) {
					return false;
				}
			}
		}
		return f.delete();
	}

	private static void initTempDirPath(String[] args) {
		if (args != null) {
			for (int i = 0; i < args.length - 1; i++) {
				if ("-t".equals(args[i]) || "-temp".equals(args[i])) {
					System.setProperty(Constants.TEMP_DIR_PATH_BASE, args[i + 1]);
					return;
				}
			}
		}
		System.setProperty(Constants.TEMP_DIR_PATH_BASE, "tmp");
	}

	public static ClassLoader getDefaultCL() {
		return defaultCL;
	}
}
