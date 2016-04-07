package searches;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import recoder.CrossReferenceServiceConfiguration;
import recoder.ParserException;
import recoder.io.PropertyNames;
import refactorings.Refactoring;
import refactory.Configuration;
import refactory.FitnessFunction;
import refactory.Metrics;
import refactory.RefactoringSequence;

public class MultiObjectiveSearch extends Search
{
	private Configuration[] c;
	private ArrayList <Refactoring> refactorings;
	private String[] sourceFiles;
	private String outputPath;
		
	private int generations;
	private int populationSize;
	private float crossoverProbability;
	private float mutationProbability;
	private int initialRefactoringRange = 10;

	public MultiObjectiveSearch(CrossReferenceServiceConfiguration sc, Configuration[] c, ArrayList <Refactoring> refactorings, String[] sourceFiles) 
	{
		super(sc);
		this.c = c;
		this.refactorings = refactorings;
		this.sourceFiles = sourceFiles;
		this.generations = 5;
		this.populationSize = 10;
		this.crossoverProbability = 0.5f;
		this.mutationProbability = 0.5f;
	}

	public MultiObjectiveSearch(CrossReferenceServiceConfiguration sc, Configuration[] c, ArrayList <Refactoring> refactorings, String[] sourceFiles,
								int generations, int populationSize, float crossoverProbability, float mutationProbability)
	{
		super(sc);
		this.c = c;
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

		System.out.printf("\n\nCreating Initial Population...");
		ArrayList<RefactoringSequence> population = new ArrayList<RefactoringSequence>(this.populationSize);
		ArrayList<RefactoringSequence> newGeneration = new ArrayList<RefactoringSequence>();
		population = fitness(initialize());
		
		// At each generation, crossover is applied to produce a number of child solutions.
		// Then, mutation is applied amongst these new solutions to introduce variety.
		// Fitness is measured for the new solutions and they are sorted accordingly.
		for (int i = 1; i <= this.generations; i++)
		{
			System.out.printf("\n\nIteration %d:", i);
			newGeneration = new ArrayList<RefactoringSequence>();
			
			// Crossover is always done once for each generation but beyond that the
			// amount of times it is executed depends on the crossover probability.
			do
			{
				System.out.printf("\nCrossover...");
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
				System.out.printf("\nMutation...");
				int randomChild = (int)(Math.random() * newGeneration.size());
				newGeneration.set(randomChild, mutation(newGeneration.get(randomChild)));
			}
			
			// The current population is measured and sorted accordingly.
			System.out.printf("\nFitness...");
			newGeneration.trimToSize();
			population.ensureCapacity(this.populationSize + newGeneration.size());
			population.addAll(newGeneration);
			population = new ArrayList<RefactoringSequence>(fitness(population));
			population.trimToSize();
		}
		
		newGeneration = null;
		timeTaken = System.currentTimeMillis() - startTime;
		time = timeTaken / 1000.0;
		
		System.out.printf("\n\nPrinting Population");

		for (int i = 0; i < population.size(); i++)
		{
			resetModel();
			
			// Reconstruct model so it can be printed.
			for (int j = 0; j < population.get(i).getRefactorings().size(); j++)
			{
				this.refactorings.get(population.get(i).getRefactorings().get(j))
					             .transform(this.refactorings.get(population.get(i).getRefactorings().get(j))
					             .analyze((j + 1), population.get(i).getPositions().get(j)[0], population.get(i).getPositions().get(j)[1]));
			}

			// Output information.
			outputRefactoringInfo(super.resultsPath, time, i + 1, population.get(i).getRefactoringInfo());
			outputMetrics(population.get(i).getMOFitness(), false, false, i + 1, super.resultsPath);
			
			// Output refactored solution.
			String newOutputPath = this.outputPath + "s/Solution" + (i + 1);
			super.sc.getProjectSettings().setProperty(PropertyNames.OUTPUT_PATH, newOutputPath);
			super.print(super.sc.getSourceFileRepository());	
		}

		// Output time taken to console and refactoring information to results file.
		population = null;
		timeTaken = System.currentTimeMillis() - startTime;
		time = timeTaken / 1000.0;
		System.out.printf("\nOverall time taken for search: %.2fs", time);
		System.out.printf("\n-------------------------------------");
	}
	
