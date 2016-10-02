package net.hycube.simulator.rmi;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

import net.hycube.configuration.GlobalConstants;
import net.hycube.core.NodeId;
import net.hycube.core.NodePointer;
import net.hycube.dht.HyCubeResource;
import net.hycube.dht.HyCubeResourceDescriptor;
import net.hycube.environment.NodeProperties;
import net.hycube.simulator.SimulatorService;
import net.hycube.simulator.SimulatorServiceException;
import net.hycube.simulator.stat.MessageStat;
import net.hycube.utils.HashMapUtils;

public class RMISimulatorService extends UnicastRemoteObject implements RMIRemoteSimulatorService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4312892014510495740L;
	
	
	
	protected SimulatorService simulatorService;
	protected Map<Integer, Registry> registries;
	
	
	
	public RMISimulatorService(SimulatorService simulatorService) throws RemoteException {
		
		this.simulatorService = simulatorService;
		this.registries = new HashMap<Integer, Registry>(HashMapUtils.getHashMapCapacityForElementsNum(1, GlobalConstants.DEFAULT_HASH_MAP_LOAD_FACTOR), GlobalConstants.DEFAULT_HASH_MAP_LOAD_FACTOR);
		
	}
	
	
	public void createRegistry(int port) throws RemoteException {
		
		Registry reg = LocateRegistry.createRegistry(port);
		
		registries.put(port, reg);
		
	}
	
	
	public void shutdownRegistry(int port) throws RemoteException {
		
		Registry reg = registries.get(port);
		if (reg != null) {
			UnicastRemoteObject.unexportObject(reg, true);
		}
		
		
	}
	
	
	
	public void bind(String hostname, int port, String serviceName) throws RemoteException, AlreadyBoundException {
		
		StringBuilder sb = new StringBuilder();
		sb.append("//").append(hostname).append(":").append(port).append("/").append(serviceName);
		String url = sb.toString();
		
		try {
			Naming.bind(url, this);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Invalid URL", e);
		}
		
		
	}
	
	
	public void unbind(String hostname, int port, String serviceName) throws RemoteException, NotBoundException {
		
		StringBuilder sb = new StringBuilder();
		sb.append("//").append(hostname).append(":").append(port).append("/").append(serviceName);
		String url = sb.toString();
		
		try {
			Naming.unbind(url);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Invalid URL", e);
		}
		
		//force closing the rmi socket for this object
		UnicastRemoteObject.unexportObject(this, true);
		
	}

	
	
	
	
	@Override
	public void establishConnection(String simId, String simConnectionUrl) throws SimulatorServiceException, RemoteException {
		
		this.simulatorService.establishConnection(simId, simConnectionUrl);
		
	}
	
	
	@Override
	public void removeConnection(String simId) throws SimulatorServiceException, RemoteException {
		
		this.simulatorService.removeConnection(simId);
		
	}
	
	
	
	
	@Override
	public void readProperties(String propertiesFile) throws SimulatorServiceException, RemoteException {
		this.simulatorService.readProperties(propertiesFile);
	}
	
	@Override
	public void readPropertiesWithDefaultValues(String defaultPropertiesFile, String propertiesFile) throws SimulatorServiceException, RemoteException {
		this.simulatorService.readPropertiesWithDefaultValues(defaultPropertiesFile, propertiesFile);
	}
	
	@Override
	public NodeProperties getNodeProperties() throws SimulatorServiceException, RemoteException {
		return this.simulatorService.getNodeProperties();
	}
	
	@Override
	public void setNodeProperties(NodeProperties properties) throws SimulatorServiceException, RemoteException {
		this.simulatorService.setNodeProperties(properties);
	}
	
	
	

	@Override
	public void join(int simNodeId, NodeId nodeId, String networkAddress,
			String bootstrapNodeAddress) throws SimulatorServiceException,
			RemoteException {

		this.simulatorService.join(simNodeId, nodeId, networkAddress, bootstrapNodeAddress);
		
		
	}


	@Override
	public void leave(int simNodeId) throws SimulatorServiceException, RemoteException {

		this.simulatorService.leave(simNodeId);
		
	}


	@Override
	public void discardNode(int simNodeId) throws SimulatorServiceException,
			RemoteException {

		this.simulatorService.discardNode(simNodeId);
		
	}


	@Override
	public void recoverNode(int simNodeId) throws SimulatorServiceException,
			RemoteException {

		this.simulatorService.recoverNode(simNodeId);
		
	}


	@Override
	public void recoverNodeNS(int simNodeId) throws SimulatorServiceException,
			RemoteException {

		this.simulatorService.recoverNodeNS(simNodeId);
		
	}


	@Override
	public void recoverAllNodes() throws SimulatorServiceException, RemoteException {
		
		this.simulatorService.recoverAllNodes();
		
	}


	@Override
	public void recoverAllNodesNS() throws SimulatorServiceException, RemoteException {
		
		this.simulatorService.recoverAllNodesNS();
		
	}


	@Override
	public void routeMessageAsync(int simNodeId, NodeId destinationNode)
			throws SimulatorServiceException, RemoteException {
		
		this.simulatorService.routeMessageAsync(simNodeId, destinationNode);
		
	}


	@Override
	public boolean routeMessageSync(int simNodeId, NodeId destinationNode)
			throws SimulatorServiceException, RemoteException {
		
		return this.simulatorService.routeMessageSync(simNodeId, destinationNode);
		
	}


	@Override
	public int getMessagesSentNum() throws RemoteException {

		return this.simulatorService.getMessagesSentNum();
		
	}


	@Override
	public int getMessagesReceivedNum() throws RemoteException {
		
		return this.simulatorService.getMessagesReceivedNum();
		
	}


	@Override
	public int getMessagesDeliveredNum() throws RemoteException {
		
		return this.simulatorService.getMessagesDeliveredNum();
		
	}


	@Override
	public int getMessagesLostNum() throws RemoteException {
		
		return this.simulatorService.getMessagesLostNum();
		
	}

	
	@Override
	public MessageStat getMessageStatistics() throws RemoteException {
		
		return this.simulatorService.getMessageStatistics();
		
	}
	
	

	@Override
	public NodePointer lookup(int simNodeId, NodeId lookupNodeId)
			throws SimulatorServiceException, RemoteException {
		
		return this.simulatorService.lookup(simNodeId, lookupNodeId);
		
	}
	
	
	@Override
	public NodePointer lookup(int simNodeId, NodeId lookupNodeId, Object[] parameters)
			throws SimulatorServiceException, RemoteException {
		
		return this.simulatorService.lookup(simNodeId, lookupNodeId, parameters);
		
	}


	@Override
	public NodePointer[] search(int simNodeId, NodeId searchNodeId, short k,
			boolean ignoreTargetNode) throws SimulatorServiceException,
			RemoteException {

		return this.simulatorService.search(simNodeId, searchNodeId, k, ignoreTargetNode);
		
	}
	
	
	@Override
	public NodePointer[] search(int simNodeId, NodeId searchNodeId, short k,
			boolean ignoreTargetNode, Object[] parameters) throws SimulatorServiceException,
			RemoteException {

		return this.simulatorService.search(simNodeId, searchNodeId, k, ignoreTargetNode, parameters);
		
	}

	
	

	@Override
	public void startBackgroundProcess(int simNodeId, String backgroundProcessKey) throws SimulatorServiceException, RemoteException {
		this.simulatorService.startBackgroundProcess(simNodeId, backgroundProcessKey);
	}
	
	@Override
	public void stopBackgroundProcess(int simNodeId, String backgroundProcessKey) throws SimulatorServiceException, RemoteException {
		this.simulatorService.stopBackgroundProcess(simNodeId, backgroundProcessKey);
	}
	
	@Override
	public boolean isBackgroundProcessRunning(int simNodeId, String backgroundProcessKey) throws SimulatorServiceException, RemoteException {
		return this.simulatorService.isBackgroundProcessRunning(simNodeId, backgroundProcessKey);
	}
	
	@Override
	public void processBackgroundProcess(int simNodeId, String backgroundProcessKey) throws SimulatorServiceException, RemoteException {
		this.simulatorService.processBackgroundProcess(simNodeId, backgroundProcessKey);
	}
	
	
	
	@Override
	public Object callExtension(int simNodeId, String extKey) throws SimulatorServiceException, RemoteException {
		return this.simulatorService.callExtension(simNodeId, extKey);
	}
	
	@Override
	public Object callExtension(int simNodeId, String extKey, Object arg) throws SimulatorServiceException, RemoteException {
		return this.simulatorService.callExtension(simNodeId, extKey, arg);
	}
	
	@Override
	public Object callExtension(int simNodeId, String extKey, Object[] args) throws SimulatorServiceException, RemoteException {
		return this.simulatorService.callExtension(simNodeId, extKey, args);
	}
	
	@Override
	public Object callExtension(int simNodeId, String extKey, Object entryPoint, Object[] args) throws SimulatorServiceException, RemoteException {
		return this.simulatorService.callExtension(simNodeId, extKey, entryPoint, args);
	}
	


	@Override
	public boolean put(int simNodeId, NodePointer recipient, BigInteger key, HyCubeResource resource, Object[] parameters) throws SimulatorServiceException, RemoteException {
		return this.simulatorService.put(simNodeId, recipient, key, resource, parameters);
	}
	
	@Override
	public boolean refreshPut(int simNodeId, NodePointer recipient, BigInteger key, HyCubeResourceDescriptor resourceDescriptor, Object[] parameters) throws SimulatorServiceException, RemoteException {
		return this.simulatorService.refreshPut(simNodeId, recipient, key, resourceDescriptor, parameters);
	}
	
	@Override
	public HyCubeResource[] get(int simNodeId, NodePointer recipient, BigInteger key, HyCubeResourceDescriptor criteria, Object[] parameters) throws SimulatorServiceException, RemoteException {
		return this.simulatorService.get(simNodeId, recipient, key, criteria, parameters);
	}
	
	@Override
	public boolean delete(int simNodeId, NodePointer recipient, BigInteger key, HyCubeResourceDescriptor criteria, Object[] parameters) throws SimulatorServiceException, RemoteException {
		return this.simulatorService.delete(simNodeId, recipient, key, criteria, parameters);
	}
	
	@Override
	public boolean isReplica(int simNodeId, BigInteger key, NodeId nodeId, int k) throws SimulatorServiceException, RemoteException {
		return this.simulatorService.isReplica(simNodeId, key, nodeId, k);
	}
	
	
	
	@Override
	public void resetStats() throws SimulatorServiceException, RemoteException {
		
		this.simulatorService.resetStats();
		
	}
	
	
	
	@Override
	public void clear() throws SimulatorServiceException, RemoteException {
		
		this.simulatorService.clear();
		
	}
	
	
	
	
	@Override
	public void runGC() throws SimulatorServiceException, RemoteException {
		
		this.simulatorService.runGC();
		
	}
	
	
	
	
	@Override
	public void discard() throws RemoteException {

		this.simulatorService.discard();
		
	}
	
	
	
}
