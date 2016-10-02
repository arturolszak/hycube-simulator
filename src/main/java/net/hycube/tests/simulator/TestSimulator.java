package net.hycube.tests.simulator;

import java.io.IOException;
import java.util.Random;

import net.hycube.core.HyCubeNodeId;
import net.hycube.core.InitializationException;
import net.hycube.core.NodeId;
import net.hycube.environment.SystemTimeProvider;
import net.hycube.environment.TimeProvider;
import net.hycube.simulator.Simulator;
import net.hycube.simulator.SimulatorServiceException;
import net.hycube.simulator.transport.SimNetworkProxyException;
import net.hycube.simulator.transport.jms.JMSActiveMQSimWakeableNetworkProxy;

public class TestSimulator {

	protected static final int SCHEDULER_THREAD_POOL_SIZE = 4;
	
	
	/**
	 * @param args
	 */
	
	public static void main(String[] args) {
		
		
		String simId = "S001";
		int expectedNumOfNodes = 100;
		
		boolean separateMRQueue = true;
		int numThreads = 0;
		int numMRThreads = 8;
		int numNonMRThreads = 8;
		
		String propertiesFileName = "hycube_simulator.cfg";
		
		String jmsUrl = "tcp://127.0.0.1:51001";
		String jmsQueueName = "SimMessages";
		
		int recoveryRepeat = 2;
		
		
		
		//create the network proxy
		JMSActiveMQSimWakeableNetworkProxy simNetworkProxy = new JMSActiveMQSimWakeableNetworkProxy();
		try {
			simNetworkProxy.initialize(jmsUrl, jmsQueueName);
		} catch (InitializationException e) {
			e.printStackTrace();
			return;
		}
		
		
		//initialize time provider
		TimeProvider simTimeProvider = new SystemTimeProvider(SCHEDULER_THREAD_POOL_SIZE);
		
		Simulator sim;
		try {
			sim = Simulator.initialize(simId, propertiesFileName, simNetworkProxy, simTimeProvider, separateMRQueue, numThreads, numMRThreads, numNonMRThreads, expectedNumOfNodes);
		} catch (InitializationException e) {
			e.printStackTrace();
			return;
		}
		
		
		
		Random rand = new Random();
		
		
		String[] addresses = new String[expectedNumOfNodes];
		for (int i=0; i<expectedNumOfNodes; i++) {
			String address = simId + ":" + String.format("%016d", i);
			addresses[i] = address;
		}
		
		
		NodeId[] nodeIds = new NodeId[expectedNumOfNodes];
		
		for (int i = 0; i < expectedNumOfNodes; i++) {
			
			String bootstrap = null;
			
			if (i >= 1) {
				int bootstrapIndex = rand.nextInt(i);
				bootstrap = addresses[bootstrapIndex];
			}
			
			NodeId nodeId = HyCubeNodeId.generateRandomNodeId(4, 32);
			nodeIds[i] = nodeId;
			
			System.out.println("Joining node: " + addresses[i] + ", bootstrap: " + bootstrap);
			try {
				sim.join(i, nodeId, addresses[i], bootstrap);
			} catch (SimulatorServiceException e) {
				e.printStackTrace();
				return;
			}
			
		}
		
		
		
		
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		for (int r = 0; r < recoveryRepeat; r++) {
			System.out.print("Recovering: ");
			System.out.flush();
			for (int i = expectedNumOfNodes-1; i >= 0; i--) {
				System.out.print(i + ",");
				System.out.flush();
				
				System.out.println("Recoverying node: " + i);
				try {
					sim.recoverNode(i);
					Thread.sleep(100);
				} catch (SimulatorServiceException e) {
					e.printStackTrace();
					return;
				} catch (InterruptedException e) {
					e.printStackTrace();
					return;
				}
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				
			}
			System.out.println();
		}
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
		
		
		for (int from = 0; from < expectedNumOfNodes; from++) {
			for (int to = 0; to < expectedNumOfNodes; to++) {
				System.out.println("From: " + from + " to: " + to);
				try {
					sim.routeMessageAsync(from, nodeIds[to]);
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						e.printStackTrace();
						return;
					}
				} catch (SimulatorServiceException e) {
					e.printStackTrace();
					return;
				}
			}
		}
			
		
		
			
		
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
		
		System.out.println("Messages sent:      " + sim.getMessagesSentNum());
		System.out.println("Messages delivered: " + sim.getMessagesDeliveredNum());
		System.out.println("Messages received:  " + sim.getMessagesReceivedNum());
		System.out.println("Messages lost:      " + sim.getMessagesLostNum());
		
		
		
		System.out.println("Press ENTER to continue...");
		
		
		
		boolean discarded = false;
		
		while (! discarded) {
		
			try {
				System.in.read();
			} catch (IOException e) {
	
			}
	

			
			//discard:		
			
			for (int i = 0; i < expectedNumOfNodes; i++) {
				System.out.println("Leaving: " + i);
				try {
					sim.leave(i);
				} catch (SimulatorServiceException e) {
					e.printStackTrace();
					return;
				}
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
			}
			
			sim.discard();
			
			try {
				simNetworkProxy.discard();
			} catch (SimNetworkProxyException e) {
				e.printStackTrace();
				return;
			}
			
			simTimeProvider.discard();
			
		
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
			}
			
			
			discarded = true;
			
		}
		
		
		
		
		System.out.println("Finished #");
	
	
	}
	
}
