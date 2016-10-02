package net.hycube.simulator.transport;

public class SimNetworkProxyException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5993474913596414412L;

	public SimNetworkProxyException() {

	}

	public SimNetworkProxyException(String msg) {
		super(msg);
	}

	public SimNetworkProxyException(Throwable e) {
		super(e);
	}

	public SimNetworkProxyException(String msg, Throwable e) {
		super(msg, e);
	}

}
