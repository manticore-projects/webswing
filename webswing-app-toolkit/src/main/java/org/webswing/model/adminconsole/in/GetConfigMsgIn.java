package org.webswing.model.adminconsole.in;

import java.util.UUID;

import org.webswing.model.MsgIn;
import org.webswing.model.SyncMsg;

public class GetConfigMsgIn implements SyncMsg, MsgIn {

	private static final long serialVersionUID = 4827410791774249746L;
	
	private String path;
	private String correlationId = UUID.randomUUID().toString();

	public GetConfigMsgIn() {
	}
	
	public GetConfigMsgIn(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}

}
