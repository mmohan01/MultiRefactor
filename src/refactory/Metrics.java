package refactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import recoder.abstraction.Method;
import recoder.abstraction.Type;
import recoder.convenience.AbstractTreeWalker;
import recoder.convenience.ForestWalker;
import recoder.convenience.TreeWalker;
import recoder.java.CompilationUnit;
import recoder.java.ProgramElement;
import recoder.java.declaration.ClassDeclaration;
import recoder.java.declaration.ConstructorDeclaration;
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
	// Iterated from the class declarations.
	public int classDesignSize()
	{
		int classCounter = 0;
		this.tw = new ForestWalker(this.units);

		while (this.tw.next(ClassDeclaration.class))
			classCounter++;
		
		return classCounter;
	}
	
	// Amount of methods in project.
	// Iterated from the method declarations.
	// Not including constructor methods.
	public int numberOfMethods()
	{
		int methodCounter = 0;
		this.tw = new ForestWalker(this.units);

		while (this.tw.next(MethodDeclaration.class))
			if (!(this.tw.getProgramElement() instanceof ConstructorDeclaration))
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
			typeCounter++;
		
		methodCounter = numberOfMethods();
		return methodCounter / typeCounter;
	}
	
	// Amount of interfaces over the overall amount of classes (as a percentage).
	public float abstractness()
	{
		int typeCounter = 0;
		int interfaceCounter = 0;
		this.tw = new ForestWalker(this.units);

		while (this.tw.next(TypeDeclaration.class))
		{
			typeCounter++;
			if (this.tw.getProgramElement() instanceof InterfaceDeclaration)
				interfaceCounter++;
		}
		
		float answer = (float)((interfaceCounter / typeCounter) * 100);
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
				if (td.isAbstract())
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
				if (td.isStatic())
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
				if (td.isFinal())
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
				if ((md.isFinal()) && (md.isStatic()))
					constantCounter++;
			}
			else if (this.tw.getProgramElement() instanceof FieldDeclaration)
			{
				FieldDeclaration fd = (FieldDeclaration)(this.tw.getProgramElement());
				if ((fd.isFinal()) && (fd.isStatic()))
					constantCounter++;
			}
		}

		return constantCounter;
	}
	
	// Amount of classes in the project that are declared inside other classes.
	public int innerClassAmount()
	{
		int innerClassCounter = 0;
		this.tw = new ForestWalker(this.units);

		while (this.tw.next(TypeDeclaration.class))
		{
			TypeDeclaration td = (TypeDeclaration)(this.tw.getProgramElement());
			if (td.getContainingClassType() != null)
				innerClassCounter++;
		}

		return innerClassCounter;
	}
	
	// Amount of distinct class hierarchies in a project.
	public int numberOfHierarchies()
	{
		ArrayList<String> types = new ArrayList<String>();
		
		for (int i = 0; i < this.units.size(); i++)
		{
			for (TypeDeclaration td : this.units.get(i).getDeclarations())
			{
				if (!(td.isInner()) && (td instanceof ClassDeclaration))
				{
					// Prevents "Zero Service" outputs logged to the console.
					if (td.getProgramModelInfo() == null)
						td.getFactory().getServiceConfiguration().getChangeHistory().updateModel();
						
					if (!td.getSupertypes().get(0).getFullName().equals("java.lang.Object"))
						types.add(td.getSupertypes().get(0).getName());
				}
			}
		}
					
		Set<String> distinctTypes = new HashSet<String>(types);
		return distinctTypes.size();
	}
	
	// Average amount of classes away from the root per class.
	public float averageNumberOfAncestors()
	{
		int classCounter = 0;
		int accumulativeCounter = 0;

		for (int i = 0; i < this.units.size(); i++)
		{
			for (TypeDeclaration td : this.units.get(i).getDeclarations())
			{
				if ((!td.isInner()) && !(td instanceof EnumDeclaration) && !(td instanceof TypeParameterDeclaration))
				{					
					// Prevents "Zero Service" outputs logged to the console.
					if (td.getProgramModelInfo() == null)
						td.getFactory().getServiceConfiguration().getChangeHistory().updateModel();

					classCounter++;
					accumulativeCounter += td.getAllSupertypes().size() - 2;
				}
			}
		}

		return accumulativeCounter/classCounter;
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
			// Get all relevant classes in compilation unit.
			typeDeclarations = new ArrayList<TypeDeclaration>();
			for (TypeDeclaration td : getAllTypes(this.units.get(i)))
			{
				if (!(td instanceof EnumDeclaration) && !(td instanceof TypeParameterDeclaration))
					typeDeclarations.add(td);
				
				if (td.getTypesInScope() != null)
					for (TypeDeclaration td2 : td.getTypesInScope())
						if (!(td2 instanceof EnumDeclaration) && !(td2 instanceof TypeParameterDeclaration))
							typeDeclarations.add(td);
			}

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
					if (!(m instanceof ConstructorDeclaration))
					{
						methodCounter++;
						types = new ArrayList<String>();

						for (Type t : m.getSignature())
						{
							types.add(t.getName());
							allTypes.add(t.getName());
						}

						distinctTypes = new HashSet<String>(types);
						cohesionCounter += distinctTypes.size();
					}
				}

				distinctTypes = new HashSet<String>(allTypes);

				if ((methodCounter*distinctTypes.size()) > 0)
					cohesionAmongMethods += cohesionCounter / (methodCounter * distinctTypes.size());
			}
		}

		return cohesionAmongMethods / classCounter;
	}
	
	// The accumulative number of other distinct classes each class depends on.
	public int directClassCoupling()
	{
		int couplingCounter = 0;
		ArrayList<String> types;
		Set<String> distinctTypes;
		
		for (int i = 0; i < this.units.size(); i++)
		{
			types = new ArrayList<String>();
			
			for (TypeDeclaration td : getAllTypes(this.units.get(i)))
			{
				// Prevents "Zero Service" outputs logged to the console.
				if (td.getProgramModelInfo() == null)
					td.getFactory().getServiceConfiguration().getChangeHistory().updateModel();
				
				for (FieldSpecification fs : td.getFieldsInScope())
					types.add(fs.getType().getName());
				
				for (Method m : td.getMethods())
					for (Type t : m.getSignature())
						types.add(t.getName());
			}

			distinctTypes = new HashSet<String>(types);
			couplingCounter += distinctTypes.size();
		}

		return couplingCounter;
	}
	
	// Amount of child classes in the project.
	public int childAmount()
	{
		int childCounter = 0;
		
		for (int i = 0; i < this.units.size(); i++)
		{
			for (TypeDeclaration td : this.units.get(i).getDeclarations())
			{
				if ((!td.isInner()) && !(td instanceof EnumDeclaration) && !(td instanceof TypeParameterDeclaration))
				{					
					// Prevents "Zero Service" outputs logged to the console.
					if (td.getProgramModelInfo() == null)
						td.getFactory().getServiceConfiguration().getChangeHistory().updateModel();

					if (!td.getSupertypes().get(0).getFullName().equals("java.lang.Object"))
						childCounter++;
				}
			}
		}

		return childCounter;
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
	public int visibility()
	{
		int counter = 0;
		this.tw = new ForestWalker(this.units);

		while (this.tw.next())
		{
			ProgramElement pe = this.tw.getProgramElement(); 
			
			if (pe instanceof VariableDeclaration)
			{
				VariableDeclaration vd = (VariableDeclaration) pe;
				counter += identifier(vd.getVisibilityModifier());
			}
			if (pe instanceof MethodDeclaration)
			{
				MethodDeclaration md = (MethodDeclaration) pe;
				counter += identifier(md.getVisibilityModifier());
			}
			if (pe instanceof TypeDeclaration)
			{
				TypeDeclaration td = (TypeDeclaration) pe;
				counter += identifier(td.getVisibilityModifier());
			}
		}

		return counter;
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
	
	// Accumulative ratio of the amount of private or 
	// protected attributes in a class to the overall amount.
	public float dataAccessMetric()
	{
		int counter, publicCounter;
		float accumulativeCounter = 0;

		for (int i = 0; i < this.units.size(); i++)
		{
			counter = 0;
			publicCounter = 0;
			this.tw = new TreeWalker(this.units.get(i));

			while (this.tw.next(VariableDeclaration.class))
			{
				VariableDeclaration vd = (VariableDeclaration)(this.tw.getProgramElement());
				if (!(vd.getVisibilityModifier() instanceof Public))
					publicCounter++;
				counter++;
			}

			if (counter > 0)
				accumulativeCounter += publicCounter / counter;
		}

		return accumulativeCounter;
	}
	
	// Returns a value to represent the visibility of a modifier.
	private int identifier(VisibilityModifier vm)
	{
		if (vm instanceof Public)
			return 3;
		else if (vm instanceof Protected)
			return 2;
		else if (vm instanceof Private)
			return 0;
		else
			return 1;
	}

	private ArrayList<TypeDeclaration> getAllTypes(CompilationUnit cu)
	{
		AbstractTreeWalker tw = new TreeWalker(cu);
		ArrayList<TypeDeclaration> types = new ArrayList<TypeDeclaration>();

		while (tw.next(TypeDeclaration.class))
			types.add((TypeDeclaration) tw.getProgramElement());

		return types;
	} 
}