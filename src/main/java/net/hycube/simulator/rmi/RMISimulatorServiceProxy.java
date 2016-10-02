package net.hycube.simulator.rmi;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;

import net.hycube.core.NodeId;
import net.hycube.core.NodePointer;
import net.hycube.dht.HyCubeResource;
import net.hycube.dht.HyCubeResourceDescriptor;
import net.hycube.environment.NodeProperties;
import net.hycube.simulator.SimulatorServiceException;
import net.hycube.simulator.SimulatorServiceProxy;
import net.hycube.simulator.SimulatorServiceProxyException;
import net.hycube.simulator.stat.MessageStat;


@SuppressWarnings("deprecation")
public class RMISimulatorServiceProxy implements SimulatorServiceProxy {

	protected RMIRemoteSimulatorService remoteSimService;
	protected boolean discarded;
	
	
	
	public RMISimulatorServiceProxy(String url) throws SimulatorServiceProxyException {

		//initialize remoteSeimService
		try {
			remoteSimService = (RMIRemoteSimulatorService) Naming.lookup(url);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Invalid URL specified.", e);
		} catch (NotBoundException e) {
			throw new SimulatorServiceProxyException("An exception has been thrown while trying to establish connection with the remote simulator.", e);
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("An exception has been thrown while trying to establish connection with the remote simulator.", e);
		} 
		
		discarded = false;
		
	}
	
	
	
	
	public static void setupRMISecurityManager() {
		System.setSecurityManager(new RMISecurityManager());
	}
	
	
	
	
	@Override
	public void establishConnection(String simId, String simConnectionUrl) throws SimulatorServiceException, SimulatorServiceProxyException {
		
		try {
			this.remoteSimService.establishConnection(simId, simConnectionUrl);
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
		
	}
	
	
	@Override
	public void closeConnection(String simId) throws SimulatorServiceException, SimulatorServiceProxyException {
		
		try {
			this.remoteSimService.removeConnection(simId);
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
		
	}
	
	
	
	@Override
	public void readProperties(String propertiesFile) throws SimulatorServiceException, SimulatorServiceProxyException {
		try {
			this.remoteSimService.readProperties(propertiesFile);
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
	}
	
	@Override
	public void readPropertiesWithDefaultValues(String defaultPropertiesFile, String propertiesFile) throws SimulatorServiceException, SimulatorServiceProxyException {
		try {
			this.remoteSimService.readPropertiesWithDefaultValues(defaultPropertiesFile, propertiesFile);
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
	}
	
	@Override
	public NodeProperties getNodeProperties() throws SimulatorServiceException, SimulatorServiceProxyException {
		try {
			return this.remoteSimService.getNodeProperties();
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
	}
	
	@Override
	public void setNodeProperties(NodeProperties properties) throws SimulatorServiceException, SimulatorServiceProxyException {
		try {
			this.remoteSimService.setNodeProperties(properties);
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
	}
	
	
	
	
	@Override
	public void join(int simNodeId, NodeId nodeId, String networkAddress,
			String bootstrapNodeAddress) throws SimulatorServiceException, SimulatorServiceProxyException {
		
		try {
			this.remoteSimService.join(simNodeId, nodeId, networkAddress, bootstrapNodeAddress);
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
		
	}

	@Override
	public void leave(int simNodeId) throws SimulatorServiceException, SimulatorServiceProxyException {
		
		try {
			this.remoteSimService.leave(simNodeId);
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
		
	}

	@Override
	public void discardNode(int simNodeId) throws SimulatorServiceException, SimulatorServiceProxyException {
		
		try {
			this.remoteSimService.discardNode(simNodeId);
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
		
	}

	@Override
	public void recoverNode(int simNodeId) throws SimulatorServiceException, SimulatorServiceProxyException {
		
		try {
			this.remoteSimService.recoverNode(simNodeId);
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
		
	}

	@Override
	public void recoverNodeNS(int simNodeId) throws SimulatorServiceException, SimulatorServiceProxyException {
		
		try {
			this.remoteSimService.recoverNodeNS(simNodeId);
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
		
	}

	@Override
	public void recoverAllNodes() throws SimulatorServiceException, SimulatorServiceProxyException {
		
		try {
			this.remoteSimService.recoverAllNodes();
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
		
	}

	@Override
	public void recoverAllNodesNS() throws SimulatorServiceException, SimulatorServiceProxyException {
		
		try {
			this.remoteSimService.recoverAllNodesNS();
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
		
	}

	@Override
	public void routeMessageAsync(int simNodeId, NodeId destinationNode)
			throws SimulatorServiceException, SimulatorServiceProxyException {
		
		try {
			this.remoteSimService.routeMessageAsync(simNodeId, destinationNode);
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
		
	}

	@Override
	public boolean routeMessageSync(int simNodeId, NodeId destinationNode)
			throws SimulatorServiceException, SimulatorServiceProxyException {
		
		try {
			return this.remoteSimService.routeMessageSync(simNodeId, destinationNode);
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
		
	}

	@Override
	public int getMessagesSentNum() throws SimulatorServiceProxyException {

		try {
			return this.remoteSimService.getMessagesSentNum();
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
		
	}

	@Override
	public int getMessagesReceivedNum() throws SimulatorServiceProxyException {
		
		try {
			return this.remoteSimService.getMessagesReceivedNum();
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
		
	}

	@Override
	public int getMessagesDeliveredNum() throws SimulatorServiceProxyException {
		
		try {
			return this.remoteSimService.getMessagesDeliveredNum();
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
		
	}

	@Override
	public int getMessagesLostNum() throws SimulatorServiceProxyException {
		
		try {
			return this.remoteSimService.getMessagesLostNum();
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
		
	}

	
	
	@Override
	public MessageStat getMessageStatistics() throws SimulatorServiceProxyException {
		
		try {
			return this.remoteSimService.getMessageStatistics();
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
		
	}
	
	
	
	@Override
	public NodePointer lookup(int simNodeId, NodeId lookupNodeId)
			throws SimulatorServiceException, SimulatorServiceProxyException {
	
		try {
			return this.remoteSimService.lookup(simNodeId, lookupNodeId);
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
		
	}
	
	@Override
	public NodePointer lookup(int simNodeId, NodeId lookupNodeId, Object[] parameters)
			throws SimulatorServiceException, SimulatorServiceProxyException {
	
		try {
			return this.remoteSimService.lookup(simNodeId, lookupNodeId, parameters);
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
		
	}

	@Override
	public NodePointer[] search(int simNodeId, NodeId searchNodeId, short k,
			boolean ignoreTargetNode) throws SimulatorServiceException, SimulatorServiceProxyException {
		
		try {
			return this.remoteSimService.search(simNodeId, searchNodeId, k, ignoreTargetNode);
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
				
	}
	
	@Override
	public NodePointer[] search(int simNodeId, NodeId searchNodeId, short k,
			boolean ignoreTargetNode, Object[] parameters) 
					throws SimulatorServiceException, SimulatorServiceProxyException {
		
		try {
			return this.remoteSimService.search(simNodeId, searchNodeId, k, ignoreTargetNode, parameters);
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
				
	}
	
	
	
	
	
	@Override
	public void startBackgroundProcess(int simNodeId, String backgroundProcessKey) throws SimulatorServiceException, SimulatorServiceProxyException {
		try {
			this.remoteSimService.startBackgroundProcess(simNodeId, backgroundProcessKey);
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
	}
	
	@Override
	public void stopBackgroundProcess(int simNodeId, String backgroundProcessKey) throws SimulatorServiceException, SimulatorServiceProxyException {
		try {
			this.remoteSimService.stopBackgroundProcess(simNodeId, backgroundProcessKey);
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
	}
	
	@Override
	public boolean isBackgroundProcessRunning(int simNodeId, String backgroundProcessKey) throws SimulatorServiceException, SimulatorServiceProxyException {
		try {
			return this.remoteSimService.isBackgroundProcessRunning(simNodeId, backgroundProcessKey);
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
	}
	
	@Override
	public void processBackgroundProcess(int simNodeId, String backgroundProcessKey) throws SimulatorServiceException, SimulatorServiceProxyException {
		try {
			this.remoteSimService.processBackgroundProcess(simNodeId, backgroundProcessKey);
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
	}
	
	
	
	
	@Override
	public Object callExtension(int simNodeId, String extKey) throws SimulatorServiceException, SimulatorServiceProxyException {
		try {
			return this.remoteSimService.callExtension(simNodeId, extKey);
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
	}
	
	@Override
	public Object callExtension(int simNodeId, String extKey, Object arg) throws SimulatorServiceException, SimulatorServiceProxyException {
		try {
			return this.remoteSimService.callExtension(simNodeId, extKey, arg);
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
	}
	
	@Override
	public Object callExtension(int simNodeId, String extKey, Object[] args) throws SimulatorServiceException, SimulatorServiceProxyException {
		try {
			return this.remoteSimService.callExtension(simNodeId, extKey, args);
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
	}
	
	@Override
	public Object callExtension(int simNodeId, String extKey, Object entryPoint, Object[] args) throws SimulatorServiceException, SimulatorServiceProxyException {
		try {
			return this.remoteSimService.callExtension(simNodeId, extKey, entryPoint, args);
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
	}
	
	

	
	@Override
	public boolean put(int simNodeId, NodePointer recipient, BigInteger key, HyCubeResource resource, Object[] parameters) throws SimulatorServiceException, SimulatorServiceProxyException {
		try {
			return this.remoteSimService.put(simNodeId, recipient, key, resource, parameters);
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
	}
	
	@Override
	public boolean refreshPut(int simNodeId, NodePointer recipient, BigInteger key, HyCubeResourceDescriptor resourceDescriptor, Object[] parameters) throws SimulatorServiceException, SimulatorServiceProxyException {
		try {
			return this.remoteSimService.refreshPut(simNodeId, recipient, key, resourceDescriptor, parameters);
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
	}
	
	@Override
	public HyCubeResource[] get(int simNodeId, NodePointer recipient, BigInteger key, HyCubeResourceDescriptor criteria, Object[] parameters) throws SimulatorServiceException, SimulatorServiceProxyException {
		try {
			return this.remoteSimService.get(simNodeId, recipient, key, criteria, parameters);
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
	}
	
	@Override
	public boolean delete(int simNodeId, NodePointer recipient, BigInteger key, HyCubeResourceDescriptor criteria, Object[] parameters) throws SimulatorServiceException, SimulatorServiceProxyException {
		try {
			return this.remoteSimService.delete(simNodeId, recipient, key, criteria, parameters);
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
	}
	
	@Override
	public boolean isReplica(int simNodeId, BigInteger key, NodeId nodeId, int k) throws SimulatorServiceException, SimulatorServiceProxyException {
		try {
			return this.remoteSimService.isReplica(simNodeId, key, nodeId, k);
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
	}
	
	
	
	@Override
	public void resetStats() throws SimulatorServiceException, SimulatorServiceProxyException {
		
		try {
			this.remoteSimService.resetStats();
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
		
	}
	
	
	@Override
	public void clear() throws SimulatorServiceException, SimulatorServiceProxyException {
		
		try {
			this.remoteSimService.clear();
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
		
	}
	
	
	
	@Override
	public void runGC() throws SimulatorServiceException, SimulatorServiceProxyException {
		try {
			this.remoteSimService.runGC();
		} catch (RemoteException e) {
			throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
	}
	
	

	@Override
	public void discard() throws SimulatorServiceProxyException {

		try {
			this.remoteSimService.discard();
		} catch (RemoteException e) {
			//throw new SimulatorServiceProxyException("A RemoteException was thrown while invoking a remote method.", e);
		}
		
		
		this.remoteSimService = null;
		this.discarded = true;
		
	}

	public boolean isDiscarded() {
		return discarded;
	}
	
	
}
