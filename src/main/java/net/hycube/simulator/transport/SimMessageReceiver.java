package net.hycube.simulator.transport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import net.hycube.configuration.GlobalConstants;
import net.hycube.core.InitializationException;
import net.hycube.core.NodeParameterSet;
import net.hycube.environment.Environment;
import net.hycube.environment.NodeProperties;
import net.hycube.eventprocessing.Event;
import net.hycube.eventprocessing.EventCategory;
import net.hycube.messaging.messages.Message;
import net.hycube.messaging.messages.MessageByteConversionException;
import net.hycube.messaging.messages.MessageFactory;
import net.hycube.simulator.environment.SimEnvironment;
import net.hycube.simulator.log.LogHelper;
import net.hycube.transport.MessageReceiver;
import net.hycube.transport.MessageReceiverException;
import net.hycube.transport.MessageReceiverProcessEventProxy;
import net.hycube.transport.MessageReceiverRuntimeException;
import net.hycube.transport.NetworkAdapter;
import net.hycube.transport.NetworkNodePointer;
import net.hycube.utils.ClassInstanceLoadException;
import net.hycube.utils.ClassInstanceLoader;

public class SimMessageReceiver implements MessageReceiver {

	private static org.apache.commons.logging.Log userLog = LogHelper.getUserLog();
	private static org.apache.commons.logging.Log devLog = LogHelper.getDevLog(SimMessageReceiver.class); 
	private static org.apache.commons.logging.Log msgLog = LogHelper.getMessagesLog();
	
	public static final int RECEIVE_TIMEOUT_MS = 100;
	
	protected NodeProperties properties;
	protected MessageFactory messageFactory;
	protected HashMap<String, NetworkAdapter> networkAdapters;
	protected SimNetworkProxy networkProxy;
	protected List<String> addresses;
	protected BlockingQueue<Event> receiveEventQueue;	
	protected boolean initialized = false;
	protected SimEnvironment environment;
	
    protected boolean hold = false;
    protected boolean wasHeld = false;
    protected int wasHeldNum = 0;
    protected Object holdLock = new Object();
    
    protected boolean wakeable;
    protected Object wakeableLock = new Object();
    
    protected Object recvLock = new Object();
    
    protected MessageReceiverProcessEventProxy messageReceiverProcessEventProxy;
	
	
	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public synchronized void initialize(Environment environment, BlockingQueue<Event> receiveEventQueue, NodeProperties properties) throws InitializationException {
		
		if (devLog.isInfoEnabled()) {
			devLog.info("Initializing message receiver.");
		}
		
		if (receiveEventQueue == null) {
			throw new IllegalArgumentException("receiveEventQueue is null.");
		}
		
		if (environment == null) {
			throw new IllegalArgumentException("environment is null.");
		}
		
		if (!(environment instanceof SimEnvironment)) {
			throw new IllegalArgumentException("environment must be an instance of SimEnvironment.");
		}
		
		this.properties = properties;
		
		this.networkAdapters = new HashMap<String, NetworkAdapter>();
		this.addresses = new ArrayList<String>();
		
		this.environment = (SimEnvironment) environment;
		
		this.networkProxy = this.environment.getSimNetworkProxy();
		if (networkProxy == null) {
			throw new IllegalArgumentException("networkProxy is null.");
		}
		
		this.receiveEventQueue = receiveEventQueue;
		
		this.messageReceiverProcessEventProxy = new MessageReceiverProcessEventProxy(this);
		
		
		//Message factory:
		try {
			String messageFactoryKey = properties.getProperty(NodeParameterSet.PROP_KEY_MESSAGE_FACTORY);
			if (messageFactoryKey == null || messageFactoryKey.trim().isEmpty()) throw new InitializationException(InitializationException.Error.INVALID_PARAMETER_VALUE, properties.getAbsoluteKey(NodeParameterSet.PROP_KEY_MESSAGE_FACTORY), "Invalid parameter value: " + properties.getAbsoluteKey(NodeParameterSet.PROP_KEY_MESSAGE_FACTORY));
			NodeProperties messageFactoryProperties = properties.getNestedProperty(NodeParameterSet.PROP_KEY_MESSAGE_FACTORY, messageFactoryKey);
			String messageFactoryClass = messageFactoryProperties.getProperty(GlobalConstants.PROP_KEY_CLASS);

			messageFactory = (MessageFactory) ClassInstanceLoader.newInstance(messageFactoryClass, MessageFactory.class);
			messageFactory.initialize(messageFactoryProperties);
		} catch (ClassInstanceLoadException e) {
			throw new InitializationException(InitializationException.Error.CLASS_INSTANTIATION_ERROR, e.getLoadedClassName(), "Unable to create message factory instance.", e);
		}
		
		
		
		this.initialized = true;
		
		if (userLog.isInfoEnabled()) {
			userLog.info("Initialized message receiver.");
		}
		if (devLog.isInfoEnabled()) {
			devLog.info("Initialized message receiver.");
		}
	}
	
