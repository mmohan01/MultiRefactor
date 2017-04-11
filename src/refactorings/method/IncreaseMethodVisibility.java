package refactorings.method;

import java.util.ArrayList;

import multirefactor.AccessFlags;
import recoder.CrossReferenceServiceConfiguration;
import recoder.abstraction.Method;
import recoder.convenience.TreeWalker;
import recoder.java.declaration.AnnotationPropertyDeclaration;
import recoder.java.declaration.ConstructorDeclaration;
import recoder.java.declaration.InterfaceDeclaration;
import recoder.java.declaration.MethodDeclaration;
import recoder.java.declaration.TypeDeclaration;
import recoder.java.declaration.modifier.Private;
import recoder.java.reference.MemberReference;
import recoder.java.reference.MethodReference;
import recoder.java.reference.SuperReference;
import recoder.java.reference.ThisReference;
import recoder.java.reference.TypeReference;
import recoder.kit.MethodKit;
import recoder.kit.MiscKit;
import recoder.kit.Problem;
import recoder.kit.ProblemReport;
import recoder.kit.transformation.Modify;
import recoder.service.CrossReferenceSourceInfo;
import refactorings.MethodRefactoring;

public class IncreaseMethodVisibility extends MethodRefactoring 
{		
	public IncreaseMethodVisibility(CrossReferenceServiceConfiguration sc) 
	{
		super(sc);
	}
	
	public IncreaseMethodVisibility() 
	{
		super();
	}
	
