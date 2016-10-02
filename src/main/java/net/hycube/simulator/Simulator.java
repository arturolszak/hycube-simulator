package net.hycube.simulator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import net.hycube.HyCubeMultiQueueMultipleNodeService;
import net.hycube.HyCubeNodeService;
import net.hycube.NodeServiceException;
import net.hycube.backgroundprocessing.BackgroundProcessEntryPoint;
import net.hycube.common.EntryPoint;
import net.hycube.configuration.GlobalConstants;
import net.hycube.core.InitializationException;
import net.hycube.core.NodeId;
import net.hycube.core.NodePointer;
import net.hycube.dht.DeleteWaitCallback;
import net.hycube.dht.GetWaitCallback;
import net.hycube.dht.HyCubeDHTManagerEntryPoint;
import net.hycube.dht.HyCubeResource;
import net.hycube.dht.HyCubeResourceDescriptor;
import net.hycube.dht.PutWaitCallback;
import net.hycube.dht.RefreshPutWaitCallback;
import net.hycube.environment.NodeProperties;
import net.hycube.environment.NodePropertiesInitializationException;
import net.hycube.environment.TimeProvider;
import net.hycube.eventprocessing.EventCategory;
import net.hycube.eventprocessing.EventProcessingErrorCallback;
import net.hycube.eventprocessing.EventQueueProcessingInfo;
import net.hycube.eventprocessing.EventType;
import net.hycube.eventprocessing.ThreadPoolInfo;
import net.hycube.join.JoinWaitCallback;
import net.hycube.lookup.LookupWaitCallback;
import net.hycube.messaging.ack.MessageAckCallback;
import net.hycube.messaging.ack.WaitMessageAckCallback;
import net.hycube.messaging.data.DataMessage;
import net.hycube.messaging.data.ReceivedDataMessage;
import net.hycube.messaging.processing.MessageSendInfo;
import net.hycube.search.SearchWaitCallback;
import net.hycube.simulator.environment.SimEnvironment;
import net.hycube.simulator.log.LogHelper;
import net.hycube.simulator.stat.MessageStat;
import net.hycube.simulator.transport.SimNetworkProxy;
import net.hycube.simulator.transport.SimNetworkProxyException;
import net.hycube.transport.MessageReceiver;
import net.hycube.utils.HashMapUtils;

public class Simulator implements SimulatorService {

	private static org.apache.commons.logging.Log userLog = LogHelper.getUserLog();
	private static org.apache.commons.logging.Log devLog = LogHelper.getDevLog(Simulator.class); 
	
	
	protected static final int SIM_ID_LENGTH = 4;
	
	protected static final long PROCESSING_THREAD_KEEP_ALIVE_TIME = 60;
	
	protected static final int DEFAULT_EXPECTED_NUMBER_OF_NODES = 100;
	
	
	protected String simId;
	
	protected int expectedNumberOfNodes;
	
	protected SimEnvironment environment;
	protected HyCubeMultiQueueMultipleNodeService mulNodeService;
	
	protected MessageReceiver messageReceiver;
	
	
	protected SimNetworkProxy simNetworkProxy;
	protected TimeProvider simTimeProvider;
	
	
	protected HashMap<Integer, HyCubeNodeService> nodeServices;
	protected HashMap<Integer, NodeId> nodeIds;
	protected HashMap<Integer, LinkedBlockingQueue<ReceivedDataMessage>> msgQueues;
	
	
	
	protected int msgSentCounter;
	protected int msgReceivedCounter;
	protected int msgDeliveredCounter;
	protected int msgLostCounter;
	protected Object msgCountersLock = new Object();
	
	
	
	
	public class SimulatorEventProcessingErrorCallback implements EventProcessingErrorCallback {
		@Override
		public void errorOccurred(Object arg) {
			Simulator.this.errorOccured(arg);	
		}
	}
	
	
	
	
	protected Simulator() {
		
	}
	

	public static Simulator initialize(String simId, String propertiesFileName, SimNetworkProxy simNetworkProxy, TimeProvider simTimeProvider, boolean separateMRQueue, int numThreads, int numMRThreads, int numNonMRThreads) throws InitializationException {
		return initialize(simId, null, propertiesFileName, simNetworkProxy, simTimeProvider, separateMRQueue, numThreads, numMRThreads, numNonMRThreads, 0);
	}
	
