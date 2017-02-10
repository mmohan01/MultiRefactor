package searches;

import java.util.ArrayList;

import multirefactor.Configuration;
import multirefactor.FitnessFunction;
import multirefactor.Metrics;
import recoder.CrossReferenceServiceConfiguration;
import recoder.ParserException;
import recoder.io.PropertyNames;

public class RandomSearch extends Search
{
	private int iterations;
	
	private String[] sourceFiles;
	private boolean getBest = false;

	public RandomSearch(CrossReferenceServiceConfiguration sc, Configuration c) 
	{
		super(sc, c);
		this.iterations = 100;
	}

	public RandomSearch(CrossReferenceServiceConfiguration sc, Configuration c, int iterations) 
	{
		super(sc, c);
		this.iterations = iterations;
	}
	
	public RandomSearch(CrossReferenceServiceConfiguration sc, Configuration c, int iterations, String[] sourceFiles) 
	{
		super(sc, c);
		this.iterations = iterations;
		this.sourceFiles = sourceFiles;
		this.getBest = true;
	}

	public void run()
	{
		Metrics m = new Metrics(super.sc.getSourceFileRepository().getKnownCompilationUnits());				
		FitnessFunction ff = new FitnessFunction(m, super.c.getConfiguration());
		super.refactoringInfo = new ArrayList<String>();
		ArrayList<Integer> refactorings = new ArrayList<Integer>(this.iterations);
		ArrayList<int[]> positions = new ArrayList<int[]>(this.iterations);

		float benchmark = 0.0f;
		float best = benchmark;
		float newValue = 0;
		int bestIteration = 1;
		int[] position = new int[3];

		String runInfo = String.format("Search: Random\r\nIterations: %d", this.iterations);
		super.outputSearchInfo(super.resultsPath, runInfo);
		super.outputMetrics(benchmark, m, true, true, super.resultsPath);
		System.out.printf("\r\n\r\nRefactoring...");
		long timeTaken, startTime = System.currentTimeMillis();
		double time;

		// Apply refactorings for the given amount of iterations.
		// If a refactoring does not improve on the initial visibility,
		// it is reversed and the iteration doesn't count.
		for (int i = 1; i <= this.iterations; i++)
		{
			// Random refactoring chosen and random class chosen to refactor.
			// Random element is then chosen from the available amount.
			position = super.randomRefactoring(super.c.getRefactorings());
				
			if (position[2] == -1)
			{
				System.out.printf("\r\nSearch terminated before specified number of iterations due to lack of available refactorings");
				this.iterations = i - 1;
				break;
			}

			// Refactoring is applied to that element.
			super.c.getRefactorings().get(position[2]).transform(super.c.getRefactorings().get(position[2]).analyze(i, position[0], position[1]));
			super.refactoringInfo.add(super.c.getRefactorings().get(position[2]).getRefactoringInfo());
			System.out.printf("\r\n%s", super.c.getRefactorings().get(position[2]).getRefactoringInfo());
			newValue = ff.calculateNormalisedScore(m);
				
			if (this.getBest)
			{
				refactorings.add(position[2]);
				positions.add(position);
			}

			if (newValue > best)
			{
				best = newValue;
				bestIteration = i;
			}
			
			if (i % 25 == 0)
			{
				timeTaken = System.currentTimeMillis() - startTime;
				time = timeTaken / 1000.0;
				int percent = (int) ((float) i / this.iterations*100);
				System.out.printf("\r\n  Search has taken %.2fs so far (%d%% complete)", time, percent);
			}
		}

		// Output time taken to console and refactoring information to results file.
		timeTaken = System.currentTimeMillis() - startTime;
		time = timeTaken / 1000.0;
		System.out.printf("\r\nTime taken to refactor: %.2fs", time);
		super.outputRefactoringInfo(super.resultsPath, time, best - benchmark);

		// Output the final metric values to the results file.
		super.outputMetrics(newValue, m, false, true, super.resultsPath);

		// Output the best value measured during the search and at what iteration it was acquired.
		System.out.printf("\r\n\r\nBest score acquired was %.2f at iteration %d", best, bestIteration);
		System.out.printf("\r\nScore has improved overall by %.2f", best - benchmark);
		
		if ((this.getBest) && (bestIteration != this.iterations))
		{
			System.out.printf("\r\nReconstructing Best Iteration...");
			
			// Save the input path and then overwrite the program model using the constructor.
			// Recreate relevant refactoring objects to ensure old program model is no longer referenced.
			String inputPath = super.sc.getProjectSettings().getProperty(PropertyNames.INPUT_PATH);
			String outputPath = super.sc.getProjectSettings().getProperty(PropertyNames.OUTPUT_PATH);
			super.sc = new CrossReferenceServiceConfiguration();
			super.c.resetRefactorings(super.sc, super.c.getRefactorings(), true);
			
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
			super.sc.getProjectSettings().setProperty(PropertyNames.OUTPUT_PATH, outputPath);
			super.sc.getProjectSettings().ensureSystemClassesAreInPath();
			
			for (int i = 0; i < bestIteration; i++)
			{
				super.c.getRefactorings().get(refactorings.get(i)).transform(super.c.getRefactorings().get(refactorings.get(i))
					   .analyze(i + 1, positions.get(i)[0], positions.get(i)[1]));
			}
		}
		
		// Apply refactorings from the AST to the source code.
		System.out.printf("\r\nApplying Transformations...");
		super.print(super.sc.getSourceFileRepository());

		timeTaken = System.currentTimeMillis() - startTime;
		time = timeTaken / 1000.0;
		System.out.printf("\r\nOverall time taken for search: %.2fs", time);
		System.out.printf("\r\n-------------------------------------");
	}
}