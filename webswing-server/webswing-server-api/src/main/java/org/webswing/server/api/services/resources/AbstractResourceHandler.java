package org.webswing.server.api.services.resources;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.server.api.base.AbstractUrlHandler;
import org.webswing.server.api.util.ServerApiUtil;
import org.webswing.server.common.util.CommonUtil;
import org.webswing.server.model.exception.WsException;

public abstract class AbstractResourceHandler extends AbstractUrlHandler implements ResourceHandler {

	private static final Logger log = LoggerFactory.getLogger(AbstractResourceHandler.class);

	private final WebResourceProvider webResourceProvider;

	public AbstractResourceHandler(AbstractUrlHandler parent, WebResourceProvider webResourceProvider) {
		super(parent);
		this.webResourceProvider = webResourceProvider;
	}

	@Override
	protected String getPath() {
		return "";
	}

	@Override
	public boolean serve(HttpServletRequest req, HttpServletResponse res) throws WsException {
		try {
			if (req.getMethod().equals("GET") || req.getMethod().equals("PUT")) {
				return lookup(req).respondGet(req, res);
			} else if (req.getMethod().equals("HEAD")) {
				return lookup(req).respondHead(req, res);
			}
			return false;
		} catch (IOException e) {
			throw new WsException("Failed to process resource.", e);
		}
	}

	protected static interface LookupResult {
		boolean respondGet(HttpServletRequest req, HttpServletResponse resp) throws IOException;

		boolean respondHead(HttpServletRequest req, HttpServletResponse resp) throws IOException;

		long getLastModified();
	}

	protected static class ErrorResult implements LookupResult {
		protected final int statusCode;
		protected final String message;

		public ErrorResult(int statusCode, String message) {
			this.statusCode = statusCode;
			this.message = message;
		}

		public long getLastModified() {
			return -1;
		}

		public boolean respondGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
			if(statusCode == HttpServletResponse.SC_NOT_FOUND){
				return false;
			}else{
				resp.sendError(statusCode,message);
				return true;
			}
		}

		public boolean respondHead(HttpServletRequest req, HttpServletResponse resp) {
			return false;
		}
	}

	private static class RedirectResult implements LookupResult {

		private final String path;

		public RedirectResult(String path) {
			if (path.startsWith("/")) {
				path = path.substring(1);
			}
			this.path = path;
		}

		public long getLastModified() {
			return -1;
		}

		public boolean respondGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
			ServerApiUtil.sendHttpRedirect(req, resp, path);
			return true;
		}

		public boolean respondHead(HttpServletRequest req, HttpServletResponse resp) throws IOException {
			ServerApiUtil.sendHttpRedirect(req, resp, path);
			return true;
		}
	}

	private static class ResourceUrl implements LookupResult {
		protected final URLConnection url;
		private final String mime;

		public ResourceUrl(String mime, URLConnection url) {
			this.mime = mime;
			this.url = url;
		}

		public long getLastModified() {
			return this.url.getLastModified();
		}

		protected void setHeaders(HttpServletResponse resp) {
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.setHeader("Cache-Control", "public, max-age=120");
			resp.setDateHeader("Last-Modified", getLastModified());
			resp.setContentType(mime);
			if (url.getContentLength() >= 0)
				resp.setContentLength(url.getContentLength());
		}

		public boolean respondGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
			long ims = req.getDateHeader("If-Modified-Since");
			if (ims != -1 && Math.abs(ims - getLastModified()) < 1000) { //modification timestamp is same rounded to seconds
				resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			} else {
				setHeaders(resp);
				final OutputStream os = resp.getOutputStream();
				CommonUtil.transferStreams(url.getInputStream(), os);
			}
			return true;
		}

		public boolean respondHead(HttpServletRequest req, HttpServletResponse resp) {
			setHeaders(resp);
			return true;
		}
	}

	private static class PreCompressedResourceUrl extends ResourceUrl {
		private final String contentEncoding;

		public PreCompressedResourceUrl(String mime, URLConnection url, String contentEncoding) {
			super(mime, url);
			this.contentEncoding = contentEncoding;
		}

		@Override
		protected void setHeaders(HttpServletResponse resp) {
			super.setHeaders(resp);
			resp.setHeader("Content-Encoding", contentEncoding);
			resp.setHeader("Vary", "Accept-Encoding");
		}
	}

	@Override
	public long getLastModified(HttpServletRequest req) {
		return lookup(req).getLastModified();
	}

	protected LookupResult lookup(HttpServletRequest req) {
		LookupResult r = (LookupResult) req.getAttribute("lookupResult-" + getFullPathMapping());
		if (r == null) {
			r = lookupNoCache(req);
			req.setAttribute("lookupResult-" + getFullPathMapping(), r);
		}
		return r;
	}

	protected LookupResult lookupNoCache(HttpServletRequest req) {
		String path = getPathInfo(req);
		return lookupNoCache(req, path);
	}

	protected LookupResult lookupNoCache(HttpServletRequest req, String path) {
		if (path.isEmpty()) {
			path = "/index.html";
		}

		if (isForbidden(path))
			return new ErrorResult(HttpServletResponse.SC_NOT_FOUND, "Forbidden");

		URL url = webResourceProvider.getWebResource(path + "/index.html");//check if this is folder with default index
		if (url != null && !req.getPathInfo().endsWith("/")) {
			return new RedirectResult(path + "/");
		}
		if (url == null) {
			url = webResourceProvider.getWebResource(path);
		}
		if (url == null) {
			return new ErrorResult(HttpServletResponse.SC_NOT_FOUND, "Not found");
		}

		String mimeType = getMimeType(url.getPath());

		// Check for pre-compressed variants (.br, .gz)
		String acceptEncoding = req.getHeader("Accept-Encoding");
		if (acceptEncoding != null) {
			// Prefer brotli over gzip
			if (acceptEncoding.contains("br")) {
				URL brUrl = webResourceProvider.getWebResource(path + ".br");
				if (brUrl != null) {
					try {
						return new PreCompressedResourceUrl(mimeType, brUrl.openConnection(), "br");
					} catch (IOException e) {
                        log.error("Failed to serve pre-compressed brotli for {}", path, e);
					}
				}
			}
			if (acceptEncoding.contains("gzip")) {
				URL gzUrl = webResourceProvider.getWebResource(path + ".gz");
				if (gzUrl != null) {
					try {
						return new PreCompressedResourceUrl(mimeType, gzUrl.openConnection(), "gzip");
					} catch (IOException e) {
                        log.error("Failed to serve pre-compressed gzip for {}", path, e);
					}
				}
			}
		}

		try {
			return new ResourceUrl(mimeType, url.openConnection());
		} catch (IOException e) {
            log.error("Failed to serve path {} with resource {}", path, url.toString(), e);
			return new ErrorResult(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	protected boolean isForbidden(String path) {
		String lpath = path.toLowerCase();
		return lpath.startsWith("/web-inf/") || lpath.startsWith("/meta-inf/");
	}

	protected String getMimeType(String path) {
		String mime = getServletContext().getMimeType(path);
		return mime != null ? mime : "application/octet-stream";
	}

}