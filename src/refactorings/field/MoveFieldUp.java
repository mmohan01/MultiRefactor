package refactorings.field;

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
import recoder.java.declaration.FieldDeclaration;
import recoder.java.declaration.MemberDeclaration;
import recoder.java.declaration.MethodDeclaration;
import recoder.java.declaration.TypeDeclaration;
import recoder.java.reference.FieldReference;
import recoder.java.reference.MethodReference;
import recoder.java.reference.PackageReference;
import recoder.java.reference.SuperReference;
import recoder.java.reference.ThisReference;
import recoder.java.reference.TypeReference;
import recoder.java.reference.VariableReference;
import recoder.kit.MiscKit;
import recoder.kit.PackageKit;
import recoder.kit.ProblemReport;
import recoder.kit.UnitKit;
import recoder.kit.VariableKit;
import recoder.list.generic.ASTList;
import recoder.service.CrossReferenceSourceInfo;
import refactorings.FieldRefactoring;

public class MoveFieldUp extends FieldRefactoring 
{
	private TypeDeclaration currentDeclaration, superDeclaration;
	private int position, newPosition;
	private ArrayList<TypeDeclaration> sisterClasses;
	private ArrayList<Integer> sisterPositions;	
	private ASTList<Import> superDeclarationImports;
	
	public MoveFieldUp(CrossReferenceServiceConfiguration sc) 
	{
		super(sc);
	}
	
	public MoveFieldUp() 
	{
		super();
	}

