package org.webswing.server.api.services.files.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.Constants;
import org.webswing.server.api.base.AbstractUrlHandler;
import org.webswing.server.api.services.application.AppPathHandler;
import org.webswing.server.api.services.files.FileTransferHandler;
import org.webswing.server.common.datastore.WebswingDataStoreType;
import org.webswing.server.common.model.security.WebswingAction;
import org.webswing.server.common.service.security.impl.WebswingSecuritySubject;
import org.webswing.server.model.exception.WsException;

import com.google.common.primitives.Longs;

public class FileTransferHandlerImpl extends AbstractUrlHandler implements FileTransferHandler {

	private static final Logger log = LoggerFactory.getLogger(FileTransferHandlerImpl.class);
	private static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.
	private static final String HMAC_ALGORITHM = "HmacSHA256";

	private final AppPathHandler manager;

	/**
	 * Instance-scoped HMAC key for file ID signing.
	 * Generated once per server lifecycle — all file IDs issued by this instance
	 * can be verified. File IDs do not survive a server restart, which is
	 * acceptable since the transfer data store is also ephemeral.
	 */
	private final byte[] hmacKey;

	public FileTransferHandlerImpl(AppPathHandler parent) {
		super(parent);
		this.manager = parent;

		// Generate a random HMAC key at startup
		java.security.SecureRandom sr = new java.security.SecureRandom();
		this.hmacKey = new byte[32];
		sr.nextBytes(this.hmacKey);
	}

	@Override
	protected String getPath() {
		return "file";
	}

	@Override
	public boolean serve(HttpServletRequest req, HttpServletResponse res) throws WsException {
		// override security subject resolution using transfer token
		// FIXME find a better solution
		WebswingSecuritySubject.buildAndSetTransferSubjectFrom(req);

		try {
			if (req.getMethod().equals("GET")) {
				handleDownload(req, res);
				return true;
			} else if (req.getMethod().equals("POST")) {
				handleUpload(req, res);
				return true;
			} else if (req.getMethod().equals("OPTIONS")) {
				return true;
			}
		} catch (Exception e) {
			log.error("FileTransfer failed.", e);
			throw new WsException("Failed to process file transfer " + req.getMethod(), e);
		}
		return false;
	}

