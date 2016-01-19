package edu.atilim.acma.search;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.UUID;
import java.util.Map.Entry;

import edu.atilim.acma.RunConfig;
import edu.atilim.acma.concurrent.Instance;
import edu.atilim.acma.concurrent.InstanceSet;
import edu.atilim.acma.design.Design;
import edu.atilim.acma.design.io.WriteClass;
import edu.atilim.acma.ui.ConfigManager;
import edu.atilim.acma.util.ACMAUtil;

public abstract class ConcurrentParallelBeeColony extends ConcurrentAlgorithm {
    private static final int randomDepth = 20;

    private static final int COMMAND_GENERATE = 1;
    private static final int COMMAND_STEP = 2;
    private static final int COMMAND_EXIT = 4;
    private static final int COMMAND_RESETEXPANSION = 5;
    private static final int COMMAND_DUMPEXPANSION = 6;

    private int maxTrials;
    private int populationSize;
    private int iterations;
    private int runCount;

    private transient int exhaust = 0;

    public ConcurrentParallelBeeColony() {
    }

    public ConcurrentParallelBeeColony(String name, RunConfig config, Design initialDesign, int maxTrials, int populationSize, int iterations, int runCount) {
        super(name, config, initialDesign);

        this.maxTrials = maxTrials;
        this.populationSize = populationSize;
        this.iterations = iterations;
        this.runCount = runCount;
    }

