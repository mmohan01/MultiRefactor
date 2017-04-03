package searches;

import java.util.ArrayList;

import multirefactor.Configuration;
import multirefactor.FitnessFunction;
import multirefactor.Metrics;
import recoder.CrossReferenceServiceConfiguration;
import recoder.ParserException;
import recoder.io.PropertyNames;

public class SimulatedAnnealingSearch extends Search
{
	private int iterations;
	private float temperature;
	private boolean alwaysMove;
	private int breakout = 34;
	
	private String[] sourceFiles;
	private boolean getBest = false;

	public SimulatedAnnealingSearch(CrossReferenceServiceConfiguration sc, Configuration c) 
	{
		super(sc, c);
		this.iterations = 100;
		this.temperature = 4.0f;
		this.alwaysMove = true;
	}

	public SimulatedAnnealingSearch(CrossReferenceServiceConfiguration sc, Configuration c, int iterations, float temperature, boolean alwaysMove) 
	{
		super(sc, c);
		this.iterations = iterations;
		this.temperature = temperature;
		this.alwaysMove = alwaysMove;
	}
	
	public SimulatedAnnealingSearch(CrossReferenceServiceConfiguration sc, Configuration c, 
			                        int iterations, float temperature, boolean alwaysMove, String[] sourceFiles) 
	{
		super(sc, c);
		this.iterations = iterations;
		this.temperature = temperature;
		this.alwaysMove = alwaysMove;
		this.sourceFiles = sourceFiles;
		this.getBest = true;
	}
	
