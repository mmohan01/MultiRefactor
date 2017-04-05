package refactorings.method;

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
import recoder.java.declaration.ConstructorDeclaration;
import recoder.java.declaration.MemberDeclaration;
import recoder.java.declaration.MethodDeclaration;
import recoder.java.declaration.TypeDeclaration;
import recoder.java.declaration.TypeParameterDeclaration;
import recoder.java.reference.MemberReference;
import recoder.java.reference.MethodReference;
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

public class MoveMethodDown extends MethodRefactoring 
{
	private TypeDeclaration currentDeclaration, subDeclaration;
	private List<MemberReference> superReferences;
	private ArrayList<SuperReference> thisReferences;
	private ASTList<Import> subDeclarationImports;
	private int position, newPosition;
	private boolean identical;
	
	public MoveMethodDown(CrossReferenceServiceConfiguration sc) 
	{
		super(sc);
	}
	
	public MoveMethodDown() 
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
		mayRefactor(md);
		this.currentDeclaration = md.getMemberParent();
		
		// Prevents "Zero Service" outputs logged to the console.
		if (this.currentDeclaration.getProgramModelInfo() == null)
			md.getFactory().getServiceConfiguration().getChangeHistory().updateModel();

		ArrayList<Type> types = super.getTypes(md, si);
		this.position = super.getPosition(this.currentDeclaration, md);
		this.identical = false;
		ASTList<Import> methodImports = super.getMemberImports(types, UnitKit.getCompilationUnit(this.currentDeclaration).getImports(), si);
		boolean addPackageImport = false;
		Package pack = this.currentDeclaration.getPackage();		
		
		ASTList<MemberDeclaration> members = this.subDeclaration.getMembers();
		
		if (members.isEmpty())
			this.newPosition = 0;
		// Places method after last method in the class, or at the end.
		else
		{
			this.newPosition = members.size();
			
			for (int i = members.size() - 1; i >= 0; i--)
			{
				if (members.get(i) instanceof MethodDeclaration)
				{
					this.newPosition = i + 1;
					break;
				}
			}
		}
		
		// Construct refactoring transformation.
		// The transformation is handled here manually and the transformation
		// method will do nothing for this refactoring when it is called.
		super.transformation = null;
		getChangeHistory().begin(this);
		
		// Checks packages before changes are made to check if the current 
		// declaration package needs to be added as an import to its subtype.
		if (!(this.currentDeclaration.getPackage().equals(this.subDeclaration.getPackage())))
			addPackageImport = true;

		// Find "this." references in method and change them to "super." references.
		this.thisReferences =  new ArrayList<SuperReference>();
		for (ThisReference tr : super.getThisReferences(md))
		{
			if ((tr.getReferenceSuffix() != null) && !(tr.getReferenceSuffix().equals(md)))
			{
				SuperReference sr = new SuperReference(tr.deepClone());
				tr.getASTParent().replaceChild(tr, sr);
				this.thisReferences.add(sr);
			}
		}
		
		// Any "super." references to the method in the sub class are changed to "this." references.
		this.superReferences = MethodKit.getReferences(si, md, this.subDeclaration, true);
		for (MemberReference mr : this.superReferences)
			if (((MethodReference) mr).getReferencePrefix() instanceof SuperReference)
				((MethodReference) mr).replaceChild(((MethodReference) mr).getReferencePrefix(), new ThisReference());
		
		// If there is an identical method in the sub class, it is removed.
		for (Method m : this.subDeclaration.getMethods())
		{
			if (m.equals(md))
			{
				this.identical = true;
				this.newPosition = super.getPosition(this.subDeclaration, (MethodDeclaration) m);
				detach((MethodDeclaration) m);
				break;
			}
		}
		
		detach(md);
		attach(md, this.subDeclaration, this.newPosition);
		
		// Add any applicable imports from the current class to the sub class.
		this.subDeclarationImports =  UnitKit.getCompilationUnit(this.subDeclaration).getImports();
		ASTList<Import> imports =  this.subDeclarationImports;
		
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
		super.refactoringInfo = "Iteration " + iteration + ": \"Move Method Down\" applied to method " 
				+ super.getMethodName(md) + " from " + currentClassName	+ " to " + subClassName;
		
