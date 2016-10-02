package net.hycube.simulator;

import java.math.BigInteger;

import net.hycube.core.NodeId;
import net.hycube.core.NodePointer;
import net.hycube.dht.HyCubeResource;
import net.hycube.dht.HyCubeResourceDescriptor;
import net.hycube.environment.NodeProperties;
import net.hycube.simulator.stat.MessageStat;

public interface SimulatorService {

	
	public void establishConnection(String simId, String simConnectionUrl) throws SimulatorServiceException;
	public void removeConnection(String simId) throws SimulatorServiceException;
	
	public void readProperties(String propertiesFile) throws SimulatorServiceException;
	public void readPropertiesWithDefaultValues(String defaultPropertiesFile, String propertiesFile) throws SimulatorServiceException;
	public NodeProperties getNodeProperties() throws SimulatorServiceException;
	public void setNodeProperties(NodeProperties properties) throws SimulatorServiceException;
	
	
	public void join(int simNodeId, NodeId nodeId, String networkAddress, String bootstrapNodeAddress) throws SimulatorServiceException;
	public void leave(int simNodeId) throws SimulatorServiceException;
	public void discardNode(int simNodeId) throws SimulatorServiceException;
	
	public void recoverNode(int simNodeId) throws SimulatorServiceException;
	public void recoverNodeNS(int simNodeId) throws SimulatorServiceException;
	public void recoverAllNodes() throws SimulatorServiceException;
	public void recoverAllNodesNS() throws SimulatorServiceException;
	
	
	public void routeMessageAsync(int simNodeId, NodeId destinationNode) throws SimulatorServiceException;
	public boolean routeMessageSync(int simNodeId, NodeId destinationNode) throws SimulatorServiceException;
	
	public int getMessagesSentNum();
	public int getMessagesReceivedNum();
	public int getMessagesDeliveredNum();
	public int getMessagesLostNum();
	
	
	public MessageStat getMessageStatistics();
	
	
	public NodePointer lookup(int simNodeId, NodeId lookupNodeId) throws SimulatorServiceException;
	public NodePointer lookup(int simNodeId, NodeId lookupNodeId, Object[] parameters) throws SimulatorServiceException;
	public NodePointer[] search(int simNodeId, NodeId searchNodeId, short k, boolean ignoreTargetNode) throws SimulatorServiceException;
	public NodePointer[] search(int simNodeId, NodeId searchNodeId, short k, boolean ignoreTargetNode, Object[] parameters) throws SimulatorServiceException;
	
	
	
	public void startBackgroundProcess(int simNodeId, String backgroundProcessKey) throws SimulatorServiceException;
	public void stopBackgroundProcess(int simNodeId, String backgroundProcessKey) throws SimulatorServiceException;
	public boolean isBackgroundProcessRunning(int simNodeId, String backgroundProcessKey) throws SimulatorServiceException;
	public void processBackgroundProcess(int simNodeId, String backgroundProcessKey) throws SimulatorServiceException;
	
	public Object callExtension(int simNodeId, String extKey) throws SimulatorServiceException;
	public Object callExtension(int simNodeId, String extKey, Object arg) throws SimulatorServiceException;
	public Object callExtension(int simNodeId, String extKey, Object[] args) throws SimulatorServiceException;
	public Object callExtension(int simNodeId, String extKey, Object entryPoint, Object[] args) throws SimulatorServiceException;
	
	
	public boolean put(int simNodeId, NodePointer recipient, BigInteger key, HyCubeResource resource, Object[] parameters) throws SimulatorServiceException;
	public boolean refreshPut(int simNodeId, NodePointer recipient, BigInteger key, HyCubeResourceDescriptor resourceDescriptor, Object[] parameters) throws SimulatorServiceException;
	public HyCubeResource[] get(int simNodeId, NodePointer recipient, BigInteger key, HyCubeResourceDescriptor criteria, Object[] parameters) throws SimulatorServiceException;
	public boolean delete(int simNodeId, NodePointer recipient, BigInteger key, HyCubeResourceDescriptor criteria, Object[] parameters) throws SimulatorServiceException;
	public boolean isReplica(int simNodeId, BigInteger key, NodeId nodeId, int k) throws SimulatorServiceException;
	
	
	public void resetStats() throws SimulatorServiceException;
	
	public void clear() throws SimulatorServiceException;
	
	
	public void runGC() throws SimulatorServiceException;
	
	
	public void discard();
	
	
	

	
	
}
