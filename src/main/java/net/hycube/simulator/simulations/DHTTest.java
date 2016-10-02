package net.hycube.simulator.simulations;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import net.hycube.configuration.GlobalConstants;
import net.hycube.core.HyCubeNodeId;
import net.hycube.core.NodeId;
import net.hycube.core.NodePointer;
import net.hycube.dht.HyCubeResource;
import net.hycube.dht.HyCubeResourceDescriptor;
import net.hycube.dht.HyCubeRoutingDHTManager;
import net.hycube.environment.FileNodePropertiesReader;
import net.hycube.environment.NodeProperties;
import net.hycube.environment.NodePropertiesConversionException;
import net.hycube.environment.NodePropertiesInitializationException;
import net.hycube.random.RandomMultiple;
import net.hycube.simulator.Simulation;
import net.hycube.simulator.SimulatorMaster;
import net.hycube.simulator.SimulatorMasterException;
import net.hycube.simulator.SimulatorServiceException;
import net.hycube.simulator.SimulatorServiceProxy;
import net.hycube.simulator.SimulatorServiceProxyException;
import net.hycube.simulator.WeightSimulatorPointer;
import net.hycube.utils.ArrayUtils;
import net.hycube.utils.FileUtils;
import net.hycube.utils.HashMapUtils;
import net.hycube.utils.ObjectToStringConverter.MappedType;

public class DHTTest implements Simulation {

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

	protected static final String PROP_KEY_TESTS_NUM = "TestsNum";

	protected static final String PROP_KEY_RUN_MEMORY_CLEAR_EXTENSION = "RunMemoryClearExtension";


	protected static final String PROP_KEY_K = "K";
	protected static final String PROP_KEY_WAIT_TIME_AFTER_PUT = "WaitTimeAfterPut";
	protected static final String PROP_KEY_WAIT_TIME_BEFORE_GET = "WaitTimeBeforeGet";
	protected static final String PROP_KEY_REFRESH_RESOURCES = "RefreshResources";
	protected static final String PROP_KEY_DELETE_RESOURCES = "DeleteResources";
	protected static final String PROP_KEY_EXACT_PUT = "ExactPut";
	protected static final String PROP_KEY_EXACT_REFRESH_PUT = "ExactRefreshPut";
	protected static final String PROP_KEY_EXACT_GET = "ExactGet";
	protected static final String PROP_KEY_EXACT_DELETE = "ExactDelete";
	protected static final String PROP_KEY_SKIP_PUT_NODES_NUM = "SkipPutNodesNum";
	protected static final String PROP_KEY_GET_FROM_CLOSEST = "GetFromClosest";
	protected static final String PROP_KEY_SET_PUT_RECIPIENT = "SetPutRecipient";
	protected static final String PROP_KEY_SET_GET_RECIPIENT = "SetGetRecipient";
	protected static final String PROP_KEY_SECURE_ROUTING = "SecureRouting";
	protected static final String PROP_KEY_REGISTER_ROUTE = "RegisterRoute";
	protected static final String PROP_KEY_ANONYMOUS_ROUTE = "AnonymousRoute";
	protected static final String PROP_KEY_REPLICATE_NUM = "ReplicateNum";



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

	protected static final int SLEEP_TIME_AFTER_REPLICATE = 50;
	protected static final int SLEEP_TIME_AFTER_REPLICATES = 8000;

	protected static final int SLEEP_TIME_AFTER_DHT_PURGE = 20;
	protected static final int SLEEP_TIME_AFTER_DHT_PURGES = 2000;

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




		int testsNum = (Integer) properties.getProperty(PROP_KEY_TESTS_NUM, MappedType.INT);

		resOut.write("TestsNum: " + testsNum);
		resOut.newLine();
		resOut.flush();








