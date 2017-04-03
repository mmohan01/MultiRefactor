package multirefactor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import recoder.abstraction.ClassType;
import recoder.abstraction.Method;
import recoder.abstraction.Type;
import recoder.convenience.AbstractTreeWalker;
import recoder.convenience.ForestWalker;
import recoder.convenience.TreeWalker;
import recoder.java.CompilationUnit;
import recoder.java.ProgramElement;
import recoder.java.declaration.ClassDeclaration;
import recoder.java.declaration.FieldDeclaration;
import recoder.java.declaration.FieldSpecification;
import recoder.java.declaration.InterfaceDeclaration;
import recoder.java.declaration.MemberDeclaration;
import recoder.java.declaration.MethodDeclaration;
import recoder.java.declaration.TypeDeclaration;
import recoder.java.declaration.VariableDeclaration;
import recoder.java.declaration.modifier.Private;
import recoder.java.declaration.modifier.Protected;
import recoder.java.declaration.modifier.Public;
import recoder.java.declaration.modifier.VisibilityModifier;
import recoder.java.reference.MethodReference;
import recoder.java.reference.TypeReference;
import recoder.kit.MethodKit;
import recoder.service.CrossReferenceSourceInfo;
import recoder.service.SourceInfo;
import refactorings.Refactoring;

// Calculates various software metrics from the source code input.
// Contains implementations of the full QMOOD suite and 2 metrics from the CK suite.
public class Metrics 
{
	private List<CompilationUnit> units;
	private ArrayList<String> affectedClasses;
	private HashMap<String, Integer> elementDiversity;
	private HashMap<String, Integer> elementScores;
	
	public Metrics(List<CompilationUnit> units)
	{
		this.units = units;
		this.elementScores = new HashMap<String, Integer>();
	}
	
	// Amount of classes in the project.
	// Includes both ordinary classes and interfaces.
	public int classDesignSize()
	{
		int classCounter = 0;
		ForestWalker tw = new ForestWalker(this.units);

		while (tw.next(TypeDeclaration.class))
		{
			TypeDeclaration td = (TypeDeclaration) tw.getProgramElement();
			if ((td.getName() != null) && ((td instanceof ClassDeclaration) || (td instanceof InterfaceDeclaration)))
				classCounter++;
		}
		
		return classCounter;
	}
	
	// Amount of distinct class hierarchies in the project.
	// Excludes classes from external libraries.
	public int numberOfHierarchies()
	{
		SourceInfo si = this.units.get(0).getFactory().getServiceConfiguration().getSourceInfo();
		Set<String> baseTypes = new HashSet<String>();

		for (int i = 0; i < this.units.size(); i++)
		{
			for (TypeDeclaration td : getAllTypes(this.units.get(i)))
			{
				if (td.isOrdinaryClass())
				{
					// Prevents "Zero Service" outputs logged to the console.
					if (td.getProgramModelInfo() == null)
						td.getFactory().getServiceConfiguration().getChangeHistory().updateModel();
					
					if (!(td.getSupertypes().get(0) instanceof TypeDeclaration) && (si.getSubtypes(td).size() > 0))
						baseTypes.add(td.getFullName());
				}
			}
		}
		
		return baseTypes.size();
	}
	
	// Average amount of classes away from the root per class.
	// Excludes classes from external libraries.
	public float averageNumberOfAncestors()
	{
		int classCounter = 0;
		int superTypeCounter = 0;

		for (int i = 0; i < this.units.size(); i++)
		{
			for (ClassType ct : getAllTypes(this.units.get(i)))
			{
				if (ct.isOrdinaryClass())
				{				
					// Prevents "Zero Service" outputs logged to the console.
					if (ct.getProgramModelInfo() == null)
						((ClassDeclaration) ct).getFactory().getServiceConfiguration().getChangeHistory().updateModel();

					classCounter++;
					while (ct.getSupertypes().get(0) instanceof TypeDeclaration)
					{
						superTypeCounter++;
						ct = ct.getSupertypes().get(0);
					}
				}
			}
		}

		return (float) superTypeCounter / (float) classCounter;
	}
	