	public static Simulator initialize(String simId, String propertiesFileName, SimNetworkProxy simNetworkProxy, TimeProvider simTimeProvider, boolean separateMRQueue, int numThreads, int numMRThreads, int numNonMRThreads,  int expectedNumOfNodes) throws InitializationException {
		return initialize(simId, null, propertiesFileName, simNetworkProxy, simTimeProvider, separateMRQueue, numThreads, numMRThreads, numNonMRThreads, expectedNumOfNodes);
	}
	
	
	public static Simulator initialize(String simId, String defaultPropertiesFileName, String propertiesFileName, SimNetworkProxy simNetworkProxy, TimeProvider simTimeProvider, boolean separateMRQueue, int numThreads, int numMRThreads, int numNonMRThreads) throws InitializationException {
		return initialize(simId, propertiesFileName, simNetworkProxy, simTimeProvider, separateMRQueue, numThreads, numMRThreads, numNonMRThreads, 0);
	}
	
	public static Simulator initialize(String simId, String defaultPropertiesFileName, String propertiesFileName, SimNetworkProxy simNetworkProxy, TimeProvider simTimeProvider, boolean separateMRQueue, int numThreads, int numMRThreads, int numNonMRThreads,  int expectedNumOfNodes) throws InitializationException {
		
		Simulator simulator = new Simulator();
		
		userLog.info("Initializing.");
		devLog.info("Initializing.");
		
		if (simId == null || simId.length() != SIM_ID_LENGTH) {
			throw new IllegalArgumentException("The simulator id must be " + SIM_ID_LENGTH + " chactaers long.");
		}
		
		simulator.simId = simId;
		
		if (expectedNumOfNodes <= 0) {
			expectedNumOfNodes = DEFAULT_EXPECTED_NUMBER_OF_NODES;
		}
		
		
		simulator.msgSentCounter = 0;
		simulator.msgReceivedCounter = 0;
		simulator.msgDeliveredCounter = 0;
		simulator.msgLostCounter = 0;
		
		
		simulator.simNetworkProxy = simNetworkProxy;
		simulator.simTimeProvider = simTimeProvider;
		
		if (defaultPropertiesFileName != null) {
			simulator.environment = SimEnvironment.initializeWithDefaultProperties(defaultPropertiesFileName, propertiesFileName, simNetworkProxy, simTimeProvider);
		}
		else {
			simulator.environment = SimEnvironment.initialize(propertiesFileName, simNetworkProxy, simTimeProvider);
		}


		
		simulator.nodeServices = new HashMap<Integer, HyCubeNodeService>(HashMapUtils.getHashMapCapacityForElementsNum(expectedNumOfNodes, GlobalConstants.DEFAULT_HASH_MAP_LOAD_FACTOR), GlobalConstants.DEFAULT_HASH_MAP_LOAD_FACTOR);
		simulator.nodeIds = new HashMap<Integer, NodeId>(HashMapUtils.getHashMapCapacityForElementsNum(expectedNumOfNodes, GlobalConstants.DEFAULT_HASH_MAP_LOAD_FACTOR), GlobalConstants.DEFAULT_HASH_MAP_LOAD_FACTOR);
		simulator.msgQueues = new HashMap<Integer, LinkedBlockingQueue<ReceivedDataMessage>>(HashMapUtils.getHashMapCapacityForElementsNum(expectedNumOfNodes, GlobalConstants.DEFAULT_HASH_MAP_LOAD_FACTOR), GlobalConstants.DEFAULT_HASH_MAP_LOAD_FACTOR);

		
		
		
		EventQueueProcessingInfo[] eventQueuesProcessingInfo;
		
		if (separateMRQueue) {
		
			List<EventType> eventTypesOE = new ArrayList<EventType>(EventCategory.values().length);
			for (EventCategory ec : EventCategory.values()) {
				if (ec != EventCategory.receiveMessageEvent) {
					EventType et = new EventType(ec);
					eventTypesOE.add(et);
				}
			}
			
			ThreadPoolInfo tpiMR = new ThreadPoolInfo(numMRThreads, PROCESSING_THREAD_KEEP_ALIVE_TIME);
			EventQueueProcessingInfo eqpiMR = new EventQueueProcessingInfo(tpiMR, new EventType[] {new EventType(EventCategory.receiveMessageEvent)}, true);
			
			ThreadPoolInfo tpiOE = new ThreadPoolInfo(numNonMRThreads, PROCESSING_THREAD_KEEP_ALIVE_TIME);
			EventQueueProcessingInfo eqpiOE = new EventQueueProcessingInfo(tpiOE, (EventType[])eventTypesOE.toArray(new EventType[eventTypesOE.size()]), false);
			
			eventQueuesProcessingInfo = new EventQueueProcessingInfo[] {eqpiOE, eqpiMR};
			
		}
		else {
			
			List<EventType> eventTypes = new ArrayList<EventType>(EventCategory.values().length);
			for (EventCategory ec : EventCategory.values()) {
				EventType et = new EventType(ec);
				eventTypes.add(et);
			}
			
			ThreadPoolInfo tpi = new ThreadPoolInfo(numThreads, PROCESSING_THREAD_KEEP_ALIVE_TIME);
			EventQueueProcessingInfo eqpi = new EventQueueProcessingInfo(tpi, (EventType[])eventTypes.toArray(new EventType[eventTypes.size()]), true);
			
			eventQueuesProcessingInfo = new EventQueueProcessingInfo[] {eqpi};
			
		}
		
		
		
		EventProcessingErrorCallback errorCallback = simulator.new SimulatorEventProcessingErrorCallback();
		
		Object errorCallbackArg = null;

		
		
		
		simulator.mulNodeService = HyCubeMultiQueueMultipleNodeService.initialize(simulator.environment, eventQueuesProcessingInfo, expectedNumOfNodes, errorCallback, errorCallbackArg);
		
		
		//message receiver
		simulator.messageReceiver = simulator.mulNodeService.initializeMessageReceiver();
		
		
		
		//establish connection with self:
		try {
			simulator.simNetworkProxy.establishConnectionWithSelf(simulator.simId);
		} catch (SimNetworkProxyException e) {
			throw new InitializationException("An exception has been thrown by the sim network proxy while trying to establish a messaging connection with self.", e);
		}
		
		
		userLog.info("Initialized.");
		devLog.info("Initialized.");
		
		
		return simulator;
	
		
	}
	
	
	
