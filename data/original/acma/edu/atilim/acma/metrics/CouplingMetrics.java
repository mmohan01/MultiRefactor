package edu.atilim.acma.metrics;

import edu.atilim.acma.design.Field;
import edu.atilim.acma.design.Method;
import edu.atilim.acma.design.Method.Parameter;
import edu.atilim.acma.design.Type;
import edu.atilim.acma.metrics.MetricTable.MetricRow;

public final class CouplingMetrics {
	@TypeMetric
	public static void calculateAttrMetrics(Type type, MetricRow row) {
		row.set("eC_Attr", type.getDependentFields().size());

		row.set("iC_Attr", 0);

		for (Field field : type.getFields()) {
			if (field.getType() != type) {
				row.increase("iC_Attr");
			}
		}
	}

	@TypeMetric
	public static void calculateMethodMetrics(Type type, MetricRow row) {
		row.set("eC_Par", type.getDependentMethodsAsParameter().size());

		row.set("iC_Par", 0);

		for (Method method : type.getMethods()) {
			for (Parameter parameter : method.getParameters()) {
				if (type != parameter.getType())
					row.increase("iC_Par");
			}
		}
	}

	@TypeMetric
	public static void calculateDependenceMetrics(Type type, MetricRow row) {

		int res = 0;
		int numOfDepAttr = type.getDependentFields().size();
		int numOfDepMetAsParameter = type.getDependentMethodsAsParameter().size(); 
		int numOfDepMetAsInstantiator = type.getDependentMethodsAsInstantiator().size();
		res = numOfDepAttr + numOfDepMetAsParameter + numOfDepMetAsInstantiator;

		row.set("Dep_In", res);
		row.set("Dep_Out", 0);

		for (Field field : type.getFields()) {
			if (field.getType() != type) {
				row.increase("Dep_Out");
			}
		}

		for (Method method : type.getMethods()) {
			for (Parameter parameter : method.getParameters()) {
				if (type != parameter.getType())
					row.increase("Dep_Out");
			}
		}
	}

	//TODO: This is dead slow O(n^3). Either move into package metrics, or omit entirely.
	//@TypeMetric
	public static void calculateAssocElementsMetrics(Type type, MetricRow row) {
		row.set("NumAssEl_ssc", 0);
		row.set("NumAssEl_nsb", 0);

		for (Type t : type.getPackage().getTypes()) {
			for(Field field : t.getFields())
			{
				if(field.getType()!=t && (field.getType()!=null && field.getType().getPackage().equals(type.getPackage()) ))
					row.increase("NumAssEl_ssc");
				else if(field.getType()!=t && field.getType()!=null )
					row.increase("NumAssEl_nsb");

			}

			for (Method method : t.getMethods()) {
				for (Parameter parameter : method.getParameters()) {
					if (t!= parameter.getType() && (parameter.getType()!=null && parameter.getType().getPackage().equals(type.getPackage())) )
						row.increase("NumAssEl_ssc");
					else if (t != parameter.getType() && parameter.getType()!=null)
						row.increase("NumAssEl_nsb");
				}

				if(t!=method.getReturnType() && (method.getReturnType()!=null && method.getReturnType().getPackage().equals(type.getPackage()) ) )
					row.increase("NumAssEl_ssc");
				else if (t != method.getReturnType() && method.getReturnType()!=null)
					row.increase("NumAssEl_nsb");
			}
		} // end of package types loop
	} // end of calculateAssocElementsMetrics method
}
