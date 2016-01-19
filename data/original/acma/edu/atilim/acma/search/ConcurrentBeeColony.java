package edu.atilim.acma.search;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import edu.atilim.acma.RunConfig;
import edu.atilim.acma.design.Design;

public class ConcurrentBeeColony extends ConcurrentMultiRunAlgorithm {
	private int maxTrials;
	private int populationSize;
	private int iterations;

	public ConcurrentBeeColony() {
	}

	public ConcurrentBeeColony(String name, RunConfig config, Design initialDesign, int maxTrials, int populationSize, int iterations, int runCount) {
		super(name, config, initialDesign, runCount);
		
		this.maxTrials = maxTrials;
		this.populationSize = populationSize;
		this.iterations = iterations;
	}
	
	@Override
	public AbstractAlgorithm spawnAlgorithm() {
		return new BeeColonyAlgorithm(new SolutionDesign(getInitialDesign(), getConfig()), null, maxTrials, populationSize, iterations);
	}

	@Override
	public String getRunInfo() {
		return String.format("Artificial Bee Colony. Population Size: %d. Max Trials: %d. Iterations: %d.", populationSize, maxTrials, iterations);
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		
		out.writeInt(0); //version
		
		out.writeInt(maxTrials);
		out.writeInt(populationSize);
		out.writeInt(iterations);
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		
		in.readInt();
		maxTrials = in.readInt();
		populationSize = in.readInt();
		iterations = in.readInt();
	}
}
