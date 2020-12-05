package org.webswing.server.api.services.websocket.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCode;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.PongMessage;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.Constants;
import org.webswing.model.Msg;
import org.webswing.model.app.in.ServerToAppFrameMsgIn;
import org.webswing.model.app.out.AppHandshakeMsgOut;
import org.webswing.model.app.out.AppToServerFrameMsgOut;
import org.webswing.server.api.services.sessionpool.SessionPoolHolderService;
import org.webswing.server.api.services.swinginstance.ConnectedSwingInstance;
import org.webswing.server.api.services.websocket.ApplicationWebSocketConnection;
import org.webswing.server.api.services.websocket.util.ApplicationWebSocketConfigurator;
import org.webswing.server.common.util.JwtUtil;
import org.webswing.server.common.util.ProtoMapper;

import com.google.inject.Inject;

@ServerEndpoint(value = "/{appPath}/async/app-bin", configurator = ApplicationWebSocketConfigurator.class)
public class ApplicationWebSocketConnectionImpl extends AbstractWebSocketConnection implements ApplicationWebSocketConnection {
	
	private static final Logger log = LoggerFactory.getLogger(ApplicationWebSocketConnectionImpl.class);

	private ProtoMapper protoMapper = new ProtoMapper(ProtoMapper.PROTO_PACKAGE_SERVER_APP_FRAME);
	private final SessionPoolHolderService sessionPoolHolderService;

	private String instanceId;
	private String sessionPoolId;
	
	private ConnectedSwingInstance instance;
	private boolean secured;
	private Timer pingTimer = new Timer(true);
	
	@Inject
	public ApplicationWebSocketConnectionImpl(SessionPoolHolderService sessionPoolHolderService) {
		this.sessionPoolHolderService = sessionPoolHolderService;
	}

	@OnOpen
	public void onOpen(Session session, EndpointConfig config, @PathParam("appPath") String appPath) {
		super.onOpen(session, config);
		
		this.instanceId = (String) config.getUserProperties().get(ApplicationWebSocketConfigurator.ATTR_INSTANCE_ID);
		this.sessionPoolId = (String) config.getUserProperties().get(ApplicationWebSocketConfigurator.ATTR_SESSION_POOL_ID);
		boolean reconnect = Boolean.parseBoolean((String) config.getUserProperties().get(ApplicationWebSocketConfigurator.ATTR_RECONNECT));
		
		if (!sessionPoolHolderService.connectApplication(this, reconnect)) {
			log.error("Could not find a connected instance [" + instanceId + "] from session pool [" + sessionPoolId + "] on this server, it has probably been restarted. Wait for reconnect from browser. Disconnecting...");
			disconnect(CloseCodes.CANNOT_ACCEPT, "Connected instance not found!");
			return;
		}
		
		log.info("Application connected with instanceId [" + instanceId + "] and sessionPoolId [" + sessionPoolId + "].");
		
		pingTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (session != null) {
					synchronized (session) {
						try {
							session.getBasicRemote().sendPing(ByteBuffer.wrap(Constants.WEBSOCKET_PING_PONG_CONTENT.getBytes(StandardCharsets.UTF_8)));
						} catch (IllegalArgumentException | IOException e) {
							log.warn("Could not send ping message for session [" + session.getId() + "]", e);
						}
					}
				}
			}
		}, Constants.WEBSOCKET_PING_PONG_INTERVAL, Constants.WEBSOCKET_PING_PONG_INTERVAL);
	}
	
	@OnMessage
	public void onMessage(Session session, byte[] bytes, boolean last) {
		try {
			Pair<Msg, Integer> frameWithLength = super.getCompleteMessage(bytes, last);
			if (frameWithLength == null) {
				// incomplete
				return;
			}
			
			AppToServerFrameMsgOut msgOut = (AppToServerFrameMsgOut) frameWithLength.getKey();
		
			if (msgOut.getHandshake() != null) {
				AppHandshakeMsgOut handshake = msgOut.getHandshake();
				
				try {
					if (!JwtUtil.validateHandshakeToken(handshake.getSecretMessage())) {
						throw new IllegalArgumentException("Invalid token [" + handshake.getSecretMessage() + "] received during handshake!");
					}
					secured = true;
				} catch (Exception e1) {
					log.error("Could not validate handshake secret message! Disconnecting...", e1);
					disconnect(CloseCodes.CANNOT_ACCEPT, "Connection not secured!");
					return;
				}
			} else if (!secured) {
				// we must get handshake first, otherwise we disconnect
				disconnect(CloseCodes.CANNOT_ACCEPT, "Connection not secured!");
				return;
			} else {
				if (instance == null) {
					// this should not happen, since we disconnect if we cannot get a connected instance in onOpen
					log.warn("Could not handle message from application, instance not initialized for instanceId [" + instanceId + "]!");
					return;
				}
				
				instance.handleAppMessage(msgOut);
			}
		} catch (IOException e) {
			log.error("Could not decode proto message from app [" + instanceId + "]!", e);
		}
	}
	
	@OnMessage
	public void onPong(Session session, PongMessage pongMessage) {
		super.onPong(session, pongMessage, log);
	}
	
	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		if (session != null) {
			log.info("Websocket closed to app, instance [" + instanceId + "]" 
					+ (closeReason != null ? ", close code [" + closeReason.getCloseCode().getCode() + "], reason [" + closeReason.getReasonPhrase() + "]!" : ""));
		}
		if (instance != null) {
			instance.applicationDisconnected(Constants.APP_WEBSOCKET_CLOSE_REASON_RECONNECT.equals(closeReason.getReasonPhrase()));
		}
		pingTimer.cancel();
	}
	
	@OnError
	public void onError(Session session, Throwable t) {
		if (session != null) {
			log.error("Websocket error in app connection, instance [" + instanceId + "]!", t);
		} else {
			log.error("Websocket error in app connection, no session!", t);
		}
	}

	@Override
	public void sendMessage(ServerToAppFrameMsgIn msgIn) {
		sendMessage(msgIn, true);
	}
	
	@Override
	public void sendMessage(ServerToAppFrameMsgIn msgIn, boolean logStats) {
		try {
			super.sendMessage(protoMapper.encodeProto(msgIn));
		} catch (IOException e) {
			log.error("Error sending msg to server, session [" + session.getId() + "]!", e);
		}
	}
	
	@Override
	protected Msg decodeIncomingMessage(byte[] bytes) throws IOException {
		return protoMapper.decodeProto(bytes, AppToServerFrameMsgOut.class);
	}
	
	@Override
	public void instanceConnected(ConnectedSwingInstance instance) {
		this.instance = instance;
	}
	
	private void disconnect(CloseCode closeCode, String reason) {
		if (session != null && session.isOpen()) {
			try {
				session.close(new CloseReason(closeCode, reason));
			} catch (IOException e) {
				log.error("Failed to destroy websocket app connection instance [" + instanceId + "]!", e);
			}
		}
		pingTimer.cancel();
	}
	
	@Override
	public void disconnect(String reason) {
		disconnect(CloseCodes.NORMAL_CLOSURE, reason);
	}
	
	@Override
	public String getInstanceId() {
		return instanceId;
	}
	
	@Override
	public String getSessionPoolId() {
		return sessionPoolId;
	}
	
}