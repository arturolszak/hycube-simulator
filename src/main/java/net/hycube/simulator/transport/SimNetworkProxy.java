package net.hycube.simulator.transport;


public interface SimNetworkProxy {

	public void sendMessage(SimMessage msg) throws SimNetworkProxyException;
	
	public SimMessage receiveMessage() throws SimNetworkProxyException;
	
	public SimMessage receiveMessage(long timeout) throws SimNetworkProxyException;
	
	public SimMessage receiveMessageNow() throws SimNetworkProxyException;
	
	
	public void establishConnection(String simId, String simConnectionUrl) throws SimNetworkProxyException;

	public void establishConnectionWithSelf(String simId) throws SimNetworkProxyException;

	
	public void removeConnection(String simId) throws SimNetworkProxyException;
	
	
	public Object parseConnectionUrl(String connectionUrl); 
	
	
	public void discard() throws SimNetworkProxyException;


	
}
