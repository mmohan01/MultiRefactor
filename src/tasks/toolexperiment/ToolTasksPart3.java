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
import searches.MonoObjectiveSearch;
import searches.Search;
import tasks.Tasks;

public class ToolTasksPart3 extends Tasks
{
	// No attributes - empty constructor.
	public ToolTasksPart3()
	{
		super();
	}
	
	public ToolTasksPart3(String pathway)
	{
		super(pathway);		
	}
	
	public void run()
	{								
		String[] input = new String[]{
				"./data/original/json-1.1",
				"./data/original/mango",
				"./data/original/beaver-0.9.11",
				"./data/original/apachexmlrpc-2.0",
				"./data/original/jhotdraw-5.3"};
		
		// Create an initial service configuration to be overwritten.
		// Reads the source code from the specified directory.
		CrossReferenceServiceConfiguration sc = new CrossReferenceServiceConfiguration();
		String[][] sourceFiles = new String[][]{
				super.read("./data/original/json-1.1"),
				super.read("./data/original/mango"),
				super.read("./data/original/beaver-0.9.11"),
				super.read("./data/original/apachexmlrpc-2.0"),
				super.read("./data/original/jhotdraw-5.3")};
		
		// Create empty list of refactorings.
		// Reads the metric configuration in from a specified text file.
		ArrayList<Refactoring> refactorings = new ArrayList<Refactoring>();
		Configuration c = new Configuration("./configurations/classdesignsize.txt", refactorings);
		
		// Initialise search tasks.
		ArrayList<Search> searches = new ArrayList<Search>();
		MonoObjectiveSearch geneticAlgorithm1 = new MonoObjectiveSearch(sc, c, sourceFiles[0], true, 100, 50, 0.2f, 0.8f);
		geneticAlgorithm1.setInitialRefactoringRange(50);
		searches.add(geneticAlgorithm1);
		MonoObjectiveSearch geneticAlgorithm2 = new MonoObjectiveSearch(sc, c, sourceFiles[0], true, 100, 50, 0.2f, 0.8f);
		geneticAlgorithm2.setInitialRefactoringRange(50);
		searches.add(geneticAlgorithm2);
		MonoObjectiveSearch geneticAlgorithm3 = new MonoObjectiveSearch(sc, c, sourceFiles[0], true, 100, 50, 0.2f, 0.8f);
		geneticAlgorithm3.setInitialRefactoringRange(50);
		searches.add(geneticAlgorithm3);
		MonoObjectiveSearch geneticAlgorithm4 = new MonoObjectiveSearch(sc, c, sourceFiles[0], true, 100, 50, 0.2f, 0.8f);
		geneticAlgorithm4.setInitialRefactoringRange(50);
		searches.add(geneticAlgorithm4);
		MonoObjectiveSearch geneticAlgorithm5 = new MonoObjectiveSearch(sc, c, sourceFiles[0], true, 100, 50, 0.2f, 0.8f);
		geneticAlgorithm5.setInitialRefactoringRange(50);
		searches.add(geneticAlgorithm5);
		
		MonoObjectiveSearch geneticAlgorithm6 = new MonoObjectiveSearch(sc, c, sourceFiles[1], true, 100, 50, 0.2f, 0.8f);
		geneticAlgorithm6.setInitialRefactoringRange(50);
		searches.add(geneticAlgorithm6);
		MonoObjectiveSearch geneticAlgorithm7 = new MonoObjectiveSearch(sc, c, sourceFiles[1], true, 100, 50, 0.2f, 0.8f);
		geneticAlgorithm7.setInitialRefactoringRange(50);
		searches.add(geneticAlgorithm7);
		MonoObjectiveSearch geneticAlgorithm8 = new MonoObjectiveSearch(sc, c, sourceFiles[1], true, 100, 50, 0.2f, 0.8f);
		geneticAlgorithm8.setInitialRefactoringRange(50);
		searches.add(geneticAlgorithm8);
		MonoObjectiveSearch geneticAlgorithm9 = new MonoObjectiveSearch(sc, c, sourceFiles[1], true, 100, 50, 0.2f, 0.8f);
		geneticAlgorithm9.setInitialRefactoringRange(50);
		searches.add(geneticAlgorithm9);
		MonoObjectiveSearch geneticAlgorithm10 = new MonoObjectiveSearch(sc, c, sourceFiles[1], true, 100, 50, 0.2f, 0.8f);
		geneticAlgorithm10.setInitialRefactoringRange(50);
		searches.add(geneticAlgorithm10);
		
		MonoObjectiveSearch geneticAlgorithm11 = new MonoObjectiveSearch(sc, c, sourceFiles[2], true, 100, 50, 0.2f, 0.8f);
		geneticAlgorithm11.setInitialRefactoringRange(50);
		searches.add(geneticAlgorithm11);
		MonoObjectiveSearch geneticAlgorithm12 = new MonoObjectiveSearch(sc, c, sourceFiles[2], true, 100, 50, 0.2f, 0.8f);
		geneticAlgorithm12.setInitialRefactoringRange(50);
		searches.add(geneticAlgorithm12);
		MonoObjectiveSearch geneticAlgorithm13 = new MonoObjectiveSearch(sc, c, sourceFiles[2], true, 100, 50, 0.2f, 0.8f);
		geneticAlgorithm13.setInitialRefactoringRange(50);
		searches.add(geneticAlgorithm13);
		MonoObjectiveSearch geneticAlgorithm14 = new MonoObjectiveSearch(sc, c, sourceFiles[2], true, 100, 50, 0.2f, 0.8f);
		geneticAlgorithm14.setInitialRefactoringRange(50);
		searches.add(geneticAlgorithm14);
		MonoObjectiveSearch geneticAlgorithm15 = new MonoObjectiveSearch(sc, c, sourceFiles[2], true, 100, 50, 0.2f, 0.8f);
		geneticAlgorithm15.setInitialRefactoringRange(50);
		searches.add(geneticAlgorithm15);
		
		MonoObjectiveSearch geneticAlgorithm16 = new MonoObjectiveSearch(sc, c, sourceFiles[3], true, 100, 50, 0.2f, 0.8f);
		geneticAlgorithm16.setInitialRefactoringRange(50);
		searches.add(geneticAlgorithm16);
		MonoObjectiveSearch geneticAlgorithm17 = new MonoObjectiveSearch(sc, c, sourceFiles[3], true, 100, 50, 0.2f, 0.8f);
		geneticAlgorithm17.setInitialRefactoringRange(50);
		searches.add(geneticAlgorithm17);
		MonoObjectiveSearch geneticAlgorithm18 = new MonoObjectiveSearch(sc, c, sourceFiles[3], true, 100, 50, 0.2f, 0.8f);
		geneticAlgorithm18.setInitialRefactoringRange(50);
		searches.add(geneticAlgorithm18);
		MonoObjectiveSearch geneticAlgorithm19 = new MonoObjectiveSearch(sc, c, sourceFiles[3], true, 100, 50, 0.2f, 0.8f);
		geneticAlgorithm19.setInitialRefactoringRange(50);
		searches.add(geneticAlgorithm19);
		MonoObjectiveSearch geneticAlgorithm20 = new MonoObjectiveSearch(sc, c, sourceFiles[3], true, 100, 50, 0.2f, 0.8f);
		geneticAlgorithm20.setInitialRefactoringRange(50);
		searches.add(geneticAlgorithm20);
		
		MonoObjectiveSearch geneticAlgorithm21 = new MonoObjectiveSearch(sc, c, sourceFiles[4], true, 100, 50, 0.2f, 0.8f);
		geneticAlgorithm21.setInitialRefactoringRange(50);
		searches.add(geneticAlgorithm21);
		MonoObjectiveSearch geneticAlgorithm22 = new MonoObjectiveSearch(sc, c, sourceFiles[4], true, 100, 50, 0.2f, 0.8f);
		geneticAlgorithm22.setInitialRefactoringRange(50);
		searches.add(geneticAlgorithm22);
		MonoObjectiveSearch geneticAlgorithm23 = new MonoObjectiveSearch(sc, c, sourceFiles[4], true, 100, 50, 0.2f, 0.8f);
		geneticAlgorithm23.setInitialRefactoringRange(50);
		searches.add(geneticAlgorithm23);
		MonoObjectiveSearch geneticAlgorithm24 = new MonoObjectiveSearch(sc, c, sourceFiles[4], true, 100, 50, 0.2f, 0.8f);
		geneticAlgorithm24.setInitialRefactoringRange(50);
		searches.add(geneticAlgorithm24);
		MonoObjectiveSearch geneticAlgorithm25 = new MonoObjectiveSearch(sc, c, sourceFiles[4], true, 100, 50, 0.2f, 0.8f);
		geneticAlgorithm25.setInitialRefactoringRange(50);
		searches.add(geneticAlgorithm25);
		
		String[] metricConfiguration = new String[]{
				"./configurations/classdesignsize.txt",
				"./configurations/numberofhierarchies.txt",
				"./configurations/averagenumberofancestors.txt",
				"./configurations/dataaccessmetric.txt",
				"./configSurations/directclasscoupling.txt",
				"./configurations/cohesionamongmethods.txt",
				"./configurations/aggregation.txt",
				"./configurations/functionalabstraction.txt",
				"./configurations/numberofpolymorphicmethods.txt",
				"./configurations/classinterfacesize.txt",
				"./configurations/numberofmethods.txt",
				"./configurations/weightedmethodsperclass.txt",
				"./configurations/numberofchildren.txt",
				"./configurations/abstractness.txt",
				"./configurations/abstractratio.txt",
				"./configurations/staticratio.txt",
				"./configurations/finalratio.txt",
				"./configurations/constantratio.txt",
				"./configurations/innerclassratio.txt",
				"./configurations/referencedmethodsratio.txt",
				"./configurations/visibilityratio.txt",
				"./configurations/linesofcode.txt",
				"./configurations/numberoffiles.txt"};
		
		// Create list of output directories for
		// each refactored project to be written to.
		String[] output = new String[metricConfiguration.length];
		
		// Create list of output directories for
		// each result data output to be written to.
		String[] resultsDir = new String[metricConfiguration.length];
		
		long timeTaken, startTime = System.currentTimeMillis();
		double time;
				
		for (int i = 0; i < metricConfiguration.length; i++)
		{
			output[i] = "./data/refactored/ToolExperiment/Part3/" + 
			            metricConfiguration[i].substring((metricConfiguration[i].lastIndexOf("/") + 1), metricConfiguration[i].lastIndexOf(".")) + "/";

			resultsDir[i] = "./results/ToolExperiment/Part3/" + 
					        metricConfiguration[i].substring((metricConfiguration[i].lastIndexOf("/") + 1), metricConfiguration[i].lastIndexOf(".")) + "/";

			for (int j = 0; j < searches.size(); j++)
			{
				// Creates new service configuration to start from scratch.
				sc = new CrossReferenceServiceConfiguration();
				int path = (int) Math.floor(j/5);
				
				String outputPath = output[i] + input[path].substring(input[path].lastIndexOf("/") + 1) + "/" + ((j % 5) + 1) + "/";
				String resultsPath = resultsDir[i] + input[path].substring(input[path].lastIndexOf("/") + 1) + "/" + ((j % 5) + 1) + "/";

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
				c = new Configuration(metricConfiguration[i], refactorings);
				searches.get(j).setConfiguration(c);
				searches.get(j).setServiceConfiguration(sc);
				searches.get(j).setResultsPath(resultsPath);
				searches.get(j).run();
				
				// Output overall time taken to console.
				timeTaken = System.currentTimeMillis() - startTime;
				time = timeTaken / 1000.0;
				System.out.printf("\r\n\r\nTime taken to run part 3 so far: %.2fs", time);
				System.out.printf("\n\r-----------------------------------------------");
				System.out.printf("\n\r-----------------------------------------------");
			}	
		}
	}
}