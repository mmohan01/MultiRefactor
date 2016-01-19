package refactorings.type;

import recoder.CrossReferenceServiceConfiguration;
import recoder.convenience.AbstractTreeWalker;
import recoder.convenience.TreeWalker;
import recoder.java.ProgramElement;
import recoder.java.declaration.TypeDeclaration;
import recoder.java.declaration.TypeParameterDeclaration;
import recoder.java.declaration.modifier.Private;
import recoder.kit.Problem;
import recoder.kit.ProblemReport;
import recoder.kit.transformation.Modify;
import refactorings.Refactoring;

public class DecreaseClassSecurity extends Refactoring 
{
	public DecreaseClassSecurity(CrossReferenceServiceConfiguration sc) 
	{
		super(sc);
	}
	
	public DecreaseClassSecurity() 
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
			super.tw.next(TypeDeclaration.class);
			TypeDeclaration td = (TypeDeclaration) super.tw.getProgramElement();
			if (!mayRefactor(td))
				i--;
		}
			
		ProgramElement pe = super.tw.getProgramElement();
		TypeDeclaration td = (TypeDeclaration) pe;
		
		// Construct refactoring transformation.
		super.transformation = new Modify(config, true, td, super.visibilityDown(td.getVisibilityModifier()));
		report = super.transformation.analyze();
		if (report instanceof Problem) 
			return setProblemReport(report);

		// Specify refactoring information for results information.
		super.refactoringInfo = "Iteration " + iteration + ": \"Decrease Class Security\" applied at class " 
				+ super.getFileName(getSourceFileRepository().getKnownCompilationUnits().get(unit).getName())
				+ " to element " + pe.getClass().getSimpleName() + " (" + ((TypeDeclaration) pe).getName()
				+ ") from " + super.currentModifier(td.getVisibilityModifier()) + " to " 
				+ super.refactoredDownModifier(td.getVisibilityModifier());

		return setProblemReport(EQUIVALENCE);
	}
	
	public ProblemReport analyzeReverse() 
	{
		// Initialise and pick the element to visit.
		CrossReferenceServiceConfiguration config = getServiceConfiguration();
		ProblemReport report = EQUIVALENCE;	
		TypeDeclaration td = (TypeDeclaration) super.tw.getProgramElement();

		// Construct refactoring transformation.
		super.transformation = new Modify(config, true, td, super.visibilityUp(td.getVisibilityModifier()));
		report = super.transformation.analyze();
		if (report instanceof Problem) 
			return setProblemReport(report);
		
		return setProblemReport(EQUIVALENCE);
	}

	public boolean mayRefactor(TypeDeclaration td)
	{		
		if ((td.getVisibilityModifier() instanceof Private) || (td instanceof TypeParameterDeclaration))
			return false;
		else
			return true;	
	}
	
	// Count the amount of available elements in the chosen class for refactoring.
	// If an element is not applicable for the current refactoring it is not counted.
	public int getAmount(int unit)
	{
		int counter = 0;
		AbstractTreeWalker tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));

		// Only counts the relevant program element.
		while (tw.next(TypeDeclaration.class))
		{
			TypeDeclaration td = (TypeDeclaration) tw.getProgramElement();
			if (mayRefactor(td))
				counter++;
		}

		return counter;
	}
	
	public int getID(int unit, int element)
	{		
		super.tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));

		for (int i = 0; i < element; i++)
		{
			super.tw.next(TypeDeclaration.class);
			TypeDeclaration td = (TypeDeclaration) super.tw.getProgramElement();
			if (!mayRefactor(td))
				i--;
		}
		
		return super.tw.getProgramElement().getID();
	}
}