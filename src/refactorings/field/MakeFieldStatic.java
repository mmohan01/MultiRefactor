package refactorings.field;

import java.util.ArrayList;

import multirefactor.AccessFlags;
import recoder.CrossReferenceServiceConfiguration;
import recoder.convenience.AbstractTreeWalker;
import recoder.convenience.TreeWalker;
import recoder.java.ProgramElement;
import recoder.java.declaration.FieldDeclaration;
import recoder.kit.Problem;
import recoder.kit.ProblemReport;
import recoder.kit.transformation.Modify;
import refactorings.Refactoring;

public class MakeFieldStatic extends Refactoring 
{	
	public MakeFieldStatic(CrossReferenceServiceConfiguration sc) 
	{
		
		super(sc);
	}
	
	public MakeFieldStatic() 
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
			super.tw.next(FieldDeclaration.class);
			FieldDeclaration fd = (FieldDeclaration) super.tw.getProgramElement();
			if (!mayRefactor(fd))
				i--;
		}
			
		ProgramElement pe = super.tw.getProgramElement();
		FieldDeclaration fd = (FieldDeclaration) pe;
		int last = pe.toString().lastIndexOf(">");
		
		// Construct refactoring transformation.
		super.transformation = new Modify(config, true, fd, AccessFlags.STATIC);
		report = super.transformation.analyze();
		if (report instanceof Problem) 
			return setProblemReport(report);

		// Specify refactoring information for results information.
		super.refactoringInfo = "Iteration " + iteration + ": \"Make Field Static\" applied at class " 
				+ super.getFileName(getSourceFileRepository().getKnownCompilationUnits().get(unit).getName())
				+ " to field " + pe.toString().substring(last + 2);
		
		// Stores list of names of classes affected by refactoring.
		super.affectedClasses = new ArrayList<String>(1);
		super.affectedClasses.add(super.getFileName(getSourceFileRepository().getKnownCompilationUnits().get(unit).getName()));
		
		return setProblemReport(EQUIVALENCE);
	}
	
	public ProblemReport analyzeReverse() 
	{
		// Initialise and pick the element to visit.
		FieldDeclaration fd = (FieldDeclaration) super.tw.getProgramElement();
		
		// Find iterator in declaration list.
		int counter = -1;
		for (int i = 0; i < fd.getDeclarationSpecifiers().size(); i++)
			if (fd.getDeclarationSpecifiers().get(i).toString().contains("Static"))
				counter = i;

		// Construct refactoring transformation.
		super.transformation = null;
		detach(fd.getDeclarationSpecifiers().get(counter));
		return setProblemReport(EQUIVALENCE);
	}

	public boolean mayRefactor(FieldDeclaration fd)
	{
		if (fd.isStatic())
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
		while (tw.next(FieldDeclaration.class))
		{
			FieldDeclaration fd = (FieldDeclaration) tw.getProgramElement();
			if (mayRefactor(fd))
				counter++;
		}

		return counter;
	}
	
	public String getName(int unit, int element)
	{		
		AbstractTreeWalker tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));

		for (int i = 0; i < element; i++)
		{
			tw.next(FieldDeclaration.class);
			FieldDeclaration fd = (FieldDeclaration) tw.getProgramElement();
			if (!mayRefactor(fd))
				i--;
		}

		FieldDeclaration fd = (FieldDeclaration) tw.getProgramElement();
		return fd.toString();
	}
}