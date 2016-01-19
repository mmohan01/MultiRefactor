package searches;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import recoder.CrossReferenceServiceConfiguration;
import recoder.io.PropertyNames;
import recoder.io.SourceFileRepository;
import recoder.java.CompilationUnit;
import refactorings.Refactoring;
import refactory.Configuration;
import refactory.FitnessFunction;
import refactory.Metrics;

public abstract class Search {
    protected CrossReferenceServiceConfiguration sc;
    protected Configuration c;
    protected String resultsPath;
    protected List<String> refactoringInfo;

    public Search(CrossReferenceServiceConfiguration sc, Configuration c)
     {
        this.sc = sc;
        this.c = c;
        String output = sc.getProjectSettings().getProperty(PropertyNames.OUTPUT_PATH);
        this.resultsPath = "./results/" + output.substring(output.lastIndexOf("/") + 1) + "/";
    }

    public abstract void run();

    protected abstract void print(SourceFileRepository sfr)
     {
        List<CompilationUnit> list = sfr.getKnownCompilationUnits();

        for (CompilationUnit cu: list)
         {
            //cu.validateAll();
            //if(!sfr.isUpToDate(cu))
            //	System.out.println("\nClass changed");

            try
             {
                sfr.print(cu);
            }
             catch (IOException e)
             {
                System.out.println("\nEXCEPTION: Cannot print changes to output.");
                System.exit(1);
            }
        }
    }

    protected abstract int[] randomElement(Refactoring r)
     {
        int[] position = new int[2];
        position[0] = (int) (Math.random() * this.sc.getSourceFileRepository().getKnownCompilationUnits().size());

        // Only counts the relevant program element.
        int amount = r.getAmount(position[0]);
        // Count the amount of available elements in the chosen class for refactoring.
        // If none are available find the next element up that is available.
        // If there are still no elements found look in the other direction and if there
        // are no elements available at all return -1 so it can be handled in the search.
        if (amount == 0)
         {
            int[] temp = new int[2];
            position[1] = 1;
            temp = nextElementUp(position[0], position[1], r);

            if ((temp[0] == -1) && (temp[1] == -1))
             {
                temp = nextElementDown(position[0], position[1], r);
            }

            position = temp;
        }
         else
            // Random element chosen from the available amount.
            position[1] = (int) (Math.random() * (amount - 1)) + 1;

        return position;
    }

    protected abstract int[] nextElementUp(int currentUnit, int currentElement, Refactoring r)
     {
        int[] position = new int[2];

        if (currentElement >= r.getAmount(currentUnit))
         {
            if (currentUnit == (this.sc.getSourceFileRepository().getKnownCompilationUnits().size() - 1))
             {
                position[0] = -1;
                position[1] = -1;
            }
             else
             {
                currentUnit++;

                int amount = 0;
                while (amount == 0)
                 {
                    if (currentUnit >= this.sc.getSourceFileRepository().getKnownCompilationUnits().size())
                     {
                        position[0] = -1;
                        position[1] = -1;
                        amount = -1;
                    }
                    // Only counts the relevant program element.
                    else
                        amount = r.getAmount(currentUnit);

                    if (amount == 0)
                        currentUnit++;
                }
                if (amount != -1)
                 {
                    position[0] = currentUnit;
                    position[1] = 1;
                }
            }
        }
         else
         {
            position[0] = currentUnit;
            position[1] = currentElement + 1;
        }

        return position;
    }

    protected abstract int[] nextElementDown(int currentUnit, int currentElement, Refactoring r)
     {
        int[] position = new int[2];

        if (currentElement <= 1)
         {
            if (currentUnit == 0)
             {
                position[0] = -1;
                position[1] = -1;
            }
             else
             {
                currentUnit--;

                int amount = 0;
                while (amount == 0)
                 {
                    if (currentUnit < 0)
                     {
                        position[0] = -1;
                        position[1] = -1;
                        amount = -1;
                    }
                    // Only counts the relevant program element.
                    else
                        amount = r.getAmount(currentUnit);

                    if (amount == 0)
                        currentUnit--;
                }
                if (amount != -1)
                 {
                    position[0] = currentUnit;
                    position[1] = amount;
                }
        // This is a makeshift solution to using the method consecutively
        // for different refactorings. If the element from the previous refactoring
        // is out of bounds of the current refactoring, the last element in the class
        // of the previous refactoring is used. This may not be the closest element
        // to the previous point in the program, but getting that would be awkward.
            }
        }
         else if (currentElement >= r.getAmount(currentUnit))
         {
            int amount = 0;
            while (amount == 0)
             {
                if (currentUnit < 0)
                 {
                    position[0] = -1;
                    position[1] = -1;
                    amount = -1;
                }
                // Only counts the relevant program element.
                else
                    amount = r.getAmount(currentUnit);

                if (amount == 0)
                    currentUnit--;
            }
            if (amount != -1)
             {
                position[0] = currentUnit;
                position[1] = amount;
            }
        }
         else
         {
            position[0] = currentUnit;
            position[1] = currentElement - 1;
        }

        return position;
    }

