package searches;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import multirefactor.Configuration;
import multirefactor.FitnessFunction;
import multirefactor.Metrics;
import multirefactor.RefactoringSequence;
import recoder.CrossReferenceServiceConfiguration;
import recoder.io.PropertyNames;
import refactorings.Refactoring;

public class MultiObjectiveSearch extends GeneticAlgorithmSearch
{
	private Configuration[] c;
	private ArrayList<Refactoring> refactorings;
	private String[] sourceFiles;
	private String outputPath;
	private FitnessFunction[] ff;
		
	private int generations;
	private int populationSize;
	private float crossoverProbability;
	private float mutationProbability;
	private int initialRefactoringRange = 50;

	public MultiObjectiveSearch(CrossReferenceServiceConfiguration sc, Configuration[] c, ArrayList <Refactoring> refactorings, String[] sourceFiles) 
	{
		super(sc);
		this.c = c;
		this.ff = new FitnessFunction[this.c.length];
		this.refactorings = refactorings;
		this.sourceFiles = sourceFiles;
		this.generations = 100;
		this.populationSize = 50;
		this.crossoverProbability = 0.2f;
		this.mutationProbability = 0.8f;
	}

	public MultiObjectiveSearch(CrossReferenceServiceConfiguration sc, Configuration[] c, ArrayList <Refactoring> refactorings, String[] sourceFiles,
								int generations, int populationSize, float crossoverProbability, float mutationProbability)
	{
		super(sc);
		this.c = c;
		this.ff = new FitnessFunction[this.c.length];
		this.sourceFiles = sourceFiles;
		this.generations = generations;
		this.populationSize = populationSize;
		this.crossoverProbability = crossoverProbability;
		this.mutationProbability = mutationProbability;
	}

