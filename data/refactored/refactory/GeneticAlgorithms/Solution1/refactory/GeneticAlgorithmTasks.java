package refactory;

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
import searches.GeneticAlgorithmSearch;
import searches.MultiObjectiveSearch;

public class GeneticAlgorithmTasks {
    String pathway = "./data/original/refactory";

    // No attributes - empty constructor.
    public GeneticAlgorithmTasks() {}

    public GeneticAlgorithmTasks(String pathway)
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
        Configuration c = new Configuration("data/metrics.txt", refactorings);
        Configuration[] cGA = { c };

        String outputGA = createOutputDirectory(this.pathway, "GeneticAlgorithm");
        String resultsDirGA = "./results/" + this.pathway.substring(this.pathway.lastIndexOf("/") + 1) + "/GeneticAlgorithm/";
        GeneticAlgorithmSearch geneticAlgorithm = new GeneticAlgorithmSearch(sc, c, sourceFiles);

        String outputMOGA = createOutputDirectory(this.pathway, "MOGeneticAlgorithm");
        String resultsDirMOGA = "./results/" + this.pathway.substring(this.pathway.lastIndexOf("/") + 1) + "/MOGeneticAlgorithm/";
        MultiObjectiveSearch multiObjectiveAlgorithm = new MultiObjectiveSearch(cGA, sourceFiles, outputMOGA);

        long timeTaken, startTime = System.currentTimeMillis();
        double time;

        for (int i = 0; i < 1; i++)
         {
            // Creates new service configuration to start from scratch.
            sc = new CrossReferenceServiceConfiguration();

            // Initialise available refactorings. Needs to be done each
            // time as the service configuration won't be updated otherwise.
            refactorings = new ArrayList<Refactoring>();
            IncreaseClassSecurity ics = new IncreaseClassSecurity(sc);
            refactorings.add(ics);
            IncreaseMethodSecurity ims = new IncreaseMethodSecurity(sc);
            refactorings.add(ims);
            IncreaseFieldSecurity ifs = new IncreaseFieldSecurity(sc);
            refactorings.add(ifs);
            DecreaseClassSecurity dcs = new DecreaseClassSecurity(sc);
            refactorings.add(dcs);
            DecreaseMethodSecurity dms = new DecreaseMethodSecurity(sc);
            refactorings.add(dms);
            DecreaseFieldSecurity dfs = new DecreaseFieldSecurity(sc);
            refactorings.add(dfs);
            MakeClassAbstract mca = new MakeClassAbstract(sc);
            refactorings.add(mca);
            MakeClassConcrete mcc = new MakeClassConcrete(sc);
            refactorings.add(mcc);
            MakeClassFinal mcf = new MakeClassFinal(sc);
            refactorings.add(mcf);
            MakeClassNonFinal mcnf = new MakeClassNonFinal(sc);
            refactorings.add(mcnf);
            MakeFieldFinal mff = new MakeFieldFinal(sc);
            refactorings.add(mff);
            MakeFieldNonFinal mfnf = new MakeFieldNonFinal(sc);
            refactorings.add(mfnf);
            MakeFieldStatic mfs = new MakeFieldStatic(sc);
            refactorings.add(mfs);
            MakeFieldNonStatic mfns = new MakeFieldNonStatic(sc);
            refactorings.add(mfns);
            MakeMethodFinal mmf = new MakeMethodFinal(sc);
            refactorings.add(mmf);
            MakeMethodNonFinal mmnf = new MakeMethodNonFinal(sc);
            refactorings.add(mmnf);
            MakeMethodStatic mms = new MakeMethodStatic(sc);
            refactorings.add(mms);
            MakeMethodNonStatic mmns = new MakeMethodNonStatic(sc);
            refactorings.add(mmns);

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
            sc.getProjectSettings().setProperty(PropertyNames.OUTPUT_PATH, outputGA);
            sc.getProjectSettings().ensureSystemClassesAreInPath();
            sc.getProjectSettings().ensureExtensionClassesAreInPath();

            c = new Configuration("data/metrics.txt", refactorings);
            geneticAlgorithm.setConfiguration(c);
            geneticAlgorithm.setServiceConfiguration(sc);
            geneticAlgorithm.setResultsPath(resultsDirGA);
            geneticAlgorithm.run();

            cGA[0] = c;
            multiObjectiveAlgorithm.setConfiguration(cGA);
            multiObjectiveAlgorithm.setResultsPath(resultsDirMOGA);
//			multiObjectiveAlgorithm.run();
        }

        // Output overall time taken to console.
        timeTaken = System.currentTimeMillis() - startTime;
        time = timeTaken / 1000.0;
        System.out.printf("\n\nTime taken to run program: %.2fs", time);
    }

    // Returns an array of file paths representing the
    // project, found using the file pathway input.
    public String[] read(String pathName)
     {
        File filePath = new File(pathName);
        if (!filePath.exists() || !filePath.isDirectory())
            throw new RuntimeException("Path to files does not exist.");

        Stack<File> dirs = new Stack<File>();
        ArrayList<File> files = new ArrayList<File>();
        dirs.push(filePath);

        // Extracts only the java files from the
        // input and adds them to the file array.
        while (dirs.size() > 0)
         {
            File dir = dirs.pop();
            File[] subfiles = dir.listFiles();

            for (File f: subfiles)
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

    public String createOutputDirectory(String pathName, String name)
     {
        File filePath = new File(pathName);
        String output = "./data/refactored/" + filePath.getName() + "/" + name;
        return output;
    }
}
