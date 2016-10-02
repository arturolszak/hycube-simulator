package net.hycube.simulator.simulations;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import net.hycube.configuration.GlobalConstants;
import net.hycube.core.HyCubeNodeId;
import net.hycube.core.NodeId;
import net.hycube.environment.FileNodePropertiesReader;
import net.hycube.environment.NodeProperties;
import net.hycube.environment.NodePropertiesConversionException;
import net.hycube.environment.NodePropertiesInitializationException;
import net.hycube.random.RandomMultiple;
import net.hycube.simulator.Simulation;
import net.hycube.simulator.SimulatorMaster;
import net.hycube.simulator.SimulatorMasterException;
import net.hycube.simulator.SimulatorServiceException;
import net.hycube.simulator.SimulatorServiceProxyException;
import net.hycube.simulator.WeightSimulatorPointer;
import net.hycube.simulator.stat.MessageStat;
import net.hycube.utils.ArrayUtils;
import net.hycube.utils.FileUtils;
import net.hycube.utils.HashMapUtils;
import net.hycube.utils.ObjectToStringConverter.MappedType;

public class RecoveryEfficiencyTestNotVerbose implements Simulation {

	protected static final String PROP_KEY_SIMULATORS = "Simulators";
	protected static final String PROP_KEY_SIM_CONN_STRING = "SimConnString";
	protected static final String PROP_KEY_MSG_CONN_STRING = "MsgConnString";
	protected static final String PROP_KEY_WEIGHT = "Weight";
	protected static final String PROP_KEY_MAX_NUM_NODES = "MaxNumNodes";
	protected static final String PROP_KEY_RESULTS_FILE_NAME = "ResultsFileName";
	protected static final String PROP_KEY_DEFAULT_CONF_FILE_NAME = "DefaultConfFileName";

	protected static final String PROP_KEY_SIMULATIONS_NUM = "SimulationsNum";

	protected static final String PROP_KEY_SIMULATIONS = "Simulations";

	protected static final String PROP_KEY_CONF_FILE_NAME = "ConfFileName";

	protected static final String PROP_KEY_NUM_NODES = "NumNodes";
	protected static final String PROP_KEY_INITIAL_RECOVERY_REPEAT = "InitialRecoveryRepeat";
	protected static final String PROP_KEY_DISCARD_PERCENTS = "DiscardPercents";
	protected static final String PROP_KEY_DISCARD_STEPS_NUM = "DiscardStepsNum";
	protected static final String PROP_KEY_RECOVERY_AFTER_DISCARDS_REPEAT = "RecoveryAfterDiscardsRepeat";
	protected static final String PROP_KEY_NS_RECOVERY_AFTER_DISCARDS = "NSRecoveryAfterDiscards";
	protected static final String PROP_KEY_FULL_RECOVERY_AFTER_DISCARDS = "FullRecoveryAfterDiscards";
	protected static final String PROP_KEY_ROUTE_TESTS_NUM = "RouteTestsNum";

	protected static final String PROP_KEY_RUN_MEMORY_CLEAR_EXTENSION = "RunMemoryClearExtension";

	protected static final boolean SYNCHRONOUS_ROUTE = false;

	protected static final int SLEEP_TIME_AFTER_JOIN = 100;
	protected static final int SLEEP_TIME_AFTER_JOINS = 6000;
	protected static final int SLEEP_TIME_AFTER_RECOVERY = 100;
	protected static final int SLEEP_TIME_AFTER_RECOVERIES = 6000;
	protected static final int SLEEP_TIME_AFTER_NODE_MEMORY_CLEAR = 20;
	protected static final int SLEEP_TIME_AFTER_NODE_MEMORY_CLEARS = 4000;
	protected static final int SLEEP_TIME_AFTER_DROP = 20;
	protected static final int SLEEP_TIME_AFTER_DROPS = 4000;
	protected static final int SLEEP_TIME_AFTER_KEEPALIVE_AFTER_DROPS = 20;
	protected static final int SLEEP_TIME_AFTER_KEEPALIVES_AFTER_DROPS = 6000;
	protected static final int SLEEP_TIME_AFTER_ROUTE = 20;
	protected static final int SLEEP_TIME_AFTER_ROUTES = 16000;
	protected static final int SLEEP_TIME_AFTER_MSG_DISCARD_AFTER_ROUTES = 50;
	protected static final int SLEEP_TIME_AFTER_MSG_DISCARDS_AFTER_ROUTES = 4000;
	protected static final int SLEEP_TIME_AFTER_SIMULATION = 4000;

