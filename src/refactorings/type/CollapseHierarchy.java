package refactorings.type;

import java.util.ArrayList;

import recoder.CrossReferenceServiceConfiguration;
import recoder.abstraction.ClassType;
import recoder.abstraction.Field;
import recoder.abstraction.Method;
import recoder.abstraction.Package;
import recoder.abstraction.Type;
import recoder.bytecode.ClassFile;
import recoder.convenience.AbstractTreeWalker;
import recoder.convenience.TreeWalker;
import recoder.java.CompilationUnit;
import recoder.java.Import;
import recoder.java.ProgramElement;
import recoder.java.declaration.ClassDeclaration;
import recoder.java.declaration.ConstructorDeclaration;
import recoder.java.declaration.Extends;
import recoder.java.declaration.FieldDeclaration;
import recoder.java.declaration.MemberDeclaration;
import recoder.java.declaration.MethodDeclaration;
import recoder.java.declaration.TypeDeclaration;
import recoder.java.reference.FieldReference;
import recoder.java.reference.MemberReference;
import recoder.java.reference.MethodReference;
import recoder.java.reference.SuperConstructorReference;
import recoder.java.reference.SuperReference;
import recoder.java.reference.TypeReference;
import recoder.kit.MethodKit;
import recoder.kit.PackageKit;
import recoder.kit.ProblemReport;
import recoder.kit.UnitKit;
import recoder.list.generic.ASTList;
import recoder.service.CrossReferenceSourceInfo;
import refactorings.Refactoring;

public class CollapseHierarchy extends Refactoring 
{
	private TypeDeclaration currentDeclaration, superDeclaration, containingType;
	private ArrayList<MemberDeclaration> members, sisterMembers;
	private ArrayList<TypeDeclaration> sisterClasses;
	private ArrayList<ClassDeclaration> subClasses;
	private ArrayList<Integer> sisterPositions;	
	private ASTList<Import> superDeclarationImports;
	private int[] importSizes;
	private CompilationUnit unit;
	private int position;
	private boolean detachUnit, isNested;
	
	public CollapseHierarchy(CrossReferenceServiceConfiguration sc) 
	{
		super(sc);
	}
	
	public CollapseHierarchy() 
	{
		super();
	}
	
