package net.hycube.simulator;

import java.math.BigInteger;

import net.hycube.core.NodeId;
import net.hycube.core.NodePointer;
import net.hycube.dht.HyCubeResource;
import net.hycube.dht.HyCubeResourceDescriptor;
import net.hycube.environment.NodeProperties;
import net.hycube.simulator.stat.MessageStat;

public interface SimulatorServiceProxy {

	
	public void establishConnection(String simId, String simConnectionUrl) throws SimulatorServiceException, SimulatorServiceProxyException;
	public void closeConnection(String simId) throws SimulatorServiceException, SimulatorServiceProxyException;
	
	public void readProperties(String propertiesFile) throws SimulatorServiceException, SimulatorServiceProxyException;
	public void readPropertiesWithDefaultValues(String defaultPropertiesFile, String propertiesFile) throws SimulatorServiceException, SimulatorServiceProxyException;
	public NodeProperties getNodeProperties() throws SimulatorServiceException, SimulatorServiceProxyException;
	public void setNodeProperties(NodeProperties properties) throws SimulatorServiceException, SimulatorServiceProxyException;
	
	public void join(int simNodeId, NodeId nodeId, String networkAddress, String bootstrapNodeAddress) throws SimulatorServiceException, SimulatorServiceProxyException;
	public void leave(int simNodeId) throws SimulatorServiceException, SimulatorServiceProxyException;
	public void discardNode(int simNodeId) throws SimulatorServiceException, SimulatorServiceProxyException;
	
	public void recoverNode(int simNodeId) throws SimulatorServiceException, SimulatorServiceProxyException;
	public void recoverNodeNS(int simNodeId) throws SimulatorServiceException, SimulatorServiceProxyException;
	public void recoverAllNodes() throws SimulatorServiceException, SimulatorServiceProxyException;
	public void recoverAllNodesNS() throws SimulatorServiceException, SimulatorServiceProxyException;
	
	
	public void routeMessageAsync(int simNodeId, NodeId destinationNode) throws SimulatorServiceException, SimulatorServiceProxyException;
	public boolean routeMessageSync(int simNodeId, NodeId destinationNode) throws SimulatorServiceException, SimulatorServiceProxyException;
	
	public int getMessagesSentNum() throws SimulatorServiceProxyException;
	public int getMessagesReceivedNum() throws SimulatorServiceProxyException;
	public int getMessagesDeliveredNum() throws SimulatorServiceProxyException;
	public int getMessagesLostNum() throws SimulatorServiceProxyException;
	
	
	public MessageStat getMessageStatistics() throws SimulatorServiceProxyException;
	
	
	public NodePointer lookup(int simNodeId, NodeId lookupNodeId) throws SimulatorServiceException, SimulatorServiceProxyException;
	public NodePointer lookup(int simNodeId, NodeId lookupNodeId, Object[] parameters) throws SimulatorServiceException, SimulatorServiceProxyException;
	public NodePointer[] search(int simNodeId, NodeId searchNodeId, short k, boolean ignoreTargetNode) throws SimulatorServiceException, SimulatorServiceProxyException;
	public NodePointer[] search(int simNodeId, NodeId searchNodeId, short k, boolean ignoreTargetNode, Object[] parameters) throws SimulatorServiceException, SimulatorServiceProxyException;
	
	
	public void startBackgroundProcess(int simNodeId, String backgroundProcessKey) throws SimulatorServiceException, SimulatorServiceProxyException;
	public void stopBackgroundProcess(int simNodeId, String backgroundProcessKey) throws SimulatorServiceException, SimulatorServiceProxyException;
	public boolean isBackgroundProcessRunning(int simNodeId, String backgroundProcessKey) throws SimulatorServiceException, SimulatorServiceProxyException;
	public void processBackgroundProcess(int simNodeId, String backgroundProcessKey) throws SimulatorServiceException, SimulatorServiceProxyException;
	
	public Object callExtension(int simNodeId, String extKey) throws SimulatorServiceException, SimulatorServiceProxyException;
	public Object callExtension(int simNodeId, String extKey, Object arg) throws SimulatorServiceException, SimulatorServiceProxyException;
	public Object callExtension(int simNodeId, String extKey, Object[] args) throws SimulatorServiceException, SimulatorServiceProxyException;
	public Object callExtension(int simNodeId, String extKey, Object entryPoint, Object[] args) throws SimulatorServiceException, SimulatorServiceProxyException;
	
	
	public boolean put(int simNodeId, NodePointer recipient, BigInteger key, HyCubeResource resource, Object[] parameters) throws SimulatorServiceException, SimulatorServiceProxyException;
	public boolean refreshPut(int simNodeId, NodePointer recipient, BigInteger key, HyCubeResourceDescriptor resourceDescriptor, Object[] parameters) throws SimulatorServiceException, SimulatorServiceProxyException;
	public HyCubeResource[] get(int simNodeId, NodePointer recipient, BigInteger key, HyCubeResourceDescriptor criteria, Object[] parameters) throws SimulatorServiceException, SimulatorServiceProxyException;
	public boolean delete(int simNodeId, NodePointer recipient, BigInteger key, HyCubeResourceDescriptor criteria, Object[] parameters) throws SimulatorServiceException, SimulatorServiceProxyException;
	public boolean isReplica(int simNodeId, BigInteger key, NodeId nodeId, int k) throws SimulatorServiceException, SimulatorServiceProxyException;
	
	
	public void resetStats() throws SimulatorServiceException, SimulatorServiceProxyException;
	
	public void clear() throws SimulatorServiceException, SimulatorServiceProxyException;
	
	
	public void runGC() throws SimulatorServiceException, SimulatorServiceProxyException;
	
	
	public void discard() throws SimulatorServiceProxyException;
	
}
