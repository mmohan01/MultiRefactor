package searches;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import multirefactor.Configuration;
import multirefactor.FitnessFunction;
import multirefactor.Metrics;
import multirefactor.RefactoringSequence;
import recoder.CrossReferenceServiceConfiguration;
import recoder.ParserException;
import recoder.io.PropertyNames;

public class GeneticAlgorithmSearch extends Search
{
	private String[] sourceFiles;
	private String outputPath;
	private FitnessFunction ff;
	
	private boolean printAll;	
	private int generations;
	private int populationSize;
	private float crossoverProbability;
	private float mutationProbability;
	private int initialRefactoringRange = 50;

	public GeneticAlgorithmSearch(CrossReferenceServiceConfiguration sc, Configuration c, String[] sourceFiles) 
	{
		super(sc, c);
		this.sourceFiles = sourceFiles;
		this.printAll = true;
		this.generations = 100;
		this.populationSize = 50;
		this.crossoverProbability = 0.2f;
		this.mutationProbability = 0.8f;	
	}

	public GeneticAlgorithmSearch(CrossReferenceServiceConfiguration sc, Configuration c, String[] sourceFiles,
								  boolean printAll, int generations, int populationSize, float crossoverProbability, float mutationProbability) 
	{
		super(sc, c);
		this.sourceFiles = sourceFiles;
		this.printAll = printAll;
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
		
		String runInfo = String.format("Search: Genetic Algorithm\r\nGenerations: %d\r\nPopulation Size: %d"
									   + "\r\nCrossover Probability: %f\r\nMutation Probability: %f",
				                       this.generations, this.populationSize, this.crossoverProbability, this.mutationProbability);
		Metrics m = new Metrics(super.sc.getSourceFileRepository().getKnownCompilationUnits());
		this.ff = new FitnessFunction(m, super.c.getConfiguration());
		float benchmark = 0.0f;
		
		// If priority objective is being used.
		if (super.c.getPriorityClasses() != null)
			this.ff.setPriorityClasses(super.c.getPriorityClasses());
		
		// If priority objective is being used and there are also non priority classes.
		if (super.c.getNonPriorityClasses() != null)
			this.ff.setNonPriorityClasses(super.c.getNonPriorityClasses());
		
		if (this.printAll)
		{
			for (int i = 1; i <= populationSize; i++)
			{
				super.outputSearchInfo(super.resultsPath, i, runInfo);
				outputMetrics(benchmark, m, true, false, i, super.resultsPath);
			}
		}
		else
		{
			super.outputSearchInfo(super.resultsPath, runInfo);
			super.outputMetrics(benchmark, m, true, true, super.resultsPath);
		}
		
		long timeTaken, startTime = System.currentTimeMillis();
		double time;
		int unitPosition;
		
		System.out.printf("\r\n\r\nCreating Initial Population...");
		ArrayList<RefactoringSequence> population = new ArrayList<RefactoringSequence>(this.populationSize);
		ArrayList<RefactoringSequence> newGeneration = new ArrayList<RefactoringSequence>();
		population = initialise();
		
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
				int[] parents = rankSelection(population.size(), 2);
				newGeneration.addAll(crossover(population.get(parents[0]), population.get(parents[1])));
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
			
			// Increases the size of the array list so it can add the new solutions before 
			// sorting them and being trimmed back down to the population size.
			newGeneration.trimToSize();
			population.ensureCapacity(this.populationSize + newGeneration.size());
			population.addAll(newGeneration);
			
			// Sort new population by fitness and truncate it to remove weakest solutions.
			population = new ArrayList<RefactoringSequence>(sort(population));
			population.trimToSize();
		}
		
		newGeneration = null;
		timeTaken = System.currentTimeMillis() - startTime;
		time = timeTaken / 1000.0;
		
