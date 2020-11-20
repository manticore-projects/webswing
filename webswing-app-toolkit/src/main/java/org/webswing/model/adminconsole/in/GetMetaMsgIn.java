package org.webswing.model.adminconsole.in;

import java.util.List;
import java.util.UUID;

import org.webswing.model.MsgIn;
import org.webswing.model.SyncMsg;

public class GetMetaMsgIn implements SyncMsg, MsgIn {

	private static final long serialVersionUID = 518679934438603445L;
	
	private String path;
	private byte[] serverConfig;
	private List<AppConfigMsgIn> appConfigs;
	private String correlationId = UUID.randomUUID().toString();

	public GetMetaMsgIn() {
	}
	
	public GetMetaMsgIn(String path, byte[] serverConfig, List<AppConfigMsgIn> appConfigs) {
		this.path = path;
		this.serverConfig = serverConfig;
		this.appConfigs = appConfigs;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public byte[] getServerConfig() {
		return serverConfig;
	}

	public void setServerConfig(byte[] serverConfig) {
		this.serverConfig = serverConfig;
	}

	public List<AppConfigMsgIn> getAppConfigs() {
		return appConfigs;
	}

	public void setAppConfigs(List<AppConfigMsgIn> appConfigs) {
		this.appConfigs = appConfigs;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}

}
