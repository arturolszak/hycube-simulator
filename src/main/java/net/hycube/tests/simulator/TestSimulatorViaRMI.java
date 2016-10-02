package net.hycube.tests.simulator;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Random;

import net.hycube.core.HyCubeNodeId;
import net.hycube.core.InitializationException;
import net.hycube.core.NodeId;
import net.hycube.core.NodePointer;
import net.hycube.environment.SystemTimeProvider;
import net.hycube.environment.TimeProvider;
import net.hycube.simulator.Simulator;
import net.hycube.simulator.SimulatorServiceException;
import net.hycube.simulator.SimulatorServiceProxyException;
import net.hycube.simulator.rmi.RMISimulatorService;
import net.hycube.simulator.rmi.RMISimulatorServiceProxy;
import net.hycube.simulator.transport.SimNetworkProxyException;
import net.hycube.simulator.transport.jms.JMSActiveMQSimWakeableNetworkProxy;

public class TestSimulatorViaRMI {

	protected static final int SCHEDULER_THREAD_POOL_SIZE = 4;
	
	protected static final boolean TEST_ROUTE = true;
	protected static final boolean SYNCHRONOUS_ROUTE = false;
	protected static final boolean TEST_LOOKUP = false;
	protected static final boolean TEST_SEARCH = false;
	protected static final int TEST_SEARCH_K = 8;
	
	
	/**
	 * @param args
	 */
	
	public static void main(String[] args) {
		
		
		String simId = "S001";
		int expectedNumOfNodes = 50;
		
		boolean separateMRQueue = true;
		int numThreads = 0;
		int numMRThreads = 8;
		int numNonMRThreads = 8;
		
		String propertiesFileName = "hycube_simulator.cfg";
		
		String jmsUrl = "tcp://127.0.0.1:51001";
		String jmsQueueName = "SimMessages";
		
		String rmiHostname = "127.0.0.1";
		int rmiPort = 1099;
		String rmiServiceName = "HyCubeSimulator";
		
		int recoveryRepeat = 1;
		
		
		
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
		
		
		
		//make a connection to the simulator via RMI:
		RMISimulatorServiceProxy simProxy = null;
		try {
			simProxy = new RMISimulatorServiceProxy("//" + rmiHostname + ":" + rmiPort + "/" + rmiServiceName);
		} catch (SimulatorServiceProxyException e) {
			e.printStackTrace();
			return;
		}
		
		
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
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
				try {
					simProxy.join(i, nodeId, addresses[i], bootstrap);
				} catch (SimulatorServiceProxyException e) {
					e.printStackTrace();
					return;
				}
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
			for (int i = expectedNumOfNodes-1; i >= 0; i--) {
				System.out.println("Recoverying node: " + i + " (" + (r+1) + ")");
				try {
					simProxy.recoverNode(i);
					Thread.sleep(100);
				} catch (SimulatorServiceException e) {
					e.printStackTrace();
					return;
				} catch (InterruptedException e) {
					e.printStackTrace();
					return;
				} catch (SimulatorServiceProxyException e) {
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
		
		
		
		if (TEST_ROUTE) {
		
			for (int from = 0; from < expectedNumOfNodes; from++) {
				for (int to = 0; to < expectedNumOfNodes; to++) {
					System.out.println("From: " + from + " to: " + to);
					try {
						if (SYNCHRONOUS_ROUTE) {
							simProxy.routeMessageSync(from, nodeIds[to]);
						}
						else {
							simProxy.routeMessageAsync(from, nodeIds[to]);
							try {
								Thread.sleep(20);
							} catch (InterruptedException e) {
							}
						}
					} catch (SimulatorServiceException e) {
						e.printStackTrace();
						return;
					} catch (SimulatorServiceProxyException e) {
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
			
			
			
			try {
				System.out.println("Messages sent:      " + simProxy.getMessagesSentNum());
				System.out.println("Messages delivered: " + simProxy.getMessagesDeliveredNum());
				System.out.println("Messages received:  " + simProxy.getMessagesReceivedNum());
				System.out.println("Messages lost:      " + simProxy.getMessagesLostNum());
			} catch (SimulatorServiceProxyException e) {
				e.printStackTrace();
				return;
			}
		
		}
		
		
		if (TEST_LOOKUP) {
			for (int i = 0; i < expectedNumOfNodes; i++) {
				int lookupTestSimNodeId = rand.nextInt(expectedNumOfNodes);
				System.out.println("Performing lookup for: " + nodeIds[lookupTestSimNodeId].toHexString() + ", initiated by: " + nodeIds[i].toHexString());
				NodePointer res;
				try {
					res = simProxy.lookup(i, nodeIds[lookupTestSimNodeId]);
				} catch (SimulatorServiceException e) {
					e.printStackTrace();
					return;
				} catch (SimulatorServiceProxyException e) {
					e.printStackTrace();
					return;
				}
				System.out.println("--result: " + (res != null ? res.getNodeId().toHexString() : "NONE"));
			}
		}
		
		
		if (TEST_SEARCH) {
			for (int i = 0; i < expectedNumOfNodes; i++) {
				int searchTestSimNodeId = rand.nextInt(expectedNumOfNodes);
				System.out.println("Performing search for: " + nodeIds[searchTestSimNodeId].toHexString() + ", initiated by: " + nodeIds[i].toHexString());
				NodePointer[] res;
				try {
					res = simProxy.search(i, nodeIds[searchTestSimNodeId], (short) TEST_SEARCH_K, false);
				} catch (SimulatorServiceException e) {
					e.printStackTrace();
					return;
				} catch (SimulatorServiceProxyException e) {
					e.printStackTrace();
					return;
				}
				
				String resStr;
				if (res != null) {
					resStr = "";
					for (NodePointer np : res) resStr = resStr + np.getNodeId().toHexString() + ", "; 
				}
				else resStr = "NONE";
				System.out.println("--result: " + resStr);
			}
		}
		
		
		
		
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
					simProxy.leave(i);
				} catch (SimulatorServiceException e) {
					e.printStackTrace();
					return;
				} catch (SimulatorServiceProxyException e) {
					e.printStackTrace();
					return;
				}
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
			}
			
			
			simProxy = null;
			
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
