package refactorings.field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import recoder.CrossReferenceServiceConfiguration;
import recoder.abstraction.ClassType;
import recoder.abstraction.Field;
import recoder.abstraction.Method;
import recoder.abstraction.Package;
import recoder.abstraction.Type;
import recoder.bytecode.ClassFile;
import recoder.convenience.TreeWalker;
import recoder.java.Import;
import recoder.java.ProgramElement;
import recoder.java.declaration.ClassDeclaration;
import recoder.java.declaration.FieldDeclaration;
import recoder.java.declaration.MemberDeclaration;
import recoder.java.declaration.MethodDeclaration;
import recoder.java.declaration.TypeDeclaration;
import recoder.java.declaration.TypeParameterDeclaration;
import recoder.java.reference.FieldReference;
import recoder.java.reference.MethodReference;
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

public class MoveFieldDown extends FieldRefactoring 
{
	private TypeDeclaration currentDeclaration, subDeclaration;
	private List<VariableReference> superReferences;
	private ArrayList<SuperReference> thisReferences;
	private ASTList<Import> subDeclarationImports;
	private int position, newPosition, duplicatePosition;

	public MoveFieldDown(CrossReferenceServiceConfiguration sc) 
	{
		super(sc);
	}
	
	public MoveFieldDown() 
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
		mayRefactor(fd);
		this.position = super.getPosition(this.currentDeclaration, fd);
		this.duplicatePosition = -1;
		this.superReferences = new ArrayList<VariableReference>();
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
		// declaration package needs to be added as an import to its subtype.
		if (!(this.currentDeclaration.getPackage().equals(this.subDeclaration.getPackage())))
			addPackageImport = true;

		// Find "this." references in field and change them to "super." references.
		this.thisReferences =  new ArrayList<SuperReference>();
		for (ThisReference tr : super.getThisReferences(fd))
		{
			SuperReference sr = new SuperReference(tr.deepClone());
			tr.getASTParent().replaceChild(tr, sr);
			this.thisReferences.add(sr);
		}

		// Checks if the sub class contains an identical version of the field.
		for (MemberDeclaration dec : this.subDeclaration.getMembers())
		{
			if ((dec instanceof FieldDeclaration) && (dec.toSource().equals(fd.toSource())))
			{
				this.duplicatePosition = super.getPosition(this.subDeclaration, dec);
				detach(dec);
				break;
			}
		}
		
		// Any "super." references to the field in the sub class are changed to "this." references.
		this.superReferences.addAll(VariableKit.getReferences(si, fd.getFieldSpecifications().get(0), this.subDeclaration, true));

		for (VariableReference vr : this.superReferences)
			if (((FieldReference) vr).getReferencePrefix() instanceof SuperReference)
				((VariableReference) vr).replaceChild(((FieldReference) vr).getReferencePrefix(), new ThisReference());
		
		ASTList<MemberDeclaration> members = this.subDeclaration.getMembers();
		
		if (members.isEmpty())
			this.newPosition = 0;
		else
		{
			// Places field after last field in the class, or at the start.
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

		detach(fd);
		attach(fd, this.subDeclaration, this.newPosition);
		
		// Add any applicable imports from the current class to the sub class.
		this.subDeclarationImports =  UnitKit.getCompilationUnit(this.subDeclaration).getImports();
		ASTList<Import> imports =  this.subDeclarationImports;
		
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
				
		UnitKit.getCompilationUnit(this.subDeclaration).setImports(imports);

		// Specify refactoring information for results information.
		String currentUnitName = UnitKit.getCompilationUnit(this.currentDeclaration).getName();
		String subUnitName = UnitKit.getCompilationUnit(this.subDeclaration).getName();
		String currentPackageName = this.currentDeclaration.getPackage().getFullName();
		String subPackageName = this.subDeclaration.getPackage().getFullName();
		String currentClassName = this.currentDeclaration.getFullName().substring(currentPackageName.length() + 1).replace('.', '\\');
		String subClassName = this.subDeclaration.getFullName().substring(subPackageName.length() + 1).replace('.', '\\');
		super.refactoringInfo = "Iteration " + iteration + ": \"Move Field Down\" applied to field " 
				+ fd.toString().substring(last + 2) + " from " + currentClassName + " to " + subClassName;
		
		// Stores list of names of classes affected by refactoring.
		super.affectedClasses = new ArrayList<String>(2);
		super.affectedClasses.add(super.getFileName(currentUnitName, currentClassName));
		super.affectedClasses.add(super.getFileName(subUnitName, subClassName));
		super.affectedElement = "::" + fd.toString();
		
		return setProblemReport(EQUIVALENCE);
	}

