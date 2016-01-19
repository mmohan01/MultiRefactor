package edu.atilim.acma.metrics;

import java.util.List;

import edu.atilim.acma.design.Accessibility;
import edu.atilim.acma.design.Field;
import edu.atilim.acma.design.Method;
import edu.atilim.acma.design.Type;
import edu.atilim.acma.metrics.MetricTable.MetricRow;

public final class SizeMetrics {
	@TypeMetric
	public static void calculateFieldMetrics(Type type, MetricRow row) {
		row.set("numFields", type.getFields().size());
		
		row.set("numConstants", 0);
		
		int totVis = 0;
		
		for (Field f : type.getFields()) {
			if (f.isConstant())
				row.increase("numConstants");
			totVis += getVisibility(f.getAccess());
		}
		
		row.set("avrgFieldVisibility", totVis / row.get("numFields"));
 	}
	
	@TypeMetric
	public static void calculateMethodMetrics(Type type, MetricRow row) {
		List<Method> methods = type.getMethods();
		
		row.set("numOps", methods.size());
		
		row.set("getters", 0);
		row.set("setters", 0);
		
		int totVis = 0;
		int totStatic = 0;
		
		for (Method m : methods) {
			if (m.isStatic())
				totStatic++;
			
			totVis += getVisibility(m.getAccess());
			
			if (m.isGetter())
				row.increase("getters");
			else if (m.isSetter())
				row.increase("setters");
		}
		
		row.set("avrgMethodVisibility", totVis / row.get("numOps"));
		row.set("staticness", totStatic / row.get("numOps"));
	}
	
	private static int getVisibility(Accessibility access) {
		switch (access) {
		case PUBLIC:
			return 3;
		case PROTECTED:
			return 2;
		case PACKAGE:
			return 1;
		}
		
		return 0;
	}
}