	// Average ratio of the amount of private, package or 
	// protected attributes in a class to the overall amount per class.
	public float dataAccessMetric()
	{
		int counter, nonPublicCounter;
		int classCounter = 0;
		float dataAccessMetric = 0;
		
		for (int i = 0; i < this.units.size(); i++)
		{
			for (TypeDeclaration td : getAllTypes(this.units.get(i)))
			{
				if ((td instanceof ClassDeclaration) || (td instanceof InterfaceDeclaration))
				{
					counter = 0;
					nonPublicCounter = 0;
					classCounter++;
					
					for (MemberDeclaration md : td.getMembers())
					{
						if (md instanceof FieldDeclaration)
						{	
							counter++;
							if (!(((FieldDeclaration) md).getVisibilityModifier() instanceof Public))
								nonPublicCounter++;
						}
					}
					
					if (counter > 0)
						dataAccessMetric += (float) nonPublicCounter / (float) counter;
				}
			}
		}
		
		return dataAccessMetric / (float) classCounter;
	}
	
	// Average number of other distinct classes each class depends on per class.
	// Only includes user defined classes from the project.
	public float directClassCoupling()
	{
		int couplingCounter = 0;
		int classCounter = 0;
		Set<String> distinctTypes;
		SourceInfo si = this.units.get(0).getFactory().getServiceConfiguration().getSourceInfo();

		for (int i = 0; i < this.units.size(); i++)
		{			
			for (TypeDeclaration td : getAllTypes(this.units.get(i)))
			{
				if ((td instanceof ClassDeclaration) || (td instanceof InterfaceDeclaration))
				{
					classCounter++;
					distinctTypes = new HashSet<String>();
					
					// Prevents "Zero Service" outputs logged to the console.
					if (td.getProgramModelInfo() == null)
						td.getFactory().getServiceConfiguration().getChangeHistory().updateModel();

					for (MemberDeclaration md : td.getMembers())
						if (md instanceof MethodDeclaration)
							for (Type t : ((MethodDeclaration) md).getSignature())
								if ((t != null) && ((t instanceof ClassDeclaration) || (t instanceof InterfaceDeclaration)))
									distinctTypes.add(t.getFullName());

					for (FieldSpecification fs : td.getFieldsInScope())
					{
						TreeWalker tw = new TreeWalker(fs);
						while (tw.next(TypeReference.class)) 
						{
							Type t = si.getType(tw.getProgramElement());

							if ((t != null) && ((t instanceof ClassDeclaration) || (t instanceof InterfaceDeclaration)))
								distinctTypes.add(t.getFullName()); 
						}
					}

					couplingCounter += distinctTypes.size();	
				}
			}
		}

		return (float) couplingCounter / (float) classCounter;
	}

	// Average cohesion among methods ratio per class.
	// Ratio gets the accumulation of the amount of distinct parameter types for each method
	// over the maximum possible amount of distinct parameter types across all the methods.
	// Denominator is calculated by multiplying the amount of methods by the amount of
	// distinct parameter types in all of the methods of the class.
	public float cohesionAmongMethods()
	{
		int methodCounter, cohesionCounter;
		int classCounter = 0;
		float cohesionAmongMethods = 0;

		ArrayList<String> types;
		ArrayList<String> allTypes;
		Set<String> distinctTypes;

		for (int i = 0; i < this.units.size(); i++)
		{		
			for (TypeDeclaration td : getAllTypes(this.units.get(i)))
			{
				if ((td instanceof ClassDeclaration) || (td instanceof InterfaceDeclaration))
				{
					classCounter++;
					methodCounter = 0;
					cohesionCounter = 0;
					allTypes = new ArrayList<String>();
				
					// Prevents "Zero Service" outputs logged to the console.
					if (td.getProgramModelInfo() == null)
						td.getFactory().getServiceConfiguration().getChangeHistory().updateModel();
					
					for (MemberDeclaration md : td.getMembers())
					{
						if (md instanceof MethodDeclaration)
						{
							methodCounter++;
							types = new ArrayList<String>();
							
							for (Type t : ((MethodDeclaration) md).getSignature())
							{
								types.add(t.getFullName());
								allTypes.add(t.getFullName());
							}

							distinctTypes = new HashSet<String>(types);
							cohesionCounter += distinctTypes.size();
						}
					}

					distinctTypes = new HashSet<String>(allTypes);

					if ((methodCounter * distinctTypes.size()) > 0)
						cohesionAmongMethods += (float) cohesionCounter / (float) (methodCounter * distinctTypes.size());
				}
			}
		}

		return cohesionAmongMethods / (float) classCounter;
	}

