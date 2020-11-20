package org.webswing.server.api.services.swinginstance.holder;

import java.util.List;

import org.webswing.server.api.services.swinginstance.ConnectedSwingInstance;

public interface SwingInstanceHolder {

	ConnectedSwingInstance findInstanceByOwnerId(String ownerId);

	ConnectedSwingInstance findInstanceByInstanceId(String instanceId);

	List<ConnectedSwingInstance> getAllInstances();

	List<ConnectedSwingInstance> getAllClosedInstances();

	void add(ConnectedSwingInstance swingInstance);

	void remove(ConnectedSwingInstance swingInstance, boolean force);
	
	int getRunningInstacesCount();
	
	int getConnectedInstancesCount();
	
	int getClosedInstacesCount();
}
