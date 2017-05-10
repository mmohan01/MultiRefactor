package searches;

import java.util.ArrayList;

import multirefactor.Configuration;
import multirefactor.FitnessFunction;
import multirefactor.Metrics;
import multirefactor.RefactoringSequence;
import recoder.CrossReferenceServiceConfiguration;
import recoder.io.PropertyNames;
import refactorings.Refactoring;

public class ManyObjectiveSearch extends GeneticAlgorithmSearch
{
	private Configuration[] c;
	private ArrayList<Refactoring> refactorings;
	private String[] sourceFiles;
	private String outputPath;
	private FitnessFunction[] ff;
	private ArrayList<float[]> referencePoints;
	private int referencePointAmount;
	private int divisions = 1;
		
	private int generations;
	private int populationSize;
	private float crossoverProbability;
	private float mutationProbability;
	private int initialRefactoringRange = 50;
	private boolean topSolutionFlag = true;

	public ManyObjectiveSearch(CrossReferenceServiceConfiguration sc, Configuration[] c, ArrayList <Refactoring> refactorings, String[] sourceFiles) 
	{
		super(sc);
		this.c = c;
		this.ff = new FitnessFunction[this.c.length];
		this.refactorings = refactorings;
		this.sourceFiles = sourceFiles;
		this.generations = 100;
		this.populationSize = 50;
		this.crossoverProbability = 0.2f;
		this.mutationProbability = 0.8f;
	}

	public ManyObjectiveSearch(CrossReferenceServiceConfiguration sc, Configuration[] c, ArrayList <Refactoring> refactorings, String[] sourceFiles,
								int generations, int populationSize, float crossoverProbability, float mutationProbability)
	{
		super(sc);
		this.c = c;
		this.ff = new FitnessFunction[this.c.length];
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
					
		String runInfo = String.format("Search: Many-Objective Genetic Algorithm\r\nGenerations: %d\r\nPopulation Size: %d" +
									   "\r\nCrossover Probability: %f\r\nMutation Probability: %f",
									   this.generations, this.populationSize, this.crossoverProbability, this.mutationProbability);
		
		for (int i = 1; i <= populationSize; i++)
			super.outputSearchInfo(super.resultsPath, i, runInfo);
	
		long timeTaken, startTime = System.currentTimeMillis();
		double time;
		int unitPosition;

		System.out.printf("\r\n\r\nCreating Initial Population...");
		ArrayList<RefactoringSequence> population = new ArrayList<RefactoringSequence>(this.populationSize);
		ArrayList<RefactoringSequence> newGeneration = new ArrayList<RefactoringSequence>();
		
		// Work out the amount of reference points in a hyperplane that best corresponds to the population size.	
		referencePointAmount = this.c.length;
		while (this.referencePointAmount < this.populationSize)
		{
			this.divisions++;
			this.referencePointAmount = numberOfReferencePoints(this.divisions);
		}
		
		this.referencePoints = generateReferencePoints();
		population = fitness(initialise());
		
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
				int randomS1, randomS2;
				RefactoringSequence[] parents = new RefactoringSequence[2];
				randomS1 = (int)(Math.random() * population.size());

				do 
					randomS2 = (int)(Math.random() * population.size());
				while (randomS2 == randomS1);

				parents[0] = population.get(randomS1);
				parents[1] = population.get(randomS2);
				newGeneration.addAll(crossover(parents[0], parents[1]));
			}
			while (Math.random() < this.crossoverProbability);
			
			// The amount of times if, at all, mutation is applied depends on the
			// mutation probability. This will mutate the children of the current generation.
			while (Math.random() < this.mutationProbability)
			{
				System.out.printf("\r\n  Mutation...");
				int randomChild = (int)(Math.random() * newGeneration.size());
				newGeneration.set(randomChild, super.mutation(newGeneration.get(randomChild), this.refactorings, this.c[0], this.sourceFiles, this.ff));
			}
			
