package net.hycube.simulator.transport.jms;

import java.io.InterruptedIOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import net.hycube.core.InitializationException;
import net.hycube.simulator.transport.SimMessage;
import net.hycube.simulator.transport.SimNetworkProxy;
import net.hycube.simulator.transport.SimNetworkProxyException;
import net.hycube.simulator.transport.SimNodePointer;

import org.apache.activemq.ActiveMQConnectionFactory;

public class JMSActiveMQSimNetworkProxy implements SimNetworkProxy {

	
	protected static final String PROP_NAME_WAKEUP_MESSAGE = "WakeupMessage";
	
	protected static final boolean TRANSACTED = false;
	protected static final int DELIVERY_MODE = DeliveryMode.NON_PERSISTENT;
	protected static final int ACK_MODE = Session.AUTO_ACKNOWLEDGE;
	
	protected static final boolean CONVERT_INT_TO_BYTE_ARRAY_BEFORE_SEND = false;
	
	protected static final String SIM_ADDRESS_CHARSET = "UTF-8";
	
	
	protected static class SimConnection {
		
		public String jmsUrl;
		public String msgQueueName;

		public Connection connection;
		public Session session;
		public Destination msgQueue;
		public MessageProducer producer;
		
		
		
	}
	
	
	
	protected boolean initialized = false;
	
	protected String jmsUrl;
	protected String queueName;
	
	protected Connection connection;
	protected Session session;
	protected Destination msgQueue;
	
	protected MessageConsumer consumer;
	
	protected MessageProducer replyProducer;
	
	protected HashMap<String, SimConnection> simConnections;
	
	
	//session and message producer for wakeup messages (to wake up receive calls)
	protected Session wakeupSession;
	protected MessageProducer wakeupProducer;
	
	
	protected long sentCounter;
	protected long receivedCounter;
	
	
	
	
	public synchronized void initialize(String jmsUrl, String msgQueueName) throws InitializationException {
		
		this.jmsUrl = jmsUrl;
		this.queueName = msgQueueName;

		this.simConnections = new HashMap<String, JMSActiveMQSimNetworkProxy.SimConnection>();

		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(jmsUrl);
        try {
            this.connection = connectionFactory.createConnection();
            this.connection.start();
            this.session = connection.createSession(TRANSACTED, ACK_MODE);
            this.msgQueue = this.session.createQueue(msgQueueName);

            //Set up a consumer to consume messages off of the message queue
            this.consumer = this.session.createConsumer(this.msgQueue);
            
            
    		this.wakeupSession = connection.createSession(TRANSACTED, ACK_MODE);	
    		this.wakeupProducer = wakeupSession.createProducer(msgQueue);
    		this.wakeupProducer.setDeliveryMode(DELIVERY_MODE);
           
    		
    		purgeQueue();
    		
                        
        } catch (JMSException e) {
        	throw new InitializationException("A JMS exception has been thrown while initializing the message consumer.", e);
        }
		        
        this.sentCounter = 0;
		this.receivedCounter = 0;
        
        
        this.initialized = true;
        
	}
	
	
	
	protected void purgeQueue() throws JMSException {
        //purge the queue, skip all the messages
		
		//a wakeup message is send to prevent receive from blocking if the queue is empty
		//(first call must be blocking, because receiveNoWait() may not return messages immediately)
		Message wakeupMessage = this.wakeupSession.createMessage();
        wakeupMessage.setBooleanProperty(PROP_NAME_WAKEUP_MESSAGE, true);
		this.wakeupProducer.send(wakeupMessage);
        
		//wait until messages are available
		this.consumer.receive();
		
		//remove all messages from the queue
        //boolean queuePurged = false;
        while (this.consumer.receiveNoWait() != null) {
        	//if (queuePurged == false) System.out.print("Purging queue: ");
        	//queuePurged = true;
        	//System.out.print("*");
        }
        //if (queuePurged) System.out.println();
	}
	
	

	@Override
	public synchronized void establishConnection(String simId, String simConnectionUrl) throws SimNetworkProxyException {
		
		String[] parsedConnectionUrl = parseConnectionUrl(simConnectionUrl);
		
		if (parsedConnectionUrl == null) throw new IllegalArgumentException("Invalid sim connection url string.");
		
		String simJmsUrl = parsedConnectionUrl[0];
		String simMsgQueueName = parsedConnectionUrl[1];
		
		establishConnection(simId, simJmsUrl, simMsgQueueName);
		
	}
	
