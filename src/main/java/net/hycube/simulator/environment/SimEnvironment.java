package net.hycube.simulator.environment;

import net.hycube.core.InitializationException;
import net.hycube.environment.Environment;
import net.hycube.environment.NodeProperties;
import net.hycube.environment.NodePropertiesInitializationException;
import net.hycube.environment.TimeProvider;
import net.hycube.environment.TimeProviderEventScheduler;
import net.hycube.simulator.log.LogHelper;
import net.hycube.simulator.stat.MessageStat;
import net.hycube.simulator.transport.SimNetworkProxy;

public class SimEnvironment extends Environment {
	
	
	private static org.apache.commons.logging.Log userLog = LogHelper.getUserLog();
	private static org.apache.commons.logging.Log devLog = LogHelper.getDevLog(SimEnvironment.class);
	
	
	protected TimeProvider timeProvider;
	protected TimeProviderEventScheduler eventScheduler;
	protected SimNetworkProxy simNetworkProxy;
	protected NodeProperties nodeProperties;
	
	protected MessageStat messageStat;

	
	protected SimEnvironment() {
		
	}
	
	public static SimEnvironment initialize(SimNetworkProxy simNetworkProxy, TimeProvider simTimeProvider) throws InitializationException {
		return initialize((String)null, (String)null, simNetworkProxy, simTimeProvider);
	}
	
	public static SimEnvironment initialize(String propertiesFileName, SimNetworkProxy simNetworkProxy, TimeProvider simTimeProvider) throws InitializationException {
		return initialize(propertiesFileName, (String)null, simNetworkProxy, simTimeProvider);
	}
	
	public static SimEnvironment initialize(String propertiesFileName, String configurationNS, SimNetworkProxy simNetworkProxy, TimeProvider simTimeProvider) throws InitializationException {
		
		SimEnvironment simEnvironment = new SimEnvironment();
		
		simEnvironment.simNetworkProxy = simNetworkProxy;
		
		simEnvironment.timeProvider = simTimeProvider;
		
		simEnvironment.eventScheduler = new TimeProviderEventScheduler(simEnvironment.timeProvider);
		
		try {
			simEnvironment.readProperties(propertiesFileName, configurationNS, TRIM_PROP_VALUES);
		} catch (NodePropertiesInitializationException e) {
			userLog.error("An error occured while trying to read the configuration files.");
			devLog.error("An error occured while trying to read the configuration files.", e);
			throw new InitializationException(InitializationException.Error.PARAMETERS_READ_ERROR, null, "An error occured while trying to read the configuration files.", e);
		}

		

		simEnvironment.messageStat = new MessageStat(0, 0, 0);
		
		
		return simEnvironment;
		
		
	}
	
	
	
	public static SimEnvironment initializeWithDefaultProperties(String defaultPropertiesFileName, String propertiesFileName, SimNetworkProxy simNetworkProxy, TimeProvider simTimeProvider) throws InitializationException {
		return initializeWithDefaultProperties(defaultPropertiesFileName, propertiesFileName, (String)null, simNetworkProxy, simTimeProvider);
	}
	
	public static SimEnvironment initializeWithDefaultProperties(String defaultPropertiesFileName, String propertiesFileName, String configurationNS, SimNetworkProxy simNetworkProxy, TimeProvider simTimeProvider) throws InitializationException {
		
		SimEnvironment simEnvironment = new SimEnvironment();
		
		simEnvironment.simNetworkProxy = simNetworkProxy;
		
		simEnvironment.timeProvider = simTimeProvider;
		
		simEnvironment.eventScheduler = new TimeProviderEventScheduler(simEnvironment.timeProvider);
		
		try {
			simEnvironment.readProperties(propertiesFileName, configurationNS, TRIM_PROP_VALUES);
		} catch (NodePropertiesInitializationException e) {
			userLog.error("An error occured while trying to read the configuration files.");
			devLog.error("An error occured while trying to read the configuration files.", e);
			throw new InitializationException(InitializationException.Error.PARAMETERS_READ_ERROR, null, "An error occured while trying to read the configuration files.", e);
		}

		

		simEnvironment.messageStat = new MessageStat(0, 0, 0);
		
		
		return simEnvironment;
		
		
	}

	
	@Override
	public TimeProvider getTimeProvider() {
		return timeProvider;
	}
	
	@Override
	public TimeProviderEventScheduler getEventScheduler() {
		return eventScheduler;
	}
	
	public SimNetworkProxy getSimNetworkProxy() {
		return simNetworkProxy;
	}



	public MessageStat getMessageStat() {
		return messageStat;
	}
	

	public void resetMessageStat() {
		
		synchronized (messageStat) {
			messageStat.setMsgSentCounter(0);
			messageStat.setMsgDeliveredCounter(0);
			messageStat.setMsgRouteLengthSum(0);
			
		}
		
	}
	

}
