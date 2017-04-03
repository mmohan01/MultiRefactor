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
		super.m = new Metrics(super.sc.getSourceFileRepository().getKnownCompilationUnits());		
		super.refactoringInfo = new ArrayList<String>();
		FitnessFunction ff = new FitnessFunction(super.m, super.c.getConfiguration());
		String currentRefactoringInfo = "";
		boolean keepGoing, improved;
		
		float benchmark = 0.0f;
		float currentScore = benchmark;
		float newScore;
		int iteration = 1;
		String unit;
		int[] position = new int[3];
		int steepestElement = 0, steepestRefactoring = 0;
		String steepestUnit = "";

		String ascent = (this.steepestAscent) ? "Steepest" : "First";
		String runInfo = String.format("Search: Hill Climbing\r\nAscent: %s", ascent);
		super.outputSearchInfo(super.resultsPath, runInfo);
		super.outputMetrics(benchmark, true, true, super.resultsPath);
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
					position = super.randomRefactoring(super.c.getRefactorings());
					
					// If an element could be found to refactor.
					// If the search is first ascent, the first neighbour to give a 
					// better score will be returned and the score and information stored.
					// If it is steepest ascent, the best neighbour is noted so that it
					// can be applied after all options have been evaluated.
					if ((position[0] != -1) && (position[1] != -1))
					{
						unit = super.sc.getSourceFileRepository().getKnownCompilationUnits().get(position[1]).getName();
						super.c.getRefactorings().get(position[0]).transform(super.c.getRefactorings().get(position[0])
								                                  .analyze(iteration, position[1], position[2]));
						newScore = ff.calculateNormalisedScore(super.m);
						
						if (j == 0)
							System.out.printf("\r\nNeighbours: %d", j + 1);
						else
							System.out.printf(", %d", j + 1);
						
						if (newScore > currentScore)
						{	
							improved = true;
							currentScore = newScore;
							currentRefactoringInfo = super.c.getRefactorings().get(position[0]).getRefactoringInfo();
							
							// Notes element so the refactoring can be 
							// applied after evaluating each available solution.
							if (this.steepestAscent)
							{
								steepestUnit = unit;
								steepestElement = position[2];
								steepestRefactoring = position[0];
							}
							else
								break;
						}
						
						// Refactoring reversed so the next neighbour can be evaluated.
						super.c.getRefactorings().get(position[0]).transform(super.c.getRefactorings().get(position[0]).analyzeReverse());
					}
					else
						break;
				}
				
				// Either there were no initial refactorings available 
				// or there are no refactorings left to apply.
				if (position[0] == -1)
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

				if ((iteration % 25 == 0) && (position[0] != -1))
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
			if (((maxIterations != -1) && (iteration == maxIterations)) || (position[0] == -1))
				break;	
		}

		// Output time taken to console and refactoring information to results file.
		timeTaken = System.currentTimeMillis() - startTime;
		time = timeTaken / 1000.0;
		System.out.printf("\r\nTime taken to refactor: %.2fs", time);
		super.outputRefactoringInfo(super.resultsPath, time, currentScore - benchmark);

		// Output the final metric values to the results file.
		super.outputMetrics(currentScore, false, true, super.resultsPath);

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