	@Override
	public void establishConnection(String simId, String simConnectionUrl) throws SimulatorServiceException {
		try {
			this.simNetworkProxy.establishConnection(simId, simConnectionUrl);
		} catch (SimNetworkProxyException e) {
			throw new SimulatorServiceException("A simulator network proxy thrown an exception while trying to establish the connection.", e);
		}
	}
	
	
	@Override
	public void removeConnection(String simId) throws SimulatorServiceException {
		try {
			this.simNetworkProxy.removeConnection(simId);
		} catch (SimNetworkProxyException e) {
			throw new SimulatorServiceException("A simulator network proxy thrown an exception while trying to close the connection.", e);
		}
	}
	
	
	
	@Override
	public void readProperties(String propertiesFile) throws SimulatorServiceException {
		try {
			this.environment.readProperties(propertiesFile);
		} catch (NodePropertiesInitializationException e) {
			throw new SimulatorServiceException("An exceotion has been thrown reding properties.", e);
		}
	}
	
	@Override
	public void readPropertiesWithDefaultValues(String defaultPropertiesFile, String propertiesFile) throws SimulatorServiceException {
		try {
			this.environment.readPropertiesWithDefaultValues(defaultPropertiesFile, propertiesFile);
		} catch (NodePropertiesInitializationException e) {
			throw new SimulatorServiceException("An exceotion has been thrown reding properties.", e);
		}
	}
	
	@Override
	public NodeProperties getNodeProperties() throws SimulatorServiceException {
		return this.environment.getNodeProperties();
	}
	
