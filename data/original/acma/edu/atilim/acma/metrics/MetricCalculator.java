package edu.atilim.acma.metrics;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import edu.atilim.acma.RunConfig;
import edu.atilim.acma.design.Design;
import edu.atilim.acma.design.Package;
import edu.atilim.acma.design.Type;
import edu.atilim.acma.util.Log;
import edu.atilim.acma.util.Pair;

public class MetricCalculator {
	static {
		try {
			Class.forName("edu.atilim.acma.metrics.MetricRegistry");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private static final int METHOD_PACK_ROW = 0;
	private static final int METHOD_PACK_TABLE = 1;
	private static final int METHOD_TYPE_ROW = 2;
	private static final int METHOD_TYPE_TABLE = 3;
	
	private static ArrayList<Pair<Method, Integer>> methods;
	
	static void registerType(String name) throws Exception {
		if (methods == null) methods = new ArrayList<Pair<Method,Integer>>();
		
    	Class<?> c = Class.forName("edu.atilim.acma.metrics." + name);
    	
    	Log.config("Exploring all metric calculators in %s", c.getName());
    	
    	for (Method m : c.getDeclaredMethods()) {
    		Class<?>[] paramTypes = m.getParameterTypes();

    		if (m.isAnnotationPresent(TypeMetric.class)) {
    			if (paramTypes.length != 2)	throw new Exception("Metric calculator methods should accept two parameters.");
    			if (paramTypes[0] != Type.class) throw new Exception("Type metric calculator methods should accept a type parameter.");
    			int type = 0;
    			if (paramTypes[1] == MetricTable.MetricRow.class)
    				type = METHOD_TYPE_ROW;
    			else if (paramTypes[1] == MetricTable.class)
    				type = METHOD_TYPE_TABLE;
    			else
    				throw new Exception("Type metric calculator methods should accept a MetricTable or MetricRow.");
    			
    			methods.add(Pair.create(m, type));
    		}
    		
    		if (m.isAnnotationPresent(PackageMetric.class)) {
    			if (paramTypes.length != 2)	throw new Exception("Metric calculator methods should accept two parameters.");
    			if (paramTypes[0] != Package.class) throw new Exception("Package metric calculator methods should accept a package parameter.");
    			int type = 0;
    			if (paramTypes[1] == MetricTable.MetricRow.class)
    				type = METHOD_PACK_ROW;
    			else if (paramTypes[1] == MetricTable.class)
    				type = METHOD_PACK_TABLE;
    			else
    				throw new Exception("Package metric calculator methods should accept a MetricTable or MetricRow.");
    			
    			methods.add(Pair.create(m, type));
    		}
    	}
	}
	
	public static double normalize(MetricTable table) {
		return normalize(table, RunConfig.getDefault());
	}
	
	public static double normalize(MetricTable table, RunConfig config) {
		return MetricNormalizer.normalize(table.getSummary(), config);
	}
	
	public static MetricTable calculate(Design d) {
		return calculate(d, RunConfig.getDefault());
	}

	public static MetricTable calculate(Design d, RunConfig config) {
		List<Type> types = d.getTypes();
		List<Package> packages = d.getPackages();
		
		MetricTable table = new HashedMetricTable();
		
		if (methods  == null) return table;
		
		for (Pair<Method, Integer> pmt : methods) {
			Method m = pmt.getFirst();
			int mt = pmt.getSecond();
			
			for (int i = 0; i < types.size(); i++) {
				Type t = types.get(i);

				try {
					if (mt == METHOD_TYPE_TABLE)
						m.invoke(null, t, table);
					else if (mt == METHOD_TYPE_ROW)
						m.invoke(null, t, table.row(t));
				} catch (Exception e) {
					// This really should never happen.
					e.printStackTrace();
				}
			}
			
			for (int i = 0; i < packages.size(); i++) {
				Package p = packages.get(i);

				try {
					if (mt == METHOD_PACK_TABLE)
						m.invoke(null, p, table);
					else if (mt == METHOD_PACK_ROW)
						m.invoke(null, p, table.row(p));
				} catch (Exception e) {
					// This really should never happen.
					e.printStackTrace();
				}
			}
		}
		
		return table;
	}
}
