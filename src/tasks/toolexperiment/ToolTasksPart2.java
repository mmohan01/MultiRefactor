package tasks.toolexperiment;

import java.util.ArrayList;

import multirefactor.Configuration;
import recoder.CrossReferenceServiceConfiguration;
import recoder.ParserException;
import recoder.io.PropertyNames;
import refactorings.Refactoring;
import refactorings.field.*;
import refactorings.method.*;
import refactorings.type.*;
import searches.GeneticAlgorithmSearch;
import searches.MonoObjectiveSearch;
import searches.Search;
import tasks.Tasks;

public class ToolTasksPart2 extends Tasks
{
	String pathway = "./data/original/jhotdraw/jhotdraw-5.3";
	
	// No attributes - empty constructor.
	public ToolTasksPart2()
	{
		super();
	}
	
	public ToolTasksPart2(String pathway)
	{
		super(pathway);		
	}
	
	public void run()
	{				
		// Create an initial service configuration to be overwritten.
		// Reads the source code from the specified directory.
		CrossReferenceServiceConfiguration sc = new CrossReferenceServiceConfiguration();
		String[] sourceFiles = super.read(this.pathway);
		
		// Create empty list of refactorings.
		// Reads the metric configuration in from a specified text file.
		ArrayList<Refactoring> refactorings = new ArrayList<Refactoring>();
		Configuration c = new Configuration("configurations/visibilityratio.txt", refactorings);
		
		// Initialise search tasks.
		ArrayList<Search> searches = new ArrayList<Search>();
		searches.add(new MonoObjectiveSearch(sc, c, sourceFiles, false, 50, 10, 0.2f, 0.8f));
		searches.add(new MonoObjectiveSearch(sc, c, sourceFiles, false, 100, 10, 0.2f, 0.8f));
		searches.add(new MonoObjectiveSearch(sc, c, sourceFiles, false, 200, 10, 0.2f, 0.8f));
		searches.add(new MonoObjectiveSearch(sc, c, sourceFiles, false, 50, 50, 0.2f, 0.8f));
		searches.add(new MonoObjectiveSearch(sc, c, sourceFiles, false, 100, 50, 0.2f, 0.8f));
		searches.add(new MonoObjectiveSearch(sc, c, sourceFiles, false, 200, 50, 0.2f, 0.8f));
		searches.add(new MonoObjectiveSearch(sc, c, sourceFiles, false, 50, 100, 0.2f, 0.8f));
		searches.add(new MonoObjectiveSearch(sc, c, sourceFiles, false, 100, 100, 0.2f, 0.8f));
		searches.add(new MonoObjectiveSearch(sc, c, sourceFiles, false, 200, 100, 0.2f, 0.8f));
		
		long timeTaken, startTime = System.currentTimeMillis();
		double time;
				
		for (int i = 1; i <= 3; i++)
		{
			int refactoringRange = 0;
			
			switch (i)
			{
			case 1:
				refactoringRange = 50;
				break;
			case 2:
				refactoringRange = 100;
				break;
			case 3:
				refactoringRange = 200;
				break;
			}

			// Create list of output directories for
			// each refactored project to be written to.
			String[] outputDir = new String[]{
					"./data/refactored/ToolExperiment/Part2/G50-PS10-RR" + refactoringRange + "/",
					"./data/refactored/ToolExperiment/Part2/G100-PS10-RR" + refactoringRange + "/",
					"./data/refactored/ToolExperiment/Part2/G200-PS10-RR" + refactoringRange + "/",
					"./data/refactored/ToolExperiment/Part2/G50-PS50-RR" + refactoringRange + "/",
					"./data/refactored/ToolExperiment/Part2/G100-PS50-RR" + refactoringRange + "/",
					"./data/refactored/ToolExperiment/Part2/G200-PS50-RR" + refactoringRange + "/",
					"./data/refactored/ToolExperiment/Part2/G50-PS100-RR" + refactoringRange + "/",
					"./data/refactored/ToolExperiment/Part2/G100-PS100-RR" + refactoringRange + "/",
					"./data/refactored/ToolExperiment/Part2/G200-PS100-RR" + refactoringRange + "/"};

			// Create list of output directories for
			// each result data output to be written to.
			String[] resultsDir = new String[]{
					"./results/ToolExperiment/Part2/G50-PS10-RR" + refactoringRange + "/",
					"./results/ToolExperiment/Part2/G100-PS10-RR" + refactoringRange + "/",
					"./results/ToolExperiment/Part2/G200-PS10-RR" + refactoringRange + "/",
					"./results/ToolExperiment/Part2/G50-PS50-RR" + refactoringRange + "/",
					"./results/ToolExperiment/Part2/G100-PS50-RR" + refactoringRange + "/",
					"./results/ToolExperiment/Part2/G200-PS50-RR" + refactoringRange + "/",
					"./results/ToolExperiment/Part2/G50-PS100-RR" + refactoringRange + "/",
					"./results/ToolExperiment/Part2/G100-PS100-RR" + refactoringRange + "/",
					"./results/ToolExperiment/Part2/G200-PS100-RR" + refactoringRange + "/"};

			for (int j = 0; j < searches.size(); j++)
			{
				// Creates new service configuration to start from scratch.
				sc = new CrossReferenceServiceConfiguration();

				// Initialise available refactorings. Needs to be done each 
				// time as the service configuration won't be updated otherwise.
				refactorings = new ArrayList<Refactoring>();
				DecreaseMethodVisibility dmv = new DecreaseMethodVisibility(sc);
				refactorings.add(dmv);
				DecreaseFieldVisibility dfv = new DecreaseFieldVisibility(sc);
				refactorings.add(dfv);
				IncreaseMethodVisibility imv = new IncreaseMethodVisibility(sc);
				refactorings.add(imv);
				IncreaseFieldVisibility ifv = new IncreaseFieldVisibility(sc);
				refactorings.add(ifv);
				MakeClassAbstract mca = new MakeClassAbstract(sc);
				refactorings.add(mca);
				MakeClassConcrete mcc = new MakeClassConcrete(sc);
				refactorings.add(mcc);
				MakeClassFinal mcf = new MakeClassFinal(sc);
				refactorings.add(mcf);
				MakeMethodFinal mmf = new MakeMethodFinal(sc);
				refactorings.add(mmf);
				MakeFieldFinal mff = new MakeFieldFinal(sc);
				refactorings.add(mff);
				MakeClassNonFinal mcnf = new MakeClassNonFinal(sc);
				refactorings.add(mcnf);
				MakeMethodNonFinal mmnf = new MakeMethodNonFinal(sc);
				refactorings.add(mmnf);
				MakeFieldNonFinal mfnf = new MakeFieldNonFinal(sc);
				refactorings.add(mfnf);
				MakeMethodStatic mms = new MakeMethodStatic(sc);
				refactorings.add(mms);
				MakeFieldStatic mfs = new MakeFieldStatic(sc);
				refactorings.add(mfs);
				MakeMethodNonStatic mmns = new MakeMethodNonStatic(sc);
				refactorings.add(mmns);
				MakeFieldNonStatic mfns = new MakeFieldNonStatic(sc);
				refactorings.add(mfns);
				MoveMethodUp mmu = new MoveMethodUp(sc);
				refactorings.add(mmu);
				MoveFieldUp mfu = new MoveFieldUp(sc);
				refactorings.add(mfu);
				MoveMethodDown mmd = new MoveMethodDown(sc);
				refactorings.add(mmd);	
				MoveFieldDown mfd = new MoveFieldDown(sc);
				refactorings.add(mfd);
				RemoveInterface ri = new RemoveInterface(sc);
				refactorings.add(ri);
				RemoveClass rc = new RemoveClass(sc);
				refactorings.add(rc);
				RemoveMethod rm = new RemoveMethod(sc);
				refactorings.add(rm);
				RemoveField rf = new RemoveField(sc);
				refactorings.add(rf);
				CollapseHierarchy ch = new CollapseHierarchy(sc);
				refactorings.add(ch);

				try 
				{
					// Read the original input.
					sc.getSourceFileRepository().getCompilationUnitsFromFiles(sourceFiles);
				}
				catch (ParserException e) 
				{
					System.out.println("\r\nEXCEPTION: Cannot read input.");
					System.exit(1);
				}

				// Set up initial properties of service configuration.
				sc.getProjectSettings().setProperty(PropertyNames.INPUT_PATH, this.pathway + super.readLibs(this.pathway));
				sc.getProjectSettings().setProperty(PropertyNames.OUTPUT_PATH, outputDir[j]);
				sc.getProjectSettings().ensureSystemClassesAreInPath();

				// initialise search task.			
				c = new Configuration("configurations/visibilityratio.txt", refactorings);
				searches.get(j).setConfiguration(c);
				searches.get(j).setServiceConfiguration(sc);
				searches.get(j).setResultsPath(resultsDir[j]);
				((GeneticAlgorithmSearch) searches.get(i)).setInitialRefactoringRange(refactoringRange);
				searches.get(j).run();
				searches.set(i, null);
				
				// Output overall time taken to console.
				timeTaken = System.currentTimeMillis() - startTime;
				time = timeTaken / 1000.0;
				System.out.printf("\r\n\r\nTime taken to run part 2 so far: %.2fs", time);
				System.out.printf("\n\r-----------------------------------------------");
				System.out.printf("\n\r-----------------------------------------------");
			}	
		}
	}
}