	// Average amount of user defined attributes declared per class.
	// Only counts classes defined in the project.
	public float aggregation()
	{
		int counter = 0;
		int classCounter = 0;
		SourceInfo si = this.units.get(0).getFactory().getServiceConfiguration().getSourceInfo();
		
		for (int i = 0; i < this.units.size(); i++)
		{			
			for (TypeDeclaration td : getAllTypes(this.units.get(i)))
			{
				if ((td instanceof ClassDeclaration) || (td instanceof InterfaceDeclaration))
				{
					classCounter++;
					for (FieldSpecification f : td.getFieldsInScope())
					{
						Type t = si.getType(f);	
						if ((t != null) && ((t instanceof ClassDeclaration) || (t instanceof InterfaceDeclaration)))
							counter++;
					}
				}
			}
		}

		return (float) counter / (float) classCounter;
	}
	
	// Average functional abstraction ratio per class.
	// Ratio gets the amount of inherited methods accessible within a class
	// (methods declared in a super class of the current class that are public
	// or protected, or are package and contain the same package) over the overall
	// amount of methods accessible (inherited and declared within the class) to the class.
	// Excludes methods inherited from external library classes.
	public float functionalAbstraction()
	{
		int counter, inheritedCounter;
		int classCounter = 0;
		float functionalAbstraction = 0;
		
		for (int i = 0; i < this.units.size(); i++)
		{
			for (TypeDeclaration td : getAllTypes(this.units.get(i)))
			{
				if ((td instanceof ClassDeclaration) || (td instanceof InterfaceDeclaration))
				{
					classCounter++;
					inheritedCounter = 0;
					counter = 0;
					TypeDeclaration superType = td;
					
					for (MemberDeclaration md : td.getMembers())
						if (md instanceof MethodDeclaration)
							counter++;
					
					// Prevents "Zero Service" outputs logged to the console.
					if (td.getProgramModelInfo() == null)
						td.getFactory().getServiceConfiguration().getChangeHistory().updateModel();
					
					while (superType.getSupertypes().get(0) instanceof TypeDeclaration)
					{
						superType = (TypeDeclaration) superType.getSupertypes().get(0);
						
						for (MemberDeclaration md : td.getMembers())
							if (md instanceof MethodDeclaration)
								if ((md.isPublic()) || (md.isProtected()) || (!(md.isPrivate()) && (td.getPackage().equals(superType.getPackage()))))
									inheritedCounter++;
					}
					
					counter += inheritedCounter;
					
					if (counter > 0)
						functionalAbstraction += (float) inheritedCounter / (float) counter;
				}
			}
		}
		
		return functionalAbstraction / (float) classCounter;
	}
	
	// Average amount of polymorphic methods 
	// (methods that are redefined/overwritten) per class.
	// Abstract method declarations and constructors are included.
	public float numberOfPolymorphicMethods()
	{
		int counter = 0;
		int classCounter = 0;
		SourceInfo si = this.units.get(0).getFactory().getServiceConfiguration().getSourceInfo();
		
		for (int i = 0; i < this.units.size(); i++)
		{			
			for (TypeDeclaration td : getAllTypes(this.units.get(i)))
			{
				if ((td instanceof ClassDeclaration) || (td instanceof InterfaceDeclaration))
				{
					// Prevents "Zero Service" outputs logged to the console.
					if (td.getProgramModelInfo() == null)
						td.getFactory().getServiceConfiguration().getChangeHistory().updateModel();
					
					classCounter++;
					for (MemberDeclaration md : td.getMembers())
						if (md instanceof MethodDeclaration)							
							if (MethodKit.getRedefiningMethods((CrossReferenceSourceInfo) si, (Method) md).size() > 0)
								counter++;
				}
			}
		}
		
		return (float) counter / (float) classCounter;
	}
	
	// Average amount of public methods per class.
	public float classInterfaceSize()
	{
		int counter = 0;
		int classCounter = 0;
		
		for (int i = 0; i < this.units.size(); i++)
		{
			for (TypeDeclaration td : getAllTypes(this.units.get(i)))
			{
				if ((td instanceof ClassDeclaration) || (td instanceof InterfaceDeclaration))
				{
					classCounter++;
					for (MemberDeclaration md : td.getMembers())
						if ((md instanceof MethodDeclaration) && (((MethodDeclaration) md).getVisibilityModifier() instanceof Public))
							counter++;
				}
			}
		}

		return (float) counter / (float) classCounter;
	}
	
