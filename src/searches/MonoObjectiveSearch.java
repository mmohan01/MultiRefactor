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

public class MonoObjectiveSearch extends GeneticAlgorithmSearch
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

	public MonoObjectiveSearch(CrossReferenceServiceConfiguration sc, Configuration c, String[] sourceFiles) 
	{
		super(sc, c);
		this.sourceFiles = sourceFiles;
		this.printAll = true;
		this.generations = 100;
		this.populationSize = 50;
		this.crossoverProbability = 0.2f;
		this.mutationProbability = 0.8f;	
	}

	public MonoObjectiveSearch(CrossReferenceServiceConfiguration sc, Configuration c, String[] sourceFiles,
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
		super.setPriority(this.ff, super.c);
		float benchmark = 0.0f;
		
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
		
		if (this.printAll)
		{
			System.out.printf("\r\n\r\nPrinting Population");
			
			for (int i = 0; i < population.size(); i++)
			{
				System.out.printf("\r\n  Solution %d", i + 1);
				m = reconstructSolution(population.get(i), timeTaken / 1000.0, benchmark, i + 1);
				outputMetrics(population.get(i).getFitness(), m, false, false, i + 1, super.resultsPath);

				// Output refactored solution.
				String newOutputPath = this.outputPath + "Solution" + (i + 1) + "/";
				super.sc.getProjectSettings().setProperty(PropertyNames.OUTPUT_PATH, newOutputPath);
				super.print(super.sc.getSourceFileRepository());	
			}
			
			// Add diversity score for finished solution. Only used for objective 
			// experiment to make it easier to compare against multi-objective approach
			// and avoid the need to manually work out diversity score afterwards.
			super.setConfiguration(new Configuration("./configurations/diversity.txt"));
	
			m = new Metrics(super.sc.getSourceFileRepository().getKnownCompilationUnits(), population.get(0).getElementDiversity());	
			float diversity = m.diversity();
			String runName = String.format("%sresultsSolution1.txt", super.resultsPath);

			try 
			{
				BufferedWriter bw = new BufferedWriter(new FileWriter(runName, true));
				bw.append(String.format("\r\n\r\n**Diversity objective score: %f**", diversity));
				bw.close();
			}
			catch (IOException e) 
			{
				System.out.println("\r\nEXCEPTION: Cannot output diversity score to text file.");
				System.exit(1);
			}
		}
		else
		{
			m = reconstructSolution(population.get(0), timeTaken / 1000.0, benchmark, -1);	
			super.outputMetrics(population.get(0).getFitness(), m, false, true, super.resultsPath);
			System.out.printf("\r\n\r\nScore has improved overall by %f", population.get(0).getFitness() - benchmark);
			System.out.printf("\r\nPrinting Top Solution");
			super.sc.getProjectSettings().setProperty(PropertyNames.OUTPUT_PATH, this.outputPath);
			super.print(super.sc.getSourceFileRepository());	
		}
		
		// Output time taken to console and refactoring information to results file.
		population = null;
		timeTaken = System.currentTimeMillis() - startTime;
		System.out.printf("\r\n\r\nOverall time taken for search: %.2fs", timeTaken / 1000.0);
		System.out.printf("\r\n-------------------------------------");
	}
	
	// Creates an initial population of refactoring solutions at random.
	// Using the initial refactoring range a random amount of refactorings will
	// be applied in that range to create a possible solution and this will be 
	// repeated until the population size has been satisfied.
	private ArrayList<RefactoringSequence> initialise()
	{
		ArrayList<RefactoringSequence> population = new ArrayList<RefactoringSequence>(this.populationSize);
		
		for (int i = 0; i < this.populationSize; i++)
		{
			// Reinitialise the program model to the original state.
			if (i > 0)
				resetModel();
			
			// Applies random refactorings to each solution to create an initial population.
			// The amount of refactorings applied in each case is chosen randomly within the range supplied.
			population.add(super.createInitialSolution(this.initialRefactoringRange, super.c.getRefactorings(), i + 1));
			
			// Calculate fitness up front so current model isn't needed at a later point.
			Metrics m = new Metrics(super.sc.getSourceFileRepository().getKnownCompilationUnits(), population.get(i).getAffectedClasses(), 
									population.get(i).getElementDiversity());		
			population.get(i).setFitness(this.ff.calculateNormalisedScore(m));	
		}
		
		return population;
	}

	// Reinitialises the program model before applying the crossover to generate a child solution.
	// This is done twice in order to generate the offspring expected from the crossover process.
	private ArrayList<RefactoringSequence> crossover(RefactoringSequence p1, RefactoringSequence p2)
	{		
		ArrayList<RefactoringSequence> children = new ArrayList<RefactoringSequence>(2);
		Metrics m;
		
		// For each refactoring sequence passed in, a cut point is randomly chosen.
		int cutPoint1 = ((int)(Math.random() * (p1.getRefactorings().size() - 1))) + 1;
		int cutPoint2 = ((int)(Math.random() * (p2.getRefactorings().size() - 1))) + 1;
		
		// Initialise the program model to the original state and generate the first child solution.
		resetModel();
		RefactoringSequence child1 = super.generateChild(p1, p2, cutPoint1, cutPoint2, 1, super.c.getRefactorings());
		
		// Calculate fitness up front so current model isn't needed at a later point.
		m = new Metrics(super.sc.getSourceFileRepository().getKnownCompilationUnits(), child1.getAffectedClasses(), child1.getElementDiversity());		
		child1.setFitness(this.ff.calculateNormalisedScore(m));
		children.add(child1);
		
		// Initialise the program model again and generate the second child solution.
		resetModel();
		RefactoringSequence child2 = super.generateChild(p2, p1, cutPoint2, cutPoint1, 2, super.c.getRefactorings());
		
		// Calculate fitness up front so current model isn't needed at a later point.
		m = new Metrics(super.sc.getSourceFileRepository().getKnownCompilationUnits(), child2.getAffectedClasses(), child2.getElementDiversity());		
		child2.setFitness(this.ff.calculateNormalisedScore(m));
		children.add(child2);

		return children;
	}

	// Applies a random refactoring to the end of the refactoring sequence passed in.
	// If the refactoring is not applicable it will keep trying until an applicable	refactoring
	// is found or it runs out of possibilities. In this case the original sequence is returned.
	protected RefactoringSequence mutation(RefactoringSequence p)
	{					
		// Initialise the program model to the original state.
		resetModel();
		RefactoringSequence solution;
		
		for (int i = 0; i < p.getRefactorings().size(); i++)
		{
			int unitPosition = super.unitPosition(p.getNames().get(i)[0]);
			super.c.getRefactorings().get(p.getRefactorings().get(i)).transform(super.c.getRefactorings().get(p.getRefactorings().get(i))
				   .analyze((i + 1), unitPosition, p.getPositions().get(i)));
		}

		int[] result = super.randomRefactoring(super.c.getRefactorings());
		
		// Applies refactoring to model and adds it to the sequence.
		if (result[0] != -1)
		{			
			solution = super.applyRefactoring(p, result, super.c.getRefactorings());
			
			// Calculate fitness up front so current model isn't needed at a later point.
			Metrics m = new Metrics(super.sc.getSourceFileRepository().getKnownCompilationUnits(), solution.getAffectedClasses(),
					                solution.getElementDiversity());		
			solution.setFitness(this.ff.calculateNormalisedScore(m));	
		}
		else
		{
			System.out.printf("\r\n    Mutation N/A");
			solution = p;
		}
		
		return solution;
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

	
	// Outputs the metric values for a solution.
	// Can be used for when printing out the whole population.
	private void outputMetrics(float score, Metrics metric, boolean initial, boolean log, int solution, String pathName)
	{
		FitnessFunction ff = new FitnessFunction(super.c.getConfiguration());
		super.setPriority(this.ff, super.c);
		
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
		
	// Method reconstructs the model for printing by applying the refactorings in the refactoring solution.
	// The refactoring details are then output and the metric object used to measure fitness for the solution is returned.
	private Metrics reconstructSolution(RefactoringSequence solution, double time, float benchmark, int solutionRank)
	{
		// Initialise the program model to the original state.
		resetModel();
		int unitPosition;

		// Reconstruct model so it can be printed.
		for (int i = 0; i < solution.getRefactorings().size(); i++)
		{
			unitPosition = super.unitPosition(solution.getNames().get(i)[0]);
			super.c.getRefactorings().get(solution.getRefactorings().get(i)).transform(super.c.getRefactorings().get(solution.getRefactorings().get(i))
				   .analyze((i + 1), unitPosition, solution.getPositions().get(i)));
		}

		//Output refactoring information and return metric object.
		outputRefactoringInfo(super.resultsPath, time, solution.getFitness() - benchmark, solutionRank, solution.getRefactoringInfo());
		return new Metrics(super.sc.getSourceFileRepository().getKnownCompilationUnits(), solution.getAffectedClasses(), solution.getElementDiversity());	
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
				bw.append(String.format("\r\n%s", refactoringInfo.get(i)));

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
	
	
	public void setInitialRefactoringRange(int refactoringRange)
	{
		this.initialRefactoringRange = refactoringRange;
	}
}