	// Executes the Genetic Algorithm.
	public void run() 
	{
		// Store output path from original program model for printing.
		this.outputPath = super.sc.getProjectSettings().getProperty(PropertyNames.OUTPUT_PATH);	
				
		String runInfo = String.format("Search: Multi-Objective Genetic Algorithm\r\nGenerations: %d\r\nPopulation Size: %d" +
									   "\r\nCrossover Probability: %f\r\nMutation Probability: %f",
									   this.generations, this.populationSize, this.crossoverProbability, this.mutationProbability);
		
		for (int i = 1; i <= populationSize; i++)
			super.outputSearchInfo(super.resultsPath, i, runInfo);
	
		long timeTaken, startTime = System.currentTimeMillis();
		double time;
		int unitPosition;

		System.out.printf("\r\n\r\nCreating Initial Population...");
		ArrayList<RefactoringSequence> population = new ArrayList<RefactoringSequence>(this.populationSize);
		ArrayList<RefactoringSequence> newGeneration = new ArrayList<RefactoringSequence>();
		population = fitness(initialise());
		
		// At each generation, crossover is applied to produce a number of child solutions.
		// Then, mutation is applied amongst these new solutions to introduce variety.
		// Fitness is measured for the new solutions and they are sorted accordingly.
		for (int i = 1; i <= this.generations; i++)
		{
			System.out.printf("\r\n\r\nIteration %d:", i);
			newGeneration = new ArrayList<RefactoringSequence>();
			
			// Crossover is always done once for each generation but beyond that the
			// amount of times it is executed depends on the crossover probability.
			do
			{
				System.out.printf("\r\n  Crossover...");
				int randomS1, randomS2;
				RefactoringSequence[] parents = new RefactoringSequence[2];
				
				for (int j = 0; j < 2; j++)
				{
					randomS1 = (int)(Math.random() * population.size());
					
					do
						randomS2 = (int)(Math.random() * population.size());
					while (randomS2 == randomS1);

					parents[j] = binaryTournament(population.get(randomS1), population.get(randomS2));
				}
				
				newGeneration.addAll(crossover(parents[0], parents[1]));
			}
			while (Math.random() < this.crossoverProbability);
			
			// The amount of times if, at all, mutation is applied depends on the
			// mutation probability. This will mutate the children of the current generation.
			while (Math.random() < this.mutationProbability)
			{
				System.out.printf("\r\n  Mutation...");
				int randomChild = (int)(Math.random() * newGeneration.size());
				newGeneration.set(randomChild, super.mutation(newGeneration.get(randomChild), this.refactorings, this.c[0], this.sourceFiles, this.ff));
			}
			
			// The current population is measured and sorted accordingly.
			System.out.printf("\r\n  Fitness...");
			newGeneration.trimToSize();
			population.ensureCapacity(this.populationSize + newGeneration.size());
			population.addAll(newGeneration);
			population = new ArrayList<RefactoringSequence>(fitness(population));
			population.trimToSize();
		}
		
		newGeneration = null;
		timeTaken = System.currentTimeMillis() - startTime;
		time = timeTaken / 1000.0;
		
		System.out.printf("\r\n\r\nPrinting Population");
		
		// Find solutions from final population that are in the top rank.
		ArrayList<RefactoringSequence> topRank = new ArrayList<RefactoringSequence>();
		for (int i = 0; i < population.size(); i++)
			if (population.get(i).getRank() == 1)
				topRank.add(population.get(i));
		
		// Find the ideal solution from the top rank in the population.
		int topSolution = super.findTopSolution(topRank, this.c.length);

		for (int i = 0; i < population.size(); i++)
		{
			System.out.printf("\r\n  Solution %d", i + 1);
			this.refactorings = super.resetModel(this.refactorings, this.c[0], this.sourceFiles);
			
			// Reconstruct model so it can be printed.
			for (int j = 0; j < population.get(i).getRefactorings().size(); j++)
			{
				unitPosition = super.unitPosition(population.get(i).getNames().get(j));
				this.refactorings.get(population.get(i).getRefactorings().get(j))
				                 .transform(this.refactorings.get(population.get(i).getRefactorings().get(j))
						         .analyze((j + 1), unitPosition, population.get(i).getPositions().get(j)));
			}
			
			// Output information.
			super.outputRefactoringInfo(super.resultsPath, time, i + 1, population.get(i).getRefactoringInfo());
			boolean top = (i == topSolution) ? true : false;
			super.outputMetrics(population.get(i).getMOFitness(), false, false, top, i + 1, super.resultsPath);
			
			// Output refactored solution.
			String newOutputPath = this.outputPath;
			newOutputPath += (population.get(i).getRank() == 1) ? "TopRank/Solution" + (i + 1) + "/" : "Solution" + (i + 1) + "/";
			if (i == topSolution)
				newOutputPath = newOutputPath.substring(0, newOutputPath.length() - 1) + "-IdealSolution/";
			super.sc.getProjectSettings().setProperty(PropertyNames.OUTPUT_PATH, newOutputPath);
			super.print(super.sc.getSourceFileRepository());	
		}

		// Output time taken to console and refactoring information to results file.
		population = null;
		timeTaken = System.currentTimeMillis() - startTime;
		time = timeTaken / 1000.0;
		System.out.printf("\r\n\r\nOverall time taken for search: %.2fs", time);
		System.out.printf("\r\n-------------------------------------");
	}
	
	// Creates an initial population of refactoring solutions at random.
	// Using the initial refactoring range a random amount of refactorings will
	// be applied in that range to create a possible solution and this will be 
	// repeated until the population size has been satisfied.
	private ArrayList<RefactoringSequence> initialise()
	{
		ArrayList<RefactoringSequence> population = new ArrayList<RefactoringSequence>(this.populationSize);
		Metrics m = new Metrics(super.sc.getSourceFileRepository().getKnownCompilationUnits());	
		float benchmark[] = new float[this.c.length];
		float finalScore[] = new float[this.c.length];
		
		for (int i = 0; i < this.c.length; i++)
		{
			this.ff[i] = new FitnessFunction(m, this.c[i].getConfiguration());
			super.setPriority(this.ff[i], this.c[i]);
			benchmark[i] = 0.0f;
		}
		
		for (int i = 0; i < this.populationSize; i++)
		{
			// Reinitialise the program model to the original state.
			if (i > 0)
				this.refactorings = super.resetModel(this.refactorings, this.c[0], this.sourceFiles);

			super.outputMetrics(benchmark, true, false, false, i + 1, super.resultsPath);
			
			// Applies random refactorings to each solution to create an initial population.
			// The amount of refactorings applied in each case is chosen randomly within the range supplied.
			population.add(super.createInitialSolution(this.initialRefactoringRange, this.refactorings, i + 1));
			
			// Calculate fitness up front so current model isn't needed at a later point.
			m = new Metrics(super.sc.getSourceFileRepository().getKnownCompilationUnits(), population.get(i).getAffectedClasses(), 
							population.get(i).getElementDiversity());		
			for (int j = 0; j < this.c.length; j++)
				finalScore[j] = this.ff[j].calculateNormalisedScore(m);
			population.get(i).setMOFitness(finalScore.clone());
		}
		
		return population;
	}
		
