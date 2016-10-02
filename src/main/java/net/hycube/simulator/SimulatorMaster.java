package net.hycube.simulator;

import java.util.HashMap;

public class SimulatorMaster {
	
	
	public static final String COMMENT_PREFIX = "#";
	
	public static final String COMMAND_EXIT = 					"exit";
	
	public static final String COMMAND_CONNECT_CMD = 			"connectCmd";
	public static final String COMMAND_DISCONNECT_CMD = 		"disconnectCmd";
	public static final String COMMAND_CONNECT_MSG = 			"connectMsg";
	public static final String COMMAND_DISCONNECT_MSG = 		"disconnectMsg";
	
	public static final String COMMAND_CLEAR_SIMULATOR = 		"clearSimulator";
	
	public static final String COMMAND_JOIN = 					"join";
	public static final String COMMAND_LEAVE = 					"leave";
	public static final String COMMAND_DROP_NODE = 				"dropNode";
	
	public static final String COMMAND_RECOVER_NODE = 			"recoverNode";
	public static final String COMMAND_RECOVER_NODE_NS = 		"recoverNodeNS";
	public static final String COMMAND_RECOVER_ALL_NODES = 		"recoverAllNodes";
	public static final String COMMAND_RECOVER_ALL_NODES_NS = 	"recoverAllNodesNS";
	
	public static final String COMMAND_ROUTE_MSG_ASYNC = 		"routeMessageAsync";
	public static final String COMMAND_ROUTE_MSG_SYNC = 		"routeMessageSync";
	
	public static final String COMMAND_PRINT_ROUTING_STATISTICS = "printRoutingStatistics";	
	
	public static final String COMMAND_LOOKUP = 				"lookup";
	public static final String COMMAND_SEARCH = 				"search";
	
	
	
	
	protected SimulatorServiceProxyFactory simProxyFactory;
	protected HashMap<String, SimulatorPointer> simulators;
	
	
	
	protected SimulatorMaster() {
		
	}
	
	
	public static SimulatorMaster initialize(SimulatorServiceProxyFactory simProxyFactory) {
		
		SimulatorMaster simMaster = new SimulatorMaster();
		
		simMaster.simProxyFactory = simProxyFactory;
		simMaster.simulators = new HashMap<String, SimulatorPointer>();
		
		
		return simMaster;
		
	}
	
	
	public SimulatorPointer getSimulatorPointer(String simId) {
		return simulators.get(simId);
	}
	
	

	
	
	public void connectToSimulator(String simId, String simCommandConnectionUrl, String simMessageConnectionUrl) throws SimulatorMasterException {

		SimulatorPointer simPointer = new SimulatorPointer(simId, simCommandConnectionUrl, simMessageConnectionUrl);
		simulators.put(simId, simPointer);
		
		SimulatorServiceProxy simProxy;
		try {
			simProxy = simProxyFactory.createSimulatorServiceProxy(simCommandConnectionUrl);
		} catch (SimulatorServiceProxyException e) {
			throw new SimulatorMasterException("An exception has been thrown while initializing the simulator service proxy.", e);
		}
		simPointer.setSimulatorServiceProxy(simProxy);
		
	}
	

	public void disconnectFromSimulator(String simId) throws SimulatorMasterException {

		simulators.remove(simId);
		
	}

	
	
	public void establishMessageConnection(String simIdFrom, String simIdTo) throws SimulatorMasterException {
		
		SimulatorPointer simFrom = simulators.get(simIdFrom);
		SimulatorPointer simTo = simulators.get(simIdTo);
		
		try {
			simFrom.getSimulatorServiceProxy().establishConnection(simIdTo, simTo.getSimMessageConnectionUrl());
		} catch (SimulatorServiceException e) {
			throw new SimulatorMasterException("An exception has been thrown while establishing messages connection between simulators.", e);
		} catch (SimulatorServiceProxyException e) {
			throw new SimulatorMasterException("An exception has been thrown while establishing messages connection between simulators.", e);
		}
		
	}
	
	
	public void closeMessageConnection(String simIdFrom, String simIdTo) throws SimulatorMasterException {
		
		SimulatorPointer simFrom = simulators.get(simIdFrom);
		
		try {
			simFrom.getSimulatorServiceProxy().closeConnection(simIdTo);
		} catch (SimulatorServiceException e) {
			throw new SimulatorMasterException("An exception has been thrown while establishing messages connection between simulators.", e);
		} catch (SimulatorServiceProxyException e) {
			throw new SimulatorMasterException("An exception has been thrown while establishing messages connection between simulators.", e);
		}
		
	}
	
	

	public void discard() {
		

		
		
	}
	
	
	
	
	
}
