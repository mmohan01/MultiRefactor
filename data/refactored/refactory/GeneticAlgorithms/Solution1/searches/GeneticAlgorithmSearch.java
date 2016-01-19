package searches;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import recoder.CrossReferenceServiceConfiguration;
import recoder.ParserException;
import recoder.io.PropertyNames;
import refactorings.Refactoring;
import refactory.Configuration;
import refactory.FitnessFunction;
import refactory.Metrics;
import refactory.RefactoringSequence;

public class GeneticAlgorithmSearch extends Search {
    private String[] sourceFiles;

    private boolean printAll;
    private int generations;
    private int populationSize;
    private float crossoverProbability;
    private float mutationProbability;
    private int initialRefactoringRange = 2;

    public GeneticAlgorithmSearch(CrossReferenceServiceConfiguration sc, Configuration c, String[] sourceFiles)
     {
        super(sc, c);
        this.sourceFiles = sourceFiles;
        this.printAll = true;
        this.generations = 5;
        this.populationSize = 2;
        this.crossoverProbability = 0.8f;
        this.mutationProbability = 0.2f;
    }

    public GeneticAlgorithmSearch(CrossReferenceServiceConfiguration sc, Configuration c, String[] sourceFiles,
                                  boolean printAll, int generations, int populationSize, float crossoverProbability, float mutationProbability)
     {
        super(sc, c);
        this.sourceFiles = sourceFiles;
        this.printAll = printAll;
        this.generations = generations;
        this.populationSize = populationSize;
        this.crossoverProbability = crossoverProbability;
        this.mutationProbability = mutationProbability;
    }

    // Executes the Genetic Algorithm.
    public void run()
     {
        String runInfo = String.format("Search: Genetic Algorithm\r\nGenerations: %d\r\nPopulation Size: %d"
                                       + "\r\nCrossover Probability: %f\r\nMutation Probability: %f",
                                       this.generations, this.populationSize, this.crossoverProbability, this.mutationProbability);
        Metrics m = new Metrics(super.sc);
        FitnessFunction ff = new FitnessFunction();
        float benchmark = ff.calculateScore(m, super.c.getConfiguration());

        if (this.printAll)
         {
            for (int i = 1; i <= populationSize; i++)
             {
                outputSearchInfo(super.resultsPath, i, runInfo);
                outputMetrics(benchmark, m, true, false, i, super.resultsPath);
            }
        }
         else
         {
            super.outputSearchInfo(super.resultsPath, runInfo);
            super.outputMetrics(benchmark, m, true, true, super.resultsPath);
        }

        long timeTaken, startTime = System.currentTimeMillis();
        double time;

        System.out.printf("\n\nCreating Initial Population...");
        ArrayList<RefactoringSequence> population = initialize();

        // At each generation, crossover is applied to produce a number of child solutions.
        // Then, mutation is applied amongst these new solutions to introduce variety.
        // Fitness is measured for the new solutions and they are sorted accordingly.
        for (int i = 1; i <= this.generations; i++)
         {
            System.out.printf("\n\nIteration %d:", i);
            ArrayList<RefactoringSequence> newGeneration = new ArrayList<RefactoringSequence>();

            // Crossover is always done once for each generation but beyond that the
            // amount of times it is executed depends on the crossover probability.
            do
             {
                System.out.printf("\nCrossover...");
                int[] parents = rankSelection(population.size(), 2);
                newGeneration.addAll(crossover(population.get(parents[0]), population.get(parents[1])));
            } while(Math.random() < this.crossoverProbability);

            // The amount of times if, at all, mutation is applied depends on the
            // mutation probability. This will mutate the children of the current generation.
            while (Math.random() < this.mutationProbability)
             {
                System.out.printf("\nMutation...");
                int randomChild = (int)(Math.random() * newGeneration.size());
                newGeneration.set(randomChild, mutation(newGeneration.get(randomChild)));
            }

            // On the first generation the initial population
            // and children are all measured for fitness.
            if (i == 1)
             {
                population.addAll(newGeneration);
                population = fitness(population);
            }
            // Only the new solutions need to be measured for fitness.
            else
             {
                newGeneration = fitness(newGeneration);
                population.addAll(newGeneration);
            }

            // Sort new population by fitness and truncate it to remove weakest solutions.
            population = new ArrayList<RefactoringSequence>(sort(population));
        }

        timeTaken = System.currentTimeMillis() - startTime;
        time = timeTaken / 1000.0;

        if (this.printAll)
         {
            for (int i = 0; i < population.size(); i++)
             {
                outputRefactoringInfo(super.resultsPath, time, population.get(i).getFitness() - benchmark, i + 1, population.get(i).getRefactoringInfo());
                m = new Metrics(population.get(i).getServiceConfiguration());
                outputMetrics(population.get(i).getFitness(), m, false, false, i + 1, super.resultsPath);
            }

            System.out.printf("\n\nPrinting Population");

            for (int i = 0; i < population.size(); i++)
             {
                String newOutputPath = super.sc.getProjectSettings().getProperty(PropertyNames.OUTPUT_PATH) + "s/Solution" + (i + 1);
                population.get(i).getServiceConfiguration().getProjectSettings().setProperty(PropertyNames.OUTPUT_PATH, newOutputPath);
                super.print(population.get(i).getServiceConfiguration().getSourceFileRepository());
            }
        }
         else
         {
            outputRefactoringInfo(super.resultsPath, time, population.get(0).getFitness() - benchmark, -1, population.get(0).getRefactoringInfo());
            m = new Metrics(population.get(0).getServiceConfiguration());
            super.outputMetrics(population.get(0).getFitness(), m, false, true, super.resultsPath);
            System.out.printf("\n\nScore has improved overall by %f", population.get(0).getFitness() - benchmark);
            System.out.printf("\nPrinting Top Solution");
            super.print(population.get(0).getServiceConfiguration().getSourceFileRepository());
        }

        // Output time taken to console and refactoring information to results file.
        timeTaken = System.currentTimeMillis() - startTime;
        time = timeTaken / 1000.0;
        System.out.printf("\nOverall time taken for search: %.2fs", time);
    }