    protected abstract int[] getNeighbour(int currentUnit, int currentElement, int iteration, List<Refactoring> refactorings,
            boolean steepestDescent, FitnessFunction ff, Metrics m, float currentScore)
     {
        float newScore = currentScore;
        int[] position = new int[2];
        int[] neighbour = new int[3];
        neighbour[0] = currentUnit;
        neighbour[1] = currentElement;
        neighbour[2] = -1;

        for (int i = 0; i < refactorings.size(); i++)
         {
            double random = Math.random();
            if (random >= 0.5)
                position = nextElementUp(currentUnit, currentElement, refactorings.get(i));
             else
                position = nextElementDown(currentUnit, currentElement, refactorings.get(i));

            if ((position[0] != -1) && (position[1] != -1))
             {
                refactorings.get(i).transform(refactorings.get(i).analyze(iteration, position[0], position[1]));
                newScore = ff.calculateScore(m, this.c.getConfiguration());
                refactorings.get(i).transform(refactorings.get(i).analyzeReverse());

                if (newScore > currentScore)
                 {
                    neighbour[0] = position[0];
                    neighbour[1] = position[1];
                    neighbour[2] = i;

                    if (steepestDescent)
                        currentScore = newScore;
                     else
                        break;
                }
            }

            if (random >= 0.5)
                position = nextElementDown(currentUnit, currentElement, refactorings.get(i));
             else
                position = nextElementUp(currentUnit, currentElement, refactorings.get(i));

            if ((position[0] != -1) && (position[1] != -1))
             {
                refactorings.get(i).transform(refactorings.get(i).analyze(iteration, position[0], position[1]));
                newScore = ff.calculateScore(m, c.getConfiguration());
                refactorings.get(i).transform(refactorings.get(i).analyzeReverse());

                if (newScore > currentScore)
                 {
                    neighbour[0] = position[0];
                    neighbour[1] = position[1];
                    neighbour[2] = i;

                    if (steepestDescent)
                        currentScore = newScore;
                     else
                        break;
                }
            }
        }

        return neighbour;
    }

    // Output search information to results file.
    protected abstract void outputSearchInfo(String pathName, String runInfo)
     {
        String runName = String.format("%sresults.txt", pathName);
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

    // Output refactoring information to results file.
    protected abstract void outputRefactoringInfo(String pathName, double time, double qualityGain)
     {
        String runName = String.format("%sresults.txt", pathName);
        File dir = new File(pathName);
        if (!dir.exists())
            dir.mkdirs();

        try
         {
            BufferedWriter bw = new BufferedWriter(new FileWriter(runName, true));
            bw.append("\r\n\r\n======== Applied Refactorings ========");

            for (int i = 0; i < this.refactoringInfo.size(); i++)
                bw.append(String.format("\r\n%s", this.refactoringInfo.get(i)));

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

    // Outputs the metric values for the project.
    protected abstract void outputMetrics(float score, Metrics metric, boolean initial, boolean log, String pathName)
     {
        // Create a location for the results output.
        String runName = String.format("%sresults.txt", pathName);
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
            bw.append(String.format("\r\nAmount of classes in project: %d", metric.classDeclarationAmount()));
            bw.append(String.format("\r\nAmount of methods in project: %d", metric.methodDeclarationAmount()));
            bw.append(String.format("\r\nAmount of methods per class: %d", metric.methodsPerType()));
            bw.append(String.format("\r\nRatio of interfaces to overall amount of classes: %.2f%%", metric.abstractness()));
            bw.append(String.format("\r\nAmount of abstract classes/methods in project: %d", metric.abstractAmount()));
            bw.append(String.format("\r\nAmount of static methods/variables in project: %d", metric.staticAmount()));
            bw.append(String.format("\r\nAmount of final classes/methods/variables in project: %d", metric.finalAmount()));
            bw.append(String.format("\r\nAmount of inner classes in project: %d", metric.innerClassAmount()));
            bw.append(String.format("\r\nAmount of child classes in project: %d", metric.childAmount()));
            bw.append(String.format("\r\nAmount lines of code in project: %d", metric.linesOfCode()));
            bw.append(String.format("\r\nAmount of files in project: %d", metric.fileAmount()));
            bw.append(String.format("\r\nAmount of visibility in project: %d", metric.visibility()));
            bw.append(String.format("\r\nOverall score: %f", score));
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
            System.out.printf("\n\nAmount of classes in project: %d", metric.classDeclarationAmount());
            System.out.printf("\nAmount of methods in project: %d", metric.methodDeclarationAmount());
            System.out.printf("\nAmount of methods per class: %d", metric.methodsPerType());
            System.out.printf("\nRatio of interfaces to overall amount of classes: %.2f%%", metric.abstractness());
            System.out.printf("\nAmount of abstract classes/methods in project: %d", metric.abstractAmount());
            System.out.printf("\nAmount of static methods/variables in project: %d", metric.staticAmount());
            System.out.printf("\nAmount of final classes/methods/variables in project: %d", metric.finalAmount());
            System.out.printf("\nAmount of inner classes in project: %d", metric.innerClassAmount());
            System.out.printf("\nAmount of child classes in project: %d", metric.childAmount());
            System.out.printf("\nAmount lines of code in project: %d", metric.linesOfCode());
            System.out.printf("\nAmount of files in project: %d", metric.fileAmount());
            System.out.printf("\nAmount of visibility in project: %d", metric.visibility());
            System.out.printf("\nOverall score: %.2f", score);
        }
    }

    public abstract void setResultsPath(String resultsPath)
     {
        this.resultsPath = resultsPath;
    }

    public abstract String getResultsPath()
     {
        return this.resultsPath;
    }

    public abstract void setServiceConfiguration(CrossReferenceServiceConfiguration sc)
     {
        this.sc = sc;
    }

    public abstract void setConfiguration(Configuration c)
     {
        this.c = c;
    }
}
