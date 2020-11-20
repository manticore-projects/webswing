package org.webswing.server.api.services.sessionpool.loadbalance;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.webswing.server.api.services.sessionpool.ServerSessionPoolConnector;
import org.webswing.server.common.model.SecuredPathConfig;

public class RoundRobinLoadBalanceResolver implements LoadBalanceResolver {

	private Deque<ServerSessionPoolConnector> sessionPools = new ConcurrentLinkedDeque<>();
	
	@Override
	public ServerSessionPoolConnector resolveLoadBalance(String path, SecuredPathConfig config) {
		ServerSessionPoolConnector firstPool = null;
		
		synchronized (sessionPools) {
			ServerSessionPoolConnector sp = sessionPools.pollFirst();
			if (sp == null) {
				return null;
			}
			
			sessionPools.addLast(sp);
			
			while (sp != firstPool) {
				// break condition so that we do only one loop through all session pools
				
				if (firstPool == null) {
					firstPool = sp;
				}
				
				if (sp.accepts(path, config)) {
					return sp;
				}
				
				sp = sessionPools.pollFirst();
				sessionPools.addLast(sp);
			}
		}
		
		return null;
	}
	
	@Override
	public void registerSessionPool(ServerSessionPoolConnector sessionPool) {
		synchronized (sessionPools) {
			this.sessionPools.addLast(sessionPool);
		}
	}
	
	@Override
	public void unregisterSessionPool(ServerSessionPoolConnector sessionPool) {
		synchronized (sessionPools) {
			this.sessionPools.remove(sessionPool);
		}
	}

}
