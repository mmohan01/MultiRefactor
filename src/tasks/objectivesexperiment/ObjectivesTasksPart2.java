package tasks.objectivesexperiment;

import java.util.ArrayList;

import multirefactor.Configuration;
import recoder.CrossReferenceServiceConfiguration;
import recoder.ParserException;
import recoder.io.PropertyNames;
import refactorings.Refactoring;
import refactorings.field.*;
import refactorings.method.*;
import refactorings.type.*;
import searches.MultiObjectiveSearch;
import tasks.Tasks;

public class ObjectivesTasksPart2 extends Tasks
{
	// No attributes - empty constructor.
	public ObjectivesTasksPart2()
	{
		super();
	}

	public ObjectivesTasksPart2(String pathway)
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
		Configuration[] cFirst1 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/prioritybeaver-0.9.11.txt")};
		Configuration[] cFirst2 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/priorityapachexmlrpc-3.1.1.txt")};
		Configuration[] cFirst3 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/priorityjrdf-0.3.4.3.txt")};
		Configuration[] cFirst4 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/priorityganttproject-1.11.1.txt")};
		Configuration[] cFirst5 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/priorityjhotdraw-6.0b1.txt")};
		Configuration[] cFirst6 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/priorityxom-1.2.1.txt")};

		Configuration[] cSecond = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/diversity.txt")};

		Configuration[] cThird1 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/elementrecentnessbeaver.txt")};
		Configuration[] cThird2 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/elementrecentnessapachexmlrpc.txt")};
		Configuration[] cThird3 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/elementrecentnessjrdf.txt")};
		Configuration[] cThird4 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/elementrecentnessganttproject.txt")};
		Configuration[] cThird5 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/elementrecentnessjhotdraw.txt")};
		Configuration[] cThird6 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/elementrecentnessxom.txt")};