    // Creates an initial population of refactoring solutions at random.
    // Using the initial refactoring range a random amount of refactorings will
    // be applied in that range to create a possible solution and this will be
    // repeated until the population size has been satisfied.
    private ArrayList<RefactoringSequence> initialize()
     {
        ArrayList<Refactoring> refactorings = super.c.getRefactorings();
        ArrayList<RefactoringSequence> population = new ArrayList<RefactoringSequence>(this.populationSize);
        CrossReferenceServiceConfiguration[] scArray = new CrossReferenceServiceConfiguration[this.populationSize];

        for (int i = 0; i < this.populationSize; i++)
         {
            // Create copy of the initial program model.
            CrossReferenceServiceConfiguration scCopy = new CrossReferenceServiceConfiguration();

            try
             {
                // Read the original input.
                scCopy.getSourceFileRepository().getCompilationUnitsFromFiles(this.sourceFiles);
            }
             catch (ParserException e)
             {
                System.out.println("\nEXCEPTION: Cannot read input.");
                System.exit(1);
            }

            // Set up initial properties of service configuration.
            // Saves new model into array so it can be updated and passed to the relevant refactoring.
            scCopy.getProjectSettings().setProperty(PropertyNames.INPUT_PATH, super.sc.getProjectSettings().getProperty(PropertyNames.INPUT_PATH));
            scCopy.getProjectSettings().setProperty(PropertyNames.OUTPUT_PATH, super.sc.getProjectSettings().getProperty(PropertyNames.OUTPUT_PATH));
            scCopy.getProjectSettings().ensureSystemClassesAreInPath();
            scCopy.getProjectSettings().ensureExtensionClassesAreInPath();
            scArray[i] = scCopy;

            // Applies random refactorings to each solution to create an initial population.
            // The amount of refactorings applied in each case is chosen randomly within the range supplied.
            int refactoringAmount = ((int)(Math.random() * this.initialRefactoringRange)) + 1;
            ArrayList<int[]> posSequence = new ArrayList<int[]>();
            ArrayList<Integer> refSequence = new ArrayList<Integer>();
            ArrayList<Integer> IDSequence = new ArrayList<Integer>();
            ArrayList<String> refactoringInfo = new ArrayList<String>();

            for (int j = 0; j < refactoringAmount; j++)
             {
                int[] result = randomRefactoring(scArray[i]);
                int[] position = { result[1], result[2] };

                if (result[0] == -1)
                 {
                    System.out.printf("\nThere are no refactorings available for the rest of the sequence.");
                    j = refactoringAmount;
                }
                 else
                 {
                    IDSequence.add(refactorings.get(result[0]).getID(position[0], position[1]));
                    refactorings.get(result[0]).transform(refactorings.get(result[0]).analyze((j + 1), position[0], position[1]));
                    refactoringInfo.add(refactorings.get(result[0]).getRefactoringInfo());
                    refSequence.add(result[0]);
                    posSequence.add(position);
                    scArray[i] = refactorings.get(result[0]).getServiceConfiguration();
                }
            }

            population.add(new RefactoringSequence(scArray[i], refSequence, posSequence, IDSequence, refactoringInfo));
        }

        return population;
    }

