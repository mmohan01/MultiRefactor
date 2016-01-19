package edu.atilim.acma.metrics;

import java.util.Stack;

import edu.atilim.acma.design.Type;
import edu.atilim.acma.util.Pair;

public final class MiscMetrics {
	@TypeMetric
	public static void calculateNestingMetric(Type type, MetricTable table) {
		if(type.getParentType() != null) return;
		
		// Find inner types
		Stack<Pair<Type, Integer>> types = new Stack<Pair<Type,Integer>>();
		types.push(Pair.create(type, 0));
		
		// If there are more inner types to iterate on
		while (types.size() > 0) {
			// Get the first type to evaluate
			Pair<Type, Integer> current = types.pop();
			
			Type curType = current.getFirst();
			int curLevel = current.getSecond();
			
			// Push it's inner types to stack for further evaluation
			for (Type innerType : curType.getNestedTypes()) {
				types.push(Pair.create(innerType, curLevel + 1));
			}
			
			// Set the nesting metric of this type
			table.row(curType).set("nesting", curLevel);
		}
 	}
}
