package org.webswing.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.webswing.classloader.SwingClassLoaderFactory;
import org.webswing.ext.services.ImageService;
import org.webswing.ext.services.PdfService;
import org.webswing.ext.services.SwingClassLoaderFactoryService;
import org.webswing.services.impl.DataStoreServiceImpl;
import org.webswing.services.impl.DirectDrawServiceImpl;
import org.webswing.services.impl.ImageServiceImpl;
import org.webswing.services.impl.JPDFWriterPdfServiceImpl;
import org.webswing.services.impl.JsLinkServiceImpl;
import org.webswing.services.impl.PdfServiceImpl;
import org.webswing.services.impl.ServerConnectionServiceImpl;
import org.webswing.toolkit.util.Services;

public class ClassLoaderUtil {
	
	private static ClassLoader serviceClassLoader;

	/**
	 * Called from main.Main using reflection to initialize services in isolated classloader.
	 */
	public static void initializeServices() {
		serviceClassLoader = Thread.currentThread().getContextClassLoader();
		
		ImageService imageService = ImageServiceImpl.getInstance();
		SwingClassLoaderFactoryService classloaderService = SwingClassLoaderFactory.getInstance();
		DirectDrawServiceImpl directDrawServiceImpl = DirectDrawServiceImpl.getInstance();
		ServerConnectionServiceImpl serverService = ServerConnectionServiceImpl.getInstance();
		DataStoreServiceImpl dataStoreService = DataStoreServiceImpl.getInstance();
		JsLinkServiceImpl jsLinkService = JsLinkServiceImpl.getInstance();
		
		PdfService pdfService;
		try {
			Class.forName("com.qoppa.pdfWriter.PDFDocument");
			pdfService = JPDFWriterPdfServiceImpl.getInstance();
		} catch (ClassNotFoundException e) {
			AppLogger.info("JPDFWriter library not found on classpath, fallback to FreeHEP PDF service.");
			pdfService = PdfServiceImpl.getInstance();
		}
		
		Services.initialize(imageService, pdfService, serverService, dataStoreService, classloaderService, directDrawServiceImpl, jsLinkService);
		// start jms connection to server
		serverService.initialize();
	}

	public static List<Method> getAllConstructors(JavaClass clazz) {
		List<Method> result = new ArrayList<Method>();
		for (Method m : clazz.getMethods()) {
			if (m.getName().equals("<init>")) {
				result.add(m);
			}
		}
		return result;
	}

	public static Method getPaintMethod(JavaClass clazz) {
		for (Method m : clazz.getMethods()) {
			if (m.getName().equals("paint") && m.isPublic() && m.getArgumentTypes().length == 1 && m.getArgumentTypes()[0].getSignature().equals("Ljava/awt/Graphics;")) {
				return m;
			}
		}
		return null;
	}

	public static boolean isSubClassOfJComponent(JavaClass clazz) {
		try {
			for (JavaClass c : clazz.getSuperClasses()) {
				if (c.getClassName().equals("javax.swing.JComponent")) {
					return true;
				}
			}
			return false;
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean isInPackage(String packageInspected, String[] packagePrefixed) {
		for (String prefix : packagePrefixed) {
			if (packageInspected != null && packageInspected.startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}

	public static String[] createArgNames(int number) {
		String[] result = new String[number];
		for (int i = 0; i < number; i++) {
			result[i] = "arg" + i;
		}
		return result;
	}

	public static InstructionHandle findInstructionHandle(InstructionList il, Instruction i) {
		for (InstructionHandle ih = il.getStart(); ih != null; ih = ih.getNext()) {
			if (ih.getInstruction().equals(i)) {
				return ih;
			}
		}
		return null;
	}
	
	public static ClassLoader getServiceClassLoader() {
		return serviceClassLoader;
	}

}
