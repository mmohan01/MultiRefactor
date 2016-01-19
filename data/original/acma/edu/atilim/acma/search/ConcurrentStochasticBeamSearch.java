package edu.atilim.acma.search;

import java.io.BufferedWriter;
import java.io.Externalizable;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import edu.atilim.acma.RunConfig;
import edu.atilim.acma.concurrent.Instance;
import edu.atilim.acma.concurrent.InstanceSet;
import edu.atilim.acma.design.Design;
import edu.atilim.acma.design.io.WriteClass;
import edu.atilim.acma.transition.actions.Action;
import edu.atilim.acma.ui.ConfigManager;
import edu.atilim.acma.util.ACMAUtil;
import edu.atilim.acma.util.Pair;

public class ConcurrentStochasticBeamSearch extends ConcurrentAlgorithm {

	private int beamLength;
	private int randomDepth;
	private int runCount;
	private int iterations;
	private boolean useBoltzman;
	
	public ConcurrentStochasticBeamSearch() {
	}
	
	public ConcurrentStochasticBeamSearch(String name, RunConfig config, Design initialDesign, int beamLength, int randomDepth, int iterations, int runCount) {
		super(name, config, initialDesign);
		
		this.beamLength = beamLength;
		this.randomDepth = randomDepth;
		this.runCount = runCount;
		this.iterations = iterations;
		this.useBoltzman = false;
	}

	public ConcurrentStochasticBeamSearch(String name, RunConfig config, Design initialDesign, int beamLength, int randomDepth, int iterations, int runCount, boolean useBoltzman) {
		super(name, config, initialDesign);
		
		this.beamLength = beamLength;
		this.randomDepth = randomDepth;
		this.runCount = runCount;
		this.iterations = iterations;
		this.useBoltzman = useBoltzman;
	}
	