	// Creates an initial population of refactoring solutions at random.
	// Using the initial refactoring range a random amount of refactorings will
	// be applied in that range to create a possible solution and this will be 
	// repeated until the population size has been satisfied.
	private ArrayList<RefactoringSequence> initialize()
	{
		ArrayList<RefactoringSequence> population = new ArrayList<RefactoringSequence>(this.populationSize);
		FitnessFunction ff = new FitnessFunction();
		Metrics m = new Metrics(super.sc.getSourceFileRepository().getKnownCompilationUnits());	
		float benchmark[] = new float[this.c.length];
		float finalScore[] = new float[this.c.length];
		
		for (int i = 0; i < this.c.length; i++)
			benchmark[i] = ff.calculateScore(m, this.c[i].getConfiguration());
		
		for (int i = 0; i < this.populationSize; i++)
		{
			// Reinitialise the initial program model.
			if (i > 0)
				resetModel();

			outputMetrics(benchmark, true, false, i + 1, super.resultsPath);

			// Applies random refactorings to each solution to create an initial population.
			// The amount of refactorings applied in each case is chosen randomly within the range supplied.
			int refactoringAmount = ((int)(Math.random() * this.initialRefactoringRange)) + 1;
			ArrayList<int[]> posSequence = new ArrayList<int[]>(refactoringAmount);
			ArrayList<Integer> refSequence = new ArrayList<Integer>(refactoringAmount);
			ArrayList<String[]> nameSequence = new ArrayList<String[]>(refactoringAmount);
			ArrayList<String> refactoringInfo = new ArrayList<String>(refactoringAmount);
			
			for (int j = 0; j < refactoringAmount; j++)
			{				
				int[] result = randomRefactoring();
				int[] position = {result[1], result[2]};

				if (result[0] == -1)
				{
					System.out.printf("\nThere are no refactorings available for the rest of the sequence.");
					j = refactoringAmount;
				}
				else
				{
					nameSequence.add(new String[]{super.sc.getSourceFileRepository().getKnownCompilationUnits().get(position[0]).getName(),
                                                  this.refactorings.get(result[0]).getName(position[0], position[1])});
					this.refactorings.get(result[0]).transform(this.refactorings.get(result[0])
					                 .analyze((j + 1), position[0], position[1]));
					refactoringInfo.add(this.refactorings.get(result[0]).getRefactoringInfo());
					refSequence.add(result[0]);
					posSequence.add(position);
				}
			}
			
			refSequence.trimToSize();
			posSequence.trimToSize();
			nameSequence.trimToSize();
			refactoringInfo.trimToSize();
			population.add(new RefactoringSequence(refSequence, posSequence, nameSequence, refactoringInfo));
			
			// Calculate fitness up front so program model isn't needed at a later point.
			m = new Metrics(super.sc.getSourceFileRepository().getKnownCompilationUnits());		
			for (int j = 0; j < this.c.length; j++)
				finalScore[j] = ff.calculateScore(m, this.c[j].getConfiguration());
			population.get(i).setMOFitness(finalScore);
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
		FitnessFunction ff = new FitnessFunction();
		Metrics m;
		float finalScore[] = new float[this.c.length];
		
		int c1Size = cutPoint1 + (p2.getRefactorings().size() - cutPoint2);
		ArrayList<Integer> c1Refactorings = new ArrayList<Integer>(c1Size);
		ArrayList<int[]> c1Positions = new ArrayList<int[]>(c1Size);
		ArrayList<String[]> c1Names = new ArrayList<String[]>(c1Size);
		ArrayList<String> refactoringInfo1 = new ArrayList<String>(c1Size);
		
		// Reinitialise the initial program model.
		resetModel();
		
		for (int i = 0; i < c1Size; i++)
		{				
			// The first sequence in each solution will be applicable so 
			// refactorings can be applied without checking.
			if (i < cutPoint1)
			{	
				this.refactorings.get(p1.getRefactorings().get(i))
				                 .transform(this.refactorings.get(p1.getRefactorings().get(i))
						         .analyze((i + 1), p1.getPositions().get(i)[0], p1.getPositions().get(i)[1]));
				refactoringInfo1.add(this.refactorings.get(p1.getRefactorings().get(i)).getRefactoringInfo());
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
					this.refactorings.get(p2.getRefactorings().get(i2))
					                 .transform(this.refactorings.get(p2.getRefactorings().get(i2))
							         .analyze((i + 1), unitPosition, elementPosition));
					refactoringInfo1.add(this.refactorings.get(p2.getRefactorings().get(i2)).getRefactoringInfo());
					c1Refactorings.add(p2.getRefactorings().get(i2));
					c1Positions.add(new int[] {unitPosition, elementPosition});
					c1Names.add(p2.getNames().get(i2));
				}
				else
					System.out.printf("\n  Refactoring %d N/A at child 1", i + 1);
			}
		}

		c1Refactorings.trimToSize();
		c1Positions.trimToSize();
		c1Names.trimToSize();
		refactoringInfo1.trimToSize();
		children.add(new RefactoringSequence(c1Refactorings, c1Positions, c1Names, refactoringInfo1));
		
		// Calculate fitness up front so program model isn't needed at a later point.
		m = new Metrics(super.sc.getSourceFileRepository().getKnownCompilationUnits());		
		for (int j = 0; j < this.c.length; j++)
			finalScore[j] = ff.calculateScore(m, this.c[j].getConfiguration());
		children.get(0).setMOFitness(finalScore);

		// Reinitialise the initial program model again for second child.
		resetModel();
		
		int c2Size = cutPoint2 + (p1.getRefactorings().size() - cutPoint1);
		ArrayList<Integer> c2Refactorings = new ArrayList<Integer>(c2Size);
		ArrayList<int[]> c2Positions = new ArrayList<int[]>(c2Size);
		ArrayList<String[]> c2Names = new ArrayList<String[]>(c2Size);
		ArrayList<String> refactoringInfo2 = new ArrayList<String>(c2Size);
					
		for (int i = 0; i < c2Size; i++)
		{			
			// The first sequence in each solution will be applicable so 
			// refactorings can be applied without checking.
			if (i < cutPoint2)
			{
				this.refactorings.get(p2.getRefactorings().get(i))
				                 .transform(this.refactorings.get(p2.getRefactorings().get(i))
						         .analyze((i + 1), p2.getPositions().get(i)[0], p2.getPositions().get(i)[1]));
				refactoringInfo2.add(this.refactorings.get(p2.getRefactorings().get(i)).getRefactoringInfo());
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
					refactoringInfo2.add(this.refactorings.get(p1.getRefactorings().get(i2)).getRefactoringInfo());
					c2Refactorings.add(p1.getRefactorings().get(i2));
					c2Names.add(p1.getNames().get(i2));
					c2Positions.add(new int[] {unitPosition, elementPosition});
				}
				else
					System.out.printf("\n  Refactoring %d N/A at child 2", i + 1);
			}
		}
		