	@Override
	public synchronized void registerNetworkAdapter(NetworkAdapter networkAdapter) throws MessageReceiverException {
		if (networkAdapter instanceof SimNetworkAdapter) {
			registerNetworkAdapter((SimNetworkAdapter)networkAdapter);
		}
		else {
			throw new IllegalArgumentException("The network adapter should be an instance of SimNetworkAdapter class.");
		}
	}
	
	public synchronized void registerNetworkAdapter(SimNetworkAdapter networkAdapter) {
		
		if (devLog.isInfoEnabled()) {
			devLog.info("Registering new network adapter.");
		}
		
		if (!initialized) throw new MessageReceiverRuntimeException("The message receiver is not initialized.");
		
		hold();	//after hold() call, no new selections will be made

		synchronized(recvLock) {	//waits for the current receive to finish and does not allow receive meanwhile
			unhold();	//hold is no longer needed, recvLock is acquired
			
			if (networkAdapters.containsKey(networkAdapter.getPublicAddressString())) {
				throw new MessageReceiverRuntimeException("The message receiver already registered a network adapter with the same network address.");
			}
			
			this.networkAdapters.put(networkAdapter.getPublicAddressString(), networkAdapter);
			this.addresses.add(networkAdapter.getPublicAddressString());
		}
	
		if (userLog.isInfoEnabled()) {
			userLog.info("Registered new network adapter. Network address: " + networkAdapter.getPublicAddressString());
		}
		if (devLog.isInfoEnabled()) {
			devLog.info("Registered new network adapter. Network address: " + networkAdapter.getPublicAddressString());
		}
		
	}

	@Override
	public synchronized void unregisterNetworkAdapter(NetworkAdapter networkAdapter) {
		if (networkAdapter instanceof SimNetworkAdapter) {
			unregisterNetworkAdapter((SimNetworkAdapter)networkAdapter);
		}
		else {
			throw new IllegalArgumentException("The network adapter should be an instance of SimNetworkAdapter class.");
		}
	}
	
	public synchronized void unregisterNetworkAdapter(SimNetworkAdapter networkAdapter) {
		if (devLog.isInfoEnabled()) {
			devLog.info("Unregistering new network adapter.");
		}
		
		if (!initialized) throw new MessageReceiverRuntimeException("The message receiver is not initialized.");
		
		hold();	//after hold() call, no new selections will be made

		synchronized(recvLock) {	//waits for the current receive to finish and does not allow receive meanwhile
			unhold();	//hold is no longer needed, recvLock is acquired
			
			if (! this.networkAdapters.containsKey(networkAdapter.getPublicAddressString())) {
				//do nothing - this network adapter is not registered for this instance of network receiver
				return;
			}
			
			this.networkAdapters.remove(networkAdapter.getPublicAddressString());
			this.addresses.remove(networkAdapter.getPublicAddressString());
		}
		
		if (userLog.isInfoEnabled()) {
			userLog.info("Unregistered new network adapter. Network address: " + networkAdapter.getPublicAddressString());
		}
		if (devLog.isInfoEnabled()) {
			devLog.info("Unregistered new network adapter. Network address: " + networkAdapter.getPublicAddressString());
		}
		
	}
	
