package edu.atilim.acma.metrics;

import java.util.List;
import java.util.Stack;
import edu.atilim.acma.design.Type;
import edu.atilim.acma.metrics.MetricTable.MetricRow;

public final class InheritanceMetrics {
	@TypeMetric
	public static void calculateInterfacesMetrics(Type type, MetricRow row) {
		row.set("iFImpl", type.getInterfaces().size());
		row.set("NOC", type.getExtenders().size());
	}

	@TypeMetric
	public static void calculateNumberOfDesc(Type type, MetricRow row) {
		List<Type> typeList = null;
		Stack<Type> childrenTypes = new Stack<Type>();
		childrenTypes.push(type);

		row.set("NumDesc", 0);

		while( childrenTypes.size() > 0)
		{
			Type currChildrenType = childrenTypes.pop();
			typeList = currChildrenType.getExtenders();

			for(Type t : typeList)
			{
				childrenTypes.push(t);
				row.increase("NumDesc");
			}
		}
	}
	
	@TypeMetric
	public static void calculateNumberOfAnc(Type type, MetricRow row) {
		
		Type parType = type.getSuperType();
		row.set("NumAnc", 0);
		
		while(parType != null)
		{
			row.increase("NumAnc");
			parType = parType.getSuperType();
			
		}
	}
}
