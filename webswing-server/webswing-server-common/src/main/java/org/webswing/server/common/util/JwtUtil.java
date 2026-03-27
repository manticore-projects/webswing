package org.webswing.server.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.Constants;
import org.webswing.server.common.model.security.AbstractWebswingUserProto;
import org.webswing.server.common.model.security.MapProto;
import org.webswing.server.common.model.security.WebswingAction;
import org.webswing.server.common.model.security.WebswingLoginSessionTokenClaimProto;
import org.webswing.server.common.model.security.WebswingTokenClaimProto;
import org.webswing.server.common.service.security.AbstractWebswingUser;
import org.webswing.server.common.service.security.WebswingLoginSessionTokenClaim;
import org.webswing.server.common.service.security.WebswingTokenClaim;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JwtUtil {

	private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

	private static final byte[] signingKey = System.getProperty(Constants.WEBSWING_CONNECTION_SECRET).getBytes(StandardCharsets.UTF_8);
	private static final String encryptionAlg = "AES/CBC/PKCS5Padding";
	private static final int IV_LENGTH = 16;
	private static final String encryptionKeySpec = "AES";
	
	private static boolean usegGzip = Boolean.valueOf(System.getProperty(Constants.JWT_SERIALIZATION_USE_GZIP, Constants.JWT_SERIALIZATION_USE_GZIP_DEFAULT));
	private static boolean useProto = Boolean.valueOf(System.getProperty(Constants.JWT_SERIALIZATION_USE_PROTO, Constants.JWT_SERIALIZATION_USE_PROTO_DEFAULT));
	private static boolean useEncryption = Boolean.valueOf(System.getProperty(Constants.jWT_SERIALIZATION_USE_ENCRYPTION, Constants.JWT_SERIALIZATION_USE_ENCRYPTION_DEFAULT));
	
	private static ProtoMapper protoMapper = new ProtoMapper(ProtoMapper.PROTO_PACKAGE_JWT, ProtoMapper.PROTO_PACKAGE_JWT);
	private static ObjectMapper mapper = new ObjectMapper();
	
	private static SecretKey secretKey;
	
	static {
		try {
			secretKey = new SecretKeySpec(Arrays.copyOfRange(signingKey, 0, 32), encryptionKeySpec);
		} catch (Exception e) {
			log.error("Failed to initialize JWT encryption!", e);
		}
	}

	public static boolean validateHandshakeToken(String token) {
		return validateToken(token, Constants.JWT_SUBJECT_HANDSHAKE);
	}

	private static boolean validateToken(String token, String subjectExpected) {
		try {
			createTokenParser(subjectExpected).parse(token);
			return true;
		} catch (Exception e) {
			log.debug("Could not validate JWT token [" + token + "]!", e);
		}
		return false;
	}

	public static String createHandshakeToken() {
		return createTokenBuilder(Long.getLong(Constants.JWT_HANDSHAKE_TOKEN_EXPIRATION_MILLIS, Constants.JWT_HANDSHAKE_TOKEN_EXPIRATION_MILLIS_DEFAULT), Constants.JWT_SUBJECT_HANDSHAKE)
				.compact();
	}

	public static String createAccessToken(String webswingClaim) {
		return createTokenBuilder(Long.getLong(Constants.JWT_ACCESS_TOKEN_EXPIRATION_MILLIS, Constants.JWT_ACCESS_TOKEN_EXPIRATION_MILLIS_DEFAULT), Constants.JWT_SUBJECT_ACCESS)
				.claim(Constants.JWT_CLAIM_WEBSWING, webswingClaim)
				.compact();
	}

	public static String createRefreshToken(String webswingClaim) {
		return createTokenBuilder(Long.getLong(Constants.JWT_REFRESH_TOKEN_EXPIRATION_MILLIS, Constants.JWT_REFRESH_TOKEN_EXPIRATION_MILLIS_DEFAULT), Constants.JWT_SUBJECT_REFRESH)
				.claim(Constants.JWT_CLAIM_WEBSWING, webswingClaim)
				.compact();
	}
	
	public static String createLoginSessionToken(String webswingLoginSessionClaim) {
		return createTokenBuilder(Long.getLong(Constants.JWT_LOGIN_SESSION_TOKEN_EXPIRATION_MILLIS, Constants.JWT_LOGIN_SESSION_TOKEN_EXPIRATION_MILLIS_DEFAULT), Constants.JWT_SUBJECT_LOGIN_SESSION)
				.claim(Constants.JWT_CLAIM_WEBSWING_LOGIN_SESSION, webswingLoginSessionClaim)
				.compact();
	}
	
	public static String createTransferToken(String webswingClaim) {
		return createTokenBuilder(Long.getLong(Constants.JWT_TRANSFER_TOKEN_EXPIRATION_MILLIS, Constants.JWT_TRANSFER_TOKEN_EXPIRATION_MILLIS_DEFAULT), Constants.JWT_SUBJECT_TRANSFER)
				.claim(Constants.JWT_CLAIM_WEBSWING, webswingClaim)
				.compact();
	}
	
	public static String createAdminConsoleLoginToken(String webswingClaim) {
		return createTokenBuilder(Long.getLong(Constants.JWT_ADMIN_CONSOLE_LOGIN_TOKEN_EXPIRATION_MILLIS, Constants.JWT_ADMIN_CONSOLE_LOGIN_TOKEN_EXPIRATION_MILLIS_DEFAULT), Constants.JWT_SUBJECT_ADMIN_CONSOLE_LOGIN)
				.claim(Constants.JWT_CLAIM_WEBSWING, webswingClaim)
				.compact();
	}
	
	public static String createAdminConsoleAccessToken(String webswingClaim) {
		return createTokenBuilder(Long.getLong(Constants.JWT_ADMIN_CONSOLE_ACCESS_TOKEN_EXPIRATION_MILLIS, Constants.JWT_ADMIN_CONSOLE_ACCESS_TOKEN_EXPIRATION_MILLIS_DEFAULT), Constants.JWT_SUBJECT_ADMIN_CONSOLE_ACCESS)
				.claim(Constants.JWT_CLAIM_WEBSWING, webswingClaim)
				.compact();
	}
	
	public static String createAdminConsoleRefreshToken(String webswingClaim) {
		return createTokenBuilder(Long.getLong(Constants.JWT_ADMIN_CONSOLE_REFRESH_TOKEN_EXPIRATION_MILLIS, Constants.JWT_ADMIN_CONSOLE_REFRESH_TOKEN_EXPIRATION_MILLIS_DEFAULT), Constants.JWT_SUBJECT_ADMIN_CONSOLE_REFRESH)
				.claim(Constants.JWT_CLAIM_WEBSWING, webswingClaim)
				.compact();
	}
	
	public static String createAdminConsoleDownloadToken(String webswingClaim) {
		return createTokenBuilder(Long.getLong(Constants.JWT_ADMIN_CONSOLE_DOWNLOAD_TOKEN_EXPIRATION_MILLIS, Constants.JWT_ADMIN_CONSOLE_DOWNLOAD_TOKEN_EXPIRATION_MILLIS_DEFAULT), Constants.JWT_SUBJECT_ADMIN_CONSOLE_DOWNLOAD)
				.claim(Constants.JWT_CLAIM_WEBSWING, webswingClaim)
				.compact();
	}

	private static JwtBuilder createTokenBuilder(long expirationMillis, String subject) {
		return Jwts.builder()
				.signWith(Keys.hmacShaKeyFor(signingKey))
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
				.setSubject(subject)
				.setId(UUID.randomUUID().toString());
	}

	public static Jws<Claims> parseAccessTokenClaims(String token) {
		return parseTokenClaims(token, Constants.JWT_SUBJECT_ACCESS);
	}

	public static Jws<Claims> parseRefreshTokenClaims(String token) {
		return parseTokenClaims(token, Constants.JWT_SUBJECT_REFRESH);
	}
	
	public static Jws<Claims> parseLoginSessionTokenClaims(String token) {
		return parseTokenClaims(token, Constants.JWT_SUBJECT_LOGIN_SESSION, Constants.JWT_CLAIM_WEBSWING_LOGIN_SESSION);
	}
	
	public static Jws<Claims> parseTransferTokenClaims(String token) {
		return parseTokenClaims(token, Constants.JWT_SUBJECT_TRANSFER);
	}
	
	public static Jws<Claims> parseAdminConsoleLoginTokenClaims(String token) {
		return parseTokenClaims(token, Constants.JWT_SUBJECT_ADMIN_CONSOLE_LOGIN);
	}
	
	public static Jws<Claims> parseAdminConsoleAccessTokenClaims(String token) {
		return parseTokenClaims(token, Constants.JWT_SUBJECT_ADMIN_CONSOLE_ACCESS);
	}
	
	public static Jws<Claims> parseAdminConsoleRefreshTokenClaims(String token) {
		return parseTokenClaims(token, Constants.JWT_SUBJECT_ADMIN_CONSOLE_REFRESH);
	}
	
	public static Jws<Claims> parseAdminConsoleDownloadTokenClaims(String token) {
		return parseTokenClaims(token, Constants.JWT_SUBJECT_ADMIN_CONSOLE_DOWNLOAD);
	}

	private static Jws<Claims> parseTokenClaims(String token, String subjectExpected) {
		return parseTokenClaims(token, subjectExpected, Constants.JWT_CLAIM_WEBSWING);
	}
	
	private static Jws<Claims> parseTokenClaims(String token, String subjectExpected, String claimId) {
		try {
			Jws<Claims> claims = createTokenParser(subjectExpected).parseClaimsJws(token);
			if (claims.getBody().containsKey(claimId)) {
				claims.getBody().put(claimId, claims.getBody().get(claimId, String.class));
			}
			return claims;
		} catch (Exception e) {
			log.debug("Could not validate and parse claims from JWT token [" + token + "]!", e);
		}
		return null;
	}

	private static JwtParser createTokenParser(String subjectExpected) {
		return Jwts.parserBuilder()
				.setSigningKey(signingKey)
				.setAllowedClockSkewSeconds(Long.getLong(Constants.JWT_CLOCK_SKEW_SECONDS, Constants.JWT_CLOCK_SKEW_SECONDS_DEFAULT))
				.requireSubject(subjectExpected)
				.build();
	}

	public static String serializeWebswingClaim(WebswingTokenClaim webswingClaim) {
		// serialize (json/protobuf)
		// gzip
		// encrypt
		
		byte[] serialized = null;
		
		if (useProto) {
			try {
				serialized = protoMapper.encodeProto(new WebswingTokenClaimProto(webswingClaim));
			} catch (IOException e) {
				log.error("Failed to serialize user map!", e);
			}
		} else {
			try {
				serialized = mapper.writeValueAsBytes(webswingClaim);
			} catch (JsonProcessingException e) {
				log.error("Failed to serialize user map!", e);
			}
		}
		
		return compressAndEncryptWebswingClaim(serialized);
	}
	
	public static String serializeWebswingLoginSessionClaim(WebswingLoginSessionTokenClaim loginSessionClaim) {
		// serialize (json/protobuf)
		// gzip
		// encrypt
		
		byte[] serialized = null;
		
		if (useProto) {
			try {
				serialized = protoMapper.encodeProto(new WebswingLoginSessionTokenClaimProto(loginSessionClaim));
			} catch (IOException e) {
				log.error("Failed to serialize user map!", e);
			}
		} else {
			try {
				serialized = mapper.writeValueAsBytes(loginSessionClaim);
			} catch (JsonProcessingException e) {
				log.error("Failed to serialize user map!", e);
			}
		}
		
		return compressAndEncryptWebswingClaim(serialized);
	}
	
	private static String compressAndEncryptWebswingClaim(byte[] serializedWebswingClaim) {
		byte[] claimBytes = serializedWebswingClaim;

		if (usegGzip) {
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); GZIPOutputStream gzip = new GZIPOutputStream(baos)) {
				gzip.write(claimBytes);
				gzip.finish();
				claimBytes = baos.toByteArray();
			} catch (IOException e) {
				log.error("Could not gzip token claim!", e);
			}
		}
		
		if (useEncryption) {
			if (secretKey == null) {
				return null;
			}
			try {
				byte[] iv = new byte[IV_LENGTH];
				new SecureRandom().nextBytes(iv);
				IvParameterSpec ivSpec = new IvParameterSpec(iv);
				Cipher cipher = Cipher.getInstance(encryptionAlg);
				cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
				byte[] encrypted = cipher.doFinal(claimBytes);
				// Prepend IV to encrypted data
				byte[] combined = new byte[iv.length + encrypted.length];
				System.arraycopy(iv, 0, combined, 0, iv.length);
				System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
				claimBytes = Base64.getUrlEncoder().encode(combined);
			} catch (Exception e) {
				log.error("Failed to encrypt user map for JWT token!", e);
			}
		}
		
		return new String(claimBytes, StandardCharsets.UTF_8);
	}
	
	public static WebswingTokenClaim deserializeWebswingClaim(String webswingClaim) throws IOException {
		// dencrypt
		// ungzip
		// deserialize (json/protobuf)
		
		byte[] serialized = decryptAndDecompressWebswingClaim(webswingClaim);
		
		if (useProto) {
			WebswingTokenClaimProto claimProto = protoMapper.decodeProto(serialized, WebswingTokenClaimProto.class);
			
			WebswingTokenClaim claim = new WebswingTokenClaim();
			claim.setHost(claimProto.getHost());
			if (claimProto.getAttributes() != null) {
				Map<String, Object> attributes = new HashMap<>();
				for (MapProto entry : claimProto.getAttributes()) {
					try {
						attributes.put(entry.getKey(), mapper.readValue(entry.getValue(), Object.class));
					} catch (Exception e) {
						log.error("Could not deserialize attribute [" + entry.getKey() + "]!", e);
					}
				}
				claim.setAttributes(attributes);
			}
			if (claimProto.getUserMap() != null) {
				Map<String, AbstractWebswingUser> userMap = new HashMap<>();
				for (AbstractWebswingUserProto user : claimProto.getUserMap()) {
					AbstractWebswingUser awu = new AbstractWebswingUser();
					awu.setUserId(user.getUserId());
					awu.setRoles(user.getRoles());
					
					List<String> permissions = new ArrayList<>();
					if (user.getPermissions() != null) {
						permissions.addAll(permissions);
					}
					if (user.getWebswingActionPermissions() != null) {
						for (WebswingAction wa : user.getWebswingActionPermissions()) {
							permissions.add(wa.name());
						}
					}
					awu.setPermissions(permissions);
					if (user.getUserAttributes() != null) {
						Map<String, Serializable> userAttributes = new HashMap<>();
						for (MapProto entry : user.getUserAttributes()) {
							try {
								userAttributes.put(entry.getKey(), mapper.readValue(entry.getValue(), Serializable.class));
							} catch (Exception e) {
								log.error("Could not deserialize attribute [" + entry.getKey() + "]!", e);
							}
						}
						awu.setUserAttributes(userAttributes);
					}
					
					userMap.put(user.getSecuredPath(), awu);
				}
				claim.setUserMap(userMap);
			}
			
			return claim;
		} else {
			try {
				return mapper.readValue(serialized, WebswingTokenClaim.class);
			} catch (IOException e) {
				log.error("Failed to deserialize user map!", e);
				throw e;
			}
		}
	}
	
	public static WebswingLoginSessionTokenClaim deserializeWebswingLoginSessionClaim(String loginSessionClaim) throws IOException {
		// dencrypt
		// ungzip
		// deserialize (json/protobuf)
		
		byte[] serialized = decryptAndDecompressWebswingClaim(loginSessionClaim);
		
		if (useProto) {
			WebswingLoginSessionTokenClaimProto claimProto = protoMapper.decodeProto(serialized, WebswingLoginSessionTokenClaimProto.class);
			
			WebswingLoginSessionTokenClaim claim = new WebswingLoginSessionTokenClaim();
			if (claimProto.getAttributes() != null) {
				Map<String, Object> attributes = new HashMap<>();
				for (MapProto entry : claimProto.getAttributes()) {
					try {
						attributes.put(entry.getKey(), mapper.readValue(entry.getValue(), Object.class));
					} catch (Exception e) {
						log.error("Could not deserialize attribute [" + entry.getKey() + "]!", e);
					}
				}
				claim.setAttributes(attributes);
			}
			
			return claim;
		} else {
			try {
				return mapper.readValue(serialized, WebswingLoginSessionTokenClaim.class);
			} catch (IOException e) {
				log.error("Failed to deserialize user map!", e);
				throw e;
			}
		}
	}
	
	private static byte[] decryptAndDecompressWebswingClaim(String serializedWebswingClaim) {
		byte[] claimBytes = serializedWebswingClaim.getBytes(StandardCharsets.UTF_8);
		
		if (useEncryption) {
			if (secretKey == null) {
				return null;
			}

			try {
				byte[] decoded = Base64.getUrlDecoder().decode(claimBytes);
				// Extract IV (first 16 bytes)
				byte[] iv = Arrays.copyOfRange(decoded, 0, IV_LENGTH);
				byte[] encrypted = Arrays.copyOfRange(decoded, IV_LENGTH, decoded.length);
				IvParameterSpec ivSpec = new IvParameterSpec(iv);
				Cipher cipher = Cipher.getInstance(encryptionAlg);
				cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
				claimBytes = cipher.doFinal(encrypted);
			} catch (Exception e) {
				log.error("Failed to decrypt user map for JWT token!", e);
			}
		}
		
		if (usegGzip) {
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(claimBytes))) {
				IOUtils.copy(gzip, baos);
				claimBytes = baos.toByteArray();
			} catch (IOException e) {
				log.error("Could not un-gzip token claim!", e);
			}
		}
		
		return claimBytes;
	}
	
}