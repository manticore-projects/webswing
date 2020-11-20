package org.webswing.model.adminconsole.in;

import java.util.UUID;

import org.webswing.model.MsgIn;
import org.webswing.model.SyncMsg;

public class GetSwingSessionsMsgIn implements SyncMsg, MsgIn {

	private static final long serialVersionUID = 1693834115245508470L;

	private String path;
	private String correlationId = UUID.randomUUID().toString();

	public GetSwingSessionsMsgIn() {
	}
	
	public GetSwingSessionsMsgIn(String path) {
		super();
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
