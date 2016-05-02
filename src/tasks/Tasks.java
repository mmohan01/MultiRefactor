package tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.Stack;

import recoder.CrossReferenceServiceConfiguration;
import recoder.ParserException;
import recoder.io.PropertyNames;
import refactorings.Refactoring;
import refactorings.field.*;
import refactorings.method.*;
import refactorings.type.*;
import refactory.Configuration;
import searches.*;

public class Tasks 
{
	String pathway = "./data/original/refactory";
	
	// No attributes - empty constructor.
	public Tasks(){}
	
	public Tasks(String pathway)
	{
		this.pathway = pathway;		
	}
	
	public void run()
	{				
		// Create an initial service configuration to be overwritten.
		// Reads the source code from the specified directory.
		CrossReferenceServiceConfiguration sc = new CrossReferenceServiceConfiguration();
		String[] sourceFiles = read(this.pathway);
		
		// Create empty list of refactorings.
		// Reads the metric configuration in from them to a specified text file.
		ArrayList<Refactoring> refactorings = new ArrayList<Refactoring>();
		Configuration c = new Configuration("configurations/allmetrics.txt", refactorings);
		Configuration[] cGA = {c};
		
		// Initialise search tasks.
		ArrayList<Search> searches = new ArrayList<Search>();
		RandomSearch random = new RandomSearch(sc, c);
		searches.add(random);
		HillClimbingSearch hillClimbing = new HillClimbingSearch(sc, c, 5, 10, true, 100);
		searches.add(hillClimbing);
		SimulatedAnnealingSearch simulatedAnnealing = new SimulatedAnnealingSearch(sc, c, 100, 4.0f, false);
		searches.add(simulatedAnnealing);
		GeneticAlgorithmSearch geneticAlgorithm = new GeneticAlgorithmSearch(sc, c, sourceFiles);
		searches.add(geneticAlgorithm);
		MultiObjectiveSearch MOGeneticAlgorithm = new MultiObjectiveSearch(sc, cGA, refactorings, sourceFiles);
		searches.add(MOGeneticAlgorithm);
				
		// Create list of output directories for
		// each refactored project to be written to.
		String[] output = new String[]{
		createOutputDirectory(this.pathway, "Random"),
		createOutputDirectory(this.pathway, "HillClimbing"),
		createOutputDirectory(this.pathway, "SimulatedAnnealing"),
		createOutputDirectory(this.pathway, "GeneticAlgorithm"),
		createOutputDirectory(this.pathway, "MOGeneticAlgorithm")};

		// Create list of output directories for
		// each result data output to be written to.
		String[] resultsDir = new String[]{
		"./results/" + this.pathway.substring(this.pathway.lastIndexOf("/") + 1) + "/Random/",
		"./results/" + this.pathway.substring(this.pathway.lastIndexOf("/") + 1) + "/HillClimbing/",
		"./results/" + this.pathway.substring(this.pathway.lastIndexOf("/") + 1) + "/SimulatedAnnealing/",
		"./results/" + this.pathway.substring(this.pathway.lastIndexOf("/") + 1) + "/GeneticAlgorithm/",
		"./results/" + this.pathway.substring(this.pathway.lastIndexOf("/") + 1) + "/MOGeneticAlgorithm/"};

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
			sc.getProjectSettings().setProperty(PropertyNames.INPUT_PATH, this.pathway + readLibs(this.pathway));
			sc.getProjectSettings().setProperty(PropertyNames.OUTPUT_PATH, output[i]);
			sc.getProjectSettings().ensureSystemClassesAreInPath();

			// initialise search task.			
			if (searches.get(i).getClass().getName().contains("MultiObjectiveSearch"))
			{
				c = new Configuration("configurations/allmetrics.txt");
				cGA = new Configuration[] {c};
				((MultiObjectiveSearch) searches.get(i)).setConfigurations(cGA);
				((MultiObjectiveSearch) searches.get(i)).setRefactorings(refactorings);
			}
			else
			{
				c = new Configuration("configurations/allmetrics.txt", refactorings);
				searches.get(i).setConfiguration(c);
			}
			
			searches.get(i).setServiceConfiguration(sc);
			searches.get(i).setResultsPath(resultsDir[i]);
			if (i == 0)
			searches.get(i).run();
		}	

		// Output overall time taken to console.
		timeTaken = System.currentTimeMillis() - startTime;
		time = timeTaken / 1000.0;
		System.out.printf("\r\n\r\nTime taken to run program: %.2fs", time);
	}
	
	// Returns an array of file paths representing the
	// project, found using the file pathway input.
	public String[] read(String pathName) 
	{
		File filePath = new File(pathName);
		if (!filePath.exists() || !filePath.isDirectory())
			throw new RuntimeException("\r\nPath to files does not exist.");

		Stack<File> dirs  = new Stack<File>();
		ArrayList<File> files = new ArrayList<File>();
		dirs.push(filePath);
		
		// Extracts only the java files from the
		// input and adds them to the file array.
		while (dirs.size() > 0) 
		{
			File dir = dirs.pop();
			File[] subfiles = dir.listFiles();
			
			for (File f : subfiles) 
			{
				if (f.isDirectory())
					dirs.push(f);
				else if (f.getName().endsWith(".java"))
					files.add(f);
			}
		}
		
		String[] fileList = new String[files.size()];

		for (int i = 0; i < files.size(); i++)
			fileList[i] = files.get(i).getAbsolutePath();

		return fileList;
	}
	
	// Returns the file paths that represent
	// the libraries present in the project.
	public String readLibs(String pathName) 
	{
		File filePath = new File(pathName);
		if (!filePath.exists() || !filePath.isDirectory())
			throw new RuntimeException("\r\nPath to files does not exist.");

		Stack<File> dirs  = new Stack<File>();
		ArrayList<File> files = new ArrayList<File>();
		dirs.push(filePath);

		// Extracts only the jar files from the
		// input and adds them to the file array.
		while (dirs.size() > 0) 
		{
			File dir = dirs.pop();
			File[] subfiles = dir.listFiles();

			for (File f : subfiles) 
			{
				if (f.isDirectory())
					dirs.push(f);
				else if (f.getName().endsWith(".jar")) 
					files.add(f);
			}
		}

		String jars = "";

		for (int i = 0; i < files.size(); i++)
			jars += File.pathSeparator + files.get(i).getAbsolutePath();

		return jars;
	}
	
	public String createOutputDirectory(String pathName, String name)
	{
		File filePath = new File(pathName);
		String output = "./data/refactored/" + filePath.getName() + "/" + name;	
		return output;
	}
}