	public synchronized void establishConnection(String simId, String simJmsUrl, String simMsgQueueName) throws SimNetworkProxyException {
		
		SimConnection simConn = new SimConnection();
		
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(simJmsUrl);

        try {
        	simConn.connection = connectionFactory.createConnection();
        	simConn.connection.start();
            simConn.session = simConn.connection.createSession(TRANSACTED, ACK_MODE);
            simConn.msgQueue = simConn.session.createQueue(simMsgQueueName);
 
            //Setup a message producer to send message to the queue the simulator is consuming from
            simConn.producer = simConn.session .createProducer(simConn.msgQueue);
            simConn.producer.setDeliveryMode(DELIVERY_MODE);
            
            
        } catch (JMSException e) {
        	throw new SimNetworkProxyException("A JMS exception has been thrown while initializing the message producer.", e);
        }
	
        
        simConnections.put(simId, simConn);
        
	}
	
	
	@Override
	public synchronized void establishConnectionWithSelf(String simId) throws SimNetworkProxyException {

		SimConnection simConn = new SimConnection();
		
		try {
        	simConn.connection = this.connection;
        	simConn.connection.start();
            simConn.session = simConn.connection.createSession(TRANSACTED, ACK_MODE);
            simConn.msgQueue = simConn.session.createQueue(this.queueName);
 
            //Setup a message producer to send message to the queue the simulator is consuming from
            simConn.producer = session.createProducer(simConn.msgQueue);
            simConn.producer.setDeliveryMode(DELIVERY_MODE);
                 
            
        } catch (JMSException e) {
        	throw new SimNetworkProxyException("A JMS exception has been thrown while initializing the message producer.", e);
        }
	
        
        simConnections.put(simId, simConn);
	}
	
	
	@Override
	public synchronized void removeConnection(String simId) throws SimNetworkProxyException {
		
		
		SimConnection simConn = simConnections.remove(simId);
		
		if (simConn != null) {
			try {
				simConn.session.close();
				simConn.connection.close();
			} catch (JMSException e) {
				throw new SimNetworkProxyException("A JMS exception has been thrown while releasing JMS reseources.", e);
	        }
			
		}
		
	}
	
	
	
	@Override
	public void sendMessage(SimMessage msg) throws SimNetworkProxyException {
		
		String recipientAddress = msg.getRecipientAddress();
		SimNodePointer recipient = new SimNodePointer(recipientAddress);
		
		String recSim = recipient.getSimId();
		
		SimConnection simConn = simConnections.get(recSim);
		if (simConn == null) {
			throw new SimNetworkProxyException("Unable to send a message to an undefined simulator.");
		}
		
		try {
			
			synchronized(simConn.session) {
			
				BytesMessage bytesMessage = simConn.session.createBytesMessage();
				
				//pack the sim message into the jms bytes message:

				if (CONVERT_INT_TO_BYTE_ARRAY_BEFORE_SEND) {
					//!!converted to byte array, writeUTF is heavily memory-consuming, writeInt slightly less
//					bytesMessage.writeUTF(msg.getSenderAddress());
//					bytesMessage.writeUTF(msg.getRecipientAddress());

					byte[] senderAddressB = msg.getSenderAddress().getBytes(Charset.forName("UTF-8"));
//					bytesMessage.writeInt(senderAddressB.length);
					byte[] senderAddressLengthB = new byte[Integer.SIZE/8];
					for (int i = 0; i < senderAddressLengthB.length; i++) senderAddressLengthB[i] =  (byte) ((senderAddressB.length & (0xFF << 8 * (senderAddressLengthB.length - 1 - i))) >>> 8 * (senderAddressLengthB.length - 1 - i));
					bytesMessage.writeBytes(senderAddressLengthB);
					bytesMessage.writeBytes(senderAddressB);
					
					byte[] recipientAddressB = msg.getRecipientAddress().getBytes(Charset.forName("UTF-8"));
					byte[] recipientAddressLengthB = new byte[Integer.SIZE/8];
					for (int i = 0; i < recipientAddressLengthB.length; i++) recipientAddressLengthB[i] =  (byte) ((recipientAddressB.length & (0xFF << 8 * (recipientAddressLengthB.length - 1 - i))) >>> 8 * (recipientAddressLengthB.length - 1 - i));
//					bytesMessage.writeInt(recipientAddressB.length);
					bytesMessage.writeBytes(recipientAddressLengthB);
					bytesMessage.writeBytes(recipientAddressB);
					
					
					byte[] msgLengthB = new byte[Integer.SIZE/8];
					for (int i = 0; i < msgLengthB.length; i++) msgLengthB[i] =  (byte) ((msg.getMessageBytes().length & (0xFF << 8 * (msgLengthB.length - 1 - i))) >>> 8 * (msgLengthB.length - 1 - i));
//					bytesMessage.writeInt(msg.getMessageBytes().length);
					bytesMessage.writeBytes(msgLengthB);
					bytesMessage.writeBytes(msg.getMessageBytes());
				}
				else {
					//!!converted to byte array, writeUTF is heavily memory-consuming
	//				bytesMessage.writeUTF(msg.getSenderAddress());
	//				bytesMessage.writeUTF(msg.getRecipientAddress());
	
					byte[] senderAddressB = msg.getSenderAddress().getBytes(Charset.forName("UTF-8"));
					bytesMessage.writeInt(senderAddressB.length);
					bytesMessage.writeBytes(senderAddressB);
					
					byte[] recipientAddressB = msg.getRecipientAddress().getBytes(Charset.forName("UTF-8"));
					bytesMessage.writeInt(recipientAddressB.length);
					bytesMessage.writeBytes(recipientAddressB);
					
					bytesMessage.writeInt(msg.getMessageBytes().length);
					bytesMessage.writeBytes(msg.getMessageBytes());
				}
				
				simConn.producer.send(bytesMessage);
		
			}
			

			
			sentCounter++;
			
			
		} catch (JMSException e) {
			if (e.getCause() instanceof InterruptedException || e.getCause() instanceof InterruptedIOException) {
				//just return, the processing has finished, the message might be discarded
				return;
			}
			else {
				throw new SimNetworkProxyException("A JMS exception has been thrown while sending a message", e);
			}
		}
        

	}

