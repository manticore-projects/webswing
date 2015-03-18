package org.webswing.model.admin.s2c;

import java.util.Date;

import org.webswing.model.Msg;

public class SwingSessionMsg implements Msg {

	private static final long serialVersionUID = 147477596803123012L;
	private String id;
	private String user;
	private String application;
	private Date startedAt;
	private Date endedAt;
	private Boolean connected;
	private Date disconnectedSince;
	private SwingJvmStatsMsg state;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public Date getStartedAt() {
		return startedAt;
	}

	public Boolean getConnected() {
		return connected;
	}

	public void setConnected(Boolean connected) {
		this.connected = connected;
	}

	public void setStartedAt(Date startedAt) {
		this.startedAt = startedAt;
	}

	public Date getDisconnectedSince() {
		return disconnectedSince;
	}

	public void setDisconnectedSince(Date disconnectedSince) {
		this.disconnectedSince = disconnectedSince;
	}

	public SwingJvmStatsMsg getState() {
		return state;
	}

	public void setState(SwingJvmStatsMsg state) {
		this.state = state;
	}

	public Date getEndedAt() {
		return endedAt;
	}

	public void setEndedAt(Date endedAt) {
		this.endedAt = endedAt;
	}

}