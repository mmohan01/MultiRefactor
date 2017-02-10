package refactorings.type;

import java.util.ArrayList;

import multirefactor.AccessFlags;
import recoder.CrossReferenceServiceConfiguration;
import recoder.convenience.TreeWalker;
import recoder.java.declaration.TypeDeclaration;
import recoder.java.expression.operator.New;
import recoder.java.reference.TypeReference;
import recoder.kit.Problem;
import recoder.kit.ProblemReport;
import recoder.kit.transformation.Modify;
import refactorings.TypeRefactoring;

public class MakeClassAbstract extends TypeRefactoring 
{
	public MakeClassAbstract(CrossReferenceServiceConfiguration sc) 
	{
		super(sc);
	}
	
	public MakeClassAbstract() 
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
			super.tw.next(TypeDeclaration.class);
		
		TypeDeclaration td = (TypeDeclaration) super.tw.getProgramElement();

		// Construct refactoring transformation.
		super.transformation = new Modify(config, true, td, AccessFlags.ABSTRACT);
		report = super.transformation.analyze();
		if (report instanceof Problem) 
			return setProblemReport(report);
		
		// Specify refactoring information for results information.
		super.refactoringInfo = "Iteration " + iteration + ": \"Make Class Abstract\" applied to class "  + td.getName();

		// Stores list of names of classes affected by refactoring.
		super.affectedClasses = new ArrayList<String>(1);
		super.affectedClasses.add(td.getName());
		super.affectedElement = td.getName();

		return setProblemReport(EQUIVALENCE);
	}

	public ProblemReport analyzeReverse() 
	{
		// Initialise and pick the element to visit.
		TypeDeclaration td = (TypeDeclaration) super.tw.getProgramElement();
		
		// Find iterator in declaration list.
		int counter = -1;
		for (int i = 0; i < td.getDeclarationSpecifiers().size(); i++)
			if (td.getDeclarationSpecifiers().get(i).toString().contains("Abstract"))
				counter = i;

		// Construct refactoring transformation.
		super.transformation = null;
		detach(td.getDeclarationSpecifiers().get(counter));
		return setProblemReport(EQUIVALENCE);
	}

	protected boolean mayRefactor(TypeDeclaration td)
	{
		if ((td.getName() == null) || (td.isAbstract()) || (td.isFinal()) || !(td.isOrdinaryClass()) || 
			(getCrossReferenceSourceInfo().getSubtypes(td).size() == 0))
			return false;
		else
		{
			for (TypeReference tr : getCrossReferenceSourceInfo().getReferences(td, true))
				if (tr.getParent() instanceof New)
						return false;

			return true;	
		}
	}
}