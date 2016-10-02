package net.hycube.simulator.transport.jms;

import java.io.InterruptedIOException;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;

import net.hycube.core.InitializationException;
import net.hycube.simulator.transport.SimMessage;
import net.hycube.simulator.transport.SimNetworkProxyException;
import net.hycube.simulator.transport.SimWakeableNetworkProxy;



public class JMSActiveMQSimWakeableNetworkProxy extends JMSActiveMQSimNetworkProxy implements SimWakeableNetworkProxy {

	
	protected boolean wakeupFlag;
	
	
	
	@Override
	public synchronized void initialize(String jmsUrl, String msgQueueName) throws InitializationException {
		
		super.initialize(jmsUrl, msgQueueName);
		
		this.wakeupFlag = false;
		
//		try {
//			
//			this.wakeupSession = connection.createSession(transacted, ackMode);
//			
//			this.wakeupProducer = wakeupSession.createProducer(msgQueue);
//			this.wakeupProducer.setDeliveryMode(DELIVERY_MODE);
//			
//		} catch (JMSException e) {
//			throw new InitializationException("A JMS exception has been thrown while initializing the message consumer.", e);
//		}
		
		
	}
	
	
	
	@Override
	public void wakeup() throws SimNetworkProxyException {

		//not sychronized so that it does not wait till the received returns
		this.wakeupFlag = true;
		
		//at most one thread is between the check of this flag and receive call (synchronized block)
		//so AT MOST one thread will call (possibly blocking) receive 
		//and this thread will wake up either by a message that is already in the queue or by the wakeup message sent below
		//no other thread will consume that message
		
		//send a wakeup message to the queue that will interrupt the receive call
		synchronized (wakeupSession) {
			//an empty message will be used to wake up the consumer.receive call
			try {
				Message wakeupMessage = wakeupSession.createMessage();
				wakeupMessage.setBooleanProperty(PROP_NAME_WAKEUP_MESSAGE, true);
				wakeupProducer.send(wakeupMessage);
			} catch (JMSException e) {
				throw new SimNetworkProxyException("A JMSException has been thrown while sendng a wakeup message to the local messages queue.", e);
			}
			
			
		}
		
		

		
	}

	@Override
	public void clearWakeupFlag() {
		
		synchronized(this) {
			this.wakeupFlag = false;
		}

		
	}
	
	
	
	@Override
	public SimMessage receiveMessage() throws SimNetworkProxyException {
		
		
		Message msg = null;
		try {
			synchronized (this) {
				//if wakeup was called, receive should not be called
				if (this.wakeupFlag) return null;			
				synchronized (this.session) {
					msg = this.consumer.receive();
				}
				
			}

		} catch (JMSException e) {
			if (e.getCause() instanceof InterruptedException || e.getCause() instanceof InterruptedIOException) {
				//just return, the processing has finished
				return null;
			}
			else {
				throw new SimNetworkProxyException("A JMS exception has been thrown while receiving a message.", e);
			}
		}
		
		SimMessage simMessage = unpackJMSMessage(msg);
		
		if (simMessage != null) {
			receivedCounter++;
		}
		
		
		return simMessage;
		
	}

	@Override
	public SimMessage receiveMessage(long timeout) throws SimNetworkProxyException {
		
		Message msg = null;
		try {
			//if wakeup was called, receive should not be called
			if (this.wakeupFlag) return null;			
			synchronized (this.session) {
				msg = this.consumer.receive(timeout);
			}

		} catch (JMSException e) {
			if (e.getCause() instanceof InterruptedException || e.getCause() instanceof InterruptedIOException) {
				//just return, the processing has finished
				return null;
			}
			else {
				throw new SimNetworkProxyException("A JMS exception has been thrown while receiving a message.", e);
			}
		}
		
		SimMessage simMessage = unpackJMSMessage(msg);

		if (simMessage != null) {
			receivedCounter++;
		}
		
		
		return simMessage;
		
	}