    // Method uses single-point crossover. For each refactoring sequence passed in,
    // a cut point is randomly chosen. The segments of each sequence are then switched to
    // create two child solutions. After this the refactorings are applied for each child
    // and any inapplicable refactorings are removed form the new sequences.
    private ArrayList<RefactoringSequence> crossover(RefactoringSequence p1, RefactoringSequence p2)
     {
        ArrayList<RefactoringSequence> children = new ArrayList<RefactoringSequence>(2);
        ArrayList<Refactoring> refactorings = super.c.getRefactorings();
        int cutPoint1 = ((int)(Math.random() * (p1.getRefactorings().size() - 1))) + 1;
        int cutPoint2 = ((int)(Math.random() * (p2.getRefactorings().size() - 1))) + 1;
        int elementPosition, i2;

        int c1Size = cutPoint1 + (p2.getRefactorings().size() - cutPoint2);
        ArrayList<Integer> c1Refactorings = new ArrayList<Integer>();
        ArrayList<int[]> c1Positions = new ArrayList<int[]>();
        ArrayList<Integer> c1IDs = new ArrayList<Integer>();
        ArrayList<String> refactoringInfo1 = new ArrayList<String>();

        // Create copies of the initial program model.
        CrossReferenceServiceConfiguration sc1 = new CrossReferenceServiceConfiguration();
        CrossReferenceServiceConfiguration sc2 = new CrossReferenceServiceConfiguration();

        try
         {
            // Read the original input.
            sc1.getSourceFileRepository().getCompilationUnitsFromFiles(this.sourceFiles);
            sc2.getSourceFileRepository().getCompilationUnitsFromFiles(this.sourceFiles);
        }
         catch (ParserException e)
         {
            System.out.println("\nEXCEPTION: Cannot read input.");
            System.exit(1);
        }

        // Set up initial properties of service configurations.
        sc1.getProjectSettings().setProperty(PropertyNames.INPUT_PATH, super.sc.getProjectSettings().getProperty(PropertyNames.INPUT_PATH));
        sc1.getProjectSettings().setProperty(PropertyNames.OUTPUT_PATH, super.sc.getProjectSettings().getProperty(PropertyNames.OUTPUT_PATH));
        sc1.getProjectSettings().ensureSystemClassesAreInPath();
        sc1.getProjectSettings().ensureExtensionClassesAreInPath();
        sc2.getProjectSettings().setProperty(PropertyNames.INPUT_PATH, super.sc.getProjectSettings().getProperty(PropertyNames.INPUT_PATH));
        sc2.getProjectSettings().setProperty(PropertyNames.OUTPUT_PATH, super.sc.getProjectSettings().getProperty(PropertyNames.OUTPUT_PATH));
        sc2.getProjectSettings().ensureSystemClassesAreInPath();
        sc2.getProjectSettings().ensureExtensionClassesAreInPath();

        for (int i = 0; i < c1Size; i++)
         {
            // The first sequence in each solution will be applicable so
            // refactorings can be applied without checking.
            if (i < cutPoint1)
             {
                refactorings.get(p1.getRefactorings().get(i)).setServiceConfiguration(sc1);
                refactorings.get(p1.getRefactorings().get(i)).transform(refactorings.get(p1.getRefactorings().get(i)).analyze((i + 1), p1.getPositions().get(i)[0], p1.getPositions().get(i)[1]));
                refactoringInfo1.add(refactorings.get(p1.getRefactorings().get(i)).getRefactoringInfo());
                c1Refactorings.add(p1.getRefactorings().get(i));
                c1Positions.add(p1.getPositions().get(i));
                c1IDs.add(p1.getIDs().get(i));
                sc1 = refactorings.get(p1.getRefactorings().get(i)).getServiceConfiguration();
            }
            // For the second sequence, a check will have
            // to be made for each contiguous refactoring.
            else
             {
                elementPosition = -1;
                i2 = cutPoint2 + (i - cutPoint1);
                refactorings.get(p2.getRefactorings().get(i2)).setServiceConfiguration(sc1);

                // Checks for the relevant program element by comparing the names of
                // each applicable element in the class with the desired element name.
                for (int j = 1; j <= refactorings.get(p2.getRefactorings().get(i2)).getAmount(p2.getPositions().get(i2)[0]); j++)
                 {
                    if (refactorings.get(p2.getRefactorings().get(i2)).getID(p2.getPositions().get(i2)[0], j) == (p2.getIDs().get(i2)))
                     {
                        elementPosition = j;
                        break;
                    }
                }

                // If the element exists and can be refactored.
                if (elementPosition != -1)
                 {
                    refactorings.get(p2.getRefactorings().get(i2)).transform(refactorings.get(p2.getRefactorings().get(i2)).analyze((i + 1), p2.getPositions().get(i2)[0], elementPosition));
                    refactoringInfo1.add(refactorings.get(p2.getRefactorings().get(i2)).getRefactoringInfo());
                    c1Refactorings.add(p2.getRefactorings().get(i2));
                    c1Positions.add(new int[] { p2.getPositions().get(i2)[0], elementPosition });
                    c1IDs.add(p2.getIDs().get(i2));
                    sc1 = refactorings.get(p2.getRefactorings().get(i2)).getServiceConfiguration();
                }
            }
        }

        children.add(new RefactoringSequence(sc1, c1Refactorings, c1Positions, c1IDs, refactoringInfo1));

        int c2Size = cutPoint2 + (p1.getRefactorings().size() - cutPoint1);
        ArrayList<Integer> c2Refactorings = new ArrayList<Integer>();
        ArrayList<int[]> c2Positions = new ArrayList<int[]>();
        ArrayList<Integer> c2IDs = new ArrayList<Integer>();
        ArrayList<String> refactoringInfo2 = new ArrayList<String>();

        for (int i = 0; i < c2Size; i++)
         {
            // The first sequence in each solution will be applicable so
            // refactorings can be applied without checking.
            if (i < cutPoint2)
             {
                refactorings.get(p2.getRefactorings().get(i)).setServiceConfiguration(sc2);
                refactorings.get(p2.getRefactorings().get(i)).transform(refactorings.get(p2.getRefactorings().get(i)).analyze((i + 1), p2.getPositions().get(i)[0], p2.getPositions().get(i)[1]));
                refactoringInfo2.add(refactorings.get(p2.getRefactorings().get(i)).getRefactoringInfo());
                c2Refactorings.add(p2.getRefactorings().get(i));
                c2Positions.add(p2.getPositions().get(i));
                c2IDs.add(p2.getIDs().get(i));
                sc2 = refactorings.get(p2.getRefactorings().get(i)).getServiceConfiguration();
            }
            // For the second sequence, a check will have
            // to be made for each contiguous refactoring.
            else
             {
                elementPosition = -1;
                i2 = cutPoint1 + (i - cutPoint2);
                refactorings.get(p1.getRefactorings().get(i2)).setServiceConfiguration(sc2);

                // Checks for the relevant program element by comparing the names of
                // each applicable element in the class with the desired element name.
                for (int j = 1; j <=  refactorings.get(p1.getRefactorings().get(i2)).getAmount(p1.getPositions().get(i2)[0]); j++)
                 {
                    if (refactorings.get(p1.getRefactorings().get(i2)).getID(p1.getPositions().get(i2)[0], j) == (p1.getIDs().get(i2)))
                     {
                        elementPosition = j;
                        break;
                    }
                }

                // If the element exists and can be refactored.
                if (elementPosition != -1)
                 {
                    refactorings.get(p1.getRefactorings().get(i2)).transform(refactorings.get(p1.getRefactorings().get(i2)).analyze((i + 1), p1.getPositions().get(i2)[0], elementPosition));
                    refactoringInfo2.add(refactorings.get(p1.getRefactorings().get(i2)).getRefactoringInfo());
                    c2Refactorings.add(p1.getRefactorings().get(i2));
                    c2Positions.add(new int[] { p1.getPositions().get(i2)[0], elementPosition });
                    c2IDs.add(p1.getIDs().get(i2));
                    sc2 = refactorings.get(p1.getRefactorings().get(i2)).getServiceConfiguration();
                }
            }
        }

        children.add(new RefactoringSequence(sc2, c2Refactorings, c2Positions, c2IDs, refactoringInfo2));

        return children;
    }