	// Average amount of methods per class.
	public float numberOfMethods()
	{
		int classCounter = 0;
		int methodCounter = 0;

		for (int i = 0; i < this.units.size(); i++)
		{			
			for (TypeDeclaration td : getAllTypes(this.units.get(i)))
			{
				if ((td instanceof ClassDeclaration) || (td instanceof InterfaceDeclaration))
				{
					classCounter++;
					for (MemberDeclaration md : td.getMembers())
						if (md instanceof MethodDeclaration)
							methodCounter++;
				}
			}
		}

		return (float) methodCounter / (float) classCounter;
	}
	
	
	// Average amount of complexity of all methods per class.
	// The complexity is calculated using the amount of lines of code per method.
	public float weightedMethodsPerClass()
	{
		int classCounter = 0;
		int methodCounter = 0;
		
		for (int i = 0; i < this.units.size(); i++)
		{
			for (TypeDeclaration td : getAllTypes(this.units.get(i)))
			{
				if ((td instanceof ClassDeclaration) || (td instanceof InterfaceDeclaration))
				{
					classCounter++;
					for (MemberDeclaration md : td.getMembers())
						if (md instanceof MethodDeclaration)
							methodCounter += (md.getEndPosition().getLine() - md.getStartPosition().getLine() + 1);
				}
			}
		}
		
		return (float) methodCounter / (float) classCounter;
	}
	
	// Average amount of direct child classes per class.
	// Only includes ordinary classes within the project.
	public float numberOfChildren()
	{		
		int childCounter = 0;
		int classCounter = 0;
		
		for (int i = 0; i < this.units.size(); i++)
		{
			for (TypeDeclaration td : getAllTypes(this.units.get(i)))
			{				
				if (td.isOrdinaryClass())
				{					
					// Prevents "Zero Service" outputs logged to the console.
					if (td.getProgramModelInfo() == null)
						td.getFactory().getServiceConfiguration().getChangeHistory().updateModel();

					classCounter++;
					if (td.getSupertypes().get(0) instanceof TypeDeclaration)
						childCounter++;
				}
			}
		}

		return (float) childCounter / (float) classCounter;
	}
	
	
	// Ratio of the amount of interfaces over the overall amount of classes.
	public float abstractness()
	{
		int classCounter = 0;
		int interfaceCounter = 0;
		ForestWalker tw = new ForestWalker(this.units);

		while (tw.next(TypeDeclaration.class))
		{
			TypeDeclaration td = (TypeDeclaration) tw.getProgramElement();
			if ((td.getName() != null) && ((td instanceof ClassDeclaration) || (td instanceof InterfaceDeclaration)))
			{
				classCounter++;
				if (td instanceof InterfaceDeclaration)
					interfaceCounter++;
			}
		}
		
		float answer = (float) interfaceCounter / (float) classCounter;
		return answer;
	}
	
	// Average ratio of abstract elements over abstract
	// and potentially abstract elements per class.
	// Variables can't be abstract.
	public float abstractRatio()
	{
		int counter, abstractCounter;
		int classCounter = 0;
		float abstractAmount = 0;
		
		for (int i = 0; i < this.units.size(); i++)
		{
			for (TypeDeclaration td : getAllTypes(this.units.get(i)))
			{				
				if ((td instanceof ClassDeclaration) || (td instanceof InterfaceDeclaration))
				{
					counter = 1;
					abstractCounter = 0;
					classCounter++;
					
					if (td.isAbstract())
						abstractCounter++;
				
					for (MemberDeclaration md : td.getMembers())
					{
						if (md instanceof MethodDeclaration)
						{
							counter++;
							if (((MethodDeclaration) md).isAbstract())
								abstractCounter++;
						}
					}

					abstractAmount += (float) abstractCounter / (float) counter;
				}
			}
		}
		
		return abstractAmount / (float) classCounter;
	}

	// Average ratio of static elements over static
	// and potentially static elements per class.
	// Of the variable declarations, only a field can be static.
	public float staticRatio()
	{
		int counter, staticCounter;
		int classCounter = 0;
		float staticAmount = 0;
		
		for (int i = 0; i < this.units.size(); i++)
		{
			for (TypeDeclaration td : getAllTypes(this.units.get(i)))
			{				
				if ((td instanceof ClassDeclaration) || (td instanceof InterfaceDeclaration))
				{
					counter = 1;
				   	staticCounter = 0;
					classCounter++;
					
					if (td.isStatic())
						staticCounter++;
					
					for (MemberDeclaration md : td.getMembers())
					{
						if ((md instanceof MethodDeclaration) || (md instanceof FieldDeclaration))
						{
							counter++;
							if (md.isStatic())
								staticCounter++;
						}
					}
					
					staticAmount += (float) staticCounter / (float) counter;
				}
			}
		}

		return staticAmount / (float) classCounter;
	}
	
