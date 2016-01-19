package edu.atilim.acma.search;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;


public class BeamSearchAlgorithm extends AbstractAlgorithm {
	
	private Set<SolutionDesign> solutions;
	
	private int beamLength;
	private int randomDepth;

	public BeamSearchAlgorithm(SolutionDesign initialDesign, AlgorithmObserver observer, int beamLength, int randomDepth) {
		super(initialDesign, observer);
			
		this.beamLength = beamLength;
		this.randomDepth = randomDepth;
		this.solutions = new HashSet<SolutionDesign>();
		
		generateInitialPopulation();
	}
	
	private void generateInitialPopulation() {
		for (int i = 0; i < beamLength; i++) {
			solutions.add(initialDesign.getRandomNeighbor(randomDepth));
		}
	}

	@Override
	public String getName() {
		return "Beam Search";
	}
	
	@Override
	protected void beforeStart() {
		AlgorithmObserver observer = getObserver();
		if (observer != null) {
			observer.onStart(this, initialDesign);
		}
	}
	
	@Override
	protected void afterFinish() {
		AlgorithmObserver observer = getObserver();
		if (observer != null) {
			observer.onFinish(this, finalDesign);
		}
	}

	@Override
	public boolean step() {
		log("Starting generation %d", getStepCount());
		
		SortedSet<SolutionDesign> found = new TreeSet<SolutionDesign>();
		
		for (SolutionDesign sd : solutions) {
			for (SolutionDesign nb : sd) {
				found.add(nb);
				
				if (found.size() > beamLength)
					found.remove(found.first());
			}
		}
		
		log("Finished generation %d. Best score: %.6f", getStepCount(), found.last().getScore());
		
		solutions = found;
		
		return false;
	}
}
