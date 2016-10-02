package net.hycube.simulator.rmi;

import java.math.BigInteger;
import java.rmi.Remote;
import java.rmi.RemoteException;

import net.hycube.core.NodeId;
import net.hycube.core.NodePointer;
import net.hycube.dht.HyCubeResource;
import net.hycube.dht.HyCubeResourceDescriptor;
import net.hycube.environment.NodeProperties;
import net.hycube.simulator.SimulatorServiceException;
import net.hycube.simulator.stat.MessageStat;

public interface RMIRemoteSimulatorService extends Remote {

	public void establishConnection(String simId, String simConnectionUrl) throws SimulatorServiceException, RemoteException;
	public void removeConnection(String simId) throws SimulatorServiceException, RemoteException;
	
	public void readProperties(String propertiesFile) throws SimulatorServiceException, RemoteException;
	public void readPropertiesWithDefaultValues(String defaultPropertiesFile, String propertiesFile) throws SimulatorServiceException, RemoteException;
	public NodeProperties getNodeProperties() throws SimulatorServiceException, RemoteException;
	public void setNodeProperties(NodeProperties properties) throws SimulatorServiceException, RemoteException;
	
	public void join(int simNodeId, NodeId nodeId, String networkAddress, String bootstrapNodeAddress) throws SimulatorServiceException, RemoteException;
	public void leave(int simNodeId) throws SimulatorServiceException, RemoteException;
	public void discardNode(int simNodeId) throws SimulatorServiceException, RemoteException;
	
	public void recoverNode(int simNodeId) throws SimulatorServiceException, RemoteException;
	public void recoverNodeNS(int simNodeId) throws SimulatorServiceException, RemoteException;
	public void recoverAllNodes() throws SimulatorServiceException, RemoteException;
	public void recoverAllNodesNS() throws SimulatorServiceException, RemoteException;
	
	
	public void routeMessageAsync(int simNodeId, NodeId destinationNode) throws SimulatorServiceException, RemoteException;
	public boolean routeMessageSync(int simNodeId, NodeId destinationNode) throws SimulatorServiceException, RemoteException;
	
	public int getMessagesSentNum() throws RemoteException;
	public int getMessagesReceivedNum() throws RemoteException;
	public int getMessagesDeliveredNum() throws RemoteException;
	public int getMessagesLostNum() throws RemoteException;
	
	
	public MessageStat getMessageStatistics() throws RemoteException;
	
	
	public NodePointer lookup(int simNodeId, NodeId lookupNodeId) throws SimulatorServiceException, RemoteException;
	public NodePointer lookup(int simNodeId, NodeId lookupNodeId, Object[] parameters) throws SimulatorServiceException, RemoteException;
	public NodePointer[] search(int simNodeId, NodeId searchNodeId, short k, boolean ignoreTargetNode) throws SimulatorServiceException, RemoteException;
	public NodePointer[] search(int simNodeId, NodeId searchNodeId, short k, boolean ignoreTargetNode, Object[] parameters) throws SimulatorServiceException, RemoteException;
	
	
	public void startBackgroundProcess(int simNodeId, String backgroundProcessKey) throws SimulatorServiceException, RemoteException;
	public void stopBackgroundProcess(int simNodeId, String backgroundProcessKey) throws SimulatorServiceException, RemoteException;
	public boolean isBackgroundProcessRunning(int simNodeId, String backgroundProcessKey) throws SimulatorServiceException, RemoteException;
	public void processBackgroundProcess(int simNodeId, String backgroundProcessKey) throws SimulatorServiceException, RemoteException;
	
	public Object callExtension(int simNodeId, String extKey) throws SimulatorServiceException, RemoteException;
	public Object callExtension(int simNodeId, String extKey, Object arg) throws SimulatorServiceException, RemoteException;
	public Object callExtension(int simNodeId, String extKey, Object[] args) throws SimulatorServiceException, RemoteException;
	public Object callExtension(int simNodeId, String extKey, Object entryPoint, Object[] args) throws SimulatorServiceException, RemoteException;
	

	public boolean put(int simNodeId, NodePointer recipient, BigInteger key, HyCubeResource resource, Object[] parameters) throws SimulatorServiceException, RemoteException;
	public boolean refreshPut(int simNodeId, NodePointer recipient, BigInteger key, HyCubeResourceDescriptor resourceDescriptor, Object[] parameters) throws SimulatorServiceException, RemoteException;
	public HyCubeResource[] get(int simNodeId, NodePointer recipient, BigInteger key, HyCubeResourceDescriptor criteria, Object[] parameters) throws SimulatorServiceException, RemoteException;
	public boolean delete(int simNodeId, NodePointer recipient, BigInteger key, HyCubeResourceDescriptor criteria, Object[] parameters) throws SimulatorServiceException, RemoteException;
	public boolean isReplica(int simNodeId, BigInteger key, NodeId nodeId, int k) throws SimulatorServiceException, RemoteException;
	
	
	public void resetStats() throws SimulatorServiceException, RemoteException;
	
	public void clear() throws SimulatorServiceException, RemoteException;
	
	
	
	public void runGC() throws SimulatorServiceException, RemoteException;
	
	
	
	public void discard() throws RemoteException;

}
