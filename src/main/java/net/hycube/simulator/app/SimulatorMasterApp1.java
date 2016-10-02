package net.hycube.simulator.app;

import java.util.Arrays;

import net.hycube.simulator.Simulation;
import net.hycube.simulator.SimulatorMaster;
import net.hycube.simulator.SimulatorMasterException;
import net.hycube.simulator.SimulatorServiceProxyFactory;
import net.hycube.simulator.rmi.RMISimulatorServiceProxyFactory;
import net.hycube.utils.ClassInstanceLoadException;
import net.hycube.utils.ClassInstanceLoader;

public class SimulatorMasterApp1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	
		
		String usage = "Usage:\n"
				+ "args[0] - simulation class name\n";
		
		if (args.length < 1) {
			System.out.println(usage);
			return;
		}
		
		String simulationClassName = args[0];
		
		System.out.println("Initializing RMI simulator service proxy factory...");
		SimulatorServiceProxyFactory simProxyFactory = new RMISimulatorServiceProxyFactory();
		
		
		System.out.println("Initializing the simulator master...");
		SimulatorMaster simMaster = SimulatorMaster.initialize(simProxyFactory);
		
		
		System.out.println("Simulator master initialized.");
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
		}
		
		
		

		
		System.out.println("Creating the simulation class instance");
		Simulation simulation;
		try {
			simulation = (Simulation) ClassInstanceLoader.newInstance(simulationClassName, Simulation.class);
		} catch (ClassInstanceLoadException e) {
			e.printStackTrace();
			return;
		}
		
		
		String[] simArgs;
		if (args.length > 1) {
			//pass the remaining command line arguments to the simulation:
			simArgs = Arrays.copyOfRange(args, 1, args.length);
		}
		else simArgs = new String[0];
		
		
		
		System.out.println("Running the simulation...");
		try {
			simulation.runSimulation(simMaster, simArgs);
		} catch (SimulatorMasterException e) {
			e.printStackTrace();
			return;
		}
		
		
		
		
		System.out.println("Discarding simulator master...");
		simMaster.discard();
		
		System.out.println("Finished. #");
		
		
	}
	
	
}
