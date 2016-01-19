package searches;

import java.util.ArrayList;

import recoder.CrossReferenceServiceConfiguration;
import refactorings.Refactoring;
import refactory.Configuration;
import refactory.FitnessFunction;
import refactory.Metrics;

public class SimulatedAnnealingSearch extends Search
{
	private int iterations;
	private float temperature;

	public SimulatedAnnealingSearch(CrossReferenceServiceConfiguration sc, Configuration c) 
	{
		super(sc, c);
		this.iterations = 100;
		this.temperature = 4.0f;
	}

	public SimulatedAnnealingSearch(CrossReferenceServiceConfiguration sc, Configuration c, int iterations, float temperature) 
	{
		super(sc, c);
		this.iterations = iterations;
		this.temperature = temperature;
	}
	
	public void run()
	{
		ArrayList<Refactoring> refactorings = c.getRefactorings();
		Metrics m = new Metrics(super.sc.getSourceFileRepository().getKnownCompilationUnits());		
		FitnessFunction ff = new FitnessFunction();
		super.refactoringInfo = new ArrayList<String>();

		float benchmark = ff.calculateScore(m, super.c.getConfiguration());
		float best = benchmark;
		float currentTemperature = this.temperature;
		int bestIteration = 1; 
		int r = -1;
		int[] position = new int[2];
		int[] neighbour = new int[3];

		String runInfo = String.format("Search: Simulated Annealing\r\nIterations: %d\r\nStarting Temperature: %f", this.iterations, this.temperature);
		super.outputSearchInfo(super.resultsPath, runInfo);
		super.outputMetrics(benchmark, m, true, true, super.resultsPath);
		System.out.printf("\n\nRefactoring...");
		long timeTaken, startTime = System.currentTimeMillis();
		double time;

		// Applies refactorings by starting at a random point and find all the neighbouring 
		// refactorings. These are then compared with the current score and the improved options
		// are chosen until there are no longer any available.
		for (int i = 1; i <= this.iterations; i++)
		{
			// Find element to refactor. If the search has only 
			// started a random element will be chosen as a start point.
			if (i == 1)
			{
				if (refactorings.size() > 0)
				{
					r = (int) (Math.random() * refactorings.size());
					position = super.randomElement(refactorings.get(r));
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
					for (r = 0; r < refactorings.size(); r++)
					{
						// Stops the loop from repeating the check for the previous refactoring.
						if ((r == exclude) && ((r + 1) < refactorings.size()))
							r++;
						else if (r == exclude)
							break;
						
						position = super.randomElement(refactorings.get(r));
						if ((position[0] != -1) && (position[1] != -1))
							break;
					}
					
					if ((position[0] == -1) && (position[1] == -1))
						r = -1;
				}
			}
			
			// If a random element could be found for a starting point.
			if ((position[0] != -1) && (position[1] != -1))
			{
				neighbour = super.getNeighbour(position[0], position[1], i, refactorings, currentTemperature, ff, m, best);
				position[0] = neighbour[0];
				position[1] = neighbour[1];
				r = neighbour[2];
			}
			
			if (r == -1)
			{
				System.out.printf("\nThere are no refactorings available - search terminating.");
				i = this.iterations;
			}
			else
			{
				refactorings.get(r).transform(refactorings.get(r).analyze(i, position[0], position[1]));
				super.refactoringInfo.add(refactorings.get(r).getRefactoringInfo());
				best = ff.calculateScore(m, super.c.getConfiguration());
				bestIteration = i;
			}

			if (i % 25 == 0)
			{
				timeTaken = System.currentTimeMillis() - startTime;
				time = timeTaken / 1000.0;
				int percent = (int) ((float)i / this.iterations*100);
				System.out.printf("\n  Search has taken %.2fs so far (%d%% complete)", time, percent);
			}
			
			double step = (this.iterations - i) / (double)this.iterations;
			currentTemperature = (float) (this.temperature * step * step);
		}

		// Output time taken to console and refactoring information to results file.
		timeTaken = System.currentTimeMillis() - startTime;
		time = timeTaken / 1000.0;
		System.out.printf("\nTime taken to refactor: %.2fs", time);
		super.outputRefactoringInfo(super.resultsPath, time, best - benchmark);

		// Output the final metric values to the results file.
		super.outputMetrics(best, m, false, true, super.resultsPath);

		// Output the lowest visibility value measured during the search and at what iteration it was acquired.
		System.out.printf("\n\nBest score acquired was %f at iteration %d", best, bestIteration);
		System.out.printf("\nScore has improved overall by %f", best - benchmark);

		// Apply refactorings from the AST to the source code.
		System.out.printf("\nApplying Transformations...");   	
		super.print(super.sc.getSourceFileRepository());

		timeTaken = System.currentTimeMillis() - startTime;
		time = timeTaken / 1000.0;
		System.out.printf("\nOverall time taken for search: %.2fs", time);
	}
}