package searches;

import java.util.ArrayList;
import java.util.List;

import recoder.CrossReferenceServiceConfiguration;
import recoder.io.SourceFileRepository;
import refactorings.Refactoring;
import refactory.Configuration;
import refactory.FitnessFunction;
import refactory.Metrics;

public class RandomSearch extends Search
{
	private int iterations;

	public RandomSearch (CrossReferenceServiceConfiguration sc, Configuration c) 
	{
		super(sc, c);
		this.iterations = 100;
	}

	public RandomSearch(CrossReferenceServiceConfiguration sc, Configuration c, int iterations) 
	{
		super(sc, c);
		this.iterations = iterations;
	}

	public void run()
	{
		List<Refactoring> refactorings = c.getRefactorings();
		Metrics m = new Metrics(super.sc.getSourceFileRepository().getKnownCompilationUnits());		
		
		FitnessFunction ff = new FitnessFunction();
		super.refactoringInfo = new ArrayList<String>();

		float benchmark = ff.calculateScore(m, super.c.getConfiguration());
		float best = benchmark;
		float newValue = 0;
		int bestIteration = 1;
		int[] position = new int[2];

		String runInfo = String.format("Search: Random\r\nIterations: %d", iterations);
		super.outputSearchInfo(this.resultsPath, runInfo);
		super.outputMetrics(benchmark, m, true, true, this.resultsPath);
		System.out.printf("\n\nRefactoring...");
		long timeTaken, startTime = System.currentTimeMillis();
		double time;

		// Apply refactorings for the given amount of iterations.
		// If a refactoring does not improve on the initial visibility,
		// it is reversed and the iteration doesn't count.
		for (int i = 1; i <= this.iterations; i++)
		{
			// Random refactoring chosen and random class chosen to refactor.
			// Random element is then chosen from the available amount.
			int randomRef = (int) (Math.random() * (refactorings.size()));
			position = super.randomElement(refactorings.get(randomRef));
			
			// Checks in case no elements are returned for the refactoring.
			// Will check again for each available refactoring in the search and 
			// if there are still no applicable elements returned the search will terminate.
			if ((position[0] == -1) && (position[1] == -1))
			{
				int exclude = randomRef;
				for (randomRef = 0; randomRef < refactorings.size(); randomRef++)
				{
					// Stops the loop from repeating the check for the previous refactoring.
					if ((randomRef == exclude) && ((randomRef + 1) < refactorings.size()))
						randomRef++;
					else if (randomRef == exclude)
						break;
					
					position = super.randomElement(refactorings.get(randomRef));
					if ((position[0] != -1) && (position[1] != -1))
						break;
				}
				
				if ((position[0] == -1) && (position[1] == -1))
				{
					System.out.printf("\nSearch terminated before specified number of iterations due to lack of available refactorings");
					break;
				}
			}

			// Refactoring is applied to that element.
			refactorings.get(randomRef).transform(refactorings.get(randomRef).analyze(i, position[0], position[1]));
			super.refactoringInfo.add(refactorings.get(randomRef).getRefactoringInfo());	

			newValue = ff.calculateScore(m, super.c.getConfiguration());

			if (newValue < benchmark)
			{
				refactorings.get(randomRef).transform(refactorings.get(randomRef).analyzeReverse());

				if ((randomRef >= 0) && (randomRef <= 5))
				{
					super.refactoringInfo.remove(super.refactoringInfo.size() - 1);
					i--;
				}
			}
			if (newValue > best)
			{
				best = newValue;
				bestIteration = i;
			}
			if (i%25 == 0)
			{
				timeTaken = System.currentTimeMillis() - startTime;
				time = timeTaken/1000.0;
				int percent = (int) ((float)i/this.iterations*100);
				System.out.printf("\n  Search has taken %.2fs so far (%d%% complete)", time, percent);
			}
		}

		// Output time taken to console and refactoring information to results file.
		timeTaken = System.currentTimeMillis() - startTime;
		time = timeTaken/1000.0;
		System.out.printf("\nTime taken to refactor: %.2fs", time);
		super.outputRefactoringInfo(super.resultsPath, time, best - benchmark);

		// Output the final metric values to the results file.
		super.outputMetrics(newValue, m, false, true, super.resultsPath);

		// Output the lowest visibility value measured during the search and at what iteration it was acquired.
		System.out.printf("\n\nBest score acquired was %f at iteration %d", best, bestIteration);
		System.out.printf("\nScore has improved overall by %.2f", best - benchmark);

		// Apply refactorings from the AST to the source code.
		System.out.printf("\nApplying Transformations...");
		SourceFileRepository sfr = super.sc.getSourceFileRepository();    	
		super.print(sfr);

		timeTaken = System.currentTimeMillis() - startTime;
		time = timeTaken/1000.0;
		System.out.printf("\nOverall time taken for search: %.2fs", time);
	}
}