package searches;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import recoder.CrossReferenceServiceConfiguration;
import recoder.io.PropertyNames;
import recoder.io.SourceFileRepository;
import recoder.java.CompilationUnit;
import refactorings.Refactoring;
import refactory.Configuration;
import refactory.FitnessFunction;
import refactory.Metrics;

public abstract class Search 
{
	protected CrossReferenceServiceConfiguration sc;
	protected Configuration c;
	protected String resultsPath;
	protected ArrayList<String> refactoringInfo;

	public Search(CrossReferenceServiceConfiguration sc, Configuration c) 
	{
		this.sc = sc;
		this.c = c;
		String output = sc.getProjectSettings().getProperty(PropertyNames.OUTPUT_PATH);
		this.resultsPath = "./results/" + output.substring(output.lastIndexOf("/") + 1) + "/";
	}
	
	public abstract void run();
	
	protected void print(SourceFileRepository sfr)
	{
		List<CompilationUnit> list = sfr.getKnownCompilationUnits();

		for (CompilationUnit cu : list)
		{
			//cu.validateAll();
			//if(!sfr.isUpToDate(cu))
			//	System.out.println("\nClass changed");
			
			try 
			{
				sfr.print(cu);
			} 
			catch (IOException e) 
			{
				System.out.println("\nEXCEPTION: Cannot print changes to output.");
				System.exit(1);
			}
		}
	}
	
	protected int[] randomElement(Refactoring r)
	{
		int[] position = new int[2];
		position[0] = (int) (Math.random() * r.getServiceConfiguration().getSourceFileRepository().getKnownCompilationUnits().size());
			
		// Only counts the relevant program element.
		int amount = r.getAmount(position[0]);
		// Count the amount of available elements in the chosen class for refactoring.
		// If none are available find the next element up that is available.
		// If there are still no elements found look in the other direction and if there 
		// are no elements available at all return -1 so it can be handled in the search.
		if (amount == 0)
		{
			int[] temp = new int[2];
			position[1] = 1;
			temp = nextElementUp(position[0], position[1], r);
			
			if ((temp[0] == -1) && (temp[1] == -1))
			{
				temp = nextElementDown(position[0], position[1], r);
			}	
			
			position = temp;
		}
		else
			// Random element chosen from the available amount.
			position[1] = (int) (Math.random() * (amount-1)) + 1;
		
		return position;
	}
	
	protected int[] nextElementUp(int currentUnit, int currentElement, Refactoring r) 
	{
		int[] position = new int[2];
		
		if  (currentElement >= r.getAmount(currentUnit))
		{
			if (currentUnit == (r.getServiceConfiguration().getSourceFileRepository().getKnownCompilationUnits().size() - 1))
			{
				position[0] = -1;
				position[1] = -1;
			}
			else
			{				
				currentUnit++;

				int amount = 0;
				while (amount == 0)
				{
					if (currentUnit >= r.getServiceConfiguration().getSourceFileRepository().getKnownCompilationUnits().size())
					{
						position[0] = -1;
						position[1] = -1;
						amount = -1;
					}
					// Only counts the relevant program element.
					else
						amount = r.getAmount(currentUnit);

					if (amount == 0)
						currentUnit++;
				}
				if (amount != -1)
				{
					position[0] = currentUnit;
					position[1] = 1;
				}
			}
		}
		else
		{
			position[0] = currentUnit;
			position[1] = currentElement + 1;
		}

		return position;
	}
	
	protected int[] nextElementDown(int currentUnit, int currentElement, Refactoring r) 
	{
		int[] position = new int[2];
		
		if (currentElement <= 1)
		{
			if (currentUnit == 0)
			{
				position[0] = -1;
				position[1] = -1;
			}
			else
			{
				currentUnit--;

				int amount = 0;
				while (amount == 0)
				{
					if (currentUnit < 0)
					{
						position[0] = -1;
						position[1] = -1;
						amount = -1;
					}
					// Only counts the relevant program element.
					else
						amount = r.getAmount(currentUnit);

					if (amount == 0)
						currentUnit--;
				}
				if (amount != -1)
				{
					position[0] = currentUnit;
					position[1] = amount;
				}
			}
		}
		// This is a makeshift solution to using the method consecutively 
		// for different refactorings. If the element from the previous refactoring
		// is out of bounds of the current refactoring, the last element in the class
		// of the previous refactoring is used. This may not be the closest element
		// to the previous point in the program, but getting that would be awkward.
		else if (currentElement >= r.getAmount(currentUnit))
		{
			int amount = 0;
			while (amount == 0)
			{
				if (currentUnit < 0)
				{
					position[0] = -1;
					position[1] = -1;
					amount = -1;
				}
				// Only counts the relevant program element.
				else
					amount = r.getAmount(currentUnit);

				if (amount == 0)
					currentUnit--;
			}
			if (amount != -1)
			{
				position[0] = currentUnit;
				position[1] = amount;
			}
		}
		else
		{
			position[0] = currentUnit;
			position[1] = currentElement - 1;
		}

		return position;
	}
	
