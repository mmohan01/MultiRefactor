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
//		Configuration[] cFirst1 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/prioritybeaver-0.9.11.txt")};
//		Configuration[] cFirst2 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/priorityapachexmlrpc-3.1.1.txt")};
//		Configuration[] cFirst3 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/priorityjrdf-0.3.4.3.txt")};
//		Configuration[] cFirst4 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/priorityganttproject-1.11.1.txt")};
//		Configuration[] cFirst5 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/priorityjhotdraw-6.0b1.txt")};
//		Configuration[] cFirst6 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/priorityxom-1.2.1.txt")};
//
//		Configuration[] cSecond = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/diversity.txt")};
//
//		Configuration[] cThird1 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/elementrecentnessbeaver.txt")};
//		Configuration[] cThird2 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/elementrecentnessapachexmlrpc.txt")};
//		Configuration[] cThird3 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/elementrecentnessjrdf.txt")};
//		Configuration[] cThird4 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/elementrecentnessganttproject.txt")};
//		Configuration[] cThird5 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/elementrecentnessjhotdraw.txt")};
//		Configuration[] cThird6 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/elementrecentnessxom.txt")};

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
//		MultiObjectiveSearch first1 = new MultiObjectiveSearch(sc, cFirst1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
//		first1.setTopSolutionFlag(false);
//		searches.add(first1);
//		MultiObjectiveSearch first2 = new MultiObjectiveSearch(sc, cFirst1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
//		first2.setTopSolutionFlag(false);
//		searches.add(first2);
//		MultiObjectiveSearch first3 = new MultiObjectiveSearch(sc, cFirst1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
//		first3.setTopSolutionFlag(false);
//		searches.add(first3);
//		MultiObjectiveSearch first4 = new MultiObjectiveSearch(sc, cFirst1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
//		first4.setTopSolutionFlag(false);
//		searches.add(first4);
//		MultiObjectiveSearch first5 = new MultiObjectiveSearch(sc, cFirst1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
//		first5.setTopSolutionFlag(false);
//		searches.add(first5);
//		
//		MultiObjectiveSearch first6 = new MultiObjectiveSearch(sc, cFirst2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
//		first6.setTopSolutionFlag(false);
//		searches.add(first6);
//		MultiObjectiveSearch first7 = new MultiObjectiveSearch(sc, cFirst2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
//		first7.setTopSolutionFlag(false);
//		searches.add(first7);
//		MultiObjectiveSearch first8 = new MultiObjectiveSearch(sc, cFirst2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
//		first8.setTopSolutionFlag(false);
//		searches.add(first8);
//		MultiObjectiveSearch first9 = new MultiObjectiveSearch(sc, cFirst2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
//		first9.setTopSolutionFlag(false);
//		searches.add(first9);
//		MultiObjectiveSearch first10 = new MultiObjectiveSearch(sc, cFirst2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
//		first10.setTopSolutionFlag(false);
//		searches.add(first10);
//		
//		MultiObjectiveSearch first11 = new MultiObjectiveSearch(sc, cFirst3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
//		first11.setTopSolutionFlag(false);
//		searches.add(first11);
//		MultiObjectiveSearch first12 = new MultiObjectiveSearch(sc, cFirst3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
//		first12.setTopSolutionFlag(false);
//		searches.add(first12);
//		MultiObjectiveSearch first13 = new MultiObjectiveSearch(sc, cFirst3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
//		first13.setTopSolutionFlag(false);
//		searches.add(first13);
//		MultiObjectiveSearch first14 = new MultiObjectiveSearch(sc, cFirst3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
//		first14.setTopSolutionFlag(false);
//		searches.add(first14);
//		MultiObjectiveSearch first15 = new MultiObjectiveSearch(sc, cFirst3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
//		first15.setTopSolutionFlag(false);
//		searches.add(first15);
//		
//		MultiObjectiveSearch first16 = new MultiObjectiveSearch(sc, cFirst4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
//		first16.setTopSolutionFlag(false);
//		searches.add(first16);
//		MultiObjectiveSearch first17 = new MultiObjectiveSearch(sc, cFirst4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
//		first17.setTopSolutionFlag(false);
//		searches.add(first17);
//		MultiObjectiveSearch first18 = new MultiObjectiveSearch(sc, cFirst4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
//		first18.setTopSolutionFlag(false);
//		searches.add(first18);
//		MultiObjectiveSearch first19 = new MultiObjectiveSearch(sc, cFirst4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
//		first19.setTopSolutionFlag(false);
//		searches.add(first19);
//		MultiObjectiveSearch first20 = new MultiObjectiveSearch(sc, cFirst4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
//		first20.setTopSolutionFlag(false);
//		searches.add(first20);
//		
//		MultiObjectiveSearch first21 = new MultiObjectiveSearch(sc, cFirst5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
//		first21.setTopSolutionFlag(false);
//		searches.add(first21);
//		MultiObjectiveSearch first22 = new MultiObjectiveSearch(sc, cFirst5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
//		first22.setTopSolutionFlag(false);
//		searches.add(first22);
//		MultiObjectiveSearch first23 = new MultiObjectiveSearch(sc, cFirst5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
//		first23.setTopSolutionFlag(false);
//		searches.add(first23);
//		MultiObjectiveSearch first24 = new MultiObjectiveSearch(sc, cFirst5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
//		first24.setTopSolutionFlag(false);
//		searches.add(first24);
//		MultiObjectiveSearch first25 = new MultiObjectiveSearch(sc, cFirst5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
//		first25.setTopSolutionFlag(false);
//		searches.add(first25);
//		
//		MultiObjectiveSearch first26 = new MultiObjectiveSearch(sc, cFirst6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
//		first26.setTopSolutionFlag(false);
//		searches.add(first26);
//		MultiObjectiveSearch first27 = new MultiObjectiveSearch(sc, cFirst6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
//		first27.setTopSolutionFlag(false);
//		searches.add(first27);
//		MultiObjectiveSearch first28 = new MultiObjectiveSearch(sc, cFirst6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
//		first28.setTopSolutionFlag(false);
//		searches.add(first28);
//		MultiObjectiveSearch first29 = new MultiObjectiveSearch(sc, cFirst6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
//		first29.setTopSolutionFlag(false);
//		searches.add(first29);
//		MultiObjectiveSearch first30 = new MultiObjectiveSearch(sc, cFirst6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
//		first30.setTopSolutionFlag(false);
//		searches.add(first30);
//		
//		
//		MultiObjectiveSearch second1 = new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
//		second1.setTopSolutionFlag(false);
//		searches.add(second1);
//		MultiObjectiveSearch second2 = new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
//		second2.setTopSolutionFlag(false);
//		searches.add(second2);
//		MultiObjectiveSearch second3 = new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
//		second3.setTopSolutionFlag(false);
//		searches.add(second3);
//		MultiObjectiveSearch second4 = new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
//		second4.setTopSolutionFlag(false);
//		searches.add(second4);
//		MultiObjectiveSearch second5 = new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
//		second5.setTopSolutionFlag(false);
//		searches.add(second5);
//		
//		MultiObjectiveSearch second6 = new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
//		second6.setTopSolutionFlag(false);
//		searches.add(second6);
//		MultiObjectiveSearch second7 = new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
//		second7.setTopSolutionFlag(false);
//		searches.add(second7);
//		MultiObjectiveSearch second8 = new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
//		second8.setTopSolutionFlag(false);
//		searches.add(second8);
//		MultiObjectiveSearch second9 = new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
//		second9.setTopSolutionFlag(false);
//		searches.add(second9);
//		MultiObjectiveSearch second10 = new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
//		second10.setTopSolutionFlag(false);
//		searches.add(second10);
//		
//		MultiObjectiveSearch second11 = new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
//		second11.setTopSolutionFlag(false);
//		searches.add(second11);
//		MultiObjectiveSearch second12 = new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
//		second12.setTopSolutionFlag(false);
//		searches.add(second12);
//		MultiObjectiveSearch second13 = new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
//		second13.setTopSolutionFlag(false);
//		searches.add(second13);
//		MultiObjectiveSearch second14 = new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
//		second14.setTopSolutionFlag(false);
//		searches.add(second14);
//		MultiObjectiveSearch second15 = new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
//		second15.setTopSolutionFlag(false);
//		searches.add(second15);
//		
//		MultiObjectiveSearch second16 = new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
//		second16.setTopSolutionFlag(false);
//		searches.add(second16);
//		MultiObjectiveSearch second17 = new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
//		second17.setTopSolutionFlag(false);
//		searches.add(second17);
//		MultiObjectiveSearch second18 = new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
//		second18.setTopSolutionFlag(false);
//		searches.add(second18);
//		MultiObjectiveSearch second19 = new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
//		second19.setTopSolutionFlag(false);
//		searches.add(second19);
//		MultiObjectiveSearch second20 = new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
//		second20.setTopSolutionFlag(false);
//		searches.add(second20);
//		
//		MultiObjectiveSearch second21 = new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
//		second21.setTopSolutionFlag(false);
//		searches.add(second21);
//		MultiObjectiveSearch second22 = new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
//		second22.setTopSolutionFlag(false);
//		searches.add(second22);
//		MultiObjectiveSearch second23 = new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
//		second23.setTopSolutionFlag(false);
//		searches.add(second23);
//		MultiObjectiveSearch second24 = new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
//		second24.setTopSolutionFlag(false);
//		searches.add(second24);
//		MultiObjectiveSearch second25 = new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
//		second25.setTopSolutionFlag(false);
//		searches.add(second25);
//		
//		MultiObjectiveSearch second26 = new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
//		second26.setTopSolutionFlag(false);
//		searches.add(second26);
//		MultiObjectiveSearch second27 = new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
//		second27.setTopSolutionFlag(false);
//		searches.add(second27);
//		MultiObjectiveSearch second28 = new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
//		second28.setTopSolutionFlag(false);
//		searches.add(second28);
//		MultiObjectiveSearch second29 = new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
//		second29.setTopSolutionFlag(false);
//		searches.add(second29);
//		MultiObjectiveSearch second30 = new MultiObjectiveSearch(sc, cSecond, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
//		second30.setTopSolutionFlag(false);
//		searches.add(second30);
//		
//		
//		MultiObjectiveSearch third1 = new MultiObjectiveSearch(sc, cThird1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
//		third1.setTopSolutionFlag(false);
//		searches.add(third1);
//		MultiObjectiveSearch third2 = new MultiObjectiveSearch(sc, cThird1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
//		third2.setTopSolutionFlag(false);
//		searches.add(third2);
//		MultiObjectiveSearch third3 = new MultiObjectiveSearch(sc, cThird1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
//		third3.setTopSolutionFlag(false);
//		searches.add(third3);
//		MultiObjectiveSearch third4 = new MultiObjectiveSearch(sc, cThird1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
//		third4.setTopSolutionFlag(false);
//		searches.add(third4);
//		MultiObjectiveSearch third5 = new MultiObjectiveSearch(sc, cThird1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
//		third5.setTopSolutionFlag(false);
//		searches.add(third5);
//		
//		MultiObjectiveSearch third6 = new MultiObjectiveSearch(sc, cThird2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
//		third6.setTopSolutionFlag(false);
//		searches.add(third6);
//		MultiObjectiveSearch third7 = new MultiObjectiveSearch(sc, cThird2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
//		third7.setTopSolutionFlag(false);
//		searches.add(third7);
//		MultiObjectiveSearch third8 = new MultiObjectiveSearch(sc, cThird2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
//		third8.setTopSolutionFlag(false);
//		searches.add(third8);
//		MultiObjectiveSearch third9 = new MultiObjectiveSearch(sc, cThird2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
//		third9.setTopSolutionFlag(false);
//		searches.add(third9);
//		MultiObjectiveSearch third10 = new MultiObjectiveSearch(sc, cThird2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
//		third10.setTopSolutionFlag(false);
//		searches.add(third10);
//		
//		MultiObjectiveSearch third11 = new MultiObjectiveSearch(sc, cThird3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
//		third11.setTopSolutionFlag(false);
//		searches.add(third11);
//		MultiObjectiveSearch third12 = new MultiObjectiveSearch(sc, cThird3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
//		third12.setTopSolutionFlag(false);
//		searches.add(third12);
//		MultiObjectiveSearch third13 = new MultiObjectiveSearch(sc, cThird3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
//		third13.setTopSolutionFlag(false);
//		searches.add(third13);
//		MultiObjectiveSearch third14 = new MultiObjectiveSearch(sc, cThird3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
//		third14.setTopSolutionFlag(false);
//		searches.add(third14);
//		MultiObjectiveSearch third15 = new MultiObjectiveSearch(sc, cThird3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
//		third15.setTopSolutionFlag(false);
//		searches.add(third15);
//		
//		MultiObjectiveSearch third16 = new MultiObjectiveSearch(sc, cThird4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
//		third16.setTopSolutionFlag(false);
//		searches.add(third16);
//		MultiObjectiveSearch third17 = new MultiObjectiveSearch(sc, cThird4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
//		third17.setTopSolutionFlag(false);
//		searches.add(third17);
//		MultiObjectiveSearch third18 = new MultiObjectiveSearch(sc, cThird4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
//		third18.setTopSolutionFlag(false);
//		searches.add(third18);
//		MultiObjectiveSearch third19 = new MultiObjectiveSearch(sc, cThird4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
//		third19.setTopSolutionFlag(false);
//		searches.add(third19);
//		MultiObjectiveSearch third20 = new MultiObjectiveSearch(sc, cThird4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
//		third20.setTopSolutionFlag(false);
//		searches.add(third20);
//		
//		MultiObjectiveSearch third21 = new MultiObjectiveSearch(sc, cThird5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
//		third21.setTopSolutionFlag(false);
//		searches.add(third21);
//		MultiObjectiveSearch third22 = new MultiObjectiveSearch(sc, cThird5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
//		third22.setTopSolutionFlag(false);
//		searches.add(third22);
//		MultiObjectiveSearch third23 = new MultiObjectiveSearch(sc, cThird5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
//		third23.setTopSolutionFlag(false);
//		searches.add(third23);
//		MultiObjectiveSearch third24 = new MultiObjectiveSearch(sc, cThird5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
//		third24.setTopSolutionFlag(false);
//		searches.add(third24);
//		MultiObjectiveSearch third25 = new MultiObjectiveSearch(sc, cThird5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
//		third25.setTopSolutionFlag(false);
//		searches.add(third25);
//		
//		MultiObjectiveSearch third26 = new MultiObjectiveSearch(sc, cThird6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
//		third26.setTopSolutionFlag(false);
//		searches.add(third26);
//		MultiObjectiveSearch third27 = new MultiObjectiveSearch(sc, cThird6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
//		third27.setTopSolutionFlag(false);
//		searches.add(third27);
//		MultiObjectiveSearch third28 = new MultiObjectiveSearch(sc, cThird6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
//		third28.setTopSolutionFlag(false);
//		searches.add(third28);
//		MultiObjectiveSearch third29 = new MultiObjectiveSearch(sc, cThird6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
//		third29.setTopSolutionFlag(false);
//		searches.add(third29);
//		MultiObjectiveSearch third30 = new MultiObjectiveSearch(sc, cThird6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
//		third30.setTopSolutionFlag(false);
//		searches.add(third30);
		
		
		MultiObjectiveSearch fourth1 = new MultiObjectiveSearch(sc, cFourth1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
		fourth1.setTopSolutionFlag(false);
		searches.add(fourth1);
		MultiObjectiveSearch fourth2 = new MultiObjectiveSearch(sc, cFourth1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
		fourth2.setTopSolutionFlag(false);
		searches.add(fourth2);
		MultiObjectiveSearch fourth3 = new MultiObjectiveSearch(sc, cFourth1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
		fourth3.setTopSolutionFlag(false);
		searches.add(fourth3);
		MultiObjectiveSearch fourth4 = new MultiObjectiveSearch(sc, cFourth1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
		fourth4.setTopSolutionFlag(false);
		searches.add(fourth4);
		MultiObjectiveSearch fourth5 = new MultiObjectiveSearch(sc, cFourth1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
		fourth5.setTopSolutionFlag(false);
		searches.add(fourth5);
		
		MultiObjectiveSearch fourth6 = new MultiObjectiveSearch(sc, cFourth2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
		fourth6.setTopSolutionFlag(false);
		searches.add(fourth6);
		MultiObjectiveSearch fourth7 = new MultiObjectiveSearch(sc, cFourth2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
		fourth7.setTopSolutionFlag(false);
		searches.add(fourth7);
		MultiObjectiveSearch fourth8 = new MultiObjectiveSearch(sc, cFourth2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
		fourth8.setTopSolutionFlag(false);
		searches.add(fourth8);
		MultiObjectiveSearch fourth9 = new MultiObjectiveSearch(sc, cFourth2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
		fourth9.setTopSolutionFlag(false);
		searches.add(fourth9);
		MultiObjectiveSearch fourth10 = new MultiObjectiveSearch(sc, cFourth2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
		fourth10.setTopSolutionFlag(false);
		searches.add(fourth10);
		
		MultiObjectiveSearch fourth11 = new MultiObjectiveSearch(sc, cFourth3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
		fourth11.setTopSolutionFlag(false);
		searches.add(fourth11);
		MultiObjectiveSearch fourth12 = new MultiObjectiveSearch(sc, cFourth3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
		fourth12.setTopSolutionFlag(false);
		searches.add(fourth12);
		MultiObjectiveSearch fourth13 = new MultiObjectiveSearch(sc, cFourth3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
		fourth13.setTopSolutionFlag(false);
		searches.add(fourth13);
		MultiObjectiveSearch fourth14 = new MultiObjectiveSearch(sc, cFourth3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
		fourth14.setTopSolutionFlag(false);
		searches.add(fourth14);
		MultiObjectiveSearch fourth15 = new MultiObjectiveSearch(sc, cFourth3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
		fourth15.setTopSolutionFlag(false);
		searches.add(fourth15);
		
		MultiObjectiveSearch fourth16 = new MultiObjectiveSearch(sc, cFourth4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
		fourth16.setTopSolutionFlag(false);
		searches.add(fourth16);
		MultiObjectiveSearch fourth17 = new MultiObjectiveSearch(sc, cFourth4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
		fourth17.setTopSolutionFlag(false);
		searches.add(fourth17);
		MultiObjectiveSearch fourth18 = new MultiObjectiveSearch(sc, cFourth4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
		fourth18.setTopSolutionFlag(false);
		searches.add(fourth18);
		MultiObjectiveSearch fourth19 = new MultiObjectiveSearch(sc, cFourth4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
		fourth19.setTopSolutionFlag(false);
		searches.add(fourth19);
		MultiObjectiveSearch fourth20 = new MultiObjectiveSearch(sc, cFourth4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
		fourth20.setTopSolutionFlag(false);
		searches.add(fourth20);
		
		MultiObjectiveSearch fourth21 = new MultiObjectiveSearch(sc, cFourth5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
		fourth21.setTopSolutionFlag(false);
		searches.add(fourth21);
		MultiObjectiveSearch fourth22 = new MultiObjectiveSearch(sc, cFourth5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
		fourth22.setTopSolutionFlag(false);
		searches.add(fourth22);
		MultiObjectiveSearch fourth23 = new MultiObjectiveSearch(sc, cFourth5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
		fourth23.setTopSolutionFlag(false);
		searches.add(fourth23);
		MultiObjectiveSearch fourth24 = new MultiObjectiveSearch(sc, cFourth5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
		fourth24.setTopSolutionFlag(false);
		searches.add(fourth24);
		MultiObjectiveSearch fourth25 = new MultiObjectiveSearch(sc, cFourth5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
		fourth25.setTopSolutionFlag(false);
		searches.add(fourth25);
		
		MultiObjectiveSearch fourth26 = new MultiObjectiveSearch(sc, cFourth6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
		fourth26.setTopSolutionFlag(false);
		searches.add(fourth26);
		MultiObjectiveSearch fourth27 = new MultiObjectiveSearch(sc, cFourth6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
		fourth27.setTopSolutionFlag(false);
		searches.add(fourth27);
		MultiObjectiveSearch fourth28 = new MultiObjectiveSearch(sc, cFourth6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
		fourth28.setTopSolutionFlag(false);
		searches.add(fourth28);
		MultiObjectiveSearch fourth29 = new MultiObjectiveSearch(sc, cFourth6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
		fourth29.setTopSolutionFlag(false);
		searches.add(fourth29);
		MultiObjectiveSearch fourth30 = new MultiObjectiveSearch(sc, cFourth6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
		fourth30.setTopSolutionFlag(false);
		searches.add(fourth30);
		
		
		MultiObjectiveSearch fifth1 = new MultiObjectiveSearch(sc, cFifth1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
		fifth1.setTopSolutionFlag(false);
		searches.add(fifth1);
		MultiObjectiveSearch fifth2 = new MultiObjectiveSearch(sc, cFifth1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
		fifth2.setTopSolutionFlag(false);
		searches.add(fifth2);
		MultiObjectiveSearch fifth3 = new MultiObjectiveSearch(sc, cFifth1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
		fifth3.setTopSolutionFlag(false);
		searches.add(fifth3);
		MultiObjectiveSearch fifth4 = new MultiObjectiveSearch(sc, cFifth1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
		fifth4.setTopSolutionFlag(false);
		searches.add(fifth4);
		MultiObjectiveSearch fifth5 = new MultiObjectiveSearch(sc, cFifth1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
		fifth5.setTopSolutionFlag(false);
		searches.add(fifth5);
		
		MultiObjectiveSearch fifth6 = new MultiObjectiveSearch(sc, cFifth2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
		fifth6.setTopSolutionFlag(false);
		searches.add(fifth6);
		MultiObjectiveSearch fifth7 = new MultiObjectiveSearch(sc, cFifth2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
		fifth7.setTopSolutionFlag(false);
		searches.add(fifth7);
		MultiObjectiveSearch fifth8 = new MultiObjectiveSearch(sc, cFifth2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
		fifth8.setTopSolutionFlag(false);
		searches.add(fifth8);
		MultiObjectiveSearch fifth9 = new MultiObjectiveSearch(sc, cFifth2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
		fifth9.setTopSolutionFlag(false);
		searches.add(fifth9);
		MultiObjectiveSearch fifth10 = new MultiObjectiveSearch(sc, cFifth2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
		fifth10.setTopSolutionFlag(false);
		searches.add(fifth10);
		
		MultiObjectiveSearch fifth11 = new MultiObjectiveSearch(sc, cFifth3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
		fifth11.setTopSolutionFlag(false);
		searches.add(fifth11);
		MultiObjectiveSearch fifth12 = new MultiObjectiveSearch(sc, cFifth3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
		fifth12.setTopSolutionFlag(false);
		searches.add(fifth12);
		MultiObjectiveSearch fifth13 = new MultiObjectiveSearch(sc, cFifth3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
		fifth13.setTopSolutionFlag(false);
		searches.add(fifth13);
		MultiObjectiveSearch fifth14 = new MultiObjectiveSearch(sc, cFifth3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
		fifth14.setTopSolutionFlag(false);
		searches.add(fifth14);
		MultiObjectiveSearch fifth15 = new MultiObjectiveSearch(sc, cFifth3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
		fifth15.setTopSolutionFlag(false);
		searches.add(fifth15);
		
		MultiObjectiveSearch fifth16 = new MultiObjectiveSearch(sc, cFifth4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
		fifth16.setTopSolutionFlag(false);
		searches.add(fifth16);
		MultiObjectiveSearch fifth17 = new MultiObjectiveSearch(sc, cFifth4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
		fifth17.setTopSolutionFlag(false);
		searches.add(fifth17);
		MultiObjectiveSearch fifth18 = new MultiObjectiveSearch(sc, cFifth4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
		fifth18.setTopSolutionFlag(false);
		searches.add(fifth18);
		MultiObjectiveSearch fifth19 = new MultiObjectiveSearch(sc, cFifth4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
		fifth19.setTopSolutionFlag(false);
		searches.add(fifth19);
		MultiObjectiveSearch fifth20 = new MultiObjectiveSearch(sc, cFifth4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
		fifth20.setTopSolutionFlag(false);
		searches.add(fifth20);
		
		MultiObjectiveSearch fifth21 = new MultiObjectiveSearch(sc, cFifth5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
		fifth21.setTopSolutionFlag(false);
		searches.add(fifth21);
		MultiObjectiveSearch fifth22 = new MultiObjectiveSearch(sc, cFifth5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
		fifth22.setTopSolutionFlag(false);
		searches.add(fifth22);
		MultiObjectiveSearch fifth23 = new MultiObjectiveSearch(sc, cFifth5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
		fifth23.setTopSolutionFlag(false);
		searches.add(fifth23);
		MultiObjectiveSearch fifth24 = new MultiObjectiveSearch(sc, cFifth5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
		fifth24.setTopSolutionFlag(false);
		searches.add(fifth24);
		MultiObjectiveSearch fifth25 = new MultiObjectiveSearch(sc, cFifth5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
		fifth25.setTopSolutionFlag(false);
		searches.add(fifth25);
		
		MultiObjectiveSearch fifth26 = new MultiObjectiveSearch(sc, cFifth6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
		fifth26.setTopSolutionFlag(false);
		searches.add(fifth26);
		MultiObjectiveSearch fifth27 = new MultiObjectiveSearch(sc, cFifth6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
		fifth27.setTopSolutionFlag(false);
		searches.add(fifth27);
		MultiObjectiveSearch fifth28 = new MultiObjectiveSearch(sc, cFifth6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
		fifth28.setTopSolutionFlag(false);
		searches.add(fifth28);
		MultiObjectiveSearch fifth29 = new MultiObjectiveSearch(sc, cFifth6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
		fifth29.setTopSolutionFlag(false);
		searches.add(fifth29);
		MultiObjectiveSearch fifth30 = new MultiObjectiveSearch(sc, cFifth6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
		fifth30.setTopSolutionFlag(false);
		searches.add(fifth30);
		
		
		MultiObjectiveSearch sixth1 = new MultiObjectiveSearch(sc, cSixth1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
		sixth1.setTopSolutionFlag(false);
		searches.add(sixth1);
		MultiObjectiveSearch sixth2 = new MultiObjectiveSearch(sc, cSixth1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
		sixth2.setTopSolutionFlag(false);
		searches.add(sixth2);
		MultiObjectiveSearch sixth3 = new MultiObjectiveSearch(sc, cSixth1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
		sixth3.setTopSolutionFlag(false);
		searches.add(sixth3);
		MultiObjectiveSearch sixth4 = new MultiObjectiveSearch(sc, cSixth1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
		sixth4.setTopSolutionFlag(false);
		searches.add(sixth4);
		MultiObjectiveSearch sixth5 = new MultiObjectiveSearch(sc, cSixth1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
		sixth5.setTopSolutionFlag(false);
		searches.add(sixth5);
		
		MultiObjectiveSearch sixth6 = new MultiObjectiveSearch(sc, cSixth2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
		sixth6.setTopSolutionFlag(false);
		searches.add(sixth6);
		MultiObjectiveSearch sixth7 = new MultiObjectiveSearch(sc, cSixth2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
		sixth7.setTopSolutionFlag(false);
		searches.add(sixth7);
		MultiObjectiveSearch sixth8 = new MultiObjectiveSearch(sc, cSixth2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
		sixth8.setTopSolutionFlag(false);
		searches.add(sixth8);
		MultiObjectiveSearch sixth9 = new MultiObjectiveSearch(sc, cSixth2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
		sixth9.setTopSolutionFlag(false);
		searches.add(sixth9);
		MultiObjectiveSearch sixth10 = new MultiObjectiveSearch(sc, cSixth2, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
		sixth10.setTopSolutionFlag(false);
		searches.add(sixth10);
		
		MultiObjectiveSearch sixth11 = new MultiObjectiveSearch(sc, cSixth3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
		sixth11.setTopSolutionFlag(false);
		searches.add(sixth11);
		MultiObjectiveSearch sixth12 = new MultiObjectiveSearch(sc, cSixth3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
		sixth12.setTopSolutionFlag(false);
		searches.add(sixth12);
		MultiObjectiveSearch sixth13 = new MultiObjectiveSearch(sc, cSixth3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
		sixth13.setTopSolutionFlag(false);
		searches.add(sixth13);
		MultiObjectiveSearch sixth14 = new MultiObjectiveSearch(sc, cSixth3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
		sixth14.setTopSolutionFlag(false);
		searches.add(sixth14);
		MultiObjectiveSearch sixth15 = new MultiObjectiveSearch(sc, cSixth3, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
		sixth15.setTopSolutionFlag(false);
		searches.add(sixth15);
		
		MultiObjectiveSearch sixth16 = new MultiObjectiveSearch(sc, cSixth4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
		sixth16.setTopSolutionFlag(false);
		searches.add(sixth16);
		MultiObjectiveSearch sixth17 = new MultiObjectiveSearch(sc, cSixth4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
		sixth17.setTopSolutionFlag(false);
		searches.add(sixth17);
		MultiObjectiveSearch sixth18 = new MultiObjectiveSearch(sc, cSixth4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
		sixth18.setTopSolutionFlag(false);
		searches.add(sixth18);
		MultiObjectiveSearch sixth19 = new MultiObjectiveSearch(sc, cSixth4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
		sixth19.setTopSolutionFlag(false);
		searches.add(sixth19);
		MultiObjectiveSearch sixth20 = new MultiObjectiveSearch(sc, cSixth4, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
		sixth20.setTopSolutionFlag(false);
		searches.add(sixth20);
		
		MultiObjectiveSearch sixth21 = new MultiObjectiveSearch(sc, cSixth5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
		sixth21.setTopSolutionFlag(false);
		searches.add(sixth21);
		MultiObjectiveSearch sixth22 = new MultiObjectiveSearch(sc, cSixth5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
		sixth22.setTopSolutionFlag(false);
		searches.add(sixth22);
		MultiObjectiveSearch sixth23 = new MultiObjectiveSearch(sc, cSixth5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
		sixth23.setTopSolutionFlag(false);
		searches.add(sixth23);
		MultiObjectiveSearch sixth24 = new MultiObjectiveSearch(sc, cSixth5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
		sixth24.setTopSolutionFlag(false);
		searches.add(sixth24);
		MultiObjectiveSearch sixth25 = new MultiObjectiveSearch(sc, cSixth5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
		sixth25.setTopSolutionFlag(false);
		searches.add(sixth25);
		
		MultiObjectiveSearch sixth26 = new MultiObjectiveSearch(sc, cSixth6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
		sixth26.setTopSolutionFlag(false);
		searches.add(sixth26);
		MultiObjectiveSearch sixth27 = new MultiObjectiveSearch(sc, cSixth6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
		sixth27.setTopSolutionFlag(false);
		searches.add(sixth27);
		MultiObjectiveSearch sixth28 = new MultiObjectiveSearch(sc, cSixth6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
		sixth28.setTopSolutionFlag(false);
		searches.add(sixth28);
		MultiObjectiveSearch sixth29 = new MultiObjectiveSearch(sc, cSixth6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
		sixth29.setTopSolutionFlag(false);
		searches.add(sixth29);
		MultiObjectiveSearch sixth30 = new MultiObjectiveSearch(sc, cSixth6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
		sixth30.setTopSolutionFlag(false);
		searches.add(sixth30);
		
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
			int search = (int) Math.floor(i/30);
			int path = (int) Math.floor(i/5);
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
			searches.get(i).run();
		}	

		// Output overall time taken to console.
		timeTaken = System.currentTimeMillis() - startTime;
		time = timeTaken / 1000.0;
		System.out.printf("\r\n\r\nTime taken to run program: %.2fs", time);
	}
}