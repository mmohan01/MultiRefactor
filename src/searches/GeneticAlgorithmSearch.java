package searches;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import multirefactor.Configuration;
import multirefactor.FitnessFunction;
import multirefactor.RefactoringSequence;
import recoder.CrossReferenceServiceConfiguration;
import recoder.ParserException;
import recoder.io.PropertyNames;
import recoder.java.CompilationUnit;
import refactorings.Refactoring;

public abstract class GeneticAlgorithmSearch extends Search
{			
	public GeneticAlgorithmSearch(CrossReferenceServiceConfiguration sc, Configuration c) 
	{
		super(sc, c);
	}
	
	public GeneticAlgorithmSearch(CrossReferenceServiceConfiguration sc) 
	{
		super(sc);
	}

	public abstract void setInitialRefactoringRange(int refactoringRange);
	
	// Checks if the priority objective is being used in the search and if so, 
	// stores the priority classes and, if relevant, non priority classes in 
	// the fitness function object. Also checks if the element recentness 
	// objective is used and stores the previous versions of the code if so.
	protected static void setAdditionalInfo(FitnessFunction ff, Configuration c)
	{
		// If priority objective is being used.
		if (c.getPriorityClasses() != null)
			ff.setPriorityClasses(c.getPriorityClasses());

		// If priority objective is being used and there are also non priority classes.
		if (c.getNonPriorityClasses() != null)
			ff.setNonPriorityClasses(c.getNonPriorityClasses());
		
		// If element recentness objective is being used.
		if (c.getPreviousUnits() != null)
			ff.setPreviousUnits(c.getPreviousUnits());
	}
	
	// Avoids creating a new metric object by instead resetting its components with setter methods.
	// This allows information to be stored in the metrics object for calculation of the element 
	// recentness objective and updated as the search goes along, without losing the updated information.
	protected void resetMetrics(List<CompilationUnit> units, ArrayList<String> affectedClasses, HashMap<String, Integer> elementDiversity)
	{
		super.m.setUnits(units);
		super.m.setAffectedClasses(affectedClasses);
		super.m.setElementDiversity(elementDiversity);
	}
	 	
	// Output search information to results file.
	// Can be used for a population of solutions to generate separate results files.
	protected void outputSearchInfo(String pathName, int solution, String runInfo)
	{
		// Create a location for the results output.
		String runName = String.format("%sresultsSolution%d.txt", pathName, solution);
		File dir = new File(pathName);
		if (!dir.exists()) 
			dir.mkdirs();

		try 
		{
			BufferedWriter bw = new BufferedWriter(new FileWriter(runName, false));
			bw.write(String.format("======== Search Information ========"));
			bw.write(String.format("\r\n%s", runInfo));
			bw.close();
		}
		catch (IOException e) 
		{
			System.out.println("\r\nEXCEPTION: Cannot export results to text file.");
			System.exit(1);
		}
	}
		
