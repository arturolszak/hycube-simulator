package net.hycube.simulator.app;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import net.hycube.core.InitializationException;
import net.hycube.environment.SystemTimeProvider;
import net.hycube.environment.TimeProvider;
import net.hycube.simulator.Simulator;
import net.hycube.simulator.rmi.RMISimulatorService;
import net.hycube.simulator.transport.SimNetworkProxyException;
import net.hycube.simulator.transport.jms.JMSActiveMQSimWakeableNetworkProxy;

public class SimulatorRemoteServiceApp1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String usage = "Usage:\n"
				+ "args[0]  - simId\n"
				+ "args[1]  - expected number of nodes\n"
				+ "args[2]  - separate MR queue\n"
				+ "args[3]  - numThreads\n"
				+ "args[4]  - num MR threads\n"
				+ "args[5]  - num non-MR threads\n"
				+ "args[6]  - properties file name\n"
				+ "args[7]  - messages: jms url\n"
				+ "args[8]  - messages: jms queue name\n"
				+ "args[9]  - sim comm: rmi host name\n"
				+ "args[10] - sim comm: rmi port\n"
				+ "args[11] - sim comm: rmi service name\n";
		
		if (args.length == 0) {
			System.out.println(usage);
			return;
		}
		
		String simId = args[0];
		int expectedNumOfNodes = Integer.parseInt(args[1]);
		
		boolean separateMRQueue = Boolean.parseBoolean(args[2]);
		int numThreads = Integer.parseInt(args[3]);
		int numMRThreads = Integer.parseInt(args[4]);
		int numNonMRThreads = Integer.parseInt(args[5]);
		
		String propertiesFileName = args[6];
		
		String jmsUrl = args[7];
		String jmsQueueName = args[8];
		
		String rmiHostname = args[9];
		int rmiPort = Integer.parseInt(args[10]);
		String rmiServiceName = args[11];
		
		
		
		//create the network proxy
		System.out.println("Initializing JMS (ActiveMQ) simulator network proxy...");
		JMSActiveMQSimWakeableNetworkProxy simNetworkProxy = new JMSActiveMQSimWakeableNetworkProxy();
		try {
			simNetworkProxy.initialize(jmsUrl, jmsQueueName);
		} catch (InitializationException e) {
			e.printStackTrace();
			return;
		}
		
		
		
		//initialize time provider
		System.out.println("Initializing simulator time provider...");
		TimeProvider simTimeProvider = new SystemTimeProvider();
		
		
		
		System.out.println("Initializing the simulator...");
		Simulator sim;
		try {
			sim = Simulator.initialize(simId, propertiesFileName, simNetworkProxy, simTimeProvider, separateMRQueue, numThreads, numMRThreads, numNonMRThreads, expectedNumOfNodes);
		} catch (InitializationException e) {
			e.printStackTrace();
			return;
		}
		
		
		
		System.out.println("Initializing simulator RMI service...");
		RMISimulatorService rmiSimService;
		try {
			rmiSimService = new RMISimulatorService(sim);
			rmiSimService.createRegistry(rmiPort);
			rmiSimService.bind(rmiHostname, rmiPort, rmiServiceName);
		} catch (RemoteException e) {
			e.printStackTrace();
			return;
		} catch (AlreadyBoundException e) {
			e.printStackTrace();
			return;
		}
		
		
		System.out.println("Initialized.");
		
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
		
		System.out.println("The simulator is active.");
		
		
		
		
		boolean discarded = false;
		
		while (! discarded) {
		
			System.out.println("Press ENTER to discard the simulator.");
			
			try {
				System.in.read();
			} catch (IOException e) {
	
			}

			
			
			
			//discard:
	
			System.out.println("Discarding RMI simulator service...");
			try {
				rmiSimService.unbind(rmiHostname, rmiPort, rmiServiceName);
				rmiSimService.shutdownRegistry(rmiPort);
			} catch (RemoteException e) {
				e.printStackTrace();
				return;
			} catch (NotBoundException e) {
				e.printStackTrace();
				return;
			}
			
			
			
			System.out.println("Discarding the simulator...");
			sim.discard();
			
			
			System.out.println("Discarding simulator network proxy...");
			try {
				simNetworkProxy.discard();
			} catch (SimNetworkProxyException e) {
				e.printStackTrace();
				return;
			}
			
			
			System.out.println("Discarding simulator time provider...");
			simTimeProvider.discard();
			
		
			discarded = true;
			System.out.println("Discarded.");
			
		}
		
		
		
		
		System.out.println("Finished #");
		
		
	}

	
}