    // Applies a random refactoring to the end of the refactoring sequence passed in.
    // If the refactoring is not applicable it will keep trying until an applicable
    // refactoring is found or it runs out of possibilities. In this case the original
    // sequence is returned.
    private RefactoringSequence mutation(RefactoringSequence p)
     {
        ArrayList<Refactoring> refactorings = super.c.getRefactorings();
        ArrayList<Integer> refSequence = new ArrayList<Integer>(p.getRefactorings());
        ArrayList<int[]> posSequence = new ArrayList<int[]>(p.getPositions());
        ArrayList<Integer> IDSequence = new ArrayList<Integer>(p.getIDs());
        ArrayList<String> refactoringInfo = new ArrayList<String>(p.getRefactoringInfo());

        int[] result = randomRefactoring(p.getServiceConfiguration());
        int[] position = { result[1], result[2] };

        // Applies refactoring to model and adds it to the sequence.
        if (result[0] != -1)
         {
            refSequence.add(result[0]);
            posSequence.add(position);
            IDSequence.add(refactorings.get(result[0]).getID(position[0], position[1]));
            refactorings.get(result[0]).transform(refactorings.get(result[0]).analyze((refSequence.size()), position[0], position[1]));
            refactoringInfo.add(refactorings.get(result[0]).getRefactoringInfo());
            p.setRefactorings(refSequence);
            p.setPositions(posSequence);
            p.setIDs(IDSequence);
            p.setRefactoringInfo(refactoringInfo);
            p.setServiceConfiguration(refactorings.get(result[0]).getServiceConfiguration());
        }

        return p;
    }

