package org.webswing.model.adminconsole.out;

import java.util.ArrayList;
import java.util.List;

import org.webswing.model.MsgOut;

public class SummaryWarningMsgOut implements MsgOut {

	private static final long serialVersionUID = 7263123944623475517L;

	private String instanceId;
	private List<String> warnings = new ArrayList<>();
	
	public String getInstanceId() {
		return instanceId;
	}
	
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	
	public List<String> getWarnings() {
		return warnings;
	}
	
	public void setWarnings(List<String> warnings) {
		this.warnings = warnings;
	}
	
}