	public ProblemReport analyze(int iteration, int unit, int element) 
	{
		// Initialise and pick the element to visit.
		super.tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));
		
		for (int i = 0; i < element; i++)
		{
			super.tw.next(TypeDeclaration.class);
			TypeDeclaration td = (TypeDeclaration) super.tw.getProgramElement();
			if (!mayRefactor(td))
				i--;
		}
		
		ProgramElement pe = super.tw.getProgramElement();
		this.currentDeclaration = (TypeDeclaration) pe;
		this.superDeclaration = (TypeDeclaration) this.currentDeclaration.getSupertypes().get(0);
		this.unit = UnitKit.getCompilationUnit(this.currentDeclaration);
		CrossReferenceSourceInfo si = getCrossReferenceSourceInfo();
		this.subClasses = new ArrayList<ClassDeclaration>(si.getSubtypes(this.currentDeclaration).size()); 
		this.importSizes = new int[si.getSubtypes(this.currentDeclaration).size()];
		sisterMembers = new ArrayList<MemberDeclaration>();
		sisterClasses = new ArrayList<TypeDeclaration>();
		sisterPositions = new ArrayList<Integer>();
		boolean addPackageImport = false;
		Package pack = this.currentDeclaration.getPackage();

		this.members = new ArrayList<MemberDeclaration>(this.currentDeclaration.getMembers().size());
		for (MemberDeclaration md : this.currentDeclaration.getMembers())
			this.members.add(md);
		
		// Construct refactoring transformation.
		// The transformation is handled here manually and the transformation
		// method will do nothing for this refactoring when it is called.
		super.transformation = null;
		getChangeHistory().begin(this);
		
		// Checks packages before changes are made to check if the current 
		// declaration package needs to be added as an import to its supertype.
		if (!(this.currentDeclaration.getPackage().equals(this.superDeclaration.getPackage())))
			addPackageImport = true;
			
		// Removes any identical versions of the members of the class in the sister classes. 
		for (ClassType ct : si.getSubtypes(this.superDeclaration))
		{
			if (!(ct.equals(this.currentDeclaration)) && (ct instanceof TypeDeclaration))
			{
				for (MemberDeclaration dec : ((TypeDeclaration) ct).getMembers())
				{
					if (this.members.contains(dec))
					{
						this.sisterClasses.add((TypeDeclaration) ct);
						this.sisterPositions.add(super.getPosition((TypeDeclaration) ct, dec));
						this.sisterMembers.add(dec);
						detach(dec);
					}
				}
			}
		}

		// Also checks if the super class contains an identical version of any of the members.
		for (MemberDeclaration dec : this.superDeclaration.getMembers())
		{
			if (this.members.contains(dec))
			{
				this.sisterClasses.add(this.superDeclaration);
				this.sisterPositions.add(super.getPosition(this.superDeclaration, dec));
				this.sisterMembers.add(dec);
				detach(dec);
			}
		}
		
		// Also checks for abstract methods that are overridden by any of the current members.
		for (MemberDeclaration md : this.members)
		{
			if (md instanceof MethodDeclaration)
			{
				for (Method m : MethodKit.getRedefinedMethods((MethodDeclaration) md))
				{			
					if ((m instanceof MethodDeclaration) && ((MethodDeclaration) m).getMemberParent().equals(this.superDeclaration) && 
						!(this.sisterMembers.contains(m)))
					{
						this.sisterClasses.add(this.superDeclaration);
						this.sisterMembers.add((MemberDeclaration) m);
						this.sisterPositions.add(super.getPosition(this.superDeclaration, (MethodDeclaration) m));
						detach((MethodDeclaration) m);
						break;
					}
				}
			}
		}
		
		for (MemberDeclaration md : this.members)
		{
			detach(md);

			if (!(md instanceof ConstructorDeclaration))
			{
				// Find "super." references in method and remove them from their parent element.
				ArrayList<SuperReference> references = super.getSuperReferences(md);
				for (SuperReference sr : references)
					sr.getASTParent().replaceChild(sr, null);

				attach(md, this.superDeclaration, this.superDeclaration.getMembers().size());
			}
		}
		
		// Add any applicable imports from the current class to the super class.
		this.superDeclarationImports =  UnitKit.getCompilationUnit(this.superDeclaration).getImports();
		ASTList<Import> imports =  this.superDeclarationImports;

		for (Import ci : this.unit.getImports())
			if (!(imports.contains(ci)))
				imports.add(ci);
			
		if (addPackageImport)
			imports.add(getProgramFactory().createImport(PackageKit.createPackageReference(getProgramFactory(), pack)));
		UnitKit.getCompilationUnit(this.superDeclaration).setImports(imports);
		
		// Update any sub classes to now extend from the super class.		
		for (int i = 0; i < si.getSubtypes(this.currentDeclaration).size(); i++)
		{
			ClassDeclaration ct = (ClassDeclaration) si.getSubtypes(this.currentDeclaration).get(i);
			this.importSizes[i] = UnitKit.getCompilationUnit(ct).getImports().size();
			
			if (!(ct.getPackage().equals(this.superDeclaration.getPackage())))
			{
				Import imp = getProgramFactory().createImport(PackageKit.createPackageReference(getProgramFactory(), 
						                                                                        this.superDeclaration.getPackage()));
						                                 
				if (!(UnitKit.getCompilationUnit(ct).getImports().contains(imp)))
					attach(imp, UnitKit.getCompilationUnit(ct), UnitKit.getCompilationUnit(ct).getImports().size());
			}
			
			this.subClasses.add(ct);
			Extends superClass = getProgramFactory().createExtends(getProgramFactory().createTypeReference(this.superDeclaration.getIdentifier()));
			attach(superClass, ct);
		}
		
		if (this.currentDeclaration.getContainingClassType() == null)
		{
			this.position = super.getPosition(this.unit, this.currentDeclaration);
			this.isNested = false;
		}
		else
		{
			this.position = super.getPosition(this.currentDeclaration.getContainingClassType(), this.currentDeclaration);
			this.isNested = true;
			this.containingType = this.currentDeclaration.getContainingClassType();
		}
		
		if ((this.currentDeclaration.getContainingClassType() != null) || (this.unit.getTypeDeclarationCount() > 1))
		{
			detachUnit = false;
			detach(this.currentDeclaration);
		}
		else
		{
			detachUnit = true;
			detach(this.unit);
		}

		// Specify refactoring information for results information.		
		super.refactoringInfo = "Iteration " + iteration + ": \"Collapse Hierarchy\" applied from all elements in " 
				+ ((TypeDeclaration) pe).getName() + " to " + this.superDeclaration.getName();
	
		getChangeHistory().updateModel();
		return setProblemReport(EQUIVALENCE);
	}
	
	public ProblemReport analyzeReverse() 
	{
		// Construct refactoring transformation.
		super.transformation = null;
		
		if (detachUnit)
			attach(this.unit);
		else
			if (this.isNested)
				attach(this.currentDeclaration, this.containingType, this.position);
			else
				attach(this.currentDeclaration, this.unit, this.position);
			
		
		for (int i = this.members.size() - 1; i >= 0; i--)
		{
			if (!(this.members.get(i) instanceof ConstructorDeclaration))
				detach(this.members.get(i));
			
			attach(this.members.get(i), this.currentDeclaration, 0);
		}
		
		// Adds any duplicates of the members in the sister classes back.
		for (int i = 0; i < this.sisterClasses.size(); i++)
			attach(this.sisterMembers.get(i), this.sisterClasses.get(i), this.sisterPositions.get(i));
		
		// Updates sub classes to extend from the current class again.
		for (ClassDeclaration cd : this.subClasses)
		{
			Extends currentClass = getProgramFactory().createExtends(getProgramFactory().createTypeReference(this.currentDeclaration.getIdentifier()));
			attach(currentClass, cd);
			
			if (UnitKit.getCompilationUnit(cd).getImports().size() != this.importSizes[this.subClasses.indexOf(cd)])
			{
				ASTList<Import> imports = UnitKit.getCompilationUnit(cd).getImports();
				imports.remove(imports.size() - 1);
				UnitKit.getCompilationUnit(cd).setImports(imports);
			}
		}
				
		// Reset the imports in the class.
		UnitKit.getCompilationUnit(this.superDeclaration).setImports(this.superDeclarationImports);	
		getChangeHistory().updateModel();
		return setProblemReport(EQUIVALENCE);
	}

	public boolean mayRefactor(TypeDeclaration td)
	{
		// Prevents "Zero Service" outputs logged to the console.
		if (td.getProgramModelInfo() == null)
			td.getFactory().getServiceConfiguration().getChangeHistory().updateModel();

		// Makes a number of initial checks against the method, the class and the super class in order to quickly exclude insufficient candidates. 
		if (!(td.getSupertypes().get(0) instanceof TypeDeclaration) || !(td.getSupertypes().get(0).isOrdinaryClass()) ||
			(td.getAllSupertypes().size() == 2) || !(td.isOrdinaryClass()) || (td.getSupertypes().get(0).isAbstract()) || 
			(td.getName() == null) || (td.getTypes().size() > 0) || (getCrossReferenceSourceInfo().getReferences(td, true).size() > 0))
			return false;
		else
		{
			CrossReferenceSourceInfo si = getCrossReferenceSourceInfo();
			TypeDeclaration std = (TypeDeclaration) td.getSupertypes().get(0);
			
			// If the class implements any interfaces and the super class doesn't implement the same interfaces.
			if ((((ClassDeclaration) td).getImplementedTypes() != null) && 
				!(((ClassDeclaration) td).getImplementedTypes().equals(((ClassDeclaration) std).getImplementedTypes()))) 
				return false;
			
			// If the subtypes aren't ordinary class declarations or if they reference
			// a constructor from the current class that isn't the default constructor.
			for (ClassType ct : si.getSubtypes(td))
			{
				if (!(ct.isOrdinaryClass()))
					return false;
			
				AbstractTreeWalker tw = new TreeWalker((TypeDeclaration) ct);
				if (tw.next(SuperConstructorReference.class)) 
					return false;
			}
			
			for (MemberDeclaration md : td.getMembers())
			{
				// Get methods referenced in member declaration.
				ArrayList<MethodReference> methods = super.getMethods(md);

				// Check if methods can be accessed in super type.
				for (MethodReference mr : methods)
				{
					Method m = si.getMethod(mr);
					if (td.getMethods().contains(m))
						continue;

					if (!(m.isPublic()) && !(m.isPrivate()))
					{
						if (!m.getPackage().equals(std.getPackage()))
						{
							if (!m.isProtected())
								return false;
							else if (!(std.getAllSupertypes().contains(m.getContainingClassType())))
								return false;
						}
					}
				}

				// Get fields accessed in member declaration.
				ArrayList<Field> fields = super.getFields(md, si);

				// Check if fields can be accessed in super type.
				for (Field f : fields)
				{
					if (td.getFieldsInScope().contains(f))
						continue;
					
					if (!(f.isPublic()) && !(f.isPrivate()))
					{
						if (!f.getContainingClassType().getPackage().equals(std.getPackage()))
						{
							if (!f.isProtected())
								return false;
							else if (!(std.getAllSupertypes().contains(f.getContainingClassType())))
								return false;
						}
					}
				}

				// Get types accessed in method.
				ArrayList<Type> types = super.getTypes(md, si);

				// In the case that a nested class in the current unit is being referenced 
				// in the object and the super class is in a separate package, don't move it. 
				for (TypeDeclaration typedec : super.getTypeDeclarations(UnitKit.getCompilationUnit(td)))
					if (!(typedec.equals(td)) && (types.contains(typedec)) && !(td.getPackage().equals(std.getPackage())))
						return false;
				
				// Check if inner types can be accessed in super type.
				for (Type t: types)
				{
					if ((((ClassType) t).isInner()) && ((t instanceof TypeDeclaration) || (t instanceof ClassFile)))
					{
						if (!(((ClassType) t).isPublic()) && !(((ClassType) t).isPrivate()))
						{
							if (!((ClassType) t).getContainingClassType().getPackage().equals(std.getPackage()))
							{
								if (!((ClassType) t).isProtected())
									return false;
								else if (!(std.getAllSupertypes().contains((ClassType) t)))
									return false;
							}
						}
					}
				}

				if (md instanceof MethodDeclaration)
				{
					if (md instanceof ConstructorDeclaration)
					{
						if (si.getReferences((ConstructorDeclaration) md).size() > 0)
							return false;
					}
					else
					{
						if (md.isStatic())
							for (MemberReference mr : si.getReferences((MethodDeclaration) md))
								if ((((MethodReference) mr).getReferencePrefix() instanceof TypeReference) && 
									!(((MethodReference) mr).getReferencePrefix().toSource().equals(std.getName())))
									return false;
					}

					// Check if the super type has the method and if it is distinctly overridden by any other sub classes.
					for (Method m : MethodKit.getRedefinedMethods((Method) md))
						if ((m instanceof MethodDeclaration) && (((MethodDeclaration) m).getMemberParent().equals(std)))
							for (Method m2 : MethodKit.getRedefiningMethods(si, m))
								if (!(((MethodDeclaration) m2).getMemberParent().equals(td)) && !(m2.equals(md)))
									return false;

					// Check if the super class contains an unrelated method with the same name.
					for (Method m : std.getMethods())
						if ((m.getName().equals(((MethodDeclaration) md).getName())) && !(m.isAbstract()) && !(m.equals(md)))
							return false;
				}	
				else if (md instanceof FieldDeclaration)
				{
					// Checks if the super class already contain a field with the same name.
					for (MemberDeclaration dec : std.getMembers())
					{
						if (dec instanceof FieldDeclaration)
						{
							int lastdec = dec.toString().lastIndexOf(">");
							int lastmd = md.toString().lastIndexOf(">");
							if ((md.toString().substring(lastmd + 2).equals(dec.toString().substring(lastdec + 2))) && !(md.equals(dec)))
								return false;
						}
					}
			
					if (md.isStatic())
						for (FieldReference mr : si.getReferences(((FieldDeclaration) md).getFieldSpecifications().get(0)))
							if ((((FieldReference) mr).getReferencePrefix() instanceof TypeReference) && 
								!(((FieldReference) mr).getReferencePrefix().toSource().equals(std.getName()))) 
								return false;
				}
			}
			
			return true;
		}
	}

	// Count the amount of available elements in the chosen class for refactoring.
	// If an element is not applicable for the current refactoring it is not counted.
	public int getAmount(int unit)
	{
		int counter = 0;
		AbstractTreeWalker tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));

		// Only counts the relevant program element.
		while (tw.next(TypeDeclaration.class))
		{
			TypeDeclaration td = (TypeDeclaration) tw.getProgramElement();
			if (mayRefactor(td))
				counter++;
		}

		return counter;
	}
	
	public String getName(int unit, int element)
	{		
		AbstractTreeWalker tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));

		for (int i = 0; i < element; i++)
		{
			tw.next(TypeDeclaration.class);
			TypeDeclaration td = (TypeDeclaration) tw.getProgramElement();
			if (!mayRefactor(td))
				i--;
		}
		
		TypeDeclaration td = (TypeDeclaration) tw.getProgramElement();
		return td.getName();
	}
}