		c2Refactorings.trimToSize();
		c2Positions.trimToSize();
		c2Names.trimToSize();
		refactoringInfo2.trimToSize();
		children.add(new RefactoringSequence(c2Refactorings, c2Positions, c2Names, refactoringInfo2));
		
		// Calculate fitness up front so program model isn't needed at a later point.
		m = new Metrics(super.sc.getSourceFileRepository().getKnownCompilationUnits());		
		for (int j = 0; j < this.c.length; j++)
			finalScore[j] = ff.calculateScore(m, this.c[j].getConfiguration());
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
			this.refactorings.get(p.getRefactorings().get(i)).transform(this.refactorings.get(p.getRefactorings().get(i))
				             .analyze((i + 1), p.getPositions().get(i)[0], p.getPositions().get(i)[1]));
		}
		
		int[] result = randomRefactoring();
		int[] position = {result[1], result[2]};
		
		// Applies refactoring to model and adds it to the sequence.
		if (result[0] != -1)
		{
			ArrayList<Integer> refSequence = new ArrayList<Integer>(p.getRefactorings().size() + 1);
			refSequence = p.getRefactorings();
			ArrayList<int[]> posSequence = new ArrayList<int[]>(p.getPositions().size() + 1);
			posSequence = p.getPositions();
			ArrayList<String[]> nameSequence = new ArrayList<String[]>(p.getNames().size() + 1);
			nameSequence = p.getNames();
			ArrayList<String> refactoringInfo = new ArrayList<String>(p.getRefactoringInfo().size() + 1);
			refactoringInfo = p.getRefactoringInfo();
			
			refSequence.add(result[0]);
			posSequence.add(position);
			nameSequence.add(new String[]{super.sc.getSourceFileRepository().getKnownCompilationUnits().get(position[0]).getName(),
                                          this.refactorings.get(result[0]).getName(position[0], position[1])});
			this.refactorings.get(result[0]).transform(this.refactorings.get(result[0])
					         .analyze((refSequence.size()), position[0], position[1]));
			refactoringInfo.add(this.refactorings.get(result[0]).getRefactoringInfo());
			p.setRefactorings(refSequence);
			p.setPositions(posSequence);
			p.setNames(nameSequence);
			p.setRefactoringInfo(refactoringInfo);
		
			// Calculate fitness up front so program model isn't needed at a later point.
			Metrics m = new Metrics(super.sc.getSourceFileRepository().getKnownCompilationUnits());	
			float finalScore[] = new float[this.c.length];

			for (int j = 0; j < this.c.length; j++)
				finalScore[j] = new FitnessFunction().calculateScore(m, this.c[j].getConfiguration());
			p.setMOFitness(finalScore);
		}
		
		return p;
	}
	
	// Sorts the population by fitness depending on ranks of non dominated 
	// solutions and calculation of crowding distance for last rank added.
	private ArrayList<RefactoringSequence> fitness(ArrayList<RefactoringSequence> population)
	{		
		ArrayList<ArrayList<RefactoringSequence>> dominationFronts = fastNonDominatedSort(population);

		ArrayList<RefactoringSequence> sortedPopulation = new ArrayList<RefactoringSequence>(population.size());
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
			System.out.println("\nEXCEPTION: Cannot read input.");
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
		pathName = pathName.substring(0, (pathName.length() - 1));
		pathName += "s/";
		String runName = String.format("%sresultsSolution%d.txt", pathName, solution);
		File dir = new File(pathName);
		if (!dir.exists()) 
			dir.mkdirs();

		try 
		{
			BufferedWriter bw = new BufferedWriter(new FileWriter(runName, true));
			bw.append("\r\n\r\n======== Applied Refactorings ========");

			for (int i = 0; i < refactoringInfo.size(); i++) 
				bw.append(String.format("\r\n%s", refactoringInfo.get(i)));

			bw.append(String.format("\r\n\r\nTime taken to refactor: %.2fs", time));
			bw.close();
		}
		catch (IOException e) 
		{
			System.out.println("\nEXCEPTION: Cannot export results to text file.");
			System.exit(1);
		}
	}

	// Outputs the metric values for a solution.
	private void outputMetrics(float[] scores, boolean initial, boolean log, int solution, String pathName)
	{
		// Create a location for the results output.
		pathName = pathName.substring(0, (pathName.length() - 1));
		pathName += "s/";
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

			bw.close();
		}
		catch (IOException e) 
		{
			System.out.println("\nEXCEPTION: Cannot export results to text file.");
			System.exit(1);
		}

		if (log)
		{
			// Outputs the fitness function values for the project to the console for immediate feedback.
			for (int i = 0; i < scores.length; i++)
				System.out.printf("\n\nFitness function %d score: %.2f", (i + 1), scores[i]);
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

	// This inner class allows sorting by fitness so that the more fit solutions are at the front of the list.
	private class FitnessComparator implements Comparator<RefactoringSequence> 
	{
		// Compares the two specified individuals using the fitness operator.
		// Returns -1, 0 or 1 as the first argument is greater than, equal to, or less than the second.
		public int compare(RefactoringSequence s1, RefactoringSequence s2) 
		{   
			if (s1.getFitness() > s2.getFitness())
				return -1;
			else if (s1.getFitness() < s2.getFitness())
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
}