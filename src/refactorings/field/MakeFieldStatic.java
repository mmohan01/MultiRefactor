package refactorings.field;

import java.util.ArrayList;

import multirefactor.AccessFlags;
import recoder.CrossReferenceServiceConfiguration;
import recoder.convenience.TreeWalker;
import recoder.java.declaration.FieldDeclaration;
import recoder.kit.Problem;
import recoder.kit.ProblemReport;
import recoder.kit.transformation.Modify;
import refactorings.FieldRefactoring;

public class MakeFieldStatic extends FieldRefactoring 
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
			super.tw.next(FieldDeclaration.class);
			
		FieldDeclaration fd = (FieldDeclaration) super.tw.getProgramElement();
		int last = fd.toString().lastIndexOf(">");
		
		// Construct refactoring transformation.
		super.transformation = new Modify(config, true, fd, AccessFlags.STATIC);
		report = super.transformation.analyze();
		if (report instanceof Problem) 
			return setProblemReport(report);

		// Specify refactoring information for results information.
		super.refactoringInfo = "Iteration " + iteration + ": \"Make Field Static\" applied at class " 
				+ super.getFileName(getSourceFileRepository().getKnownCompilationUnits().get(unit).getName())
				+ " to field " + fd.toString().substring(last + 2);
		
		// Stores list of names of classes affected by refactoring.
		super.affectedClasses = new ArrayList<String>(1);
		super.affectedClasses.add(super.getFileName(getSourceFileRepository().getKnownCompilationUnits().get(unit).getName()));
		super.affectedElement = fd.toString().substring(last + 2);
		
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

	protected boolean mayRefactor(FieldDeclaration fd)
	{
		if (fd.isStatic())
			return false;
		else
			return true;	
	}
}