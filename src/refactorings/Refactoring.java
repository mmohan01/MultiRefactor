package refactorings;

import java.util.ArrayList;

import multirefactor.AccessFlags;
import recoder.CrossReferenceServiceConfiguration;
import recoder.abstraction.ArrayType;
import recoder.abstraction.ClassType;
import recoder.abstraction.Field;
import recoder.abstraction.Package;
import recoder.abstraction.PrimitiveType;
import recoder.abstraction.Type;
import recoder.convenience.AbstractTreeWalker;
import recoder.convenience.TreeWalker;
import recoder.java.CompilationUnit;
import recoder.java.Import;
import recoder.java.NonTerminalProgramElement;
import recoder.java.ProgramElement;
import recoder.java.declaration.MemberDeclaration;
import recoder.java.declaration.MethodDeclaration;
import recoder.java.declaration.TypeDeclaration;
import recoder.java.declaration.modifier.Private;
import recoder.java.declaration.modifier.Protected;
import recoder.java.declaration.modifier.Public;
import recoder.java.declaration.modifier.VisibilityModifier;
import recoder.java.reference.FieldReference;
import recoder.java.reference.MethodReference;
import recoder.java.reference.SuperReference;
import recoder.java.reference.ThisReference;
import recoder.java.reference.TypeReference;
import recoder.kit.Problem;
import recoder.kit.ProblemReport;
import recoder.kit.TwoPassTransformation;
import recoder.list.generic.ASTArrayList;
import recoder.list.generic.ASTList;
import recoder.service.CrossReferenceSourceInfo;

public abstract class Refactoring extends TwoPassTransformation 
{
	protected TwoPassTransformation transformation;
	protected String refactoringInfo; 
	protected ArrayList<String> affectedClasses;
	protected AbstractTreeWalker tw;
	
	public Refactoring(CrossReferenceServiceConfiguration sc) 
	{
		super(sc);
	}
		
	public Refactoring() 
	{
		super();
	}
	
	public abstract ProblemReport analyze(int iteration, int unit, int element);
	
	public abstract ProblemReport analyzeReverse();
	
	public abstract int getAmount(int unit);
	
	public abstract String getName(int unit, int element);
	
	public ProblemReport analyze(int iteration, String name, int element)
	{
		int unit = -1;

		for (int i = 0; i < getSourceFileRepository().getKnownCompilationUnits().size(); i++)
		{
			int index = getSourceFileRepository().getKnownCompilationUnits().get(i).getName().indexOf("MultiRefactor\\") + 14;
			String unitName = getSourceFileRepository().getKnownCompilationUnits().get(i).getName().substring(index);
			
			if (name.endsWith(unitName))
			{
				unit = i;
				break;
			}
		}
		
		return analyze(iteration, unit, element);
	}
	
	public void transform(ProblemReport p) 
	{
		if (p instanceof Problem)
        {
        	System.out.println("\r\nPROBLEM REPORT: ");
        	System.err.println(p.toString());
        }
		else
		{			
			if (this.transformation != null)
			{
				super.transform();
				this.transformation.transform();
			}
		}
	}	


	protected String currentModifier(VisibilityModifier vm)
	{		
		if (vm instanceof Public)
			return "public";
		else if (vm instanceof Protected)
			return "protected";
		else if (vm instanceof Private)
			return "private";
		else
			return "package";
	}
	
	protected int visibilityUp(VisibilityModifier vm)
	{
		if ((vm instanceof Public) || (vm instanceof Protected))
			return AccessFlags.PUBLIC;
		else if (vm instanceof Private)
			return AccessFlags.PACKAGE;
		else
			return AccessFlags.PROTECTED;
	}
	
	protected String refactoredUpModifier(VisibilityModifier vm)
	{		
		if ((vm instanceof Protected) || (vm instanceof Public))
			return "public";
		else if (vm instanceof Private)
			return "package";
		else
			return "protected";
	}
	