	private void handleDownload(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, WsException {
		String fileId = request.getParameter("id");

		// Validate fileId format: only Base64URL characters and underscores
		if (fileId == null || !fileId.matches("[A-Za-z0-9_\\-=]+")) {
			log.warn("Invalid file id parameter received");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		String userId = getUser() != null ? getUser().getUserId() : "null";

		String fileUserId = null;
		String fileName = "";
		String fileSize = "";
		try {
			String[] fileData = fileId.split("_");
            // Expect at least 4 segments: name, userId, size, hmac
            if (fileData.length < 4) {
                log.warn("File id has insufficient segments: {}", sanitizeForLog(fileId));
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            fileName = decodeHashedFileData(fileData[0]);
            fileUserId = decodeHashedFileData(fileData[1]);
            fileSize = decodeHashedFileData(fileData[2]);

            // Verify HMAC to prevent forged file IDs
            String payload = fileData[0] + "_" + fileData[1] + "_" + fileData[2];
            String expectedMac = fileData[3];
            if (!verifyHmac(payload, expectedMac)) {
                log.warn("HMAC verification failed for file id [{}]", sanitizeForLog(fileId));
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        } catch (Exception e) {
			log.error("Failed to decode file data [{}]!", sanitizeForLog(fileId), e);
			response.sendError(HttpServletResponse.SC_NOT_FOUND); // 404.
			return;
		}

		checkPermission(WebswingAction.file_download);

		if (!userId.equals(fileUserId)) {
			log.error("Requested file for user [{}] by user [{}] not allowed!", sanitizeForLog(fileUserId), sanitizeForLog(userId));
			response.sendError(HttpServletResponse.SC_FORBIDDEN); // 403.
			return;
		}

		try {
			InputStream is = manager.getDataStore().readData(WebswingDataStoreType.transfer.name(), fileId, Long.getLong(Constants.FILE_SERVLET_WAIT_TIMEOUT, 300000));

			if (is != null) {
				response.reset();
				response.setBufferSize(DEFAULT_BUFFER_SIZE);
				response.setContentType("application/octet-stream");
				Long longSize = Longs.tryParse(fileSize);
				if (longSize != null && longSize > 0) {
					response.setHeader("Content-Length", fileSize);
				}
				String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedName + "\"; filename*=UTF-8''" + encodedName);
				BufferedInputStream input = null;
				BufferedOutputStream output = null;

				try {
					input = new BufferedInputStream(is, DEFAULT_BUFFER_SIZE);
					output = new BufferedOutputStream(response.getOutputStream(), DEFAULT_BUFFER_SIZE);

					byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
					int length;
					while ((length = input.read(buffer)) > 0) {
						output.write(buffer, 0, length);
					}
				} finally {
					close(output);
					close(input);
				}

				return;
			}
		} catch (Exception e) {
			log.error("Error while downloading file id [{}], name [{}]!", sanitizeForLog(fileId), sanitizeForLog(fileName), e);
		}

		response.sendError(HttpServletResponse.SC_NOT_FOUND); // 404.
    }

	private void handleUpload(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException, WsException {
		checkPermission(WebswingAction.file_upload);
		try {
			double maxMB = manager.getConfig().getUploadMaxSize();
			long maxsize = (long) (maxMB * 1024 * 1024);
			Part filePart = request.getPart("files[]"); // Retrieves <input type="file" name="file">

			if (filePart == null) {
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				resp.getWriter().write("No file part found in request.");
				return;
			}

			String filename = getFilename(filePart);

			// Validate filename
			if (filename == null || filename.isEmpty()) {
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				resp.getWriter().write("Invalid or missing filename.");
				return;
			}

			if (maxsize > 0 && filePart.getSize() > maxsize) {
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				resp.getWriter().write(String.format("File '%s' is too large. (Max. file size is %.1fMB)", escapeJson(filename), maxMB));
			} else {
				String fileId = createHashedUploadFileId(filename, filePart.getSize() + "");
				try (InputStream filecontent = filePart.getInputStream()) {
					manager.getDataStore().storeData(WebswingDataStoreType.transfer.name(), fileId, filecontent, true);
				}

				log.info("File {} uploaded (size:{})", sanitizeForLog(filename), filePart.getSize());

				resp.setContentType("application/json; charset=UTF-8");
				resp.setCharacterEncoding("UTF-8");
				resp.getWriter().write("{\"files\":[{\"name\":\"" + escapeJson(filename) + "\", \"id\":\"" + escapeJson(fileId) + "\"}]}");
			}
		} catch (Exception e) {
			if (e.getCause() instanceof EOFException) {
				log.warn("File upload canceled by user: {}", sanitizeForLog(e.getMessage()));
			} else {
				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				resp.getWriter().write("Upload finished with error...");
				log.error("Error while uploading file: {}", sanitizeForLog(e.getMessage()), e);
			}
		}
	}

	private String createHashedUploadFileId(String fileName, String fileSize) {
		String hashedName = new String(Base64.getUrlEncoder().encode(fileName.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
		String hashedUserId = getUser() != null ? getUser().getUserId() : "null";
		String hashedSize = new String(Base64.getUrlEncoder().encode(fileSize.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
		hashedUserId = new String(Base64.getUrlEncoder().encode(hashedUserId.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);

		String payload = hashedName + "_" + hashedUserId + "_" + hashedSize;
		String mac = computeHmac(payload);
		return payload + "_" + mac;
	}

	private String decodeHashedFileData(String data) {
		return new String(Base64.getUrlDecoder().decode(data.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
	}

	/**
	 * Compute HMAC-SHA256 over the given payload, returned as URL-safe Base64.
	 */
	private String computeHmac(String payload) {
		try {
			Mac mac = Mac.getInstance(HMAC_ALGORITHM);
			mac.init(new SecretKeySpec(hmacKey, HMAC_ALGORITHM));
			byte[] raw = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
			return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
		} catch (Exception e) {
			throw new RuntimeException("HMAC computation failed", e);
		}
	}

	/**
	 * Verify that the given HMAC matches the payload.
	 * Uses constant-time comparison to prevent timing attacks.
	 */
	private boolean verifyHmac(String payload, String expectedMac) {
		String computedMac = computeHmac(payload);
		return java.security.MessageDigest.isEqual(
				computedMac.getBytes(StandardCharsets.UTF_8),
				expectedMac.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Sanitize a string for safe inclusion in log messages.
	 * Strips newlines, tabs, and carriage returns to prevent log injection.
	 */
	private static String sanitizeForLog(String input) {
		if (input == null) return "null";
		return input.replaceAll("[\\r\\n\\t]", "_");
	}

	/**
	 * Escape a string for safe inclusion in a JSON string value.
	 * Handles backslash, double-quote, and control characters.
	 */
	private static String escapeJson(String input) {
		if (input == null) return "";
		return input
					   .replace("\\", "\\\\")
					   .replace("\"", "\\\"")
					   .replace("\n", "\\n")
					   .replace("\r", "\\r")
					   .replace("\t", "\\t");
	}

	private void close(Closeable resource) {
		if (resource != null) {
			try {
				resource.close();
			} catch (IOException e) {
				log.warn("Failed to close resource", e);
			}
		}
	}

	private String getFilename(Part part) {
		if (part == null) {
			return null;
		}
		String header = part.getHeader("Content-Disposition");
		if (header == null) {
			return null;
		}
		for (String cd : header.split(";")) {
			if (cd.trim().startsWith("filename")) {
				String filename = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
				// Extract base name — handle both forward and back slashes
				int lastSlash = filename.lastIndexOf('/');
				int lastBackslash = filename.lastIndexOf('\\');
				int lastSep = Math.max(lastSlash, lastBackslash);
				String baseName = filename.substring(lastSep + 1);
				// Reject path traversal patterns and empty names
				if (baseName.isEmpty() || baseName.contains("..")) {
					return null;
				}
				return baseName;
			}
		}
		return null;
	}

}