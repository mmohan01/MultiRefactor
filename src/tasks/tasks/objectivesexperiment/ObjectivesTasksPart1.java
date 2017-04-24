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
import searches.GeneticAlgorithmSearch;
import searches.ManyObjectiveSearch;
import searches.MonoObjectiveSearch;
import tasks.Tasks;

public class ObjectivesTasksPart1 extends Tasks
{
	// No attributes - empty constructor.
	public ObjectivesTasksPart1()
	{
		super();
	}

	public ObjectivesTasksPart1(String pathway)
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
		Configuration[] cM1 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/prioritybeaver-0.9.11.txt"), 
							   new Configuration("./configurations/diversity.txt"), new Configuration("./configurations/elementrecentnessbeaver.txt")};
		Configuration[] cM2 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/priorityapachexmlrpc-3.1.1.txt"), 
							   new Configuration("./configurations/diversity.txt"), new Configuration("./configurations/elementrecentnessapachexmlrpc.txt")};
		Configuration[] cM3 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/priorityjrdf-0.3.4.3.txt"), 
							   new Configuration("./configurations/diversity.txt"), new Configuration("./configurations/elementrecentnessjrdf.txt")};
		Configuration[] cM4 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/priorityganttproject-1.11.1.txt"), 
						       new Configuration("./configurations/diversity.txt"), new Configuration("./configurations/elementrecentnessganttproject.txt")};
		Configuration[] cM5 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/priorityjhotdraw-6.0b1.txt"), 
							   new Configuration("./configurations/diversity.txt"), new Configuration("./configurations/elementrecentnessjhotdraw.txt")};
		Configuration[] cM6 = {new Configuration("./configurations/qualityfunction.txt"), new Configuration("./configurations/priorityxom-1.2.1.txt"), 
							   new Configuration("./configurations/diversity.txt"), new Configuration("./configurations/elementrecentnessxom.txt")};
		
		// Initialise search tasks.
		ArrayList<GeneticAlgorithmSearch> searches = new ArrayList<GeneticAlgorithmSearch>();
		MonoObjectiveSearch geneticAlgorithm1 = new MonoObjectiveSearch(sc, c, sourceFiles[0], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm1);
		MonoObjectiveSearch geneticAlgorithm2 = new MonoObjectiveSearch(sc, c, sourceFiles[0], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm2);
		MonoObjectiveSearch geneticAlgorithm3 = new MonoObjectiveSearch(sc, c, sourceFiles[0], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm3);
		MonoObjectiveSearch geneticAlgorithm4 = new MonoObjectiveSearch(sc, c, sourceFiles[0], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm4);
		MonoObjectiveSearch geneticAlgorithm5 = new MonoObjectiveSearch(sc, c, sourceFiles[0], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm5);
		MonoObjectiveSearch geneticAlgorithm6 = new MonoObjectiveSearch(sc, c, sourceFiles[0], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm6);
		MonoObjectiveSearch geneticAlgorithm7 = new MonoObjectiveSearch(sc, c, sourceFiles[0], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm7);
		MonoObjectiveSearch geneticAlgorithm8 = new MonoObjectiveSearch(sc, c, sourceFiles[0], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm8);
		MonoObjectiveSearch geneticAlgorithm9 = new MonoObjectiveSearch(sc, c, sourceFiles[0], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm9);
		MonoObjectiveSearch geneticAlgorithm10 = new MonoObjectiveSearch(sc, c, sourceFiles[0], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm10);
		
		MonoObjectiveSearch geneticAlgorithm11 = new MonoObjectiveSearch(sc, c, sourceFiles[1], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm11);
		MonoObjectiveSearch geneticAlgorithm12 = new MonoObjectiveSearch(sc, c, sourceFiles[1], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm12);
		MonoObjectiveSearch geneticAlgorithm13 = new MonoObjectiveSearch(sc, c, sourceFiles[1], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm13);
		MonoObjectiveSearch geneticAlgorithm14 = new MonoObjectiveSearch(sc, c, sourceFiles[1], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm14);
		MonoObjectiveSearch geneticAlgorithm15 = new MonoObjectiveSearch(sc, c, sourceFiles[1], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm15);
		MonoObjectiveSearch geneticAlgorithm16 = new MonoObjectiveSearch(sc, c, sourceFiles[1], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm16);
		MonoObjectiveSearch geneticAlgorithm17 = new MonoObjectiveSearch(sc, c, sourceFiles[1], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm17);
		MonoObjectiveSearch geneticAlgorithm18 = new MonoObjectiveSearch(sc, c, sourceFiles[1], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm18);
		MonoObjectiveSearch geneticAlgorithm19 = new MonoObjectiveSearch(sc, c, sourceFiles[1], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm19);
		MonoObjectiveSearch geneticAlgorithm20 = new MonoObjectiveSearch(sc, c, sourceFiles[1], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm20);
		
		MonoObjectiveSearch geneticAlgorithm21 = new MonoObjectiveSearch(sc, c, sourceFiles[2], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm21);
		MonoObjectiveSearch geneticAlgorithm22 = new MonoObjectiveSearch(sc, c, sourceFiles[2], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm22);
		MonoObjectiveSearch geneticAlgorithm23 = new MonoObjectiveSearch(sc, c, sourceFiles[2], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm23);
		MonoObjectiveSearch geneticAlgorithm24 = new MonoObjectiveSearch(sc, c, sourceFiles[2], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm24);
		MonoObjectiveSearch geneticAlgorithm25 = new MonoObjectiveSearch(sc, c, sourceFiles[2], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm25);
		MonoObjectiveSearch geneticAlgorithm26 = new MonoObjectiveSearch(sc, c, sourceFiles[2], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm26);
		MonoObjectiveSearch geneticAlgorithm27 = new MonoObjectiveSearch(sc, c, sourceFiles[2], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm27);
		MonoObjectiveSearch geneticAlgorithm28 = new MonoObjectiveSearch(sc, c, sourceFiles[2], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm28);
		MonoObjectiveSearch geneticAlgorithm29 = new MonoObjectiveSearch(sc, c, sourceFiles[2], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm29);
		MonoObjectiveSearch geneticAlgorithm30 = new MonoObjectiveSearch(sc, c, sourceFiles[2], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm30);
		
		MonoObjectiveSearch geneticAlgorithm31 = new MonoObjectiveSearch(sc, c, sourceFiles[3], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm31);
		MonoObjectiveSearch geneticAlgorithm32 = new MonoObjectiveSearch(sc, c, sourceFiles[3], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm32);
		MonoObjectiveSearch geneticAlgorithm33 = new MonoObjectiveSearch(sc, c, sourceFiles[3], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm33);
		MonoObjectiveSearch geneticAlgorithm34 = new MonoObjectiveSearch(sc, c, sourceFiles[3], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm34);
		MonoObjectiveSearch geneticAlgorithm35 = new MonoObjectiveSearch(sc, c, sourceFiles[3], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm35);
		MonoObjectiveSearch geneticAlgorithm36 = new MonoObjectiveSearch(sc, c, sourceFiles[3], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm36);
		MonoObjectiveSearch geneticAlgorithm37 = new MonoObjectiveSearch(sc, c, sourceFiles[3], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm37);
		MonoObjectiveSearch geneticAlgorithm38 = new MonoObjectiveSearch(sc, c, sourceFiles[3], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm38);
		MonoObjectiveSearch geneticAlgorithm39 = new MonoObjectiveSearch(sc, c, sourceFiles[3], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm39);
		MonoObjectiveSearch geneticAlgorithm40 = new MonoObjectiveSearch(sc, c, sourceFiles[3], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm40);
		
		MonoObjectiveSearch geneticAlgorithm41 = new MonoObjectiveSearch(sc, c, sourceFiles[4], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm41);
		MonoObjectiveSearch geneticAlgorithm42 = new MonoObjectiveSearch(sc, c, sourceFiles[4], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm42);
		MonoObjectiveSearch geneticAlgorithm43 = new MonoObjectiveSearch(sc, c, sourceFiles[4], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm43);
		MonoObjectiveSearch geneticAlgorithm44 = new MonoObjectiveSearch(sc, c, sourceFiles[4], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm44);
		MonoObjectiveSearch geneticAlgorithm45 = new MonoObjectiveSearch(sc, c, sourceFiles[4], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm45);
		MonoObjectiveSearch geneticAlgorithm46 = new MonoObjectiveSearch(sc, c, sourceFiles[4], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm46);
		MonoObjectiveSearch geneticAlgorithm47 = new MonoObjectiveSearch(sc, c, sourceFiles[4], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm47);
		MonoObjectiveSearch geneticAlgorithm48 = new MonoObjectiveSearch(sc, c, sourceFiles[4], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm48);
		MonoObjectiveSearch geneticAlgorithm49 = new MonoObjectiveSearch(sc, c, sourceFiles[4], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm49);
		MonoObjectiveSearch geneticAlgorithm50 = new MonoObjectiveSearch(sc, c, sourceFiles[4], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm50);
		
		MonoObjectiveSearch geneticAlgorithm51 = new MonoObjectiveSearch(sc, c, sourceFiles[5], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm51);
		MonoObjectiveSearch geneticAlgorithm52 = new MonoObjectiveSearch(sc, c, sourceFiles[5], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm52);
		MonoObjectiveSearch geneticAlgorithm53 = new MonoObjectiveSearch(sc, c, sourceFiles[5], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm53);
		MonoObjectiveSearch geneticAlgorithm54 = new MonoObjectiveSearch(sc, c, sourceFiles[5], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm54);
		MonoObjectiveSearch geneticAlgorithm55 = new MonoObjectiveSearch(sc, c, sourceFiles[5], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm55);
		MonoObjectiveSearch geneticAlgorithm56 = new MonoObjectiveSearch(sc, c, sourceFiles[5], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm56);
		MonoObjectiveSearch geneticAlgorithm57 = new MonoObjectiveSearch(sc, c, sourceFiles[5], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm57);
		MonoObjectiveSearch geneticAlgorithm58 = new MonoObjectiveSearch(sc, c, sourceFiles[5], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm58);
		MonoObjectiveSearch geneticAlgorithm59 = new MonoObjectiveSearch(sc, c, sourceFiles[5], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm59);
		MonoObjectiveSearch geneticAlgorithm60 = new MonoObjectiveSearch(sc, c, sourceFiles[5], true, 100, 50, 0.2f, 0.8f);
		searches.add(geneticAlgorithm60);
		
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm1 = new ManyObjectiveSearch(sc, cM1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm1.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm1);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm2 = new ManyObjectiveSearch(sc, cM1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm2.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm2);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm3 = new ManyObjectiveSearch(sc, cM1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm3.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm3);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm4 = new ManyObjectiveSearch(sc, cM1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm4.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm4);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm5 = new ManyObjectiveSearch(sc, cM1, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm5.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm5);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm6 = new ManyObjectiveSearch(sc, cM2, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm6.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm6);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm7 = new ManyObjectiveSearch(sc, cM2, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm7.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm7);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm8 = new ManyObjectiveSearch(sc, cM2, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm8.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm8);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm9 = new ManyObjectiveSearch(sc, cM2, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm9.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm9);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm10 = new ManyObjectiveSearch(sc, cM2, refactorings, sourceFiles[0], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm10.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm10);
		
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm11 = new ManyObjectiveSearch(sc, cM3, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm11.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm11);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm12 = new ManyObjectiveSearch(sc, cM3, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm12.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm12);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm13 = new ManyObjectiveSearch(sc, cM3, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm13.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm13);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm14 = new ManyObjectiveSearch(sc, cM3, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm14.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm14);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm15 = new ManyObjectiveSearch(sc, cM3, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm15.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm15);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm16 = new ManyObjectiveSearch(sc, cM4, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm16.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm16);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm17 = new ManyObjectiveSearch(sc, cM4, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm17.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm17);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm18 = new ManyObjectiveSearch(sc, cM4, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm18.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm18);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm19 = new ManyObjectiveSearch(sc, cM4, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm19.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm19);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm20 = new ManyObjectiveSearch(sc, cM4, refactorings, sourceFiles[1], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm20.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm20);
		
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm21 = new ManyObjectiveSearch(sc, cM5, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm21.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm21);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm22 = new ManyObjectiveSearch(sc, cM5, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm22.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm22);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm23 = new ManyObjectiveSearch(sc, cM5, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm23.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm23);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm24 = new ManyObjectiveSearch(sc, cM5, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm24.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm24);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm25 = new ManyObjectiveSearch(sc, cM5, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm25.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm25);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm26 = new ManyObjectiveSearch(sc, cM6, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm26.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm26);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm27 = new ManyObjectiveSearch(sc, cM6, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm27.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm27);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm28 = new ManyObjectiveSearch(sc, cM6, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm28.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm28);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm29 = new ManyObjectiveSearch(sc, cM6, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm29.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm29);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm30 = new ManyObjectiveSearch(sc, cM6, refactorings, sourceFiles[2], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm30.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm30);
		
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm31 = new ManyObjectiveSearch(sc, cM5, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm31.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm31);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm32 = new ManyObjectiveSearch(sc, cM5, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm32.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm32);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm33 = new ManyObjectiveSearch(sc, cM5, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm33.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm33);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm34 = new ManyObjectiveSearch(sc, cM5, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm34.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm34);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm35 = new ManyObjectiveSearch(sc, cM5, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm35.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm35);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm36 = new ManyObjectiveSearch(sc, cM6, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm36.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm36);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm37 = new ManyObjectiveSearch(sc, cM6, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm37.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm37);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm38 = new ManyObjectiveSearch(sc, cM6, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm38.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm38);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm39 = new ManyObjectiveSearch(sc, cM6, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm39.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm39);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm40 = new ManyObjectiveSearch(sc, cM6, refactorings, sourceFiles[3], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm40.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm40);
		
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm41 = new ManyObjectiveSearch(sc, cM5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm41.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm41);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm42 = new ManyObjectiveSearch(sc, cM5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm42.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm42);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm43 = new ManyObjectiveSearch(sc, cM5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm43.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm43);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm44 = new ManyObjectiveSearch(sc, cM5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm44.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm44);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm45 = new ManyObjectiveSearch(sc, cM5, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm45.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm45);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm46 = new ManyObjectiveSearch(sc, cM6, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm46.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm46);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm47 = new ManyObjectiveSearch(sc, cM6, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm47.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm47);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm48 = new ManyObjectiveSearch(sc, cM6, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm48.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm48);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm49 = new ManyObjectiveSearch(sc, cM6, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm49.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm49);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm50 = new ManyObjectiveSearch(sc, cM6, refactorings, sourceFiles[4], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm50.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm50);
		
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm51 = new ManyObjectiveSearch(sc, cM5, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm51.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm51);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm52 = new ManyObjectiveSearch(sc, cM5, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm52.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm52);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm53 = new ManyObjectiveSearch(sc, cM5, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm53.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm53);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm54 = new ManyObjectiveSearch(sc, cM5, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm54.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm54);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm55 = new ManyObjectiveSearch(sc, cM5, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm55.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm55);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm56 = new ManyObjectiveSearch(sc, cM6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm56.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm56);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm57 = new ManyObjectiveSearch(sc, cM6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm57.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm57);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm58 = new ManyObjectiveSearch(sc, cM6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm58.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm58);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm59 = new ManyObjectiveSearch(sc, cM6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm59.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm59);
		ManyObjectiveSearch manyObjectiveGeneticAlgorithm60 = new ManyObjectiveSearch(sc, cM6, refactorings, sourceFiles[5], 100, 50, 0.2f, 0.8f);
		manyObjectiveGeneticAlgorithm60.setTopSolutionFlag(false);
		searches.add(manyObjectiveGeneticAlgorithm60);
		
		// Create list of output directories for
		// each refactored project to be written to.
		String[] outputDir = new String[]{
				"./data/refactored/ObjectivesExperiment/Part1/Mono-Objective/",
				"./data/refactored/ObjectivesExperiment/Part1/Many-Objective/"};

		// Create list of output directories for
		// each result data output to be written to.
		String[] resultsDir = new String[]{
				"./results/ObjectivesExperiment/Part1/Mono-Objective/",
				"./results/ObjectivesExperiment/Part1/Many-Objective/"};

		long timeTaken, startTime = System.currentTimeMillis();
		double time;
		
		for (int i = 0; i < searches.size(); i++)
		{
			// Creates new service configuration to start from scratch.
			sc = new CrossReferenceServiceConfiguration();
			int search = (i < 60) ? 0 : 1;
			int path = (int) Math.floor(i / 10);
			int run = (i % 10) + 1;

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
			if (searches.get(i).getClass().getName().contains("ManyObjectiveSearch"))
				((ManyObjectiveSearch) searches.get(i)).setRefactorings(refactorings);
			else
			{
				c = new Configuration("./configurations/qualityfunction.txt", refactorings);
				searches.get(i).setConfiguration(c);
			}
			
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