	// Average ratio of final elements over final
	// and potentially final elements per class.
	public float finalRatio()
	{
		int counter, finalCounter;
		int classCounter = 0;
		float finalAmount = 0;
		
		for (int i = 0; i < this.units.size(); i++)
		{
			for (TypeDeclaration td : getAllTypes(this.units.get(i)))
			{				
				if ((td instanceof ClassDeclaration) || (td instanceof InterfaceDeclaration))
				{
					counter = 1;
					finalCounter = 0;
					classCounter++;
					
					if (td.isFinal())
						finalCounter++;
					
					for (MemberDeclaration md : td.getMembers())
					{
						if (md instanceof MethodDeclaration)
						{
							counter++;
							if (((MethodDeclaration) md).isFinal())
								finalCounter++;
							
							TreeWalker tw = new TreeWalker(md);
							
							while (tw.next(VariableDeclaration.class))
							{
								counter++;
								VariableDeclaration vd = (VariableDeclaration)(tw.getProgramElement());
								if (vd.isFinal())
									finalCounter++;
							}
						}
						else if (md instanceof FieldDeclaration)
						{
							counter++;
							if (((FieldDeclaration) md).isFinal())
								finalCounter++;
						}
					}
					
					finalAmount += (float) finalCounter / (float) counter;
				}
			}
		}

		return finalAmount / (float) classCounter;
	}
	
	// Average ratio of constant elements over constant
	// and potentially constant elements per class.
	// Of the variable declarations, only a field can be constant.
	public float constantRatio()
	{
		int counter, constantCounter;
		int classCounter = 0;
		float constantAmount = 0;
		
		for (int i = 0; i < this.units.size(); i++)
		{
			for (TypeDeclaration td : getAllTypes(this.units.get(i)))
			{				
				if ((td instanceof ClassDeclaration) || (td instanceof InterfaceDeclaration))
				{
					counter = 1;
					constantCounter = 0;
					classCounter++;
					
					if ((td.isStatic()) && (td.isFinal()))
						constantCounter++;
					
					for (MemberDeclaration md : td.getMembers())
					{
						if (md instanceof MethodDeclaration)
						{
							counter++;
							if ((md.isStatic()) && (((MethodDeclaration) md).isFinal()))
								constantCounter++;
						}
						else if (md instanceof FieldDeclaration)
						{
							counter++;
							if ((md.isStatic()) && (((FieldDeclaration) md).isFinal()))
								constantCounter++;
						}
					}
					
					constantAmount += (float) constantCounter / (float) counter;
				}
			}
		}

		return constantAmount / (float) classCounter;
	}
	
	// Ratio of amount of classes in the project that are 
	// declared inside other classes over the amount of classes.
	public float innerClassRatio()
	{
		int innerClassCounter = 0;
		int classCounter = 0;
		
		for (int i = 0; i < this.units.size(); i++)
		{			
			for (TypeDeclaration td : getAllTypes(this.units.get(i)))
			{				
				if ((td instanceof ClassDeclaration) || (td instanceof InterfaceDeclaration))
				{
					classCounter++;
					
					if (td.getContainingClassType() != null)
						innerClassCounter++;
				}
			}
		}
		
		return (float) innerClassCounter / (float) classCounter;
	}	
	
