package searches;

import java.util.ArrayList;

import multirefactor.Configuration;
import multirefactor.FitnessFunction;
import multirefactor.Metrics;
import recoder.CrossReferenceServiceConfiguration;

public class HillClimbingSearch extends Search
{
	private int restartCount;
	private int neighbourCount;
	private boolean steepestAscent;
	
	private int maxIterations = -1;

	
	public HillClimbingSearch(CrossReferenceServiceConfiguration sc, Configuration c) 
	{
		super(sc, c);
		this.restartCount = 0;
		this.neighbourCount = 10;
		this.steepestAscent = false;
	}
	
	public HillClimbingSearch(CrossReferenceServiceConfiguration sc, Configuration c, int restartCount, int neighbourCount, boolean steepestAscent) 
	{
		super(sc, c);
		this.restartCount = restartCount;
		this.neighbourCount = neighbourCount;
		this.steepestAscent = steepestAscent;
	}
	
	public HillClimbingSearch(CrossReferenceServiceConfiguration sc, Configuration c, 
							  int restartCount, int neighbourCount, boolean steepestAscent, int maxIterations) 
	{
		super(sc, c);
		this.restartCount = restartCount;
		this.neighbourCount = neighbourCount;
		this.steepestAscent = steepestAscent;
		this.maxIterations = maxIterations;
	}
	
	public void run()
	{
		Metrics m = new Metrics(super.sc.getSourceFileRepository().getKnownCompilationUnits());		
		FitnessFunction ff = new FitnessFunction(m, super.c.getConfiguration());
		super.refactoringInfo = new ArrayList<String>();
		String currentRefactoringInfo = "";
		boolean keepGoing, improved;
		
		float benchmark = 0;
		float currentScore = benchmark;
		float newScore;
		int iteration = 1;
		int r = -2;
		String unit;
		int[] position = new int[2];
		int steepestElement = 0, steepestRefactoring = 0;
		String steepestUnit = "";

		String ascent = (this.steepestAscent) ? "Steepest" : "First";
		String runInfo = String.format("Search: Hill Climbing\r\nAscent: %s", ascent);
		super.outputSearchInfo(super.resultsPath, runInfo);
		super.outputMetrics(benchmark, m, true, true, super.resultsPath);
		System.out.printf("\r\n\r\nRefactoring...");
		long timeTaken, startTime = System.currentTimeMillis();
		double time;
	
		// Applies refactorings at random to get a number of neighbouring solutions.
		// These are then compared with the current score and the improved options
		// are chosen until there are no longer any available.
		for (int i = 0; i <= this.restartCount; i++)
		{
			if (i > 0)
				System.out.printf("\r\n Restarting %d of %d", i, this.restartCount);
			
			keepGoing = true;
			
			// Loops until no more refactorings are available or until
			// none of the neighbouring solutions found give a better score.
			while (keepGoing)
			{	
				improved = false;
				
				// Finds the random neighbouring solutions, 
				// the amount of which is denoted by neighbour count.
				for (int j = 0; j < this.neighbourCount; j++)
				{
					// Find element to refactor. A random element and refactoring will be chosen.
					if (super.c.getRefactorings().size() > 0)
					{
						r = (int) (Math.random() * super.c.getRefactorings().size());
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
							if (r == exclude)
								if ((r + 1) < super.c.getRefactorings().size())
									r++;
								else
									break;

							position = super.randomElement(super.c.getRefactorings().get(r));
							if ((position[0] != -1) && (position[1] != -1))
								break;
						}

						if ((position[0] == -1) && (position[1] == -1))
							r = -1;
					}
					
					// If an element could be found to refactor.
					// If the search is first ascent, the first neighbour to give a 
					// better score will be returned and the score and information stored.
					// If it is steepest ascent, the best neighbour is noted so that it
					// can be applied after all options have been evaluated.
					if ((position[0] != -1) && (position[1] != -1))
					{
						unit = super.sc.getSourceFileRepository().getKnownCompilationUnits().get(position[0]).getName();
						super.c.getRefactorings().get(r).transform(super.c.getRefactorings().get(r).analyze(iteration, position[0], position[1]));
						newScore = ff.calculateNormalizedScore(m);
						
						if (j == 0)
							System.out.printf("\r\nNeighbours: %d", j + 1);
						else
							System.out.printf(", %d", j + 1);
						
						if (newScore > currentScore)
						{	
							improved = true;
							currentScore = newScore;
							currentRefactoringInfo = super.c.getRefactorings().get(r).getRefactoringInfo();
							
							// Notes element so the refactoring can be 
							// applied after evaluating each available solution.
							if (this.steepestAscent)
							{
								steepestUnit = unit;
								steepestElement = position[1];
								steepestRefactoring = r;
							}
							else
								break;
						}
						
						// Refactoring reversed so the next neighbour can be evaluated.
						super.c.getRefactorings().get(r).transform(super.c.getRefactorings().get(r).analyzeReverse());
					}
					else
						break;
				}
				
				// Either there were no initial refactorings available 
				// or there are no refactorings left to apply.
				if (r == -1)
				{
					if (iteration == 1)
						System.out.printf("\r\nThere are no refactorings available - search terminating.");
					else
						System.out.printf("\r\nThere are no more refactorings available - search terminating.");

					keepGoing = false;
				}
				// No neighbours improved the current solution.
				else if (!improved)
					keepGoing = false;
				// For steepest ascent, best neighbour is retroactively applied.
				else
				{
					if (this.steepestAscent)
					{
						super.c.getRefactorings().get(steepestRefactoring).transform(super.c.getRefactorings().get(steepestRefactoring)
							   .analyze(iteration, steepestUnit, steepestElement));
					}

						super.refactoringInfo.add(currentRefactoringInfo);
						System.out.printf("\r\n%s", currentRefactoringInfo);
				}

				if ((iteration % 25 == 0) && (r != -1))
				{
					timeTaken = System.currentTimeMillis() - startTime;
					time = timeTaken / 1000.0;
					System.out.printf("\r\n  Search has taken %.2fs so far (%d iterations)", time, iteration);
				}

				if ((maxIterations != -1) && (iteration == maxIterations))
					keepGoing = false;
				else if (keepGoing)
					iteration++;
			}
			
			// Stops search if the specified maximum iterations have been reached.
			if (((maxIterations != -1) && (iteration == maxIterations)) || (r == -1))
				break;	
		}

		// Output time taken to console and refactoring information to results file.
		timeTaken = System.currentTimeMillis() - startTime;
		time = timeTaken / 1000.0;
		System.out.printf("\r\nTime taken to refactor: %.2fs", time);
		super.outputRefactoringInfo(super.resultsPath, time, currentScore - benchmark);

		// Output the final metric values to the results file.
		super.outputMetrics(currentScore, m, false, true, super.resultsPath);

		// Output the improvement obtained during the search.
		System.out.printf("\r\n\r\nScore has improved overall by %.2f", currentScore - benchmark);

		// Apply refactorings from the AST to the source code.
		System.out.printf("\r\nApplying Transformations...");	
		super.print(super.sc.getSourceFileRepository());

		timeTaken = System.currentTimeMillis() - startTime;
		time = timeTaken / 1000.0;
		System.out.printf("\r\nOverall time taken for search: %.2fs", time);
		System.out.printf("\r\n-------------------------------------");
	}
}