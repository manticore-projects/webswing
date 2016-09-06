package org.webswing.server.common.model.admin;

import java.io.Serializable;
import java.util.Map;

import org.webswing.server.common.model.SecuredPathConfig;

public class ApplicationInfo implements Serializable {
	private static final long serialVersionUID = 7847135426884331742L;
	private String path;
	private String url;
	private byte[] icon;
	private String name;
	private InstanceManagerStatus status;
	private SecuredPathConfig config;
	private int connectedInstances;
	private int runningInstances;
	private int maxRunningInstances;
	private int finishedInstances;
	private Map<String, Map<Long, Number>> stats;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getRunningInstances() {
		return runningInstances;
	}

	public int getConnectedInstances() {
		return connectedInstances;
	}

	public void setConnectedInstances(int connectedInstances) {
		this.connectedInstances = connectedInstances;
	}

	public void setRunningInstances(int runningInstances) {
		this.runningInstances = runningInstances;
	}

	public int getFinishedInstances() {
		return finishedInstances;
	}

	public void setFinishedInstances(int finishedInstances) {
		this.finishedInstances = finishedInstances;
	}

	public int getMaxRunningInstances() {
		return maxRunningInstances;
	}

	public void setMaxRunningInstances(int maxRunningInstances) {
		this.maxRunningInstances = maxRunningInstances;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public byte[] getIcon() {
		return icon;
	}

	public void setIcon(byte[] icon) {
		this.icon = icon;
	}

	public SecuredPathConfig getConfig() {
		return config;
	}

	public void setConfig(SecuredPathConfig config) {
		this.config = config;
	}

	public InstanceManagerStatus getStatus() {
		return status;
	}

	public void setStatus(InstanceManagerStatus status) {
		this.status = status;
	}

	public Map<String, Map<Long, Number>> getStats() {
		return stats;
	}

	public void setStats(Map<String, Map<Long, Number>> stats) {
		this.stats = stats;
	}

}