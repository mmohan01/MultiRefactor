package tasks;

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
import searches.MultiObjectiveSearch;

public class ElementRecentnessTasks extends Tasks
{
	// No attributes - empty constructor.
	public ElementRecentnessTasks()
	{
		super();
	}

	public ElementRecentnessTasks(String pathway)
	{
		super(pathway);		
	}
	
	public void run()
	{		
		String[] input = new String[]{	
				"./data/original/beaver/beaver-0.9.11",
				"./data/original/apachexmlrpc/apachexmlrpc-3.1.1",
				"./data/original/jrdf/jrdf-0.3.4.3",
				"./data/original/ganttproject/ganttproject-1.11.1",
				"./data/original/jhotdraw/jhotdraw-6.0b1", 
				"./data/original/xom/xom-1.2.1"};
		
		// Create an initial service configuration to be overwritten.
		// Reads the source code from the specified directory.
		CrossReferenceServiceConfiguration sc = new CrossReferenceServiceConfiguration();
		String[][] sourceFiles = new String[][]{
			super.read(input[0]),
			super.read(input[1]),
			super.read(input[2]),
			super.read(input[3]), 
			super.read(input[4]),
			super.read(input[5])};
		
		// Create empty list of refactorings.
		// Reads the metric configuration in from a specified text file.
		ArrayList<Refactoring> refactorings = new ArrayList<Refactoring>();
		Configuration c = new Configuration("./configurations/qualityfunction.txt", refactorings);
		Configuration[] cM1 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/elementrecentnessbeaver.txt")};
		Configuration[] cM2 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/elementrecentnessapachexmlrpc.txt")};
		Configuration[] cM3 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/elementrecentnessjrdf.txt")};
		Configuration[] cM4 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/elementrecentnessganttproject.txt")};
		Configuration[] cM5 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/elementrecentnessjhotdraw.txt")};
		Configuration[] cM6 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/elementrecentnessxom.txt")};
		
		// Initialise search tasks.
		ArrayList<GeneticAlgorithmSearch> searches = new ArrayList<GeneticAlgorithmSearch>();
		searches.add(new MonoObjectiveSearch(sc, c, sourceFiles[0], true, 100, 50, 0.2f, 0.8f));
		searches.add(new MonoObjectiveSearch(sc, c, sourceFiles[0], true, 100, 50, 0.2f, 0.8f));
		searches.add(new MonoObjectiveSearch(sc, c, sourceFiles[0], true, 100, 50, 0.2f, 0.8f));
		searches.add(new MonoObjectiveSearch(sc, c, sourceFiles[0], true, 100, 50, 0.2f, 0.8f));
		searches.add(new MonoObjectiveSearch(sc, c, sourceFiles[0], true, 100, 50, 0.2f, 0.8f));
	
		searches.add(new MonoObjectiveSearch(sc, c, sourceFiles[1], true, 100, 50, 0.2f, 0.8f));
		searches.add(new MonoObjectiveSearch(sc, c, sourceFiles[1], true, 100, 50, 0.2f, 0.8f));
		searches.add(new MonoObjectiveSearch(sc, c, sourceFiles[1], true, 100, 50, 0.2f, 0.8f));
		searches.add(new MonoObjectiveSearch(sc, c, sourceFiles[1], true, 100, 50, 0.2f, 0.8f));
		searches.add(new MonoObjectiveSearch(sc, c, sourceFiles[1], true, 100, 50, 0.2f, 0.8f));
		
		searches.add(new MonoObjectiveSearch(sc, c, sourceFiles[2], true, 100, 50, 0.2f, 0.8f));
		searches.add(new MonoObjectiveSearch(sc, c, sourceFiles[2], true, 100, 50, 0.2f, 0.8f));
		searches.add(new MonoObjectiveSearch(sc, c, sourceFiles[2], true, 100, 50, 0.2f, 0.8f));
		searches.add(new MonoObjectiveSearch(sc, c, sourceFiles[2], true, 100, 50, 0.2f, 0.8f));
		searches.add(new MonoObjectiveSearch(sc, c, sourceFiles[2], true, 100, 50, 0.2f, 0.8f));
		
		searches.add(new MonoObjectiveSearch(sc, c, sourceFiles[3], true, 100, 50, 0.2f, 0.8f));
		searches.add(new MonoObjectiveSearch(sc, c, sourceFiles[3], true, 100, 50, 0.2f, 0.8f));
		searches.add(new MonoObjectiveSearch(sc, c, sourceFiles[3], true, 100, 50, 0.2f, 0.8f));
		searches.add(new MonoObjectiveSearch(sc, c, sourceFiles[3], true, 100, 50, 0.2f, 0.8f));
		searches.add(new MonoObjectiveSearch(sc, c, sourceFiles[3], true, 100, 50, 0.2f, 0.8f));
		
		searches.add(new MonoObjectiveSearch(sc, c, sourceFiles[4], true, 100, 50, 0.2f, 0.8f));
		searches.add(new MonoObjectiveSearch(sc, c, sourceFiles[4], true, 100, 50, 0.2f, 0.8f));
		searches.add(new MonoObjectiveSearch(sc, c, sourceFiles[4], true, 100, 50, 0.2f, 0.8f));
		searches.add(new MonoObjectiveSearch(sc, c, sourceFiles[4], true, 100, 50, 0.2f, 0.8f));
		searches.add(new MonoObjectiveSearch(sc, c, sourceFiles[4], true, 100, 50, 0.2f, 0.8f));
		
		searches.add(new MonoObjectiveSearch(sc, c, sourceFiles[5], true, 100, 50, 0.2f, 0.8f));
		searches.add(new MonoObjectiveSearch(sc, c, sourceFiles[5], true, 100, 50, 0.2f, 0.8f));
		searches.add(new MonoObjectiveSearch(sc, c, sourceFiles[5], true, 100, 50, 0.2f, 0.8f));
		searches.add(new MonoObjectiveSearch(sc, c, sourceFiles[5], true, 100, 50, 0.2f, 0.8f));
		searches.add(new MonoObjectiveSearch(sc, c, sourceFiles[5], true, 100, 50, 0.2f, 0.8f));
		
		searches.add(new MultiObjectiveSearch(sc, cM1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cM1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cM1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cM1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cM1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f));
	
		searches.add(new MultiObjectiveSearch(sc, cM2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cM2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cM2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cM2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cM2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f));
		
		searches.add(new MultiObjectiveSearch(sc, cM3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cM3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cM3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cM3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cM3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f));
		
		searches.add(new MultiObjectiveSearch(sc, cM4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cM4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cM4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cM4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cM4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f));
		
		searches.add(new MultiObjectiveSearch(sc, cM5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cM5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cM5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cM5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cM5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f));
		
		searches.add(new MultiObjectiveSearch(sc, cM6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cM6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cM6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cM6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cM6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f));
		
		// Create list of output directories for
		// each refactored project to be written to.
		String[] outputDir = new String[]{
				"./data/refactored/ElementRecentnessExperiment/Mono-Objective/",
				"./data/refactored/ElementRecentnessExperiment/Multi-Objective/"};

		// Create list of output directories for
		// each result data output to be written to.
		String[] resultsDir = new String[]{
				"./results/ElementRecentnessExperiment/Mono-Objective/",
				"./results/ElementRecentnessExperiment/Multi-Objective/"};

		long timeTaken, startTime = System.currentTimeMillis();
		double time;
		
		for (int i = 0; i < searches.size(); i++)
		{
			// Creates new service configuration to start from scratch.
			sc = new CrossReferenceServiceConfiguration();
			int search = (i < 30) ? 0 : 1;
			int path = (int) Math.floor(i / 5);
			int run = (i % 5) + 1;

			while (path > 5)
				path -= 6;
			
			String outputPath = outputDir[search] + input[path].substring(input[path].lastIndexOf("/") + 1) + "/" + run + "/";
			String resultsPath = resultsDir[search] + input[path].substring(input[path].lastIndexOf("/") + 1) + "/" + run + "/";

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
			ExtractSubclass es = new ExtractSubclass(sc);
			refactorings.add(es);
			
			try 
			{
				// Read the original input.
				sc.getSourceFileRepository().getCompilationUnitsFromFiles(sourceFiles[path]);
			}
			catch (ParserException e) 
			{
				System.out.println("\r\nEXCEPTION: Cannot read input.");
				System.exit(1);
			}
			
			// Set up initial properties of service configuration.
			sc.getProjectSettings().setProperty(PropertyNames.INPUT_PATH, input[path] + super.readLibs(input[path]));
			sc.getProjectSettings().setProperty(PropertyNames.OUTPUT_PATH, outputPath);
			sc.getProjectSettings().ensureSystemClassesAreInPath();

			// initialise search task.			
			if (searches.get(i).getClass().getName().contains("MultiObjectiveSearch"))
				((MultiObjectiveSearch) searches.get(i)).setRefactorings(refactorings);
			else
			{
				c = new Configuration("./configurations/qualityfunction.txt", refactorings);
				searches.get(i).setConfiguration(c);
			}
			
			searches.get(i).setServiceConfiguration(sc);
			searches.get(i).setResultsPath(resultsPath);
			searches.get(i).run();
			searches.set(i, null);
		}	

		// Output overall time taken to console.
		timeTaken = System.currentTimeMillis() - startTime;
		time = timeTaken / 1000.0;
		System.out.printf("\r\n\r\nTime taken to run program: %.2fs", time);
	}
}