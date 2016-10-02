package net.hycube.simulator;

public class WeightSimulatorPointer extends SimulatorPointer {
	
	protected int weight;
	protected int maxNumNodes;
	protected int numNodes;
	
	
	public int getWeight() {
		return weight;
	}
	
	public void setWeight(int weight) {
		this.weight = weight;
	}
	
	public int getMaxNumNodes() {
		return maxNumNodes;
	}
	
	public void setMaxNumNodes(int maxNumNodes) {
		this.maxNumNodes = maxNumNodes;
	}
	
	public int getNumNodes() {
		return numNodes;
	}
	
	public void setNumNodes(int numNodes) {
		this.numNodes = numNodes;
	}
	
	
	public WeightSimulatorPointer(String simId, String simCommandsConnectionUrl, String simMessageConnectionUrl, int weight, int maxNumNodes) {
		super(simId, simCommandsConnectionUrl, simMessageConnectionUrl);
		this.weight = weight;
		this.maxNumNodes = maxNumNodes;
	}

	
}
