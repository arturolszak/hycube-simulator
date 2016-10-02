package net.hycube.simulator.stat;

import java.util.List;

import net.hycube.core.InitializationException;
import net.hycube.core.NodeAccessor;
import net.hycube.environment.NodeProperties;
import net.hycube.environment.NodePropertiesConversionException;
import net.hycube.messaging.data.DataMessageSendProcessInfo;
import net.hycube.messaging.messages.HyCubeMessage;
import net.hycube.messaging.messages.HyCubeMessageType;
import net.hycube.messaging.processing.MessageSendProcessInfo;
import net.hycube.messaging.processing.MessageSendProcessor;
import net.hycube.messaging.processing.ProcessMessageException;

public class StatMessageSendProcessor implements MessageSendProcessor {

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
	public boolean processSendMessage(MessageSendProcessInfo mspi) throws ProcessMessageException {
		
		HyCubeMessage msg = (HyCubeMessage)mspi.getMsg();

		if (! messageTypes.contains(msg.getType())) return true;
		
		try {
			switch (msg.getType()) {
				case DATA:
					processSendDataMessage(mspi);
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

	protected void processSendDataMessage(MessageSendProcessInfo mspi) {
		
		DataMessageSendProcessInfo dmspi = (DataMessageSendProcessInfo)mspi;
		
		//if this is a data message and this is the sending node, update the counters:
		
		if (mspi.getMsg().getSenderId().equals(nodeAccessor.getNodeId()) && mspi.getMsg().getHopCount() == 0 && ( ! dmspi.isResent())) {
			this.messageStatExtension.incrementMsgSentCounter();
		}
		
		
			

		
	}

	@Override
	public void discard() {
		
	}

}
