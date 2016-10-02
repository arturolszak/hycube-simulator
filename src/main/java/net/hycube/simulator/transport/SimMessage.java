package net.hycube.simulator.transport;


public class SimMessage {

	protected String senderAddress;
	protected String recipientAddress;
	protected byte[] messageBytes;
	
	public SimMessage(byte[] messageBytes, String senderAddress, String recipientAddress) {
		this.senderAddress = senderAddress;
		this.recipientAddress = recipientAddress;
		this.messageBytes = messageBytes;
		
	}
	
	
	public byte[] getMessageBytes() {
		return messageBytes;
	}

	public String getSenderAddress() {
		return senderAddress;
	}
	
	public String getRecipientAddress() {
		return recipientAddress;
	}
	
	
	
}