		// Stores list of names of classes affected by refactoring.
		super.affectedClasses = new ArrayList<String>(2);
		super.affectedClasses.add(super.getFileName(currentUnitName, currentClassName));
		super.affectedClasses.add(super.getFileName(subUnitName, subClassName));
		super.affectedElement = ":" + super.getMethodName(md) + ":";
		
		return setProblemReport(EQUIVALENCE);
	}

	public ProblemReport analyzeReverse() 
	{
		// Initialise and pick the element to visit.
		MethodDeclaration md = (MethodDeclaration) this.subDeclaration.getMembers().get(this.newPosition);

		// Find new "super." references in method and change them back to "this." references.
		for (SuperReference sr : this.thisReferences)
		{
			sr.setReferencePrefix(null);
			sr.getASTParent().replaceChild(sr, new ThisReference());	
		}
		
		// Any "this." references to the method in the sub class are changed back to "super." references.
		for (MemberReference mr : this.superReferences)
			if (((MethodReference) mr).getReferencePrefix() instanceof ThisReference)
				((MethodReference) mr).replaceChild(((MethodReference) mr).getReferencePrefix(), new SuperReference());
				
		
		// If there is an identical method in the sub class, it is added back to the class.
		if (this.identical)
			attach(md.deepClone(), this.subDeclaration, this.newPosition);

		// Construct refactoring transformation.
		super.transformation = null;
		detach(md);
		attach(md, this.currentDeclaration, this.position);
		
		// Reset the imports in the class.
		UnitKit.getCompilationUnit(this.subDeclaration).setImports(this.subDeclarationImports);		
		return setProblemReport(EQUIVALENCE);
	}
	
	protected boolean mayRefactor(MethodDeclaration md)
	{							
		TypeDeclaration td = md.getMemberParent();
		CrossReferenceSourceInfo si = getCrossReferenceSourceInfo();
		TypeDeclaration std;
		
		// Prevents "Zero Service" outputs logged to the console.
		if (td.getProgramModelInfo() == null)
			td.getFactory().getServiceConfiguration().getChangeHistory().updateModel();
		
		// Makes a number of initial checks against the method and the class in order to quickly exclude insufficient candidates. 
		if (!(td.isOrdinaryClass()) || (md instanceof ConstructorDeclaration) || (MethodKit.isMain(getServiceConfiguration().getNameInfo(), md)) ||
			 (MethodKit.isSerializationMethod(getServiceConfiguration().getNameInfo(), md)) || (md.isAbstract()) || (md.isPrivate()) || 
			 (td == null) || (td.getName() == null))
			return false;
		else
		{								
			// Prevents "Zero Service" outputs logged to the console.
			if (md.getMemberParent().getProgramModelInfo() == null)
				md.getFactory().getServiceConfiguration().getChangeHistory().updateModel();
						
			// Check if the method is overridden by any of the sub classes.
			if (MethodKit.getRedefiningMethods(si, md).size() > 0)
				return false;
			
			// Checks if the class contains one or more generic parameter
			// types and the method contains a reference to it/them.
			if (td.getTypeParameters() != null)
				for (TypeParameterDeclaration tpd : td.getTypeParameters())
					if (md.toSource().contains(tpd.getName()))
						return false;

			// Are there any references to the method in the class outside of the method itself.
			for (MemberReference mr : MethodKit.getReferences(si, md, td, true))
				if (!(mr.getASTParent().equals(md)))
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
			
			// Check each child class to see if the elements 
			// of the method can be accessed from the class.
			for (int i = 0; i < subtypes.size(); i++)
			{
				std = (TypeDeclaration) subtypes.get(i);
				boolean next = false;
				
				// Check if the sub class contains an unrelated method with the same name.
				for (Method m : std.getMethods())
				{
					if ((m.getName().equals(md.getName())) && (m.getSignature().size() == md.getSignature().size()) && 
						!(m.isAbstract()) && !(m.equals(md)))
					{
						next = true;
						break;
					}
				}
				
				if (next)
					continue;
				
				// If the class implements any interfaces and the sub class doesn't implement the same interfaces.
				if ((((ClassDeclaration) td).getImplementedTypes() != null) && 
					!(((ClassDeclaration) td).getImplementedTypes().equals(((ClassDeclaration) std).getImplementedTypes()))) 
					next = true;
				
				if (next)
					continue;
				
				// Get methods referenced in method.
				ArrayList<MethodReference> methods = super.getMethods(md);

				// Check if methods can be accessed in sub type.
				for (MethodReference mr : methods)
				{
					Method m = si.getMethod(mr);

					if (m.equals(si.getMethod(md)))
						continue;
					
					// Check if method is in an outer class being accessed from a nested class.
					if (!(m.getContainingClassType().equals(td)) && (m instanceof ProgramElement) &&
						(UnitKit.getCompilationUnit(md).equals(UnitKit.getCompilationUnit((ProgramElement) m))))
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
				
				// Get fields accessed in method.
				ArrayList<Field> fields = super.getFields(md, si);

				// Check if fields can be accessed in super type.
				for (Field f : fields)
				{
					// Check if field is in an outer class being accessed from a nested class.
					if (!(f.getContainingClassType().equals(td)) && (f instanceof ProgramElement) &&
						(UnitKit.getCompilationUnit(md).equals(UnitKit.getCompilationUnit((ProgramElement) f))))
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
				
				// Get types accessed in method.
				ArrayList<Type> types = super.getTypes(md, si);

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
				
				// Checks any reference to the method in the program and works out if
				// it will be applicable when the method is moved to the sub class.
				for (MemberReference mr : si.getReferences(md))
				{
					if (!(mr instanceof MethodReference))
					{
						next = true;
						break;
					}
					
					// Check whether the method can be accessed from the class.
					if (!(std.getPackage().equals(MiscKit.getParentTypeDeclaration(mr).getPackage())) && !(md.isPublic()))
					{
						if (!(md.isProtected()))
						{
							next = true;
							break;
						}
						else if (!(MiscKit.getParentTypeDeclaration(mr).getAllSupertypes().contains(std)))
						{
							next = true;
							break;
						}
					}
					
					// If reference is "this." or "super." or has no prefix 
					// it needs to be in the sub class or one of its sub classes.
					if ((((MethodReference) mr).getReferencePrefix() instanceof ThisReference) || 
						(((MethodReference) mr).getReferencePrefix() instanceof SuperReference) || 
						(((MethodReference) mr).getReferencePrefix() == null))         
					{
						if (!(MiscKit.getParentTypeDeclaration(mr).equals(std)) && 
							!(si.getAllSubtypes(std).contains(MiscKit.getParentTypeDeclaration(mr))))
						{
							next = true;
							break;
						}
					}
					// If the reference is prefixed by a type reference, it needs to be a
					// static method and the type reference needs to be the type of the sub class. 
					else if (((MethodReference) mr).getReferencePrefix() instanceof TypeReference)
					{
						if (!(((MethodReference) mr).getReferencePrefix().toSource().equals(std.getName())) || 
							!(md.isStatic()))
						{
							next = true;
							break;
						}
					}
					// If the reference if being called from a variable, the variable needs to be the type of the sub class or
					// one of its sub classes (in this case, if the method is a package type, it will need to be accessible).
					else
					{  
						if (!(si.getType(((MethodReference) mr).getReferencePrefix()).equals(std)))
						{
							if (!(si.getAllSubtypes(std).contains(si.getType(((MethodReference) mr).getReferencePrefix()))) ||
								(!(md.isProtected()) && !(md.isPublic()) && 
								 !(((TypeDeclaration) si.getType(((MethodReference) mr).getReferencePrefix())).getPackage().equals(std.getPackage()))))
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
		int from  = refactoringInfo.indexOf(" to method ") + 11;
		int to = refactoringInfo.indexOf(") ", from) + 1;
		String name = refactoringInfo.substring(from,  to);
		
		from = refactoringInfo.lastIndexOf(" to ") + 4; 
		String subclassName = refactoringInfo.substring(from,  refactoringInfo.length());

		while (tw.next(MethodDeclaration.class))
		{
			element++;
			MethodDeclaration md = (MethodDeclaration) tw.getProgramElement();
			
			if ((md.getName() != null) && (super.getMethodName(md).equals(name)))
				return ((mayRefactor(md)) && (this.subDeclaration.getName().equals(subclassName))) ? element : -1;
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