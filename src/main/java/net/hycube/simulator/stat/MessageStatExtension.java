package net.hycube.simulator.stat;

import net.hycube.common.EntryPoint;
import net.hycube.core.InitializationException;
import net.hycube.core.NodeAccessor;
import net.hycube.environment.NodeProperties;
import net.hycube.extensions.Extension;
import net.hycube.simulator.environment.SimEnvironment;

public class MessageStatExtension implements Extension {

	
	protected MessageStat messageStat;
	
	
	
	public long getMsgSentCounter() {
		synchronized (messageStat) {
			return messageStat.getMsgSentCounter();
		}
	}

	public void setMsgSentCounter(long msgSentCounter) {
		synchronized (messageStat) {
			messageStat.setMsgSentCounter(msgSentCounter);
		}
	}

	public long getMsgDeliveredCounter() {
		synchronized (messageStat) {
			return messageStat.getMsgDeliveredCounter();
		}
	}

	public void setMsgDeliveredCounter(long msgDeliveredCounter) {
		synchronized (messageStat) {
			messageStat.setMsgDeliveredCounter(msgDeliveredCounter);
		}
	}

	public long getMsgRouteLengthSum() {
		synchronized (messageStat) {
			return messageStat.getMsgRouteLengthSum();
		}
	}

	public void setMsgRouteLengthSum(long msgRouteLengthSum) {
		synchronized (messageStat) {
			messageStat.setMsgRouteLengthSum(msgRouteLengthSum);
		}
	}
	
	
	
	
	public Object getMsgCountersLock() {
		return messageStat;
	}
	
	

	public void incrementMsgSentCounter() {
		synchronized (messageStat) {
			messageStat.setMsgSentCounter(messageStat.getMsgSentCounter() + 1);
		}
	}
	
	public void incrementMsgDeliveredCounter() {
		synchronized (messageStat) {
			messageStat.setMsgDeliveredCounter(messageStat.getMsgDeliveredCounter() + 1);
		}
	}
	
	public void updateRouteLengthSum(int newMsgRouteLength) {
		synchronized (messageStat) {
			messageStat.setMsgRouteLengthSum(messageStat.getMsgRouteLengthSum() + newMsgRouteLength);
		}
	}
	
	
	
	@Override
	public void initialize(NodeAccessor nodeAccessor, NodeProperties properties) throws InitializationException {
		
		this.messageStat = ((SimEnvironment)nodeAccessor.getEnvironment()).getMessageStat();		
		
	}

	@Override
	public void postInitialize() throws InitializationException {
		
	}

	
	
	
	
	public MessageStat getStat() {
		return messageStat;
	}
	
	
	@Override
	public void discard() {
		

		
	}

	@Override
	public EntryPoint getExtensionEntryPoint() {
		return null;
	}



}
