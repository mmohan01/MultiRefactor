package edu.atilim.acma;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import edu.atilim.acma.design.Design;
import edu.atilim.acma.design.io.ZIPDesignReader;
import edu.atilim.acma.metrics.MetricSummary;
import edu.atilim.acma.transition.actions.Action;

public class ActionImpactCalculator implements Runnable {
	private static final int IMPACT_DECREASE = 1;
	private static final int IMPACT_INCREASE = 2;
	
	private HashMap<String, HashMap<String, Integer>> impactTable;
	
	@Override
	public void run() {
		System.out.println("Leading Eaop for action observations.");
		Design d = new ZIPDesignReader("./data/benchmarks/eaop.zip").read();
		
		if (d == null) {
			System.out.println("Could not read 'eaop.zip'.");
			System.exit(1);
		}
		
		System.out.println("Exploring all possible actions.");
		Set<Action> actions = d.getPossibleActions();
		System.out.printf("There are %d possible actions found.\n", actions.size());
		
		System.out.println("Calculating metrics.");
		MetricSummary summary = d.getMetrics().getSummary();
		System.out.println("Metrics calculated.");
		
		System.out.println("Starting operation. This may take a couple of minutes...");
		impactTable = new HashMap<String, HashMap<String,Integer>>();
		for (Action act : actions) {
			String type = act.getClass().getEnclosingClass().getSimpleName();
			
			Design cdesign = d.copy();
			act.perform(cdesign);
			
			MetricSummary csummary = cdesign.getMetrics().getSummary();
			
			for (Entry<String, Double> e : csummary.getMetrics().entrySet()) {
				if (e.getValue() + 1.0e-5 < summary.get(e.getKey())) {
					registerImpact(type, e.getKey(), IMPACT_DECREASE);
				} else 	if (e.getValue() - 1.0e-5 > summary.get(e.getKey())) {
					registerImpact(type, e.getKey(), IMPACT_INCREASE);
				}
			}
		}
		System.out.println("Completed operation.");
		
		for (Entry<String, HashMap<String, Integer>> e : impactTable.entrySet()) {
			String action = e.getKey();
			
			System.out.printf("%s:\n", action);
			
			for (Entry<String, Integer> me : e.getValue().entrySet()) {
				String metric = me.getKey();
				Integer impact = me.getValue();
				
				if (impact == null || impact == 0) continue;
				
				String impactText = "increase and decrease";
				if (impact == IMPACT_DECREASE)
					impactText = "decrease";
				else if (impact == IMPACT_INCREASE)
					impactText = "increase";
				
				System.out.printf("\t%s: %s\n", metric, impactText);
			}
		}
	}
	
	private void registerImpact(String action, String metric, int impact) {
		HashMap<String, Integer> imap = impactTable.get(action);
		if (imap == null) {
			imap = new HashMap<String, Integer>();
			impactTable.put(action, imap);
		}
		
		Integer value = imap.get(metric);
		if (value == null)
			value = impact;
		else
			value |= impact;
		
		imap.put(metric, value);
	}
}
