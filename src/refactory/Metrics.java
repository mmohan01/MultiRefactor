package refactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import recoder.abstraction.ClassType;
import recoder.abstraction.Method;
import recoder.abstraction.PrimitiveType;
import recoder.abstraction.Type;
import recoder.convenience.AbstractTreeWalker;
import recoder.convenience.ForestWalker;
import recoder.convenience.TreeWalker;
import recoder.java.CompilationUnit;
import recoder.java.ProgramElement;
import recoder.java.declaration.ClassDeclaration;
import recoder.java.declaration.EnumDeclaration;
import recoder.java.declaration.FieldDeclaration;
import recoder.java.declaration.FieldSpecification;
import recoder.java.declaration.InterfaceDeclaration;
import recoder.java.declaration.MethodDeclaration;
import recoder.java.declaration.TypeDeclaration;
import recoder.java.declaration.TypeParameterDeclaration;
import recoder.java.declaration.VariableDeclaration;
import recoder.java.declaration.modifier.Private;
import recoder.java.declaration.modifier.Protected;
import recoder.java.declaration.modifier.Public;
import recoder.java.declaration.modifier.VisibilityModifier;
import recoder.java.reference.MethodReference;
import recoder.java.reference.TypeReference;
import recoder.service.SourceInfo;

// Calculates various software metrics from the source code input.
public class Metrics 
{
	private List<CompilationUnit> units;
	private AbstractTreeWalker tw;
	
	public Metrics(List<CompilationUnit> units)
	{
		this.units = units;
	}
	
	public List<CompilationUnit> getUnits()
	{
		return this.units;
	}
	
	public void setUnits(List<CompilationUnit> units)
	{
		this.units = units;
	}
	
	// Amount of classes in project.
	// Includes both ordinary classes and interfaces.
	public int classDesignSize()
	{
		int classCounter = 0;
		this.tw = new ForestWalker(this.units);

		while (this.tw.next(TypeDeclaration.class))
		{
			TypeDeclaration td = (TypeDeclaration) tw.getProgramElement();

			if ((td.getName() != null) && ((td instanceof ClassDeclaration) || (td instanceof InterfaceDeclaration)))
				classCounter++;
		}
		
		return classCounter;
	}
	
	// Amount of methods in project.
	// Iterated from the method declarations.
	public int numberOfMethods()
	{
		int methodCounter = 0;
		this.tw = new ForestWalker(this.units);

		while (this.tw.next(MethodDeclaration.class))
			methodCounter++;
		
		return methodCounter;
	}
	
	// Amount of methods over the overall amount of classes.
	// Uses all method declarations and all types.
	// Abstract method declarations are not excluded.
	public float methodsPerType()
	{
		int typeCounter = 0;
		int methodCounter;
		this.tw = new ForestWalker(this.units);

		while (this.tw.next(TypeDeclaration.class))
		{
			TypeDeclaration td = (TypeDeclaration) tw.getProgramElement();
			if ((td.getName() != null) && ((td instanceof ClassDeclaration) || (td instanceof InterfaceDeclaration)))
				typeCounter++;
		}
		
		methodCounter = numberOfMethods();
		return (float) methodCounter / (float) typeCounter;
	}
	
	// Amount of interfaces over the overall amount of classes (as a percentage).
	public float abstractness()
	{
		int typeCounter = 0;
		int interfaceCounter = 0;
		this.tw = new ForestWalker(this.units);

		while (this.tw.next(TypeDeclaration.class))
		{
			TypeDeclaration td = (TypeDeclaration) tw.getProgramElement();
			if ((td.getName() != null) && ((td instanceof ClassDeclaration) || (td instanceof InterfaceDeclaration)))
			{
				typeCounter++;
				if (td instanceof InterfaceDeclaration)
					interfaceCounter++;
			}
		}
		
		float answer = ((float) interfaceCounter / (float) typeCounter) * 100;
		return answer;
	}
	
