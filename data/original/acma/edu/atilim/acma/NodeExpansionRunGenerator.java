package edu.atilim.acma;

import java.util.ArrayList;
import java.util.List;

import edu.atilim.acma.design.Design;
import edu.atilim.acma.design.io.ZIPDesignReader;
import edu.atilim.acma.search.ConcurrentBeamSearch;
import edu.atilim.acma.search.ConcurrentHillClimbing;
import edu.atilim.acma.search.ConcurrentParallelBeeColony;
import edu.atilim.acma.search.ConcurrentStochasticBeamSearch;
import edu.atilim.acma.ui.ConfigManager;

public class NodeExpansionRunGenerator implements Runnable {

	@Override
	public void run() {
		System.out.println("Getting run config");
		RunConfig rc = ConfigManager.getRunConfig("Default");
		
		List<Benchmark> benchmarks = new ArrayList<NodeExpansionRunGenerator.Benchmark>();
		benchmarks.add(new Benchmark("Xmlrpc", "./data/benchmarks/apachexmlrpc.zip", 900));
		benchmarks.add(new Benchmark("Beaver", "./data/benchmarks/beaver.zip", 600));
		benchmarks.add(new Benchmark("Jflex", "./data/benchmarks/jflex.zip", 1000));
		benchmarks.add(new Benchmark("Json", "./data/benchmarks/json.zip", 400));
		benchmarks.add(new Benchmark("Mango", "./data/benchmarks/mango.zip", 1200));
		benchmarks.add(new Benchmark("Mosaic", "./data/benchmarks/mosaic.zip", 200));

		for (Benchmark b : benchmarks) {
			Design design = new ZIPDesignReader(b.path).read();
			
			//ABC
			TaskQueue.push(new ConcurrentParallelBeeColony(String.format("%s|ABC|20", b.name), rc, design, 50, 20, 7, 1));
			TaskQueue.push(new ConcurrentParallelBeeColony(String.format("%s|ABC|40", b.name), rc, design, 50, 40, 7500, 1));
			TaskQueue.push(new ConcurrentParallelBeeColony(String.format("%s|ABC|60", b.name), rc, design, 50, 60, 7500, 1));
			TaskQueue.push(new ConcurrentParallelBeeColony(String.format("%s|ABC|80", b.name), rc, design, 50, 80, 7500, 1));
			TaskQueue.push(new ConcurrentParallelBeeColony(String.format("%s|ABC|100", b.name), rc, design, 50, 100, 7500, 1));
			TaskQueue.push(new ConcurrentParallelBeeColony(String.format("%s|ABC|120", b.name), rc, design, 50, 120, 7500, 1));
			TaskQueue.push(new ConcurrentParallelBeeColony(String.format("%s|ABC|200", b.name), rc, design, 50, 200, 7500, 1));
			
			//LBS
			TaskQueue.push(new ConcurrentBeamSearch(String.format("%s|BS|20", b.name), rc, design, 20, 100, b.iterations, 1));
			TaskQueue.push(new ConcurrentBeamSearch(String.format("%s|BS|40", b.name), rc, design, 40, 100, b.iterations, 1));
			TaskQueue.push(new ConcurrentBeamSearch(String.format("%s|BS|60", b.name), rc, design, 60, 100, b.iterations, 1));
			
			//SBS
			TaskQueue.push(new ConcurrentStochasticBeamSearch(String.format("%s|SBS|20", b.name), rc, design, 20, 100, b.iterations, 1));
			TaskQueue.push(new ConcurrentStochasticBeamSearch(String.format("%s|SBS|40", b.name), rc, design, 40, 100, b.iterations, 1));
			TaskQueue.push(new ConcurrentStochasticBeamSearch(String.format("%s|SBS|60", b.name), rc, design, 60, 100, b.iterations, 1));
			
			//MSD
			TaskQueue.push(new ConcurrentHillClimbing(String.format("%s|MSD|R30|D5", b.name), rc, design, 30, 5, false, 1));
			TaskQueue.push(new ConcurrentHillClimbing(String.format("%s|MSD|R30|D10", b.name), rc, design, 30, 10, false, 1));
			TaskQueue.push(new ConcurrentHillClimbing(String.format("%s|MSD|R30|D15", b.name), rc, design, 30, 15, false, 1));
			TaskQueue.push(new ConcurrentHillClimbing(String.format("%s|MSD|R30|D20", b.name), rc, design, 30, 20, false, 1));
		}
		
		System.out.println("Completed...");
	}

	private class Benchmark {
		String name;
		String path;
		int iterations;
		
		private Benchmark(String name, String path, int iterations) {
			this.name = name;
			this.path = path;
			this.iterations = iterations;
		}
	}
}