			// The current population is measured and sorted accordingly.
			System.out.printf("\r\n  Fitness...");
			newGeneration.trimToSize();
			population.ensureCapacity(this.populationSize + newGeneration.size());
			population.addAll(newGeneration);
			population = new ArrayList<RefactoringSequence>(fitness(population));
			population.trimToSize();
		}
		
		newGeneration = null;
		timeTaken = System.currentTimeMillis() - startTime;
		time = timeTaken / 1000.0;
		
		System.out.printf("\r\n\r\nPrinting Population");
		
		// Find solutions from final population that are in the top rank.
		ArrayList<RefactoringSequence> topRank = new ArrayList<RefactoringSequence>();
		for (int i = 0; i < population.size(); i++)
			if (population.get(i).getRank() == 1)
				topRank.add(population.get(i));
		
		// Find the ideal solution from the top rank in the population.
		int topSolution = (topSolutionFlag) ? super.findTopSolution(topRank, this.c.length) : super.findTopSolution(topRank);

		for (int i = 0; i < population.size(); i++)
		{
			System.out.printf("\r\n  Solution %d", i + 1);
			this.refactorings = super.resetModel(this.refactorings, this.c[0], this.sourceFiles);
			
			// Reconstruct model so it can be printed.
			for (int j = 0; j < population.get(i).getRefactorings().size(); j++)
			{
				unitPosition = super.unitPosition(population.get(i).getNames().get(j));
				this.refactorings.get(population.get(i).getRefactorings().get(j))
				                 .transform(this.refactorings.get(population.get(i).getRefactorings().get(j))
						         .analyze((j + 1), unitPosition, population.get(i).getPositions().get(j)));
			}
			
			// Output information.
			super.outputRefactoringInfo(super.resultsPath, time, i + 1, population.get(i).getRefactoringInfo());
			boolean top = (i == topSolution) ? true : false;
			super.outputMetrics(population.get(i).getMOFitness(), false, false, top, i + 1, super.resultsPath);
			
			// Output refactored solution.
			String newOutputPath = this.outputPath;
			newOutputPath += (population.get(i).getRank() == 1) ? "TopRank/Solution" + (i + 1) + "/" : "Solution" + (i + 1) + "/";
			if (i == topSolution)
				newOutputPath = newOutputPath.substring(0, newOutputPath.length() - 1) + "-IdealSolution/";
			super.sc.getProjectSettings().setProperty(PropertyNames.OUTPUT_PATH, newOutputPath);
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
		super.m = new Metrics(super.sc.getSourceFileRepository().getKnownCompilationUnits());	
		float benchmark[] = new float[this.c.length];
		float finalScore[] = new float[this.c.length];
		
		for (int i = 0; i < this.c.length; i++)
		{
			this.ff[i] = new FitnessFunction(super.m, this.c[i].getConfiguration());
			super.setAdditionalInfo(this.ff[i], this.c[i]);
			benchmark[i] = 0.0f;
		}
		
		for (int i = 0; i < this.populationSize; i++)
		{
			// Reinitialise the program model to the original state.
			if (i > 0)
				this.refactorings = super.resetModel(this.refactorings, this.c[0], this.sourceFiles);

			super.outputMetrics(benchmark, true, false, false, i + 1, super.resultsPath);
			
			// Applies random refactorings to each solution to create an initial population.
			// The amount of refactorings applied in each case is chosen randomly within the range supplied.
			population.add(super.createInitialSolution(this.initialRefactoringRange, this.refactorings, i + 1));
			
			// Calculate fitness up front so current model isn't needed at a later point.
			super.resetMetrics(super.sc.getSourceFileRepository().getKnownCompilationUnits(), population.get(i).getAffectedClasses(), 
							   population.get(i).getElementDiversity());
			for (int j = 0; j < this.c.length; j++)
				finalScore[j] = this.ff[j].calculateNormalisedScore(super.m);
			population.get(i).setMOFitness(finalScore.clone());
		}
		
		return population;
	}
	
	// Reinitialises the program model before applying the crossover to generate a child solution.
	// This is done twice in order to generate the offspring expected from the crossover process.
	private ArrayList<RefactoringSequence> crossover(RefactoringSequence p1, RefactoringSequence p2)
	{						
		ArrayList<RefactoringSequence> children = new ArrayList<RefactoringSequence>(2);
		float finalScore[] = new float[this.c.length];
		
		// For each refactoring sequence passed in, a cut point is randomly chosen.
		// If the parent solution has only 1 refactoring, the cut point will be at the
		// end to avoid the possibility of producing an offspring with no refactorings.
		int cutPoint1 = 1;
		int cutPoint2 = 1;
		
		if ((p1.getRefactorings().size() != 1) && (Math.random() >= 0.5))
			cutPoint1 = p1.getRefactorings().size() - 1;

		// If the first parent solution has 2 refactorings, the cut point of the second solution will be
		// chosen at random between the 2 possibilities. Otherwise, it will be the same as cut point 1.
		if (p2.getRefactorings().size() != 1)
		{
			if (p1.getRefactorings().size() == 2)
				cutPoint2 = (Math.random() < 0.5) ? 1 : p2.getRefactorings().size() - 1;
			else 
				cutPoint2 = (cutPoint1 == 1) ? 1 : p2.getRefactorings().size() - 1;
		}
		
		// Initialise the program model to the original state and generate the first child solution.
		this.refactorings = super.resetModel(this.refactorings, this.c[0], this.sourceFiles);
		RefactoringSequence child1 = super.generateChild(p1, p2, cutPoint1, cutPoint2, 1, this.refactorings);
		
		// Calculate fitness up front so current model isn't needed at a later point.
		super.resetMetrics(super.sc.getSourceFileRepository().getKnownCompilationUnits(), child1.getAffectedClasses(), child1.getElementDiversity());	
		for (int j = 0; j < this.c.length; j++)
			finalScore[j] = this.ff[j].calculateNormalisedScore(super.m);
		child1.setMOFitness(finalScore.clone());
		children.add(child1);
		
		// Initialise the program model again and generate the second child solution.
		this.refactorings = super.resetModel(this.refactorings, this.c[0], this.sourceFiles);
		RefactoringSequence child2 = super.generateChild(p2, p1, cutPoint2, cutPoint1, 2, this.refactorings);

		// Calculate fitness up front so current model isn't needed at a later point.
		super.resetMetrics(super.sc.getSourceFileRepository().getKnownCompilationUnits(), child2.getAffectedClasses(), child2.getElementDiversity());	
		for (int j = 0; j < this.c.length; j++)
			finalScore[j] = this.ff[j].calculateNormalisedScore(super.m);
		child2.setMOFitness(finalScore.clone());
		children.add(child2);

		return children;
	}
		
	// Sorts the population by fitness depending on ranks of non dominated solutions.
	private ArrayList<RefactoringSequence> fitness(ArrayList<RefactoringSequence> population)
	{		
		ArrayList<ArrayList<RefactoringSequence>> dominationFronts = super.fastNonDominatedSort(population, this.populationSize, this.c.length);

		ArrayList<RefactoringSequence> sortedPopulation = new ArrayList<RefactoringSequence>(this.populationSize);
		int i = 0;
		
		while ((dominationFronts.size() > i) && ((sortedPopulation.size() + dominationFronts.get(i).size()) <= this.populationSize)) 
		{
			sortedPopulation.addAll(dominationFronts.get(i));
			i++;
		}
		
		if (sortedPopulation.size() != this.populationSize)
		{
			int remainingSolutions = this.populationSize - sortedPopulation.size();
			sortedPopulation.addAll(referencePointAssignment(sortedPopulation, dominationFronts.get(i), remainingSolutions));
		}

		sortedPopulation.trimToSize();
		return sortedPopulation;
	}


	// Return the amount of reference points that are generated with the relevant amount 
	// of objectives and the provided amount of reference points per axis of the hyperplane.
	// This is represented by n!/p!(n-p)! where p is "axisDivisions" and n is "axisDivisions" plus
	// the amount of objectives - 1 (the amount of dimensions of the simplex representing the hyperplane). 
	private int numberOfReferencePoints(int axisDivisions)
	{
		int numerator = this.c.length + axisDivisions - 1;
		int topValue = numerator;
		int bottomValue = numerator - axisDivisions;
		
		for (int i = numerator - 1; i > axisDivisions; i--)
			topValue *= i;
		
		for (int i = numerator - axisDivisions - 1; i > 1; i--)
			bottomValue *= i;
		
		return topValue / bottomValue;
	}
	
	// Accumulates the lists of reference points compiled 
	// from the recursive method and returns them as a single list.
	private ArrayList<float[]> generateReferencePoints() 
	{
		return generateReferencePointsRecursive(new float[this.c.length], this.divisions, this.divisions, 0);
	}

	// Finds the vector values of the set of reference points generated
	// across the hyperplane, with the reference points having equal stepsizes.
	// Based off the generate_recursive method of Tsung-Che Chiang 
	// (http://web.ntnu.edu.tw/~tcchiang/publications/nsga3cpp/nsga3cpp.htm).
	private ArrayList<float[]> generateReferencePointsRecursive(float[] point, int left, int total, int element) 
	{
		// Create new list so that only the relevant reference points are returned.
		ArrayList<float[]> referencePoints = new ArrayList<float[]>();
		
		// Compose and return reference point.
		if (element == (this.c.length - 1)) 
		{
			point[element] = (float) left / total;
			referencePoints.add(point.clone());
		} 
		else 
		{
			for (int i = 0; i <= left; i += 1) 
			{
				// Call recursive method to find relevant reference points.
				point[element] = (float) i / total;
				referencePoints.addAll(generateReferencePointsRecursive(point, left - i, total, element + 1));
			}
		}
		
		return referencePoints;
	}

	// Applies the reference point calculations for the current population and chooses the remaining solutions.
	private ArrayList<RefactoringSequence> referencePointAssignment(ArrayList<RefactoringSequence> sortedPopulation, 
																	ArrayList<RefactoringSequence> lastRank, int remainingSolutions) 
	{
		ArrayList<RefactoringSequence> remainingPopulation = new ArrayList<RefactoringSequence>(remainingSolutions);
		float[][] hyperplane = constructHyperplane(sortedPopulation, lastRank);
		
		// For each reference point, the list of solutions that are associated with it (i.e. closer to it than any of the
		// other reference points) are stored for both the chosen solutions and the solutions from the last relevant front.
		ArrayList<ArrayList<RefactoringSequence>> mainAssociatedSolutions = new ArrayList<ArrayList<RefactoringSequence>>(this.referencePointAmount);
		ArrayList<ArrayList<RefactoringSequence>> remainingAssociatedSolutions = new ArrayList<ArrayList<RefactoringSequence>>(this.referencePointAmount);
		int index, minimumNicheCount;
		float distance, smallestDistance;		
		
		// Initialise lists of solutions so that they can be added to when calculating the associated solutions. 
		for (int i = 0; i < this.referencePointAmount; i++) 
		{
			mainAssociatedSolutions.add(new ArrayList<RefactoringSequence>());
			remainingAssociatedSolutions.add(new ArrayList<RefactoringSequence>());
		}
		
		// For each solution already chosen, find the reference point that it is closest to. When the relevant
		// reference point is found, add that solution to the reference points list of associated solutions.
		for (int i = 0; i < sortedPopulation.size(); i++)
		{
			smallestDistance = calculateDistance(sortedPopulation.get(i), this.referencePoints.get(0), hyperplane);
			index = 0;
			
			for (int j = 1; j < this.referencePointAmount; j++)
			{
				distance = calculateDistance(sortedPopulation.get(i), this.referencePoints.get(j), hyperplane);
				
				if (distance < smallestDistance)
				{
					smallestDistance = distance;
					index = j;
				}
			}
			
			mainAssociatedSolutions.get(index).add(sortedPopulation.get(i));
		}
		
		// Do the same for the remaining rank of solutions 
		// from which the last set of solutions will be chosen.
		for (int i = 0; i < lastRank.size(); i++)
		{
			smallestDistance = calculateDistance(lastRank.get(i), this.referencePoints.get(0), hyperplane);
			index = 0;
			
			for (int j = 1; j < this.referencePointAmount; j++)
			{
				distance = calculateDistance(lastRank.get(i), this.referencePoints.get(j), hyperplane);
				
				if (distance < smallestDistance)
				{
					smallestDistance = distance;
					index = j;
				}
			}
			
			remainingAssociatedSolutions.get(index).add(lastRank.get(i));
		}
		
		// Choose the solutions from the last rank to add to the population.
		for (int i = 0; i < remainingSolutions; i++)
		{			
			// Remove any reference points that contain no solutions to choose 
			// from. Needs to be done each time a solution is chosen in case  
			// that reference point had no other solutions to choose from.
			for (int j = 0; j < remainingAssociatedSolutions.size(); j++)
			{
				if (remainingAssociatedSolutions.get(j).size() == 0)
				{
					mainAssociatedSolutions.remove(j);
					remainingAssociatedSolutions.remove(j);
					j--;
				}
			}
		
			minimumNicheCount = mainAssociatedSolutions.get(0).size();
			
			// Find the minimum niche count among the reference points for
			// the associated solutions from the chosen population. This needs
			// to be recalculated each time after another solution is chosen.			
			for (int j = 1; j < mainAssociatedSolutions.size(); j++)
				if (mainAssociatedSolutions.get(j).size() < minimumNicheCount)
					minimumNicheCount = mainAssociatedSolutions.get(j).size();

			// Looks through the reference points with the least associated solutions and if there is
			// an associated solution for the point among the last rank, it is added to the population.
			for (int j = 0; j < mainAssociatedSolutions.size(); j++)
			{
				// If there is a corresponding solution among the last rank.
				if (mainAssociatedSolutions.get(j).size() == minimumNicheCount)
				{
					// If there is more than 1 corresponding solutions to choose from.
					if (remainingAssociatedSolutions.get(j).size() > 1)
					{
						// If the niche count is 0, the solution with the 
						// closest distance to the reference point is chosen.
						if (minimumNicheCount == 0)
						{
							smallestDistance = calculateDistance(remainingAssociatedSolutions.get(j).get(0), this.referencePoints.get(j), hyperplane);
							index = 0;
							
							for (int k = 1; k < remainingAssociatedSolutions.get(j).size(); k++)
							{
								distance = calculateDistance(remainingAssociatedSolutions.get(j).get(k), this.referencePoints.get(j), hyperplane);

								if (distance < smallestDistance)
								{
									smallestDistance = distance;
									index = k;
								}
							}
						}
						// If the niche count is greater than 0, the 
						// solution is chosen at random from those available.
						else
							index = (int)(Math.random() * remainingAssociatedSolutions.get(j).size());
					}
					else
						index = 0;
					
					// Update the reference point lists of associated solutions.
					// Add the chosen solution to the list of chosen solutions from the last rank.
					mainAssociatedSolutions.get(j).add(remainingAssociatedSolutions.get(j).get(index));
					remainingPopulation.add(remainingAssociatedSolutions.get(j).get(index));
					remainingAssociatedSolutions.get(j).remove(index);
					break;
				}
			}
		}
		
		return remainingPopulation;
	}
	
	// Finds the ideal point (the best theoretical solution that could be generated from the best objective values
	// across all the solutions within the current population) and the intercept values of the objective axes and
	// the worst points on the axes (the point that represents the worst solution on that axis) to represent the 
	// hyperplane encapsulating the population and aid with normalisation of the objective values in each solution. 
	private float[][] constructHyperplane(ArrayList<RefactoringSequence> topRanks, ArrayList<RefactoringSequence> bottomRank)
	{
		ArrayList<RefactoringSequence> population = new ArrayList<RefactoringSequence>(topRanks);
		population.addAll(bottomRank);
		float[] idealPoint = new float[this.c.length];
		float[] intercepts = new float[this.c.length];
		RefactoringSequence[] extremePoints = new RefactoringSequence[this.c.length];
		float minimumASF;
		boolean duplicate = false;
		
		// Initialises the best and worst objective values by
		// passing in the values for the first solution.
		for (int i = 0; i < this.c.length; i++)
			idealPoint[i] = population.get(0).getMOFitness()[i];
					
		// Finds the best and worst possible points that could be 
		// theoretically present among the current population of solutions.
		for (int i = 1; i < population.size(); i++) 
			for (int j = 0; j < this.c.length; j++) 
				if (population.get(i).getMOFitness()[j] > idealPoint[j])
					idealPoint[j] = population.get(i).getMOFitness()[j];
		
		// Finds the solutions that represent the extreme points in the population.
		for (int i = 0; i < this.c.length; i++) 
		{
			minimumASF = achievementScalarizationFunction(population.get(0), i);
			extremePoints[i] = population.get(0);
			
			for (int j = 1; j < population.size(); j++) 
			{			
				float asf = achievementScalarizationFunction(population.get(j), i);
				
				if (asf < minimumASF) 
				{
					minimumASF = asf;
					extremePoints[i] = population.get(j);
				}
			}
		}
		
		// Check whether there are duplicate extreme points.
		for (int i = 0; !duplicate && i < extremePoints.length; i++) 
			for (int j = i + 1; !duplicate && j < extremePoints.length; j++)
				duplicate = (extremePoints[i] == extremePoints[j]);

		// Duplicates exist so the unique hyperplane cannot be constructed.
		// In this case, the worst objective values in each axis are used as the intercepts.
		if (duplicate)
		{
			for (int i = 0; i < this.c.length; i++) 
				intercepts[i] = extremePoints[i].getMOFitness()[i];
		}
		// Otherwise, the intercepts are found using gaussian elimination.
		else 
		{
			// Matrix A and vector b declared for guassian elimination.
			float[][] A = new float[this.c.length][this.c.length];
			float[] b = new float[this.c.length];
			
			for (int i = 0; i < this.c.length; i++)
				b[i] = 1.0f;

			// Find the equation of the hyperplane, where A and b
			// represent the linear equations that make up the equation.
			for (int i = 0; i < this.c.length; i++)
			{
				float[] aux = new float[this.c.length];

				for (int j = 0; j < this.c.length; j++)
					aux[j] = extremePoints[i].getMOFitness()[j];

				A[i] = aux;
			}

			// The equation of the hyperplane.
			float[] x = guassianElimination(A, b);

			// Find intercepts.
			for (int i = 0; i < this.c.length; i++)
				intercepts[i] = 1.0f / x[i];
		}

		return new float[][]{idealPoint, intercepts};
	}

	// Uses to find the extreme points along each objective axis as the extreme point will have the minimal ASF value.
	// Based off the ASF method of Tsung-Che Chiang (http://web.ntnu.edu.tw/~tcchiang/publications/nsga3cpp/nsga3cpp.htm).
	private float achievementScalarizationFunction(RefactoringSequence solution, int index) 
	{
		float weight = (index == 0) ? 1.0f : 0.000001f;
		float maxRatio = solution.getMOFitness()[0] / weight;

		for (int i = 1; i <  this.c.length; i++) 
		{
			weight = (index == i) ? 1.0f : 0.000001f;

			if (maxRatio < solution.getMOFitness()[i] / weight)
				maxRatio = solution.getMOFitness()[i] / weight;
		}

		return maxRatio;
	}

	// Given a NxN matrix A and a Nx1 vector b, gaussian elimination is used to generate a Nx1
	// vector x such that Ax = b. This vector can then be used to get the intercepts to the 
	// hyperplane on each axis. Based off the GaussianElimination method of Tsung-Che Chiang 
	// (http://web.ntnu.edu.tw/~tcchiang/publications/nsga3cpp/nsga3cpp.htm).
	private float[] guassianElimination(float[][] A, float[] b) 
	{		
		int N = A.length;
		float[] x = new float[N];
		
		for (int i = 0; i < N; i++)
			x[i] = 0.0f;
		
		float Ab[][] = new float[N][N + 1];
		
		for (int i = 0; i < N; i++)
		{
			float[] temp = new float[A[i].length + 1];
			for (int j = 0; j <= A[i].length; j++)
			{
				if (j == A[i].length)
					temp[j] = b[i];
				else
					temp[j] = A[i][j];
			}
			
			Ab[i] = temp;
		}
		
		for (int base = 0; base < N - 1; base++) 
		{
			for (int target = base + 1; target < N; target++) 
			{
				float ratio = Ab[target][base] / Ab[base][base];
				
				for (int term = 0; term < Ab[base].length; term++) 
					Ab[target][term] -= Ab[base][term] * ratio;
			}
		}

		for (int i = N - 1; i >= 0; i--) 
		{
			for (int known = i + 1; known < N; known++) 
				Ab[i][N] -= Ab[i][known] * x[known];
			
			x[i] = Ab[i][N] / Ab[i][i];
		}
		
		return x;
	}

	// Finds the distance between a solution and a corresponding reference point. This is found by calculating
	// the perpendicular distance between the vector value of the solution and the reference line that connects
	// the reference point to the ideal point. The reference point (R) represents the reference line and the 
	// solution point (s) is represented as the distance from the ideal point. To find the point on R closest 
	// to S (S.R/R.R)*R is used. The perpendicular distance vector is found by taking the closest point away
	// from the solution point. The magnitude of that vector will represent the perpendicular distance.
	private float calculateDistance(RefactoringSequence solution, float[] referencePoint, float[][] hyperplane)
	{
		float[] solutionPoint = new float[this.c.length];
		float[] closestPoint = new float[this.c.length];
		float[] distanceVector = new float[this.c.length];
		float sDotR = 0.0f, rDotR = 0.0f, distance = 0.0f;
		
		// Find the translation vector representing the solution within
		// the space where the ideal point is the origin and the vector
		// represents the ratio from the idea point to the worst point.
		for (int i = 0; i < this.c.length; i++)
		{
			if ((hyperplane[0][i] - hyperplane[1][i]) > 0.000001f)
				solutionPoint[i] = (hyperplane[0][i] - solution.getMOFitness()[i]) / (hyperplane[0][i] - hyperplane[1][i]);
			else
			{
				solutionPoint[i] = (hyperplane[0][i] - solution.getMOFitness()[i]) / 0.000001f;
			}
		}
		
		// Find the dot products of the solution and the
		// reference line and the reference line with itself.
		for (int i = 0; i < this.c.length; i++)
		{
			sDotR += (solutionPoint[i] * referencePoint[i]);
			rDotR += (referencePoint[i] * referencePoint[i]);
		}
		
		float scale = sDotR / rDotR;
		
		// The closest point represents the point on the
		// reference line that is closest to the solution point.
		for (int i = 0; i < this.c.length; i++)
			closestPoint[i] = scale * referencePoint[i];
		
		// Find the distance vector connecting the solution point 
		// and the point on the reference line closest to the solution.
		for (int i = 0; i < this.c.length; i++)
			distanceVector[i] = solutionPoint[i] - closestPoint[i];
				
		// Find the magnitude of the distance vector.
		for (int i = 0; i < this.c.length; i++)
			distance += (distanceVector[i] * distanceVector[i]);
		
		return (float) Math.sqrt(distance);
	}
			
	
	public void setConfigurations(Configuration[] c)
	{
		this.c = c;
	}
	
	public void setRefactorings(ArrayList<Refactoring> refactorings)
	{
		this.refactorings = refactorings;
	}
	
	public void setInitialRefactoringRange(int refactoringRange)
	{
		this.initialRefactoringRange = refactoringRange;
	}
	
	public void setTopSolutionFlag(boolean topSolutionFlag)
	{
		this.topSolutionFlag = topSolutionFlag;
	}
}