	public ProblemReport analyzeReverse() 
	{
		// Initialise and pick the element to visit.
		FieldDeclaration fd = (FieldDeclaration) this.subDeclaration.getMembers().get(this.newPosition);
				
		// Find new "super." references in field and change them back to "this." references.
		for (SuperReference sr : this.thisReferences)
		{
			sr.setReferencePrefix(null);
			sr.getASTParent().replaceChild(sr, new ThisReference());	
		}
				
		// Any "this." references to the field in the sub class are changed back to "super." references.
		for (VariableReference vr : this.superReferences)
			if (((FieldReference) vr).getReferencePrefix() instanceof ThisReference)
				((VariableReference) vr).replaceChild(((FieldReference) vr).getReferencePrefix(), new SuperReference());
				
		// Construct refactoring transformation.
		super.transformation = null;
		detach(fd);
		attach(fd, this.currentDeclaration, this.position);
		
		// If there was a duplicate of the field in the class originally it is added back.
		if (duplicatePosition != -1)
		{
			FieldDeclaration dec = fd.deepClone();
			attach(dec, this.subDeclaration, this.duplicatePosition);
		}
		
		UnitKit.getCompilationUnit(this.subDeclaration).setImports(this.subDeclarationImports);		
		return setProblemReport(EQUIVALENCE);
	}
	
