package refactorings.type;

import java.util.ArrayList;

import multirefactor.AccessFlags;
import recoder.CrossReferenceServiceConfiguration;
import recoder.convenience.TreeWalker;
import recoder.java.declaration.TypeDeclaration;
import recoder.kit.Problem;
import recoder.kit.ProblemReport;
import recoder.kit.UnitKit;
import recoder.kit.transformation.Modify;
import refactorings.TypeRefactoring;

public class MakeClassNonFinal extends TypeRefactoring 
{
	public MakeClassNonFinal(CrossReferenceServiceConfiguration sc) 
	{
		super(sc);
	}
	
	public MakeClassNonFinal() 
	{
		super();
	}
	
	public ProblemReport analyze(int iteration, int unit, int element) 
	{
		// Initialise and pick the element to visit.
		super.tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));
		
		for (int i = 0; i < element; i++)
			super.tw.next(TypeDeclaration.class);
		
		TypeDeclaration td = (TypeDeclaration) super.tw.getProgramElement();
		int counter = -1;
		
		// Prevents "Zero Service" outputs logged to the console.
		if (td.getProgramModelInfo() == null)
			td.getFactory().getServiceConfiguration().getChangeHistory().updateModel();

		// Find iterator in declaration list.		
		for (int i = 0; i < td.getDeclarationSpecifiers().size(); i++)
			if (td.getDeclarationSpecifiers().get(i).toString().contains("Final"))
				counter = i;
		
		// Construct refactoring transformation.
		// The transformation is handled here manually and the transformation
		// method will do nothing for this refactoring when it is called.
		super.transformation = null;
		getChangeHistory().begin(this);
		detach(td.getDeclarationSpecifiers().get(counter));

		// Specify refactoring information for results information.
		super.refactoringInfo = "Iteration " + iteration + ": \"Make Class Non Final\" applied to class " 
		        + super.getClassName(UnitKit.getCompilationUnit(td).getName(), td.getFullName());
		
		// Stores list of names of classes affected by refactoring.
		String fileName = super.getFileName(UnitKit.getCompilationUnit(td).getName(), td.getFullName());
		super.affectedClasses = new ArrayList<String>(1);
		super.affectedClasses.add(fileName);
		super.affectedElement = fileName;

		return setProblemReport(EQUIVALENCE);
	}
	
	public ProblemReport analyzeReverse() 
	{
		// Initialise and pick the element to visit.
		CrossReferenceServiceConfiguration config = getServiceConfiguration();
		ProblemReport report = EQUIVALENCE;	
		TypeDeclaration td = (TypeDeclaration) super.tw.getProgramElement();

		// Construct refactoring transformation.
		super.transformation = new Modify(config, true, td, AccessFlags.FINAL);
		report = super.transformation.analyze();
		if (report instanceof Problem) 
			return setProblemReport(report);
		
		return setProblemReport(EQUIVALENCE);
	}

	protected boolean mayRefactor(TypeDeclaration td)
	{
		if ((td.getName() == null) || (!td.isFinal()) || !(td.isOrdinaryClass()))
			return false;
		else
			return true;	
	}
}