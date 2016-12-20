package refactorings.type;

import java.util.ArrayList;

import recoder.CrossReferenceServiceConfiguration;
import recoder.convenience.AbstractTreeWalker;
import recoder.convenience.TreeWalker;
import recoder.java.CompilationUnit;
import recoder.java.ProgramElement;
import recoder.java.declaration.ConstructorDeclaration;
import recoder.java.declaration.InterfaceDeclaration;
import recoder.java.declaration.MemberDeclaration;
import recoder.kit.ProblemReport;
import refactorings.Refactoring;

public class RemoveInterface extends Refactoring 
{
	InterfaceDeclaration type, containingType;
	CompilationUnit unit;
	int position, unitPosition;
	boolean detachUnit, isNested;
	
	public RemoveInterface(CrossReferenceServiceConfiguration sc) 
	{
		super(sc);
	}
	
	public RemoveInterface() 
	{
		super();
	}
	
	public ProblemReport analyze(int iteration, int unit, int element) 
	{
		// Initialise and pick the element to visit.
		super.tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));
		
		for (int i = 0; i < element; i++)
		{
			super.tw.next(InterfaceDeclaration.class);
			InterfaceDeclaration id = (InterfaceDeclaration) super.tw.getProgramElement();
			if (!mayRefactor(id))
				i--;
		}
		
		ProgramElement pe = super.tw.getProgramElement();
		this.type = (InterfaceDeclaration) pe;
		this.unit = getSourceFileRepository().getKnownCompilationUnits().get(unit);

		if (this.type.getContainingClassType() == null)
		{
			this.position = super.getPosition(this.unit, this.type);
			this.isNested = false;
		}
		else
		{
			this.position = super.getPosition(this.type.getContainingClassType(), this.type);
			this.isNested = true;
			this.containingType = (InterfaceDeclaration) this.type.getContainingClassType();
		}
		
		if ((this.type.getContainingClassType() != null) || (this.unit.getTypeDeclarationCount() > 1))
		{
			this.detachUnit = true;
			this.unitPosition = unit;
		}
		else
			this.detachUnit = false;
		
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
		super.refactoringInfo = "Iteration " + iteration + ": \"Remove Interface\" applied to interface " + ((InterfaceDeclaration) pe).getName();
		
		// Stores list of names of classes affected by refactoring.
		super.affectedClasses = new ArrayList<String>(1);
		super.affectedClasses.add(((InterfaceDeclaration) pe).getName());

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

	public boolean mayRefactor(InterfaceDeclaration id)
	{		
		if ((getCrossReferenceSourceInfo().getSubtypes(id).size() > 0) || (id.getName() == null) ||
			(getCrossReferenceSourceInfo().getReferences(id, true).size() > 0))
			return false;
		else
		{			
			int members = id.getMembers().size();
			
			for (MemberDeclaration dec : id.getMembers())
				if (dec instanceof ConstructorDeclaration)
					members--;
			
			if (members > 0)
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
		while (tw.next(InterfaceDeclaration.class))
		{
			InterfaceDeclaration id = (InterfaceDeclaration) tw.getProgramElement();
			if (mayRefactor(id))
				counter++;
		}

		return counter;
	}
	
	public String getName(int unit, int element)
	{
		AbstractTreeWalker tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));

		for (int i = 0; i < element; i++)
		{
			tw.next(InterfaceDeclaration.class);
			InterfaceDeclaration id = (InterfaceDeclaration) tw.getProgramElement();
			if (!mayRefactor(id))
				i--;
		}
		
		InterfaceDeclaration id = (InterfaceDeclaration) tw.getProgramElement();
		return id.getName();
	}
}