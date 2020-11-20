package org.webswing.model.adminconsole.out;

import java.util.ArrayList;
import java.util.List;

import org.webswing.model.MsgOut;

public class StatEntryMsgOut implements MsgOut {

	private static final long serialVersionUID = 1961576836793924844L;

	private String metric;
	private List<MetricMsgOut> stats = new ArrayList<>();

	public StatEntryMsgOut() {
	}
	
	public StatEntryMsgOut(String metric, List<MetricMsgOut> stats) {
		this.metric = metric;
		this.stats = stats;
	}

	public String getMetric() {
		return metric;
	}

	public void setMetric(String metric) {
		this.metric = metric;
	}

	public List<MetricMsgOut> getStats() {
		return stats;
	}
	
	public void setStats(List<MetricMsgOut> stats) {
		this.stats = stats;
	}
	
}
