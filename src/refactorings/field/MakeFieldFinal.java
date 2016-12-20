package refactorings.field;

import java.util.ArrayList;

import multirefactor.AccessFlags;
import recoder.CrossReferenceServiceConfiguration;
import recoder.convenience.AbstractTreeWalker;
import recoder.convenience.TreeWalker;
import recoder.java.ProgramElement;
import recoder.java.declaration.EnumDeclaration;
import recoder.java.declaration.VariableDeclaration;
import recoder.java.reference.VariableReference;
import recoder.kit.MiscKit;
import recoder.kit.Problem;
import recoder.kit.ProblemReport;
import recoder.kit.transformation.Modify;
import refactorings.Refactoring;

public class MakeFieldFinal extends Refactoring 
{	
	public MakeFieldFinal(CrossReferenceServiceConfiguration sc) 
	{
		super(sc);
	}
	
	public MakeFieldFinal() 
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
			super.tw.next(VariableDeclaration.class);
			VariableDeclaration vd = (VariableDeclaration) super.tw.getProgramElement();
			if (!mayRefactor(vd))
				i--;
		}
		
		ProgramElement pe = super.tw.getProgramElement();
		VariableDeclaration vd = (VariableDeclaration) pe;
		int last = pe.toString().lastIndexOf(">");
		
		// Construct refactoring transformation.
		super.transformation = new Modify(config, true, vd, AccessFlags.FINAL);
		report = super.transformation.analyze();
		if (report instanceof Problem)
			return setProblemReport(report);

		// Specify refactoring information for results information.
		super.refactoringInfo = "Iteration " + iteration + ": \"Make Field Final\" applied at class " 
				+ super.getFileName(getSourceFileRepository().getKnownCompilationUnits().get(unit).getName())
				+ " to " + pe.getClass().getSimpleName() + " " + pe.toString().substring(last + 2);
		
		// Stores list of names of classes affected by refactoring.
		super.affectedClasses = new ArrayList<String>(1);
		super.affectedClasses.add(super.getFileName(getSourceFileRepository().getKnownCompilationUnits().get(unit).getName()));

		return setProblemReport(EQUIVALENCE);
	}
	
	public ProblemReport analyzeReverse() 
	{
		// Initialise and pick the element to visit.
		VariableDeclaration vd = (VariableDeclaration) super.tw.getProgramElement();

		// Find iterator in declaration list.
		int counter = -1;
		for (int i = 0; i < vd.getDeclarationSpecifiers().size(); i++)
			if (vd.getDeclarationSpecifiers().get(i).toString().contains("Final"))
				counter = i;
		
		// Construct refactoring transformation.
		super.transformation = null;
		detach(vd.getDeclarationSpecifiers().get(counter));
		return setProblemReport(EQUIVALENCE);
	}

	public boolean mayRefactor(VariableDeclaration vd)
	{				
		if ((vd.isFinal()) || (MiscKit.getParentTypeDeclaration(vd) instanceof EnumDeclaration))
			return false;
		else
		{			
			if (!(vd.toSource().contains("=")))
			{	
				int references = 0;

				for (VariableReference vr : getCrossReferenceSourceInfo().getReferences(vd.getVariables().get(0)))
				{
					if (vr.getASTParent().toSource().contains(vd.getVariables().get(0).getName() + " ="))
					{
						references++;
						
						if (references > 1)
							return false;
					}
				}
			}
			else
			{
				for (VariableReference vr : getCrossReferenceSourceInfo().getReferences(vd.getVariables().get(0)))
					if (vr.getASTParent().toSource().contains(vd.getVariables().get(0).getName() + " ="))
						return false;
			}

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
		while (tw.next(VariableDeclaration.class))
		{
			VariableDeclaration vd = (VariableDeclaration) tw.getProgramElement();
			if (mayRefactor(vd))
				counter++;
		}

		return counter;
	}
	
	public String getName(int unit, int element)
	{
		AbstractTreeWalker tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));

		for (int i = 0; i < element; i++)
		{
			tw.next(VariableDeclaration.class);
			VariableDeclaration vd = (VariableDeclaration) tw.getProgramElement();
			if (!mayRefactor(vd))
				i--;
		}

		VariableDeclaration vd = (VariableDeclaration) tw.getProgramElement();
		return vd.toString();
	}
}