    @Override
     public void runMaster(InstanceSet instances) {

        long timeCounter = 0;
        double scoreCounter = 0;
        double qualityGainCounter = 0;
        double lowestQualityGain = Double.MAX_VALUE;
        double highestQualityGain = -Double.MAX_VALUE;
        double bestScore = 0;
        double worstScore = 0;

        int metricSize = ConfigManager.getMetrics(getConfig()).size();
        String[] metricNames = new String[metricSize];
        double[] initialMetricValues = new double[metricSize];
        double[] metricValuesCounter = new double[metricSize];
        double[] metricQualityGainCounter = new double[metricSize];
        double[] lowestMetricQualityGain = new double[metricSize];
        double[] highestMetricQualityGain = new double[metricSize];
        double[] bestMetricValue = new double[metricSize];
        double[] worstMetricValue = new double[metricSize];
        int counter = 0;
        String runName = "";

        for (int runs = 0; runs < runCount; runs++) {
            long startTime = System.currentTimeMillis();
            exhaust = 0;

            instances.broadcast(COMMAND_RESETEXPANSION);

            System.out.printf("Ordering food source generation to %d instances.\n", instances.size());

            boolean first = true;
            int sourcesPerInstance = populationSize / instances.size();
            int remainder = populationSize % instances.size();
            for (Instance i: instances) {
                i.send(COMMAND_GENERATE);

                if (first) i.send(sourcesPerInstance + remainder);
                 else i.send(sourcesPerInstance);

                first = false;
            }

            SolutionDesign best = null;
            SolutionDesign initial = new SolutionDesign(getInitialDesign(), getConfig());

            for (int i = 0; i < iterations; i++) {
                System.out.printf("Starting iteration %d, current best score: %.4f.\n", i + 1, best == null ? 0 : best.getScore());

                instances.broadcast(COMMAND_STEP);

                for (Instance instance: instances) {
                    SolutionDesign cur = new SolutionDesign(instance.receive(Design.class), getConfig());

                    if (best == null || cur.isBetterThan(best)) {
                        best = cur;
                        exhaust = 0;
                    } else
                        exhaust++;
                }
                instances.broadcast(best.getDesign());

                if (iterations == Integer.MAX_VALUE && exhaust > 750)
                    break;
            }

            System.out.printf("Finished %d iterations. Found best design with score: %.6f.\n", iterations, best.getScore());

            instances.broadcast(COMMAND_DUMPEXPANSION);
            expansion = 0;
            for (Instance i: instances) {
                expansion += i.receive(Long.class);
            }

            Design bestDesign = best.getDesign();
            long timeTaken = System.currentTimeMillis() - startTime;
            bestDesign.setTag(new RunInfoTag(timeTaken,
                    String.format("Artificial Bee Colony. Population Size: %d. Max Trials: %d. Iterations: %d.", populationSize, maxTrials, iterations), expansion));
            runName = onFinish(bestDesign, runCount);

            timeCounter += timeTaken;
            scoreCounter += best.getScore();
            double qualityGain = initial.getScore() - best.getScore();
            qualityGainCounter += qualityGain;
            if (qualityGain < lowestQualityGain)
                lowestQualityGain = qualityGain;
            if (qualityGain > highestQualityGain)
                highestQualityGain = qualityGain;
            bestScore = initial.getScore() - highestQualityGain;
            worstScore = initial.getScore() - lowestQualityGain;

            counter = 0;
            if (runs == 0)
                for (Entry<String, Double> e: initial.getMetricSummary().getMetrics().entrySet())
                {
                    metricNames[counter] = e.getKey();
                    initialMetricValues[counter] = e.getValue();
                    metricValuesCounter[counter] = 0;
                    metricQualityGainCounter[counter] = 0;
                    lowestMetricQualityGain[counter] = Double.MAX_VALUE;
                    highestMetricQualityGain[counter] = -Double.MAX_VALUE;
                    bestMetricValue[counter] = 0;
                    worstMetricValue[counter] = 0;
                    counter++;
            }

            counter = 0;
            for (Entry<String, Double> e: best.getMetricSummary().getMetrics().entrySet())
             {
                if (getConfig().isMetricEnabled(metricNames[counter]))
                 {
                    metricValuesCounter[counter] += e.getValue();
                    double metricQualityGain;
                    if (ConfigManager.getMetrics(getConfig()).get(counter).isMaximized())
                        metricQualityGain = e.getValue() - initialMetricValues[counter];
                     else
                        metricQualityGain = initialMetricValues[counter] - e.getValue();

                    metricQualityGainCounter[counter] += metricQualityGain;
                    if (metricQualityGain < lowestMetricQualityGain[counter])
                        lowestMetricQualityGain[counter] = metricQualityGain;
                    if (metricQualityGain > highestMetricQualityGain[counter])
                        highestMetricQualityGain[counter] = metricQualityGain;

                    if (ConfigManager.getMetrics(getConfig()).get(counter).isMaximized())
                     {
                        bestMetricValue[counter] = initialMetricValues[counter] + highestMetricQualityGain[counter];
                        worstMetricValue[counter] = initialMetricValues[counter] + lowestMetricQualityGain[counter];
                    }
                     else
                     {
                        bestMetricValue[counter] = initialMetricValues[counter] - highestMetricQualityGain[counter];
                        worstMetricValue[counter] = initialMetricValues[counter] - lowestMetricQualityGain[counter];
                    }
                }

                counter++;
            }

            if (runs == (runCount - 1))
             {
                WriteClass ex = new WriteClass(bestDesign);
                ex.afterBase();
            }
        }

        double time = timeCounter / 1000.0;

        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(runName, true));
            bw.append("====== Task Summary ======\r\n\r\n");
            bw.append("* General Info:\r\n");
            bw.append(String.format("  * Average time across all runs: %.2f seconds\r\n", time / runCount));
            bw.append(String.format("  * Average quality gain across all runs: %.6f (Average Score: %.6f)\r\n",
                                    qualityGainCounter / runCount, scoreCounter / runCount));
            bw.append(String.format("  * Highest quality gain across all runs: %.6f (Score: %.6f)\r\n", highestQualityGain, bestScore));
            bw.append(String.format("  * Lowest quality gain across all runs: %.6f (Score: %.6f)\r\n", lowestQualityGain, worstScore));
            bw.append(String.format("  * Time taken to complete task: %.2f seconds\r\n", time));
            bw.append("\r\n* Metric Summary:\r\n");

            for (int i = 0; i < metricSize; i++)
             {
                if (getConfig().isMetricEnabled(metricNames[i]))
                 {
                    bw.append(String.format("  * %s\r\n", metricNames[i]));
                    bw.append(String.format("    * Average Quality Gain: %.4f (Average Score: %.4f)\r\n",
                            metricQualityGainCounter[i] / runCount, metricValuesCounter[i] / runCount));
                    bw.append(String.format("    * Highest Quality Gain: %.4f (Best Score: %.4f)\r\n",
                            highestMetricQualityGain[i], bestMetricValue[i]));
                    bw.append(String.format("    * Lowest Quality Gain: %.4f (Worst Score: %.4f)\r\n",
                            lowestMetricQualityGain[i], worstMetricValue[i]));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try { bw.close(); } catch (Exception e) {}
        }

        instances.broadcast(COMMAND_EXIT);
    }

    private transient long expansion = 0;

