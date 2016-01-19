package refactory;

import java.util.ArrayList;

public class FitnessFunction 
{
	// No attributes - empty constructor.
	public FitnessFunction(){}
	
	public float calculateScore(Metrics m, ArrayList<Triple<String, Boolean, Float>> configuration)
	{
		float amount = 0;
		float value = 0;

		for (Triple<String, Boolean, Float> metric : configuration)
		{
			switch (metric.getFirst()) 
			{
			case "classDesignSize":
				value = m.classDesignSize();
				break;
			case "numberOfMethods":
				value = m.numberOfMethods();
				break;
			case "methodsPerType":
				value = m.methodsPerType();
				break;
			case "abstractness":
				value = m.abstractness();
				break;
			case "abstractAmount":
				value = m.abstractAmount();
				break;
			case "staticAmount":
				value = m.staticAmount();
			break;
			case "finalAmount":
				value = m.finalAmount();
				break;
			case "innerClassAmount":
				value = m.innerClassAmount();
				break;
			case "numberOfHierarchies":
				value = m.numberOfHierarchies();
				break;
			case "averageNumberOfAncestors":
				value = m.averageNumberOfAncestors();
				break;
			case "cohesionAmongMethods":
				value = m.cohesionAmongMethods();
				break;
			case "directClassCoupling":
				value = m.directClassCoupling();
				break;
			case "childAmount":
				value = m.childAmount();
				break;
			case "linesOfCode":
				value = m.linesOfCode();
				break;
			case "fileAmount":
				value = m.fileAmount();
				break;
			case "visibility":
				value = m.visibility();
				break;
			case "classInterfaceSize":
				value = m.classInterfaceSize();
				break;
			case "dataAccessMetric":
				value = m.dataAccessMetric();
				break;
			default:
				value = 0;
			}
			
			amount += (metric.getSecond() == true) ? (metric.getThird() * value) : -(metric.getThird() * value);
		}

		return amount;
	}
	
	public boolean isParetoDominant(Metrics m1, Metrics m2, ArrayList<Triple<String, Boolean, Float>> configuration)
	{
		boolean better = false;
		float m1Value = 0;
		float m2Value = 0;

		for (Triple<String, Boolean, Float> metric : configuration)
		{
			switch (metric.getFirst()) 
			{
			case "classDesignSize":
				m1Value = m1.classDesignSize();
				m2Value = m2.classDesignSize();
				break;
			case "numberOfMethods":
				m1Value = m1.numberOfMethods();
				m2Value = m2.numberOfMethods();
				break;
			case "methodsPerType":
				m1Value = m1.methodsPerType();
				m2Value = m2.methodsPerType();
				break;
			case "abstractness":
				m1Value = m1.abstractness();
				m2Value = m2.abstractness();
				break;
			case "abstractAmount":
				m1Value = m1.abstractAmount();
				m2Value = m2.abstractAmount();
				break;
			case "staticAmount":
				m1Value = m1.staticAmount();
				m2Value = m2.staticAmount();
				break;
			case "finalAmount":
				m1Value = m1.finalAmount();
				m2Value = m2.finalAmount();
				break;
			case "innerClassAmount":
				m1Value = m1.innerClassAmount();
				m2Value = m2.innerClassAmount();
				break;
			case "numberOfHierarchies":
				m1Value = m1.numberOfHierarchies();
				m2Value = m2.numberOfHierarchies();
				break;
			case "averageNumberOfAncestors":
				m1Value = m1.averageNumberOfAncestors();
				m2Value = m2.averageNumberOfAncestors();
				break;
			case "cohesionAmongMethods":
				m1Value = m1.cohesionAmongMethods();
				m2Value = m2.cohesionAmongMethods();
				break;
			case "directClassCoupling":
				m1Value = m1.directClassCoupling();
				m2Value = m2.directClassCoupling();
				break;
			case "childAmount":
				m1Value = m1.childAmount();
				m2Value = m2.childAmount();
				break;
			case "linesOfCode":
				m1Value = m1.linesOfCode();
				m2Value = m2.linesOfCode();
				break;
			case "fileAmount":
				m1Value = m1.fileAmount();
				m2Value = m2.fileAmount();
				break;
			case "visibility":
				m1Value = m1.visibility();
				m2Value = m2.visibility();
				break;
			case "classInterfaceSize":
				m1Value = m1.classInterfaceSize();
				m2Value = m2.classInterfaceSize();
				break;
			case "dataAccessMetric":
				m1Value = m1.dataAccessMetric();
				m2Value = m2.dataAccessMetric();
				break;
			default:
				m1Value = 0;
				m2Value = 0;
			}
			
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
	
	public String[] createOutput(Metrics m, ArrayList<Triple<String, Boolean, Float>> configuration)
	{
		String[] outputs = new String[configuration.size()];

		for (int i = 0; i < configuration.size(); i++)
		{
			switch (configuration.get(i).getFirst()) 
			{
			case "classDesignSize":
				outputs[i] = String.format("Amount of classes in project: %d", m.classDesignSize());
				break;
			case "numberOfMethods":
				outputs[i] = String.format("Amount of methods in project: %d", m.numberOfMethods());
				break;
			case "methodsPerType":
				outputs[i] = String.format("Amount of methods per class: %f", m.methodsPerType());
				break;
			case "abstractness":
				outputs[i] = String.format("Ratio of interfaces to overall amount of classes: %.2f%%", m.abstractness());
				break;
			case "abstractAmount":
				outputs[i] = String.format("Amount of abstract classes/methods in project: %d", m.abstractAmount());
				break;
			case "staticAmount":
				outputs[i] = String.format("Amount of static methods/variables in project: %d", m.staticAmount());
			break;
			case "finalAmount":
				outputs[i] = String.format("Amount of final classes/methods/variables in project: %d", m.finalAmount());
				break;
			case "innerClassAmount":
				outputs[i] = String.format("Amount of inner classes in project: %d", m.innerClassAmount());
				break;
			case "numberOfHierarchies":
				outputs[i] = String.format("Amount of hierarchies in project: %d", m.numberOfHierarchies());
				break;
			case "averageNumberOfAncestors":
				outputs[i] = String.format("Amount of ancestors per class: %f", m.averageNumberOfAncestors());
				break;
			case "cohesionAmongMethods":
				outputs[i] = String.format("Amount of cohesion among methods per class: %f", m.cohesionAmongMethods());
				break;
			case "directClassCoupling":
				outputs[i] = String.format("Amount of coupling in project: %d", m.directClassCoupling ());
				break;
			case "childAmount":
				outputs[i] = String.format("Amount of child classes in project: %d", m.childAmount());
				break;
			case "linesOfCode":
				outputs[i] = String.format("Amount lines of code in project: %d", m.linesOfCode());
				break;
			case "fileAmount":
				outputs[i] = String.format("Amount of files in project: %d", m.fileAmount());
				break;
			case "visibility":
				outputs[i] = String.format("Amount of visibility in project: %d", m.visibility());
				break;
			case "classInterfaceSize":
				outputs[i] = String.format("Amount of public methods in project: %d", m.classInterfaceSize ());
				break;
			case "dataAccessMetric":
				outputs[i] = String.format("Accumulative ratio of private/protected attributes to overall attributes per class: %f", m.dataAccessMetric ());
				break;
			default:
				outputs[i] = "STRING INPUT DOES NOT RELATE TO A METRIC";
			}
			
		}

		return outputs;			
	}
}
