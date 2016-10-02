package net.hycube.simulator;

public interface SimulatorServiceProxyFactory {

	public SimulatorServiceProxy createSimulatorServiceProxy(String url) throws SimulatorServiceProxyException;
	
	
}