	// Applies random refactorings to create a solution in the initial population.
	protected RefactoringSequence createInitialSolution(int initialRefactoringRange, ArrayList<Refactoring> refactorings, int solution)
	{
		int refactoringAmount = ((int)(Math.random() * initialRefactoringRange)) + 1;
		ArrayList<Integer> posSequence = new ArrayList<Integer>(refactoringAmount);
		ArrayList<Integer> refSequence = new ArrayList<Integer>(refactoringAmount);
		ArrayList<String> nameSequence = new ArrayList<String>(refactoringAmount);
		ArrayList<String> refactoringInfo = new ArrayList<String>(refactoringAmount);
		ArrayList<String> affectedClasses = new ArrayList<String>(refactoringAmount);
		HashMap<String, Integer> elementDiversity = new HashMap<String, Integer>();
		System.out.printf("\r\n Solution %d", solution);
		
		for (int j = 0; j < refactoringAmount; j++)
		{				
			int[] result = super.randomRefactoring(refactorings);

			if (result[0] == -1)
			{
				System.out.printf("\r\n    There are no refactorings available for the rest of the sequence.");
				j = refactoringAmount;
			}
			else
			{
				nameSequence.add(super.sc.getSourceFileRepository().getKnownCompilationUnits().get(result[1]).getName());
				refactorings.get(result[0]).transform(refactorings.get(result[0]).analyze((j + 1), result[1], result[2]));
				refactoringInfo.add(refactorings.get(result[0]).getRefactoringInfo());
				affectedClasses.addAll(refactorings.get(result[0]).getAffectedClasses());
				elementDiversity = addElement(elementDiversity, refactorings.get(result[0]).getAffectedElement());
				refSequence.add(result[0]);
				posSequence.add(result[2]);
			}
		}

		// Trim the lists to the correct size.
		refSequence.trimToSize();
		posSequence.trimToSize();
		nameSequence.trimToSize();
		refactoringInfo.trimToSize();
		affectedClasses.trimToSize();

		// Create refactoring sequence.
		return new RefactoringSequence(refSequence, posSequence, nameSequence, refactoringInfo, affectedClasses, elementDiversity);
	}
	
	protected int unitPosition(String name)
	{
		int unit = -1;

		for (int i = 0; i < this.sc.getSourceFileRepository().getKnownCompilationUnits().size(); i++)
		{
			int index = 5;
			
			if (this.sc.getSourceFileRepository().getKnownCompilationUnits().get(i).getName().indexOf("MultiRefactor\\") != 0)
				index = this.sc.getSourceFileRepository().getKnownCompilationUnits().get(i).getName().indexOf("MultiRefactor\\") + 14;
			
			String unitName = this.sc.getSourceFileRepository().getKnownCompilationUnits().get(i).getName().substring(index);

			if (name.endsWith(unitName))
			{
				unit = i;
				break;
			}
		}

		return unit;
	}
	