	@Override
	public void runMaster(InstanceSet instances) {
		
		long timeCounter = 0;
		double scoreCounter = 0;
		double qualityGainCounter = 0;
		double lowestQualityGain = Double.MAX_VALUE;
		double highestQualityGain = -Double.MAX_VALUE;
		double bestScore = 0;
		double worstScore = 0;
		
		int metricSize = ConfigManager.getMetrics(getConfig()).size();
		String[] metricNames = new String[metricSize];
		double[] initialMetricValues = new double[metricSize];
		double[] metricValuesCounter = new double[metricSize];
		double[] metricQualityGainCounter = new double[metricSize];
		double[] lowestMetricQualityGain = new double[metricSize];
		double[] highestMetricQualityGain = new double[metricSize];
		double[] bestMetricValue = new double[metricSize];
		double[] worstMetricValue = new double[metricSize];
		int counter = 0;
		String runName = "";
		
		for (int runs = 0; runs < runCount; runs++) {
			long startTime = System.currentTimeMillis();
			long expansion = 0;
			
			HashSet<Design> population = new HashSet<Design>();
			
			System.out.printf("Generating %d random designs for initial population.\n", beamLength);
			SolutionDesign initial = new SolutionDesign(getInitialDesign(), getConfig());
			population.add(initial.getDesign());
			for (int i = 1; i < beamLength; i++) {			
				SolutionDesign random = initial.getRandomNeighbor(randomDepth);
				population.add(random.getDesign());
			}
			System.out.println("Generated initial population.");
			
			expansion += population.size();
			
			for (int i = 0 ; i < iterations; i++) {				
				System.out.printf("Starting iteration %d.\n", i + 1);
				instances.broadcast(Boolean.TRUE);
				int expanded = expandPopulationMaster(instances, population);
				
				expansion += expanded;
			}
			
			SolutionDesign best = initial;
			for (Design d : population) {
				SolutionDesign sd = new SolutionDesign(d, getConfig());
				if (sd.isBetterThan(best))
					best = sd;
			}
			
			System.out.printf("Finished %d iterations. Found best design with score: %.6f.\n", iterations, best.getScore());
			
			Design bestDesign = best.getDesign();
			long timeTaken = System.currentTimeMillis() - startTime;
			bestDesign.setTag(new RunInfoTag(timeTaken, 
					String.format("Beam Search. Beam Length: %d. Randomization: %d. Iterations: %d.", beamLength, randomDepth, iterations), expansion));
			runName = onFinish(bestDesign, runCount);
			
			timeCounter += timeTaken;
			scoreCounter += best.getScore();
			double qualityGain = initial.getScore() - best.getScore();
			qualityGainCounter += qualityGain;
			if (qualityGain < lowestQualityGain)
				lowestQualityGain = qualityGain;
			if (qualityGain > highestQualityGain)
				highestQualityGain = qualityGain;
			bestScore = initial.getScore() - highestQualityGain;
			worstScore = initial.getScore() - lowestQualityGain;
			
			counter = 0;
			if (runs == 0)
				for (Entry<String, Double> e : initial.getMetricSummary().getMetrics().entrySet())
				{
					metricNames[counter] = e.getKey();
					initialMetricValues[counter] = e.getValue();
					metricValuesCounter[counter] = 0;
					metricQualityGainCounter[counter] = 0;
					lowestMetricQualityGain[counter] = Double.MAX_VALUE;
					highestMetricQualityGain[counter] = -Double.MAX_VALUE;
					bestMetricValue[counter] = 0;
					worstMetricValue[counter] = 0;
					counter++;
				}

			counter = 0;
			for (Entry<String, Double> e : best.getMetricSummary().getMetrics().entrySet())
			{
				if (getConfig().isMetricEnabled(metricNames[counter]))
				{
					metricValuesCounter[counter] += e.getValue();
					double metricQualityGain;
					if (ConfigManager.getMetrics(getConfig()).get(counter).isMaximized())
						metricQualityGain = e.getValue() - initialMetricValues[counter];
					else
						metricQualityGain = initialMetricValues[counter] - e.getValue();
					
					metricQualityGainCounter[counter] += metricQualityGain;
					if (metricQualityGain < lowestMetricQualityGain[counter])
						lowestMetricQualityGain[counter] = metricQualityGain;
					if (metricQualityGain > highestMetricQualityGain[counter])
						highestMetricQualityGain[counter] = metricQualityGain;

					if (ConfigManager.getMetrics(getConfig()).get(counter).isMaximized())
					{
						bestMetricValue[counter] = initialMetricValues[counter] + highestMetricQualityGain[counter];
						worstMetricValue[counter] = initialMetricValues[counter] + lowestMetricQualityGain[counter];
					}
					else
					{
						bestMetricValue[counter] = initialMetricValues[counter] - highestMetricQualityGain[counter];
						worstMetricValue[counter] = initialMetricValues[counter] - lowestMetricQualityGain[counter];
					}
				}
				counter++;
			}
			
			if (runs == (runCount - 1))
			{
				WriteClass ex = new WriteClass(bestDesign);
				ex.afterBase();
			}
		}
		
		double time = timeCounter/1000.0; 
		
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(runName, true));
			bw.append("====== Task Summary ======\r\n\r\n");
			bw.append("* General Info:\r\n");
			bw.append(String.format("  * Average time across all runs: %.2f seconds\r\n", time/runCount));
			bw.append(String.format("  * Average quality gain across all runs: %.6f (Average Score: %.6f)\r\n", 
									qualityGainCounter/runCount, scoreCounter/runCount));
			bw.append(String.format("  * Highest quality gain across all runs: %.6f (Score: %.6f)\r\n", highestQualityGain, bestScore));
			bw.append(String.format("  * Lowest quality gain across all runs: %.6f (Score: %.6f)\r\n", lowestQualityGain, worstScore));
			bw.append(String.format("  * Time taken to complete task: %.2f seconds\r\n", time));
			bw.append("\r\n* Metric Summary:\r\n");
			
			for (int i = 0; i < metricSize; i++)
			{
				if (getConfig().isMetricEnabled(metricNames[i]))
				{
					bw.append(String.format("  * %s\r\n", metricNames[i]));
					bw.append(String.format("    * Average Quality Gain: %.4f (Average Score: %.4f)\r\n", 
							metricQualityGainCounter[i]/runCount, metricValuesCounter[i]/runCount));
					bw.append(String.format("    * Highest Quality Gain: %.4f (Best Score: %.4f)\r\n", 
							highestMetricQualityGain[i], bestMetricValue[i]));
					bw.append(String.format("    * Lowest Quality Gain: %.4f (Worst Score: %.4f)\r\n", 
							lowestMetricQualityGain[i], worstMetricValue[i]));
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try { bw.close(); } catch (Exception e) { }
		}
		
		instances.broadcast(Boolean.FALSE);
	}
	
	private int expandPopulationMaster(InstanceSet instances, HashSet<Design> population) {
		System.out.println("Scattering population to instances.");
		instances.scatter(new ArrayList<Design>(population));
		System.out.println("Waiting for population expansion.");
		
		ArrayList<FoundDesignHandle> handles = instances.gather(FoundDesignHandle.class);
		System.out.printf("Received %d design handles. Choosing %d within.\n", handles.size(), beamLength);
		
		int expansion = handles.size();
			
		instances.broadcast(selectDesigns(handles));
		
		System.out.println("Waiting for new population");
		ArrayList<Design> newpop = instances.gather(Design.class);
		population.clear();
		for (Design d : newpop) {
			population.add(d);
		}
		
		System.out.printf("New population generated with %d designs.\n", newpop.size());
		
		return expansion;
	}
	