	// A neighbour will be chosen using the list of available refactorings and
	// the applicable elements on either side of the current position. If the 
	// element is at the edge of a compilation unit the next class will be used.
	// The neighbour returned will contain the element and refactoring that improves 
	// the current score, and the level of improvement will depend on whether the search is
	// first descent or steepest descent.
	protected int[] getNeighbour(int currentUnit, int currentElement, int iteration, ArrayList<Refactoring> refactorings, 
			boolean steepestDescent, FitnessFunction ff, Metrics m, float currentScore)
	{
		float newScore = currentScore;
		int[] position = new int[2];
		int[] neighbour = new int[3];
		neighbour[0] = currentUnit;
		neighbour[1] = currentElement;
		neighbour[2] = -1;

		for (int i = 0; i < refactorings.size(); i++)
		{
			double random = Math.random();
			if (random >= 0.5)
				position = nextElementUp(currentUnit, currentElement, refactorings.get(i));
			else
				position = nextElementDown(currentUnit, currentElement, refactorings.get(i));

			if ((position[0] != -1) && (position[1] != -1))
			{
				refactorings.get(i).transform(refactorings.get(i).analyze(iteration, position[0], position[1]));
				newScore = ff.calculateScore(m, this.c.getConfiguration());
				refactorings.get(i).transform(refactorings.get(i).analyzeReverse());

				if (newScore > currentScore)
				{
					neighbour[0] = position[0];
					neighbour[1] = position[1];
					neighbour[2] = i;
					
					if (steepestDescent)
						currentScore = newScore;
					else
						break;
				}
			}

			if (random >= 0.5)
				position = nextElementDown(currentUnit, currentElement, refactorings.get(i));
			else
				position = nextElementUp(currentUnit, currentElement, refactorings.get(i));

			if ((position[0] != -1) && (position[1] != -1))
			{
				refactorings.get(i).transform(refactorings.get(i).analyze(iteration, position[0], position[1]));
				newScore = ff.calculateScore(m, this.c.getConfiguration());
				refactorings.get(i).transform(refactorings.get(i).analyzeReverse());

				if (newScore > currentScore)
				{
					neighbour[0] = position[0];
					neighbour[1] = position[1];
					neighbour[2] = i;
					
					if (steepestDescent)
						currentScore = newScore;
					else
						break;
				}
			}
		}
		
		return neighbour;
	}