	protected boolean mayRefactor(FieldDeclaration fd)
	{
		TypeDeclaration td =  MiscKit.getParentTypeDeclaration(fd);
		CrossReferenceSourceInfo si = getCrossReferenceSourceInfo();
		TypeDeclaration std;
		
		// Makes initial checks against the field and the class in order to quickly exclude insufficient candidates. 
		if (!(td.isOrdinaryClass()) || (fd.isPrivate()) || (fd.getFieldSpecifications().size() > 1))
			return false;
		else
		{			
			// Are there any references to the field in the class.
			if (VariableKit.getReferences(si, fd.getFieldSpecifications().get(0), td, true).size() > 0)
				return false;
			
			// Checks if the class contains one or more generic parameter
			// types and the field contains a reference to it/them.
			if (td.getTypeParameters() != null)
				for (TypeParameterDeclaration tpd : td.getTypeParameters())
					if (fd.toSource().contains(tpd.getName()))
						return false;
			
			// Allows the program to go through the child classes in order of class name
			// and pick the first one (if any) that is applicable for the refactoring.
			List<ClassType> subtypes = si.getSubtypes(td);
			List<ClassType> exclude = new ArrayList<ClassType>();

			for (ClassType ct : subtypes)
				if ((ct.getName() == null) || !(ct instanceof ClassDeclaration))
					exclude.add(ct);

			subtypes.removeAll(exclude);
			Collections.sort(subtypes,  new NameComparator());
			
			// Check each child class to see if the field can be accessed from the class.
			for (int i = 0; i < subtypes.size(); i++)
			{
				std = (TypeDeclaration) subtypes.get(i);
				boolean next = false;
				
				// Checks if a field with the same name is in the sub class that isn't identical.
				for (MemberDeclaration md : std.getMembers())
					if ((md.toString().equals(fd.toString())) && !(md.toSource().equals(fd.toSource())))
						next = true;
				
				if (next)
					continue;
				
				// If the class implements any interfaces and the sub class doesn't implement the same interfaces.
				if ((((ClassDeclaration) td).getImplementedTypes() != null) && 
					!(((ClassDeclaration) td).getImplementedTypes().equals(((ClassDeclaration) std).getImplementedTypes()))) 
					next = true;
				
				if (next)
					continue;

				// Checks if the sub class already contains a field with the same name.
				for (MemberDeclaration md : std.getMembers())
				{
					if (md instanceof FieldDeclaration)
					{
						int lastmd = md.toString().lastIndexOf(">");
						int lastfd = fd.toString().lastIndexOf(">");
						if ((fd.toString().substring(lastfd + 2).equals(md.toString().substring(lastmd + 2))) && !(fd.equals(md)))
						{
							next = true;
							break;
						}
					}
				}
				
				if (next)
					continue;
				
				// Get any methods referenced in field.
				ArrayList<MethodReference> methods = super.getMethods(fd);

				// Check if methods can be accessed in sub type.
				for (MethodReference mr : methods)
				{
					Method m = si.getMethod(mr);
					
					// Check if method is in an outer class being accessed from a nested class.
					if (!(m.getContainingClassType().equals(td)) && (m instanceof ProgramElement) &&
						(UnitKit.getCompilationUnit(fd).equals(UnitKit.getCompilationUnit((ProgramElement) m))))
						return false;

					if (m.isPrivate())
					{
						if (!(m.getContainingClassType().equals(std)))
						{
							next = true;
							break;
						}
					}
					else if (!m.isPublic())
					{
						if (!m.getPackage().equals(std.getPackage()))
						{
							if (!m.isProtected())
							{
								next = true;
								break;
							}
							else if (!(std.getAllSupertypes().contains(m.getContainingClassType())))
							{
								next = true;
								break;
							}
						}
					}
				}

				if (next)
					continue;
				
				// Check if there are any other fields used by the field.
				ArrayList<Field> fields = super.getFields(fd, si);

				// Check if fields can be accessed in super type.
				for (Field f : fields)
				{
					if (f.equals(fd.getFieldSpecifications().get(0)))
						continue;
					
					// Check if field is in an outer class being accessed from a nested class.
					if (!(f.getContainingClassType().equals(td)) && (f instanceof ProgramElement) &&
						(UnitKit.getCompilationUnit(fd).equals(UnitKit.getCompilationUnit((ProgramElement) f))))
						return false;
					
					if (f.isPrivate())
					{
						if (!(f.getContainingClassType().equals(std)))
						{
							next = true;
							break;
						}
					}
					else if (!f.isPublic())
					{
						if (!f.getContainingClassType().getPackage().equals(std.getPackage()))
						{
							if (!f.isProtected())
							{
								next = true;
								break;
							}
							else if (!(std.getAllSupertypes().contains(f.getContainingClassType())))
							{
								next = true;
								break;
							}
						}
					}
				}

				if (next)
					continue;
				
				// Get types accessed by field.
				ArrayList<Type> types = super.getTypes(fd, si);

				// Check if types can be accessed in super type.
				for (Type t: types)
				{
					if (((t instanceof TypeDeclaration) || (t instanceof ClassFile)))
					{
						if (((ClassType) t).isPrivate())
						{
							if (!(((ClassType) t).equals(std)))
							{
								next = true;
								break;
							}
						}
						else if (!((ClassType) t).isPublic())
						{
							if (!((ClassType) t).getPackage().equals(std.getPackage()))
							{
								if (!((ClassType) t).isProtected())
								{
									next = true;
									break;
								}
								else if (!(std.getAllSupertypes().contains((ClassType) t)))
								{
									next = true;
									break;
								}
							}
						}
					}
				}
				
				if (next)
					continue;
				
				// Checks any reference to the field in the program and if the class used to reference the
				// field is not the current child class then the child class will not be applicable.
				// An exception is if the field reference is a super reference in the child class. In this
				// case the reference can be modified during the refactoring to point to the right class.
				for (FieldReference fr : si.getReferences(fd.getFieldSpecifications().get(0)))
				{
					// Check whether the field can be accessed from the class.
					if (!(std.getPackage().equals(MiscKit.getParentTypeDeclaration(fr).getPackage())) && !(fd.isPublic()))
					{
						if (!(fd.isProtected()))
						{
							next = true;
							break;
						}
						else if (!(MiscKit.getParentTypeDeclaration(fr).getAllSupertypes().contains(std)))
						{
							next = true;
							break;
						}
					}

					// If reference is "this." or "super." or has no prefix 
					// it needs to be in the sub class or one of its sub classes.
					if ((fr.getReferencePrefix() instanceof ThisReference) || (fr.getReferencePrefix() instanceof SuperReference) ||
							(fr.getReferencePrefix() == null))
					{
						if (!(MiscKit.getParentTypeDeclaration(fr).equals(std)) &&
								!(si.getAllSubtypes(std).contains(MiscKit.getParentTypeDeclaration(fr))))
						{
							next = true;
							break;
						}
					}
					// If the reference is prefixed by a type reference, it needs to be a
					// static field and the type reference needs to be the type of the sub class. 
					else if (fr.getReferencePrefix() instanceof TypeReference)
					{
						if (!(fr.getReferencePrefix().toSource().equals(std.getName())) || !(fd.isStatic()))
						{
							next = true;
							break;
						}
					}
					// If the reference if being called from a variable, the variable needs to be the type of the sub class or
					// one of its sub classes (in this case, if the field is a package type, it will need to be accessible).
					else
					{
						if (!(si.getType(fr.getReferencePrefix()).equals(std)))
						{
							if (!(si.getAllSubtypes(std).contains(si.getType(fr.getReferencePrefix()))) || 
									(!(fd.isProtected()) && !(fd.isPublic()) && 
											!(((TypeDeclaration) si.getType(fr.getReferencePrefix())).getPackage().equals(std.getPackage()))))
							{
								next = true;
								break;
							}
						}
					}
				}

				if (!next)
				{
					this.subDeclaration = std;
					return true;
				}
			}
			
			return false;
		}
	}
	
