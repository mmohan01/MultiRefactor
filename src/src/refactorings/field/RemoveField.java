package refactorings.field;

import recoder.CrossReferenceServiceConfiguration;
import recoder.convenience.AbstractTreeWalker;
import recoder.convenience.TreeWalker;
import recoder.java.ProgramElement;
import recoder.java.declaration.FieldDeclaration;
import recoder.java.declaration.TypeDeclaration;
import recoder.java.declaration.TypeParameterDeclaration;
import recoder.kit.MiscKit;
import recoder.kit.ProblemReport;
import refactorings.Refactoring;

public class RemoveField extends Refactoring 
{
	private TypeDeclaration type;
	private FieldDeclaration field;
	private int position;
	
	public RemoveField(CrossReferenceServiceConfiguration sc) 
	{
		super(sc);
	}
	
	public RemoveField() 
	{
		super();
	}
	
	public ProblemReport analyze(int iteration, int unit, int element) 
	{
		// Initialise and pick the element to visit.
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
		this.field = fd;
		this.type = MiscKit.getParentTypeDeclaration(fd);
		this.position = super.getPosition(this.type, fd);
		
		// Construct refactoring transformation.
		// The transformation is handled here manually and the transformation
		// method will do nothing for this refactoring when it is called.
		super.transformation = null;
		getChangeHistory().begin(this);
		detach(fd);

		// Specify refactoring information for results information.
		super.refactoringInfo = "Iteration " + iteration + ": \"Remove Field\" applied at class "
				+ super.getFileName(getSourceFileRepository().getKnownCompilationUnits().get(unit).getName())
				+ " to field " + pe.toString().substring(last + 2);
		
		return setProblemReport(EQUIVALENCE);
	}

	public ProblemReport analyzeReverse() 
	{
		// Construct refactoring transformation.
		super.transformation = null;
		attach(this.field, this.type, this.position);
		return setProblemReport(EQUIVALENCE);
	}
	
	public boolean mayRefactor(FieldDeclaration fd)
	{			
		TypeDeclaration td =  MiscKit.getParentTypeDeclaration(fd);
		 
		if ((td.isEnumType()) || (td instanceof TypeParameterDeclaration) || 
			(getCrossReferenceSourceInfo().getReferences(fd.getVariables().get(0)).size() > 0))
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