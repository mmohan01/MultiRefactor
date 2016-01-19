package edu.atilim.acma;

import java.util.ArrayList;
import java.util.List;

import edu.atilim.acma.concurrent.SwitchMetricModeTask;
import edu.atilim.acma.design.Design;
import edu.atilim.acma.design.io.ZIPDesignReader;
import edu.atilim.acma.search.ConcurrentRandomSearch;
import edu.atilim.acma.search.ConcurrentHillClimbing;
import edu.atilim.acma.search.ConcurrentSimAnn;
import edu.atilim.acma.search.ConcurrentBeeColony;
import edu.atilim.acma.search.ConcurrentParallelBeeColony;
import edu.atilim.acma.search.ConcurrentBeamSearch;
import edu.atilim.acma.search.ConcurrentStochasticBeamSearch;
import edu.atilim.acma.ui.ConfigManager;

public class CustomTaskQueueGenerator implements Runnable {
    @Override
     public void run()
     {
        System.out.println("Getting run config");

        List<Benchmark> benchmarks = new ArrayList<CustomTaskQueueGenerator.Benchmark>();
//		benchmarks.add(new Benchmark("JHotDraw", "./data/benchmarks/jhotdraw.zip", 1000));
//		benchmarks.add(new Benchmark("JFlex", "./data/benchmarks/jflex.zip", 1000));
//		benchmarks.add(new Benchmark("Mango", "./data/benchmarks/mango.zip", 1200));
        benchmarks.add(new Benchmark("JSON", "./data/benchmarks/json.zip", 1000));
//		benchmarks.add(new Benchmark("Apache-XmlRpc", "./data/benchmarks/apachexmlrpc.zip", 900));
//		benchmarks.add(new Benchmark("Beaver", "./data/benchmarks/beaver.zip", 600));

        RunConfig rc1 = ConfigManager.getRunConfig("Technical Debt");
        RunConfig rc2 = ConfigManager.getRunConfig("Coupling");
        RunConfig rc3 = ConfigManager.getRunConfig("Inheritance");
        RunConfig rc4 = ConfigManager.getRunConfig("Abstraction");

        for (Benchmark b: benchmarks)
         {
            Design design = new ZIPDesignReader(b.path).read();

            //RS
            TaskQueue.push(new ConcurrentRandomSearch(String.format("%s|%s|RS|I5000", b.name, "Technical Debt"), rc1, design, 5000, 1));
//			TaskQueue.push(new ConcurrentRandomSearch(String.format("%s|%s|RS|I5000", b.name, "Coupling"), rc2, design, 5000, 10));
//			TaskQueue.push(new ConcurrentRandomSearch(String.format("%s|%s|RS|I5000", b.name,"Inheritance"), rc3, design, 5000, 10));
//			TaskQueue.push(new ConcurrentRandomSearch(String.format("%s|%s|RS|I5000", b.name, "Abstraction"), rc4, design, 5000, 10));
//
//			//MSD-HC
//			TaskQueue.push(new ConcurrentHillClimbing(String.format("%s|%s|MSD-HC|R30|D5", b.name, "Technical Debt"), rc1, design, 30, 5, false, 10));
//			TaskQueue.push(new ConcurrentHillClimbing(String.format("%s|%s|MSD-HC|R30|D5", b.name, "Coupling"), rc2, design, 30, 5, false, 10));
//			TaskQueue.push(new ConcurrentHillClimbing(String.format("%s|%s|MSD-HC|R30|D5", b.name, "Inheritance"), rc3, design, 30, 5, false, 10));
//			TaskQueue.push(new ConcurrentHillClimbing(String.format("%s|%s|MSD-HC|R30|D5", b.name, "Abstraction"), rc4, design, 30, 5, false, 10));
//
//			//SA
//			TaskQueue.push(new ConcurrentSimAnn(String.format("%s|%s|SA|T1.5|I5000", b.name, "Technical Debt"), rc1, design, 1.5, 5000, 10));
//			TaskQueue.push(new ConcurrentSimAnn(String.format("%s|%s|SA|T1.5|I5000", b.name, "Coupling"), rc2, design, 1.5, 5000, 10));
//			TaskQueue.push(new ConcurrentSimAnn(String.format("%s|%s|SA|T1.5|I5000", b.name, "Inheritance"), rc3, design, 1.5, 5000, 10));
//			TaskQueue.push(new ConcurrentSimAnn(String.format("%s|%s|SA|T1.5|I5000", b.name, "Abstraction"), rc4, design, 1.5, 5000, 10));
        }

        // 6(12*10) = 720 runs - 72 tasks
        System.out.println("Completed...");
    }

    private class Benchmark {
        String name;
        String path;
        int iterations;

        private Benchmark(String name, String path, int iterations)
         {
            this.name = name;
            this.path = path;
            this.iterations = iterations;
        }
    }
}
