package refactorings.method;

import multirefactor.AccessFlags;
import recoder.CrossReferenceServiceConfiguration;
import recoder.convenience.AbstractTreeWalker;
import recoder.convenience.TreeWalker;
import recoder.java.ProgramElement;
import recoder.java.declaration.ConstructorDeclaration;
import recoder.java.declaration.MethodDeclaration;
import recoder.kit.MethodKit;
import recoder.kit.MiscKit;
import recoder.kit.Problem;
import recoder.kit.ProblemReport;
import recoder.kit.transformation.Modify;
import refactorings.Refactoring;

public class MakeMethodFinal extends Refactoring 
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
		{
			super.tw.next(MethodDeclaration.class);
			MethodDeclaration md = (MethodDeclaration) super.tw.getProgramElement();
			if (!mayRefactor(md))
				i--;
		}
		
		ProgramElement pe = super.tw.getProgramElement();
		MethodDeclaration md = (MethodDeclaration) pe;

		// Construct refactoring transformation.
		super.transformation = new Modify(config, true, md, AccessFlags.FINAL);
		report = super.transformation.analyze();
		if (report instanceof Problem) 
			return setProblemReport(report);

		// Specify refactoring information for results information.
		super.refactoringInfo = "Iteration " + iteration + ": \"Make Method Final\" applied at class " 
				+ super.getFileName(getSourceFileRepository().getKnownCompilationUnits().get(unit).getName())
				+ " to method " + ((MethodDeclaration) pe).getName();

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
	
	public boolean mayRefactor(MethodDeclaration md)
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
}