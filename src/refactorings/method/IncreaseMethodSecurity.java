package refactorings.method;

import java.util.ArrayList;

import recoder.CrossReferenceServiceConfiguration;
import recoder.abstraction.Method;
import recoder.convenience.AbstractTreeWalker;
import recoder.convenience.TreeWalker;
import recoder.java.ProgramElement;
import recoder.java.declaration.AnnotationPropertyDeclaration;
import recoder.java.declaration.ConstructorDeclaration;
import recoder.java.declaration.InterfaceDeclaration;
import recoder.java.declaration.MethodDeclaration;
import recoder.java.declaration.TypeDeclaration;
import recoder.java.declaration.modifier.Private;
import recoder.java.reference.MemberReference;
import recoder.kit.MethodKit;
import recoder.kit.MiscKit;
import recoder.kit.Problem;
import recoder.kit.ProblemReport;
import recoder.kit.transformation.Modify;
import recoder.service.CrossReferenceSourceInfo;
import refactorings.Refactoring;
import refactory.AccessFlags;

public class IncreaseMethodSecurity extends Refactoring 
{		
	public IncreaseMethodSecurity(CrossReferenceServiceConfiguration sc) 
	{
		super(sc);
	}
	
	public IncreaseMethodSecurity() 
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
		{
			super.tw.next(MethodDeclaration.class);
			MethodDeclaration md = (MethodDeclaration) super.tw.getProgramElement();
			if (!mayRefactor(md))
				i--;
		}
	
		ProgramElement pe = super.tw.getProgramElement();
		MethodDeclaration md = (MethodDeclaration) pe;

		// Construct refactoring transformation.
		super.transformation = new Modify(config, true, md, super.visibilityDown(md.getVisibilityModifier()));
		report = super.transformation.analyze();
		if (report instanceof Problem) 
			return setProblemReport(report);

		// Specify refactoring information for results information.
		super.refactoringInfo = "Iteration " + iteration + ": \"Increase Method Security\" applied at class " 
				+ super.getFileName(getSourceFileRepository().getKnownCompilationUnits().get(unit).getName())
				+ " to method " + ((MethodDeclaration) pe).getName() + " from " + super.currentModifier(md.getVisibilityModifier()) 
				+ " to " + super.refactoredDownModifier(md.getVisibilityModifier());
		
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
	
	public boolean mayRefactor(MethodDeclaration md)
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

	// Count the amount of available elements in the chosen class for refactoring.
	// If an element is not applicable for the current refactoring it is not counted.
	public int getAmount(int unit)
	{
		int counter = 0;
		AbstractTreeWalker tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));

		// Only counts the relevant program element.
		while (tw.next(MethodDeclaration.class))
		{
			MethodDeclaration md = (MethodDeclaration) tw.getProgramElement();
			if (mayRefactor(md))
				counter++;
		}

		return counter;
	}
	
	public String getName(int unit, int element)
	{		
		AbstractTreeWalker tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));

		for (int i = 0; i < element; i++)
		{
			tw.next(MethodDeclaration.class);
			MethodDeclaration md = (MethodDeclaration) tw.getProgramElement();
			if (!mayRefactor(md))
				i--;
		}

		MethodDeclaration md = (MethodDeclaration) tw.getProgramElement();
		return md.getName();
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