		if (this.printAll)
		{
			System.out.printf("\r\n\r\nPrinting Population");
			
			for (int i = 0; i < population.size(); i++)
			{
				System.out.printf("\r\n  Population %d", i + 1);
				resetModel();
				
				// Reconstruct model so it can be printed.
				for (int j = 0; j < population.get(i).getRefactorings().size(); j++)
				{
					unitPosition = super.unitPosition(population.get(i).getNames().get(j)[0]);
					super.c.getRefactorings().get(population.get(i).getRefactorings().get(j))
						                     .transform(super.c.getRefactorings().get(population.get(i).getRefactorings().get(j))
						                     .analyze((j + 1), unitPosition, population.get(i).getPositions().get(j)));
				}
				
				//Output information.
				outputRefactoringInfo(super.resultsPath, time, population.get(i).getFitness() - benchmark, i + 1, population.get(i).getRefactoringInfo());
				m = new Metrics(super.sc.getSourceFileRepository().getKnownCompilationUnits(), population.get(i).getAffectedClasses());	
				outputMetrics(population.get(i).getFitness(), m, false, false, i + 1, super.resultsPath);

				// Output refactored solution.
				String newOutputPath = this.outputPath + "Solution" + (i + 1) + "/";
				super.sc.getProjectSettings().setProperty(PropertyNames.OUTPUT_PATH, newOutputPath);
				super.print(super.sc.getSourceFileRepository());	
			}
		}
		else
		{
			resetModel();
			
			// Reconstruct model so it can be printed.
			for (int i = 0; i < population.get(0).getRefactorings().size(); i++)
			{
				unitPosition = super.unitPosition(population.get(0).getNames().get(i)[0]);
				super.c.getRefactorings().get(population.get(0).getRefactorings().get(i))
				                         .transform(super.c.getRefactorings().get(population.get(0).getRefactorings().get(i))
					                     .analyze((i + 1), unitPosition, population.get(0).getPositions().get(i)));
			}
			
			outputRefactoringInfo(super.resultsPath, time, population.get(0).getFitness() - benchmark, -1, population.get(0).getRefactoringInfo());
			m = new Metrics(super.sc.getSourceFileRepository().getKnownCompilationUnits(), population.get(0).getAffectedClasses());	
			super.outputMetrics(population.get(0).getFitness(), m, false, true, super.resultsPath);
			System.out.printf("\r\n\r\nScore has improved overall by %f", population.get(0).getFitness() - benchmark);
			System.out.printf("\r\nPrinting Top Solution");
			super.sc.getProjectSettings().setProperty(PropertyNames.OUTPUT_PATH, this.outputPath);
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
		Metrics m;
		
		for (int i = 0; i < this.populationSize; i++)
		{
			// Reinitialise the initial program model.
			if (i > 0)
				resetModel();
			
			// Applies random refactorings to each solution to create an initial population.
			// The amount of refactorings applied in each case is chosen randomly within the range supplied.
			int refactoringAmount = ((int)(Math.random() * this.initialRefactoringRange)) + 1;
			ArrayList<Integer> posSequence = new ArrayList<Integer>(refactoringAmount);
			ArrayList<Integer> refSequence = new ArrayList<Integer>(refactoringAmount);
			ArrayList<String[]> nameSequence = new ArrayList<String[]>(refactoringAmount);
			ArrayList<String> refactoringInfo = new ArrayList<String>(refactoringAmount);
			ArrayList<String> affectedClasses = new ArrayList<String>(refactoringAmount);
			System.out.printf("\r\n  Population %d", i + 1);
			
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
				                                  super.c.getRefactorings().get(result[0]).getName(result[1], result[2])});
					super.c.getRefactorings().get(result[0]).transform(super.c.getRefactorings().get(result[0]).analyze((j + 1), result[1], result[2]));
					refactoringInfo.add(super.c.getRefactorings().get(result[0]).getRefactoringInfo());
					affectedClasses.addAll(super.c.getRefactorings().get(result[0]).getAffectedClasses());
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
			population.get(i).setFitness(this.ff.calculateNormalizedScore(m));	
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
				super.c.getRefactorings().get(p1.getRefactorings().get(i)).transform(super.c.getRefactorings().get(p1.getRefactorings().get(i))
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
					for (int j = 1; j <= super.c.getRefactorings().get(p2.getRefactorings().get(i2)).getAmount(unitPosition); j++)
					{	
						if (super.c.getRefactorings().get(p2.getRefactorings().get(i2)).getName(unitPosition, j).equals(p2.getNames().get(i2)[1]))
						{
							elementPosition = j;
							break;
						}
					}
				}
				