	// Reinitialises the program model before applying the crossover to generate a child solution.
	// This is done twice in order to generate the offspring expected from the crossover process.
	private ArrayList<RefactoringSequence> crossover(RefactoringSequence p1, RefactoringSequence p2)
	{		
		ArrayList<RefactoringSequence> children = new ArrayList<RefactoringSequence>(2);
		float finalScore[] = new float[this.c.length];
		Metrics m;
		
		// For each refactoring sequence passed in, a cut point is randomly chosen.
		int cutPoint1 = ((int)(Math.random() * (p1.getRefactorings().size() - 1))) + 1;
		int cutPoint2 = ((int)(Math.random() * (p2.getRefactorings().size() - 1))) + 1;
		
		// Initialise the program model to the original state and generate the first child solution.
		this.refactorings = super.resetModel(this.refactorings, this.c[0], this.sourceFiles);
		RefactoringSequence child1 = super.generateChild(p1, p2, cutPoint1, cutPoint2, 1, this.refactorings);
		
		// Calculate fitness up front so current model isn't needed at a later point.
		m = new Metrics(super.sc.getSourceFileRepository().getKnownCompilationUnits(), child1.getAffectedClasses(), child1.getElementDiversity());		
		for (int j = 0; j < this.c.length; j++)
			finalScore[j] = this.ff[j].calculateNormalisedScore(m);
		child1.setMOFitness(finalScore.clone());
		children.add(child1);
		
		// Initialise the program model again and generate the second child solution.
		this.refactorings = super.resetModel(this.refactorings, this.c[0], this.sourceFiles);
		RefactoringSequence child2 = super.generateChild(p2, p1, cutPoint2, cutPoint1, 2, this.refactorings);

		// Calculate fitness up front so current model isn't needed at a later point.
		m = new Metrics(super.sc.getSourceFileRepository().getKnownCompilationUnits(), child2.getAffectedClasses(), child2.getElementDiversity());		
		for (int j = 0; j < this.c.length; j++)
			finalScore[j] = this.ff[j].calculateNormalisedScore(m);
		child2.setMOFitness(finalScore.clone());
		children.add(child2);

		return children;
	}
		
