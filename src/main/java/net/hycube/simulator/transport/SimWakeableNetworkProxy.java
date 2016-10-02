package net.hycube.simulator.transport;


public interface SimWakeableNetworkProxy extends SimNetworkProxy {	

	public void wakeup() throws SimNetworkProxyException;
	
	public void clearWakeupFlag();
	
	
}