    // Calculates the fitness values for the solutions passed in.
    private ArrayList<RefactoringSequence> fitness(ArrayList<RefactoringSequence> population)
     {
        FitnessFunction ff = new FitnessFunction();

        for (int i = 0; i < population.size(); i++)
         {
            Metrics m = new Metrics(population.get(i).getServiceConfiguration());
            population.get(i).setFitness(ff.calculateScore(m, super.c.getConfiguration()));
        }

        return population;
    }

    // Sorts the population so that the more fit solutions are at the front of the list.
    // After the list is sorted, it is truncated to remove the weakest solutions.
    private ArrayList<RefactoringSequence> sort(ArrayList<RefactoringSequence> population)
     {
        RefactoringSequence[] arrayPopulation = population.toArray(new RefactoringSequence[0]);
        Arrays.sort(arrayPopulation, new FitnessComparator());
        population.clear();

        for (RefactoringSequence s: arrayPopulation)
            population.add(s);

        population.subList(this.populationSize, population.size()).clear();
        return population;
    }

    // This inner class allows sorting by fitness so that the more fit solutions are at the front of the list.
    private static class FitnessComparator implements Comparator<RefactoringSequence> {
        // Compares the two specified individuals using the fitness operator.
        // Returns -1, 0 or 1 as the first argument is less than, equal to, or greater than the second.
        public int compare(RefactoringSequence s1, RefactoringSequence s2)
         {
            if (s1.getFitness() < s2.getFitness())
                return -1;
             else if (s1.getFitness() > s2.getFitness())
                return 1;
             else
                return 0;
        }
    }

