package org.webswing.server.common.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.Constants;

import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
import com.google.common.collect.Iterators;

public class ServerUtil {

	private static final Logger log = LoggerFactory.getLogger(ServerUtil.class);

	private static final DateFormat EXPIRES_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", java.util.Locale.US);

	private static final String HEADER_AUTHORIZATION = "Authorization";
	private static final String BEARER_TYPE = "Bearer";

	private static final JsonMapper mapper = new JsonMapper();

	static {
		EXPIRES_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	public static String getClientIp(HttpServletRequest r) {
		String result = null;
		result = r.getHeader("X-Forwarded-For");
		if (result == null) {
			result = r.getRemoteAddr();
		}
		return result;
	}

	public static String getClientOs(String userAgent) {
		if (userAgent == null) {
			return "Unknown";
		}
		if (userAgent.toLowerCase().indexOf("windows") >= 0) {
			return "Windows";
		} else if (userAgent.toLowerCase().indexOf("mac") >= 0) {
			return "Mac";
		} else if (userAgent.toLowerCase().indexOf("x11") >= 0) {
			return "Linux";
		} else if (userAgent.toLowerCase().indexOf("android") >= 0) {
			return "Android";
		} else if (userAgent.toLowerCase().indexOf("iphone") >= 0) {
			return "IPhone";
		} else {
			return "Unknown";
		}
	}

	public static String domainFromUrl(String fullUrl) {
		try {
			URL url = new URL(fullUrl);
			return url.getProtocol() + "://" + url.getHost() + (url.getPort() != -1 ? ":" + url.getPort() : "");
		} catch (MalformedURLException e) {
			return null;
		}
	}

	public static boolean isAdminUrlSameOrigin(String adminUrl, String url) {
		if (StringUtils.isBlank(adminUrl)) {
			return false;
		}

		if (adminUrl.startsWith("http")) {
			return Objects.equals(ServerUtil.domainFromUrl(adminUrl), ServerUtil.domainFromUrl(url));
		}

		// adminUrl is relative, consider it same origin
		return true;
	}

	public static String getClientBrowser(String userAgent) {
		if (userAgent == null) {
			return "Unknown";
		}
		String ua = userAgent.toLowerCase();

		// Order matters: check more specific UA tokens before generic ones,
		// since Chromium-based browsers all include "chrome" in the UA string.

		if (ua.contains("edg/")) {
			// Chromium-based Edge (not legacy "Edge/")
			return extractBrowserVersion(userAgent, "Edg");
		} else if (ua.contains("opr/") || ua.contains("opera")) {
			// Opera (modern OPR/ or legacy Opera/)
			String token = ua.contains("opr/") ? "OPR" : "Opera";
			return extractBrowserVersion(userAgent, token).replace("OPR", "Opera");
		} else if (ua.contains("samsungbrowser/")) {
			return extractBrowserVersion(userAgent, "SamsungBrowser");
		} else if (ua.contains("firefox/")) {
			return extractBrowserVersion(userAgent, "Firefox");
		} else if (ua.contains("chrome/")) {
			// Must come after Edge, Opera, Samsung — they all contain "chrome"
			return extractBrowserVersion(userAgent, "Chrome");
		} else if (ua.contains("safari/") && ua.contains("version/")) {
			// Safari (real Safari, not Chrome pretending)
			return extractBrowserVersion(userAgent, "Version").replace("Version", "Safari");
		}

		return "Unknown";
	}

	private static String extractBrowserVersion(String userAgent, String token) {
		int idx = userAgent.indexOf(token);
		if (idx < 0) {
			return token;
		}
		// Grab "Token/1.2.3" and replace "/" with "-"
		String fragment = userAgent.substring(idx).split(" ")[0];
		return fragment.replace("/", "-");
	}

	public static URL getFileResource(String resource, File folder) {
		URL result = null;
		if (folder != null && folder.isDirectory()) {
			File file = new File(folder, resource);
			if (file.isFile()) {
				try {
					String folderUrl = folder.getCanonicalPath();
					String fileUrl = file.getCanonicalPath();
					if (fileUrl.contains(folderUrl)) {
						result = file.toURI().toURL();
					}
				} catch (IOException e) {
					log.error("Failed to get file from Folder.", e);
				}
			}
		}
		return result;
	}

	public static URL getWebResource(String resource, ServletContext servletContext, File webFolder) {
		URL result = getFileResource(resource, webFolder);
		if (result == null) {
			try {
				result = servletContext.getResource(resource);
			} catch (MalformedURLException e) {
				log.error("Failed to get file from Web context path.", e);
			}
		}
		return result;
	}

	public static boolean isFileLocked(File file) {
		if (file.exists()) {
			try {
				Path source = file.toPath();
				Path dest = file.toPath().resolveSibling(file.getName() + ".wstest");
				Files.move(source, dest);
				Files.move(dest, source);
				return false;
			} catch (IOException e) {
				return true;
			}
		}
		return false;
	}

	public static String getContextPath(ServletContext ctx) {
		String contextPath = ctx.getContextPath();
		String contextPathExplicit = System.getProperty(Constants.REVERSE_PROXY_CONTEXT_PATH);
		if (contextPathExplicit != null) {
			return CommonUtil.toPath(contextPathExplicit);
		} else if (contextPath != null && !contextPath.equals("/") && !contextPath.equals("")) {
			return CommonUtil.toPath(contextPath);
		} else {
			return "";
		}
	}

	public static void sendHttpRedirect(HttpServletRequest req, HttpServletResponse resp, String relativeUrl) throws IOException {
		String proto = req.getHeader("X-Forwarded-Proto");
		String host = req.getHeader("X-Forwarded-Host");
		if (StringUtils.startsWithIgnoreCase(relativeUrl, "http://") || StringUtils.startsWithIgnoreCase(relativeUrl, "https://")) {
			resp.sendRedirect(relativeUrl);
		} else if (StringUtils.isNotEmpty(proto) && StringUtils.isNotEmpty(host)) {
			if (!StringUtils.startsWith(relativeUrl, "/")) {
				String requestPath = ServerUtil.getContextPath(req.getServletContext()) + CommonUtil.toPath(req.getPathInfo());
				String requestPathBase = requestPath.startsWith("/") ? requestPath : "/" + requestPath;
				requestPathBase = requestPath.substring(0, requestPath.lastIndexOf("/") + 1);
				relativeUrl = requestPathBase + relativeUrl;
			}
			resp.sendRedirect(proto + "://" + host + relativeUrl);
		} else {
			resp.sendRedirect(relativeUrl);
		}
	}

	public static String normalizeForFileName(String text) {
		return text.replaceAll("\\W+", "_");
	}

	/**
	 * Extract the bearer token from a header.
	 *
	 * @param request The request.
	 * @return The token, or null if no Authorization header was supplied.
	 */
	public static String extractBearerToken(HttpServletRequest request) {
		Enumeration<String> headers = request.getHeaders(HEADER_AUTHORIZATION);
		if (headers == null) {
			return null;
		}
		return extractBearerToken(Iterators.forEnumeration(headers));
	}

	public static String extractBearerToken(Map<String, List<String>> map) {
		if (map == null) {
			return null;
		}
		if (!map.containsKey(Constants.HTTP_ATTR_TOKEN)) {
			return null;
		}

		List<String> paramValues = map.get(Constants.HTTP_ATTR_TOKEN);

		if (paramValues == null || paramValues.isEmpty()) {
			return null;
		}

		return paramValues.get(0);
	}

	private static String extractBearerToken(Iterator<String> headers) {
		while (headers.hasNext()) { // typically there is only one (most servers enforce that)
			String value = headers.next();
			if ((value.toLowerCase().startsWith(BEARER_TYPE.toLowerCase()))) {
				String authHeaderValue = value.substring(BEARER_TYPE.length()).trim();
				int commaIndex = authHeaderValue.indexOf(',');
				if (commaIndex > 0) {
					authHeaderValue = authHeaderValue.substring(0, commaIndex);
				}
				return authHeaderValue;
			}
		}
		return null;
	}

	public static String parseTokenFromCookie(HttpServletRequest req, String cookieName) {
		if (req.getCookies() == null) {
			return null;
		}

		String proxypath = System.getProperty(Constants.REVERSE_PROXY_CONTEXT_PATH, "").replaceAll("[^A-Za-z0-9]", "_");
		cookieName += proxypath;

		String token_cookie = null;

		for (Cookie cookie : req.getCookies()) {
			if (cookieName.equals(cookie.getName())) {
				token_cookie = cookie.getValue();
				break;
			}
		}

		return token_cookie;
	}

	public static void setTokenCookie(HttpServletResponse resp, String cookieName, String token) {
		setTokenCookie(resp, cookieName, token, false);
	}

	public static void setTokenCookie(HttpServletResponse resp, String cookieName, String token, boolean clear) {
		setCookie(resp, cookieName, token, null, clear);
	}

	public static void setCookie(HttpServletResponse resp, String cookieName, String value, String path, boolean clear) {
		String proxypath = System.getProperty(Constants.REVERSE_PROXY_CONTEXT_PATH, "").replaceAll("[^A-Za-z0-9]", "_");
		boolean serverIsHttpsOnly = Boolean.getBoolean(Constants.HTTPS_ONLY);

		StringBuilder cookieHeader = new StringBuilder();
		cookieHeader.append(cookieName).append(proxypath);
		cookieHeader.append("=");
		cookieHeader.append(value);

		if (clear) {
			cookieHeader.append("; Expires=").append(EXPIRES_FORMAT.format(new Date(0)));
		}
		if (path == null) {
			cookieHeader.append("; Path=/");
		} else {
			cookieHeader.append("; Path=").append(path);
		}
		cookieHeader.append("; HttpOnly");

		if (serverIsHttpsOnly) {
			cookieHeader.append("; Secure");
			cookieHeader.append("; SameSite=").append(System.getProperty(Constants.COOKIE_SAMESITE, "NONE")
                                                            .toUpperCase());
		}

		resp.addHeader("Set-Cookie", cookieHeader.toString());
	}

	public static void writeLoginSessionToken(HttpServletResponse resp, String serializedLoginSessionClaim) {
		String loginSessionToken = JwtUtil.createLoginSessionToken(serializedLoginSessionClaim);
		ServerUtil.setTokenCookie(resp, Constants.WEBSWING_SESSION_LOGIN_SESSION_TOKEN, loginSessionToken);
	}

	public static void writeTokens(HttpServletResponse resp, String serializedWebswingClaim, boolean cookieOnly) {
		String refreshToken = JwtUtil.createRefreshToken(serializedWebswingClaim);
		String transferToken = JwtUtil.createTransferToken(serializedWebswingClaim);
		String adminConsoleLoginToken = JwtUtil.createAdminConsoleLoginToken(serializedWebswingClaim);

		ServerUtil.setTokenCookie(resp, Constants.WEBSWING_SESSION_REFRESH_TOKEN, refreshToken);
		ServerUtil.setTokenCookie(resp, Constants.WEBSWING_SESSION_TRANSFER_TOKEN, transferToken);
		ServerUtil.setTokenCookie(resp, Constants.WEBSWING_SESSION_ADMIN_CONSOLE_LOGIN_TOKEN, adminConsoleLoginToken);

		if (!cookieOnly) {
			String accessToken = JwtUtil.createAccessToken(serializedWebswingClaim);

			ObjectNode result = mapper.createObjectNode();
			result.put("accessToken", accessToken);

			resp.setContentType("application/json");
			resp.setCharacterEncoding("UTF-8");

			try {
				resp.getWriter().write(result.toString());
			} catch (IOException e) {
				log.error("Could not write token to response!", e);
			}
		}
	}

	public static void clearLoginTokenFromCookies(HttpServletResponse resp) {
		ServerUtil.setTokenCookie(resp, Constants.WEBSWING_SESSION_LOGIN_SESSION_TOKEN, "expired", true);
	}

	public static void clearTokensFromCookies(HttpServletResponse resp) {
		ServerUtil.setTokenCookie(resp, Constants.WEBSWING_SESSION_REFRESH_TOKEN, "expired", true);
		ServerUtil.setTokenCookie(resp, Constants.WEBSWING_SESSION_TRANSFER_TOKEN, "expired", true);
		ServerUtil.setTokenCookie(resp, Constants.WEBSWING_SESSION_ADMIN_CONSOLE_LOGIN_TOKEN, "expired", true);
	}

	public static void clearAdminConsoleCookie(HttpServletResponse resp) {
		ServerUtil.setTokenCookie(resp, Constants.WEBSWING_SESSION_ADMIN_CONSOLE_REFRESH_TOKEN, "expired", true);
		ServerUtil.setTokenCookie(resp, Constants.WEBSWING_SESSION_ADMIN_CONSOLE_DOWNLOAD_TOKEN, "expired", true);
	}

}