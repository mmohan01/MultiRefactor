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
		// Reads the metric configuration in from them to a specified text file.
		ArrayList<Refactoring> refactorings = new ArrayList<Refactoring>();
		Configuration c = new Configuration("configurations/visibility.txt", refactorings);
		
		// Initialise search tasks.
		ArrayList<Search> searches = new ArrayList<Search>();
		GeneticAlgorithmSearch geneticAlgorithm1 = new GeneticAlgorithmSearch(sc, c, sourceFiles, true, 50, 10, 0.2f, 0.8f);
		geneticAlgorithm1.setInitialRefactoringRange(50);
		searches.add(geneticAlgorithm1);
		GeneticAlgorithmSearch geneticAlgorithm2 = new GeneticAlgorithmSearch(sc, c, sourceFiles, true, 50, 10, 0.5f, 0.5f);
		geneticAlgorithm2.setInitialRefactoringRange(50);
		searches.add(geneticAlgorithm2);
		GeneticAlgorithmSearch geneticAlgorithm3 = new GeneticAlgorithmSearch(sc, c, sourceFiles, true, 50, 10, 0.8f, 0.2f);
		geneticAlgorithm3.setInitialRefactoringRange(50);
		searches.add(geneticAlgorithm3);

		// Create list of output directories for
		// each refactored project to be written to.
		String[] output = new String[]{
				"./data/refactored/ToolExperiment/Part1/CP0.2-MP-0.8/",
				"./data/refactored/ToolExperiment/Part1/CP0.5-MP-0.5/",
				"./data/refactored/ToolExperiment/Part1/CP0.8-MP-0.2/"};

		// Create list of output directories for
		// each result data output to be written to.
		String[] resultsDir = new String[]{
				"./results/ToolExperiment/Part1/CP0.2-MP-0.8/",
				"./results/ToolExperiment/Part1/CP0.5-MP-0.5/",
				"./results/ToolExperiment/Part1/CP0.8-MP-0.2/"};

		long timeTaken, startTime = System.currentTimeMillis();
		double time;

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
			sc.getProjectSettings().setProperty(PropertyNames.OUTPUT_PATH, output[i]);
			sc.getProjectSettings().ensureSystemClassesAreInPath();

			// initialise search task.			
			c = new Configuration("configurations/visibility.txt", refactorings);
			searches.get(i).setConfiguration(c);
			searches.get(i).setServiceConfiguration(sc);
			searches.get(i).setResultsPath(resultsDir[i]);
			searches.get(i).run();
		}	

		// Output overall time taken to console.
		timeTaken = System.currentTimeMillis() - startTime;
		time = timeTaken / 1000.0;
		System.out.printf("\r\n\r\nTime taken to run part 1 of experiment: %.2fs", time);
		System.out.printf("\r\n-----------------------------------------------");
		System.out.printf("\r\n-----------------------------------------------");
	}
}