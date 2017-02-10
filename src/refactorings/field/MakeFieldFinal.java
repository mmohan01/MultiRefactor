package refactorings.field;

import java.util.ArrayList;

import multirefactor.AccessFlags;
import recoder.CrossReferenceServiceConfiguration;
import recoder.convenience.TreeWalker;
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
			super.tw.next(VariableDeclaration.class);
		
		VariableDeclaration vd = (VariableDeclaration) super.tw.getProgramElement();
		int last = vd.toString().lastIndexOf(">");
		
		// Construct refactoring transformation.
		super.transformation = new Modify(config, true, vd, AccessFlags.FINAL);
		report = super.transformation.analyze();
		if (report instanceof Problem)
			return setProblemReport(report);

		// Specify refactoring information for results information.
		super.refactoringInfo = "Iteration " + iteration + ": \"Make Field Final\" applied at class " 
				+ super.getFileName(getSourceFileRepository().getKnownCompilationUnits().get(unit).getName())
				+ " to " + vd.getClass().getSimpleName() + " " + vd.toString().substring(last + 2);
		
		// Stores list of names of classes affected by refactoring.
		super.affectedClasses = new ArrayList<String>(1);
		super.affectedClasses.add(super.getFileName(getSourceFileRepository().getKnownCompilationUnits().get(unit).getName()));
		super.affectedElement = vd.toString().substring(last + 2);

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

	private boolean mayRefactor(VariableDeclaration vd)
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
	
	public int getAmount(int unit)
	{
		int counter = 0;
		TreeWalker tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));
		
		// Only counts the relevant program element.
		while (tw.next(VariableDeclaration.class))
		{
			VariableDeclaration vd = (VariableDeclaration) tw.getProgramElement();
			if (mayRefactor(vd))
				counter++;
		}

		return counter;
	}
	
	public int getAbsolutePosition(int unit, int element)
	{		
		TreeWalker tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));
		int absolutePosition = 0;

		for (int i = 0; i < element; i++)
		{
			tw.next(VariableDeclaration.class);
			VariableDeclaration fd = (VariableDeclaration) tw.getProgramElement();
			if (!mayRefactor(fd))
				i--;
			
			absolutePosition++;
		}

		return absolutePosition;
	}
	
	public String getName(int unit, int element)
	{
		TreeWalker tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));

		for (int i = 0; i < element; i++)
			tw.next(VariableDeclaration.class);
			
		VariableDeclaration vd = (VariableDeclaration) tw.getProgramElement();
		return vd.toString();
	}
	
	public int checkElements(int unit, String name)
	{		
		TreeWalker tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));
		int element = 0;

		while (tw.next(VariableDeclaration.class))
		{
			element++;
			VariableDeclaration vd = (VariableDeclaration) tw.getProgramElement();
			
			if ((vd.toString() != null) && (vd.toString().equals(name)))
				return (mayRefactor(vd)) ? element : -1;
		}
		
		return -1;
	}
}