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

	private final AppPathHandler manager;
	
	public FileTransferHandlerImpl(AppPathHandler parent) {
		super(parent);
		this.manager = parent;
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
			} else if (req.getMethod().equals("OPTIONS")){
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
		String userId = getUser() != null ? getUser().getUserId() : "null";

		String fileUserId = null;
		String fileName = "";
		String fileSize = "";
		try {
			String[] fileData = fileId.split("_");
			if (fileData != null) {
				if (fileData.length > 0) {
					fileName = decodeHashedFileData(fileData[0]);
				}
				if (fileData.length > 1) {
					fileUserId = decodeHashedFileData(fileData[1]);
				}
				if (fileData.length > 2) {
					fileSize = decodeHashedFileData(fileData[2]);
				}
			}
		} catch (Exception e) {
			log.error("Failed to decode file data [" + fileId + "]!", e);
			response.sendError(HttpServletResponse.SC_NOT_FOUND); // 404.
			return;
		}
		
		checkPermission(WebswingAction.file_download);

		if (!userId.equals(fileUserId)) {
			log.error("Requested file for user [" + fileUserId + "] by user [" + userId + "] not allowed!");
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
				String encodedName= URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+","%20");
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
			log.error("Error while downloading file id [" + fileId + "], name [" + fileName + "]!", e);
		}
		
		response.sendError(HttpServletResponse.SC_NOT_FOUND); // 404.
		return;
	}

	private void handleUpload(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException, WsException {
		checkPermission(WebswingAction.file_upload);
		try {
			double maxMB = manager.getConfig().getUploadMaxSize();
			long maxsize = (long) (maxMB * 1024 * 1024);
			Part filePart = request.getPart("files[]"); // Retrieves <input type="file" name="file">
			String filename = getFilename(filePart);
			if (maxsize > 0 && filePart.getSize() > maxsize) {
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				resp.getWriter().write(String.format("File '%s' is too large. (Max. file size is %.1fMB)", escapeHtml(filename), maxMB));
			} else {
				String fileId = createHashedUploadFileId(filename, filePart.getSize() + "");
				try (InputStream filecontent = filePart.getInputStream()) {
					manager.getDataStore().storeData(WebswingDataStoreType.transfer.name(), fileId, filecontent, true);
				}

				log.info("File " + filename + " uploaded (size:" + filePart.getSize() + ")");
				
				resp.setContentType("application/json; charset=UTF-8");
				resp.setCharacterEncoding("UTF-8");
				resp.getWriter().write("{\"files\":[{\"name\":\"" + filename + "\", \"id\":\"" + fileId + "\"}]}"); // FIXME size
			}
		} catch (Exception e) {
			if (e.getCause() instanceof EOFException) {
				log.warn("File upload canceled by user: " + e.getMessage());
			} else {
				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				resp.getWriter().write("Upload finished with error...");
				log.error("Error while uploading file: " + e.getMessage(), e);
			}
		}
	}
	
	private String createHashedUploadFileId(String fileName, String fileSize) {
		String hashedName = new String(Base64.getUrlEncoder().encode(fileName.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
		String hashedUserId = getUser() != null ? getUser().getUserId() : "null";
		String hashedSize = new String(Base64.getUrlEncoder().encode(fileSize.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
		hashedUserId = new String(Base64.getUrlEncoder().encode(hashedUserId.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
		return hashedName + "_" + hashedUserId + "_" + hashedSize;
	}

	private String decodeHashedFileData(String data) {
		return new String(Base64.getUrlDecoder().decode(data.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
	}
	
	private void close(Closeable resource) {
		if (resource != null) {
			try {
				resource.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private String escapeHtml(String input) {
		if (input == null) {
			return null;
		}
		return input
			.replace("&", "&amp;")
			.replace("<", "&lt;")
			.replace(">", "&gt;")
			.replace("\"", "&quot;")
			.replace("'", "&#x27;");
	}

	private String getFilename(Part part) {
		for (String cd : part.getHeader("Content-Disposition").split(";")) {
			if (cd.trim().startsWith("filename")) {
				String filename = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
				return filename.substring(filename.lastIndexOf('/') + 1).substring(filename.lastIndexOf('\\') + 1); // MSIE fix.
			}
		}
		return null;
	}

}
