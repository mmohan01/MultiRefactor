package edu.atilim.acma.search;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import edu.atilim.acma.RunConfig;
import edu.atilim.acma.design.Design;

public class ConcurrentHillClimbing extends ConcurrentMultiRunAlgorithm {
	private int resCount;
	private int resDepth;
	private boolean firstDescent;
	private int maxIterations = 0;
	
	public ConcurrentHillClimbing() {
	}

	public ConcurrentHillClimbing(String name, RunConfig config, Design initialDesign, int resCount, int resDepth, 
			boolean firstDescent, int runCount) {
		super(name, config, initialDesign, runCount);
		
		this.resCount = resCount;
		this.resDepth = resDepth;
		this.firstDescent = firstDescent;
	}
	
	public ConcurrentHillClimbing(String name, RunConfig config, Design initialDesign, int resCount, int resDepth, 
			boolean firstDescent, int maxIterations, int runCount) {
		super(name, config, initialDesign, runCount);
		
		this.resCount = resCount;
		this.resDepth = resDepth;
		this.firstDescent = firstDescent;
		this.maxIterations = maxIterations;
	}
	
	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		
		out.writeInt(0); //version
		out.writeInt(resCount);
		out.writeInt(resDepth);
		out.writeBoolean(firstDescent);
		out.writeInt(maxIterations);
	}
	
	@Override
	public AbstractAlgorithm spawnAlgorithm() {
		HillClimbingAlgorithm algo = new HillClimbingAlgorithm(new SolutionDesign(getInitialDesign(), getConfig()), null);
		algo.setRestartCount(resCount);
		algo.setRestartDepth(resDepth);
		algo.setFirstDescent(firstDescent);
		algo.setMaxIterations(maxIterations);
		return algo;
	}
	
	@Override
	public String getRunInfo() {
		/*
		if (resCount > 0)
			return String.format("Multiple First Descend. Restart Count: %d, Depth: %d.", resCount, resDepth);
		else
			return String.format("Hill Climbing. Restart Count: %d, Depth: %d.", resCount, resDepth);
		*/
		
		if (maxIterations > 0)
			if (firstDescent)
				return String.format("First Descent Hill Climbing. Restart Count: %d. Depth: %d. Maximum Iterations: %d.", resCount, resDepth, maxIterations);
			else
				return String.format("Steepest Descent Hill Climbing. Restart Count: %d. Depth: %d. Maximum Iterations: %d.", resCount, resDepth, maxIterations);
		else
			if (firstDescent)
				return String.format("First Descent Hill Climbing. Restart Count: %d. Depth: %d.", resCount, resDepth);
			else
				return String.format("Steepest Descent Hill Climbing. Restart Count: %d. Depth: %d.", resCount, resDepth);
			
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		
		in.readInt();
		resCount = in.readInt();
		resDepth = in.readInt();
		firstDescent = in.readBoolean();
		maxIterations = in.readInt();
	}
}