    // Finds a random available refactoring in the specified model and passes back the
    // refactoring used and the position of the applicable program element in the model.
    private int[] randomRefactoring(CrossReferenceServiceConfiguration sc)
     {
        ArrayList<Refactoring> refactorings = super.c.getRefactorings();
        int[] position = new int[2];
        int r = -1;

        // Find element to refactor.
        if (refactorings.size() > 0)
         {
            r = (int)(Math.random() * refactorings.size());
            refactorings.get(r).setServiceConfiguration(sc);
            position = super.randomElement(refactorings.get(r));
        }
         else
         {
            position[0] = -1;
            position[1] = -1;
        }

        // Checks in case no elements are returned for the refactoring.
        // Will check again for each available refactoring in the search and
        // if there are still no applicable elements returned the search will terminate.
        if ((position[0] == -1) && (position[1] == -1))
         {
            int exclude = r;
            for (r = 0; r < refactorings.size(); r++)
             {
                // Stops the loop from repeating the check for the previous refactoring.
                if ((r == exclude) && ((r + 1) < refactorings.size()))
                    r++;
                 else if (r == exclude)
                    break;

                refactorings.get(r).setServiceConfiguration(sc);
                position = super.randomElement(refactorings.get(r));

                if ((position[0] != -1) && (position[1] != -1))
                    break;
            }

            if ((position[0] == -1) && (position[1] == -1))
                r = -1;
        }

        return new int[] { r, position[0], position[1] };
    }

    // Uses Roulette Selection approach but instead of appropriating the fitness values of the solutions
    // it creates manually more standard proportions in case single fitness values are not available.
    // sp is the selective pressure and has been added as an input parameter in case it is desired to change it.
    private int[] rankSelection(int populationSize, float sp)
     {
        float[] rankProportions = new float[populationSize];
        int[] parents = new int[2];
        float fitnessSum = 0;
        float dynamicSum = 0;

        // Formula creates a balanced set of proportions for each rank.
        // This overcomes the scaling problems of the proportional fitness assignment.
        // The linear ranking formula takes in sp values in the range (1,2].
        for (int i = 0; i < populationSize; i++)
         {
            rankProportions[i] = (2 - sp) + 2 * (sp - 1) * (((populationSize - 1) - i) / (populationSize - 1));
            fitnessSum += rankProportions[i];
        }

        float rouletteSelection = (float)(Math.random() * fitnessSum);

        // Find the first parent iteration.
        for (int i = 0; i < populationSize; i++)
         {
            dynamicSum += rankProportions[i];

            if (dynamicSum >= rouletteSelection)
             {
                parents[0] = i;
                i = populationSize;
            }
        }

        rouletteSelection = (float)(Math.random() * fitnessSum);
        dynamicSum = 0;

        // Find the second parent iteration.
        for (int i = 0; i < populationSize; i++)
         {
            dynamicSum += rankProportions[i];

            if (dynamicSum >= rouletteSelection)
             {
                if (i != parents[0])
                    parents[1] = i;
                 else if (i < (populationSize - 1))
                    parents[1] = (i + 1);
                 else
                    parents[1] = (i - 1);

                i = populationSize;
            }
        }

        return parents;
    }