	protected int visibilityDown(VisibilityModifier vm)
	{
		if (vm instanceof Public)
			return AccessFlags.PROTECTED;
		else if (vm instanceof Protected)
			return AccessFlags.PACKAGE;
		else
			return AccessFlags.PRIVATE;
	}
	
	public String refactoredDownModifier(VisibilityModifier vm)
	{		
		if (vm instanceof Public)
			return "protected";
		else if (vm instanceof Protected)
			return "package";
		else 
			return "private";
	}
	

	// Returns all the "super." references in a member declaration.
	protected ArrayList<SuperReference> getSuperReferences(MemberDeclaration md)
	{
		ArrayList<SuperReference> references = new ArrayList<SuperReference>();

		TreeWalker tw = new TreeWalker(md);
		while (tw.next()) 
		{
			ProgramElement pe = tw.getProgramElement();
			if (pe instanceof SuperReference) 
				references.add((SuperReference) pe); 		                 
		}

		return references;
	}
	
	// Returns all the "this." references in a member declaration.
	protected ArrayList<ThisReference> getThisReferences(MemberDeclaration md)
	{
		ArrayList<ThisReference> references = new ArrayList<ThisReference>();

		TreeWalker tw = new TreeWalker(md);
		while (tw.next()) 
		{
			ProgramElement pe = tw.getProgramElement();
			if (pe instanceof ThisReference) 
				references.add((ThisReference) pe); 		                 
		}

		return references;
	}
	
	// Returns all the types referenced in a member declaration.
	protected ArrayList<Type> getTypes(MemberDeclaration md, CrossReferenceSourceInfo si)
	{
		ArrayList<Type> types = new ArrayList<Type>();
		
		if (md instanceof MethodDeclaration)
		{
			for (Type t : ((MethodDeclaration) md).getSignature())
			{
				if (t instanceof ArrayType)
					t = ((ArrayType) t).getBaseType();
				
				if ((t != null) && !(types.contains(t)) && !(t instanceof PrimitiveType) &&
					!((t.toString().startsWith("java.lang.")) && !(t.toString().contains("%RAW%"))))
					types.add(t); 
			}
			
			Type returnType = ((MethodDeclaration) md).getReturnType();
			
			if (returnType instanceof ArrayType)
				returnType = ((ArrayType) returnType).getBaseType();
			
			if ((returnType != null) && !(types.contains(returnType)) && !(returnType instanceof PrimitiveType) &&
			    !((returnType.toString().startsWith("java.lang.")) && !(returnType.toString().contains("%RAW%"))))
				types.add(returnType);
		}
		
		TreeWalker tw = new TreeWalker(md);
		while (tw.next()) 
		{
			ProgramElement pe = tw.getProgramElement();
			if (pe instanceof TypeReference) 
			{
				Type t = si.getType(pe);
				
				if (t instanceof ArrayType)
					t = ((ArrayType) t).getBaseType();
				
				if ((t != null) && !(types.contains(t)) && !(t instanceof PrimitiveType) && 
					!((t.toString().startsWith("java.lang.")) && !(t.toString().contains("%RAW%"))))	
					types.add(t);
			}
		}
		
		return types;
	}

	// Returns all the method references in a member declaration.
	protected ArrayList<MethodReference> getMethods(MemberDeclaration md)
	{
		ArrayList<MethodReference> methods = new ArrayList<MethodReference>();
		
		TreeWalker tw = new TreeWalker(md);
		while (tw.next()) 
		{
			ProgramElement pe = tw.getProgramElement();
			if ((pe instanceof MethodReference) && !(methods.contains(pe)))
				methods.add((MethodReference) pe);
		}

		return methods;
	}
	
	// Returns all the fields referenced in a member declaration.
	protected ArrayList<Field> getFields(MemberDeclaration md, CrossReferenceSourceInfo si)
	{
		ArrayList<Field> fields = new ArrayList<Field>();

		TreeWalker tw = new TreeWalker(md);
		while (tw.next()) 
		{
			ProgramElement pe = tw.getProgramElement();
			if ((pe instanceof FieldReference) && !(fields.contains(si.getField((FieldReference) pe))))
				fields.add(si.getField((FieldReference) pe));	                 
		}

		return fields;
	}
		
