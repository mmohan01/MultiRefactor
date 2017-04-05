package refactorings.method;

import java.util.ArrayList;

import recoder.CrossReferenceServiceConfiguration;
import recoder.abstraction.ClassType;
import recoder.abstraction.Field;
import recoder.abstraction.Method;
import recoder.abstraction.Package;
import recoder.abstraction.Type;
import recoder.bytecode.ClassFile;
import recoder.convenience.TreeWalker;
import recoder.java.Identifier;
import recoder.java.Import;
import recoder.java.ProgramElement;
import recoder.java.declaration.ClassDeclaration;
import recoder.java.declaration.ConstructorDeclaration;
import recoder.java.declaration.MemberDeclaration;
import recoder.java.declaration.MethodDeclaration;
import recoder.java.declaration.TypeDeclaration;
import recoder.java.reference.MemberReference;
import recoder.java.reference.MethodReference;
import recoder.java.reference.PackageReference;
import recoder.java.reference.SuperReference;
import recoder.java.reference.ThisReference;
import recoder.java.reference.TypeReference;
import recoder.kit.MethodKit;
import recoder.kit.MiscKit;
import recoder.kit.PackageKit;
import recoder.kit.ProblemReport;
import recoder.kit.UnitKit;
import recoder.list.generic.ASTList;
import recoder.service.CrossReferenceSourceInfo;
import refactorings.MethodRefactoring;

public class MoveMethodUp extends MethodRefactoring 
{
	private TypeDeclaration currentDeclaration, superDeclaration;
	private MethodDeclaration abstractMethodDeclaration;
	private int abstractMethodPosition, position, newPosition;
	private ArrayList<TypeDeclaration> sisterClasses;
	private ArrayList<Integer> sisterPositions;
	private ASTList<Import> superDeclarationImports;
	
	public MoveMethodUp(CrossReferenceServiceConfiguration sc) 
	{
		super(sc);
	}
	
	public MoveMethodUp() 
	{
		super();
	}
	
