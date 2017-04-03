package refactorings.type;

import java.util.ArrayList;

import recoder.CrossReferenceServiceConfiguration;
import recoder.convenience.TreeWalker;
import recoder.java.CompilationUnit;
import recoder.java.declaration.ConstructorDeclaration;
import recoder.java.declaration.MemberDeclaration;
import recoder.java.declaration.TypeDeclaration;
import recoder.kit.ProblemReport;
import refactorings.TypeRefactoring;

public class RemoveClass extends TypeRefactoring 
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
		this.unit = getSourceFileRepository().getKnownCompilationUnits().get(unit);
		super.tw = new TreeWalker(this.unit);
		
		for (int i = 0; i < element; i++)
			super.tw.next(TypeDeclaration.class);
		
		this.type = (TypeDeclaration) super.tw.getProgramElement();
		String unitName = this.unit.getName();
		this.unitPosition = unit;
		
		// Prevents "Zero Service" outputs logged to the console.
		if (this.type.getProgramModelInfo() == null)
			this.type.getFactory().getServiceConfiguration().getChangeHistory().updateModel();
		
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
		super.refactoringInfo = "Iteration " + iteration + ": \"Remove Class\" applied to class " 
		        + super.getClassName(unitName, this.type.getFullName());
		
		// Stores list of names of classes affected by refactoring.
		String fileName = super.getFileName(unitName, this.type.getFullName());
		super.affectedClasses = new ArrayList<String>(1);
		super.affectedClasses.add(fileName);
		super.affectedElement = fileName;
				
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

	protected boolean mayRefactor(TypeDeclaration td)
	{		
		if (!(td.isOrdinaryClass()) || (getCrossReferenceSourceInfo().getSubtypes(td).size() > 0) || (td.getName() == null) ||
			(getCrossReferenceSourceInfo().getReferences(td, true).size() > 0))
			return false;
		else
		{			
			int members = td.getMembers().size();
			
			for (MemberDeclaration dec : td.getMembers())
				if (dec instanceof ConstructorDeclaration)
					members--;
			
			if (members > 0)
				return false;
			
			return true;	
		}
	}
}