	public ProblemReport analyze(int iteration, int unit, int element) 
	{
		// Initialise and pick the element to visit.
		CrossReferenceServiceConfiguration config = getServiceConfiguration();
		ProblemReport report = EQUIVALENCE;		
		super.tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));
		
		for (int i = 0; i < element; i++)
			super.tw.next(MethodDeclaration.class);
	
		MethodDeclaration md = (MethodDeclaration) super.tw.getProgramElement();
		
		// Prevents "Zero Service" outputs logged to the console.
		if (md.getMemberParent().getProgramModelInfo() == null)
			md.getFactory().getServiceConfiguration().getChangeHistory().updateModel();

		// Construct refactoring transformation.
		super.transformation = new Modify(config, true, md, super.visibilityDown(md.getVisibilityModifier()));
		report = super.transformation.analyze();
		if (report instanceof Problem) 
			return setProblemReport(report);

		// Specify refactoring information for results information.
		String unitName = getSourceFileRepository().getKnownCompilationUnits().get(unit).getName();
		String packageName = MiscKit.getParentTypeDeclaration(md).getPackage().getFullName();
		String className = MiscKit.getParentTypeDeclaration(md).getFullName().substring(packageName.length() + 1).replace('.', '\\');
		super.refactoringInfo = "Iteration " + iteration + ": \"Increase Method Visibility\" applied at class " 
				+ className + " to method " + super.getMethodName(md) 
				+ " from " + super.currentModifier(md.getVisibilityModifier()) 
				+ " to " + super.refactoredDownModifier(md.getVisibilityModifier());
		
		// Stores list of names of classes affected by refactoring.
		super.affectedClasses = new ArrayList<String>(1);
		super.affectedClasses.add(super.getFileName(unitName, className));
		super.affectedElement = ":" + super.getMethodName(md) + ":";
		
		return setProblemReport(EQUIVALENCE);
	}
	
	public ProblemReport analyzeReverse() 
	{
		// Initialise and pick the element to visit.
		CrossReferenceServiceConfiguration config = getServiceConfiguration();
		ProblemReport report = EQUIVALENCE;	
		MethodDeclaration md = (MethodDeclaration) super.tw.getProgramElement();

		// Construct refactoring transformation.
		super.transformation = new Modify(config, true, md, super.visibilityUp(md.getVisibilityModifier()));
		report = super.transformation.analyze();
		if (report instanceof Problem) 
			return setProblemReport(report);
		
		return setProblemReport(EQUIVALENCE);
	}
	
	protected boolean mayRefactor(MethodDeclaration md)
	{		
		if ((md.getVisibilityModifier() instanceof Private) || (md instanceof AnnotationPropertyDeclaration) || 
			(md instanceof ConstructorDeclaration) || (md.getMemberParent() instanceof InterfaceDeclaration))
			return false;
		else if ((md.isAbstract()) && (super.visibilityDown(md.getVisibilityModifier()) == AccessFlags.PRIVATE))
			return false;
		else
		{
			CrossReferenceSourceInfo si = getCrossReferenceSourceInfo();
			ArrayList<TypeDeclaration> referenceTypes = new ArrayList<TypeDeclaration>();
			
			// Prevents "Zero Service" outputs logged to the console.
			if (md.getMemberParent().getProgramModelInfo() == null)
				md.getFactory().getServiceConfiguration().getChangeHistory().updateModel();
			
			// Checks any method the method redefines to see if 
			// it will become less visible than the original method.
			for (Method m : MethodKit.getAllRedefinedMethods(md))
			{
				int override = visibility(m);
				int overridden = visibility(md) - 1;
				
				if (overridden < override)
					return false;
			}
			
			// Checks any methods that redefine the method 
			// to see if they will be unable to access it. 
			for  (Method m : MethodKit.getRedefiningMethods(si, md))
			{
				if (super.visibilityDown(md.getVisibilityModifier()) == AccessFlags.PRIVATE)
				{
					if (!(md.getContainingClassType().equals(m.getContainingClassType())))
						return false;
				}
				else if (!md.getPackage().equals(m.getPackage()))
				{
					if (!(super.visibilityDown(md.getVisibilityModifier()) == AccessFlags.PROTECTED))
						return false;
					else if (!(m.getContainingClassType().getAllSupertypes().contains(md.getContainingClassType())))
						return false;
				}
			}
				
			// Checks any reference to the method in the program 
			// and whether its class will be able to access the method.
			for (MemberReference mr : si.getReferences(md))
			{			
				// Check if a sub class can access the method.
				if ((si.getType(((MethodReference) mr).getReferencePrefix()) instanceof TypeDeclaration) && 
					((((MethodReference) mr).getReferencePrefix() instanceof TypeReference) || 
					  ((si.getType(((MethodReference) mr).getReferencePrefix()) != null) && 
				       !(((MethodReference) mr).getReferencePrefix() instanceof ThisReference) && 
				       !(((MethodReference) mr).getReferencePrefix() instanceof SuperReference))))
				{
					if (!(referenceTypes.contains(si.getType(((MethodReference) mr).getReferencePrefix()))))
						referenceTypes.add((TypeDeclaration) si.getType(((MethodReference) mr).getReferencePrefix()));
				}
				
				if (MiscKit.getParentTypeDeclaration(mr) == null)
					return false;
				else if (!(referenceTypes.contains(MiscKit.getParentTypeDeclaration(mr))))
					referenceTypes.add(MiscKit.getParentTypeDeclaration(mr));
			}
			
			for (TypeDeclaration td : referenceTypes)
			{
				if (super.visibilityDown(md.getVisibilityModifier()) == AccessFlags.PRIVATE)
				{
					if (!(md.getMemberParent().equals(td)))
						return false;
				}
				else if (!(md.getMemberParent().getPackage().equals(td.getPackage())))
				{
					if (!(super.visibilityDown(md.getVisibilityModifier()) == AccessFlags.PROTECTED))
						return false;
					else if (!(td.getAllSupertypes().contains(md.getMemberParent())))
						return false;
				}
			}
			
			return true;
		}
	}
	
	private int visibility(Method m)
	{
		if (m.isPublic())
			return 3;
		else if (m.isProtected())
			return 2;
		else if (m.isPrivate())
			return 0;
		else return 1;
	}
}