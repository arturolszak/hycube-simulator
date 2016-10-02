package net.hycube.simulator.stat;

import java.io.Serializable;


public class MessageStat implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = 2441659911423929192L;
	
	
	
	protected long msgSentCounter;
	protected long msgDeliveredCounter;
	protected long msgRouteLengthSum;
	
	
	public long getMsgSentCounter() {
		return msgSentCounter;
	}
	public void setMsgSentCounter(long msgSentCounter) {
		this.msgSentCounter = msgSentCounter;
	}
	public long getMsgDeliveredCounter() {
		return msgDeliveredCounter;
	}
	public void setMsgDeliveredCounter(long msgDeliveredCounter) {
		this.msgDeliveredCounter = msgDeliveredCounter;
	}
	public long getMsgRouteLengthSum() {
		return msgRouteLengthSum;
	}
	public void setMsgRouteLengthSum(long msgRouteLengthSum) {
		this.msgRouteLengthSum = msgRouteLengthSum;
	}
	
	
	public MessageStat(long msgSentCounter, long msgDeliveredCounter, long msgRouteLengthSum) {
		this.msgSentCounter = msgSentCounter;
		this.msgDeliveredCounter = msgDeliveredCounter;
		this.msgRouteLengthSum = msgRouteLengthSum;
	}
	
	
	
	
}