	private ArrayList<FoundDesignHandle> selectDesigns(ArrayList<FoundDesignHandle> designs) {
		Collections.shuffle(designs);
		
		ArrayList<FoundDesignHandle> selected = new ArrayList<ConcurrentStochasticBeamSearch.FoundDesignHandle>();
		
		double average = 0.0;
		double min = Double.MAX_VALUE;
		
		for (int i = 0; i < designs.size(); i++) {
			FoundDesignHandle cur = designs.get(i);
			average += cur.score;
			
			if (cur.score < min) min = cur.score;
		}
		
		average /= designs.size();
		average -= min;
		double z;
		
		if (!useBoltzman)
			z = (beamLength) / (Math.exp(-1) * designs.size());
		else
		{
			z = 0;
			
			for (int i = 0; i < designs.size(); i++) {
				FoundDesignHandle cur = designs.get(i);
				z += Math.exp(-(cur.score - min) / average);
			}
			
			z = beamLength / z;
			//z *= beamLength;
		}
			
		for (int i = 0; i < designs.size(); i++) {
			FoundDesignHandle cur = designs.get(i);

			double probability = z * Math.exp(-(cur.score - min) / average);
			
			//System.out.printf("%.6f\t%.6f\n", cur.score, probability);

			if (probability > ACMAUtil.RANDOM.nextDouble())
				selected.add(cur);
		}
		
		return selected;
	}

	@Override
	public void runWorker(Instance master) {
		while(master.receive(Boolean.class))
			expandPopulationWorker(master);
		
		System.out.println("Finalizing Instance.");
	}
	
	@SuppressWarnings("unchecked")
	private void expandPopulationWorker(Instance master) {
		System.out.println("Waiting for population");
		final ArrayList<Design> designs = (ArrayList<Design>)master.receive();
		System.out.printf("Received %d designs. Expanding neighbors.\n", designs.size());
		
		final HashMap<UUID, FoundDesign> neighbors = new HashMap<UUID, FoundDesign>();
		final List<Callable<Void>> tasks = new ArrayList<Callable<Void>>();
		
		for (Design d : designs) {
			final SolutionDesign design = new SolutionDesign(d, getConfig());
			tasks.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					Iterator<Pair<Action, Double>> iter = design.pairIterator();
					
					while (iter.hasNext()) {
						Pair<Action, Double> neighbor = iter.next();
						FoundDesign found = new FoundDesign(design, neighbor.getFirst(), neighbor.getSecond());
						
						synchronized (neighbors) {
							neighbors.put(found.id, found);
						}
					}
					
					return null;
				}
			});
		}
		
		try {
			List<Future<Void>> futures = ACMAUtil.threadPool.invokeAll(tasks);
			for (Future<Void> f : futures) f.get();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		ArrayList<FoundDesignHandle> handles = new ArrayList<FoundDesignHandle>();
		for (FoundDesign fd : neighbors.values())
			handles.add(fd.getHandle());
		master.send(handles);
		
		ArrayList<FoundDesignHandle> selected = (ArrayList<FoundDesignHandle>)master.receive();
		
		designs.clear();
		for (FoundDesignHandle d : selected) {
			FoundDesign design = neighbors.get(d.id);
			
			if (design != null)
				designs.add(design.parent.apply(design.action).getDesign());
		}
		master.send(designs);
	}
	
	public static class FoundDesignHandle implements Externalizable {
		private UUID id;
		private double score;
		
		public FoundDesignHandle() {
			
		}
		
		private FoundDesignHandle(UUID id, double score) {
			this.id = id;
			this.score = score;
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			id = (UUID)in.readObject();
			score = in.readDouble();
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(id);
			out.writeDouble(score);
		}
		
	}
	
	private static class FoundDesign {
		private UUID id;
		private SolutionDesign parent;
		private Action action;
		private double score;
		
		private FoundDesign(SolutionDesign parent, Action action, double score) {
			this.id = UUID.randomUUID();
			this.parent = parent;
			this.action = action;
			this.score = score;
		}

		public FoundDesignHandle getHandle() {
			return new FoundDesignHandle(id, score);
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		
		out.writeInt(1); //version
		out.writeInt(beamLength);
		out.writeInt(randomDepth);
		out.writeInt(runCount);
		out.writeInt(iterations);
		out.writeBoolean(useBoltzman);
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		
		int version = in.readInt();
		
		beamLength = in.readInt();
		randomDepth = in.readInt();
		runCount = in.readInt();
		iterations = in.readInt();
		
		useBoltzman = false;
		
		if (version >= 1)
			useBoltzman = in.readBoolean();
	}
}
