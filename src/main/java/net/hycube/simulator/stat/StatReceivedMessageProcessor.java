package net.hycube.simulator.stat;

import java.util.List;

import net.hycube.core.InitializationException;
import net.hycube.core.NodeAccessor;
import net.hycube.environment.NodeProperties;
import net.hycube.environment.NodePropertiesConversionException;
import net.hycube.messaging.messages.HyCubeMessage;
import net.hycube.messaging.messages.HyCubeMessageType;
import net.hycube.messaging.messages.Message;
import net.hycube.messaging.processing.ProcessMessageException;
import net.hycube.messaging.processing.ReceivedMessageProcessor;
import net.hycube.transport.NetworkNodePointer;

public class StatReceivedMessageProcessor implements ReceivedMessageProcessor {

	protected static final String PROP_KEY_MESSAGE_STAT_EXTENSION_KEY = "MessageStatExtensionKey";
	protected static final String PROP_KEY_MESSAGE_TYPES = "MessageTypes";
	
	
	protected List<Enum<?>> messageTypes;
	
	protected NodeAccessor nodeAccessor;
	protected NodeProperties properties;
	
	
	protected String messageStatExtensionKey;
	protected MessageStatExtension messageStatExtension;
	
	
	
	@Override
	public void initialize(NodeAccessor nodeAccessor, NodeProperties properties) throws InitializationException {

		this.nodeAccessor = nodeAccessor;
		this.properties = properties;
		
		
		try {
			//load message types processed by this message processor:
			this.messageTypes = properties.getEnumListProperty(PROP_KEY_MESSAGE_TYPES, HyCubeMessageType.class);
			if (this.messageTypes == null) throw new InitializationException(InitializationException.Error.INVALID_PARAMETER_VALUE, properties.getAbsoluteKey(PROP_KEY_MESSAGE_TYPES), "Invalid parameter value: " + properties.getAbsoluteKey(PROP_KEY_MESSAGE_TYPES) + ".");

			//get the ack extension
			messageStatExtensionKey = properties.getProperty(PROP_KEY_MESSAGE_STAT_EXTENSION_KEY);
			if (messageStatExtensionKey == null || messageStatExtensionKey.trim().isEmpty()) throw new InitializationException(InitializationException.Error.INVALID_PARAMETER_VALUE, properties.getAbsoluteKey(PROP_KEY_MESSAGE_STAT_EXTENSION_KEY), "Invalid parameter value: " + properties.getAbsoluteKey(PROP_KEY_MESSAGE_STAT_EXTENSION_KEY));
			try {
				messageStatExtension = (MessageStatExtension) nodeAccessor.getExtension(messageStatExtensionKey);
				if (this.messageStatExtension == null) throw new InitializationException(InitializationException.Error.MISSING_EXTENSION_ERROR, this.messageStatExtensionKey, "The MessageStatExtension is missing at the specified key: " + this.messageStatExtensionKey + ".");
			} catch (ClassCastException e) {
				throw new InitializationException(InitializationException.Error.MISSING_EXTENSION_ERROR, this.messageStatExtensionKey, "The MessageStatExtension is missing at the specified key: " + this.messageStatExtensionKey + ".");
			}


			
		} catch (NodePropertiesConversionException e) {
			throw new InitializationException(InitializationException.Error.NODE_INITIALIZATION_ERROR, null, "Unable to initialize received message processor instance. Invalid parameter value: " + e.getKey() + ".", e);
		}
		
		
	}

	@Override
	public boolean processMessage(Message message, NetworkNodePointer directSender) throws ProcessMessageException {
		
		HyCubeMessage msg = (HyCubeMessage)message;
		
		if (! messageTypes.contains(msg.getType())) return true;
		
		try {
			switch (msg.getType()) {
				case DATA:
					processDataMessage(msg);
					break;
				default:
					break;
			}
		}
		catch (Exception e) {
			throw new ProcessMessageException("An exception thrown while processing a message.", e);
		}

		return true;
		
	}

	protected void processDataMessage(HyCubeMessage msg) {
		
		//if this is a data message and it just reached its destination node, update the counters:
		
		if (msg.getRecipientId().equals(nodeAccessor.getNodeId())) {
			this.messageStatExtension.incrementMsgDeliveredCounter();
			this.messageStatExtension.updateRouteLengthSum(msg.getHopCount());
		
		}
		
		
	}

	
	@Override
	public void discard() {
		
	}

}
