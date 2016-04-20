package searches;

import java.util.ArrayList;

import recoder.CrossReferenceServiceConfiguration;
import recoder.ParserException;
import recoder.io.PropertyNames;
import refactory.Configuration;
import refactory.FitnessFunction;
import refactory.Metrics;

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
		Metrics m = new Metrics(super.sc.getSourceFileRepository().getKnownCompilationUnits());		
		FitnessFunction ff = new FitnessFunction();
		super.refactoringInfo = new ArrayList<String>();
		ArrayList<Integer> refactorings = new ArrayList<Integer>(this.iterations);
		ArrayList<int[]> positions = new ArrayList<int[]>(this.iterations);

		float benchmark = ff.calculateScore(m, super.c.getConfiguration());
		float current = benchmark, best = benchmark;
		float newScore;
		float currentTemperature = this.temperature;
		int bestIteration = 1; 
		int r = -1;
		int[] position = new int[2];
		boolean findNeighbour;
		int counter;
		boolean progress = true;

		String runInfo = String.format("Search: Simulated Annealing\r\nIterations: %d\r\nStarting Temperature: %f", this.iterations, this.temperature);
		super.outputSearchInfo(super.resultsPath, runInfo);
		super.outputMetrics(benchmark, m, true, true, super.resultsPath);
		System.out.printf("\n\nRefactoring...");
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
				
				// Find element to refactor. A random element will be chosen.
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

				// If a random element could be found.
				if ((position[0] != -1) && (position[1] != -1))
				{
					super.c.getRefactorings().get(r).transform(super.c.getRefactorings().get(r).analyze(i, position[0], position[1]));
					newScore = ff.calculateScore(m, super.c.getConfiguration());

					if (newScore > current)
					{
						super.refactoringInfo.add(super.c.getRefactorings().get(r).getRefactoringInfo());
						System.out.printf("\n%s", super.c.getRefactorings().get(r).getRefactoringInfo());
						current = newScore;
						findNeighbour = false;

						if (newScore > best)
						{
							best = newScore;
							bestIteration = i;
						}
						
						if (this.getBest)
						{
							refactorings.add(r);
							positions.add(position);
						}
					}
					else
					{
						// Probability of accepting negative move = exponential((-)difference/current temperature).
						// Exponential of a negative value is confined to the range 0 -> 1 as the negative value approaches 0. 
						float probability = (float) Math.exp((newScore - current) / currentTemperature);

						if (probability > Math.random())
						{
							super.refactoringInfo.add(super.c.getRefactorings().get(r).getRefactoringInfo());
							System.out.printf("\n%s", super.c.getRefactorings().get(r).getRefactoringInfo());
							current = newScore;
							findNeighbour = false;
							
							if (this.getBest)
							{
								refactorings.add(r);
								positions.add(position);
							}
						}
						else
						{
							super.c.getRefactorings().get(r).transform(super.c.getRefactorings().get(r).analyzeReverse());
							
							if (alwaysMove)
							{
								if (counter == 0)
									System.out.printf("\nNeighbours: %d", counter + 1);
								else
									System.out.printf(", %d", counter + 1);
							}
							
							counter++;
							
							if (counter == this.breakout)
							{
								System.out.printf("\n  There are no refactorings available - search terminating.");
								this.iterations = i - 1;
								progress = false;
								findNeighbour = false;
							}
						}
					}
				}			
				else
				{
					System.out.printf("\n  There are no refactorings available - search terminating.");
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
				System.out.printf("\n  Search has taken %.2fs so far (%d%% complete)", time, percent);
			}
			
			double step = (this.iterations - i) / (double) this.iterations;
			currentTemperature = (float) (this.temperature * step * step);
		}

		// Output time taken to console and refactoring information to results file.
		timeTaken = System.currentTimeMillis() - startTime;
		time = timeTaken / 1000.0;
		System.out.printf("\nTime taken to refactor: %.2fs", time);

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
			System.out.printf("\n\nBest score acquired was %.2f at iteration %d", best, bestIteration);
			System.out.printf("\nScore has improved overall by %.2f", best - benchmark);
			System.out.printf("\nReconstructing Best Iteration...");
			
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
				System.out.println("\nEXCEPTION: Cannot read input.");
				System.exit(1);
			}

			// Set up initial properties of service configuration.
			super.sc.getProjectSettings().setProperty(PropertyNames.INPUT_PATH, inputPath);
			super.sc.getProjectSettings().setProperty(PropertyNames.OUTPUT_PATH, outputPath);
			super.sc.getProjectSettings().ensureSystemClassesAreInPath();
			
			refactorings.trimToSize();
			positions.trimToSize();
			
			for (int i = 0; i < refactorings.size(); i++)
			{
				super.c.getRefactorings().get(refactorings.get(i)).transform(super.c.getRefactorings().get(refactorings.get(i))
					   .analyze(-1, positions.get(i)[0], positions.get(i)[1]));
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
			m = new Metrics(super.sc.getSourceFileRepository().getKnownCompilationUnits());	
			super.outputMetrics(best, m, false, true, super.resultsPath);
			System.out.printf("\n");
		}
		else
		{
			super.outputRefactoringInfo(super.resultsPath, time, current - benchmark);
			super.outputMetrics(current, m, false, true, super.resultsPath);
			System.out.printf("\n\nScore has improved overall by %.2f", current - benchmark);
		}
		
		// Apply refactorings from the AST to the source code.
		System.out.printf("\nApplying Transformations...");   	
		super.print(super.sc.getSourceFileRepository());

		timeTaken = System.currentTimeMillis() - startTime;
		time = timeTaken / 1000.0;
		System.out.printf("\nOverall time taken for search: %.2fs", time);
		System.out.printf("\n-------------------------------------");
	}
}