	// Sorts the population by fitness depending on ranks of non dominated 
	// solutions and calculation of crowding distance for last rank added.
	private ArrayList<RefactoringSequence> fitness(ArrayList<RefactoringSequence> population)
	{		
		ArrayList<ArrayList<RefactoringSequence>> dominationFronts = super.fastNonDominatedSort(population, this.populationSize, this.c.length);

		ArrayList<RefactoringSequence> sortedPopulation = new ArrayList<RefactoringSequence>(this.populationSize);
		int i = 0;
		
		while ((dominationFronts.size() > i) && ((sortedPopulation.size() + dominationFronts.get(i).size()) <= this.populationSize)) 
		{
			dominationFronts.set(i, crowdingDistanceAssignment(dominationFronts.get(i)));
			sortedPopulation.addAll(dominationFronts.get(i));
			i++;
		}
		
		if (sortedPopulation.size() != this.populationSize)
		{
			ArrayList<RefactoringSequence> front = sort(crowdingDistanceAssignment(dominationFronts.get(i)), false);
			int remainingSolutions = this.populationSize - sortedPopulation.size();
			
			for (i = 0; i < remainingSolutions; i++) 
				sortedPopulation.add(front.get(i));
		}

		sortedPopulation.trimToSize();
		return sortedPopulation;
	}

	
	// Executes the crowding distance assignment for the specified individuals.
	private ArrayList<RefactoringSequence> crowdingDistanceAssignment(ArrayList<RefactoringSequence> paretoFront) 
	{
		if (paretoFront.size() < 3) 
		{
			for (RefactoringSequence s : paretoFront) 
				s.setCrowdingDistance(Float.POSITIVE_INFINITY);
		}
		else
		{
			// Initialise crowding distance.
			for (RefactoringSequence s : paretoFront) 
				s.setCrowdingDistance(0);

			for (int i = 0; i < this.c.length; i++) 
			{
				for (RefactoringSequence s : paretoFront)
					s.setFitness(s.getMOFitness()[i]);

				// Sort solutions using the current fitness objective.
				paretoFront = sort(paretoFront, true);

				// So that boundary points are always selected.
				paretoFront.get(0).setCrowdingDistance(Float.POSITIVE_INFINITY);
				paretoFront.get(paretoFront.size() - 1).setCrowdingDistance(Float.POSITIVE_INFINITY);

				// If minimal and maximal fitness value for this
				// objective are equal, do not change crowding distance. 
				if (paretoFront.get(0).getFitness() != paretoFront.get(paretoFront.size() - 1).getFitness()) 
				{
					for (int j = 1; j < paretoFront.size() - 1; j++) 
					{
						float newCrowdingDistance = paretoFront.get(j).getCrowdingDistance();

						newCrowdingDistance += (paretoFront.get(j - 1).getFitness() - paretoFront.get(j + 1).getFitness()) /
					               			   (paretoFront.get(0).getFitness() - paretoFront.get(paretoFront.size() - 1).getFitness());

						paretoFront.get(j).setCrowdingDistance(newCrowdingDistance);
					}
				}
			}
		}

		return paretoFront;
	}
	
	// Sorts the population by fitness or crowding distance.
	private ArrayList<RefactoringSequence> sort(ArrayList<RefactoringSequence> population, boolean fitness) 
	{
		RefactoringSequence[] arrayPopulation = population.toArray(new RefactoringSequence[0]);
		population.clear();

		if (fitness)
			Arrays.sort(arrayPopulation, new FitnessComparator());
		else
			Arrays.sort(arrayPopulation, new CrowdingDistanceComparator());
		
		for (RefactoringSequence s : arrayPopulation)
			population.add(s);

		return population;
	}
		
	// Selects one out of two individuals using a binary
	// tournament selection with the crowded comparison operator.
	private RefactoringSequence binaryTournament(RefactoringSequence s1, RefactoringSequence s2) 
	{
		if (s1.getRank() < s2.getRank())
			return s1;				
		else if (s1.getRank() > s2.getRank())
			return s2;
		else
		{
			if (s1.getCrowdingDistance() > s2.getCrowdingDistance()) 
				return s1;
			else if (s2.getCrowdingDistance() > s1.getCrowdingDistance()) 
				return s2;
			else
			{
				// Both solutions are "equal". Select one at random.
				if (Math.random() < 0.5) 
					return s1;
				else 
					return s2;
			}
		}
	}
		
	
	// This inner class allows sorting by crowding distance so higher distances are at the front of the list.
	private class CrowdingDistanceComparator implements Comparator<RefactoringSequence> 
	{
		// Compares the two specified individuals using the crowding distance operator.
		// Returns -1, 0 or 1 as the first argument is greater than, equal to, or less than the second.
		public int compare(RefactoringSequence s1, RefactoringSequence s2) 
		{   
			if (s1.getCrowdingDistance() > s2.getCrowdingDistance())
				return -1;
			else if (s1.getCrowdingDistance() < s2.getCrowdingDistance())
				return 1;
			else
				return 0;
		}
	}
	
	public void setConfigurations(Configuration[] c)
	{
		this.c = c;
	}
	
	public void setRefactorings(ArrayList<Refactoring> refactorings)
	{
		this.refactorings = refactorings;
	}
	
	public void setInitialRefactoringRange(int refactoringRange)
	{
		this.initialRefactoringRange = refactoringRange;
	}
}