	// Method uses single-point crossover. Segments of each sequence are switched to create
	// a child solution using the cut points supplied. After this the refactorings are applied
	// for the child and any inapplicable refactorings are removed from the new sequence.
 	protected RefactoringSequence generateChild(RefactoringSequence p1, RefactoringSequence p2, int cutPoint1, int cutPoint2, int childNumber, 
									            ArrayList<Refactoring> refactoringList)
	{		
		int size = cutPoint1 + (p2.getRefactorings().size() - cutPoint2);
		ArrayList<Integer> refactorings = new ArrayList<Integer>(size);
		ArrayList<Integer> positions = new ArrayList<Integer>(size);
		ArrayList<String> names = new ArrayList<String>(size);
		ArrayList<String> refactoringInfo = new ArrayList<String>(size);
		ArrayList<String> affectedClasses = new ArrayList<String>(size);
		HashMap<String, Integer> elementDiversity = new HashMap<String, Integer>();
		int unitPosition, elementPosition, i2; 
		int iteration = cutPoint1 + 1;

		for (int i = 0; i < size; i++)
		{				
			// The first sequence in the solution will be applicable
			// so refactorings can be applied without checking.
			if (i < cutPoint1)
			{
				unitPosition = unitPosition(p1.getNames().get(i));
				refactoringList.get(p1.getRefactorings().get(i)).transform(refactoringList.get(p1.getRefactorings().get(i))
						       .analyze((i + 1), unitPosition, p1.getPositions().get(i)));
				refactoringInfo.add(p1.getRefactoringInfo().get(i));
				affectedClasses.add(p1.getAffectedClasses().get(i));
				elementDiversity = addElement(elementDiversity, refactoringList.get(p1.getRefactorings().get(i)).getAffectedElement());
				refactorings.add(p1.getRefactorings().get(i));
				positions.add(p1.getPositions().get(i));	
				names.add(p1.getNames().get(i));	
			}
			// For the second sequence, a check will have 
			// to be made for each contiguous refactoring.
			else
			{		
				elementPosition = -1;
				i2 = cutPoint2 + (i - cutPoint1);
				unitPosition = unitPosition(p2.getNames().get(i2));

				// Checks for the relevant program element by comparing the names of each element in the class
				//  with the desired element name and returning the position if it can be found and refactored.
				if (unitPosition != -1)
					elementPosition = refactoringList.get(p2.getRefactorings().get(i2)).checkElements(unitPosition, p2.getRefactoringInfo().get(i2));

				// If the element exists and can be refactored.
				if (elementPosition != -1)
				{
					refactoringList.get(p2.getRefactorings().get(i2)).transform(refactoringList.get(p2.getRefactorings().get(i2))
							       .analyze(iteration, unitPosition, elementPosition));
					refactoringInfo.add(refactoringList.get(p2.getRefactorings().get(i2)).getRefactoringInfo());
					affectedClasses.add(p2.getAffectedClasses().get(i2));
					elementDiversity = addElement(elementDiversity, refactoringList.get(p2.getRefactorings().get(i2)).getAffectedElement());
					refactorings.add(p2.getRefactorings().get(i2));
					positions.add(elementPosition);
					names.add(p2.getNames().get(i2));					
					iteration++;
				}
				else
					System.out.printf("\r\n    Refactoring %d N/A at child %d", i + 1, childNumber);
			}
		}
		
		// Trim the lists to the correct size.
		refactorings.trimToSize();
		positions.trimToSize();
		names.trimToSize();
		refactoringInfo.trimToSize();
		affectedClasses.trimToSize();

		// Create refactoring sequence.
		return new RefactoringSequence(refactorings, positions, names, refactoringInfo, affectedClasses, elementDiversity);
	}
	 	
	
	// Outputs the metric values for a multi-objective solution.
	protected void outputMetrics(float[] scores, boolean initial, boolean log, boolean toprank, int solution, String pathName)
	{
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

			// Outputs the fitness function values for the project to a text file.
			for (int i = 0; i < scores.length; i++)
				bw.append(String.format("\r\nFitness function %d score: %f", (i + 1), scores[i]));
			
			if (!(initial) && (toprank))
				bw.append(String.format("\r\n\r\nThis is the ideal solution in the top rank of solutions"));

			bw.close();
		}
		catch (IOException e) 
		{
			System.out.println("\r\nEXCEPTION: Cannot export results to text file.");
			System.exit(1);
		}