		Configuration[] cFourth1 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/prioritybeaver-0.9.11.txt"), 
									new Configuration("./configurations/diversity.txt")};
		Configuration[] cFourth2 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/priorityapachexmlrpc-3.1.1.txt"), 
									new Configuration("./configurations/diversity.txt")};
		Configuration[] cFourth3 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/priorityjrdf-0.3.4.3.txt"), 
									new Configuration("./configurations/diversity.txt")};
		Configuration[] cFourth4 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/priorityganttproject-1.11.1.txt"), 
									new Configuration("./configurations/diversity.txt")};
		Configuration[] cFourth5 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/priorityjhotdraw-6.0b1.txt"), 
									new Configuration("./configurations/diversity.txt")};
		Configuration[] cFourth6 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/priorityxom-1.2.1.txt"), 
									new Configuration("./configurations/diversity.txt")};

		Configuration[] cFifth1 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/prioritybeaver-0.9.11.txt"), 
								   new Configuration("./configurations/elementrecentnessbeaver.txt")};
		Configuration[] cFifth2 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/priorityapachexmlrpc-3.1.1.txt"), 
								   new Configuration("./configurations/elementrecentnessapachexmlrpc.txt")};
		Configuration[] cFifth3 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/priorityjrdf-0.3.4.3.txt"), 
								   new Configuration("./configurations/elementrecentnessjrdf.txt")};
		Configuration[] cFifth4 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/priorityganttproject-1.11.1.txt"), 
								   new Configuration("./configurations/elementrecentnessganttproject.txt")};
		Configuration[] cFifth5 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/priorityjhotdraw-6.0b1.txt"), 
								   new Configuration("./configurations/elementrecentnessjhotdraw.txt")};
		Configuration[] cFifth6 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/priorityxom-1.2.1.txt"), 
								   new Configuration("./configurations/elementrecentnessxom.txt")};

		Configuration[] cSixth1 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/diversity.txt"), 
								   new Configuration("./configurations/elementrecentnessbeaver.txt")};
		Configuration[] cSixth2 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/diversity.txt"), 
								   new Configuration("./configurations/elementrecentnessapachexmlrpc.txt")};
		Configuration[] cSixth3 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/diversity.txt"), 
								   new Configuration("./configurations/elementrecentnessjrdf.txt")};
		Configuration[] cSixth4 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/diversity.txt"), 
								   new Configuration("./configurations/elementrecentnessganttproject.txt")};
		Configuration[] cSixth5 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/diversity.txt"), 
								   new Configuration("./configurations/elementrecentnessjhotdraw.txt")};
		Configuration[] cSixth6 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/diversity.txt"), 
								   new Configuration("./configurations/elementrecentnessxom.txt")};

		ArrayList<MultiObjectiveSearch> searches = new ArrayList<MultiObjectiveSearch>();
		searches.add(new MultiObjectiveSearch(sc, cFirst1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFirst1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFirst1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFirst1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFirst1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f));
		
		searches.add(new MultiObjectiveSearch(sc, cFirst2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFirst2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFirst2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFirst2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFirst2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f));
		
		searches.add(new MultiObjectiveSearch(sc, cFirst3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFirst3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFirst3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFirst3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFirst3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f));
		
		searches.add(new MultiObjectiveSearch(sc, cFirst4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFirst4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFirst4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFirst4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFirst4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f));
		
		searches.add(new MultiObjectiveSearch(sc, cFirst5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFirst5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFirst5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFirst5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFirst5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f));
		
		searches.add(new MultiObjectiveSearch(sc, cFirst6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFirst6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFirst6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFirst6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFirst6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f));
		
		
		searches.add(new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f));
		
		searches.add(new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f));
		
		searches.add(new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f));
		
		searches.add(new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f));
		
		searches.add(new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f));
		
		searches.add(new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f));
		
		
		searches.add(new MultiObjectiveSearch(sc, cThird1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cThird1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cThird1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cThird1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cThird1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f));
		
		searches.add(new MultiObjectiveSearch(sc, cThird2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cThird2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cThird2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cThird2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cThird2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f));
		
		searches.add(new MultiObjectiveSearch(sc, cThird3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cThird3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cThird3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cThird3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cThird3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f));
		
		searches.add(new MultiObjectiveSearch(sc, cThird4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cThird4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cThird4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cThird4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cThird4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f));
		
		searches.add(new MultiObjectiveSearch(sc, cThird5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cThird5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cThird5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cThird5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cThird5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f));
		
		searches.add(new MultiObjectiveSearch(sc, cThird6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cThird6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cThird6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cThird6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cThird6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f));
		
		

		searches.add(new MultiObjectiveSearch(sc, cFourth1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFourth1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFourth1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFourth1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFourth1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f));
		
		searches.add(new MultiObjectiveSearch(sc, cFourth2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFourth2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFourth2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFourth2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFourth2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f));
		
		searches.add(new MultiObjectiveSearch(sc, cFourth3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFourth3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFourth3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFourth3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFourth3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f));
		
		searches.add(new MultiObjectiveSearch(sc, cFourth4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFourth4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFourth4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFourth4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFourth4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f));
		
		searches.add(new MultiObjectiveSearch(sc, cFourth5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFourth5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFourth5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFourth5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFourth5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f));
		
		searches.add(new MultiObjectiveSearch(sc, cFourth6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFourth6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFourth6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFourth6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFourth6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f));
		
		searches.add(new MultiObjectiveSearch(sc, cFifth1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFifth1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFifth1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFifth1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFifth1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f));
		
		searches.add(new MultiObjectiveSearch(sc, cFifth2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFifth2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFifth2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFifth2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFifth2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f));
		
		searches.add(new MultiObjectiveSearch(sc, cFifth3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFifth3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFifth3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFifth3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFifth3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f));
		
		searches.add(new MultiObjectiveSearch(sc, cFifth4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFifth4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFifth4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFifth4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFifth4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f));
		
		searches.add(new MultiObjectiveSearch(sc, cFifth5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFifth5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFifth5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFifth5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFifth5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f));
		
		searches.add(new MultiObjectiveSearch(sc, cFifth6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFifth6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFifth6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFifth6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cFifth6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f));
		
		searches.add(new MultiObjectiveSearch(sc, cSixth1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSixth1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSixth1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSixth1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSixth1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f));

		searches.add(new MultiObjectiveSearch(sc, cSixth2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSixth2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSixth2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSixth2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSixth2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f));

		searches.add(new MultiObjectiveSearch(sc, cSixth3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSixth3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSixth3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSixth3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSixth3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f));
		
		searches.add(new MultiObjectiveSearch(sc, cSixth4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSixth4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSixth4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSixth4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSixth4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f));
		
		searches.add(new MultiObjectiveSearch(sc, cSixth5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSixth5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSixth5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSixth5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSixth5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f));
		
		searches.add(new MultiObjectiveSearch(sc, cSixth6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSixth6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSixth6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSixth6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f));
		searches.add(new MultiObjectiveSearch(sc, cSixth6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f));
		
		// Create list of output directories for
		// each refactored project to be written to.	
		String[] outputDir = new String[]{
				"./data/refactored/ObjectivesExperiment/Part2/First/",
				"./data/refactored/ObjectivesExperiment/Part2/Second/",
				"./data/refactored/ObjectivesExperiment/Part2/Third/",
				"./data/refactored/ObjectivesExperiment/Part2/Fourth/",
				"./data/refactored/ObjectivesExperiment/Part2/Fifth/",
				"./data/refactored/ObjectivesExperiment/Part2/Sixth/"};

		// Create list of output directories for
		// each result data output to be written to.
		String[] resultsDir = new String[]{
				"./results/ObjectivesExperiment/Part2/First/",
				"./results/ObjectivesExperiment/Part2/Second/",
				"./results/ObjectivesExperiment/Part2/Third/",
				"./results/ObjectivesExperiment/Part2/Fourth/",
				"./results/ObjectivesExperiment/Part2/Fifth/",
				"./results/ObjectivesExperiment/Part2/Sixth/"};	

		long timeTaken, startTime = System.currentTimeMillis();
		double time;
		
		for (int i = 0; i < searches.size(); i++)
		{
			// Creates new service configuration to start from scratch.
			sc = new CrossReferenceServiceConfiguration();
			int search = (int) Math.floor(i / 30);
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

			// initialise and run search task.			
			searches.get(i).setRefactorings(refactorings);
			searches.get(i).setServiceConfiguration(sc);
			searches.get(i).setResultsPath(resultsPath);
			searches.get(i).setTopSolutionFlag(false);
			searches.get(i).run();
			searches.set(i, null);
		}	

		// Output overall time taken to console.
		timeTaken = System.currentTimeMillis() - startTime;
		time = timeTaken / 1000.0;
		System.out.printf("\r\n\r\nTime taken to run program: %.2fs", time);
	}
}