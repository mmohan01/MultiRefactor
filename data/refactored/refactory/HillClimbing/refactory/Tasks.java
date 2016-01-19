package refactory;

import java.util.ArrayList;
import java.util.List;

import recoder.CrossReferenceServiceConfiguration;
import recoder.ParserException;
import recoder.io.PropertyNames;
import refactorings.Refactoring;
import refactorings.field.*;
import refactorings.method.*;
import refactorings.type.*;
import searches.HillClimbingSearch;
import searches.RandomSearch;
import searches.Search;

public abstract class Tasks {
    String pathway = "./data/original/json-1.1";

    // No attributes - empty constructor.
    public Tasks() {}

    public Tasks(String pathway)
     {
        this.pathway = pathway;
    }

    public abstract void run()
     {
        // Create an initial service configuration to be overwritten.
        // Reads the source code from the specified directory.
        CrossReferenceServiceConfiguration sc = new CrossReferenceServiceConfiguration();
        PathReader example = new PathReader(this.pathway);
        String[] sourceFiles = example.read();

        // Create empty list of refactorings.
        // Reads the metric configuration in from the specified text file.
        List<Refactoring> refactorings = new ArrayList<Refactoring>();
        Configuration c = new Configuration("data/metrics.txt", refactorings);

        // Initialise search tasks.
        List<Search> searches = new ArrayList<Search>();
        RandomSearch random = new RandomSearch(sc, c);
        searches.add(random);
        HillClimbingSearch hillClimbing = new HillClimbingSearch(sc, c, 0, 0, false);
        searches.add(hillClimbing);

        // Create list of output directories for
        // each refactored project to be written to.
        String[] output = new String[2];
        output[0] = example.createOutputDirectory("Random");
        output[1] = example.createOutputDirectory("HillClimbing");

        // Create list of output directories for
        // each result data output to be written to.
        String[] resultsDir = new String[2];
        resultsDir[0] = "./results/" + this.pathway.substring(this.pathway.lastIndexOf("/") + 1) + "/Random/";
        resultsDir[1] = "./results/" + this.pathway.substring(this.pathway.lastIndexOf("/") + 1) + "/HillClimbing/";

        long timeTaken, startTime = System.currentTimeMillis();
        double time;

        for (int i = 0; i < searches.size(); i++)
         {
            // Creates new service configuration to start from scratch.
            sc = new CrossReferenceServiceConfiguration();

            // Initialise available refactorings. Needs to be done each
            // time as the service configuration won't be updated otherwise.
            refactorings = new ArrayList<Refactoring>();
            DecreaseClassSecurity dcs = new DecreaseClassSecurity(sc);
            refactorings.add(dcs);
            DecreaseMethodSecurity dms = new DecreaseMethodSecurity(sc);
            refactorings.add(dms);
            DecreaseFieldSecurity dfs = new DecreaseFieldSecurity(sc);
            refactorings.add(dfs);
            IncreaseClassSecurity ics = new IncreaseClassSecurity(sc);
            refactorings.add(ics);
            IncreaseMethodSecurity ims = new IncreaseMethodSecurity(sc);
            refactorings.add(ims);
            IncreaseFieldSecurity ifs = new IncreaseFieldSecurity(sc);
            refactorings.add(ifs);
//			MakeClassAbstract mca = new MakeClassAbstract(sc);
//			refactorings.add(mca);
//			MakeClassFinal mcf = new MakeClassFinal(sc);
//			refactorings.add(mcf);
//			MakeFieldFinal mff = new MakeFieldFinal(sc);
//			refactorings.add(mff);
//			MakeFieldStatic mfs = new MakeFieldStatic(sc);
//			refactorings.add(mfs);
//			MakeMethodFinal mmf = new MakeMethodFinal(sc);
//			refactorings.add(mmf);
//			MakeMethodStatic mms = new MakeMethodStatic(sc);
//			refactorings.add(mms);

            try
             {
                // Read the original input.
                sc.getSourceFileRepository().getCompilationUnitsFromFiles(sourceFiles);
            }
             catch (ParserException e)
             {
                System.out.println("\nEXCEPTION: Cannot read input.");
                System.exit(1);
            }

            // Set up initial properties of service configuration.
            sc.getProjectSettings().setProperty(PropertyNames.INPUT_PATH, this.pathway);
            sc.getProjectSettings().setProperty(PropertyNames.OUTPUT_PATH, output[i]);
            sc.getProjectSettings().ensureSystemClassesAreInPath();
            sc.getProjectSettings().ensureExtensionClassesAreInPath();

            // initialise search task.
            c = new Configuration("data/metrics.txt", refactorings);
            searches.get(i).setConfiguration(c);
            searches.get(i).setServiceConfiguration(sc);
            searches.get(i).setResultsPath(resultsDir[i]);
            searches.get(i).run();
        }

        // Output overall time taken to console.
        timeTaken = System.currentTimeMillis() - startTime;
        time = timeTaken / 1000.0;
        System.out.printf("\n\nTime taken to run program: %.2fs", time);
    }
}
