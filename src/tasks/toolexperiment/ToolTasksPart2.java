package tasks.toolexperiment;

import java.util.ArrayList;

import recoder.CrossReferenceServiceConfiguration;
import recoder.ParserException;
import recoder.io.PropertyNames;
import refactorings.Refactoring;
import refactorings.field.DecreaseFieldSecurity;
import refactorings.field.IncreaseFieldSecurity;
import refactorings.field.MakeFieldFinal;
import refactorings.field.MakeFieldNonFinal;
import refactorings.field.MakeFieldNonStatic;
import refactorings.field.MakeFieldStatic;
import refactorings.field.MoveFieldDown;
import refactorings.field.MoveFieldUp;
import refactorings.field.RemoveField;
import refactorings.method.DecreaseMethodSecurity;
import refactorings.method.IncreaseMethodSecurity;
import refactorings.method.MakeMethodFinal;
import refactorings.method.MakeMethodNonFinal;
import refactorings.method.MakeMethodNonStatic;
import refactorings.method.MakeMethodStatic;
import refactorings.method.MoveMethodDown;
import refactorings.method.MoveMethodUp;
import refactorings.method.RemoveMethod;
import refactorings.type.CollapseHierarchy;
import refactorings.type.ExtractSubclass;
import refactorings.type.MakeClassAbstract;
import refactorings.type.MakeClassConcrete;
import refactorings.type.MakeClassFinal;
import refactorings.type.MakeClassNonFinal;
import refactorings.type.RemoveClass;
import refactorings.type.RemoveInterface;
import refactory.Configuration;
import searches.GeneticAlgorithmSearch;
import searches.Search;
import tasks.Tasks;

public class ToolTasksPart2 extends Tasks
{
	String pathway = "./data/original/jhotdraw-5.3";
	
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
		// Reads the metric configuration in from them to a specified text file.
		ArrayList<Refactoring> refactorings = new ArrayList<Refactoring>();
		Configuration c = new Configuration("configurations/visibility.txt", refactorings);
		
		// Initialise search tasks.
		ArrayList<Search> searches = new ArrayList<Search>();
		GeneticAlgorithmSearch geneticAlgorithm1 = new GeneticAlgorithmSearch(sc, c, sourceFiles, true, 50, 10, 0.2f, 0.8f);
		searches.add(geneticAlgorithm1);
		GeneticAlgorithmSearch geneticAlgorithm2 = new GeneticAlgorithmSearch(sc, c, sourceFiles, true, 100, 10, 0.2f, 0.8f);
		searches.add(geneticAlgorithm2);
		GeneticAlgorithmSearch geneticAlgorithm3 = new GeneticAlgorithmSearch(sc, c, sourceFiles, true, 200, 10, 0.2f, 0.8f);
		searches.add(geneticAlgorithm3);
		GeneticAlgorithmSearch geneticAlgorithm4 = new GeneticAlgorithmSearch(sc, c, sourceFiles, true, 50, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm4);
		GeneticAlgorithmSearch geneticAlgorithm5 = new GeneticAlgorithmSearch(sc, c, sourceFiles, true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm5);
		GeneticAlgorithmSearch geneticAlgorithm6 = new GeneticAlgorithmSearch(sc, c, sourceFiles, true, 200, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm6);
		GeneticAlgorithmSearch geneticAlgorithm7 = new GeneticAlgorithmSearch(sc, c, sourceFiles, true, 50, 100, 0.2f, 0.8f);
		searches.add(geneticAlgorithm7);
		GeneticAlgorithmSearch geneticAlgorithm8 = new GeneticAlgorithmSearch(sc, c, sourceFiles, true, 100, 100, 0.2f, 0.8f);
		searches.add(geneticAlgorithm8);
		GeneticAlgorithmSearch geneticAlgorithm9 = new GeneticAlgorithmSearch(sc, c, sourceFiles, true, 200, 100, 0.2f, 0.8f);
		searches.add(geneticAlgorithm9);
		
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
			String[] output = new String[]{
					"./data/refactored/ToolExperiment/Part2/G50-PS10-RR-" + refactoringRange + "/",
					"./data/refactored/ToolExperiment/Part2/G100-PS10-RR-" + refactoringRange + "/",
					"./data/refactored/ToolExperiment/Part2/G200-PS10-RR-" + refactoringRange + "/",
					"./data/refactored/ToolExperiment/Part2/G50-PS50-RR-" + refactoringRange + "/",
					"./data/refactored/ToolExperiment/Part2/G100-PS50-RR-" + refactoringRange + "/",
					"./data/refactored/ToolExperiment/Part2/G200-PS50-RR-" + refactoringRange + "/",
					"./data/refactored/ToolExperiment/Part2/G50-PS100-RR-" + refactoringRange + "/",
					"./data/refactored/ToolExperiment/Part2/G100-PS100-RR-" + refactoringRange + "/",
					"./data/refactored/ToolExperiment/Part2/G200-PS100-RR-" + refactoringRange + "/"};

			// Create list of output directories for
			// each result data output to be written to.
			String[]resultsDir = new String[]{
					"./results/ToolExperiment/Part2/G50-PS10-RR-" + refactoringRange + "/",
					"./results/ToolExperiment/Part2/G100-PS10-RR-" + refactoringRange + "/",
					"./resultsd/ToolExperiment/Part2/G200-PS10-RR-" + refactoringRange + "/",
					"./results/ToolExperiment/Part2/G50-PS50-RR-" + refactoringRange + "/",
					"./results/ToolExperiment/Part2/G100-PS50-RR-" + refactoringRange + "/",
					"./results/ToolExperiment/Part2/G200-PS50-RR-" + refactoringRange + "/",
					"./results/ToolExperiment/Part2/G50-PS100-RR-" + refactoringRange + "/",
					"./results/ToolExperiment/Part2/G100-PS100-RR-" + refactoringRange + "/",
					"./results/ToolExperiment/Part2/G200-PS100-RR-" + refactoringRange + "/"};

			for (int j = 0; j < searches.size(); j++)
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
				ExtractSubclass es = new ExtractSubclass(sc);
				refactorings.add(es);
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
				sc.getProjectSettings().setProperty(PropertyNames.OUTPUT_PATH, output[j]);
				sc.getProjectSettings().ensureSystemClassesAreInPath();

				// initialise search task.			
				c = new Configuration("configurations/visibility.txt", refactorings);
				searches.get(j).setConfiguration(c);
				searches.get(j).setServiceConfiguration(sc);
				searches.get(j).setResultsPath(resultsDir[j]);
				((GeneticAlgorithmSearch) searches.get(i)).setInitialRefactoringRange(refactoringRange);
				searches.get(j).run();
			}	

			// Output overall time taken to console.
			timeTaken = System.currentTimeMillis() - startTime;
			time = timeTaken / 1000.0;
			System.out.printf("\r\n\r\nTime taken to run part 2 of experiment: %.2fs", time);
			System.out.printf("\n\r-----------------------------------------------");
			System.out.printf("\n\r-----------------------------------------------");
		}
	}
}