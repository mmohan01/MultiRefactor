package refactorings.method;

import recoder.CrossReferenceServiceConfiguration;
import recoder.convenience.AbstractTreeWalker;
import recoder.convenience.TreeWalker;
import recoder.java.ProgramElement;
import recoder.java.declaration.MethodDeclaration;
import recoder.kit.Problem;
import recoder.kit.ProblemReport;
import recoder.kit.transformation.Modify;
import refactorings.Refactoring;
import refactory.AccessFlags;

public class MakeMethodNonFinal extends Refactoring 
{
	public MakeMethodNonFinal(CrossReferenceServiceConfiguration sc) 
	{
		super(sc);
	}
	
	public MakeMethodNonFinal() 
	{
		super();
	}
	
	public ProblemReport analyze(int iteration, int unit, int element) 
	{
		// Initialise and pick the element to visit.
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

		// Find iterator in declaration list.
		int counter = -1;
		for (int i = 0; i < md.getDeclarationSpecifiers().size(); i++)
			if (md.getDeclarationSpecifiers().get(i).toString().contains("Final"))
				counter = i;

		// Construct refactoring transformation.
		// The transformation is handled here manually and the transformation
		// method will do nothing for this refactoring when it is called.
		super.transformation = null;
		getChangeHistory().begin(this);
		detach(md.getDeclarationSpecifiers().get(counter));

		// Specify refactoring information for results information.
		super.refactoringInfo = "Iteration " + iteration + ": \"Make Method Non Final\" applied at class " 
				+ super.getFileName(getSourceFileRepository().getKnownCompilationUnits().get(unit).getName())
				+ " to element " + pe.getClass().getSimpleName() + " (" + ((MethodDeclaration) pe).getName()
				+ ")";

		return setProblemReport(EQUIVALENCE);
	}
	
	public ProblemReport analyzeReverse() 
	{
		// Initialise and pick the element to visit.
		CrossReferenceServiceConfiguration config = getServiceConfiguration();
		ProblemReport report = EQUIVALENCE;	
		MethodDeclaration md = (MethodDeclaration) super.tw.getProgramElement();

		// Construct refactoring transformation.
		super.transformation = new Modify(config, true, md, AccessFlags.FINAL);
		report = super.transformation.analyze();
		if (report instanceof Problem) 
			return setProblemReport(report);
		
		return setProblemReport(EQUIVALENCE);
	}
	
	public boolean mayRefactor(MethodDeclaration md)
	{
		if (md.isFinal())
			return true;
		else
			return false;	
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
			//counter++;
			MethodDeclaration md = (MethodDeclaration) tw.getProgramElement();
			if (mayRefactor(md))
				counter++;
		}

		return counter;
	}
	
	public int getID(int unit, int element)
	{		
		super.tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));

		for (int i = 0; i < element; i++)
		{
			super.tw.next(MethodDeclaration.class);
			MethodDeclaration md = (MethodDeclaration) super.tw.getProgramElement();
			if (!mayRefactor(md))
				i--;
		}

		return super.tw.getProgramElement().getID();
	}
}