		//generate the test pairs of nodes
		int currDiscardNodeIndexTemp = 0;
		int[][] froms = new int[discardNodesNumsInSteps.length][];
		int[][] tos = new int[discardNodesNumsInSteps.length][];
		NodeId[][] randomLookupNodeIds = new NodeId[discardNodesNumsInSteps.length][];
		HashSet<Integer> nodeSetTemp = new HashSet<Integer>();
		for (int i = 0; i < numNodes; i++) nodeSetTemp.add(i);
		for (int step = 0; step < discardNodesNumsInSteps.length; step++) {
			froms[step] = new int[testsNum];
			tos[step] = new int[testsNum];
			randomLookupNodeIds[step] = new NodeId[testsNum];
			for (int i = 0; i < discardNodesNumsInSteps[step]; i++, currDiscardNodeIndexTemp++) {
				int indexToDrop = indexesToDrop[currDiscardNodeIndexTemp];
				nodeSetTemp.remove(indexToDrop);
			}
			for (int i = 0; i < testsNum; i++) {
				int f = rand.nextInt(nodeSetTemp.size());
				int t = rand.nextInt(nodeSetTemp.size());
				froms[step][i] = (Integer) nodeSetTemp.toArray()[f];
				tos[step][i] = (Integer) nodeSetTemp.toArray()[t];
				randomLookupNodeIds[step][i] = HyCubeNodeId.generateRandomNodeId(4, 32);
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

				System.out.println("Joining node: " + addresses[i]
						+ ", bootstrap: " + bootstrap + " on similator: "
						+ simulators[nodeSimMap.get(i)].getSimId());
				simulators[nodeSimMap.get(i)].getSimulatorServiceProxy().join(
						i, nodeId, addresses[i], bootstrap);

				Thread.sleep(SLEEP_TIME_AFTER_JOIN);

			}

			Thread.sleep(SLEEP_TIME_AFTER_JOINS);



			for (int r = 0; r < recoveryRepeat; r++) {
				for (int i = numNodes - 1; i >= 0; i--) {
					System.out.println("Recoverying node: " + i + " ("
							+ (r + 1) + ")");
					simulators[nodeSimMap.get(i)].getSimulatorServiceProxy()
					.recoverNode(i);

					Thread.sleep(SLEEP_TIME_AFTER_RECOVERY);

				}
				System.out.println();
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
							System.out.println("Calling extension: " + extKey
									+ " (" + (rep + 1) + "), node: " + i);
							simulators[nodeSimMap.get(i)]
									.getSimulatorServiceProxy().callExtension(
											i, extKey);
							Thread.sleep(SLEEP_TIME_AFTER_NODE_MEMORY_CLEAR);
						}
					}
					Thread.sleep(SLEEP_TIME_AFTER_NODE_MEMORY_CLEARS);
				}
			}

			HashSet<Integer> nodesDiscarded = new HashSet<Integer>();




			// *************************************************
			// run tests for defined discard percentages:

			resOut.newLine();
			resOut.flush();





			//get the DHT simulation parameters


			int k = (Integer) simulationProperties.getProperty(PROP_KEY_K, MappedType.INT);

			int waitTimeAfterPut = (Integer) simulationProperties.getProperty(PROP_KEY_WAIT_TIME_AFTER_PUT, MappedType.INT);
			int waitTimeBeforeGet = (Integer) simulationProperties.getProperty(PROP_KEY_WAIT_TIME_BEFORE_GET, MappedType.INT);
			boolean refreshResources = (Boolean) simulationProperties.getProperty(PROP_KEY_REFRESH_RESOURCES, MappedType.BOOLEAN);
			boolean deleteResources = (Boolean) simulationProperties.getProperty(PROP_KEY_DELETE_RESOURCES, MappedType.BOOLEAN);

			boolean exactPut = (Boolean) simulationProperties.getProperty(PROP_KEY_EXACT_PUT, MappedType.BOOLEAN);		
			boolean exactRefreshPut = (Boolean) simulationProperties.getProperty(PROP_KEY_EXACT_REFRESH_PUT, MappedType.BOOLEAN);

			boolean exactGet = (Boolean) simulationProperties.getProperty(PROP_KEY_EXACT_GET, MappedType.BOOLEAN);

			boolean exactDelete = (Boolean) simulationProperties.getProperty(PROP_KEY_EXACT_DELETE, MappedType.BOOLEAN);

			int skipPutNodesNum = (Integer) simulationProperties.getProperty(PROP_KEY_SKIP_PUT_NODES_NUM, MappedType.INT);				

			boolean getFromClosest = (Boolean) simulationProperties.getProperty(PROP_KEY_GET_FROM_CLOSEST, MappedType.BOOLEAN);

			boolean setPutRecipient = (Boolean) simulationProperties.getProperty(PROP_KEY_SET_PUT_RECIPIENT, MappedType.BOOLEAN);		// if false -> recipient = null and only one message will be routed, exact set must be false
			boolean setGetRecipient = (Boolean) simulationProperties.getProperty(PROP_KEY_SET_GET_RECIPIENT, MappedType.BOOLEAN);		// if false -> recipient = null and only one message will be routed, exact get must be false

			boolean secureRouting = (Boolean) simulationProperties.getProperty(PROP_KEY_SECURE_ROUTING, MappedType.BOOLEAN);

			boolean registerRoute = (Boolean) simulationProperties.getProperty(PROP_KEY_REGISTER_ROUTE, MappedType.BOOLEAN);
			
			boolean anonymousRoute = (Boolean) simulationProperties.getProperty(PROP_KEY_ANONYMOUS_ROUTE, MappedType.BOOLEAN);

			int replicateNum = (Integer) simulationProperties.getProperty(PROP_KEY_REPLICATE_NUM, MappedType.INT);




			int[] founds = new int[k+1];
			ArrayList<Object[]> tests = new ArrayList<Object[]>();

			resOut.write("\t\tReplicas numbers: ");
			resOut.flush();
			for (int t = 0; t < testsNum; t++) {

				//					NodeId resNodeId = HyCubeNodeId.generateRandomNodeId(4, 32);
//				NodeId resNodeId = randomLookupNodeIds[step][t];
				NodeId resNodeId = randomLookupNodeIds[0][t];

				BigInteger key = resNodeId.getBigInteger();


				int replicasNum = 0;
				System.out.print("Checking replicas num: ");

				int ii = 0;

				for (int index = 0; index < numNodes; index ++) {
					if (nodesDiscarded.contains(index)) continue;

					System.out.print(ii + ", ");
					ii++;

					boolean isReplica = (Boolean) simulators[nodeSimMap.get(index)].getSimulatorServiceProxy().isReplica(index, key, nodeIds[index], k);
					if (isReplica) replicasNum++;

				}
				System.out.println();

				resOut.write(replicasNum + ", ");
				resOut.flush();


				int pNodeInd;
				//					pNodeInd = rand.nextInt(numNodes);
				//					while (nodesDiscarded.contains(pNodeInd)) {
				//						pNodeInd = rand.nextInt(numNodes);
				//					}
//				pNodeInd = froms[step][t];
				pNodeInd = froms[0][t];

				int resId = rand.nextInt();


				HyCubeResourceDescriptor rd = new HyCubeResourceDescriptor(HyCubeResourceDescriptor.OPEN_BRACKET + HyCubeResourceDescriptor.KEY_RESOURCE_ID + HyCubeResourceDescriptor.EQUALS + Integer.toString(resId) + HyCubeResourceDescriptor.CLOSE_BRACKET);
				String resourceUrl = Integer.toString(pNodeInd) + "::" + rd.getResourceId();
				rd.setResourceUrl(resourceUrl);
				HyCubeResource res = new HyCubeResource(rd, ("DATA" + Integer.toString(resId).toString()).getBytes());




				//put

				System.out.println("Sending put: ");

				NodePointer[] storeNodes = search(simulators[nodeSimMap.get(pNodeInd)].getSimulatorServiceProxy(), pNodeInd, resNodeId, k, false);

				if (skipPutNodesNum > 0) {
					skipPutNodesNum = Math.min(skipPutNodesNum, storeNodes.length);
					storeNodes = Arrays.copyOfRange(storeNodes, skipPutNodesNum, storeNodes.length);
				}

				tests.add(new Object[] {resNodeId, key, res, pNodeInd, storeNodes});

				if (setPutRecipient) {
					for (NodePointer np : storeNodes) {
						System.out.print("@");
						put(simulators[nodeSimMap.get(pNodeInd)].getSimulatorServiceProxy(), pNodeInd, np, key, res, exactPut, secureRouting, registerRoute, anonymousRoute);
					}
				}
				else {
					System.out.print("@");
					put(simulators[nodeSimMap.get(pNodeInd)].getSimulatorServiceProxy(), pNodeInd, null, key, res, exactPut, secureRouting, registerRoute, anonymousRoute);
				}

				System.out.println();



			}

			resOut.newLine();
			resOut.flush();



			if (replicateNum > 0) {
				String[] keepAliveBackgroundProcessesToRunOnce = new String[] {
						"HyCubeDHTReplicationBackgroundProcess"
				};
				for (int rep = 0; rep < replicateNum; rep++) { // 2 repetitions are enough
					// to remove failed nodes
					for (String bpKey : keepAliveBackgroundProcessesToRunOnce) {
						for (int index = 0; index < numNodes; index++) {
							if (nodesDiscarded.contains(index))
								continue;
							System.out
							.println("Processing (once) background process: "
									+ bpKey
									+ " ("
									+ (rep + 1)
									+ "), node: " + index);
							simulators[nodeSimMap.get(index)]
									.getSimulatorServiceProxy()
									.processBackgroundProcess(index, bpKey);
							Thread.sleep(SLEEP_TIME_AFTER_REPLICATE);
						}
						Thread.sleep(SLEEP_TIME_AFTER_REPLICATES);

					}
				}	
			}



			//wait after put:

			try {
				Thread.sleep(waitTimeAfterPut);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}






			if (deleteResources) {

				for (int t = 0; t < testsNum; t++) {

					System.out.println("Sending delete: ");

					//NodeId resNodeId = (NodeId) tests.get(t)[0];
					BigInteger key = (BigInteger) tests.get(t)[1];
					HyCubeResource res = (HyCubeResource) tests.get(t)[2];
					int pNodeInd = (Integer) tests.get(t)[3];

					//NodePointer[] deleteNodes = search(simulators[nodeSimMap.get(pNodeInd)].getSimulatorServiceProxy(), pNodeInd, resNodeId, k, false);
					NodePointer[] deleteNodes = (NodePointer[]) tests.get(t)[4];

					for (NodePointer np : deleteNodes) {
						System.out.print("@");
						delete(simulators[nodeSimMap.get(pNodeInd)].getSimulatorServiceProxy(), pNodeInd, np, key, res.getResourceDescriptor(), exactDelete, secureRouting, registerRoute, anonymousRoute);
					}

					System.out.println();

				}
			}


			if (refreshResources) {

				for (int t = 0; t < testsNum; t++) {

					System.out.println("Sending refresh: ");

					//NodeId resNodeId = (NodeId) tests.get(t)[0];
					BigInteger key = (BigInteger) tests.get(t)[1];
					HyCubeResource res = (HyCubeResource) tests.get(t)[2];
					int pNodeInd = (Integer) tests.get(t)[3];

					//NodePointer[] refreshNodes = search(simulators[nodeSimMap.get(pNodeInd)].getSimulatorServiceProxy(), pNodeInd, resNodeId, k, false);
					NodePointer[] refreshNodes = (NodePointer[]) tests.get(t)[4];

					for (NodePointer np : refreshNodes) {
						System.out.print("@");
						refreshPut(simulators[nodeSimMap.get(pNodeInd)].getSimulatorServiceProxy(), pNodeInd, np, key, res.getResourceDescriptor(), exactRefreshPut, secureRouting, registerRoute, anonymousRoute);
					}

					System.out.println();

				}



			}





			int currDiscardNodeIndex = 0;
			for (int step = 0; step < discardNodesNumsInSteps.length; step++) {

				// clear the simulators:
				for (int i = 0; i < simulators.length; i++) {
					simulators[i].getSimulatorServiceProxy().resetStats();
				}

				// discard nodes

				for (int i = 0; i < discardNodesNumsInSteps[step]; i++, currDiscardNodeIndex++) {
					int indexToDrop = indexesToDrop[currDiscardNodeIndex];
					System.out.println("Dropping node: " + indexToDrop);
					nodesDiscarded.add(indexToDrop);
					simulators[nodeSimMap.get(indexToDrop)]
							.getSimulatorServiceProxy()
							.discardNode(indexToDrop);

					simulators[nodeSimMap.get(indexToDrop)].setNumNodes(simulators[nodeSimMap.get(indexToDrop)].getNumNodes() - 1);

					nodeSimMap.remove(indexToDrop);


					Thread.sleep(SLEEP_TIME_AFTER_DROP);
				}

				Thread.sleep(SLEEP_TIME_AFTER_DROPS);

				String[] keepAliveBackgroundProcessesToRunOnce = new String[] {
						"HyCubePingBackgroundProcess",
				"HyCubeAwaitingPongsBackgroundProcess" };
				for (int rep = 0; rep < 2; rep++) { // 2 repetitions are enough
					// to remove failed nodes
					for (String bpKey : keepAliveBackgroundProcessesToRunOnce) {
						for (int i = 0; i < numNodes; i++) {
							if (nodesDiscarded.contains(i))
								continue;
							System.out
							.println("Processing (once) background process: "
									+ bpKey
									+ " ("
									+ (rep + 1)
									+ "), node: " + i);
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
				resOut.flush();





				
				//reset the counters
				
				founds = new int[k+1];				


				//wait before get:

				try {
					Thread.sleep(waitTimeBeforeGet);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}



				System.out.println("Purging DHT...");

				String[] purgeDHTBackgroundProcessesToRunOnce = new String[] {
						"HyCubeDHTBackgroundProcess",
				};
				for (int rep = 0; rep < 1; rep++) { // 2 repetitions are enough
					// to remove failed nodes
					for (String bpKey : purgeDHTBackgroundProcessesToRunOnce) {
						for (int i = 0; i < numNodes; i++) {
							if (nodesDiscarded.contains(i))
								continue;
							System.out
							.println("Processing (once) background process: "
									+ bpKey
									+ " ("
									+ (rep + 1)
									+ "), node: " + i);
							simulators[nodeSimMap.get(i)]
									.getSimulatorServiceProxy()
									.processBackgroundProcess(i, bpKey);
							Thread.sleep(SLEEP_TIME_AFTER_DHT_PURGE);
						}
						Thread.sleep(SLEEP_TIME_AFTER_DHT_PURGES);
					}

				}






				for (int t = 0; t < testsNum; t++) {


					int gNodeInd;
					//					gNodeInd = rand.nextInt(numNodes);
					//					while (nodesDiscarded.contains(gNodeInd)) {
					//						gNodeInd = rand.nextInt(numNodes);
					//					}
					gNodeInd = tos[step][t];


					//get

					System.out.println("Sending gets ");

					NodePointer[] getNodes = search(simulators[nodeSimMap.get(gNodeInd)].getSimulatorServiceProxy(), gNodeInd, (NodeId) tests.get(t)[0], k, false);

					HyCubeResource[] resRetrieved = null;
					int found = 0;
					if (setGetRecipient) {
						for (NodePointer np : getNodes) {
							System.out.print("@");
							resRetrieved = get(simulators[nodeSimMap.get(gNodeInd)].getSimulatorServiceProxy(), gNodeInd, np, (BigInteger)tests.get(t)[1], ((HyCubeResource)tests.get(t)[2]).getResourceDescriptor(), exactGet, getFromClosest, secureRouting, registerRoute, anonymousRoute);
							if (resRetrieved.length > 0) {
								found++;
							}
						}
					}
					else {
						System.out.print("@");
						resRetrieved = get(simulators[nodeSimMap.get(gNodeInd)].getSimulatorServiceProxy(), gNodeInd, null, (BigInteger)tests.get(t)[1], ((HyCubeResource)tests.get(t)[2]).getResourceDescriptor(), exactGet, getFromClosest, secureRouting, registerRoute, anonymousRoute);
						if (resRetrieved.length > 0) {
							found++;
						}
					}
					System.out.println();

					founds[found]++;



					System.out.println();

				}

				resOut.write("\t\tK = " + k);
				resOut.newLine();
				for (int i = 0; i < founds.length; i++) {
					resOut.write("\t\t" + i + " : " + founds[i]);
					resOut.newLine();
				}
				resOut.flush();


				resOut.newLine();
				resOut.flush();




				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}


			}



			// finishing the simulations -> discard remaining nodes and remove
			// from the simulator

			System.out.println("Disconnecting nodes...");

			for (int i : nodeSimMap.keySet()) {

				System.out.println("Leaving: " + i);
				simulators[nodeSimMap.get(i)].getSimulatorServiceProxy().leave(
						i);

				System.out.println("Dropping: " + i);
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




	private static NodePointer[] search(SimulatorServiceProxy serv, int simNodeId, NodeId searchNodeId, int k, boolean ignoreTargetNode) throws SimulatorServiceException, SimulatorServiceProxyException {

		NodePointer[] searchResults = serv.search(simNodeId, searchNodeId, (short) k, ignoreTargetNode, null);

		return searchResults;

	}




	private static boolean put(SimulatorServiceProxy serv, int simNodeId, NodePointer np, BigInteger key, HyCubeResource res, boolean exactPut, boolean secureRouting, boolean registerRoute, boolean anonymousRoute) throws SimulatorServiceException, SimulatorServiceProxyException {

		return serv.put(simNodeId, np, key, res, HyCubeRoutingDHTManager.createPutParameters(exactPut, secureRouting, false, registerRoute, anonymousRoute));

	}

	private static boolean refreshPut(SimulatorServiceProxy serv, int simNodeId, NodePointer np, BigInteger key, HyCubeResourceDescriptor rd, boolean exactRefreshPut, boolean secureRouting, boolean registerRoute, boolean anonymousRoute) throws SimulatorServiceException, SimulatorServiceProxyException {

		return serv.refreshPut(simNodeId, np, key, rd, HyCubeRoutingDHTManager.createRefreshParameters(exactRefreshPut, secureRouting, false, registerRoute, anonymousRoute));

	}


	private static HyCubeResource[] get(SimulatorServiceProxy serv, int simNodeId, NodePointer np, BigInteger key, HyCubeResourceDescriptor rd, boolean exactGet, boolean getFromClosest, boolean secureRouting, boolean registerRoute, boolean anonymousRoute) throws SimulatorServiceException, SimulatorServiceProxyException {

		return serv.get(simNodeId, np, key, rd, HyCubeRoutingDHTManager.createGetParameters(exactGet, getFromClosest, secureRouting, false, registerRoute, anonymousRoute));

	}

	private static boolean delete(SimulatorServiceProxy serv, int simNodeId, NodePointer np, BigInteger key, HyCubeResourceDescriptor rd, boolean exactDelete, boolean secureRouting, boolean registerRoute, boolean anonymousRoute) throws SimulatorServiceException, SimulatorServiceProxyException {

		return serv.delete(simNodeId, np, key, rd, HyCubeRoutingDHTManager.createDeleteParameters(exactDelete, secureRouting, false, registerRoute, anonymousRoute));

	}



}