	// Returns all the type declarations in a compilation unit, including nested types.
	protected ArrayList<TypeDeclaration> getTypeDeclarations(CompilationUnit cu)
	{
		ArrayList<TypeDeclaration> types = new ArrayList<TypeDeclaration>();
		TreeWalker tw = new TreeWalker(cu);
		
		while (tw.next(TypeDeclaration.class)) 
			types.add((TypeDeclaration) tw.getProgramElement());

		return types;
	}

	// Returns all the imports needed in a class to compile a member declaration that references the list of types supplied.
	// Uses the list of imports from the class the member declaration was originally in to extract the necessary imports.
	protected ASTList<Import> getMemberImports(ArrayList<Type> methodTypes, ASTList<Import> classImports, CrossReferenceSourceInfo si)
	{
		ASTArrayList<Import> methodImports = new ASTArrayList<Import>(classImports.size());
		
		for (Import ci : classImports)
		{
			boolean add = false;
			
			if (ci.isMultiImport())
			{
				Package p;
			
				if (ci.getTypeReferenceCount() == 0)
					p = si.getPackage(ci.getPackageReference());
				else
					p = si.getPackage(si.getType(ci.getTypeReference()));
					
				for (Type type : methodTypes)
				{
					if ((type instanceof ClassType) && (p.getName().startsWith(((ClassType) type).getPackage().getName())))
					{
						add = true;
						methodTypes.remove(type);
						break;
					}
				}
			}
			else
			{
				for (Type t : methodTypes)
				{
					if (t.getName().equals(si.getType(ci.getTypeReference()).getName()))
					{
						add = true;
						methodTypes.remove(t);
						break;
					}
				}
			}
				
			if (add)
				methodImports.add(ci);
		}
		
		return methodImports;
	}
	
	// Returns the position of an element within its class with respect to the other elements,
	// or the position of a class in a compilation unit.
	protected int getPosition(NonTerminalProgramElement parent, MemberDeclaration child)
	{
		int position = 0;
		
		if (parent instanceof TypeDeclaration)
		{
			for (int i = 0; i < ((TypeDeclaration) parent).getMembers().size(); i++)
			{
				if (((TypeDeclaration) parent).getMembers().get(i).equals(child))
				{
					position = i;
					break;
				}
			}
		}
		else if (parent instanceof CompilationUnit)
		{
			for (int i = 0; i < ((CompilationUnit) parent).getTypeDeclarationCount(); i++)
			{
				if (((CompilationUnit) parent).getTypeDeclarationAt(i).equals(child))
				{
					position = i;
					break;
				}
			}
		}
		
		return position;
	}
	
	// Returns the set of outer types that a type declaration is declared within.
	protected ArrayList<TypeDeclaration> getContainingClasses(TypeDeclaration td)
	{
		ArrayList<TypeDeclaration> containingClasses = new ArrayList<TypeDeclaration>();
		
		while (td.getContainingClassType() != null)
		{
			td = td.getContainingClassType();
			containingClasses.add(td);
		}
		
		return containingClasses;
	}
	
	// Truncates a path name to just the element name.
	protected String getFileName(String input)
	{
		int lastDash = input.lastIndexOf("\\");
		int lastDot = input.lastIndexOf(".");
		if ((lastDash >= 0) && (lastDot >= 0)) 
			input = input.substring(lastDash + 1, lastDot);

		return input;
	}

	public String getRefactoringInfo()
	{
		return this.refactoringInfo;
	}
	
	public ArrayList<String> getAffectedClasses()
	{
		return this.affectedClasses;
	}
	
	public void setServiceConfiguration(CrossReferenceServiceConfiguration sc)
	{
		super.setServiceConfiguration(sc);
	}
}