	@Override
	public SimMessage receiveMessage() throws SimNetworkProxyException {
		
		Message msg = null;
		try {
			synchronized (this.session) {
				msg = this.consumer.receive();
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
			synchronized(this.session) {
				msg = this.consumer.receiveNoWait();
			}
		} catch (JMSException e) {
			if (e.getCause() instanceof InterruptedException) {
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
	
	
	
	
	
	public synchronized boolean isInitialized() {
		return initialized;
	}
	
	
	
	/**
	 * Parses the string in format:
	 * protocol://host:port[Queue]
	 * and returns a String[] table containing: jmsUrl string; jmsQueueName  
	 */
	@Override
	public String[] parseConnectionUrl(String connectionUrl) {
		
		//final String connUrlPattern = "protocol://host:port[Queue]";
		
		final String connUrlRegex = "^\\s*(\\w+://\\S+:\\d+)\\[(\\w+)\\]\\s*$";
		//$1 jmsUrl
		//$2 jmsQueueName
		
		Pattern p = Pattern.compile(connUrlRegex);
		Matcher m = p.matcher(connectionUrl);
		
		if (m.find()) {
			String jmsUrl = m.group(1);
			String jmsQueueName = m.group(2);
			String[] res = new String[] {jmsUrl, jmsQueueName};
			return res;
		}
		else {
			return null;
			//throw new IllegalArgumentException("The connection string should follow the pattern: " + connUrlPattern);
		}

	}
	
	
	
	@Override
	public synchronized void discard() throws SimNetworkProxyException {
		
		try {
			purgeQueue();
		} catch (JMSException e) {
			throw new SimNetworkProxyException("A JMS exception has been thrown while purging the JMS queue.", e);
        }
		
		try {
			this.wakeupSession.close();
		} catch (JMSException e) {
			throw new SimNetworkProxyException("A JMS exception has been thrown while releasing JMS resources.", e);
		}
		
		for (SimConnection simConn : simConnections.values()) {
			try {
				simConn.session.close();
				if (simConn != this.connection) simConn.connection.close();
			} catch (JMSException e) {
				throw new SimNetworkProxyException("A JMS exception has been thrown while releasing JMS resources.", e);
	        }
		}
		simConnections.clear();
		
		try {
			this.session.close();
			this.connection.close();
		} catch (JMSException e) {
			throw new SimNetworkProxyException("A JMS exception has been thrown while releasing JMS resources.", e);
        }
		
		initialized = false;
		
		
	}
	
	
	public void clearMessageQueue() throws SimNetworkProxyException {
		try {
			while (consumer.receiveNoWait() != null);
		} catch (JMSException e) {
			throw new SimNetworkProxyException("A JMS exception has been thrown while clearing the JMS queue.", e);
		}
	}
	
	
	
	public long getMessagesSentCounter() {
		return sentCounter;
	}
	
	public long getMessagesReceivedCounter() {
		return receivedCounter;
	}

}
