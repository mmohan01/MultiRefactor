package edu.atilim.acma.search;


public class RandomSearchAlgorithm extends AbstractAlgorithm {
	
	private SolutionDesign current;
	private SolutionDesign best;
	
	private int maxIters;

	public RandomSearchAlgorithm(SolutionDesign initialDesign, AlgorithmObserver observer, int maxIters) {
		super(initialDesign, observer);
		
		current = best = initialDesign;
		this.maxIters = maxIters;
	}

	@Override
	public String getName() {
		return "Random Search";
	}
	
	@Override
	protected void beforeStart() {
		AlgorithmObserver observer = getObserver();
		if (observer != null) {
			observer.onStart(this, initialDesign);
			observer.onAdvance(this, 0, maxIters);
			observer.onUpdateItems(this, current, best, AlgorithmObserver.UPDATE_BEST & AlgorithmObserver.UPDATE_CURRENT);
		}
	}
	
	@Override
	protected void afterFinish() {
		AlgorithmObserver observer = getObserver();
		if (observer != null) {
			observer.onAdvance(this, maxIters, maxIters);
			observer.onFinish(this, best);
		}
	}

	@Override
	public boolean step() {
		AlgorithmObserver observer = getObserver();
		
		log("Starting iteration %d. Current score: %.6f, Best score: %.6f", getStepCount(), current.getScore(), best.getScore());
		
		if (getStepCount() > maxIters) {
			log("Algorithm finished, the final design score: %.6f", best.getScore());
			finalDesign = best;
			return true;
		}
		
		SolutionDesign randomNeighbor = current.getRandomNeighbor();
		
		if (randomNeighbor.isBetterThan(best)) {
			best = randomNeighbor;
			
			if (observer != null) {
				observer.onUpdateItems(this, current, best, AlgorithmObserver.UPDATE_BEST);
			}
		}
		
		current = randomNeighbor;
		
		if (observer != null) {
			observer.onUpdateItems(this, current, best, AlgorithmObserver.UPDATE_CURRENT);
		}
		
		if (observer != null) {
			observer.onAdvance(this, getStepCount() + 1, maxIters);
		}
		
		return false;
	}
}