	// Average referenced inherited methods ratio per class.
	// Ratio gets the accumulation of the amount of inherited external methods accessed within
	// the methods of a class (methods declared in a super class of the current class) over
	// the overall distinct amount of external methods accessed in the methods of the class.
	public float referencedMethodsRatio()
	{
		int methodCount, inheritedMethodCount;
		int classCounter = 0;
		float referencedMethodsRatio = 0;

		ArrayList<MethodDeclaration> methods;
		SourceInfo si = this.units.get(0).getFactory().getServiceConfiguration().getSourceInfo();

		for (int i = 0; i < this.units.size(); i++)
		{
			for (TypeDeclaration td : getAllTypes(this.units.get(i)))
			{
				if ((td instanceof ClassDeclaration) || (td instanceof InterfaceDeclaration))
				{
					classCounter++;
					methods = new ArrayList<MethodDeclaration>();

					for (MemberDeclaration md : td.getMembers())
					{
						if (md instanceof MethodDeclaration)
						{							
							TreeWalker tw = new TreeWalker(md);
							while (tw.next(MethodReference.class)) 
							{
								ProgramElement pe = tw.getProgramElement();
								if (si.getMethod((MethodReference) pe) instanceof MethodDeclaration)
								{
									MethodDeclaration method = (MethodDeclaration) si.getMethod((MethodReference) pe);

									if (!(methods.contains(method)) && !(method.getContainingClassType().equals(td)))
										methods.add(method);
								}
							}
						}
					}

					methodCount = methods.size();
					inheritedMethodCount = 0;

					for (MethodDeclaration md : methods)
						if (td.getAllSupertypes().contains(md.getContainingClassType()))
							inheritedMethodCount++;

					if (methodCount > 0)
						referencedMethodsRatio += (float) inheritedMethodCount / (float) methodCount;
				}
			}
		}

		return referencedMethodsRatio / (float) classCounter;
	}
	
	// Average visibility ratio per class.
	// Ratio calculates the accumulative visibility value among 
	// type, method and variable declarations over the amount of 
	// declarations, where a higher value means more visibility.
	public float visibilityRatio()
	{
		int counter;
		float visibilityCounter;
		int classCounter = 0;
		float visibility = 0;
		
		for (int i = 0; i < this.units.size(); i++)
		{
			for (TypeDeclaration td : getAllTypes(this.units.get(i)))
			{				
				if ((td instanceof ClassDeclaration) || (td instanceof InterfaceDeclaration))
				{
					counter = 1;
					classCounter++;
					
					visibilityCounter = identifier(td.getVisibilityModifier());
					
					for (MemberDeclaration md : td.getMembers())
					{
						if (md instanceof MethodDeclaration)
						{
							counter++;
							visibilityCounter += identifier(((MethodDeclaration) md).getVisibilityModifier());
						}
						else if (md instanceof FieldDeclaration)
						{
							counter++;
							visibilityCounter += identifier(((FieldDeclaration) md).getVisibilityModifier());
						}
					}
					
					visibility += (float) visibilityCounter / (float) counter;
				}
			}
		}

		return visibility / (float) classCounter;
	}
	
	// Amount of lines of code in the project.
	public int linesOfCode()
	{		
		int lineCounter = 0;

		for (CompilationUnit u : this.units)
			lineCounter += u.getEndPosition().getLine();

		return lineCounter;
	}

	// Amount of java source files in the project.
	public int numberOfFiles()
	{
		return this.units.size();
	}
	
	// Instances of priority classes (most important classes determined 
	// by the user) affected by the refactorings of a solution.
	public int priority(ArrayList<String> priorityClasses)
	{		
		int priorityAmount = 0;

		for (String s1 : this.affectedClasses)
		{
			for (String s2 : priorityClasses)
			{
				if (s1.endsWith(s2))
				{
					priorityAmount++;
					break;
				}
			}
		}
		
		return priorityAmount;
	}
	
	// Instances of priority classes affected by the refactorings of a solution. This
	// override also incorporates a list of non priority classes (classes where
	// modifications are undesirable). The instances of non priority classes are also 
	// calculated and then taken away from the priority classes amount to give an overall value.
	public int priority(ArrayList<String> priorityClasses, ArrayList<String> nonPriorityClasses)
	{				
		int nonPriorityAmount = 0;
		int priorityAmount = priority(priorityClasses);

		for (String s1 : this.affectedClasses)
		{
			for (String s2 : nonPriorityClasses)
			{
				if (s1.endsWith(s2))
				{
					nonPriorityAmount++;
					break;
				}
			}
		}
		
		return priorityAmount - nonPriorityAmount;
	}
	
	// Diversity of refactorings in refactoring solution. This is calculated by
	// finding the average amount of refactorings per refactored element, and then
	// dividing the amount of distinct refactored element by this average. In the method
	// this calculation is a little more streamlined average = refactoring count / elements.
	// Therefore elements / average = elements * (elements / refactoring count).
	// The metric is calculated by finding elements squared over refactoring count.
	public float diversity()
	{
		int numerator = this.elementDiversity.size() * this.elementDiversity.size();
		int denominator = 0;
		
		for (Integer value : this.elementDiversity.values()) 
			denominator += value;
		
		return (float) numerator / (float) denominator;
	}
	
