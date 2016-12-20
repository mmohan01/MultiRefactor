package searches;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import multirefactor.Configuration;
import multirefactor.FitnessFunction;
import multirefactor.Metrics;
import multirefactor.RefactoringSequence;
import recoder.CrossReferenceServiceConfiguration;
import recoder.ParserException;
import recoder.io.PropertyNames;
import refactorings.Refactoring;

public class MultiObjectiveSearch extends Search
{
	private Configuration[] c;
	private ArrayList <Refactoring> refactorings;
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
		population = fitness(initialize());
		
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
				newGeneration.set(randomChild, mutation(newGeneration.get(randomChild)));
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
		int topSolution = findTopSolution(topRank);

		for (int i = 0; i < population.size(); i++)
		{
			System.out.printf("\r\n  Population %d", i + 1);
			resetModel();
			
			// Reconstruct model so it can be printed.
			for (int j = 0; j < population.get(i).getRefactorings().size(); j++)
			{
				unitPosition = super.unitPosition(population.get(i).getNames().get(j)[0]);
				this.refactorings.get(population.get(i).getRefactorings().get(j))
				                 .transform(this.refactorings.get(population.get(i).getRefactorings().get(j))
						         .analyze((j + 1), unitPosition, population.get(i).getPositions().get(j)));
			}
			
			// Output information.
			outputRefactoringInfo(super.resultsPath, time, i + 1, population.get(i).getRefactoringInfo());
			boolean top = (i == topSolution) ? true : false;
			outputMetrics(population.get(i).getMOFitness(), false, false, top, i + 1, super.resultsPath);
			
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
	private ArrayList<RefactoringSequence> initialize()
	{
		ArrayList<RefactoringSequence> population = new ArrayList<RefactoringSequence>(this.populationSize);
		Metrics m = new Metrics(super.sc.getSourceFileRepository().getKnownCompilationUnits());	
		float benchmark[] = new float[this.c.length];
		float finalScore[] = new float[this.c.length];
		
		for (int i = 0; i < this.c.length; i++)
		{
			this.ff[i] = new FitnessFunction(m, this.c[i].getConfiguration());
			
			// If priority objective is being used.
			if (this.c[i].getPriorityClasses() != null)
				this.ff[i].setPriorityClasses(this.c[i].getPriorityClasses());
			
			// If priority objective is being used and there are also non priority classes.
			if (this.c[i].getNonPriorityClasses() != null)
				this.ff[i].setNonPriorityClasses(this.c[i].getNonPriorityClasses());
			
			benchmark[i] = 0.0f;
		}
		
		for (int i = 0; i < this.populationSize; i++)
		{
			// Reinitialise the initial program model.
			if (i > 0)
				resetModel();

			outputMetrics(benchmark, true, false, false, i + 1, super.resultsPath);
			System.out.printf("\r\n  Population %d", i + 1);

			// Applies random refactorings to each solution to create an initial population.
			// The amount of refactorings applied in each case is chosen randomly within the range supplied.
			int refactoringAmount = ((int)(Math.random() * this.initialRefactoringRange)) + 1;
			ArrayList<Integer> posSequence = new ArrayList<Integer>(refactoringAmount);
			ArrayList<Integer> refSequence = new ArrayList<Integer>(refactoringAmount);
			ArrayList<String[]> nameSequence = new ArrayList<String[]>(refactoringAmount);
			ArrayList<String> refactoringInfo = new ArrayList<String>(refactoringAmount);
			ArrayList<String> affectedClasses = new ArrayList<String>(refactoringAmount);
			
			for (int j = 0; j < refactoringAmount; j++)
			{
				int[] result = randomRefactoring();
				
				if (result[0] == -1)
				{
					System.out.printf("\r\n    There are no refactorings available for the rest of the sequence.");
					j = refactoringAmount;
				}
				else
				{
					nameSequence.add(new String[]{super.sc.getSourceFileRepository().getKnownCompilationUnits().get(result[1]).getName(),
                                                  this.refactorings.get(result[0]).getName(result[1], result[2])});
					this.refactorings.get(result[0]).transform(this.refactorings.get(result[0])
					                 .analyze((j + 1), result[1], result[2]));
					refactoringInfo.add(this.refactorings.get(result[0]).getRefactoringInfo());
					affectedClasses.addAll(this.refactorings.get(result[0]).getAffectedClasses());
					refSequence.add(result[0]);
					posSequence.add(result[2]);
				}
			}
			
			refSequence.trimToSize();
			posSequence.trimToSize();
			nameSequence.trimToSize();
			refactoringInfo.trimToSize();
			affectedClasses.trimToSize();
			population.add(new RefactoringSequence(refSequence, posSequence, nameSequence, refactoringInfo, affectedClasses));
			
			// Calculate fitness up front so program model isn't needed at a later point.
			m = new Metrics(super.sc.getSourceFileRepository().getKnownCompilationUnits(), affectedClasses);		
			for (int j = 0; j < this.c.length; j++)
				finalScore[j] = this.ff[j].calculateNormalizedScore(m);
			population.get(i).setMOFitness(finalScore.clone());
		}
		
		return population;
	}
	
	// Method uses single-point crossover. For each refactoring sequence passed in,
	// a cut point is randomly chosen. The segments of each sequence are then switched to
	// create two child solutions. After this the refactorings are applied for each child
	// and any inapplicable refactorings are removed form the new sequences.
	private ArrayList<RefactoringSequence> crossover(RefactoringSequence p1, RefactoringSequence p2)
	{
		ArrayList<RefactoringSequence> children = new ArrayList<RefactoringSequence>(2);
		int cutPoint1 = ((int)(Math.random() * (p1.getRefactorings().size() - 1))) + 1;
		int cutPoint2 = ((int)(Math.random() * (p2.getRefactorings().size() - 1))) + 1;
		int unitPosition, elementPosition, i2;
		Metrics m;
		float finalScore[] = new float[this.c.length];
		
		int c1Size = cutPoint1 + (p2.getRefactorings().size() - cutPoint2);
		ArrayList<Integer> c1Refactorings = new ArrayList<Integer>(c1Size);
		ArrayList<Integer> c1Positions = new ArrayList<Integer>(c1Size);
		ArrayList<String[]> c1Names = new ArrayList<String[]>(c1Size);
		ArrayList<String> refactoringInfo1 = new ArrayList<String>(c1Size);
		ArrayList<String> affectedClasses1 = new ArrayList<String>(c1Size);
		
		// Reinitialise the initial program model.
		resetModel();
		
		for (int i = 0; i < c1Size; i++)
		{				
			// The first sequence in each solution will be applicable so 
			// refactorings can be applied without checking.
			if (i < cutPoint1)
			{	
				unitPosition = super.unitPosition(p1.getNames().get(i)[0]);
				this.refactorings.get(p1.getRefactorings().get(i)).transform(this.refactorings.get(p1.getRefactorings().get(i))
						         .analyze((i + 1), unitPosition, p1.getPositions().get(i)));
				refactoringInfo1.add(p1.getRefactoringInfo().get(i));
				affectedClasses1.add(p1.getAffectedClasses().get(i));
				c1Refactorings.add(p1.getRefactorings().get(i));
				c1Positions.add(p1.getPositions().get(i));	
				c1Names.add(p1.getNames().get(i));
			}
			// For the second sequence, a check will have 
			// to be made for each contiguous refactoring.
			else
			{
				elementPosition = -1;
				i2 = cutPoint2 + (i - cutPoint1);
				unitPosition = super.unitPosition(p2.getNames().get(i2)[0]);
				
				// Checks for the relevant program element by comparing the names of 
				// each applicable element in the class with the desired element name.
				if (unitPosition != -1)
				{
					for (int j = 1; j <= this.refactorings.get(p2.getRefactorings().get(i2)).getAmount(unitPosition); j++)
					{
						if (this.refactorings.get(p2.getRefactorings().get(i2)).getName(unitPosition, j).equals(p2.getNames().get(i2)[1]))
						{						
							elementPosition = j;
							break;
						}
					}
				}
				
				// If the element exists and can be refactored.
				if (elementPosition != -1)
				{
					this.refactorings.get(p2.getRefactorings().get(i2)).transform(this.refactorings.get(p2.getRefactorings().get(i2))
							         .analyze((i + 1), unitPosition, elementPosition));
					refactoringInfo1.add(p2.getRefactoringInfo().get(i2));
					affectedClasses1.add(p2.getAffectedClasses().get(i2));
					c1Refactorings.add(p2.getRefactorings().get(i2));
					c1Positions.add(elementPosition);
					c1Names.add(p2.getNames().get(i2));
				}
				else
					System.out.printf("\r\n    Refactoring %d N/A at child 1", i + 1);
			}
		}

		c1Refactorings.trimToSize();
		c1Positions.trimToSize();
		c1Names.trimToSize();
		refactoringInfo1.trimToSize();
		affectedClasses1.trimToSize();
		children.add(new RefactoringSequence(c1Refactorings, c1Positions, c1Names, refactoringInfo1, affectedClasses1));
		
		// Calculate fitness up front so program model isn't needed at a later point.
		m = new Metrics(super.sc.getSourceFileRepository().getKnownCompilationUnits(), affectedClasses1);		
		for (int j = 0; j < this.c.length; j++)
			finalScore[j] = this.ff[j].calculateNormalizedScore(m);
		children.get(0).setMOFitness(finalScore);

		// Reinitialise the initial program model again for second child.
		resetModel();
		
		int c2Size = cutPoint2 + (p1.getRefactorings().size() - cutPoint1);
		ArrayList<Integer> c2Refactorings = new ArrayList<Integer>(c2Size);
		ArrayList<Integer> c2Positions = new ArrayList<Integer>(c2Size);
		ArrayList<String[]> c2Names = new ArrayList<String[]>(c2Size);
		ArrayList<String> refactoringInfo2 = new ArrayList<String>(c2Size);
		ArrayList<String> affectedClasses2 = new ArrayList<String>(c2Size);
					
		for (int i = 0; i < c2Size; i++)
		{			
			// The first sequence in each solution will be applicable so 
			// refactorings can be applied without checking.
			if (i < cutPoint2)
			{
				unitPosition = super.unitPosition(p2.getNames().get(i)[0]);
				this.refactorings.get(p2.getRefactorings().get(i)).transform(this.refactorings.get(p2.getRefactorings().get(i))
						         .analyze((i + 1), unitPosition, p2.getPositions().get(i)));
				refactoringInfo2.add(p2.getRefactoringInfo().get(i));
				affectedClasses2.add(p2.getAffectedClasses().get(i));
				c2Refactorings.add(p2.getRefactorings().get(i));
				c2Positions.add(p2.getPositions().get(i));
				c2Names.add(p2.getNames().get(i));				
			}	
			// For the second sequence, a check will have 
			// to be made for each contiguous refactoring.
			else
			{
				elementPosition = -1;
				i2 = cutPoint1 + (i - cutPoint2);
				unitPosition = super.unitPosition(p1.getNames().get(i2)[0]);
				
				// Checks for the relevant program element by comparing the names of 
				// each applicable element in the class with the desired element name.
				if (unitPosition != -1)
				{
					for (int j = 1; j <= this.refactorings.get(p1.getRefactorings().get(i2)).getAmount(unitPosition); j++)
					{					
						if (this.refactorings.get(p1.getRefactorings().get(i2)).getName(unitPosition, j).equals(p1.getNames().get(i2)[1]))
						{
							elementPosition = j;
							break;
						}
					}
				}
				
				// If the element exists and can be refactored.
				if (elementPosition != -1)
				{
					this.refactorings.get(p1.getRefactorings().get(i2)).transform(this.refactorings.get(p1.getRefactorings().get(i2))
							         .analyze((i + 1), unitPosition, elementPosition));
					refactoringInfo2.add(p1.getRefactoringInfo().get(i2));
					affectedClasses2.add(p1.getAffectedClasses().get(i2));
					c2Refactorings.add(p1.getRefactorings().get(i2));
					c2Names.add(p1.getNames().get(i2));
					c2Positions.add(elementPosition);
				}
				else
					System.out.printf("\r\n    Refactoring %d N/A at child 2", i + 1);
			}
		}
		
		c2Refactorings.trimToSize();
		c2Positions.trimToSize();
		c2Names.trimToSize();
		refactoringInfo2.trimToSize();
		affectedClasses2.trimToSize();
		children.add(new RefactoringSequence(c2Refactorings, c2Positions, c2Names, refactoringInfo2, affectedClasses2));
		
		// Calculate fitness up front so program model isn't needed at a later point.
		m = new Metrics(super.sc.getSourceFileRepository().getKnownCompilationUnits(), affectedClasses2);		
		for (int j = 0; j < this.c.length; j++)
			finalScore[j] = this.ff[j].calculateNormalizedScore(m);
		children.get(1).setMOFitness(finalScore);

		return children;
	}
	
	// Applies a random refactoring to the end of the refactoring sequence passed in.
	// If the refactoring is not applicable it will keep trying until an applicable refactoring
	// is found or it runs out of possibilities. In this case the original sequence is returned.
	private RefactoringSequence mutation(RefactoringSequence p)
	{			
		// Reinitialise the program model.
		resetModel();
				
		for (int i = 0; i < p.getRefactorings().size(); i++)
		{
			int unitPosition = super.unitPosition(p.getNames().get(i)[0]);
			this.refactorings.get(p.getRefactorings().get(i)).transform(this.refactorings.get(p.getRefactorings().get(i))
					         .analyze((i + 1), unitPosition, p.getPositions().get(i)));
		}
		
		int[] result = randomRefactoring();
		
		// Applies refactoring to model and adds it to the sequence.
		if (result[0] != -1)
		{
			ArrayList<Integer> refSequence = new ArrayList<Integer>(p.getRefactorings().size() + 1);
			refSequence = p.getRefactorings();
			ArrayList<Integer> posSequence = new ArrayList<Integer>(p.getPositions().size() + 1);
			posSequence = p.getPositions();
			ArrayList<String[]> nameSequence = new ArrayList<String[]>(p.getNames().size() + 1);
			nameSequence = p.getNames();
			ArrayList<String> refactoringInfo = new ArrayList<String>(p.getRefactoringInfo().size() + 1);
			refactoringInfo = p.getRefactoringInfo();
			ArrayList<String> affectedClasses = new ArrayList<String>(p.getAffectedClasses().size() + 1);
			affectedClasses = p.getAffectedClasses();
			
			refSequence.add(result[0]);
			posSequence.add(result[2]);
			nameSequence.add(new String[]{super.sc.getSourceFileRepository().getKnownCompilationUnits().get(result[1]).getName(),
                                          this.refactorings.get(result[0]).getName(result[1], result[2])});
			this.refactorings.get(result[0]).transform(this.refactorings.get(result[0])
					         .analyze((refSequence.size()), result[1], result[2]));
			refactoringInfo.add(this.refactorings.get(result[0]).getRefactoringInfo());
			affectedClasses.addAll(this.refactorings.get(result[0]).getAffectedClasses());
			p.setRefactorings(refSequence);
			p.setPositions(posSequence);
			p.setNames(nameSequence);
			p.setRefactoringInfo(refactoringInfo);
			p.setAffectedClasses(affectedClasses);
		
			// Calculate fitness up front so program model isn't needed at a later point.
			Metrics m = new Metrics(super.sc.getSourceFileRepository().getKnownCompilationUnits(), affectedClasses);	
			float finalScore[] = new float[this.c.length];

			for (int j = 0; j < this.c.length; j++)
				finalScore[j] = this.ff[j].calculateNormalizedScore(m);
			p.setMOFitness(finalScore);
		}
		else
			System.out.printf("\r\n    Mutation N/A");
		
		return p;
	}
	
	// Sorts the population by fitness depending on ranks of non dominated 
	// solutions and calculation of crowding distance for last rank added.
	private ArrayList<RefactoringSequence> fitness(ArrayList<RefactoringSequence> population)
	{		
		ArrayList<ArrayList<RefactoringSequence>> dominationFronts = fastNonDominatedSort(population);

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

	
	// Method finds an "ideal" solution from the top rank by finding the best objective values across
	// all the solutions in the top rank and then finding a distance vector for each solution that indicates
	// how far away the objectives are from the ideal values. The worst distances for each solution
	// are compared and the solution with the lowest worst distance is considered the ideal solution.
	private int findTopSolution(ArrayList<RefactoringSequence> topRank)
	{
		float[] idealPoint = new float[this.c.length];
		float[] distanceVector = new float[this.c.length];
		float[] maxDistances = new float[topRank.size()];
		float bestDistance;
		int topSolution = 0;

		for (int i = 0; i < this.c.length; i++)
			idealPoint[i] = topRank.get(0).getMOFitness()[i];
					
		for (RefactoringSequence s : topRank)
			for (int i = 0; i < this.c.length; i++) 
				if (s.getMOFitness()[i] > idealPoint[i])
					idealPoint[i] = s.getMOFitness()[i];
		
		for (int i = 0; i < topRank.size(); i++) 
		{
			for (int j = 0; j < this.c.length; j++) 
			{
				distanceVector[j] = idealPoint[j] - topRank.get(i).getMOFitness()[j];
				
				if (j == 0)
					maxDistances[i] = distanceVector[j];
				else if (distanceVector[j] > maxDistances[i])
					maxDistances[i] = distanceVector[j];
			}
		}
		
		bestDistance = maxDistances[0];
		
		for (int i = 1; i < topRank.size(); i++) 
		{			
			if (maxDistances[i] < bestDistance)
			{
				bestDistance = maxDistances[i];
				topSolution = i;
			}
		}
			
		return topSolution;
	}
	
	// Reset the program model to the initial input so it can be reused.
	private void resetModel()
	{
		// Save the input path and then overwrite the program model using the constructor.
		// Recreate relevant refactoring objects to ensure old program model is no longer referenced.
		String inputPath = super.sc.getProjectSettings().getProperty(PropertyNames.INPUT_PATH);
		super.sc = new CrossReferenceServiceConfiguration();
		this.refactorings = this.c[0].resetRefactorings(super.sc, this.refactorings, false);

		try 
		{
			// Read the original input.			
			super.sc.getSourceFileRepository().getCompilationUnitsFromFiles(this.sourceFiles);
		}
		catch (ParserException e) 
		{
			System.out.println("\r\nEXCEPTION: Cannot read input.");
			System.exit(1);
		}

		// Set up initial properties of service configuration.
		super.sc.getProjectSettings().setProperty(PropertyNames.INPUT_PATH, inputPath);
		super.sc.getProjectSettings().ensureSystemClassesAreInPath();
	}

	// Output refactoring information to results file for a solution.
	private void outputRefactoringInfo(String pathName, double time, int solution, ArrayList<String> refactoringInfo)
	{
		// Create a location for the results output.
		String runName = String.format("%sresultsSolution%d.txt", pathName, solution);
		File dir = new File(pathName);
		if (!dir.exists()) 
			dir.mkdirs();

		try 
		{
			BufferedWriter bw = new BufferedWriter(new FileWriter(runName, true));
			bw.append("\r\n\r\n======== Applied Refactorings ========");

			for (int i = 0; i < refactoringInfo.size(); i++) 
			{
				String info = "Iteration " + (i + 1) + refactoringInfo.get(i).substring(refactoringInfo.get(i).indexOf(':'));
				bw.append(String.format("\r\n%s", info));
			}

			bw.append(String.format("\r\n\r\nTime taken to refactor: %.2fs", time));
			bw.close();
		}
		catch (IOException e) 
		{
			System.out.println("\r\nEXCEPTION: Cannot export results to text file.");
			System.exit(1);
		}
	}

	// Outputs the metric values for a solution.
	private void outputMetrics(float[] scores, boolean initial, boolean log, boolean toprank, int solution, String pathName)
	{
		// Create a location for the results output.
		String runName = String.format("%sresultsSolution%d.txt", pathName, solution);
		File dir = new File(pathName);
		if (!dir.exists()) 
			dir.mkdirs();

		try 
		{
			BufferedWriter bw = new BufferedWriter(new FileWriter(runName, true));

			if (initial)
				bw.append(String.format("\r\n\r\n======== Initial Metric Info ========"));
			else
				bw.append(String.format("\r\n\r\n======== Final Metric Info ========"));

			// Outputs the fitness function values for the project to a text file.
			for (int i = 0; i < scores.length; i++)
				bw.append(String.format("\r\nFitness function %d score: %f", (i + 1), scores[i]));
			
			if (!(initial) && (toprank))
				bw.append(String.format("\r\n\r\nThis solution has the closest maximum distance from the ideal point in the top rank of solutions"));

			bw.close();
		}
		catch (IOException e) 
		{
			System.out.println("\r\nEXCEPTION: Cannot export results to text file.");
			System.exit(1);
		}

		if (log)
		{
			// Outputs the fitness function values for the project to the console for immediate feedback.
			for (int i = 0; i < scores.length; i++)
				System.out.printf("\r\n\r\nFitness function %d score: %.2f", (i + 1), scores[i]);
		}
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

	// Finds a random available refactoring in the specified model and passes back the
	// refactoring used and the position of the applicable program element in the model.
	private int[] randomRefactoring()
	{
		int[] position = new int[2];
		int r = -1;

		// Find element to refactor.
		if (this.refactorings.size() > 0)
		{
			r = (int)(Math.random() * this.refactorings.size());
			position = super.randomElement(this.refactorings.get(r));
		}
		else
		{
			position[0] = -1;
			position[1] = -1;
		}

		// Checks in case no elements are returned for the refactoring.
		// Will check again for each available refactoring in the search and 
		// if there are still no applicable elements returned the search will terminate.
		if ((position[0] == -1) && (position[1] == -1))
		{
			int exclude = r;
			for (r = 0; r < this.refactorings.size(); r++)
			{
				// Stops the loop from repeating the check for the previous refactoring.
				if ((r == exclude) && ((r + 1) < this.refactorings.size()))
					r++;
				else if (r == exclude)
					break;
				
				position = super.randomElement(this.refactorings.get(r));
			
				if ((position[0] != -1) && (position[1] != -1))
					break;
			}

			if ((position[0] == -1) && (position[1] == -1))
				r = -1;
		}
		
		return new int[]{r, position[0], position[1]};
	}
	
	// Makes a fast non-domination sort of the specified individuals. The method returns the different
	// domination fronts in ascending order by their rank and sets their rank value.
	private ArrayList<ArrayList<RefactoringSequence>> fastNonDominatedSort(ArrayList<RefactoringSequence> population) 
	{
		ArrayList<ArrayList<RefactoringSequence>> dominationFronts = new ArrayList<ArrayList<RefactoringSequence>>();
		HashMap<RefactoringSequence, ArrayList<RefactoringSequence>> dominatedSolutions = new HashMap<RefactoringSequence, ArrayList<RefactoringSequence>>();
		HashMap<RefactoringSequence, Integer> numberOfDominatingSolutions = new HashMap<RefactoringSequence, Integer>();
		int i = 1;
		int amount = 0;
		
		for (RefactoringSequence s1 : population) 
		{
			dominatedSolutions.put(s1, new ArrayList<RefactoringSequence>());
			numberOfDominatingSolutions.put(s1, 0);

			for (RefactoringSequence s2 : population) 
			{
				if (isDominant(s1, s2)) 
					dominatedSolutions.get(s1).add(s2);
				else if (isDominant(s2, s1)) 
					numberOfDominatingSolutions.put(s1, numberOfDominatingSolutions.get(s1) + 1);
			}

			if (numberOfDominatingSolutions.get(s1) == 0) 
			{
				s1.setRank(1);
				
				if (dominationFronts.isEmpty()) 
				{
					ArrayList<RefactoringSequence> firstDominationFront = new ArrayList<RefactoringSequence>();
					firstDominationFront.add(s1);
					dominationFronts.add(firstDominationFront);
				} 
				else 
					dominationFronts.get(0).add(s1);
			}
		}
		
		if (!dominationFronts.isEmpty()) 
			amount += dominationFronts.get(0).size();
		
		// Improved the efficiency of the method here by adding fronts according to need as outlined in
		// "Reducing The Run-Time Complexity Of NSGA-II For Bi-Objective Optimization Problem".
		while ((dominationFronts.size() == i) && (amount < this.populationSize))
		{
			ArrayList<RefactoringSequence> nextDominationFront = new ArrayList<RefactoringSequence>();
			
			for (RefactoringSequence s1 : dominationFronts.get(i - 1)) 
			{
				for (RefactoringSequence s2 : dominatedSolutions.get(s1)) 
				{
					numberOfDominatingSolutions.put(s2, numberOfDominatingSolutions.get(s2) - 1);
					
					if (numberOfDominatingSolutions.get(s2) == 0) 
					{
						s2.setRank(i + 1);
						nextDominationFront.add(s2);
					}
				}
			}
			
			i++;
			
			if (!nextDominationFront.isEmpty()) 
			{
				nextDominationFront.trimToSize();
				dominationFronts.add(nextDominationFront);
				amount += dominationFronts.get(i - 1).size();
			}
		}
		
		dominationFronts.trimToSize();
		return dominationFronts;
	}

	// Returns a boolean to represent whether the first solution parameter dominates the second.
	// The solution is dominant if no individual fitness function values are worse and at least one is better.
	private boolean isDominant(RefactoringSequence s1, RefactoringSequence s2)
	{
		boolean better = false;
		float value1, value2;

		for (int i = 0; i < this.c.length; i++) 
		{
			value1 = s1.getMOFitness()[i];
			value2 = s2.getMOFitness()[i];

			if (value1 < value2) 
				return false;
			else if (value1 > value2) 
				better = true;
		}

		return better;
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
			// Initialize crowding distance.
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