package net.hycube.simulator.rmi;

import net.hycube.simulator.SimulatorServiceProxy;
import net.hycube.simulator.SimulatorServiceProxyException;
import net.hycube.simulator.SimulatorServiceProxyFactory;

public class RMISimulatorServiceProxyFactory implements SimulatorServiceProxyFactory {

	@Override
	public SimulatorServiceProxy createSimulatorServiceProxy(String url) throws SimulatorServiceProxyException {
		
		RMISimulatorServiceProxy rmiSimServiceProxy = null;

		rmiSimServiceProxy = new RMISimulatorServiceProxy(url);
		
		return rmiSimServiceProxy;
		
	}

}