	// A neighbour will be chosen using the list of available refactorings and
	// the applicable elements on either side of the current position. If the 
	// element is at the edge of a compilation unit the next class will be used.
	// The neighbour returned will contain the element and refactoring that improves 
	// the current score, or if the refactoring doesn't improve the score it may still
	// be returned, depending on the current temperature value and the magnitude of the difference.
	protected int[] getNeighbour(int currentUnit, int currentElement, int iteration, ArrayList<Refactoring> refactorings, 
			float currentTemperature, FitnessFunction ff, Metrics m, float currentScore)
	{
		float newScore = currentScore;
		int[] position = new int[2];
		int[] neighbour = new int[3];
		neighbour[0] = currentUnit;
		neighbour[1] = currentElement;
		neighbour[2] = -1;

		for (int i = 0; i < refactorings.size(); i++)
		{
			double random = Math.random();
			if (random >= 0.5)
				position = nextElementUp(currentUnit, currentElement, refactorings.get(i));
			else
				position = nextElementDown(currentUnit, currentElement, refactorings.get(i));

			if ((position[0] != -1) && (position[1] != -1))
			{
				refactorings.get(i).transform(refactorings.get(i).analyze(iteration, position[0], position[1]));
				newScore = ff.calculateScore(m, this.c.getConfiguration());
				refactorings.get(i).transform(refactorings.get(i).analyzeReverse());

				if (newScore > currentScore)
				{
					neighbour[0] = position[0];
					neighbour[1] = position[1];
					neighbour[2] = i;
					break;
				}
				else
				{
					// Probability of accepting negative move = exponential((-)difference/current temperature).
					// Exponential of a negative value is confined to the range 0 -> 1 as the negative value approaches 0. 
					float probability = (float) Math.exp((newScore - currentScore) / currentTemperature);
					
					if (probability > Math.random())
					{
						neighbour[0] = position[0];
						neighbour[1] = position[1];
						neighbour[2] = i;
						break;
					}
				}
			}

			if (random >= 0.5)
				position = nextElementDown(currentUnit, currentElement, refactorings.get(i));
			else
				position = nextElementUp(currentUnit, currentElement, refactorings.get(i));

			if ((position[0] != -1) && (position[1] != -1))
			{
				refactorings.get(i).transform(refactorings.get(i).analyze(iteration, position[0], position[1]));
				newScore = ff.calculateScore(m, this.c.getConfiguration());
				refactorings.get(i).transform(refactorings.get(i).analyzeReverse());
				
				if (newScore > currentScore)
				{
					neighbour[0] = position[0];
					neighbour[1] = position[1];
					neighbour[2] = i;
					break;
				}
				else
				{
					// Probability of accepting negative move = exponential((-)difference/current temperature).
					// Exponential of a negative value is confined to the range 0 -> 1 as the negative value approaches 0. 
					float probability = (float) Math.exp((newScore - currentScore) / currentTemperature);
					
					if (probability > Math.random())
					{
						neighbour[0] = position[0];
						neighbour[1] = position[1];
						neighbour[2] = i;
						break;
					}
				}
			}
		}

		return neighbour;
	}
	
	// Output search information to results file.
	protected void outputSearchInfo(String pathName, String runInfo)
	{
		String runName = String.format("%sresults.txt", pathName);
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
			System.out.println("\nEXCEPTION: Cannot export results to text file.");
			System.exit(1);
		}
	}
		
	// Output refactoring information to results file.
	protected void outputRefactoringInfo(String pathName, double time, double qualityGain)
	{
		String runName = String.format("%sresults.txt", pathName);
		File dir = new File(pathName);
		if (!dir.exists()) 
			dir.mkdirs();

		try 
		{
			BufferedWriter bw = new BufferedWriter(new FileWriter(runName, true));
			bw.append("\r\n\r\n======== Applied Refactorings ========");

			for (int i = 0; i < this.refactoringInfo.size(); i++) 
				bw.append(String.format("\r\n%s", this.refactoringInfo.get(i)));

			bw.append(String.format("\r\n\r\nScore has improved overall by %f", qualityGain));
			bw.append(String.format("\r\nTime taken to refactor: %.2fs", time));

			bw.close();
		}
		catch (IOException e) 
		{
			System.out.println("\nEXCEPTION: Cannot export results to text file.");
			System.exit(1);
		}
	}
	
	// Outputs the metric values for the project.
	protected void outputMetrics(float score, Metrics metric, boolean initial, boolean log, String pathName)
	{
		FitnessFunction ff = new FitnessFunction();
		String[] outputs = ff.createOutput(metric, this.c.getConfiguration());
		
		// Create a location for the results output.
		String runName = String.format("%sresults.txt", pathName);
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
			
			bw.append(String.format("\r\nOverall score: %f", score));
			bw.close();
		}
		catch (IOException e) 
		{
			System.out.println("\nEXCEPTION: Cannot export results to text file.");
			System.exit(1);
		}

		if (log)
		{
			
			// Outputs the metric values for the project to the console for immediate feedback.
			System.out.printf("\n");
			
			for (int i = 0; i < outputs.length; i++)
			{
				if(outputs[i].charAt(outputs[i].length() - 7) == '.')
					outputs[i] = outputs[i].substring(0, outputs[i].length() - 4);
				
				System.out.printf("\n%s", outputs[i]);
			}
			
			System.out.printf("\nOverall score: %.2f", score);
		}
	}
	
	public void setResultsPath(String resultsPath)
	{
		this.resultsPath = resultsPath;
	}

	public String getResultsPath()
	{
		return this.resultsPath;
	}	
	
	public void setServiceConfiguration(CrossReferenceServiceConfiguration sc)
	{
		this.sc = sc;
	}
	
	public void setConfiguration(Configuration c)
	{
		this.c = c;
	}
}