	@Override
	public SimMessage receiveMessageNow() throws SimNetworkProxyException {
		Message msg = null;
		try {
			//if wakeup was called, receive should not be called
			if (this.wakeupFlag) return null;			
			synchronized (this.session) {
				msg = this.consumer.receiveNoWait();
			}

		} catch (JMSException e) {
			if (e.getCause() instanceof InterruptedException || e.getCause() instanceof InterruptedIOException) {
				//just return, the processing has finished
				return null;
			}
			else {
				throw new SimNetworkProxyException("A JMS exception has been thrown while receiving a message.", e);
			}
		}
		
		SimMessage simMessage = unpackJMSMessage(msg);
		
		if (simMessage != null) {
			receivedCounter++;
		}
		
		return simMessage;
	}

	
	protected SimMessage unpackJMSMessage(Message msg) throws SimNetworkProxyException {
		
		if (msg == null) return null;

		try { 
			if (msg.propertyExists(PROP_NAME_WAKEUP_MESSAGE) && msg.getBooleanProperty(PROP_NAME_WAKEUP_MESSAGE) == true) {
				//this is a wakeup message, that should just be ignored
				return null;
			}
		} catch (JMSException e) {
			throw new SimNetworkProxyException("An exception has been thrown while unpacking a sim message from the JMS bytes message.", e);
		}
		
		if ( ! (msg instanceof BytesMessage)) throw new SimNetworkProxyException("The JMS message received is not an instance of " + BytesMessage.class.getName());
		
		BytesMessage bytesMsg = (BytesMessage) msg;
		
		//unpack the sim message from the JMS bytes message:
		SimMessage simMessage = null;
		try {
			
			if (CONVERT_INT_TO_BYTE_ARRAY_BEFORE_SEND) {
				//!!converted to byte array, writeUTF is heavily memory-consuming, writeInt slightly less
//				String senderAddress = bytesMsg.readUTF();
//				String recipientAddress = bytesMsg.readUTF();
				
//				int senderAddressBLength = bytesMsg.readInt();
				byte[] senderAddressLengthB = new byte[Integer.SIZE/8];
				bytesMsg.readBytes(senderAddressLengthB);
				int senderAddressBLength = 0;
				for (int i = 0; i < senderAddressLengthB.length; i++) senderAddressBLength = senderAddressBLength | ((((int)senderAddressLengthB[i]) << 8 * (senderAddressLengthB.length - 1 - i)) & (0xFF << 8 * (senderAddressLengthB.length - 1 - i)));
				byte[] senderAddressB = new byte[senderAddressBLength];
				bytesMsg.readBytes(senderAddressB);
				String senderAddress = new String(senderAddressB, SIM_ADDRESS_CHARSET);
				
//				int recipientAddressBLength = bytesMsg.readInt();
				byte[] recipientAddressLengthB = new byte[Integer.SIZE/8];
				bytesMsg.readBytes(recipientAddressLengthB);
				int recipientAddressBLength = 0;
				for (int i = 0; i < recipientAddressLengthB.length; i++) recipientAddressBLength = recipientAddressBLength | ((((int)recipientAddressLengthB[i]) << 8 * (recipientAddressLengthB.length - 1 - i)) & (0xFF << 8 * (recipientAddressLengthB.length - 1 - i)));
				byte[] recipientAddressB = new byte[recipientAddressBLength];
				bytesMsg.readBytes(recipientAddressB);
				String recipientAddress = new String(recipientAddressB, SIM_ADDRESS_CHARSET);
				
//				int msgByteLength = bytesMsg.readInt();
				byte[] msgByteLengthB = new byte[Integer.SIZE/8];
				bytesMsg.readBytes(msgByteLengthB);
				int msgByteLength = 0;
				for (int i = 0; i < msgByteLengthB.length; i++) msgByteLength = msgByteLength | ((((int)msgByteLengthB[i]) << 8 * (msgByteLengthB.length - 1 - i)) & (0xFF << 8 * (msgByteLengthB.length - 1 - i)));
				byte[] msgBytes = new byte[msgByteLength]; 
				bytesMsg.readBytes(msgBytes);
				
				simMessage = new SimMessage(msgBytes, senderAddress, recipientAddress);
				
			}
			else {
				//!!converted to byte array, writeUTF is heavily memory-consuming
	//			String senderAddress = bytesMsg.readUTF();
	//			String recipientAddress = bytesMsg.readUTF();
				
				int senderAddressBLength = bytesMsg.readInt();
				byte[] senderAddressB = new byte[senderAddressBLength];
				bytesMsg.readBytes(senderAddressB);
				String senderAddress = new String(senderAddressB, SIM_ADDRESS_CHARSET);
				
				int recipientAddressBLength = bytesMsg.readInt();
				byte[] recipientAddressB = new byte[recipientAddressBLength];
				bytesMsg.readBytes(recipientAddressB);
				String recipientAddress = new String(recipientAddressB, SIM_ADDRESS_CHARSET);
				
				int msgByteLength = bytesMsg.readInt();
				byte[] msgBytes = new byte[msgByteLength]; 
				bytesMsg.readBytes(msgBytes);
				
				simMessage = new SimMessage(msgBytes, senderAddress, recipientAddress);
				
			}
			
			
		} catch (Exception e) {
			throw new SimNetworkProxyException("An exception has been thrown while unpacking a sim message from the JMS bytes message.", e);
		}
		
		return simMessage;
		
	}
	
	
	
	@Override
	public synchronized void discard() throws SimNetworkProxyException {
		
		super.discard();
		
		
		
	}
	
	
}
