package searches;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import multirefactor.Configuration;
import multirefactor.FitnessFunction;
import multirefactor.Metrics;
import multirefactor.RefactoringSequence;
import recoder.CrossReferenceServiceConfiguration;
import recoder.ParserException;
import recoder.io.PropertyNames;
import refactorings.Refactoring;

public class ManyObjectiveSearch extends Search
{
	private Configuration[] c;
	private ArrayList <Refactoring> refactorings;
	private String[] sourceFiles;
	private String outputPath;
	private FitnessFunction[] ff;
	private ArrayList <float[]> referencePoints;
	private int referencePointAmount;
	private int divisions = 1;
		
	private int generations;
	private int populationSize;
	private float crossoverProbability;
	private float mutationProbability;
	private int initialRefactoringRange = 50;

	public ManyObjectiveSearch(CrossReferenceServiceConfiguration sc, Configuration[] c, ArrayList <Refactoring> refactorings, String[] sourceFiles) 
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

	public ManyObjectiveSearch(CrossReferenceServiceConfiguration sc, Configuration[] c, ArrayList <Refactoring> refactorings, String[] sourceFiles,
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
					
		String runInfo = String.format("Search: Many-Objective Genetic Algorithm\r\nGenerations: %d\r\nPopulation Size: %d" +
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
		
		// Work out the amount of reference points in a hyperplane that best corresponds to the population size.	
		referencePointAmount = this.c.length;
		while (this.referencePointAmount < this.populationSize)
		{
			this.divisions++;
			this.referencePointAmount = numberOfReferencePoints(this.divisions);
		}
		
		this.referencePoints = generateReferencePoints();
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
				randomS1 = (int)(Math.random() * population.size());

				do 
					randomS2 = (int)(Math.random() * population.size());
				while (randomS2 == randomS1);

				parents[0] = population.get(randomS1);		
				parents[1] = population.get(randomS2);
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
					System.out.printf("\r\nThere are no refactorings available for the rest of the sequence.");
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
		int cutPoint1 = (Math.random() < 0.5) ? 1 : p1.getRefactorings().size() - 1;
		int cutPoint2 = (cutPoint1 == 1) ? 1 : p2.getRefactorings().size() - 1;
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
					this.refactorings.get(p1.getRefactorings().get(i2))
					                 .transform(this.refactorings.get(p1.getRefactorings().get(i2))
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
			sortedPopulation.addAll(dominationFronts.get(i));
			i++;
		}
		
		if (sortedPopulation.size() != this.populationSize)
		{
			int remainingSolutions = this.populationSize - sortedPopulation.size();
			sortedPopulation.addAll(referencePointAssignment(sortedPopulation, dominationFronts.get(i), remainingSolutions));
		}

		sortedPopulation.trimToSize();
		return sortedPopulation;
	}


	// Return the amount of reference points that are generated with the relevant amount 
	// of objectives and the provided amount of reference points per axis of the hyperplane.
	// This is represented by n!/p!(n-p)! where p is "axisDivisions" and n is "axisDivisions" plus
	// the amount of objectives - 1 (the amount of dimensions of the simplex representing the hyperplane). 
	private int numberOfReferencePoints(int axisDivisions)
	{
		int numerator = this.c.length + axisDivisions - 1;
		int topValue = numerator;
		int bottomValue = numerator - axisDivisions;
		
		for (int i = numerator - 1; i > axisDivisions; i--)
			topValue *= i;
		
		for (int i = numerator - axisDivisions - 1; i > 1; i--)
			bottomValue *= i;
		
		return topValue / bottomValue;
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

	// Applies the reference point calculations for the current population and chooses the remaining solutions.
	private ArrayList<RefactoringSequence> referencePointAssignment(ArrayList<RefactoringSequence> sortedPopulation, 
																	ArrayList<RefactoringSequence> lastRank, int remainingSolutions) 
	{
		ArrayList<RefactoringSequence> remainingPopulation = new ArrayList<RefactoringSequence>(remainingSolutions);
		float[][] hyperplane = constructHyperplane(sortedPopulation, lastRank);
		
		// For each reference point, the list of solutions that are associated with it (i.e. closer to it than any of the
		// other reference points) are stored for both the chosen solutions and the solutions from the last relevant front.
		ArrayList<ArrayList<RefactoringSequence>> mainAssociatedSolutions = new ArrayList<ArrayList<RefactoringSequence>>(this.referencePointAmount);
		ArrayList<ArrayList<RefactoringSequence>> remainingAssociatedSolutions = new ArrayList<ArrayList<RefactoringSequence>>(this.referencePointAmount);
		int index, minimumNicheCount;
		float distance, smallestDistance;		
		
		// Initialise lists of solutions so that they can be added to when calculating the associated solutions. 
		for (int i = 0; i < this.referencePointAmount; i++) 
		{
			mainAssociatedSolutions.add(new ArrayList<RefactoringSequence>());
			remainingAssociatedSolutions.add(new ArrayList<RefactoringSequence>());
		}
		
		// For each solution already chosen, find the reference point that it is closest to. When the relevant
		// reference point is found, add that solution to the reference points list of associated solutions.
		for (int i = 0; i < sortedPopulation.size(); i++)
		{
			smallestDistance = calculateDistance(sortedPopulation.get(i), this.referencePoints.get(0), hyperplane);
			index = 0;
			
			for (int j = 1; j < this.referencePointAmount; j++)
			{
				distance = calculateDistance(sortedPopulation.get(i), this.referencePoints.get(j), hyperplane);
				
				if (distance < smallestDistance)
				{
					smallestDistance = distance;
					index = j;
				}
			}
			
			mainAssociatedSolutions.get(index).add(sortedPopulation.get(i));
		}
		
		// Do the same for the remaining rank of solutions 
		// from which the last set of solutions will be chosen.
		for (int i = 0; i < lastRank.size(); i++)
		{
			smallestDistance = calculateDistance(lastRank.get(i), this.referencePoints.get(0), hyperplane);
			index = 0;
			
			for (int j = 1; j < this.referencePointAmount; j++)
			{
				distance = calculateDistance(lastRank.get(i), this.referencePoints.get(j), hyperplane);
				
				if (distance < smallestDistance)
				{
					smallestDistance = distance;
					index = j;
				}
			}
			
			remainingAssociatedSolutions.get(index).add(lastRank.get(i));
		}
		
		// Choose the solutions from the last rank to add to the population.
		for (int i = 0; i < remainingSolutions; i++)
		{			
			// Remove any reference points that contain no solutions to choose 
			// from. Needs to be done each time a solution is chosen in case  
			// that reference point had no other solutions to choose from.
			for (int j = 0; j < remainingAssociatedSolutions.size(); j++)
			{
				if (remainingAssociatedSolutions.get(j).size() == 0)
				{
					mainAssociatedSolutions.remove(j);
					remainingAssociatedSolutions.remove(j);
					j--;
				}
			}
		
			minimumNicheCount = mainAssociatedSolutions.get(0).size();
			
			// Find the minimum niche count among the reference points for
			// the associated solutions from the chosen population. This needs
			// to be recalculated each time after another solution is chosen.			
			for (int j = 1; j < mainAssociatedSolutions.size(); j++)
				if (mainAssociatedSolutions.get(j).size() < minimumNicheCount)
					minimumNicheCount = mainAssociatedSolutions.get(j).size();

			// Looks through the reference points with the least associated solutions and if there is
			// an associated solution for the point among the last rank, it is added to the population.
			for (int j = 0; j < mainAssociatedSolutions.size(); j++)
			{
				// If there is a corresponding solution among the last rank.
				if (mainAssociatedSolutions.get(j).size() == minimumNicheCount)
				{
					// If there is more than 1 corresponding solutions to choose from.
					if (remainingAssociatedSolutions.get(j).size() > 1)
					{
						// If the niche count is 0, the solution with the 
						// closest distance to the reference point is chosen.
						if (minimumNicheCount == 0)
						{
							smallestDistance = calculateDistance(remainingAssociatedSolutions.get(j).get(0), this.referencePoints.get(j), hyperplane);
							index = 0;
							
							for (int k = 1; k < remainingAssociatedSolutions.get(j).size(); k++)
							{
								distance = calculateDistance(remainingAssociatedSolutions.get(j).get(k), this.referencePoints.get(j), hyperplane);

								if (distance < smallestDistance)
								{
									smallestDistance = distance;
									index = k;
								}
							}
						}
						// If the niche count is greater than 0, the 
						// solution is chosen at random from those available.
						else
							index = (int)(Math.random() * remainingAssociatedSolutions.get(j).size());
					}
					else
						index = 0;
					
					// Update the reference point lists of associated solutions.
					// Add the chosen solution to the list of chosen solutions from the last rank.
					mainAssociatedSolutions.get(j).add(remainingAssociatedSolutions.get(j).get(index));
					remainingPopulation.add(remainingAssociatedSolutions.get(j).get(index));
					remainingAssociatedSolutions.get(j).remove(index);
					break;
				}
			}
		}
		
		return remainingPopulation;
	}
	
	// Finds the ideal point (the best theoretical solution that could be generated from the best objective values
	// across all the solutions within the current population) and the intercept values of the objective axes and
	// the worst points on the axes (the point that represents the worst solution on that axis) to represent the 
	// hyperplane encapsulating the population and aid with normalisation of the objective values in each solution. 
	private float[][] constructHyperplane(ArrayList<RefactoringSequence> topRanks, ArrayList<RefactoringSequence> bottomRank)
	{
		ArrayList<RefactoringSequence> population = new ArrayList<RefactoringSequence>(topRanks);
		population.addAll(bottomRank);
		float[] idealPoint = new float[this.c.length];
		float[] intercepts = new float[this.c.length];
		RefactoringSequence[] extremePoints = new RefactoringSequence[this.c.length];
		float minimumASF;
		boolean duplicate = false;
		
		// Initialises the best and worst objective values by
		// passing in the values for the first solution.
		for (int i = 0; i < this.c.length; i++)
			idealPoint[i] = population.get(0).getMOFitness()[i];
					
		// Finds the best and worst possible points that could be 
		// theoretically present among the current population of solutions.
		for (int i = 1; i < population.size(); i++) 
			for (int j = 0; j < this.c.length; j++) 
				if (population.get(i).getMOFitness()[j] > idealPoint[j])
					idealPoint[j] = population.get(i).getMOFitness()[j];
		
		// Finds the solutions that represent the extreme points in the population.
		for (int i = 0; i < this.c.length; i++) 
		{
			minimumASF = achievementScalarizationFunction(population.get(0), i);
			extremePoints[i] = population.get(0);
			
			for (int j = 1; j < population.size(); j++) 
			{			
				float asf = achievementScalarizationFunction(population.get(j), i);
				
				if (asf < minimumASF) 
				{
					minimumASF = asf;
					extremePoints[i] = population.get(j);
				}
			}
		}
		
		// Check whether there are duplicate extreme points.
		for (int i = 0; !duplicate && i < extremePoints.length; i++) 
			for (int j = i + 1; !duplicate && j < extremePoints.length; j++)
				duplicate = (extremePoints[i] == extremePoints[j]);

		// Duplicates exist so the unique hyperplane cannot be constructed.
		// In this case, the worst objective values in each axis are used as the intercepts.
		if (duplicate)
		{
			for (int i = 0; i < this.c.length; i++) 
				intercepts[i] = extremePoints[i].getMOFitness()[i];
		}
		// Otherwise, the intercepts are found using gaussian elimination.
		else 
		{
			// Matrix A and vector b declared for guassian elimination.
			float[][] A = new float[this.c.length][this.c.length];
			float[] b = new float[this.c.length];
			
			for (int i = 0; i < this.c.length; i++)
				b[i] = 1.0f;

			// Find the equation of the hyperplane, where A and b
			// represent the linear equations that make up the equation.
			for (int i = 0; i < this.c.length; i++)
			{
				float[] aux = new float[this.c.length];

				for (int j = 0; j < this.c.length; j++)
					aux[j] = extremePoints[i].getMOFitness()[j];

				A[i] = aux;
			}

			// The equation of the hyperplane.
			float[] x = guassianElimination(A, b);

			// Find intercepts.
			for (int i = 0; i < this.c.length; i++)
				intercepts[i] = 1.0f / x[i];
		}

		return new float[][]{idealPoint, intercepts};
	}

	// Uses to find the extreme points along each objective axis as the extreme point will have the minimal ASF value.
	// Based off the ASF method of Tsung-Che Chiang (http://web.ntnu.edu.tw/~tcchiang/publications/nsga3cpp/nsga3cpp.htm).
	private float achievementScalarizationFunction(RefactoringSequence solution, int index) 
	{
		float weight = (index == 0) ? 1.0f : 0.000001f;
		float maxRatio = solution.getMOFitness()[0] / weight;

		for (int i = 1; i <  this.c.length; i++) 
		{
			weight = (index == i) ? 1.0f : 0.000001f;

			if (maxRatio < solution.getMOFitness()[i] / weight)
				maxRatio = solution.getMOFitness()[i] / weight;
		}

		return maxRatio;
	}

	// Given a NxN matrix A and a Nx1 vector b, gaussian elimination is used to generate a Nx1
	// vector x such that Ax = b. This vector can then be used to get the intercepts to the 
	// hyperplane on each axis. Based off the GaussianElimination method of Tsung-Che Chiang 
	// (http://web.ntnu.edu.tw/~tcchiang/publications/nsga3cpp/nsga3cpp.htm).
	private float[] guassianElimination(float[][] A, float[] b) 
	{		
		int N = A.length;
		float[] x = new float[N];
		
		for (int i = 0; i < N; i++)
			x[i] = 0.0f;
		
		float Ab[][] = new float[N][N + 1];
		
		for (int i = 0; i < N; i++)
		{
			float[] temp = new float[A[i].length + 1];
			for (int j = 0; j <= A[i].length; j++)
			{
				if (j == A[i].length)
					temp[j] = b[i];
				else
					temp[j] = A[i][j];
			}
			
			Ab[i] = temp;
		}
		
		for (int base = 0; base < N - 1; base++) 
		{
			for (int target = base + 1; target < N; target++) 
			{
				float ratio = Ab[target][base] / Ab[base][base];
				
				for (int term = 0; term < Ab[base].length; term++) 
					Ab[target][term] -= Ab[base][term] * ratio;
			}
		}

		for (int i = N - 1; i >= 0; i--) 
		{
			for (int known = i + 1; known < N; known++) 
				Ab[i][N] -= Ab[i][known] * x[known];
			
			x[i] = Ab[i][N] / Ab[i][i];
		}
		
		return x;
	}

	// Accumulates the lists of reference points compiled 
	// from the recursive method and returns them as a single list.
	private ArrayList<float[]> generateReferencePoints() 
	{
		return generateReferencePointsRecursive(new float[this.c.length], this.divisions, this.divisions, 0);
	}

	// Finds the vector values of the set of reference points generated
	// across the hyperplane, with the reference points having equal stepsizes.
	// Based off the generate_recursive method of Tsung-Che Chiang 
	// (http://web.ntnu.edu.tw/~tcchiang/publications/nsga3cpp/nsga3cpp.htm).
	private ArrayList<float[]> generateReferencePointsRecursive(float[] point, int left, int total, int element) 
	{
		// Create new list so that only the relevant reference points are returned.
		ArrayList<float[]> referencePoints = new ArrayList<float[]>();
		
		// Compose and return reference point.
		if (element == (this.c.length - 1)) 
		{
			point[element] = (float) left / total;
			referencePoints.add(point.clone());
		} 
		else 
		{
			for (int i = 0; i <= left; i += 1) 
			{
				// Call recursive method to find relevant reference points.
				point[element] = (float) i / total;
				referencePoints.addAll(generateReferencePointsRecursive(point, left - i, total, element + 1));
			}
		}
		
		return referencePoints;
	}

	// Finds the distance between a solution and a corresponding reference point. This is found by calculating
	// the perpendicular distance between the vector value of the solution and the reference line that connects
	// the reference point to the ideal point. The reference point (R) represents the reference line and the 
	// solution point (s) is represented as the distance from the ideal point. To find the point on R closest 
	// to S (S.R/R.R)*R is used. The perpendicular distance vector is found by taking the closest point away
	// from the solution point. The magnitude of that vector will represent the perpendicular distance.
	private float calculateDistance(RefactoringSequence solution, float[] referencePoint, float[][] hyperplane)
	{
		float[] solutionPoint = new float[this.c.length];
		float[] closestPoint = new float[this.c.length];
		float[] distanceVector = new float[this.c.length];
		float sDotR = 0.0f, rDotR = 0.0f, distance = 0.0f;
		
		// Find the translation vector representing the solution within
		// the space where the ideal point is the origin and the vector
		// represents the ratio from the idea point to the worst point.
		for (int i = 0; i < this.c.length; i++)
		{
			if ((hyperplane[0][i] - hyperplane[1][i]) > 0.000001f)
				solutionPoint[i] = (hyperplane[0][i] - solution.getMOFitness()[i]) / (hyperplane[0][i] - hyperplane[1][i]);
			else
			{
				solutionPoint[i] = (hyperplane[0][i] - solution.getMOFitness()[i]) / 0.000001f;
			}
		}
		
		// Find the dot products of the solution and the
		// reference line and the reference line with itself.
		for (int i = 0; i < this.c.length; i++)
		{
			sDotR += (solutionPoint[i] * referencePoint[i]);
			rDotR += (referencePoint[i] * referencePoint[i]);
		}
		
		float scale = sDotR / rDotR;
		
		// The closest point represents the point on the
		// reference line that is closest to the solution point.
		for (int i = 0; i < this.c.length; i++)
			closestPoint[i] = scale * referencePoint[i];
		
		// Find the distance vector connecting the solution point 
		// and the point on the reference line closest to the solution.
		for (int i = 0; i < this.c.length; i++)
			distanceVector[i] = solutionPoint[i] - closestPoint[i];
				
		// Find the magnitude of the distance vector.
		for (int i = 0; i < this.c.length; i++)
			distance += (distanceVector[i] * distanceVector[i]);
		
		return (float) Math.sqrt(distance);
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