	@Override
	public void setNodeProperties(NodeProperties properties) throws SimulatorServiceException {
		this.environment.setNodeProperties(properties);
	}
	

	
	@Override
	public void join(int simNodeId, NodeId nodeId, String networkAddress, String bootstrapNodeAddress) throws SimulatorServiceException {
			
		JoinWaitCallback joinWaitCallback = new JoinWaitCallback();
		
		HyCubeNodeService nodeService;
		
		synchronized(this) {
			
			try {
				nodeService = mulNodeService.initializeNode(nodeId, networkAddress, bootstrapNodeAddress, joinWaitCallback, null, this.messageReceiver);
			} catch (InitializationException e) {
				throw new SimulatorServiceException("An exceotion has been thrown while initializing a node.", e);
			}
			
			nodeServices.put(simNodeId, nodeService);
			
			nodeIds.put(simNodeId, nodeId);
			
			LinkedBlockingQueue<ReceivedDataMessage> msgQueue = nodeService.registerPort((short)0); 
			msgQueues.put(simNodeId, msgQueue);
			
		}
		
		try {
			joinWaitCallback.waitJoin();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void leave(int simNodeId) throws SimulatorServiceException {
		
		synchronized(this) {
			
			HyCubeNodeService nodeService = nodeServices.get(simNodeId);
			if (nodeService == null) {
				throw new SimulatorServiceException("There is no node with the simNodeId specified.");
			}
		
			nodeService.leave();
//			mulNodeService.discardNode(nodeService);
//			
//			nodeServices.remove(simNodeId);
//			nodeIds.remove(simNodeId);
//			msgQueues.remove(simNodeId);
		}
	}

	@Override
	public void discardNode(int simNodeId) throws SimulatorServiceException {

		synchronized(this) {
		
			HyCubeNodeService nodeService = nodeServices.get(simNodeId);
			if (nodeService == null) {
				throw new SimulatorServiceException("There is no node with the simNodeId specified.");
			}
			
			mulNodeService.discardNode(nodeService);
			
			nodeServices.remove(simNodeId);
			nodeIds.remove(simNodeId);
			msgQueues.remove(simNodeId);
		}
		
	}

	
	
	
	@Override
	public void recoverNode(int simNodeId) throws SimulatorServiceException {
		
		synchronized (this) {
			HyCubeNodeService nodeService = nodeServices.get(simNodeId);
			if (nodeService == null) {
				throw new SimulatorServiceException("There is no node with the simNodeId specified.");
			}
			
			nodeService.recover();
		}
		
	}
	
	@Override
	public void recoverNodeNS(int simNodeId) throws SimulatorServiceException {
		
		synchronized (this) {
			HyCubeNodeService nodeService = nodeServices.get(simNodeId);
			if (nodeService == null) {
				throw new SimulatorServiceException("There is no node with the simNodeId specified.");
			}
			
			nodeService.recoverNS();
		}
		
	}

	@Override
	public void recoverAllNodes() throws SimulatorServiceException {
		
		synchronized(this) {
			for (HyCubeNodeService nodeService : nodeServices.values()) {
				nodeService.recover();
			}
		}
		
	}
	
	@Override
	public void recoverAllNodesNS() throws SimulatorServiceException {
		
		synchronized(this) {
			for (HyCubeNodeService nodeService : nodeServices.values()) {
				nodeService.recoverNS();
			}
		}
		
	}

	
	
	
	@Override
	public void routeMessageAsync(int simNodeId, NodeId destinationNode) throws SimulatorServiceException {

		synchronized (this) {
			HyCubeNodeService nodeService = nodeServices.get(simNodeId);
			if (nodeService == null) {
				throw new SimulatorServiceException("There is no node with the simNodeId specified.");
			}
			
			StringBuilder sb = new StringBuilder(40);
			sb.append("Message: ").append(simNodeId).append(", ").append(destinationNode.calculateHash());
			final String msgText = sb.toString();
			
			MessageSendInfo msi = null;
			
			MessageAckCallback mac = new MessageAckCallback() {
				
				public void notifyDelivered(Object callbackArg) {
					synchronized (msgCountersLock) {
						msgDeliveredCounter++;
					}
					userLog.debug("Message DELIVERED: " + msgText);
					devLog.debug("Message DELIVERED: " + msgText);
				}
				
				public void notifyUndelivered(Object callbackArg) {
					synchronized (msgCountersLock) {
						msgLostCounter++;
					}
					userLog.debug("Message UNDELIVERED: " + msgText);
					devLog.debug("Message UNDELIVERED: " + msgText);
				}
				
			};
			
			byte[] data = msgText.getBytes();
			
			DataMessage msg = new DataMessage(destinationNode, null, (short)0, (short)0, data);
			try {
				msi = nodeService.send(msg, mac, null);
				msgSentCounter++;
				userLog.debug("Message send info - serial no: " + msi.getSerialNo());
				devLog.debug("Message send info - serial no: " + msi.getSerialNo());
			} catch (NodeServiceException e) {
				throw new SimulatorServiceException("An exception has been thrown while sending a message.", e);
			}
			
			
		}
		
	}

	@Override
	public boolean routeMessageSync(int simNodeId, NodeId destinationNode) throws SimulatorServiceException {
		
		MessageSendInfo msi = null;
		
		WaitMessageAckCallback mac = null;
		
		synchronized (this) {
			HyCubeNodeService nodeService = nodeServices.get(simNodeId);
			if (nodeService == null) {
				throw new SimulatorServiceException("There is no node with the simNodeId specified.");
			}
			
			StringBuilder sb = new StringBuilder(40);
			sb.append("Message: ").append(simNodeId).append(", ").append(destinationNode.calculateHash());
			final String msgText = sb.toString();
			
			mac = new WaitMessageAckCallback() {
				
				public void notifyDelivered(Object callbackArg) {
					super.notifyDelivered(callbackArg);
					synchronized (msgCountersLock) {
						msgDeliveredCounter++;
					}
					userLog.debug("Message DELIVERED: " + msgText);
					devLog.debug("Message DELIVERED: " + msgText);
				}
				
				public void notifyUndelivered(Object callbackArg) {
					super.notifyUndelivered(callbackArg);
					synchronized (msgCountersLock) {
						msgLostCounter++;
					}
					userLog.debug("Message UNDELIVERED: " + msgText);
					devLog.debug("Message UNDELIVERED: " + msgText);
				}
				
			};
			
			byte[] data = msgText.getBytes();
			
			DataMessage msg = new DataMessage(destinationNode, null, (short)0, (short)0, data);
			try {
				msi = nodeService.send(msg, mac, null);
				msgSentCounter++;
				userLog.debug("Message send info - serial no: " + msi.getSerialNo());
				devLog.debug("Message send info - serial no: " + msi.getSerialNo());
			} catch (NodeServiceException e) {
				throw new SimulatorServiceException("An exception has been thrown while sending a message.", e);
			}
			
		}
		
		try {
			mac.waitForAck();
		} catch (InterruptedException e) {
			return mac.isDelivered();
		}
		
		return mac.isDelivered();
		
	}

	@Override
	public int getMessagesSentNum() {
		synchronized (msgCountersLock) {
			return msgSentCounter;
		}
	}

	@Override
	public int getMessagesReceivedNum() {
		synchronized (msgCountersLock) {
			return msgReceivedCounter;
		}
	}

	@Override
	public int getMessagesDeliveredNum() {
		synchronized (msgCountersLock) {
			return msgDeliveredCounter;
		}
	}

	@Override
	public int getMessagesLostNum() {
		synchronized (msgCountersLock) {
			return msgLostCounter;
		}
	}
	
	
	@Override
	public MessageStat getMessageStatistics() {
		//create a copy of the env statistics and return
		MessageStat stat;
		synchronized(environment.getMessageStat()) {
			stat = new MessageStat(environment.getMessageStat().getMsgSentCounter(), environment.getMessageStat().getMsgDeliveredCounter(), environment.getMessageStat().getMsgRouteLengthSum());
		}
		return stat;
	}
	
	

	@Override
	public NodePointer lookup(int simNodeId, NodeId lookupNodeId) throws SimulatorServiceException {
		
		NodePointer lookupResult = null;
		
		LookupWaitCallback lookupCallback = new LookupWaitCallback();
		
		synchronized (this) {
			HyCubeNodeService nodeService = nodeServices.get(simNodeId);
			if (nodeService == null) {
				throw new SimulatorServiceException("There is no node with the simNodeId specified.");
			}
			
			nodeService.lookup(lookupNodeId, lookupCallback, null);
			
		}
	
		
		try {
			lookupResult = lookupCallback.waitForResult(0);
		} catch (InterruptedException e) {
			lookupResult = null;
		}
		
		return lookupResult;
		
		
	}
	
	@Override
	public NodePointer lookup(int simNodeId, NodeId lookupNodeId, Object[] parameters) throws SimulatorServiceException {
		
		NodePointer lookupResult = null;
		
		LookupWaitCallback lookupCallback = new LookupWaitCallback();
		
		synchronized (this) {
			HyCubeNodeService nodeService = nodeServices.get(simNodeId);
			if (nodeService == null) {
				throw new SimulatorServiceException("There is no node with the simNodeId specified.");
			}
			
			nodeService.lookup(lookupNodeId, lookupCallback, null, parameters);
			
		}
	
		
		try {
			lookupResult = lookupCallback.waitForResult(0);
		} catch (InterruptedException e) {
			lookupResult = null;
		}
		
		return lookupResult;
		
		
	}

	@Override
	public NodePointer[] search(int simNodeId, NodeId searchNodeId, short k, boolean ignoreTargetNode) throws SimulatorServiceException {
		
		NodePointer[] searchResults = null;
		
		SearchWaitCallback searchCallback = new SearchWaitCallback();
		
		synchronized (this) {
			HyCubeNodeService nodeService = nodeServices.get(simNodeId);
			if (nodeService == null) {
				throw new SimulatorServiceException("There is no node with the simNodeId specified.");
			}
			
			nodeService.search(searchNodeId, null, (short) k, ignoreTargetNode, searchCallback, null);
			
		}
	
		
		try {
			searchResults = searchCallback.waitForResult(0);
		} catch (InterruptedException e) {
			searchResults = null;
		}
	
		return searchResults;
		
		
	}
	
	@Override
	public NodePointer[] search(int simNodeId, NodeId searchNodeId, short k, boolean ignoreTargetNode, Object[] parameters) throws SimulatorServiceException {
		
		NodePointer[] searchResults = null;
		
		SearchWaitCallback searchCallback = new SearchWaitCallback();
		
		synchronized (this) {
			HyCubeNodeService nodeService = nodeServices.get(simNodeId);
			if (nodeService == null) {
				throw new SimulatorServiceException("There is no node with the simNodeId specified.");
			}
			
			nodeService.search(searchNodeId, null, (short) k, ignoreTargetNode, searchCallback, null, parameters);
			
		}
	
		
		try {
			searchResults = searchCallback.waitForResult(0);
		} catch (InterruptedException e) {
			searchResults = null;
		}
	
		return searchResults;
		
		
	}

	
	
	
	
	
	@Override
	public void startBackgroundProcess(int simNodeId, String backgroundProcessKey) throws SimulatorServiceException {
		synchronized (this) {
			HyCubeNodeService nodeService = nodeServices.get(simNodeId);
			if (nodeService == null) {
				throw new SimulatorServiceException("There is no node with the simNodeId specified.");
			}
			
			BackgroundProcessEntryPoint bpep = nodeService.getNode().getBackgroundProcessEntryPoint(backgroundProcessKey);
			if (bpep == null) {
				throw new SimulatorServiceException("No background process for the specified key.");
			}
			bpep.start();
		}
	}
	
	@Override
	public void stopBackgroundProcess(int simNodeId, String backgroundProcessKey) throws SimulatorServiceException {
		synchronized (this) {
			HyCubeNodeService nodeService = nodeServices.get(simNodeId);
			if (nodeService == null) {
				throw new SimulatorServiceException("There is no node with the simNodeId specified.");
			}
			
			BackgroundProcessEntryPoint bpep = nodeService.getNode().getBackgroundProcessEntryPoint(backgroundProcessKey);
			if (bpep == null) {
				throw new SimulatorServiceException("No background process for the specified key.");
			}
			bpep.stop();
		}
	}
	
	@Override
	public boolean isBackgroundProcessRunning(int simNodeId, String backgroundProcessKey) throws SimulatorServiceException {
		synchronized (this) {
			HyCubeNodeService nodeService = nodeServices.get(simNodeId);
			if (nodeService == null) {
				throw new SimulatorServiceException("There is no node with the simNodeId specified.");
			}
			
			BackgroundProcessEntryPoint bpep = nodeService.getNode().getBackgroundProcessEntryPoint(backgroundProcessKey);
			if (bpep == null) {
				throw new SimulatorServiceException("No background process for the specified key.");
			}
			return bpep.isRunning();
		}
	}
	
	@Override
	public void processBackgroundProcess(int simNodeId, String backgroundProcessKey) throws SimulatorServiceException {
		synchronized (this) {
			HyCubeNodeService nodeService = nodeServices.get(simNodeId);
			if (nodeService == null) {
				throw new SimulatorServiceException("There is no node with the simNodeId specified.");
			}
			
			BackgroundProcessEntryPoint bpep = nodeService.getNode().getBackgroundProcessEntryPoint(backgroundProcessKey);
			if (bpep == null) {
				throw new SimulatorServiceException("No background process for the specified key.");
			}
			bpep.processOnce();
		}
	}
	
	
	
	
	@Override
	public Object callExtension(int simNodeId, String extKey) throws SimulatorServiceException {
		synchronized (this) {
			HyCubeNodeService nodeService = nodeServices.get(simNodeId);
			if (nodeService == null) {
				throw new SimulatorServiceException("There is no node with the simNodeId specified.");
			}
			
			EntryPoint ep = nodeService.getNode().getExtensionEntryPoint(extKey);
			if (ep == null) {
				throw new SimulatorServiceException("No extension for the specified key.");
			}
			return ep.call();
		}
	}
	
	@Override
	public Object callExtension(int simNodeId, String extKey, Object arg) throws SimulatorServiceException {
		synchronized (this) {
			HyCubeNodeService nodeService = nodeServices.get(simNodeId);
			if (nodeService == null) {
				throw new SimulatorServiceException("There is no node with the simNodeId specified.");
			}
			
			EntryPoint ep = nodeService.getNode().getExtensionEntryPoint(extKey);
			if (ep == null) {
				throw new SimulatorServiceException("No extension for the specified key.");
			}
			return ep.call(arg);
		}
	}
	
	@Override
	public Object callExtension(int simNodeId, String extKey, Object[] args) throws SimulatorServiceException {
		synchronized (this) {
			HyCubeNodeService nodeService = nodeServices.get(simNodeId);
			if (nodeService == null) {
				throw new SimulatorServiceException("There is no node with the simNodeId specified.");
			}
			
			EntryPoint ep = nodeService.getNode().getExtensionEntryPoint(extKey);
			if (ep == null) {
				throw new SimulatorServiceException("No extension for the specified key.");
			}
			return ep.call(args);
		}
	}
	
	@Override
	public Object callExtension(int simNodeId, String extKey, Object entryPoint, Object[] args) throws SimulatorServiceException {
		synchronized (this) {
			HyCubeNodeService nodeService = nodeServices.get(simNodeId);
			if (nodeService == null) {
				throw new SimulatorServiceException("There is no node with the simNodeId specified.");
			}
			
			EntryPoint ep = nodeService.getNode().getExtensionEntryPoint(extKey);
			if (ep == null) {
				throw new SimulatorServiceException("No extension for the specified key.");
			}
			return ep.call(entryPoint, args);
		}
	}
	
	
	
	
	@Override
	public boolean put(int simNodeId, NodePointer recipient, BigInteger key, HyCubeResource resource, Object[] parameters) throws SimulatorServiceException {
		
		PutWaitCallback putCallback = new PutWaitCallback();
		
		synchronized (this) {
			HyCubeNodeService nodeService = nodeServices.get(simNodeId);
			if (nodeService == null) {
				throw new SimulatorServiceException("There is no node with the simNodeId specified.");
			}

			nodeService.put(recipient, key, resource, putCallback, null, parameters);
			
			boolean putResult;
			try {
				putResult = (Boolean) putCallback.waitPut(0);
			} catch (InterruptedException e) {
				putResult = false;
			}
			
			return putResult;
			
		}
	}
	
	
	@Override
	public boolean refreshPut(int simNodeId, NodePointer recipient, BigInteger key, HyCubeResourceDescriptor resourceDescriptor, Object[] parameters) throws SimulatorServiceException {

		RefreshPutWaitCallback refreshPutCallback = new RefreshPutWaitCallback();
		
		synchronized (this) {
			HyCubeNodeService nodeService = nodeServices.get(simNodeId);
			if (nodeService == null) {
				throw new SimulatorServiceException("There is no node with the simNodeId specified.");
			}

			nodeService.refreshPut(recipient, key, resourceDescriptor, refreshPutCallback, null, parameters);
			
			boolean refreshPutResult;
			try {
				refreshPutResult = (Boolean) refreshPutCallback.waitRefreshPut(0);
			} catch (InterruptedException e) {
				refreshPutResult = false;
			}
			
			return refreshPutResult;
			
		}
	}
	
	
	@Override
	public HyCubeResource[] get(int simNodeId, NodePointer recipient, BigInteger key, HyCubeResourceDescriptor criteria, Object[] parameters) throws SimulatorServiceException {

		GetWaitCallback getCallback = new GetWaitCallback();
		
		synchronized (this) {
			HyCubeNodeService nodeService = nodeServices.get(simNodeId);
			if (nodeService == null) {
				throw new SimulatorServiceException("There is no node with the simNodeId specified.");
			}

			nodeService.get(recipient, key, criteria, getCallback, null, parameters);
			
			HyCubeResource[] getResult;
			try {
				getResult = (HyCubeResource[]) getCallback.waitGet(0);
			} catch (InterruptedException e) {
				getResult = new HyCubeResource[]{};
			}
			
			return getResult;
			
		}
	}
	
	
	@Override
	public boolean delete(int simNodeId, NodePointer recipient, BigInteger key, HyCubeResourceDescriptor criteria, Object[] parameters) throws SimulatorServiceException {

		DeleteWaitCallback deleteCallback = new DeleteWaitCallback();
		
		synchronized (this) {
			HyCubeNodeService nodeService = nodeServices.get(simNodeId);
			if (nodeService == null) {
				throw new SimulatorServiceException("There is no node with the simNodeId specified.");
			}

			nodeService.delete(recipient, key, criteria, deleteCallback, null, parameters);
			
			boolean deleteResult;
			try {
				deleteResult = (Boolean) deleteCallback.waitDelete(0);
			} catch (InterruptedException e) {
				deleteResult = false;
			}
			
			return deleteResult;
			
		}
	}
	
	
	
	@Override
	public boolean isReplica(int simNodeId, BigInteger key, NodeId nodeId, int k) throws SimulatorServiceException {
		synchronized (this) {
			HyCubeNodeService nodeService = nodeServices.get(simNodeId);
			if (nodeService == null) {
				throw new SimulatorServiceException("There is no node with the simNodeId specified.");
			}
			return ((HyCubeDHTManagerEntryPoint)(nodeService.getNode().getDHTManagerEntryPoint())).isReplica(key, nodeId, k);
		}
	}
	
	
	
	
	@Override
	public void discard() {
		
		userLog.info("Discarding.");
		devLog.info("Discarding.");
		
		for (HyCubeNodeService ns : nodeServices.values()) {
			mulNodeService.discardNode(ns);
		}
		
		mulNodeService.discard();

		
		environment.discard();

		
		userLog.info("Discarded.");
		devLog.info("Discarded.");
		

		
		
	}
	
	
	
	@Override
	public void resetStats() throws SimulatorServiceException {
		
		synchronized(msgCountersLock) {
			msgSentCounter = 0;
			msgReceivedCounter = 0;
			msgDeliveredCounter = 0;
			msgLostCounter = 0;
		}
		

		environment.resetMessageStat();
		
		
	}
	
	
	
	@Override
	public void clear() throws SimulatorServiceException {
		
		synchronized(this) {
			
			for (HyCubeNodeService nodeService : nodeServices.values()) {
				mulNodeService.discardNode(nodeService);
			}
			
			nodeServices.clear();
			nodeIds.clear();
			msgQueues.clear();
			
			runGC();
			
		}
		
	}
	
	
	
	
	
	@Override
	public void runGC() {
		System.gc();
	}
	
	
	
	
	
	
	public void errorOccured(Object arg) {
		
	}
	
	
	
}