    @Override
     public void runWorker(Instance master) {
        ArrayList<FoodSource> foods = new ArrayList<ConcurrentParallelBeeColony.FoodSource>();
        FoodSource best = null;

        infinite:
        while (true) {
            int command = master.receive(Integer.class);

            switch (command) {
                case COMMAND_GENERATE:
                    createFoodSources(foods, master.receive(Integer.class));
                    break;
                case COMMAND_STEP:
                    sendEmployedBees(foods);
                    master.send(getBestSource(foods).design.getDesign());
                    best = new FoodSource(new SolutionDesign(master.receive(Design.class), getConfig()));
                    calculateProbabilities(foods, best.getFitness());
                    sendOnlookerBees(foods);
                    sendScoutBees(foods, best.design);
                    break;
                case COMMAND_EXIT:
                    break infinite;
                case COMMAND_RESETEXPANSION:
                    expansion = 0;
                    break;
                case COMMAND_DUMPEXPANSION:
                    master.send(expansion);
                    break;
            }
        }

        System.out.println("Finalizing...");
    }

    private void createFoodSources(ArrayList<FoodSource> foods, int n) {
        System.out.printf("Creating %d food sources as initial population.\n", n);
        foods.clear();

        SolutionDesign initial = new SolutionDesign(getInitialDesign(), getConfig());
        for (int i = 0; i < n; i++) {
            foods.add(new FoodSource(initial.getRandomNeighbor(randomDepth)));
        }
        System.out.printf("Created food sources.\n", n);

        expansion += n;
    }

    private void sendEmployedBees(ArrayList<FoodSource> foods) {
        int better = 0;

        System.out.println("Sending employed bees.");

        for (int i = 0; i < foods.size(); i++) {
            FoodSource current = foods.get(i);
            FoodSource neighbor = current.mutate();

            if (neighbor.isBetterThan(current)) {
                foods.set(i, neighbor);
                better++;
            } else {
                current.trialCount++;
            }
        }

        expansion += foods.size();
    }

    private FoodSource getBestSource(ArrayList<FoodSource> foods) {
        FoodSource best = null;

        for (int i = 0; i < foods.size(); i++) {
            FoodSource current = foods.get(i);

            if (best == null || current.getFitness() > best.getFitness()) {
                best = current;
            }
        }

        return best;
    }

    private void calculateProbabilities(ArrayList<FoodSource> foods, double maxFitness) {
        System.out.println("Calculating probabilities.");

        for (int i = 0; i < foods.size(); i++) {
            FoodSource current = foods.get(i);

            current.probability = (0.9 * (current.getFitness() / maxFitness)) + 0.1;
        }
    }

    private void sendOnlookerBees(ArrayList<FoodSource> foods) {
        System.out.println("Sending onlooker bees.");

        for (int i = 0; i < foods.size(); i++) {
            FoodSource current = foods.get(i);

            if (current.probability > ACMAUtil.RANDOM.nextDouble()) {
                FoodSource neighbor = current.mutate();

                if (neighbor.isBetterThan(current)) {
                    foods.set(i, neighbor);
                } else {
                    current.trialCount++;
                }

                expansion++;
            }
        }
    }

    private void sendScoutBees(ArrayList<FoodSource> foods, SolutionDesign randomSource) {
        System.out.println("Sending scout bees.");

        for (int i = 0; i < foods.size(); i++) {
            FoodSource current = foods.get(i);

            if (current.trialCount > maxTrials) {
                foods.set(i, new FoodSource(randomSource.getRandomNeighbor(randomDepth)));
                expansion++;
            }
        }
    }

    @Override
     public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);

        out.writeInt(0); //version

        out.
        writeInt(maxTrials);
        out.writeInt(populationSize);
        out.writeInt(iterations);
        out.writeInt(runCount);
    }

    @Override
     public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);

        in.readInt();
        maxTrials = in.readInt();
        populationSize = in.readInt();
        iterations = in.readInt();
        runCount = in.readInt();
    }

    private class FoodSource {
        private UUID id;
        private SolutionDesign design;
        private int trialCount;
        private double probability;

        private FoodSource(SolutionDesign design) {
            this.id = UUID.randomUUID();

            this.design = design;
            this.trialCount = 0;
            this.probability = 0.0;
        }

        public double getFitness() {
            return 1.0 / design.getScore();
        }

        public FoodSource mutate() {
            return new FoodSource(design.getRandomNeighbor());
        }

        public boolean isBetterThan(FoodSource other) {
            return design.isBetterThan(other.design);
        }

        @Override
         public int hashCode() {
            return id.hashCode();
        }
    }
}
