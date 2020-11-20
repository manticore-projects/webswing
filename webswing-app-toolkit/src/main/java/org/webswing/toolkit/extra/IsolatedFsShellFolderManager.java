package org.webswing.toolkit.extra;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.webswing.Constants;
import org.webswing.util.AppLogger;

import sun.awt.shell.PublicShellFolderManager;
import sun.awt.shell.ShellFolder;

@SuppressWarnings("restriction")
public class IsolatedFsShellFolderManager extends PublicShellFolderManager {

	private static File root;
	private static List<File> roots = new ArrayList<File>();
	private static String platformImagesFolder = System.getProperty("webswing.platformIcons","webswingplatformicons");

	static {
		String path = System.getProperty(Constants.SWING_START_SYS_PROP_TRANSFER_DIR, System.getProperty("user.dir") + "/upload");
		String[] paths = path.split(File.pathSeparator);
		for (int i = 0; i < paths.length; i++) {
			File root = new IsolatedRootFile(paths[i]);
			if (!root.getAbsoluteFile().exists()) {
				root.mkdirs();
			}
			roots.add(root);
			if (i == 0) {
				IsolatedFsShellFolderManager.root = root;
				if(Boolean.getBoolean(Constants.SWING_START_SYS_PROP_USE_SHARED_USER_HOME)) {
					System.setProperty("user.home", root.getAbsolutePath());
				}
			}
		}
	}

	@Override
	public Object get(String key) {
		if (key.equals("fileChooserDefaultFolder")) {
			return ensureExists(root);
		}
		if (key.equals("roots")) {
			return ensureExists(roots.toArray(new File[roots.size()]));
		}
		if (key.equals("fileChooserComboBoxFolders")) {
			return ensureExists(roots.toArray(new File[roots.size()]));
		}
		if (key.equals("fileChooserShortcutPanelFolders")) {
			return ensureExists(roots.toArray(new File[roots.size()]));
		}
		if (key.startsWith("fileChooserIcon ")) {
			String fcIconType;
			if (key.equals("fileChooserIcon ListView") || key.equals("fileChooserIcon ViewMenu")) {
				fcIconType = platformImagesFolder + "/ListView.gif";
			} else if (key.equals("fileChooserIcon DetailsView")) {
				fcIconType = platformImagesFolder + "/DetailsView.gif";;
			} else if (key.equals("fileChooserIcon UpFolder")) {
				fcIconType = platformImagesFolder + "/UpFolder.gif";;
			} else if (key.equals("fileChooserIcon NewFolder")) {
				fcIconType = platformImagesFolder + "/NewFolder.gif";;
			} else {
				return null;
			}
			return getImage(fcIconType,key);
		}
		if (key.startsWith("optionPaneIcon ")) {
			String iconType;
			if (key == "optionPaneIcon Error") {
				iconType = platformImagesFolder + "/Error.gif";
			} else if (key == "optionPaneIcon Information") {
				iconType = platformImagesFolder + "/Inform.gif";
			} else if (key == "optionPaneIcon Question") {
				iconType = platformImagesFolder + "/Question.gif";
			} else if (key == "optionPaneIcon Warning") {
				iconType = platformImagesFolder + "/Warn.gif";
			} else {
				return null;
			}
			return getImage(iconType,key);
		}
		if (key.startsWith("shell32Icon ") || key.startsWith("shell32LargeIcon ")) {
			String name = key.substring(key.indexOf(" ") + 1);
			boolean large = key.startsWith("shell32LargeIcon ");
			String filename= platformImagesFolder + "/"+name+(large?"_large":"")+".png";
			return getImage(filename,key);
		}
		return null;
	}

	private File[] ensureExists(File[] roots) {
		for (File f : roots) {
			ensureExists(f);
		}
		return roots;
	}

	private File ensureExists(File f) {
		if (!f.getAbsoluteFile().exists()) {
			boolean done = f.mkdirs();
			if (done) {
				AppLogger.error("Isolated filesystem folder " + f.getAbsolutePath() + "not found. Make sure the folder is unique for each session (use ${user} variable) or disable the 'Clear Upload Folder' option in configuration.");
			} else {
				AppLogger.error("Isolated filesystem folder " + f.getAbsolutePath() + " could not be created. Make sure the path is valid and the process has write access.");
			}
		}
		return f;
	}

	@Override
	public ShellFolder createShellFolder(File paramFile) throws FileNotFoundException {
		try {
			if (isSubfolderOfRoots(paramFile)) {
				return super.createShellFolder(paramFile);
			} else {
				throw new FileNotFoundException("Path is outside the allowed Webswing Filesystem isolation folder. (" + paramFile.getCanonicalPath() + ")");
			}
		} catch (IOException e) {
			System.err.println("Error while creating ShellFolder. " + e.getMessage());
			if (e instanceof FileNotFoundException) {
				throw (FileNotFoundException) e;
			} else {
				throw new FileNotFoundException("Error while creating ShellFolder. " + e.getMessage());
			}
		}
	}

	public static boolean isSubfolderOfRoots(File paramFile) throws IOException {
		if(Boolean.getBoolean(Constants.SWING_START_SYS_PROP_ISOLATED_FS)) {
			String cp = paramFile.getCanonicalPath();
			for (File root : roots) {
				if (cp.startsWith(root.getCanonicalPath())) {
					return true;
				}
			}
		}
		return false;
	}


	@Override
	public boolean isFileSystemRoot(File paramFile) {
		try {
			for (File root : roots) {
				if (root.getCanonicalPath().equals(paramFile.getCanonicalPath())) {
					return true;
				}
			}
			return false;
		} catch (IOException e1) {
			return super.isFileSystemRoot(paramFile);
		}
	}

	private Image getImage(String imageFile, String key) {
		try (InputStream resource = ClassLoader.getSystemResourceAsStream(imageFile)) {
			if (resource == null) {
				AppLogger.debug("IsolatedFsShellFolderManager.get("+key+"): Webswing platform image "+imageFile+" not found." );
				return null;
			}

			try (BufferedInputStream in = new BufferedInputStream(resource); ByteArrayOutputStream out = new ByteArrayOutputStream(1024)) {
				byte[] buffer = new byte[1024];
				int n;
				while ((n = in.read(buffer)) > 0) {
					out.write(buffer, 0, n);
				}
				out.flush();
				return Toolkit.getDefaultToolkit().createImage(out.toByteArray());
			}
		} catch (IOException ioe) {
			AppLogger.error("IsolatedFsShellFolderManager.getImage("+imageFile+"): "+ioe.toString());
		}
		return null;
	}
}
