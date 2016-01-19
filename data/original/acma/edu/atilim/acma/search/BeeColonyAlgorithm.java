package edu.atilim.acma.search;

import java.util.ArrayList;
import java.util.UUID;

import edu.atilim.acma.util.ACMAUtil;

public class BeeColonyAlgorithm extends AbstractAlgorithm {
	private static final int randomDepth = 20;
	
	private int maxTrials = 70;
	private int populationSize = 50;
	private int iterations = 5000;
	
	private ArrayList<FoodSource> foods;
	private FoodSource best;

	public BeeColonyAlgorithm(SolutionDesign initialDesign,	AlgorithmObserver observer, int maxTrials, int populationSize, int iterations) {
		super(initialDesign, observer);
		
		this.maxTrials = maxTrials;
		this.populationSize = populationSize;
		this.iterations = iterations;
	}

	@Override
	public String getName() {
		return "Artificial Bee Colony";
	}
	
	@Override
	protected void beforeStart() {
		AlgorithmObserver observer = getObserver();
		if (observer != null) {
			observer.onStart(this, initialDesign);
			observer.onAdvance(this, 0, iterations);
		}
	}
	
	@Override
	protected void afterFinish() {
		AlgorithmObserver observer = getObserver();
		if (observer != null) {
			observer.onAdvance(this, iterations, iterations);
			observer.onFinish(this, best.design);
		}
	}

	@Override
	public boolean step() {
		AlgorithmObserver observer = getObserver();
		
		if (foods == null) {
			log("Generating initial food sources.");
			
			foods = new ArrayList<BeeColonyAlgorithm.FoodSource>();
			
			for (int i = 0; i < populationSize; i++) {
				foods.add(new FoodSource(initialDesign.getRandomNeighbor(randomDepth)));
			}
			
			log("Generated %d food sources.", populationSize);
		}
		
		if (getStepCount() > iterations) {
			finalDesign = best.design;
			return true;
		}
		
		if (best == null)
			log("Starting iteration %d.", getStepCount());
		else
			log("Starting iteration %d. Best score: %.6f", getStepCount(), best.design.getScore());
		
		sendEmployedBees();
		calculateProbabilities();
		sendOnlookerBees();
		memorizeBestSource();
		sendScoutBees();
		
		if (observer != null) {
			observer.onUpdateItems(this, best.design, best.design, AlgorithmObserver.UPDATE_BEST);
			observer.onAdvance(this, getStepCount(), iterations);
		}

		return false;
	}
	
	private void sendEmployedBees() {
		int better = 0;
		
		log("Sending employed bees.");
		
		for (int i = 0; i < foods.size(); i++) {
			FoodSource current = foods.get(i);
			FoodSource neighbor = current.mutate();
			
			if (neighbor.isBetterThan(current)) {
				foods.set(i, neighbor);
				better++;
			} else {
				current.trialCount++;
			}
		}
		
		if (getObserver() != null)
			getObserver().onExpansion(this, foods.size());
	}
	
	private void calculateProbabilities() {
		log("Calculating probabilities.");
		
		double maxFitness = 0.0;
		
		for (int i = 0; i < foods.size(); i++) {
			double currentFitness = foods.get(i).getFitness();
			
			if (currentFitness > maxFitness) 
				maxFitness = currentFitness;
		}
		
		for (int i = 0; i < foods.size(); i++) {
			FoodSource current = foods.get(i);
			
			current.probability = (0.9 * (current.getFitness() / maxFitness)) + 0.1;
		}
	}
	
	private void sendOnlookerBees() {
		log("Sending onlooker bees.");
		
		int expanded = 0;
		
		for (int i = 0; i < foods.size(); i++) {
			FoodSource current = foods.get(i);
			
			if (current.probability > ACMAUtil.RANDOM.nextDouble()) {
				FoodSource neighbor = current.mutate();
				
				if (neighbor.isBetterThan(current)) {
					foods.set(i, neighbor);
				} else {
					current.trialCount++;
				}
				
				expanded++;
			}
		}
		
		if (getObserver() != null)
			getObserver().onExpansion(this, expanded);
	}
	
	private void memorizeBestSource() {
		log("Memorizing best food source.");
		
		for (int i = 0; i < foods.size(); i++) {
			FoodSource current = foods.get(i);
			
			if (best == null || current.getFitness() > best.getFitness()) {
				best = current;
			}
		}
	}
	
	private void sendScoutBees() {
		log("Sending scout bees.");
		
		for (int i = 0; i < foods.size(); i++) {
			FoodSource current = foods.get(i);
			
			if (current.trialCount > maxTrials) {
				if (best == null)
					foods.set(i, new FoodSource(initialDesign.getRandomNeighbor(randomDepth)));
				else
					foods.set(i, new FoodSource(best.design.getRandomNeighbor(randomDepth)));
				
				if (getObserver() != null)
					getObserver().onExpansion(this, 1);
			}
		}
	}
	
	private class FoodSource {
		private UUID id;
		private SolutionDesign design;
		private int trialCount;
		private double probability;

		private FoodSource(SolutionDesign design) {
			this.id = UUID.randomUUID();
			
			this.design = design;
			this.trialCount = 0;
			this.probability = 0.0;
		}

		public double getFitness() {
			return 1.0 / design.getScore();
		}
		
		public FoodSource mutate() {
			return new FoodSource(design.getRandomNeighbor());
		}
		
		public boolean isBetterThan(FoodSource other) {
			return design.isBetterThan(other.design);
		}
		
		@Override
		public int hashCode() {
			return id.hashCode();
		}
	}
}