    // Output search information to results file.
    // Can be used for a population of solutions to generate separate results files.
    private void outputSearchInfo(String pathName, int solution, String runInfo)
     {
        pathName = pathName.substring(0, (pathName.length() - 1));
        pathName += "s/";
        String runName = String.format("%sresultsSolution%d.txt", pathName, solution);
        File dir = new File(pathName);
        if (!dir.exists())
            dir.mkdirs();

        try
         {
            BufferedWriter bw = new BufferedWriter(new FileWriter(runName, false));
            bw.write(String.format("======== Search Information ========"));
            bw.write(String.format("\r\n%s", runInfo));
            bw.close();
        }
         catch (IOException e)
         {
            System.out.println("\nEXCEPTION: Cannot export results to text file.");
            System.exit(1);
        }
    }

    // Output refactoring information to results file for a solution.
    // Can be used for when printing out the whole population.
    private void outputRefactoringInfo(String pathName, double time, double qualityGain, int solution, ArrayList<String> refactoringInfo)
     {
        String runName;

        // Create a location for the results output.
        if (solution == -1)
            runName = String.format("%sresults.txt", pathName);
         else
         {
            pathName = pathName.substring(0, (pathName.length() - 1));
            pathName += "s/";
            runName = String.format("%sresultsSolution%d.txt", pathName, solution);
        }

        File dir = new File(pathName);
        if (!dir.exists())
            dir.mkdirs();

        try
         {
            BufferedWriter bw = new BufferedWriter(new FileWriter(runName, true));
            bw.append("\r\n\r\n======== Applied Refactorings ========");

            for (int i = 0; i < refactoringInfo.size(); i++)
                bw.append(String.format("\r\n%s", refactoringInfo.get(i)));

            bw.append(String.format("\r\n\r\nScore has improved overall by %f", qualityGain));
            bw.append(String.format("\r\nTime taken to refactor: %.2fs", time));
            bw.close();
        }
         catch (IOException e)
         {
            System.out.println("\nEXCEPTION: Cannot export results to text file.");
            System.exit(1);
        }
    }

    // Outputs the metric values for a solution.
    // Can be used for when printing out the whole population.
    private void outputMetrics(float score, Metrics metric, boolean initial, boolean log, int solution, String pathName)
     {
        FitnessFunction ff = new FitnessFunction();
        String[] outputs = ff.createOutput(metric, super.c.getConfiguration());

        // Create a location for the results output.
        pathName = pathName.substring(0, (pathName.length() - 1));
        pathName += "s/";
        String runName = String.format("%sresultsSolution%d.txt", pathName, solution);
        File dir = new File(pathName);
        if (!dir.exists())
            dir.mkdirs();

        try
         {
            BufferedWriter bw = new BufferedWriter(new FileWriter(runName, true));

            if (initial)
                bw.append(String.format("\r\n\r\n======== Initial Metric Info ========"));
             else
                bw.append(String.format("\r\n\r\n======== Final Metric Info ========"));

            // Outputs the metric values for the project to a text file.
            for (int i = 0; i < outputs.length; i++)
                bw.append("\r\n" + outputs[i]);

            bw.append(String.format("\r\nOverall fitness function score: %f", score));
            bw.close();
        }
         catch (IOException e)
         {
            System.out.println("\nEXCEPTION: Cannot export results to text file.");
            System.exit(1);
        }

        if (log)
         {
            // Outputs the metric values for the project to the console for immediate feedback.
            System.out.printf("\n");

            for (int i = 0; i < outputs.length; i++)
             {
                if (outputs[i].charAt(outputs[i].length() - 7) == '.')
                    outputs[i] = outputs[i].substring(0, outputs[i].length() - 4);

                System.out.printf("\n%s", outputs[i]);
            }

            System.out.printf("\nOverall fitness function score: %.2f", score);
        }
    }
}
