package multirefactor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FitnessFunction 
{
	ArrayList<Triple<String, Boolean, Float>> configuration;
	Map<String, Float> initialMetrics;

	// Only use if normalization functions (calculateBenchmark, calculateNormalizedScore) are not being used.
	public FitnessFunction(ArrayList<Triple<String, Boolean, Float>> configuration)
	{
		this.configuration = configuration;
	}
	
	public FitnessFunction(Metrics m, ArrayList<Triple<String, Boolean, Float>> configuration)
	{
		this.configuration = configuration;
		float baseline = 0.000001f;
		
		this.initialMetrics = new HashMap<String, Float>();
		initialMetrics.put("classDesignSize", (float) m.classDesignSize() == 0 ? baseline : (float) m.classDesignSize());
		initialMetrics.put("numberOfHierarchies", (float) m.numberOfHierarchies() == 0 ? baseline : (float) m.numberOfHierarchies());
		initialMetrics.put("averageNumberOfAncestors", m.averageNumberOfAncestors() == 0 ? baseline : m.averageNumberOfAncestors());
		initialMetrics.put("dataAccessMetric", m.dataAccessMetric() == 0 ? baseline : m.dataAccessMetric());
		initialMetrics.put("directClassCoupling", m.directClassCoupling() == 0 ? baseline : m.directClassCoupling());
		initialMetrics.put("cohesionAmongMethods", m.cohesionAmongMethods() == 0 ? baseline : m.cohesionAmongMethods());
		initialMetrics.put("aggregation", m.aggregation() == 0 ? baseline : m.aggregation());
		initialMetrics.put("functionalAbstraction", m.functionalAbstraction() == 0 ? baseline : m.functionalAbstraction());
		initialMetrics.put("numberOfPolymorphicMethods", m.numberOfPolymorphicMethods() == 0 ? baseline : m.numberOfPolymorphicMethods());
		initialMetrics.put("classInterfaceSize", m.classInterfaceSize() == 0 ? baseline : m.classInterfaceSize());
		initialMetrics.put("numberOfMethods", m.numberOfMethods() == 0 ? baseline : m.numberOfMethods());
		initialMetrics.put("weightedMethodsPerClass", m.weightedMethodsPerClass() == 0 ? baseline : m.weightedMethodsPerClass());
		initialMetrics.put("numberOfChildren", m.numberOfChildren() == 0 ? baseline : m.numberOfChildren());
		initialMetrics.put("abstractness", m.abstractness() == 0 ? baseline : m.abstractness());
		initialMetrics.put("abstractRatio", m.abstractRatio() == 0 ? baseline : m.abstractRatio());
		initialMetrics.put("staticRatio", m.staticRatio() == 0 ? baseline : m.staticRatio());
		initialMetrics.put("finalRatio", m.finalRatio() == 0 ? baseline : m.finalRatio());
		initialMetrics.put("constantRatio", m.constantRatio() == 0 ? baseline : m.constantRatio());
		initialMetrics.put("innerClassRatio", m.innerClassRatio() == 0 ? baseline : m.innerClassRatio());
		initialMetrics.put("referencedMethodsRatio", m.referencedMethodsRatio() == 0 ? baseline : m.referencedMethodsRatio());
		initialMetrics.put("visibilityRatio", m.visibilityRatio() == 0 ? baseline : m.visibilityRatio());
		initialMetrics.put("linesOfCode", (float) m.linesOfCode() == 0 ? baseline : (float) m.linesOfCode());
		initialMetrics.put("numberOfFiles", (float) m.numberOfFiles() == 0 ? baseline : (float) m.numberOfFiles());
	}
	
	public float calculateBenchmark()
	{
		float amount = 0;

		for (Triple<String, Boolean, Float> metric : this.configuration)
			if (this.initialMetrics.get(metric.getFirst()) != 0)
				amount += (metric.getSecond() == true) ? 1 : -1;
		
		return amount;
	}
	
	public float calculateScore(Metrics m)
	{
		float amount = 0;
		float value = 0;

		for (Triple<String, Boolean, Float> metric : this.configuration)
		{
			value = findMetricValue(m, metric.getFirst());			
			amount += (metric.getSecond() == true) ? (metric.getThird() * value) : -(metric.getThird() * value);
		}

		return amount;
	}
	
	public float calculateNormalizedScore(Metrics m)
	{
		float amount = 0;
		float value = 0;

		for (Triple<String, Boolean, Float> metric : this.configuration)
		{
			float metricValue = (findMetricValue(m, metric.getFirst()) == 0) ? 0.000001f : findMetricValue(m, metric.getFirst());
			value = metricValue / this.initialMetrics.get(metric.getFirst());
			value--;
			amount += (metric.getSecond() == true) ? (metric.getThird() * value) : -(metric.getThird() * value);
		}

		return amount;
	}
	
	public boolean isParetoDominant(Metrics m1, Metrics m2)
	{
		boolean better = false;
		float m1Value = 0;
		float m2Value = 0;

		for (Triple<String, Boolean, Float> metric : this.configuration)
		{
			m1Value = findMetricValue(m1, metric.getFirst());
			m2Value = findMetricValue(m2, metric.getFirst());

			if (metric.getSecond() == true)
			{
				if (m1Value > m2Value) 
					better = true;
				else if (m1Value < m2Value) 
					return false;
			}
			else
			{
				if (m1Value < m2Value) 
					better = true;
				else if (m1Value > m2Value) 
					return false;
			}
		}
		
		return better;
	}
	
	private float findMetricValue(Metrics m, String metric)
	{
		float value = 0;

		switch (metric) 
		{
		case "classDesignSize":
			value = m.classDesignSize();
			break;
		case "numberOfHierarchies":
			value = m.numberOfHierarchies();
			break;
		case "averageNumberOfAncestors":
			value = m.averageNumberOfAncestors();
			break;
		case "dataAccessMetric":
			value = m.dataAccessMetric();
			break;
		case "directClassCoupling":
			value = m.directClassCoupling();
			break;
		case "cohesionAmongMethods":
			value = m.cohesionAmongMethods();
			break;
		case "aggregation":
			value = m.aggregation();
			break;
		case "functionalAbstraction":
			value = m.functionalAbstraction();
			break;
		case "numberOfPolymorphicMethods":
			value = m.numberOfPolymorphicMethods();
			break;
		case "classInterfaceSize":
			value = m.classInterfaceSize();
			break;
		case "numberOfMethods":
			value = m.numberOfMethods();
			break;
		case "weightedMethodsPerClass":
			value = m.weightedMethodsPerClass();
			break;
		case "numberOfChildren":
			value = m.numberOfChildren();
			break;
		case "abstractness":
			value = m.abstractness();
			break;
		case "abstractRatio":
			value = m.abstractRatio();
			break;
		case "staticRatio":
			value = m.staticRatio();
			break;
		case "finalRatio":
			value = m.finalRatio();
			break;
		case "constantRatio":
			value = m.constantRatio();
			break;
		case "innerClassRatio":
			value = m.innerClassRatio();
			break;
		case "referencedMethodsRatio":
			value = m.referencedMethodsRatio();
			break;
		case "visibilityRatio":
			value = m.visibilityRatio();
			break;
		case "linesOfCode":
			value = m.linesOfCode();
			break;
		case "numberOfFiles":
			value = m.numberOfFiles();
			break;			
		default:
			value = 0;
		}
		
		return value;
	}
	
	public String[] createOutput(Metrics m)
	{
		String[] outputs = new String[this.configuration.size()];

		for (int i = 0; i < this.configuration.size(); i++)
		{
			switch (this.configuration.get(i).getFirst()) 
			{
			case "classDesignSize":
				outputs[i] = String.format("Amount of classes in project: %d", m.classDesignSize());
				break;
			case "numberOfHierarchies":
				outputs[i] = String.format("Amount of hierarchies in project: %d", m.numberOfHierarchies());
				break;
			case "averageNumberOfAncestors":
				outputs[i] = String.format("Amount of ancestors per class: %f", m.averageNumberOfAncestors());
				break;
			case "dataAccessMetric":
				outputs[i] = String.format("Average ratio of private/package/protected attributes to overall attributes per class: %f", m.dataAccessMetric ());
				break;
			case "directClassCoupling":
				outputs[i] = String.format("Average coupling per class: %f", m.directClassCoupling ());
				break;
			case "cohesionAmongMethods":
				outputs[i] = String.format("Average cohesion among methods per class: %f", m.cohesionAmongMethods());
				break;
			case "aggregation":
				outputs[i] = String.format("Average amount of user defined attributes declared per class: %f", m.aggregation());
				break;
			case "functionalAbstraction":
				outputs[i] = String.format("Average functional abstraction per class: %f", m.functionalAbstraction());
				break;
			case "numberOfPolymorphicMethods":
				outputs[i] = String.format("Average amount of polymorphic methods per class: %f", m.numberOfPolymorphicMethods());
				break;
			case "classInterfaceSize":
				outputs[i] = String.format("Average amount of public methods per class: %f", m.classInterfaceSize ());
				break;
			case "numberOfMethods":
				outputs[i] = String.format("Average amount of methods per class: %f", m.numberOfMethods());
				break;
			case "weightedMethodsPerClass":
				outputs[i] = String.format("Average amount of complexity of methods per class: %f", m.weightedMethodsPerClass());
				break;
			case "numberOfChildren":
				outputs[i] = String.format("Average amount of direct child classes per class: %f", m.numberOfChildren());
				break;
			case "abstractness":
				outputs[i] = String.format("Ratio of interfaces to overall amount of classes: %f", m.abstractness());
				break;
			case "abstractRatio":
				outputs[i] = String.format("Average ratio of abstract classes/methods per class: %f", m.abstractRatio());
				break;
			case "staticRatio":
				outputs[i] = String.format("Average ratio of static classes/methods/variables per class: %f", m.staticRatio());
				break;
			case "finalRatio":
				outputs[i] = String.format("Average ratio of final classes/methods/variables per class: %f", m.finalRatio());
				break;
			case "constantRatio":
				outputs[i] = String.format("Average ratio of constant classes/methods/variables per class: %f", m.constantRatio());
				break;
			case "innerClassRatio":
				outputs[i] = String.format("Ratio of inner classes to overall amount of ordinary classes in project: %f", m.innerClassRatio());
				break;
			case "referencedMethodsRatio":
				outputs[i] = String.format("Average inherited referenced methods ratio per class: %f", m.referencedMethodsRatio());
				break;
			case "visibilityRatio":
				outputs[i] = String.format("Average visibility ratio per class: %f", m.visibilityRatio());
				break;
			case "linesOfCode":
				outputs[i] = String.format("Amount of lines of code in project: %d", m.linesOfCode());
				break;
			case "numberOfFiles":
				outputs[i] = String.format("Amount of source files in project: %d", m.numberOfFiles());
				break;			
			default:
				outputs[i] = "STRING INPUT DOES NOT RELATE TO A METRIC";
			}	
		}

		return outputs;			
	}
}
