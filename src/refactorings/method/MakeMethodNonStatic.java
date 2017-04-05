package refactorings.method;

import java.util.ArrayList;

import multirefactor.AccessFlags;
import recoder.CrossReferenceServiceConfiguration;
import recoder.convenience.TreeWalker;
import recoder.java.Import;
import recoder.java.declaration.MethodDeclaration;
import recoder.java.reference.MemberReference;
import recoder.java.reference.MethodReference;
import recoder.java.reference.TypeReference;
import recoder.kit.MethodKit;
import recoder.kit.MiscKit;
import recoder.kit.Problem;
import recoder.kit.ProblemReport;
import recoder.kit.UnitKit;
import recoder.kit.transformation.Modify;
import refactorings.MethodRefactoring;

public class MakeMethodNonStatic extends MethodRefactoring 
{	
	public MakeMethodNonStatic(CrossReferenceServiceConfiguration sc) 
	{
		super(sc);
	}
	
	public MakeMethodNonStatic() 
	{
		super();
	}
	
	public ProblemReport analyze(int iteration, int unit, int element) 
	{
		// Initialise and pick the element to visit.
		super.tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));
		
		for (int i = 0; i < element; i++)
			super.tw.next(MethodDeclaration.class);
	
		MethodDeclaration md = (MethodDeclaration) super.tw.getProgramElement();
		int counter = -1;
		
		// Prevents "Zero Service" outputs logged to the console.
		if (md.getMemberParent().getProgramModelInfo() == null)
			md.getFactory().getServiceConfiguration().getChangeHistory().updateModel();

		// Find iterator in declaration list.
		for (int i = 0; i < md.getDeclarationSpecifiers().size(); i++)
			if (md.getDeclarationSpecifiers().get(i).toString().contains("Static"))
				counter = i;

		// Construct refactoring transformation.
		// The transformation is handled here manually and the transformation
		// method will do nothing for this refactoring when it is called.
		super.transformation = null;
		getChangeHistory().begin(this);
		detach(md.getDeclarationSpecifiers().get(counter));

		// Specify refactoring information for results information.
		String unitName = getSourceFileRepository().getKnownCompilationUnits().get(unit).getName();
		String packageName = MiscKit.getParentTypeDeclaration(md).getPackage().getFullName();
		String className = MiscKit.getParentTypeDeclaration(md).getFullName().substring(packageName.length() + 1).replace('.', '\\');
		super.refactoringInfo = "Iteration " + iteration + ": \"Make Method Non Static\" applied at class " + className + " to method " + super.getMethodName(md);
		
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
		super.transformation = new Modify(config, true, md, AccessFlags.STATIC);
		report = super.transformation.analyze();
		if (report instanceof Problem) 
			return setProblemReport(report);
		
		return setProblemReport(EQUIVALENCE);
	}

	protected boolean mayRefactor(MethodDeclaration md)
	{				
		// Prevents "Zero Service" outputs logged to the console.
		if (md.getMemberParent().getProgramModelInfo() == null)
			md.getFactory().getServiceConfiguration().getChangeHistory().updateModel();

		if (!(md.isStatic()) || (MethodKit.isMain(getNameInfo(), md)))
			return false;
		else
		{
			for (MemberReference mr : getCrossReferenceSourceInfo().getReferences(md))
			{
				if (((MethodReference) mr).getReferencePrefix() instanceof TypeReference)
					return false;
				
				for (Import i : UnitKit.getCompilationUnit(mr).getImports())
					if ((i.isStaticImport()) && (i.toString().contains(MiscKit.getParentTypeDeclaration(md).getName())))
						return false;
			}
			
			return true;
		}	
	}
}