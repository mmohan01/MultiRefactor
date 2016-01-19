package edu.atilim.acma.search;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;

import edu.atilim.acma.RunConfig;
import edu.atilim.acma.concurrent.Command;
import edu.atilim.acma.concurrent.Instance;
import edu.atilim.acma.concurrent.InstanceSet;
import edu.atilim.acma.concurrent.TaskInterruptedException;
import edu.atilim.acma.design.Design;
import edu.atilim.acma.design.io.WriteClass;
import edu.atilim.acma.metrics.MetricRegistry;
import edu.atilim.acma.ui.ConfigManager;
import edu.atilim.acma.util.Delegate;

public abstract class ConcurrentMultiRunAlgorithm extends ConcurrentAlgorithm {
    private int runCount;

    public ConcurrentMultiRunAlgorithm() {

    }

    public ConcurrentMultiRunAlgorithm(String name, RunConfig config, Design initialDesign, int runCount) {
        super(name, config, initialDesign);

        this.runCount = runCount;
    }

    private transient int completed;
    private transient int assigned;

    @Override
     public void runMaster(InstanceSet instances) {
        System.out.printf("Master process started for %s\n", getName());

        completed = assigned = 0;

        final Delegate.Void1P<Instance> assigner = new Delegate.Void1P<Instance>(){
            @Override
             public void run(Instance in) {
                in.send(new StartCommand());
                assigned++;

                System.out.printf("Assigned task to %s. Remaining: %d.\n", in.getName(), runCount - assigned);
            }
        };

        final Delegate.Void1P<Instance> finalizer = new Delegate.Void1P<Instance>(){
            @Override
             public void run(Instance in) {
                in.send(new StopCommand());

                System.out.printf("Finalized instance %s.\n", in.getName());
            }
        };

        for (Instance i: instances) {
            if (assigned < runCount) {
                assigner.run(i);
            } else {
                finalizer.run(i);
            }
        }

        boolean flag = true;

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

        while (true) {

            if (isInterrupted())
                throw new TaskInterruptedException();

            for (Instance i: instances) {
                Design fdesign = i.tryReceive(Design.class);

                if (fdesign != null) {
                    System.out.printf("Received design from %s.\n", i.getName());

                    if (assigned < runCount) {
                        assigner.run(i);
                    } else {
                        finalizer.run(i);
                    }

                    completed++;
                    runName = onFinish(fdesign, runCount);

                    SolutionDesign initial = new SolutionDesign(getInitialDesign(), getConfig());
                    SolutionDesign best = new SolutionDesign(fdesign, getConfig());

                    RunInfoTag tag = (RunInfoTag)fdesign.getTag();
                    timeCounter += tag.getRunDuration();
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

                    if (completed == 1)
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
                }

                if ((completed == runCount) && flag)
                 {
                    WriteClass ex = new WriteClass(fdesign);
                    ex.afterBase();
                }
            }

            if ((completed == runCount) && flag)
             {
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

                flag = false;
                if (completed == runCount)
                    return;
            }

            try { Thread.sleep(50); } catch (InterruptedException e) {}
        }
    }

    private transient long runStart;
    private transient long expansion = 0;

    @Override
     public void runWorker(final Instance master) {
        System.out.printf("Worker process started for %s\n", getName());

        final AlgorithmObserver observer = new AlgorithmObserver(){

            @Override
             public void onUpdateItems(AbstractAlgorithm algorithm,
                    SolutionDesign current, SolutionDesign best, int updateType) {
    }

            @Override
             public void onStart(AbstractAlgorithm algorithm, SolutionDesign initial) {
    }

            @Override
             public void onLog(AbstractAlgorithm algorithm, String log) {
                System.out.println(log);
            }

            @Override
             public void onFinish(AbstractAlgorithm algorithm, SolutionDesign last) {
                System.out.println("Pushing result to master.");

                long elapsed = System.currentTimeMillis() - runStart;
                Design design = last.getDesign();
                design.setTag(new RunInfoTag(elapsed, getRunInfo(), expansion));

                master.send(last.getDesign());
            }

            @Override
             public void onExpansion(AbstractAlgorithm algorithm, int count) {
                expansion += count;
            }

            @Override
             public void onAdvance(AbstractAlgorithm algorithm, int current, int total) {
    }

            @Override
             public void onStep(AbstractAlgorithm algorithm, int step) {
                if (isInterrupted())
                    throw new TaskInterruptedException();
            }
        };

        while (true)
         {
            Command command = master.receive(Command.class);

            if (command.getCommand().equals(StopCommand.COMMAND))
                break;

            AbstractAlgorithm algorithm = spawnAlgorithm();
            algorithm.setObserver(observer);
            runStart = System.currentTimeMillis();
            expansion = 0;
            algorithm.start(false);
        }
    }

    public abstract AbstractAlgorithm spawnAlgorithm();
    public abstract String getRunInfo();

    public static class StartCommand implements Command {
        private static final long serialVersionUID = 1L;
        public static final String COMMAND = "START";

        @Override
         public String getCommand() {
            return COMMAND;
        }
    }

    public static class StopCommand implements Command {
        public static final String COMMAND = "STOP";
        private static final long serialVersionUID = 1L;

        @Override
         public String getCommand() {
            return COMMAND;
        }
    }

    @Override
     public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);

        out.writeInt(0); //version
        out.
        writeInt(runCount);
    }

    @Override
     public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);

        in.readInt();
        runCount = in.readInt();
    }
}