		if (log)
		{
			// Outputs the fitness function values for the project to the console for immediate feedback.
			for (int i = 0; i < scores.length; i++)
				System.out.printf("\r\n\r\nFitness function %d score: %.2f", (i + 1), scores[i]);
		}
	}
				
	// Applies a random refactoring to the end of the refactoring sequence passed in.
	// If the refactoring is not applicable it will keep trying until an applicable refactoring
	// is found or it runs out of possibilities. In this case the original sequence is returned.
	protected RefactoringSequence mutation(RefactoringSequence p, ArrayList<Refactoring> refactorings, Configuration c, String[] sourceFiles, 
			                               FitnessFunction[] ff)
	{			
		// Initialise the program model to the original state.
		refactorings = resetModel(refactorings, c, sourceFiles);
		RefactoringSequence solution;
				
		for (int i = 0; i < p.getRefactorings().size(); i++)
		{
			int unitPosition = unitPosition(p.getNames().get(i));
			refactorings.get(p.getRefactorings().get(i)).transform(refactorings.get(p.getRefactorings().get(i))
					    .analyze((i + 1), unitPosition, p.getPositions().get(i)));
		}
		
		int[] result = super.randomRefactoring(refactorings);
		
		// Applies refactoring to model and adds it to the sequence.
		if (result[0] != -1)
		{
			solution = applyRefactoring(p, result, refactorings);
			
			// Calculate fitness up front so current model isn't needed at a later point.
			resetMetrics(super.sc.getSourceFileRepository().getKnownCompilationUnits(), solution.getAffectedClasses(), 
	                	 solution.getElementDiversity());
			float finalScore[] = new float[ff.length];

			for (int j = 0; j < ff.length; j++)
				finalScore[j] = ff[j].calculateNormalisedScore(super.m);
			solution.setMOFitness(finalScore);
		}
		else
		{
			System.out.printf("\r\n    Mutation N/A");
			solution = p;
		}
		
		return solution;
	}

	// Reset the program model in a multi-objective solution to the initial input so it can be reused.
	protected ArrayList<Refactoring> resetModel(ArrayList<Refactoring> refactorings, Configuration c, String[] sourceFiles)
	{
		// Save the input path and then overwrite the program model using the constructor.
		// Recreate relevant refactoring objects to ensure old program model is no longer referenced.
		String inputPath = super.sc.getProjectSettings().getProperty(PropertyNames.INPUT_PATH);
		super.sc = new CrossReferenceServiceConfiguration();
		refactorings = c.resetRefactorings(super.sc, refactorings, false);

		try 
		{
			// Read the original input.			
			super.sc.getSourceFileRepository().getCompilationUnitsFromFiles(sourceFiles);
		}
		catch (ParserException e) 
		{
			System.out.println("\r\nEXCEPTION: Cannot read input.");
			System.exit(1);
		}

		// Set up initial properties of service configuration.
		super.sc.getProjectSettings().setProperty(PropertyNames.INPUT_PATH, inputPath);
		super.sc.getProjectSettings().ensureSystemClassesAreInPath();
		
		return refactorings;
	}
	
	// Makes a fast non-domination sort of the specified individuals. The method returns the different
	// domination fronts in ascending order by their rank and sets their rank value.
	protected ArrayList<ArrayList<RefactoringSequence>> fastNonDominatedSort(ArrayList<RefactoringSequence> population, int populationSize, int length) 
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
				if (isDominant(s1, s2, length)) 
					dominatedSolutions.get(s1).add(s2);
				else if (isDominant(s2, s1, length)) 
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
		while ((dominationFronts.size() == i) && (amount < populationSize))
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
	
	// Method finds an "ideal" solution from the top rank by finding the best objective values across
	// all the solutions in the top rank and then finding a distance vector for each solution that indicates
	// how far away the objectives are from the ideal values. The worst distances for each solution
	// are compared and the solution with the lowest worst distance is considered the ideal solution.
	protected int findTopSolution(ArrayList<RefactoringSequence> topRank, int length)
	{
		float[] idealPoint = new float[length];
		float[] distanceVector = new float[length];
		float[] maxDistances = new float[topRank.size()];
		float bestDistance;
		int topSolution = 0;

		for (int i = 0; i < length; i++)
			idealPoint[i] = topRank.get(0).getMOFitness()[i];
					
		for (RefactoringSequence s : topRank)
			for (int i = 0; i < length; i++) 
				if (s.getMOFitness()[i] > idealPoint[i])
					idealPoint[i] = s.getMOFitness()[i];
		
		for (int i = 0; i < topRank.size(); i++) 
		{
			for (int j = 0; j < length; j++) 
			{
				distanceVector[j] = idealPoint[j] - topRank.get(i).getMOFitness()[j];
				
				if (j == 0)
					maxDistances[i] = distanceVector[j];
				else if (distanceVector[j] > maxDistances[i])
					maxDistances[i] = distanceVector[j];
			}
		}
		
		bestDistance = maxDistances[0];
		
		for (int i = 1; i < topRank.size(); i++) 
		{			
			if (maxDistances[i] < bestDistance)
			{
				bestDistance = maxDistances[i];
				topSolution = i;
			}
		}
			
		return topSolution;
	}
	
	protected int findTopSolution(ArrayList<RefactoringSequence> topRank)
	{
		float bestQualityValue = 0.0f;
		int topSolution = 0;

		for (int i = 0; i < topRank.size(); i++) 
		{			
			if (topRank.get(i).getMOFitness()[0] > bestQualityValue)
			{
				bestQualityValue = topRank.get(i).getMOFitness()[0];
				topSolution = i;
			}
		}
		
		return topSolution;
	}

	// Output refactoring information to results file for a multi-objective solution.
	protected void outputRefactoringInfo(String pathName, double time, int solution, ArrayList<String> refactoringInfo)
	{
		// Create a location for the results output.
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
			System.out.println("\r\nEXCEPTION: Cannot export results to text file.");
			System.exit(1);
		}
	}

	
	// Applies a specified refactoring to the end of the refactoring sequence passed in.
 	protected RefactoringSequence applyRefactoring(RefactoringSequence p, int[] result, ArrayList<Refactoring> refactorings)
 	{						
 		// Applies refactoring to model and adds it to the sequence.
 		ArrayList<Integer> refSequence = new ArrayList<Integer>(p.getRefactorings().size() + 1);
 		refSequence = p.getRefactorings();
 		ArrayList<Integer> posSequence = new ArrayList<Integer>(p.getPositions().size() + 1);
 		posSequence = p.getPositions();
 		ArrayList<String> nameSequence = new ArrayList<String>(p.getNames().size() + 1);
 		nameSequence = p.getNames();
 		ArrayList<String> refactoringInfo = new ArrayList<String>(p.getRefactoringInfo().size() + 1);
 		refactoringInfo = p.getRefactoringInfo();
 		ArrayList<String> affectedClasses = new ArrayList<String>(p.getAffectedClasses().size() + 1);
 		affectedClasses = p.getAffectedClasses();
 		HashMap<String, Integer> elementDiversity = new HashMap<String, Integer>();
 		elementDiversity = p.getElementDiversity();

 		refSequence.add(result[0]);
 		posSequence.add(result[2]);
 		nameSequence.add(super.sc.getSourceFileRepository().getKnownCompilationUnits().get(result[1]).getName());
 		refactorings.get(result[0]).transform(refactorings.get(result[0]).analyze(refSequence.size(), result[1], result[2]));
 		refactoringInfo.add(refactorings.get(result[0]).getRefactoringInfo());
 		affectedClasses.addAll(refactorings.get(result[0]).getAffectedClasses());
 		elementDiversity = addElement(elementDiversity, refactorings.get(result[0]).getAffectedElement());
 		
 		p.setRefactorings(refSequence);
 		p.setPositions(posSequence);
 		p.setNames(nameSequence);
 		p.setRefactoringInfo(refactoringInfo); 		
 		p.setAffectedClasses(affectedClasses);
 		p.setElementDiversity(elementDiversity);
 		return p;
 	}
			
	
 	// Adds an element to the hashmap. If the element already exists the value associated
 	// with it is incremented, otherwise it is added and the value is set to one.
 	private HashMap<String, Integer> addElement(HashMap<String, Integer> elementDiversity, String affectedElement)
 	{
 		if (elementDiversity.containsKey(affectedElement))
 			elementDiversity.put(affectedElement, elementDiversity.get(affectedElement) + 1);
 		else
 			elementDiversity.put(affectedElement, 1);

 		return elementDiversity;
 	}
 		
	// Returns a boolean to represent whether the first solution parameter dominates the second.
	// The solution is dominant if no individual fitness function values are worse and at least one is better.
	private boolean isDominant(RefactoringSequence s1, RefactoringSequence s2, int length)
	{
		boolean better = false;
		float value1, value2;

		for (int i = 0; i < length; i++) 
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
	
	// This inner class allows sorting by fitness so that the more fit solutions are at the front of the list.
	protected class FitnessComparator implements Comparator<RefactoringSequence> 
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
}