	@Override
	public void runSimulation(SimulatorMaster simMaster, String[] args)
			throws SimulatorMasterException {
		try {
			simulate(simMaster, args);
		} catch (NodePropertiesInitializationException e) {
			throw new SimulatorMasterException(
					"An exception has been thrown during the simulation.", e);
		} catch (SimulatorServiceException e) {
			throw new SimulatorMasterException(
					"An exception has been thrown during the simulation.", e);
		} catch (SimulatorServiceProxyException e) {
			throw new SimulatorMasterException(
					"An exception has been thrown during the simulation.", e);
		} catch (InterruptedException e) {
			throw new SimulatorMasterException(
					"An exception has been thrown during the simulation.", e);
		} catch (NodePropertiesConversionException e) {
			throw new SimulatorMasterException(
					"An exception has been thrown during the simulation.", e);
		} catch (IOException e) {
			throw new SimulatorMasterException(
					"An exception has been thrown during the simulation.", e);
		}
	}

	@SuppressWarnings("resource")
	public void simulate(SimulatorMaster simMaster, String[] args)
			throws SimulatorMasterException,
			NodePropertiesInitializationException, SimulatorServiceException,
			SimulatorServiceProxyException, InterruptedException,
			NodePropertiesConversionException, IOException {

		NodeProperties properties = readProperties(args[0]);

		Random rand = new Random();

		List<String> simulatorKeys = properties
				.getStringListProperty(PROP_KEY_SIMULATORS);
		int simCount = simulatorKeys.size();

		WeightSimulatorPointer[] simulators = new WeightSimulatorPointer[simCount];
		for (int simIndex = 0; simIndex < simulatorKeys.size(); simIndex++) {
			String simKey = simulatorKeys.get(simIndex);
			NodeProperties simProperties = properties.getNestedProperty(
					PROP_KEY_SIMULATORS, simKey);
			String simId = simKey;
			String simCommandConnectionUrl = simProperties
					.getProperty(PROP_KEY_SIM_CONN_STRING);
			String simMessageConnectionUrl = simProperties
					.getProperty(PROP_KEY_MSG_CONN_STRING);
			int weight = (Integer) simProperties.getProperty(PROP_KEY_WEIGHT,
					MappedType.INT);
			int maxNumNodes = (Integer) simProperties.getProperty(
					PROP_KEY_MAX_NUM_NODES, MappedType.INT);
			WeightSimulatorPointer simPointer = new WeightSimulatorPointer(
					simId, simCommandConnectionUrl, simMessageConnectionUrl,
					weight, maxNumNodes);
			simulators[simIndex] = simPointer;
		}

		String defaultConfFileName = properties
				.getProperty(PROP_KEY_DEFAULT_CONF_FILE_NAME);

		int simulationsNum = (Integer) properties.getProperty(
				PROP_KEY_SIMULATIONS_NUM, MappedType.INT);

		String resultsFileName = properties
				.getProperty(PROP_KEY_RESULTS_FILE_NAME);
		BufferedWriter resOut = new BufferedWriter(new FileWriter(
				resultsFileName));

		resOut.write("SIM: " + args[0]);
		resOut.newLine();
		resOut.flush();

		resOut.write("Default properties: " + defaultConfFileName);
		resOut.newLine();
		resOut.flush();

		// connect to simulators
		for (int i = 0; i < simCount; i++) {
			System.out.println("Connecting to simulator: "
					+ simulators[i].getSimId());
			simMaster.connectToSimulator(simulators[i].getSimId(),
					simulators[i].getSimCommandsConnectionUrl(),
					simulators[i].getSimMessageConnectionUrl());
			simulators[i].setSimulatorServiceProxy(simMaster
					.getSimulatorPointer(simulators[i].getSimId())
					.getSimulatorServiceProxy());

			// clear the simulator:
			simulators[i].getSimulatorServiceProxy().clear();

			// reset the simulator:
			simulators[i].getSimulatorServiceProxy().resetStats();

		}

		// establish messaging between simulators:
		for (int i = 0; i < simCount; i++) {
			for (int j = 0; j < simCount; j++) {
				if (i != j) {
					System.out.println("Establishing connection from: "
							+ simulators[i].getSimId() + " to: "
							+ simulators[j].getSimId());
					simMaster.establishMessageConnection(
							simulators[i].getSimId(), simulators[j].getSimId());
				}
			}
		}




		int numNodes = (Integer) properties.getProperty(
				PROP_KEY_NUM_NODES, MappedType.INT);
		resOut.write("NumNodes: " + numNodes);
		resOut.newLine();
		resOut.flush();



		String[] addresses = new String[numNodes];
		NodeId[] nodeIds = new NodeId[numNodes];
		int bootstrapIndexes[] = new int[numNodes];

		for (int i = 0; i < numNodes; i++) {

			if (i >= 1) {
				int bootstrapIndex = rand.nextInt(i);
				bootstrapIndexes[i] = bootstrapIndex;
			}

			NodeId nodeId = HyCubeNodeId.generateRandomNodeId(4, 32);
			nodeIds[i] = nodeId;

		}


		int recoveryRepeat = (Integer) properties.getProperty(
				PROP_KEY_INITIAL_RECOVERY_REPEAT, MappedType.INT);

		resOut.write("InitialRecoveryRepeat: " + recoveryRepeat);
		resOut.newLine();
		resOut.flush();



		List<Object> discardPercents = properties
				.getListProperty(PROP_KEY_DISCARD_PERCENTS, MappedType.INT);

		int[] discardNodesNumsInSteps = new int[discardPercents.size()];
		int discardNodesSum = 0;
		for (int i = 0; i < discardPercents.size(); i++) {
			int discardPercent = (Integer) discardPercents.get(i);
			int discardNodesNum = discardPercent * numNodes / 100;
			discardNodesNumsInSteps[i] = discardNodesNum;
			discardNodesSum += discardNodesNum;
		}

		int[] indexesToDrop = RandomMultiple.randomSelection(numNodes,
				discardNodesSum);
		ArrayUtils.shuffleArray(indexesToDrop);




		boolean runMemoryClearExtension = (Boolean) properties
				.getProperty(PROP_KEY_RUN_MEMORY_CLEAR_EXTENSION,
						MappedType.BOOLEAN);




		int routeTestsNum = (Integer) properties.getProperty(
				PROP_KEY_ROUTE_TESTS_NUM, MappedType.INT);

		resOut.write("RouteTestsNum: " + routeTestsNum);
		resOut.newLine();
		resOut.flush();




		//generate the test pairs of nodes
		int currDiscardNodeIndexTemp = 0;
		int[][] froms = new int[discardNodesNumsInSteps.length][];
		int[][] tos = new int[discardNodesNumsInSteps.length][];
		HashSet<Integer> nodeSetTemp = new HashSet<Integer>();
		for (int i = 0; i < numNodes; i++) nodeSetTemp.add(i);
		for (int step = 0; step < discardNodesNumsInSteps.length; step++) {
			froms[step] = new int[routeTestsNum];
			tos[step] = new int[routeTestsNum];
			for (int i = 0; i < discardNodesNumsInSteps[step]; i++, currDiscardNodeIndexTemp++) {
				int indexToDrop = indexesToDrop[currDiscardNodeIndexTemp];
				nodeSetTemp.remove(indexToDrop);
			}
			for (int i = 0; i < routeTestsNum; i++) {
				int f = rand.nextInt(nodeSetTemp.size());
				int t = rand.nextInt(nodeSetTemp.size());
				froms[step][i] = (Integer) nodeSetTemp.toArray()[f];
				tos[step][i] = (Integer) nodeSetTemp.toArray()[t];
			}			
		}




		resOut.newLine();
		resOut.flush();




		for (int simNo = 0; simNo < simulationsNum; simNo++) {

			resOut.write("\tSimulation: " + simNo);
			resOut.newLine();
			resOut.flush();

			NodeProperties simulationProperties = properties.getNestedProperty(
					PROP_KEY_SIMULATIONS, String.valueOf(simNo));


			String confFileName = simulationProperties
					.getProperty(PROP_KEY_CONF_FILE_NAME);

			File confFile = FileUtils.findFileOnClassPath(confFileName);
			if (confFile == null || (!confFile.exists()) || (!confFile.isFile())) throw new SimulatorMasterException("The configuration file was not found in the classpath.");

			resOut.write("\tProperties: " + confFileName);
			resOut.newLine();
			resOut.flush();

			HashMap<Integer, Integer> nodeSimMap = new HashMap<Integer, Integer>(HashMapUtils.getHashMapCapacityForElementsNum(numNodes, GlobalConstants.DEFAULT_HASH_MAP_LOAD_FACTOR), GlobalConstants.DEFAULT_HASH_MAP_LOAD_FACTOR);

			// specify the custom simulation properties:
			for (int i = 0; i < simCount; i++) {
				simulators[i].getSimulatorServiceProxy()
				.readPropertiesWithDefaultValues(defaultConfFileName,
						confFileName);
			}

			for (int i = 0; i < numNodes; i++) {

				//find the simulator that has the least relative load (divided by its weight)
				int simInd = 0;
				double[] currLoads = new double[simulators.length];
				double minLoad = 0;
				for (int sim = 0; sim < simulators.length; sim++) {
					currLoads[sim] = simulators[sim].getNumNodes() / simulators[sim].getWeight();
					if (sim== 0 || minLoad > currLoads[sim]) {
						minLoad = currLoads[sim];
						simInd = sim;
					}
				}


				if (simulators[simInd].getMaxNumNodes() < simulators[simInd]
						.getNumNodes() + 1) {
					System.out
					.println("ERROR: Could not create a node - the number of nodes would exceed the maximum number of nodes for the simulator: "
							+ simulators[simInd].getSimId()
							+ ", currNumberOfNodes: "
							+ simulators[simInd].getMaxNumNodes());
					return;
				}

				nodeSimMap.put(i, simInd);
				simulators[simInd].setNumNodes(simulators[simInd].getNumNodes() + 1);

				String address = simulators[simInd].getSimId() + ":"
						+ String.format("%016d", i);
				addresses[i] = address;

			}


			for (int i = 0; i < numNodes; i++) {

				String bootstrap = null;

				if (i >= 1) {
					bootstrap = addresses[bootstrapIndexes[i]];
				}

				NodeId nodeId = nodeIds[i];

				//				System.out.println("Joining node: " + addresses[i]
				//						+ ", bootstrap: " + bootstrap + " on similator: "
				//						+ simulators[nodeSimMap.get(i)].getSimId());
				simulators[nodeSimMap.get(i)].getSimulatorServiceProxy().join(
						i, nodeId, addresses[i], bootstrap);

				Thread.sleep(SLEEP_TIME_AFTER_JOIN);

			}

			Thread.sleep(SLEEP_TIME_AFTER_JOINS);




			for (int r = 0; r < recoveryRepeat; r++) {
				for (int i = numNodes - 1; i >= 0; i--) {
					//					System.out.println("Recoverying node: " + i + " ("
					//							+ (r + 1) + ")");
					simulators[nodeSimMap.get(i)].getSimulatorServiceProxy()
					.recoverNode(i);

					Thread.sleep(SLEEP_TIME_AFTER_RECOVERY);

				}
				//				System.out.println();
			}

			Thread.sleep(SLEEP_TIME_AFTER_RECOVERIES);



			if (runMemoryClearExtension) {
				// force clearing the caches (node implementation dependent) ->
				// call the extension
				String[] extensionsToCall = new String[] { "NodeClearMemoryExtension" };
				for (int rep = 0; rep < 1; rep++) { // 2 repetitions are enough
					// to remove failed nodes
					for (String extKey : extensionsToCall) {
						for (int i = 0; i < numNodes; i++) {
							// if (nodesDiscarded.contains(i)) continue;
							//							System.out.println("Calling extension: " + extKey
							//									+ " (" + (rep + 1) + "), node: " + i);
							simulators[nodeSimMap.get(i)]
									.getSimulatorServiceProxy().callExtension(
											i, extKey);
							Thread.sleep(SLEEP_TIME_AFTER_NODE_MEMORY_CLEAR);
						}
					}
					Thread.sleep(SLEEP_TIME_AFTER_NODE_MEMORY_CLEARS);
				}
			}




			int discardStepsNum = (Integer) simulationProperties.getProperty(PROP_KEY_DISCARD_STEPS_NUM, MappedType.INT);
			int recoveryAfterDiscardRepeat = (Integer) simulationProperties.getProperty(PROP_KEY_RECOVERY_AFTER_DISCARDS_REPEAT, MappedType.INT);
			boolean nsRecoveryAfterDiscard = (Boolean) simulationProperties.getProperty(PROP_KEY_NS_RECOVERY_AFTER_DISCARDS, MappedType.BOOLEAN);
			boolean fullRecoveryAfterDiscard = (Boolean) simulationProperties.getProperty(PROP_KEY_FULL_RECOVERY_AFTER_DISCARDS, MappedType.BOOLEAN);

			resOut.write("\tDiscardStepsNum: " + discardStepsNum);
			resOut.newLine();
			resOut.flush();

			resOut.write("\tRecoveryAfterDiscardsRepeat: " + recoveryAfterDiscardRepeat);
			resOut.newLine();
			resOut.flush();

			resOut.write("\tNSRecoveryAfterDiscards: " + nsRecoveryAfterDiscard);
			resOut.newLine();
			resOut.flush();

			resOut.write("\tFullRecoveryAfterDiscards: " + fullRecoveryAfterDiscard);
			resOut.newLine();
			resOut.flush();



			HashSet<Integer> nodesDiscarded = new HashSet<Integer>();



			// *************************************************
			// run route tests for defined discard percentages:

			resOut.newLine();
			resOut.flush();

			int currDiscardNodeIndex = 0;
			for (int step = 0; step < discardStepsNum; step++) {

				// clear the simulators:
				for (int i = 0; i < simulators.length; i++) {
					simulators[i].getSimulatorServiceProxy().resetStats();
				}

				// discard nodes

				for (int i = 0; i < discardNodesNumsInSteps[step]; i++, currDiscardNodeIndex++) {
					int indexToDrop = indexesToDrop[currDiscardNodeIndex];
					//					System.out.println("Dropping node: " + indexToDrop);
					nodesDiscarded.add(indexToDrop);
					simulators[nodeSimMap.get(indexToDrop)]
							.getSimulatorServiceProxy()
							.discardNode(indexToDrop);

					simulators[nodeSimMap.get(indexToDrop)].setNumNodes(simulators[nodeSimMap.get(indexToDrop)].getNumNodes() - 1);

					nodeSimMap.remove(indexToDrop);


					Thread.sleep(SLEEP_TIME_AFTER_DROP);
				}

				Thread.sleep(SLEEP_TIME_AFTER_DROPS);

			}


			String[] keepAliveBackgroundProcessesToRunOnce = new String[] {
					"HyCubePingBackgroundProcess",
			"HyCubeAwaitingPongsBackgroundProcess" };
			for (int rep = 0; rep < 2; rep++) { // 2 repetitions are enough
				// to remove failed nodes
				for (String bpKey : keepAliveBackgroundProcessesToRunOnce) {
					for (int i = 0; i < numNodes; i++) {
						if (nodesDiscarded.contains(i))
							continue;
						//							System.out
						//									.println("Processing (once) background process: "
						//											+ bpKey
						//											+ " ("
						//											+ (rep + 1)
						//											+ "), node: " + i);
						simulators[nodeSimMap.get(i)]
								.getSimulatorServiceProxy()
								.processBackgroundProcess(i, bpKey);
						Thread.sleep(SLEEP_TIME_AFTER_KEEPALIVE_AFTER_DROPS);
					}
					Thread.sleep(SLEEP_TIME_AFTER_KEEPALIVES_AFTER_DROPS);
				}

			}




			resOut.write("\t\tNumNodesDiscarded: " + nodesDiscarded.size());
			resOut.newLine();
			resOut.newLine();
			resOut.flush();



			for (int r = 0; r <= recoveryAfterDiscardRepeat; r++) {
				
				if (r != 0) {
					for (int i = numNodes - 1; i >= 0; i--) {
						if (nodesDiscarded.contains(i))	continue;
	
						if (nsRecoveryAfterDiscard) {
							//							System.out.println("Recoverying (NS) node: " + i + " ("
							//									+ (r) + ")");
							simulators[nodeSimMap.get(i)].getSimulatorServiceProxy()
							.recoverNodeNS(i);
	
							Thread.sleep(SLEEP_TIME_AFTER_RECOVERY);
						}
	
						if (fullRecoveryAfterDiscard) {
							//							System.out.println("Recoverying node: " + i + " ("
							//									+ (r) + ")");
							simulators[nodeSimMap.get(i)].getSimulatorServiceProxy()
							.recoverNode(i);
	
							Thread.sleep(SLEEP_TIME_AFTER_RECOVERY);
						}
	
					}
				}


				int messagesSentNum = 0;
				int messagesDeliveredNum = 0;
				int messagesReceivedNum = 0;
				int messagesLostNum = 0;

				MessageStat messageStat = new MessageStat(0, 0, 0);




				// Route messages between random pairs of nodes:

				for (int rt = 0; rt < routeTestsNum; rt++) {
					//						int from = rand.nextInt(nodeSimMap.size());
					//						int to = rand.nextInt(nodeSimMap.size());
					//						from = (Integer) nodeSimMap.keySet().toArray()[from];
					//						to = (Integer) nodeSimMap.keySet().toArray()[to];
					int from = froms[discardStepsNum-1][rt];
					int to = tos[discardStepsNum-1][rt];
					//						System.out.println("#" + rt + ": From: " + from + " to: "
					//								+ to);
					simulators[nodeSimMap.get(from)].getSimulatorServiceProxy()
					.routeMessageAsync(from, nodeIds[to]);
					Thread.sleep(SLEEP_TIME_AFTER_ROUTE);
				}

				Thread.sleep(SLEEP_TIME_AFTER_ROUTES);

				String[] msgDiscardBackgroundProcessesToRunOnceAfterRoute = new String[] { "HyCubeAwaitingAcksBackgroundProcess" };
				for (int rep = 0; rep < 1; rep++) { // 2 repetitions are enough
					// to remove failed nodes
					for (String bpKey : msgDiscardBackgroundProcessesToRunOnceAfterRoute) {
						for (int i = 0; i < numNodes; i++) {
							if (nodesDiscarded.contains(i))
								continue;
							//								System.out
							//										.println("Processing (once) background process: "
							//												+ bpKey
							//												+ " ("
							//												+ (rep + 1)
							//												+ "), node: " + i);
							simulators[nodeSimMap.get(i)]
									.getSimulatorServiceProxy()
									.processBackgroundProcess(i, bpKey);
							Thread.sleep(SLEEP_TIME_AFTER_MSG_DISCARD_AFTER_ROUTES);
						}
					}
					Thread.sleep(SLEEP_TIME_AFTER_MSG_DISCARDS_AFTER_ROUTES);
				}

				// get the statistics
				for (int i = 0; i < simCount; i++) {
					messagesSentNum += simulators[i].getSimulatorServiceProxy()
							.getMessagesSentNum();
					messagesDeliveredNum += simulators[i]
							.getSimulatorServiceProxy()
							.getMessagesDeliveredNum();
					messagesReceivedNum += simulators[i]
							.getSimulatorServiceProxy()
							.getMessagesReceivedNum();
					messagesLostNum += simulators[i].getSimulatorServiceProxy()
							.getMessagesLostNum();

					MessageStat simMessageStat = simulators[i]
							.getSimulatorServiceProxy().getMessageStatistics();
					messageStat.setMsgSentCounter(messageStat
							.getMsgSentCounter()
							+ simMessageStat.getMsgSentCounter());
					messageStat.setMsgDeliveredCounter(messageStat
							.getMsgDeliveredCounter()
							+ simMessageStat.getMsgDeliveredCounter());
					messageStat.setMsgRouteLengthSum(messageStat
							.getMsgRouteLengthSum()
							+ simMessageStat.getMsgRouteLengthSum());

				}



				resOut.write("\t\t\tTimesRecovered: " + (r));
				resOut.newLine();
				resOut.flush();

				// write the results to the results file:
				resOut.write("\t\t\tMessages sent:      " + messagesSentNum);
				resOut.newLine();
				resOut.write("\t\t\tMessages delivered: " + messagesDeliveredNum);
				resOut.newLine();
				resOut.write("\t\t\tMessages received:  " + messagesReceivedNum);
				resOut.newLine();
				resOut.write("\t\t\tMessages lost:      " + messagesLostNum);
				resOut.newLine();

				resOut.write("\t\t\tMessage stat [sent]:            "
						+ messageStat.getMsgSentCounter());
				resOut.newLine();
				resOut.write("\t\t\tMessage stat [delivered]:       "
						+ messageStat.getMsgDeliveredCounter());
				resOut.newLine();
				resOut.write("\t\t\tMessage stat [sumRouteLength]:  "
						+ messageStat.getMsgRouteLengthSum());
				resOut.newLine();
				resOut.write("\t\t\tMessage stat [avgRouteLength]:  "
						+ Math.round(100 * (((double) messageStat
								.getMsgRouteLengthSum()) / ((double) messageStat
										.getMsgDeliveredCounter()))) / 100.0);
				resOut.newLine();

				resOut.newLine();
				resOut.flush();



				//					System.out.println();




				//reset statistics:
				for (int i = 0; i < simCount; i++) {
					// reset the simulator:
					simulators[i].getSimulatorServiceProxy().resetStats();
				}




			}


			Thread.sleep(SLEEP_TIME_AFTER_RECOVERIES);





			if (runMemoryClearExtension) {
				// force clearing the caches (node implementation dependent) ->
				// call the extension
				String[] extensionsToCall = new String[] { "NodeClearMemoryExtension" };
				for (int rep = 0; rep < 1; rep++) { // 2 repetitions are enough
					// to remove failed nodes
					for (String extKey : extensionsToCall) {
						for (int i = 0; i < numNodes; i++) {
							if (nodesDiscarded.contains(i))	continue;
							// if (nodesDiscarded.contains(i)) continue;
							//								System.out.println("Calling extension: " + extKey
							//										+ " (" + (rep + 1) + "), node: " + i);
							simulators[nodeSimMap.get(i)]
									.getSimulatorServiceProxy().callExtension(
											i, extKey);
							Thread.sleep(SLEEP_TIME_AFTER_NODE_MEMORY_CLEAR);
						}
					}
					Thread.sleep(SLEEP_TIME_AFTER_NODE_MEMORY_CLEARS);
				}
			}









			// finishing the simulations -> discard remaining nodes and remove
			// from the simulator

			//			System.out.println("Disconnecting nodes...");

			for (int i : nodeSimMap.keySet()) {

				//				System.out.println("Leaving: " + i);
				simulators[nodeSimMap.get(i)].getSimulatorServiceProxy().leave(
						i);

				//				System.out.println("Dropping: " + i);
				simulators[nodeSimMap.get(i)].getSimulatorServiceProxy()
				.discardNode(i);

			}

			nodeSimMap.clear();
			for (int i = 0; i < simCount; i++) {
				simulators[i].setNumNodes(0);
			}



			Thread.sleep(SLEEP_TIME_AFTER_SIMULATION);



			// run GC on the simulators' virtual machines
			for (int i = 0; i < simCount; i++) {
				simulators[i].getSimulatorServiceProxy()
				.runGC();
			}


		}

		// close messaging between simulators:
		for (int i = 0; i < simCount; i++) {
			for (int j = 0; j < simCount; j++) {
				if (i != j) {
					System.out.println("Closing connection from: "
							+ simulators[i].getSimId() + " to: "
							+ simulators[j].getSimId());
					simMaster.closeMessageConnection(simulators[i].getSimId(),
							simulators[j].getSimId());
				}
			}
		}

		// disconnect from simulators:
		for (int i = 0; i < simCount; i++) {
			System.out.println("Disconnecting from simulator: "
					+ simulators[i].getSimId());
			simMaster.disconnectFromSimulator(simulators[i].getSimId());
			simulators[i] = null;
		}

		// close the results file
		resOut.close();

	}

	public NodeProperties readProperties(String propFileName)
			throws NodePropertiesInitializationException {
		FileNodePropertiesReader reader = FileNodePropertiesReader
				.loadPropertiesFromClassPath(propFileName, null);
		NodeProperties properties = reader.getNodeProperties();
		return properties;
	}

}
