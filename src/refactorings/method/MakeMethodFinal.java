package refactorings.method;

import java.util.ArrayList;

import multirefactor.AccessFlags;
import recoder.CrossReferenceServiceConfiguration;
import recoder.convenience.TreeWalker;
import recoder.java.declaration.ConstructorDeclaration;
import recoder.java.declaration.MethodDeclaration;
import recoder.kit.MethodKit;
import recoder.kit.MiscKit;
import recoder.kit.Problem;
import recoder.kit.ProblemReport;
import recoder.kit.transformation.Modify;
import refactorings.MethodRefactoring;

public class MakeMethodFinal extends MethodRefactoring 
{
	public MakeMethodFinal(CrossReferenceServiceConfiguration sc) 
	{
		super(sc);
	}
	
	public MakeMethodFinal() 
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
		super.transformation = new Modify(config, true, md, AccessFlags.FINAL);
		report = super.transformation.analyze();
		if (report instanceof Problem) 
			return setProblemReport(report);

		// Specify refactoring information for results information.
		String unitName = getSourceFileRepository().getKnownCompilationUnits().get(unit).getName();
		String typeName = MiscKit.getParentTypeDeclaration(md).getFullName();
		super.refactoringInfo = "Iteration " + iteration + ": \"Make Method Final\" applied at class " 
				+ super.getClassName(unitName, typeName) + " to method " + super.getMethodName(md);
		
		// Stores list of names of classes affected by refactoring.
		super.affectedClasses = new ArrayList<String>(1);
		super.affectedClasses.add(super.getFileName(unitName, typeName));
		super.affectedElement = ":" + super.getMethodName(md) + ":";

		return setProblemReport(EQUIVALENCE);
	}

	public ProblemReport analyzeReverse() 
	{
		// Initialise and pick the element to visit.
		MethodDeclaration md = (MethodDeclaration) super.tw.getProgramElement();
				
		// Find iterator in declaration list.
		int counter = -1;
		for (int i = 0; i < md.getDeclarationSpecifiers().size(); i++)
			if (md.getDeclarationSpecifiers().get(i).toString().contains("Final"))
				counter = i;

		
		// Construct refactoring transformation.
		super.transformation = null;
		detach(md.getDeclarationSpecifiers().get(counter));
		return setProblemReport(EQUIVALENCE);
	}
	
	protected boolean mayRefactor(MethodDeclaration md)
	{
		if ((md.isFinal()) || (md.isAbstract()) || (md instanceof ConstructorDeclaration) || (MiscKit.getParentTypeDeclaration(md).isInterface()))
			return false;
		else
		{
			// Prevents "Zero Service" outputs logged to the console.
			if (md.getMemberParent().getProgramModelInfo() == null)
				md.getFactory().getServiceConfiguration().getChangeHistory().updateModel();
			
			if (MethodKit.getRedefiningMethods(getCrossReferenceSourceInfo(), md).size() > 0)
				return false;
			else 
				return true;
		}
	}
}