	public ProblemReport analyze(int iteration, int unit, int element) 
	{
		// Initialise and pick the element to visit.
		CrossReferenceSourceInfo si = getCrossReferenceSourceInfo();
		super.tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));
		
		for (int i = 0; i < element; i++)
			super.tw.next(FieldDeclaration.class);
		
		FieldDeclaration fd = (FieldDeclaration) super.tw.getProgramElement();
		this.currentDeclaration = MiscKit.getParentTypeDeclaration(fd);
		
		// Prevents "Zero Service" outputs logged to the console.
		if (this.currentDeclaration.getProgramModelInfo() == null)
			fd.getFactory().getServiceConfiguration().getChangeHistory().updateModel();
		
		int last = fd.toString().lastIndexOf(">");
		this.position = super.getPosition(this.currentDeclaration, fd);
		this.superDeclaration = (TypeDeclaration) this.currentDeclaration.getSupertypes().get(0);
		this.newPosition = -1;
		sisterClasses = new ArrayList<TypeDeclaration>();
		sisterPositions = new ArrayList<Integer>();
		ArrayList<Type> types = super.getTypes(fd, si);
		ASTList<Import> fieldImports = super.getMemberImports(types, UnitKit.getCompilationUnit(this.currentDeclaration).getImports(), si);
		boolean addPackageImport = false;
		Package pack = this.currentDeclaration.getPackage();

		// Construct refactoring transformation.
		// The transformation is handled here manually and the transformation
		// method will do nothing for this refactoring when it is called.
		super.transformation = null;
		getChangeHistory().begin(this);
		
		// Checks packages before changes are made to check if the current 
		// declaration package needs to be added as an import to its supertype.
		if (!(this.currentDeclaration.getPackage().equals(this.superDeclaration.getPackage())))
			addPackageImport = true;
				
		// Removes any identical versions of the field in the sister classes. 
		for (ClassType ct : si.getSubtypes(this.superDeclaration))
		{
			if (!(ct.equals(this.currentDeclaration)) && (ct instanceof TypeDeclaration))
			{
				for (MemberDeclaration dec : ((TypeDeclaration) ct).getMembers())
				{
					if (dec.equals(fd))
					{
						this.sisterClasses.add((TypeDeclaration) ct);
						this.sisterPositions.add(super.getPosition(((TypeDeclaration) ct), dec));
						detach(dec);
					}
				}
			}
		}
		
		// Also checks if the super class contains an identical version of the field.
		for (MemberDeclaration dec : this.superDeclaration.getMembers())
		{
			if (dec.equals(fd))
			{
				this.sisterClasses.add(this.superDeclaration);
				this.sisterPositions.add(super.getPosition(this.superDeclaration, dec));
				this.newPosition = super.getPosition(this.superDeclaration, dec);
				detach(dec);
			}
		}
		
		// Places field after last field in the class, or at the start.
		if ((this.newPosition == -1) && !(this.superDeclaration.getMembers().isEmpty()))
		{
			ASTList<MemberDeclaration> members = this.superDeclaration.getMembers();
			
			for (int i = 0; i < members.size(); i++)
			{
				if (members.get(i) instanceof MethodDeclaration)
				{
					this.newPosition = i;
					break;
				}
				else if (i == members.size() - 1)
					this.newPosition = members.size();
			}
		}
		else if (this.newPosition == -1)
			this.newPosition = 0;

		// Do any references to the field in the class use "this."
		for (VariableReference mr : VariableKit.getReferences(si, fd.getFieldSpecifications().get(0), this.currentDeclaration, true))
		{
			if ((mr.toSource().contains("this.")))
			{
				SuperReference sr = new SuperReference();

				for (int i = 0; i < mr.getChildCount(); i++)
					if ((mr.getChildAt(i) instanceof ThisReference) && 
						!(((ThisReference) mr.getChildAt(i)).getReferencePrefix() instanceof TypeReference))
						mr.replaceChild(mr.getChildAt(i), sr);
			}
		}
		
		// Find "super." references in field and remove them from their parent element.
		ArrayList<SuperReference> references = super.getSuperReferences(fd);
		for (SuperReference sr : references)
			sr.getASTParent().replaceChild(sr, null);
		
		detach(fd);
		attach(fd, this.superDeclaration, this.newPosition);
		
		// Add any applicable imports from the current class to the super class.
		this.superDeclarationImports =  UnitKit.getCompilationUnit(this.superDeclaration).getImports();
		ASTList<Import> imports =  this.superDeclarationImports;
		
		// If they aren't already present, add the field imports.
		for (Import ci : fieldImports)
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
				imports.add(wholePackage);
		}
		
		// If the field type is defined as a class nested in the current class, need to contain an import to that class.
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
		super.refactoringInfo = "Iteration " + iteration + ": \"Move Field Up\" applied to field " 
				+ fd.toString().substring(last + 2)	+ " from " + currentClassName + " to " + superClassName;
		
		// Stores list of names of classes affected by refactoring.
		super.affectedClasses = new ArrayList<String>(2);
		super.affectedClasses.add(super.getFileName(currentUnitName, currentClassName));
		super.affectedClasses.add(super.getFileName(superUnitName, superClassName));
		super.affectedElement = "::" + fd.toString();
		
		return setProblemReport(EQUIVALENCE);
	}

	public ProblemReport analyzeReverse() 
	{
		// Initialise and pick the element to visit.
		FieldDeclaration fd = (FieldDeclaration) this.superDeclaration.getMembers().get(this.newPosition);

		// Do any references to the field in the class use "super."
		for (VariableReference mr : VariableKit.getReferences(getCrossReferenceSourceInfo(), fd.getFieldSpecifications().get(0), this.currentDeclaration, true))
		{
			if (mr.toSource().contains("super."))
			{
				ThisReference tr = new ThisReference();

				for (int i = 0; i < mr.getChildCount(); i++)
					if (mr.getChildAt(i) instanceof SuperReference)
						mr.replaceChild(mr.getChildAt(i), tr);
			}
		}
				
		// Construct refactoring transformation.
		super.transformation = null;
		detach(fd);
		attach(fd, this.currentDeclaration, this.position);

		// Adds any duplicates of the field in the sister classes back.
		for (int i = 0; i < this.sisterClasses.size(); i++)
		{
			FieldDeclaration dec = fd.deepClone();
			attach(dec, this.sisterClasses.get(i), this.sisterPositions.get(i));
		}
		
		// Reset the imports in the class.
		UnitKit.getCompilationUnit(this.superDeclaration).setImports(this.superDeclarationImports);	
		return setProblemReport(EQUIVALENCE);
	}
	
	protected boolean mayRefactor(FieldDeclaration fd)
	{		
		TypeDeclaration td =  MiscKit.getParentTypeDeclaration(fd);
		CrossReferenceSourceInfo si = getCrossReferenceSourceInfo();
		
		// Prevents "Zero Service" outputs logged to the console.
		if (td.getProgramModelInfo() == null)
			td.getFactory().getServiceConfiguration().getChangeHistory().updateModel();
		
		// Makes a number of initial checks against the field, the class and the super class in order to quickly exclude insufficient candidates. 
		if (!(td.getSupertypes().get(0) instanceof TypeDeclaration) || !(td.getSupertypes().get(0).isOrdinaryClass()) || 
			!(td.isOrdinaryClass()) || (fd.isPrivate()) || (fd.toSource().contains("this")) || (fd.getFieldSpecifications().size() > 1))
			return false;
		else
		{
			TypeDeclaration std = (TypeDeclaration) td.getSupertypes().get(0);
			
			// If the class implements any interfaces and the super class doesn't implement the same interfaces.
			if ((((ClassDeclaration) td).getImplementedTypes() != null) && 
				!(((ClassDeclaration) td).getImplementedTypes().equals(((ClassDeclaration) std).getImplementedTypes()))) 
				return false;
			
			// Checks if the super class already contains a field with the same name.
			for (MemberDeclaration dec : std.getMembers())
			{
				if (dec instanceof FieldDeclaration)
				{
					int lastdec = dec.toString().lastIndexOf(">");
					int lastfd = fd.toString().lastIndexOf(">");
					if ((fd.toString().substring(lastfd + 2).equals(dec.toString().substring(lastdec + 2))) && !(fd.equals(dec)))
						return false;
				}
			}
			
			// If the field is package, can it be accessed in the 
			// current class if it is moved to the super class.
			if (!(fd.isProtected()) && !(fd.isPublic()))
				if (!(std.getPackage().equals(td.getPackage())))
					return false;
			
			// Get any methods referenced in field.
			ArrayList<MethodReference> methods = super.getMethods(fd);

			// Check if methods can be accessed in super type.
			for (MethodReference mr : methods)
			{
				Method m = si.getMethod(mr);

				if ((m.getContainingClassType().equals(td)) && (mr.getReferencePrefix() == null))
					return false;
				
				// Check if method is in an outer class being accessed from a nested class.
				if (!(m.getContainingClassType().equals(td)) && (m instanceof ProgramElement) &&
					(UnitKit.getCompilationUnit(fd).equals(UnitKit.getCompilationUnit((ProgramElement) m))))
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

			// Check if there are any other fields used by the field.
			ArrayList<Field> fields = super.getFields(fd, si);

			// Check if fields can be accessed in super type.
			for (Field f : fields)
			{								
				if (f.equals(fd.getFieldSpecifications().get(0)))
					continue;
				
				if (td.getFieldsInScope().contains(f))
					return false;
				
				// Check if field is in an outer class being accessed from a nested class.
				if (!(f.getContainingClassType().equals(td)) && (f instanceof ProgramElement) &&
					(UnitKit.getCompilationUnit(fd).equals(UnitKit.getCompilationUnit((ProgramElement) f))))
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

			// Get types accessed by field.
			ArrayList<Type> types = super.getTypes(fd, si);
			
			// In the case that a nested class in the current unit is being referenced 
			// in the object and the super class is in a separate package, don't move it. 
			for (TypeDeclaration typedec : super.getTypeDeclarations(UnitKit.getCompilationUnit(td)))
				if (!(typedec.equals(td)) && (types.contains(typedec)) && !(td.getPackage().equals(std.getPackage())))
					return false;

			// Similarly, if the current class is nested and the field references another nested
			// class in the unit that is within the same outer class, don't move the field (as
			// the outer class would need to be used in the nested class reference when moved).
			if (td.getContainingClassType() instanceof TypeDeclaration)
				for (TypeDeclaration typedec : super.getTypeDeclarations(UnitKit.getCompilationUnit(td)))
					if (!(typedec.equals(td)) && (super.getContainingClasses(typedec).contains(td.getContainingClassType())) && (types.contains(typedec)))
						return false;

			// If the current class is referenced in the field explicitly i.e. an
			// object of the current class is being created, don't move it.
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
			
			// Checks any reference to the field in the program and if the field is being 
			// statically referenced and is not referencing the super class it will be inapplicable.
			for (FieldReference fr : si.getReferences(fd.getFieldSpecifications().get(0)))
			{
				if (!(std.getPackage().equals(MiscKit.getParentTypeDeclaration(fr).getPackage())) && !(fd.isPublic()))
					return false;

				if (fd.isStatic())
					if ((fr.getReferencePrefix() instanceof TypeReference) && !(fr.getReferencePrefix().toSource().equals(std.getName())))
						return false;
			}
			
			return true;
		}
	}
}