	@Override
	public void receiveMessage() throws MessageReceiverException {
		
		if (devLog.isTraceEnabled()) {
			devLog.trace("receiveMessage() called.");
		}
		
		if (!initialized) throw new MessageReceiverRuntimeException("The message receiver is not initialized.");
		
		SimMessage simMsg = null;
		
		boolean block = true;
		
		synchronized(recvLock) {
		
			//check again if initialized (initialization synchronizes on recvLock)
			if (!isInitialized()) return;
			
			if (checkHoldAndSetHeld()) {
				//return, new events will not be enqueued, the following unhold() call will enqueue the messagereceiver again
				return;
			}
			
			do {
				
				if (devLog.isDebugEnabled()) {
					devLog.debug("Checking for message - calling receiveMessage.");
				}
				
				try {
					//only one message will be received in a blocking mode, and then all the immediately available messages will be received without waiting
					if (block) {
						simMsg = this.networkProxy.receiveMessage(RECEIVE_TIMEOUT_MS);
					}
					else {
						simMsg = this.networkProxy.receiveMessageNow();
					}
				}
				catch (SimNetworkProxyException e) {
					throw new MessageReceiverException("An exception thrown while receiving a message from the network proxy object.", e);
				}
				
				if (simMsg != null) {
					String address = simMsg.getRecipientAddress();
					String senderAddress = simMsg.getSenderAddress();
					
					NetworkAdapter networkAdapter;

					//enqueue the message:
					networkAdapter = networkAdapters.get(address);

					if (networkAdapter == null) {
						//queue not defined for this sim id
						devLog.debug("Message receiver received a message for the network address for which the networkAdapter was not registered.");
						//throw new MessageReceiverException("Message receiver received a message for the network address for which the networkAdapter was not registered.");
						block = false;
						continue;
					}
					
					
					Message message = null;
					try {
						message = messageFactory.fromBytes(simMsg.getMessageBytes());
					} catch (MessageByteConversionException e) {
						if (msgLog.isDebugEnabled()) {
							msgLog.debug("Invalid message - could not convert to the Message object. Message discarded.", e);
						}
						if (devLog.isDebugEnabled()) {
							devLog.debug("Invalid message - could not convert to the Message object. Message discarded.", e);
						}
						//the message is invalid, discard -> do nothing
						block = false;
						continue;
					}
					
					
					if (devLog.isDebugEnabled()) {
						devLog.debug("Received message: " + message.getSerialNoAndSenderString());
					}
					if (msgLog.isDebugEnabled()) {
						msgLog.debug("Received message: " + message.getSerialNoAndSenderString());
					}
					
					if (devLog.isDebugEnabled()) {
						devLog.debug("Passing the received message to the network adapter.");
    				}
					
					NetworkNodePointer senderNodePointer = networkAdapter.createNetworkNodePointer(senderAddress);
					
					networkAdapters.get(address).messageReceived(message, senderNodePointer);

				}
				else {
					if (devLog.isTraceEnabled()) {
						devLog.trace("An empty object returned by networkProxy.receiveMessage() or receiveMessageNow().");
					}
				}
			
				block = false;	//wait once, and then get all the immediately available messages without waiting
				
			}
			while (simMsg != null);
		
			
			enqueueMessageReceiverEvent();
			
		}
		
	}

	
	public void startMessageReceiver() {
		enqueueMessageReceiverEvent();
	}
	
	public void startMessageReceiver(int numEventsToEnqueue) {
		if (numEventsToEnqueue <= 0) {
			throw new IllegalArgumentException("Illegal number of events to be enqueued.");
		}
		for (int i = 0; i < numEventsToEnqueue; i++) {
			enqueueMessageReceiverEvent();
		}
	}
	
	protected void enqueueMessageReceiverEvent() {
		
		boolean enqueued = false;
		while (!enqueued) {
			try {
				this.receiveEventQueue.put(new Event(environment.getTimeProvider().getCurrentTime(), EventCategory.receiveMessageEvent, messageReceiverProcessEventProxy, null));
				enqueued = true;
			} catch (InterruptedException e) {
				//do nothing, the put will be retried (enqueued is still false) 
			}
		}
	}
	
	
	@Override
	public synchronized void discard() {
		
		if (devLog.isInfoEnabled()) {
			devLog.info("Discarding the message receiver.");
		}
		
		hold();	//after hold() call, no new selections will be made

		synchronized(recvLock) {	//waits for the current receive to finish and does not allow receive meanwhile
			//unhold();	//hold is no longer needed, recvLock is acquired
		
			this.initialized = false;
		
			networkAdapters = null;
			networkProxy = null;
			addresses = null;
			receiveEventQueue = null;
			environment = null;
			
			messageReceiverProcessEventProxy = null;
			
			properties = null;
			
		}

		if (userLog.isInfoEnabled()) {
			userLog.info("Discarded the message receiver.");
		}
		if (devLog.isInfoEnabled()) {
			devLog.info("Discarded the message receiver.");
		}
		
	}
	
	

	
	
    protected void hold() {
    	synchronized(holdLock) {
    		hold = true;
    	}
    }
	
    protected void unhold() {
    	synchronized(holdLock) {
    		hold = false;
    		if (wasHeld) {
    			wasHeld = false;
    			while (wasHeldNum > 0) {
    				enqueueMessageReceiverEvent();
    				wasHeldNum--;
    			}
    		}
    		
    	}
    }
    
    protected boolean checkHoldAndSetHeld() {
    	synchronized(holdLock) {
    		if (hold) {
    			wasHeld = true;
    			wasHeldNum++;
    			synchronized (wakeableLock) {
    				this.wakeable = false;	//will not block on select()
    			}
    			return true;
    		}
    		else return false;
    	}
    }

}