	public ProblemReport analyze(int iteration, int unit, int element) 
	{		
		// Initialise and pick the element to visit.
		CrossReferenceSourceInfo si = getCrossReferenceSourceInfo();
		super.tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));
		
		for (int i = 0; i < element; i++)
			super.tw.next(MethodDeclaration.class);
		
		MethodDeclaration md = (MethodDeclaration) super.tw.getProgramElement();
		this.currentDeclaration = md.getMemberParent();
		
		// Prevents "Zero Service" outputs logged to the console.
		if (this.currentDeclaration.getProgramModelInfo() == null)
			md.getFactory().getServiceConfiguration().getChangeHistory().updateModel();
		
		this.superDeclaration = (TypeDeclaration) this.currentDeclaration.getSupertypes().get(0);
		this.sisterClasses = new ArrayList<TypeDeclaration>(si.getSubtypes(this.superDeclaration).size());
		this.sisterPositions = new ArrayList<Integer>(si.getSubtypes(this.superDeclaration).size());
		this.position =  super.getPosition(this.currentDeclaration, md);
		this.abstractMethodDeclaration = null;
		this.newPosition = -1;
		this.abstractMethodPosition = -1;
		ArrayList<Type> types = super.getTypes(md, si);
		ASTList<Import> methodImports = super.getMemberImports(types, UnitKit.getCompilationUnit(this.currentDeclaration).getImports(), si);
		boolean addPackageImport = false;
		Package pack = this.currentDeclaration.getPackage();
		
		// Construct refactoring transformation.
		// The transformation is handled here manually and the transformation
		// method will do nothing for this refactoring when it is called.
		super.transformation = null;
		getChangeHistory().begin(this);
		
		// Checks packages before changes are made to check if the current 
		// declaration package needs to be added as an import to its supertype.
		if (!(pack.equals(this.superDeclaration.getPackage())))
			addPackageImport = true;
		
		// Removes any identical versions of the method in the sister classes. 
		for (ClassType ct : si.getSubtypes(this.superDeclaration))
		{
			if (!(ct.equals(this.currentDeclaration)) && (ct instanceof TypeDeclaration))
			{				
				for (MemberDeclaration dec : ((TypeDeclaration) ct).getMembers())
				{
					if (dec.equals(md))
					{
						this.sisterClasses.add((TypeDeclaration) ct);
						this.sisterPositions.add(super.getPosition((TypeDeclaration) ct, dec));
						detach(dec);
					}
				}
			}
		}
	
		for (Method m : MethodKit.getRedefinedMethods(md))
		{		
			if ((m instanceof MethodDeclaration) && ((MethodDeclaration) m).getMemberParent().equals(this.superDeclaration))
			{
				this.abstractMethodDeclaration = (MethodDeclaration) m;
				this.abstractMethodPosition = super.getPosition(this.superDeclaration, this.abstractMethodDeclaration);
				this.newPosition = super.getPosition(this.superDeclaration, this.abstractMethodDeclaration);
				detach((MethodDeclaration) m);
				break;
			}
		}

		// Places method after last method in the class, or at the end.
		if ((this.newPosition == -1) && !(this.superDeclaration.getMembers().isEmpty()))
		{
			ASTList<MemberDeclaration> members = this.superDeclaration.getMembers();
			this.newPosition = this.superDeclaration.getMembers().size();
			
			for (int i = members.size() - 1; i >= 0; i--)
			{
				if (members.get(i) instanceof MethodDeclaration)
				{
					this.newPosition = i + 1;
					break;
				}
			}
		}
		else if (this.newPosition == -1)
			this.newPosition = 0;
		 
		// Do any references to the method in the class use "this."
		for (MemberReference mr : MethodKit.getReferences(si, md, this.currentDeclaration, true))
		{
			if (mr.toSource().contains("this."))
			{
				SuperReference sr = new SuperReference();
				for (int i = 0; i < mr.getExpressionCount(); i++)
					if (mr.getExpressionAt(i) instanceof ThisReference)
						mr.replaceChild(mr.getExpressionAt(i), sr);
			}
		}
		
		// Find "super." references in method and remove them from their parent element.
		ArrayList<SuperReference> references = super.getSuperReferences(md);
		for (SuperReference sr : references)
			sr.getASTParent().replaceChild(sr, null);
		
		detach(md);
		attach(md, this.superDeclaration, this.newPosition);
		
		// Add any applicable imports from the current class to the super class.
		this.superDeclarationImports =  UnitKit.getCompilationUnit(this.superDeclaration).getImports();
		ASTList<Import> imports =  this.superDeclarationImports;		

		// If they aren't already present, add the method imports.
		for (Import ci : methodImports)
		{
			boolean contains = false;

			for (Import i : imports)
			{
				if (i.toSource().substring(i.toSource().indexOf("import")).equals(ci.toSource().substring(ci.toSource().indexOf("import"))))
				{
					contains = true;
					break;
				}				
			}

			if (!contains)
				imports.add(ci);
		}
		
		// If the package import hasn't already been added and the supertype
		// is in a different package, create and add an import to the package.
		if (addPackageImport)
		{
			Import wholePackage = getProgramFactory().createImport(PackageKit.createPackageReference(getProgramFactory(), pack));
			boolean contains = false;

			for (Import i : imports)
			{
				if ((i.toString().equals(wholePackage.toString())))
				{
					contains = true;
					break;
				}
			}

			if (!contains)
			{
				imports.add(wholePackage);
			}
		}
		
		// If a field type from the method is defined as a class nested in the current class, need to contain an import to that class.
		for (Type t : types)
		{
			if ((t instanceof TypeDeclaration) && (((TypeDeclaration) t).getContainingClassType() instanceof TypeDeclaration) &&
				(((TypeDeclaration) t).getContainingClassType().equals(this.currentDeclaration)))
			{
				PackageReference proto = PackageKit.createPackageReference(getProgramFactory(), pack);
				ArrayList<Identifier> identifiers = new ArrayList<Identifier>();
				TypeDeclaration nestedClass = (TypeDeclaration) t;

				// It may be nested by more than one level, in which 
				// case each inner class needs to be included in the import.
				for (TypeDeclaration containingClass : super.getContainingClasses(nestedClass))
					identifiers.add(containingClass.getIdentifier());

				for (int i = identifiers.size() - 1; i >= 0; i--)
				{
					PackageReference fullPackage = getProgramFactory().createPackageReference(proto, identifiers.get(i));
					proto = fullPackage;
				}

				imports.add(getProgramFactory().createImport(proto));
				break;
			}
		}

		UnitKit.getCompilationUnit(this.superDeclaration).setImports(imports);
		
		// Specify refactoring information for results information.
		String currentUnitName = UnitKit.getCompilationUnit(this.currentDeclaration).getName();
		String superUnitName = UnitKit.getCompilationUnit(this.superDeclaration).getName();
		String currentPackageName = this.currentDeclaration.getPackage().getFullName();
		String superPackageName = this.superDeclaration.getPackage().getFullName();		
		String currentClassName = this.currentDeclaration.getFullName().substring(currentPackageName.length() + 1).replace('.', '\\');
		String superClassName = this.superDeclaration.getFullName().substring(superPackageName.length() + 1).replace('.', '\\');
		super.refactoringInfo = "Iteration " + iteration + ": \"Move Method Up\" applied to method " 
				+ super.getMethodName(md) + " from " + currentClassName + " to " + superClassName;
		
		// Stores list of names of classes affected by refactoring.
		super.affectedClasses = new ArrayList<String>(2);
		super.affectedClasses.add(super.getFileName(currentUnitName, currentClassName));
		super.affectedClasses.add(super.getFileName(superUnitName, superClassName));
		super.affectedElement = ":" + super.getMethodName(md) + ":";

		return setProblemReport(EQUIVALENCE);
	}

	public ProblemReport analyzeReverse() 
	{
		// Initialise and pick the element to visit.
		MethodDeclaration md = (MethodDeclaration) this.superDeclaration.getMembers().get(this.newPosition);

		// Do any references to the method in the class use "super."
		for (MemberReference mr : MethodKit.getReferences(getCrossReferenceSourceInfo(), md, this.currentDeclaration, true))
		{
			if (mr.toSource().contains("super."))
			{
				ThisReference tr = new ThisReference();
				for (int i = 0; i < mr.getExpressionCount(); i++)
					if (mr.getExpressionAt(i) instanceof SuperReference)
						mr.replaceChild(mr.getExpressionAt(i), tr);
			}
		}
				
		// Construct refactoring transformation.
		super.transformation = null;
		detach(md);
		attach(md, this.currentDeclaration, this.position);
		
		// Adds any duplicates of the method in the sister classes back.
		for (int i = 0; i < this.sisterClasses.size(); i++)
		{
			MethodDeclaration dec = md.deepClone();
			attach(dec, this.sisterClasses.get(i), this.sisterPositions.get(i));
		}
		
		if (this.abstractMethodPosition != -1)
			attach(this.abstractMethodDeclaration, this.superDeclaration, this.abstractMethodPosition);
		
		// Reset the imports in the class.
		UnitKit.getCompilationUnit(this.superDeclaration).setImports(this.superDeclarationImports);		
		return setProblemReport(EQUIVALENCE);
	}
	
	protected boolean mayRefactor(MethodDeclaration md)
	{
		TypeDeclaration td = md.getMemberParent();
		CrossReferenceSourceInfo si = getCrossReferenceSourceInfo();

		// Prevents "Zero Service" outputs logged to the console.
		if (td.getProgramModelInfo() == null)
			td.getFactory().getServiceConfiguration().getChangeHistory().updateModel();
		
		// Makes a number of initial checks against the method, the class and the super class in order to quickly exclude insufficient candidates. 
		if (!(td.getSupertypes().get(0) instanceof TypeDeclaration) || !(td.getSupertypes().get(0).isOrdinaryClass()) || !(td.isOrdinaryClass()) || 
			 (md instanceof ConstructorDeclaration) || (MethodKit.isMain(getServiceConfiguration().getNameInfo(), md)) || (md.isAbstract()) || 
			 (md.isPrivate()) || (MethodKit.isSerializationMethod(getServiceConfiguration().getNameInfo(), md)) || (td == null) || 
			 (td.getName() == null))
			return false;
		else
		{
			TypeDeclaration std = (TypeDeclaration) td.getSupertypes().get(0);
			
			// If the class implements any interfaces and the super class doesn't implement the same interfaces.
			if ((((ClassDeclaration) td).getImplementedTypes() != null) && 
				!(((ClassDeclaration) td).getImplementedTypes().equals(((ClassDeclaration) std).getImplementedTypes()))) 	
				return false;
			
			// Check if the super type has the method and if it is distinctly overridden by any other sub classes.
			for (Method m : MethodKit.getRedefinedMethods(md))
				if ((m instanceof MethodDeclaration) && (((MethodDeclaration) m).getMemberParent().equals(std)))
					for (Method m2 : MethodKit.getRedefiningMethods(si, m))
						if (!(((MethodDeclaration) m2).getMemberParent().equals(td)) && !(m2.equals(md)))
							return false;
			
			// Check if the super class contains an unrelated method with the same name.
			for (Method m : std.getMethods())
				if ((m.getName().equals(md.getName())) && (m.getSignature().size() == md.getSignature().size()) && 
					!(m.isAbstract()) && !(m.equals(md)))
					return false;
			
			// If the method is package, can it be accessed in the 
			// current class if it is moved to the super class.
			if (!(md.isProtected()) && !(md.isPublic()))
				if (!(std.getPackage().equals(td.getPackage())))
					return false;

			// Does the method use "this"
			if (md.toSource().contains("this"))
				return false;
			
			// Get methods referenced in method.
			ArrayList<MethodReference> methods = super.getMethods(md);
			
			// Check if methods can be accessed in super type.
			for (MethodReference mr : methods)
			{
				Method m = si.getMethod(mr);
				
				if (m.equals(si.getMethod(md)))
					continue;
				
				if ((m.getContainingClassType().equals(td)) && (mr.getReferencePrefix() == null))
					return false;
				
				// Check if method is in an outer class being accessed from a nested class.
				if (!(m.getContainingClassType().equals(td)) && (m instanceof ProgramElement) &&
					(UnitKit.getCompilationUnit(md).equals(UnitKit.getCompilationUnit((ProgramElement) m))))
					return false;
				
				if (m.isPrivate())
				{
					if (!(m.getContainingClassType().equals(std)))
						return false;
				}
				else if (!m.isPublic())
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
			
			// Get fields accessed in method.
			ArrayList<Field> fields = super.getFields(md, si);
			
			// Check if fields can be accessed in super type.
			for (Field f : fields)
			{				
				if (td.getFieldsInScope().contains(f))
					return false;
				
				// Check if field is in an outer class being accessed from a nested class.
				if (!(f.getContainingClassType().equals(td)) && (f instanceof ProgramElement) &&
					(UnitKit.getCompilationUnit(md).equals(UnitKit.getCompilationUnit((ProgramElement) f))))
					return false;
				
				if (f.isPrivate())
				{
					if (!(f.getContainingClassType().equals(std)))
						return false;
				}
				else if (!f.isPublic())
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
			
			// Similarly, if the current class is nested and the method references another nested
			// class in the unit that is within the same outer class, don't move the method (as
			// the outer class would need to be used in the nested class reference when moved).
			if (td.getContainingClassType() instanceof TypeDeclaration)
				for (TypeDeclaration typedec : super.getTypeDeclarations(UnitKit.getCompilationUnit(td)))
					if (!(typedec.equals(td)) && (super.getContainingClasses(typedec).contains(td.getContainingClassType())) && (types.contains(typedec)))
						return false;
			
			// If the current class is referenced in the method explicitly i.e. an
			// object of the current class is created within the method, don't move it.
			if (types.contains(td))
				return false;

			// Check if types can be accessed in super type.
			for (Type t: types)
			{
				if (((t instanceof TypeDeclaration) || (t instanceof ClassFile)))
				{					
					if (((ClassType) t).isPrivate())
					{
						if (!(((ClassType) t).equals(std)))
							return false;
					}
					else if (!((ClassType) t).isPublic())
					{
						if (!((ClassType) t).getPackage().equals(std.getPackage()))
						{
							if (!((ClassType) t).isProtected())
								return false;
							else if (!(std.getAllSupertypes().contains((ClassType) t)))
								return false;
						}
					}
				}
			}
			
			// Checks any reference to the method in the program and if the method is being 
			// statically referenced and is not referencing the super class it will be inapplicable.
			for (MemberReference mr : si.getReferences(md))
			{
				if (!(std.getPackage().equals(MiscKit.getParentTypeDeclaration(mr).getPackage())) && !(md.isPublic()))
					return false;
				
				if (md.isStatic())
					if ((((MethodReference) mr).getReferencePrefix() instanceof TypeReference) && 
						!(((MethodReference) mr).getReferencePrefix().toSource().equals(std.getName())))
						return false;
			}
			
			return true;
		}
	}
}