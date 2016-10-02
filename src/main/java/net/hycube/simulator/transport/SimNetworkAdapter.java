/**
 * 
 */
package net.hycube.simulator.transport;

import net.hycube.core.NodeAccessor;
import net.hycube.environment.NodeProperties;
import net.hycube.messaging.messages.Message;
import net.hycube.simulator.environment.SimEnvironment;
import net.hycube.simulator.log.LogHelper;
import net.hycube.transport.NetworkAdapter;
import net.hycube.transport.NetworkAdapterException;
import net.hycube.transport.NetworkAdapterRuntimeException;
import net.hycube.transport.NetworkNodePointer;
import net.hycube.transport.ReceivedMessageProcessProxy;
import net.hycube.utils.StringUtils;

/**
 * @author Artur Olszak
 *
 */
public class SimNetworkAdapter implements NetworkAdapter {

	private static org.apache.commons.logging.Log userLog = LogHelper.getUserLog();
	private static org.apache.commons.logging.Log msgLog = LogHelper.getMessagesLog();
	private static org.apache.commons.logging.Log devLog = LogHelper.getDevLog(SimNetworkAdapter.class); 
	
	protected boolean initialized = false;
	
	protected String addressString;
	protected byte[] addressBytes;
	protected SimNodePointer networkNodePointer;
	
	protected String interfaceAddressString;
	protected byte[] interfaceAddressBytes;
	protected SimNodePointer interfaceNetworkNodePointer;
	
	protected String simId;
	protected String simNodeId;
	protected SimNetworkProxy simNetworkProxy;
	protected ReceivedMessageProcessProxy receivedMessageProcessProxy;
	
	protected NodeAccessor nodeAccessor;
	protected NodeProperties properties;
	
	
	
	
	public boolean isInitialized() {
		return initialized;
	}

	
	public String getInterfaceAddressString() {
		return interfaceAddressString;
	}
	
	public byte[] getInterfaceAddressBytes() {
		return interfaceAddressBytes;
	}
	
	public NetworkNodePointer getInterfaceNetworkNodePointer() {
		return interfaceNetworkNodePointer;
	}

	
	
	
	public String getPublicAddressString() {
		return addressString;
	}
	
	public byte[] getPublicAddressBytes() {
		return addressBytes;
	}
	
	public NetworkNodePointer getPublicNetworkNodePointer() {
		return networkNodePointer;
	}
	
	
	
	public ReceivedMessageProcessProxy getReceivedMessageProcessProxy() {
		return receivedMessageProcessProxy;
	}
	
	public SimNetworkProxy getNetworkProxy() {
		return simNetworkProxy;
	}
	
	@Override
	public void initialize(String networkAddress, ReceivedMessageProcessProxy receivedMessageProcessProxy, NodeAccessor nodeAccessor, NodeProperties properties) throws NetworkAdapterRuntimeException {
		String[] addrSplit = validateNetworkAddress(networkAddress);
		if (addrSplit == null) {
			throw new IllegalArgumentException("An exception was thrown while initializing the network adapter. Incorrect address specified.");
		}
		initializeValidated(addrSplit, receivedMessageProcessProxy, nodeAccessor, properties);
	}
	
	public void initialize(String simId, String simNodeId, ReceivedMessageProcessProxy receivedMessageProcessProxy, NodeAccessor nodeAccessor, NodeProperties properties) throws NetworkAdapterRuntimeException {
		String[] addrSplit = validateNetworkAddress(simId, simNodeId);
		if (addrSplit == null) {
			throw new IllegalArgumentException("An exception was thrown while initializing the network adapter. Incorrect address specified.");
		}
		initializeValidated(addrSplit, receivedMessageProcessProxy, nodeAccessor, properties);
	}
	
	protected void initializeValidated(String[] addrSplit, ReceivedMessageProcessProxy receivedMessageProcessProxy, NodeAccessor nodeAccessor, NodeProperties properties) throws NetworkAdapterRuntimeException {
		
		if (devLog.isInfoEnabled()) {
			devLog.info("Initializing network adapter.");
		}
		
		this.nodeAccessor = nodeAccessor;
		this.properties = properties;
		
		this.addressString = StringUtils.join(addrSplit, ":");
		this.simId = addrSplit[0];
		this.simNodeId = addrSplit[1];
		
		this.networkNodePointer = this.createNetworkNodePointer(this.addressString);
		
		this.addressBytes = this.networkNodePointer.getAddressBytes();
		
		
		this.interfaceAddressString = this.addressString;
		this.interfaceAddressBytes = this.addressBytes;
		this.interfaceNetworkNodePointer = this.networkNodePointer;
		
		
		
		
		if (!(nodeAccessor.getEnvironment() instanceof SimEnvironment)) throw new NetworkAdapterRuntimeException("The SimNetworkAdapter may be created only for a node running in a SimEnvironment.");
		this.simNetworkProxy = ((SimEnvironment)nodeAccessor.getEnvironment()).getSimNetworkProxy();
		this.receivedMessageProcessProxy = receivedMessageProcessProxy;
			
		this.initialized = true;
		
		if (userLog.isInfoEnabled()) {
			userLog.info("Initialized network adapter.");
		}
		if (devLog.isInfoEnabled()) {
			devLog.info("Initialized network adapter.");
		}
		
	}
	
	
	
