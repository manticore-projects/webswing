package org.webswing.sessionpool.api.service.swingprocess;

import java.util.List;

import org.webswing.server.common.service.swingprocess.ProcessStartupParams;
import org.webswing.sessionpool.api.base.SessionPoolService;

public interface SwingProcessService extends SessionPoolService {

	SwingProcess startProcess(ProcessStartupParams startupParams) throws Exception;

	void kill(String instanceId, int delayMs);
	
	void killAll(String path);

	SwingProcess getByInstanceId(String instanceId);
	
	List<SwingProcess> getAll();

	void closeProcess(String instanceId);
	
}