	// Average element recentness in refactoring solution. This is calculated by
	// finding how far back the element appeared amongst the previous versions of the code, 
	// denoted with an integer. The older the element is, the larger its corresponding value.
	// This value is calculated or extracted for each relevant element in the refactoring 
	// solution, and average is calculated across the values to give an average measure of recentness.
	public float elementRecentness(ArrayList<List<CompilationUnit>> previousUnits)
	{
		int numerator = 0;
		int denominator = 0;
		
		for (Entry<String, Integer> e : this.elementDiversity.entrySet())
		{			
			String key = e.getKey();
			int value = e.getValue();
			int amount = previousUnits.size();
			
			if (this.elementScores.containsKey(key))
			{
				amount = this.elementScores.get(key);			
			}
			else
			{
				String name;
				int elementType;
				
				if (!(key.contains(":")))
				{
					elementType = 1;
					name = key.substring(key.lastIndexOf('\\') + 1);
				}
				else if (key.charAt(1) == ':')
				{
					elementType = 3;
					name = key.substring(2);
				}
				else if (key.endsWith(":"))
				{
					elementType = 2;
					name = key.substring(1, key.length() - 1);
				}
				else
				{
					elementType = 4;
					name = key.substring(key.lastIndexOf(':') + 1);
				}
				
				for (int i = previousUnits.size() - 1; i >= 0; i--)
				{
					ForestWalker tw = new ForestWalker(previousUnits.get(i));
					boolean breakout = true;
					
					if (elementType == 1)
					{
						while (tw.next(TypeDeclaration.class))
						{
							TypeDeclaration td = (TypeDeclaration) tw.getProgramElement();
							if (((td instanceof ClassDeclaration) || (td instanceof InterfaceDeclaration)) && 
								(td.getName() != null) && (td.getName() == name))
							{
								breakout = false;
								break;
							}
						}
					}
					else if (elementType == 2)
					{
						while (tw.next(MethodDeclaration.class))
						{
							MethodDeclaration md = (MethodDeclaration) tw.getProgramElement();
							if ((md.getName() != null) && (Refactoring.getMethodName(md) == name))
							{
								breakout = false;
								break;
							}
						}
					}
					else if (elementType == 3)
					{
						while (tw.next(FieldDeclaration.class))
						{
							FieldDeclaration fd = (FieldDeclaration) tw.getProgramElement();
							if ((fd.toString() != null) && (fd.toString() == name))
							{
								breakout = false;
								break;
							}
						}
					}	
					else if (elementType == 4)
					{
						while (tw.next(VariableDeclaration.class))
						{
							VariableDeclaration vd = (VariableDeclaration) tw.getProgramElement();
							if ((vd.toString() != null) && (vd.toString() == name))
							{
								breakout = false;
								break;
							}
						}
					}
					
					if (breakout)
						break;
					
					amount--;
				}	
				
				this.elementScores.put(key, amount);
			}
			
			numerator += (amount * value);
			denominator += value;	
		}
		
		return (float) numerator / (float) denominator;
	}
	
	// Returns a value to represent the visibility of a modifier.
	private float identifier(VisibilityModifier vm)
	{
		if (vm instanceof Public)
			return 1;
		else if (vm instanceof Protected)
			return (float) (2 / 3);
		else if (vm instanceof Private)
			return 0;
		else
			return (float) (1 / 3);
	}

	// Returns all the types in a compilation unit including nested types.
	private ArrayList<TypeDeclaration> getAllTypes(CompilationUnit cu)
	{
		AbstractTreeWalker tw = new TreeWalker(cu);
		ArrayList<TypeDeclaration> types = new ArrayList<TypeDeclaration>();

		while (tw.next(TypeDeclaration.class))
		{
			TypeDeclaration td = (TypeDeclaration) tw.getProgramElement();
			
			if (td.getName() != null)
				types.add(td);
		}

		return types;
	} 
	
	public void setUnits(List<CompilationUnit> units)
	{
		this.units = units;
	}
	
	public void setAffectedClasses(ArrayList<String> affectedClasses)
	{
		this.affectedClasses = affectedClasses;
	}
	
	public void setElementDiversity(HashMap<String, Integer> elementDiversity)
	{
		this.elementDiversity = elementDiversity;
	}
}