	// Amount of abstract elements in project.
	public int abstractAmount()
	{
		int abstractCounter = 0;
		this.tw = new ForestWalker(this.units);

		while (this.tw.next())
		{
			if (this.tw.getProgramElement() instanceof TypeDeclaration)
			{
				TypeDeclaration td = (TypeDeclaration)(this.tw.getProgramElement());
				if ((td.getName() != null) && (td.isAbstract()) && ((td instanceof ClassDeclaration) || (td instanceof InterfaceDeclaration)))
					abstractCounter++;
			}
			else if (this.tw.getProgramElement() instanceof MethodDeclaration)
			{
				MethodDeclaration md = (MethodDeclaration)(this.tw.getProgramElement());
				if (md.isAbstract())
					abstractCounter++;
			}
		}

		return abstractCounter;
	}
	
	// Average functional abstraction ratio per class.
	// Ratio gets the accumulation of the amount of inherited external methods accessed within
	// the methods of a class (methods declared in a super class of the current class) over
	// the overall distinct amount of external methods accessed in the methods of the class.
	public float functionalAbstraction()
	{
		int methodCount, inheritedMethodCount;
		int classCounter = 0;
		float functionalAbstraction = 0;
		
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
					
					// Prevents "Zero Service" outputs logged to the console.
					if (td.getProgramModelInfo() == null)
						td.getFactory().getServiceConfiguration().getChangeHistory().updateModel();
					
					for (Method m : td.getMethods())
					{
						if (m instanceof MethodDeclaration)
						{							
							TreeWalker tw = new TreeWalker((MethodDeclaration) m);
							while (tw.next()) 
							{
								ProgramElement pe = tw.getProgramElement();
								if ((pe instanceof MethodReference) && (si.getMethod((MethodReference) pe) instanceof MethodDeclaration))
								{
									MethodDeclaration md = (MethodDeclaration) si.getMethod((MethodReference) pe);
									
									if (!(methods.contains(md)) && !(md.getContainingClassType().equals(td)))
										methods.add(md);
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
						functionalAbstraction += (float) inheritedMethodCount / (float) methodCount;
				}
			}
		}
		
		return functionalAbstraction / (float) classCounter;
	}
	
	// Amount of static elements in project.
	// Of the variable declarations, only a field can be static.
	public int staticAmount()
	{
		int staticCounter = 0;
		this.tw = new ForestWalker(this.units);

		while (this.tw.next())
		{
			if (this.tw.getProgramElement() instanceof TypeDeclaration)
			{
				TypeDeclaration td = (TypeDeclaration)(this.tw.getProgramElement());
				if ((td.getName() != null) && (td.isStatic()) && ((td instanceof ClassDeclaration) || (td instanceof InterfaceDeclaration)))
					staticCounter++;
			}
			else if (this.tw.getProgramElement() instanceof MethodDeclaration)
			{
				MethodDeclaration md = (MethodDeclaration)(this.tw.getProgramElement());
				if (md.isStatic())
					staticCounter++;
			}
			else if (this.tw.getProgramElement() instanceof FieldDeclaration)
			{
				FieldDeclaration fd = (FieldDeclaration)(this.tw.getProgramElement());
				if (fd.isStatic())
					staticCounter++;
			}
		}

		return staticCounter;
	}
	
	// Amount of final elements in project.
	public int finalAmount()
	{
		int finalCounter = 0;
		this.tw = new ForestWalker(this.units);

		while (this.tw.next())
		{
			if (this.tw.getProgramElement() instanceof TypeDeclaration)
			{
				TypeDeclaration td = (TypeDeclaration)(this.tw.getProgramElement());
				if ((td.getName() != null) && (td.isFinal()) && ((td instanceof ClassDeclaration) || (td instanceof InterfaceDeclaration)))
					finalCounter++;
			}
			else if (this.tw.getProgramElement() instanceof MethodDeclaration)
			{
				MethodDeclaration md = (MethodDeclaration)(this.tw.getProgramElement());
				if (md.isFinal())
					finalCounter++;
			}
			else if (this.tw.getProgramElement() instanceof VariableDeclaration)
			{
				VariableDeclaration vd = (VariableDeclaration)(this.tw.getProgramElement());
				if (vd.isFinal())
					finalCounter++;
			}
		}

		return finalCounter;
	}
	
	// Amount of constant elements in project.
	public int constantAmount()
	{
		int constantCounter = 0;
		this.tw = new ForestWalker(this.units);

		while (this.tw.next())
		{
			if (this.tw.getProgramElement() instanceof MethodDeclaration)
			{
				MethodDeclaration md = (MethodDeclaration)(this.tw.getProgramElement());
				if ((md.isStatic()) && (md.isFinal()))
					constantCounter++;
			}
			else if (this.tw.getProgramElement() instanceof FieldDeclaration)
			{
				FieldDeclaration fd = (FieldDeclaration)(this.tw.getProgramElement());
				if ((fd.isStatic()) && (fd.isFinal()))
					constantCounter++;
			}
		}

		return constantCounter;
	}
	
	// Amount of classes in the project that are declared inside other classes.
	public int innerClassAmount()
	{
		int innerClassCounter = 0;
		
		for (int i = 0; i < this.units.size(); i++)
			for (TypeDeclaration td : getAllTypes(this.units.get(i)))
				if ((td.getContainingClassType() != null) && ((td instanceof ClassDeclaration) || (td instanceof InterfaceDeclaration)))
					innerClassCounter++;

		return innerClassCounter;
	}
	
	// Amount of distinct class hierarchies in a project.
	public int numberOfHierarchies()
	{
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
						
					while (td.getSupertypes().get(0) instanceof TypeDeclaration)
						td = (TypeDeclaration) td.getSupertypes().get(0);
					
					baseTypes.add(td.getFullName());
				}
			}
		}	

		return baseTypes.size();
	}
	
	// Average amount of classes away from the root per class.
	public float averageNumberOfAncestors()
	{
		int classCounter = 0;
		int accumulativeCounter = 0;
		int superTypeCounter;
 
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
					superTypeCounter = 0;
					
					while (ct.getSupertypes().get(0) instanceof TypeDeclaration)
					{
						superTypeCounter++;
						ct = ct.getSupertypes().get(0);
					}
					
					accumulativeCounter += superTypeCounter;
				}
			}
		}

		return (float) accumulativeCounter / (float) classCounter;
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
		ArrayList<TypeDeclaration> typeDeclarations;

		for (int i = 0; i < this.units.size(); i++)
		{		
			typeDeclarations = new ArrayList<TypeDeclaration>();
			
			// Get all relevant classes in compilation unit.
			for (TypeDeclaration td : getAllTypes(this.units.get(i)))
				if (!(td instanceof EnumDeclaration) && !(td instanceof TypeParameterDeclaration))
					typeDeclarations.add(td);

			for (TypeDeclaration td : typeDeclarations)
			{
				classCounter++;
				methodCounter = 0;
				cohesionCounter = 0;
				allTypes = new ArrayList<String>();
				
				// Prevents "Zero Service" outputs logged to the console.
				if (td.getProgramModelInfo() == null)
					td.getFactory().getServiceConfiguration().getChangeHistory().updateModel();
				
				for (Method m : td.getMethods())
				{
					methodCounter++;
					types = new ArrayList<String>();

					for (Type t : m.getSignature())
					{
						types.add(t.getFullName());
						allTypes.add(t.getFullName());
					}

					distinctTypes = new HashSet<String>(types);
					cohesionCounter += distinctTypes.size();
				}

				distinctTypes = new HashSet<String>(allTypes);

				if ((methodCounter * distinctTypes.size()) > 0)
					cohesionAmongMethods += (float) cohesionCounter / (float) (methodCounter * distinctTypes.size());
			}
		}

		return cohesionAmongMethods / (float) classCounter;
	}
	
	// The accumulative number of other distinct classes each class depends on.
	public int directClassCoupling()
	{
		int couplingCounter = 0;
		Set<String> distinctTypes;
		SourceInfo si = this.units.get(0).getFactory().getServiceConfiguration().getSourceInfo();
		
		for (int i = 0; i < this.units.size(); i++)
		{			
			for (TypeDeclaration td : getAllTypes(this.units.get(i)))
			{
				distinctTypes = new HashSet<String>();
				
				// Prevents "Zero Service" outputs logged to the console.
				if (td.getProgramModelInfo() == null)
					td.getFactory().getServiceConfiguration().getChangeHistory().updateModel();
				
				for (Method m : td.getMethods())
					for (Type t : m.getSignature())
						if ((t != null) && !(t.getFullName().contains("java.lang.")) && !(t instanceof PrimitiveType))
							distinctTypes.add(t.getFullName());
				
				for (FieldSpecification fs : td.getFieldsInScope())
				{
					TreeWalker tw = new TreeWalker(fs);
					while (tw.next()) 
					{
						ProgramElement pe = tw.getProgramElement();
						if (pe instanceof TypeReference) 
						{
							Type t = si.getType(pe);
							
							if ((t != null) && !(t.getFullName().contains("java.lang.")) &&	!(t instanceof PrimitiveType))
								distinctTypes.add(t.getFullName()); 
						}
					}
				}
				
				couplingCounter += distinctTypes.size();			
			}
		}

		return couplingCounter;
	}
	
	// Amount of child classes in the project.
	public int childAmount()
	{
		int childCounter = 0;
		
		for (int i = 0; i < this.units.size(); i++)
		{
			for (TypeDeclaration td : getAllTypes(this.units.get(i)))
			{
				if (td.isOrdinaryClass())
				{					
					// Prevents "Zero Service" outputs logged to the console.
					if (td.getProgramModelInfo() == null)
						td.getFactory().getServiceConfiguration().getChangeHistory().updateModel();

					if (td.getSupertypes().get(0) instanceof TypeDeclaration)
						childCounter++;
				}
			}
		}

		return childCounter;
	}
	
	public float averageChildAmount()
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

					if (td.getSupertypes().get(0) instanceof TypeDeclaration)
						childCounter++;
					
					classCounter++;
				}
			}
		}

		return (float) childCounter / (float) classCounter;
	}
	
	// Amount of lines of code in the project.
	public int linesOfCode()
	{		
		int childCounter = 0;

		for (CompilationUnit u : this.units)
			childCounter += u.getEndPosition().getLine();

		return childCounter;
	}

	// Amount of java files in the source code.
	public int fileAmount()
	{		
		return this.units.size();
	}
	
	// Amount of visibility in a project among type, method and variable declarations.
	// Returns integer value to represent the visibility where a higher value means more visibility.
	public float visibility()
	{
		float visibilityCounter = 0;
		int attributeCounter = 0;
		this.tw = new ForestWalker(this.units);

		while (this.tw.next())
		{
			ProgramElement pe = this.tw.getProgramElement(); 
			
			if (pe instanceof VariableDeclaration)
			{
				VariableDeclaration vd = (VariableDeclaration) pe;
				visibilityCounter += identifier(vd.getVisibilityModifier());
				attributeCounter++;
			}
			if (pe instanceof MethodDeclaration)
			{
				MethodDeclaration md = (MethodDeclaration) pe;
				visibilityCounter += identifier(md.getVisibilityModifier());
				attributeCounter++;
			}
			if (pe instanceof TypeDeclaration)
			{
				TypeDeclaration td = (TypeDeclaration) pe;
				
				if ((td.getName() != null) && ((td instanceof ClassDeclaration) || (td instanceof InterfaceDeclaration)))
				{
					visibilityCounter += identifier(td.getVisibilityModifier());
					attributeCounter++;
				}
			}
		}

		return visibilityCounter / (float) attributeCounter;
	}
	
	// Amount of public methods in the project.
	public int classInterfaceSize()
	{
		int counter = 0;
		this.tw = new ForestWalker(this.units);

		while (this.tw.next(MethodDeclaration.class))
		{
			MethodDeclaration md = (MethodDeclaration)(this.tw.getProgramElement());
			if (md.getVisibilityModifier() instanceof Public)
				counter++;
		}

		return counter;
	}
	
	// Accumulative ratio of the amount of private, package 
	// or protected attributes in a class to the overall amount.
	public float dataAccessMetric()
	{
		int counter = 0;
		int nonPublicCounter = 0;
		this.tw = new ForestWalker(this.units);

		while (this.tw.next(VariableDeclaration.class))
		{
			VariableDeclaration vd = (VariableDeclaration)(this.tw.getProgramElement());
			if (!(vd.getVisibilityModifier() instanceof Public))
				nonPublicCounter++;
			
			counter++;
		}

		return (float) nonPublicCounter / (float) counter;
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
}