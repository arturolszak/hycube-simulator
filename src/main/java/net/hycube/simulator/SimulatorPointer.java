package net.hycube.simulator;

public class SimulatorPointer {
	
	protected String simId;
	protected String simCommandsConnectionUrl;
	protected String simMessageConnectionUrl;

	protected SimulatorServiceProxy simProxy;
	
	
	public String getSimId() {
		return simId;
	}
	
	public void setSimId(String simId) {
		this.simId = simId;
	}
	
	public String getSimCommandsConnectionUrl() {
		return simCommandsConnectionUrl;
	}
	
	public void setSimCommandsConnectionUrl(String simCommandsConnectionUrl) {
		this.simCommandsConnectionUrl = simCommandsConnectionUrl;
	}
	
	public String getSimMessageConnectionUrl() {
		return simMessageConnectionUrl;
	}

	public void setSimMessageConnectionUrl(String simMessageConnectionUrl) {
		this.simMessageConnectionUrl = simMessageConnectionUrl;
	}

	
	public SimulatorServiceProxy getSimulatorServiceProxy() {
		return simProxy;
	}
	
	public void setSimulatorServiceProxy(SimulatorServiceProxy simProxy) {
		this.simProxy = simProxy;
	}

	
	public SimulatorPointer(String simId, String simCommandsConnectionUrl, String simMessageConnectionUrl) {
		this.simId = simId;
		this.simCommandsConnectionUrl = simCommandsConnectionUrl;
		this.simMessageConnectionUrl = simMessageConnectionUrl;
		
	}

	
}