	public void run()
	{
		super.m = new Metrics(super.sc.getSourceFileRepository().getKnownCompilationUnits());	
		super.refactoringInfo = new ArrayList<String>();
		FitnessFunction ff = new FitnessFunction(super.m, super.c.getConfiguration());
		ArrayList<int[]> results = new ArrayList<int[]>(this.iterations);

		float benchmark = 0;
		float current = benchmark, best = benchmark;
		float newScore;
		float currentTemperature = this.temperature;
		int bestIteration = 1; 
		int[] position = new int[3];
		boolean findNeighbour;
		int counter;
		boolean progress = true;

		String runInfo = String.format("Search: Simulated Annealing\r\nIterations: %d\r\nStarting Temperature: %f", this.iterations, this.temperature);
		super.outputSearchInfo(super.resultsPath, runInfo);
		super.outputMetrics(benchmark, true, true, super.resultsPath);
		System.out.printf("\r\n\r\nRefactoring...");
		long timeTaken, startTime = System.currentTimeMillis();
		double time;

		// Applies refactorings by finding neighbouring refactorings at random points. These are
		// then compared with the current score and the improved options are chosen if applicable.
		for (int i = 1; i <= this.iterations; i++)
		{
			findNeighbour = true;
			counter = 0;
			
			while (findNeighbour)
			{
				// In this case search will act more traditional and
				// will only check against one neighbour each iteration.
				if (!this.alwaysMove)
					findNeighbour = false;
				
				position = super.randomRefactoring(super.c.getRefactorings());

				// If a random element could be found.
				if ((position[1] != -1) && (position[2] != -1))
				{
					super.c.getRefactorings().get(position[0]).transform(super.c.getRefactorings().get(position[0])
							                                  .analyze(i, position[1], position[2]));
					newScore = ff.calculateNormalisedScore(super.m);

					if (newScore > current)
					{
						super.refactoringInfo.add(super.c.getRefactorings().get(position[0]).getRefactoringInfo());
						System.out.printf("\r\n%s", super.c.getRefactorings().get(position[0]).getRefactoringInfo());
						current = newScore;
						findNeighbour = false;

						if (newScore > best)
						{
							best = newScore;
							bestIteration = i;
						}
						
						if (this.getBest)
							results.add(position);
					}
					else
					{
						float probability;
						
						// if current score is 0 do not accept negative change.
						if (current == 0)
							probability = 0.0f;
						// Probability of accepting negative move = exponential((-)percentage difference/current temperature).
						// Exponential of a negative value is confined to the range 0 -> 1 as the negative value approaches 0.
						else
						{
							float percentageChange = ((newScore - current) / Math.abs(current)) * 100;
							probability = (float) Math.exp(percentageChange / currentTemperature);
						}

						if (probability > Math.random())
						{
							super.refactoringInfo.add(super.c.getRefactorings().get(position[0]).getRefactoringInfo());
							System.out.printf("\r\n%s", super.c.getRefactorings().get(position[0]).getRefactoringInfo());
							current = newScore;
							findNeighbour = false;
							
							if (this.getBest)
								results.add(position);
						}
						else
						{
							super.c.getRefactorings().get(position[0]).transform(super.c.getRefactorings().get(position[0]).analyzeReverse());
							
							if (this.alwaysMove)
							{
								if (counter == 0)
									System.out.printf("\r\nNeighbours: %d", counter + 1);
								else
									System.out.printf(", %d", counter + 1);
							}
							
							counter++;
							
							if (counter == this.breakout)
							{
								System.out.printf("\r\n  There are no refactorings available - search terminating.");
								this.iterations = i - 1;
								progress = false;
								findNeighbour = false;
							}
						}
					}
				}			
				else
				{
					System.out.printf("\r\n  There are no refactorings available - search terminating.");
					this.iterations = i - 1;
					progress = false;
					findNeighbour = false;
				}	
			}

			if ((i % 25 == 0) && (progress))
			{
				timeTaken = System.currentTimeMillis() - startTime;
				time = timeTaken / 1000.0;
				int percent = (int) ((float) i / this.iterations*100);
				System.out.printf("\r\n  Search has taken %.2fs so far (%d%% complete)", time, percent);
			}
			
			double step = (this.iterations - i) / (double) this.iterations;
			currentTemperature = (float) (this.temperature * step * step);
		}

		// Output time taken to console and refactoring information to results file.
		timeTaken = System.currentTimeMillis() - startTime;
		time = timeTaken / 1000.0;
		System.out.printf("\r\nTime taken to refactor: %.2fs", time);

		int lastI = 0, colon;
		String sub;
		
		if (super.refactoringInfo.size() > 0)
		{
			colon = super.refactoringInfo.get(super.refactoringInfo.size() - 1).indexOf(':');
			sub = super.refactoringInfo.get(super.refactoringInfo.size() - 1).substring(10, colon);
			lastI = Integer.parseInt(sub);
		}
		
		if ((this.getBest) && (bestIteration != this.iterations) && (bestIteration != lastI))
		{			
			// Output the lowest best value measured during the search and at what iteration it was acquired.
			System.out.printf("\r\n\r\nBest score acquired was %.2f at iteration %d", best, bestIteration);
			System.out.printf("\r\nScore has improved overall by %.2f", best - benchmark);
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
			
			results.trimToSize();
			
			for (int i = 0; i < results.size(); i++)
			{
				super.c.getRefactorings().get(results.get(i)[0]).transform(super.c.getRefactorings().get(results.get(i)[0])
					   .analyze(-1, results.get(i)[1], results.get(i)[2]));
			}
			
			for (int i = super.refactoringInfo.size() - 1; i > 0; i--)
			{
				colon = super.refactoringInfo.get(i).indexOf(':');
				sub = super.refactoringInfo.get(i).substring(10, colon);
				lastI = Integer.parseInt(sub);
				
				if (lastI != bestIteration)
					super.refactoringInfo.remove(i);
				else
					break;
			}
			
			// Output the final metric values to the results file.
			super.outputRefactoringInfo(super.resultsPath, time, best - benchmark);
			super.m.setUnits(super.sc.getSourceFileRepository().getKnownCompilationUnits());
			super.outputMetrics(best, false, true, super.resultsPath);
			System.out.printf("\r\n");
		}
		else
		{
			super.outputRefactoringInfo(super.resultsPath, time, current - benchmark);
			super.outputMetrics(current, false, true, super.resultsPath);
			System.out.printf("\r\n\r\nScore has improved overall by %.2f", current - benchmark);
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