	// For this refactoring a subclass is chosen to move to if there are multiple options.
	// This override of the method checks that, not only is the refactoring applicable,
	// but it is also being applied in the same way by moving to the same subclass.
	public int checkElements(int unit, String refactoringInfo)
	{		
		TreeWalker tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));
		int element = 0;
		int from  = refactoringInfo.indexOf(" to field ") + 10;
		int to = refactoringInfo.indexOf(' ', from);
		String name = refactoringInfo.substring(from,  to);
		
		from = refactoringInfo.lastIndexOf(" to ") + 4; 
		String subclassName = refactoringInfo.substring(from, refactoringInfo.length());

		while (tw.next(FieldDeclaration.class))
		{
			element++;
			FieldDeclaration fd = (FieldDeclaration) tw.getProgramElement();
			int last = fd.toString().lastIndexOf(">");
			
			if ((fd.toString() != null) && (fd.toString().substring(last + 2).equals(name)))
			{
				return ((mayRefactor(fd)) && (this.subDeclaration.getName().equals(subclassName))) ? element : -1;
			}
		}
		
		return -1;
	}
	
	// This inner class allows sorting by name so that the list is sorted alphanumerically by the class names.
	private class NameComparator implements Comparator<ClassType> 
	{
		// Compares the two specified individuals using the fitness
		// operator. Returns less than 1, 0 or more than 1 as the first
		// argument is less than, equal to, or greater than the second.
		public int compare(ClassType ct1, ClassType ct2) 
		{   
			return ct1.getName().compareTo(ct2.getName());
		}
	}
}