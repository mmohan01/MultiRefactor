package tasks.toolexperiment;

import java.util.ArrayList;

import recoder.CrossReferenceServiceConfiguration;
import recoder.ParserException;
import recoder.io.PropertyNames;
import refactorings.Refactoring;
import refactorings.field.*;
import refactorings.method.*;
import refactorings.type.*;
import multirefactor.Configuration;
import searches.MonoObjectiveSearch;
import searches.Search;
import tasks.Tasks;

public class ToolTasksPart1 extends Tasks
{
	String pathway = "./data/original/jhotdraw-5.3";

	// No attributes - empty constructor.
	public ToolTasksPart1()
	{
		super();
	}

	public ToolTasksPart1(String pathway)
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
		MonoObjectiveSearch geneticAlgorithm1 = new MonoObjectiveSearch(sc, c, sourceFiles, true, 50, 10, 0.2f, 0.2f);
		geneticAlgorithm1.setInitialRefactoringRange(50);
		searches.add(geneticAlgorithm1);
		MonoObjectiveSearch geneticAlgorithm2 = new MonoObjectiveSearch(sc, c, sourceFiles, true, 50, 10, 0.2f, 0.5f);
		geneticAlgorithm2.setInitialRefactoringRange(50);
		searches.add(geneticAlgorithm2);
		MonoObjectiveSearch geneticAlgorithm3 = new MonoObjectiveSearch(sc, c, sourceFiles, true, 50, 10, 0.2f, 0.8f);
		geneticAlgorithm3.setInitialRefactoringRange(50);
		searches.add(geneticAlgorithm3);
		MonoObjectiveSearch geneticAlgorithm4 = new MonoObjectiveSearch(sc, c, sourceFiles, true, 50, 10, 0.5f, 0.2f);
		geneticAlgorithm4.setInitialRefactoringRange(50);
		searches.add(geneticAlgorithm4);
		MonoObjectiveSearch geneticAlgorithm5 = new MonoObjectiveSearch(sc, c, sourceFiles, true, 50, 10, 0.5f, 0.5f);
		geneticAlgorithm5.setInitialRefactoringRange(50);
		searches.add(geneticAlgorithm5);
		MonoObjectiveSearch geneticAlgorithm6 = new MonoObjectiveSearch(sc, c, sourceFiles, true, 50, 10, 0.5f, 0.8f);
		geneticAlgorithm6.setInitialRefactoringRange(50);
		searches.add(geneticAlgorithm6);
		MonoObjectiveSearch geneticAlgorithm7 = new MonoObjectiveSearch(sc, c, sourceFiles, true, 50, 10, 0.8f, 0.2f);
		geneticAlgorithm7.setInitialRefactoringRange(50);
		searches.add(geneticAlgorithm7);
		MonoObjectiveSearch geneticAlgorithm8 = new MonoObjectiveSearch(sc, c, sourceFiles, true, 50, 10, 0.8f, 0.5f);
		geneticAlgorithm8.setInitialRefactoringRange(50);
		searches.add(geneticAlgorithm8);
		MonoObjectiveSearch geneticAlgorithm9 = new MonoObjectiveSearch(sc, c, sourceFiles, true, 50, 10, 0.8f, 0.2f);
		geneticAlgorithm9.setInitialRefactoringRange(50);
		searches.add(geneticAlgorithm9);

		long timeTaken, startTime = System.currentTimeMillis();
		double time;

		for (int ij = 1; ij <= 5; ij++)
		{
			// Create list of output directories for
			// each refactored project to be written to.
			String[] output = new String[]{
					"./data/refactored/ToolExperiment/Part1/CP0.2-MP0.2/" + ij + "/",
					"./data/refactored/ToolExperiment/Part1/CP0.2-MP0.5/" + ij + "/",
					"./data/refactored/ToolExperiment/Part1/CP0.2-MP0.8/" + ij + "/",
					"./data/refactored/ToolExperiment/Part1/CP0.5-MP0.2/" + ij + "/",
					"./data/refactored/ToolExperiment/Part1/CP0.5-MP0.5/" + ij + "/",
					"./data/refactored/ToolExperiment/Part1/CP0.5-MP0.8/" + ij + "/",
					"./data/refactored/ToolExperiment/Part1/CP0.8-MP0.2/" + ij + "/",
					"./data/refactored/ToolExperiment/Part1/CP0.8-MP0.5/" + ij + "/",
					"./data/refactored/ToolExperiment/Part1/CP0.8-MP0.8/" + ij + "/"};

			// Create list of output directories for
			// each result data output to be written to.
			String[] resultsDir = new String[]{
					"./results/ToolExperiment/Part1/CP0.2-MP0.2/" + ij + "/",
					"./results/ToolExperiment/Part1/CP0.2-MP0.5/" + ij + "/",
					"./results/ToolExperiment/Part1/CP0.2-MP0.8/" + ij + "/",
					"./results/ToolExperiment/Part1/CP0.5-MP0.2/" + ij + "/",
					"./results/ToolExperiment/Part1/CP0.5-MP0.5/" + ij + "/",
					"./results/ToolExperiment/Part1/CP0.5-MP0.8/" + ij + "/",
					"./results/ToolExperiment/Part1/CP0.8-MP0.2/" + ij + "/",
					"./results/ToolExperiment/Part1/CP0.8-MP0.5/" + ij + "/",
					"./results/ToolExperiment/Part1/CP0.8-MP0.8/" + ij + "/"};

			for (int i = 0; i < searches.size(); i++)
			{
				// Creates new service configuration to start from scratch.
				sc = new CrossReferenceServiceConfiguration();

				// Initialise available refactorings. Needs to be done each 
				// time as the service configuration won't be updated otherwise.
				refactorings = new ArrayList<Refactoring>();
				DecreaseMethodSecurity dms = new DecreaseMethodSecurity(sc);
				refactorings.add(dms);
				DecreaseFieldSecurity dfs = new DecreaseFieldSecurity(sc);
				refactorings.add(dfs);
				IncreaseMethodSecurity ims = new IncreaseMethodSecurity(sc);
				refactorings.add(ims);
				IncreaseFieldSecurity ifs = new IncreaseFieldSecurity(sc);
				refactorings.add(ifs);
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
				sc.getProjectSettings().setProperty(PropertyNames.OUTPUT_PATH, output[i]);
				sc.getProjectSettings().ensureSystemClassesAreInPath();

				// initialise search task.			
				c = new Configuration("configurations/visibilityratio.txt", refactorings);
				searches.get(i).setConfiguration(c);
				searches.get(i).setServiceConfiguration(sc);
				searches.get(i).setResultsPath(resultsDir[i]);
				searches.get(i).run();

				// Output overall time taken to console.
				timeTaken = System.currentTimeMillis() - startTime;
				time = timeTaken / 1000.0;
				System.out.printf("\r\n\r\nTime taken to run part 1 of experiment: %.2fs", time);
				System.out.printf("\r\n-----------------------------------------------");
				System.out.printf("\r\n-----------------------------------------------");
			}	
		}
	}
}