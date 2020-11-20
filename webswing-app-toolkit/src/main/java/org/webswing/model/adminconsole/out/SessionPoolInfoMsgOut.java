package org.webswing.model.adminconsole.out;

import java.util.List;

import org.webswing.model.MsgOut;

public class SessionPoolInfoMsgOut implements MsgOut {

	private static final long serialVersionUID = 7963526114521293161L;

	private String id;
	private int maxInstances;
	private int priority;
	private List<String> connectedServers;
	private List<SessionPoolAppMsgOut> appInstances;

	public SessionPoolInfoMsgOut() {
	}

	public SessionPoolInfoMsgOut(String id, int maxInstances, int priority, List<String> connectedServers, List<SessionPoolAppMsgOut> appInstances) {
		super();
		this.id = id;
		this.maxInstances = maxInstances;
		this.priority = priority;
		this.connectedServers = connectedServers;
		this.appInstances = appInstances;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getMaxInstances() {
		return maxInstances;
	}

	public void setMaxInstances(int maxInstances) {
		this.maxInstances = maxInstances;
	}

	public List<String> getConnectedServers() {
		return connectedServers;
	}

	public void setConnectedServers(List<String> connectedServers) {
		this.connectedServers = connectedServers;
	}

	public List<SessionPoolAppMsgOut> getAppInstances() {
		return appInstances;
	}

	public void setAppInstances(List<SessionPoolAppMsgOut> appInstances) {
		this.appInstances = appInstances;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

}