				// If the element exists and can be refactored.
				if (elementPosition != -1)
				{
					super.c.getRefactorings().get(p2.getRefactorings().get(i2)).transform(super.c.getRefactorings().get(p2.getRefactorings().get(i2))
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
		children.get(0).setFitness(this.ff.calculateNormalizedScore(m));	
		
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
				super.c.getRefactorings().get(p2.getRefactorings().get(i)).transform(super.c.getRefactorings().get(p2.getRefactorings().get(i))
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
					for (int j = 1; j <= super.c.getRefactorings().get(p1.getRefactorings().get(i2)).getAmount(unitPosition); j++)
					{
						if (super.c.getRefactorings().get(p1.getRefactorings().get(i2)).getName(unitPosition, j).equals(p1.getNames().get(i2)[1]))
						{
							elementPosition = j;
							break;
						}
					}
				}
				
				// If the element exists and can be refactored.
				if (elementPosition != -1)
				{
					super.c.getRefactorings().get(p1.getRefactorings().get(i2)).transform(super.c.getRefactorings().get(p1.getRefactorings().get(i2))
						   .analyze((i + 1), unitPosition, elementPosition));
					refactoringInfo2.add(p1.getRefactoringInfo().get(i2));
					affectedClasses2.add(p1.getAffectedClasses().get(i2));
					c2Refactorings.add(p1.getRefactorings().get(i2));
					c2Positions.add(elementPosition);
					c2Names.add(p1.getNames().get(i2));
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
		children.get(1).setFitness(this.ff.calculateNormalizedScore(m));	
		
		return children;
	}
	
	// Applies a random refactoring to the end of the refactoring sequence passed in.
	// If the refactoring is not applicable it will keep trying until an applicable	refactoring
	// is found or it runs out of possibilities. In this case the original sequence is returned.
	private RefactoringSequence mutation(RefactoringSequence p)
	{					
		// Reinitialise the program model.
		resetModel();
		
		for (int i = 0; i < p.getRefactorings().size(); i++)
		{
			int unitPosition = super.unitPosition(p.getNames().get(i)[0]);
			super.c.getRefactorings().get(p.getRefactorings().get(i)).transform(super.c.getRefactorings().get(p.getRefactorings().get(i))
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
                                          super.c.getRefactorings().get(result[0]).getName(result[1], result[2])});
			super.c.getRefactorings().get(result[0]).transform(super.c.getRefactorings().get(result[0])
				   .analyze((refSequence.size()), result[1], result[2]));
			refactoringInfo.add(super.c.getRefactorings().get(result[0]).getRefactoringInfo());
			affectedClasses.addAll(super.c.getRefactorings().get(result[0]).getAffectedClasses());
			p.setRefactorings(refSequence);
			p.setPositions(posSequence);
			p.setNames(nameSequence);
			p.setRefactoringInfo(refactoringInfo);
			p.setAffectedClasses(affectedClasses);
			
			// Calculate fitness up front so program model isn't needed at a later point.
			Metrics m = new Metrics(super.sc.getSourceFileRepository().getKnownCompilationUnits(), affectedClasses);		
			p.setFitness(this.ff.calculateNormalizedScore(m));	
		}
		else
			System.out.printf("\r\n    Mutation N/A");
		
		return p;
	}

	// Sorts the population so that the more fit solutions are at the front of the list.
	// After the list is sorted, it is truncated to remove the weakest solutions.
	private ArrayList<RefactoringSequence> sort(ArrayList<RefactoringSequence> population) 
	{
		RefactoringSequence[] arrayPopulation = population.toArray(new RefactoringSequence[0]);
		Arrays.sort(arrayPopulation, new FitnessComparator());
		ArrayList<RefactoringSequence> sortedPopulation = new ArrayList<RefactoringSequence>(this.populationSize);
		
		for (int i = 0; i < this.populationSize; i++)
			sortedPopulation.add(arrayPopulation[i]);
		
		population = null;
		arrayPopulation = null;
		return sortedPopulation;
	}


	// Reset the program model to the initial input so it can be reused.
	private void resetModel()
	{
		// Save the input path and then overwrite the program model using the constructor.
		// Recreate relevant refactoring objects to ensure old program model is no longer referenced.
		String inputPath = super.sc.getProjectSettings().getProperty(PropertyNames.INPUT_PATH);
		super.sc = new CrossReferenceServiceConfiguration();
		super.c.resetRefactorings(super.sc, this.c.getRefactorings(), true);
		
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
		
	// Finds a random available refactoring in the specified model and passes back the
	// refactoring used and the position of the applicable program element in the model.
	private int[] randomRefactoring()
	{
		int[] position = new int[2];
		int r = -1;

		// Find element to refactor.
		if (super.c.getRefactorings().size() > 0)
		{
			r = (int)(Math.random() * super.c.getRefactorings().size());
			position = super.randomElement(super.c.getRefactorings().get(r));
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
			for (r = 0; r < super.c.getRefactorings().size(); r++)
			{
				// Stops the loop from repeating the check for the previous refactoring.
				if ((r == exclude) && ((r + 1) < super.c.getRefactorings().size()))
					r++;
				else if (r == exclude)
					break;

				position = super.randomElement(super.c.getRefactorings().get(r));

				if ((position[0] != -1) && (position[1] != -1))
					break;
			}

			if ((position[0] == -1) && (position[1] == -1))
				r = -1;
		}
		
		return new int[]{r, position[0], position[1]};
	}
	
	// Uses Roulette Selection approach but instead of appropriating the fitness values of the solutions 
	// it creates manually more standard proportions in case single fitness values are not available. 
	// sp is the selective pressure and has been added as an input parameter in case it is desired to change it.
	private int[] rankSelection(int populationSize, float sp)
	{
		float[] rankProportions = new float[populationSize];
		int[] parents = new int[2];
		float fitnessSum = 0;
		float dynamicSum = 0;

		// Formula creates a balanced set of proportions for each rank.
		// This overcomes the scaling problems of the proportional fitness assignment.
		// The linear ranking formula takes in sp values in the range (1,2]. 
		for (int i = 0; i < populationSize; i++)
		{
			rankProportions[i] = (2 - sp) + 2 * (sp - 1) * (((populationSize - 1) - i) / (populationSize - 1));
			fitnessSum += rankProportions[i];
		}

		float rouletteSelection = (float)(Math.random() * fitnessSum);

		// Find the first parent iteration.
		for (int i = 0; i < populationSize; i++)
		{
			dynamicSum += rankProportions[i];

			if (dynamicSum >= rouletteSelection)
			{
				parents[0] = i;
				i = populationSize;
			}
		}

		rouletteSelection = (float)(Math.random() * fitnessSum);
		dynamicSum = 0;

		// Find the second parent iteration.
		for (int i = 0; i < populationSize; i++)
		{
			dynamicSum += rankProportions[i];

			if (dynamicSum >= rouletteSelection)
			{
				if (i != parents[0])
					parents[1] = i;
				else if (i < (populationSize - 1))
					parents[1] = (i + 1);
				else
					parents[1] = (i - 1);

				i = populationSize;
			}
		}		

		return parents;
	}
	
	// Output refactoring information to results file for a solution.
	// Can be used for when printing out the whole population.
	private void outputRefactoringInfo(String pathName, double time, double qualityGain, int solution, ArrayList<String> refactoringInfo)
	{	
		String runName;

		// Create a location for the results output.
		if (solution == -1)
			runName = String.format("%sresults.txt", pathName);
		else
			runName = String.format("%sresultsSolution%d.txt", pathName, solution);

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

			bw.append(String.format("\r\n\r\nScore has improved overall by %f", qualityGain));
			bw.append(String.format("\r\nTime taken to refactor: %.2fs", time));
			bw.close();
		}
		catch (IOException e) 
		{
			System.out.println("\r\nEXCEPTION: Cannot export results to text file.");
			System.exit(1);
		}
	}

	// Outputs the metric values for a solution.
	// Can be used for when printing out the whole population.
	private void outputMetrics(float score, Metrics metric, boolean initial, boolean log, int solution, String pathName)
	{
		FitnessFunction ff = new FitnessFunction(super.c.getConfiguration());
		
		// If priority objective is being used.
		if (super.c.getPriorityClasses() != null)
			ff.setPriorityClasses(super.c.getPriorityClasses());
		
		// If priority objective is being used and there are also non priority classes.
		if (super.c.getNonPriorityClasses() != null)
			ff.setNonPriorityClasses(super.c.getNonPriorityClasses());
		
		String[] outputs = ff.createOutput(metric);
		
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

			// Outputs the metric values for the project to a text file.
			for (int i = 0; i < outputs.length; i++)
				bw.append("\r\n" + outputs[i]);
			
			bw.append(String.format("\r\nOverall fitness function score: %f", score));
			bw.close();
		}
		catch (IOException e) 
		{
			System.out.println("\r\nEXCEPTION: Cannot export results to text file.");
			System.exit(1);
		}

		if (log)
		{
			// Outputs the metric values for the project to the console for immediate feedback.
			System.out.printf("\r\n");
			
			for (int i = 0; i < outputs.length; i++)
			{
				if(outputs[i].charAt(outputs[i].length() - 7) == '.')
					outputs[i] = outputs[i].substring(0, outputs[i].length() - 4);
				
				System.out.printf("\r\n%s", outputs[i]);
			}
			
			System.out.printf("\r\nOverall fitness function score: %.2f", score);
		}
	}
		
	public void setInitialRefactoringRange(int refactoringRange)
	{
		this.initialRefactoringRange = refactoringRange;
	}
}