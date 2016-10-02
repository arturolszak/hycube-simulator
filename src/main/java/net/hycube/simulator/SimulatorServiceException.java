package net.hycube.simulator;

public class SimulatorServiceException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5163973161226595610L;

	
	public SimulatorServiceException() {
		
	}

	public SimulatorServiceException(String msg) {
		super(msg);
		
	}

	public SimulatorServiceException(Throwable e) {
		super(e);
		
	}

	public SimulatorServiceException(String msg, Throwable e) {
		super(msg, e);
		
	}
	
	
}