	public void setPublicAddress(String addressString) {
		String[] addSplit = validateNetworkAddress(addressString);
		if (addSplit == null) {
			throw new IllegalArgumentException("Invalid network address specified.");
		}
		
		
		this.networkNodePointer = this.createNetworkNodePointer(addressString);
		
		this.addressString = addressString;
				
		this.addressBytes = this.networkNodePointer.getAddressBytes();
		
		this.nodeAccessor.getNodePointer().setNetworkNodePointer(this.networkNodePointer);
		
	}
	
	public void setPublicAddress(byte[] addressBytes) {
		String[] addSplit = validateNetworkAddress(addressString);
		if (addSplit == null) {
			throw new IllegalArgumentException("Invalid network address specified.");
		}
		
		
		this.networkNodePointer = this.createNetworkNodePointer(addressBytes);
		
		this.addressString = this.networkNodePointer.getAddressString();
				
		this.addressBytes = addressBytes;
		
		this.nodeAccessor.getNodePointer().setNetworkNodePointer(this.networkNodePointer);
		
	}
	
	public void setPublicAddress(NetworkNodePointer networkNodePointer) {
		String[] addSplit = validateNetworkAddress(addressString);
		if (addSplit == null) {
			throw new IllegalArgumentException("Invalid network address specified.");
		}
		
		if ( ! (networkNodePointer instanceof SimNodePointer)) {
			throw new IllegalArgumentException("Invalid network address specified. The network node pointer is expected to be an instance of: " + SimNodePointer.class.getName());
		}
		
		
		this.networkNodePointer = (SimNodePointer) networkNodePointer;
		
		this.addressString = this.networkNodePointer.getAddressString();
				
		this.addressBytes = this.networkNodePointer.getAddressBytes();
		
		this.nodeAccessor.getNodePointer().setNetworkNodePointer(this.networkNodePointer);
		
	}
	
	
	
		
	@Override
	public void sendMessage(Message msg, NetworkNodePointer np) throws NetworkAdapterException {
		
		if (!(np instanceof SimNodePointer)) {
			throw new IllegalArgumentException("The parameter nodePointer specified should be an instance of SimNodePointer.");
		}
		
		if (!initialized) throw new NetworkAdapterException("The network adapter is not initialized.");
		
		if (devLog.isDebugEnabled()) {
			devLog.debug("Sending message #" + msg.getSerialNoAndSenderString() + " to " + np.getAddressString());
		}
		if (msgLog.isInfoEnabled()) {
			msgLog.info("Sending message #" + msg.getSerialNoAndSenderString() + " to " + np.getAddressString());
		}
		
		//SimNodePointer simNodePointer = (SimNodePointer) np;
		
		SimMessage simMsg = new SimMessage(msg.getBytes(), this.addressString, np.getAddressString());
		
		try {
			this.simNetworkProxy.sendMessage(simMsg);
		} catch (SimNetworkProxyException e) {
			throw new NetworkAdapterException("An exception thrown while sending the message to the network proxy object.", e);
		}
		
	}

	@Override
	public void messageReceived(Message msg, NetworkNodePointer directSender) {
		if (devLog.isDebugEnabled()) {
			devLog.debug("Passing the received message to the node.");
		}
		receivedMessageProcessProxy.messageReceived(msg, directSender);
	}
	
	@Override
	public SimNodePointer createNetworkNodePointer(String address) {
		return new SimNodePointer(address);
	}

	@Override
	public SimNodePointer createNetworkNodePointer(byte[] addressBytes) {
		return new SimNodePointer(addressBytes);
	}

	
	
	@Override
	public String[] validateNetworkAddress(String networkAddress) {
		return SimNodePointer.validateNetworkAddress(networkAddress);
	}

	@Override
	public Object validateNetworkAddress(byte[] networkAddressBytes) {
		return SimNodePointer.validateNetworkAddress(networkAddressBytes);
	}
	
	public String[] validateNetworkAddress(String simId, String simNodeId) {
		return SimNodePointer.validateNetworkAddress(simId, simNodeId);
	}
	
	
	
	@Override
	public int getAddressByteLength() {
		return SimNodePointer.getAddressByteLength();
	}
	
	
	
	@Override
	public long getProximity(NetworkNodePointer np) {
		
		//calculate the simulated proximity based on the hamming distance between the two strings representing sim node ids
		
		if (!(np instanceof SimNodePointer)) throw new IllegalArgumentException("The parameter specified should be an instance of SimNodePointer.");
		
		SimNodePointer snp = (SimNodePointer)np;
		
		String n1 = this.simNodeId;
		String n2 = snp.getSimNodeId();
		
		int proximity = StringUtils.getHammingDistance(n1, n2);
		
		
		return proximity;
		
		
	}

	@Override
	public void discard() throws NetworkAdapterException {

		if (devLog.isInfoEnabled()) {
			devLog.info("Discarding the network adapter.");
		}
		
		this.initialized = false;
		
		properties = null;
		addressString = null;
		simId = null;
		simNodeId = null;
		simNetworkProxy = null;
		receivedMessageProcessProxy = null;

		if (userLog.isInfoEnabled()) {
			userLog.info("Discarded the network adapter.");
		}
		if (devLog.isInfoEnabled()) {
			devLog.info("Discarded the network adapter.");
		}
		
	}

	
	@Override
	public int getMaxMessageLength() {
		return 0;
	}

	@Override
	public boolean isFragmentMessages() {
		return false;
	}
	
	@Override
	public int getMessageFragmentLength() {
		return 0;
	}
	
	@Override
	public int getMaxMassageFragmentsCount() {
		return 0;
	}

	
	

}
