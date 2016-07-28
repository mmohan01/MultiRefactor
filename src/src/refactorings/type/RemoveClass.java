package refactorings.type;

import recoder.CrossReferenceServiceConfiguration;
import recoder.convenience.AbstractTreeWalker;
import recoder.convenience.TreeWalker;
import recoder.java.CompilationUnit;
import recoder.java.ProgramElement;
import recoder.java.declaration.MemberDeclaration;
import recoder.java.declaration.TypeDeclaration;
import recoder.kit.ProblemReport;
import recoder.list.generic.ASTList;
import refactorings.Refactoring;

public class RemoveClass extends Refactoring 
{
	TypeDeclaration type, containingType;
	CompilationUnit unit;
	int position, unitPosition;
	boolean detachUnit, isNested;
	
	public RemoveClass(CrossReferenceServiceConfiguration sc) 
	{
		super(sc);
	}
	
	public RemoveClass() 
	{
		super();
	}
	
	public ProblemReport analyze(int iteration, int unit, int element) 
	{		
		// Initialise and pick the element to visit.
		super.tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));
		
		for (int i = 0; i < element; i++)
		{
			super.tw.next(TypeDeclaration.class);
			TypeDeclaration td = (TypeDeclaration) super.tw.getProgramElement();
			if (!mayRefactor(td))
				i--;
		}
		
		ProgramElement pe = super.tw.getProgramElement();
		this.type = (TypeDeclaration) pe;
		this.unit = getSourceFileRepository().getKnownCompilationUnits().get(unit);
		this.unitPosition = unit;
		
		if (this.type.getContainingClassType() == null)
		{
			this.position = super.getPosition(this.unit, this.type);
			this.isNested = false;
		}
		else
		{
			this.position = super.getPosition(this.type.getContainingClassType(), this.type);
			this.isNested = true;
			this.containingType = this.type.getContainingClassType();
		}
		
		if ((this.type.getContainingClassType() != null) || (this.unit.getTypeDeclarationCount() > 1))
			this.detachUnit = false;
		else
			this.detachUnit = true;
		
		// Construct refactoring transformation.
		// The transformation is handled here manually and the transformation
		// method will do nothing for this refactoring when it is called.
		super.transformation = null;
		getChangeHistory().begin(this);
		
		if (this.detachUnit)
			detach(this.unit);
		else
			detach(this.type);
		
		// Specify refactoring information for results information.
		super.refactoringInfo = "Iteration " + iteration + ": \"Remove Class\" applied to class " + ((TypeDeclaration) pe).getName();
		getChangeHistory().updateModel();
		return setProblemReport(EQUIVALENCE);
	}

	public ProblemReport analyzeReverse() 
	{		
		// Construct refactoring transformation.	
		super.transformation = null;
		
		if (this.detachUnit)
			attach(this.unit);
		else
			if (this.isNested)
				attach(this.type, this.containingType, this.position);
			else
				attach(this.type, this.unit, this.position);
		
		getChangeHistory().updateModel();
		return setProblemReport(EQUIVALENCE);
	}

	public boolean mayRefactor(TypeDeclaration td)
	{		
		if (!(td.isOrdinaryClass()) || (getCrossReferenceSourceInfo().getSubtypes(td).size() > 0) || (td.getName() == null) ||
			(getCrossReferenceSourceInfo().getReferences(td, true).size() > 0))
			return false;
		else
		{			
			ASTList<MemberDeclaration> members = td.getMembers();
			members.removeAll(td.getConstructors());
			
			if (members.size() > 0)
				return false;
			
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
		while (tw.next(TypeDeclaration.class))
		{
			TypeDeclaration td = (TypeDeclaration) tw.getProgramElement();
			if (mayRefactor(td))
				counter++;
		}

		return counter;
	}
	
	public String getName(int unit, int element)
	{
		AbstractTreeWalker tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));

		for (int i = 0; i < element; i++)
		{
			tw.next(TypeDeclaration.class);
			TypeDeclaration td = (TypeDeclaration) tw.getProgramElement();
			if (!mayRefactor(td))
				i--;
		}
		
		TypeDeclaration td = (TypeDeclaration) tw.getProgramElement();
		return td.getName();
	}
}