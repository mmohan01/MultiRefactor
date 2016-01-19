package edu.atilim.acma.search;

import edu.atilim.acma.util.ACMAUtil;

public class SimAnnAlgorithm extends AbstractAlgorithm {
	
	private SolutionDesign current;
	private SolutionDesign best;
	
	private int maxIters;
	private EnergySet energySet;
	private Cooler cooler;
	private int badSteps;

	public SimAnnAlgorithm(SolutionDesign initialDesign, AlgorithmObserver observer, double startTemp, int maxIters) {
		super(initialDesign, observer);
		
		current = best = initialDesign;
		this.maxIters = maxIters;
		
		energySet = new EnergySet(10);
		cooler = new DefaultCooler(startTemp, maxIters);
		badSteps = 0;
	}

	@Override
	public String getName() {
		return "Simulated Annealing";
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
		double t = cooler.getTemperature(getStepCount());
		AlgorithmObserver observer = getObserver();
		
		log("Starting iteration %d. Current score: %.6f, Best score: %.6f, Temperature: %.4f", getStepCount(), current.getScore(), best.getScore(), t);
		
		if (t <= 0) {
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
			
			badSteps = 0;
		} else {
			badSteps++;
			
			if (badSteps > maxIters / 10.0) {
				current = best;
				return false;
			}
		}
		
		if (checkProbability(current, randomNeighbor, t)) {
			current = randomNeighbor;
			
			if (observer != null) {
				observer.onUpdateItems(this, current, best, AlgorithmObserver.UPDATE_CURRENT);
			}
		} else {
			current = current.getBestNeighbor();
			
			if (observer != null) {
				observer.onUpdateItems(this, current, best, AlgorithmObserver.UPDATE_CURRENT);
			}
		}
		
		if (observer != null) {
			observer.onAdvance(this, getStepCount() + 1, maxIters);
		}
		
		return false;
	}
	
	private boolean checkProbability(SolutionDesign cur, SolutionDesign candidate, double t) {
		double deltaE = candidate.compareScoreTo(cur);
		energySet.push(Math.abs(deltaE));
		
		if (deltaE > 0.0)
			return true;
		
		return Math.exp(((deltaE / energySet.getAverage()) / t)) > ACMAUtil.RANDOM.nextDouble();
	}

	public static interface Cooler {
		double getTemperature(int iteration);
	}
	
	private static class DefaultCooler implements Cooler {
		private double start;
		private int maxIters;
		
		public DefaultCooler(double start, int maxIters) {
			this.start = start;
			this.maxIters = maxIters;
		}

		@Override
		public double getTemperature(int iteration) {
			if (iteration >= maxIters)
				return -1.0;
			
			//return start * Math.sqrt((maxIters - iteration) / (double)maxIters);
			
			double step = (maxIters - iteration) / (double)maxIters;
			return start * step * step;
		}
	}
	
	private static class EnergySet {
		private double[] values;
		private int head;
		private int tail;
		private int count;
		private double average;
		
		public double getAverage() {
			return average;
		}
		
		public EnergySet(int capacity) {
			values = new double[capacity];
			head = tail = count = 0;
			average = 0.0;
		}
		
		public void push(double val) {
			if (count == values.length) {
				count--;
				double first = values[head];
				average += (average - first) / count;
				head = (head + 1) % values.length;
			}
			
			count++;
			values[tail] = val;
			average += (val  - average) / count;
			tail = (tail + 1) % values.length;
		}
	}
}
