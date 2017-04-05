package refactorings.field;

import java.util.ArrayList;

import recoder.CrossReferenceServiceConfiguration;
import recoder.convenience.TreeWalker;
import recoder.java.declaration.FieldDeclaration;
import recoder.java.declaration.TypeDeclaration;
import recoder.java.declaration.TypeParameterDeclaration;
import recoder.kit.MiscKit;
import recoder.kit.ProblemReport;
import refactorings.FieldRefactoring;

public class RemoveField extends FieldRefactoring 
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
			super.tw.next(FieldDeclaration.class);
		
		this.field = (FieldDeclaration) super.tw.getProgramElement();
		int last = this.field.toString().lastIndexOf(">");
		this.type = MiscKit.getParentTypeDeclaration(this.field);
		this.position = super.getPosition(this.type, this.field);
		
		// Prevents "Zero Service" outputs logged to the console.
		if (this.type.getProgramModelInfo() == null)
			this.field.getFactory().getServiceConfiguration().getChangeHistory().updateModel();
		
		// Construct refactoring transformation.
		// The transformation is handled here manually and the transformation
		// method will do nothing for this refactoring when it is called.
		super.transformation = null;
		getChangeHistory().begin(this);
		detach(this.field);

		// Specify refactoring information for results information.
		String unitName = getSourceFileRepository().getKnownCompilationUnits().get(unit).getName();
		String packageName = this.type.getPackage().getFullName();
		String className = this.type.getFullName().substring(packageName.length() + 1).replace('.', '\\');
		super.refactoringInfo = "Iteration " + iteration + ": \"Remove Field\" applied at class " + className + " to field " + this.field.toString().substring(last + 2);
		
		// Stores list of names of classes affected by refactoring.
		super.affectedClasses = new ArrayList<String>(1);
		super.affectedClasses.add(super.getFileName(unitName, className));
		super.affectedElement = "::" + this.field.toString();
		
		return setProblemReport(EQUIVALENCE);
	}

	public ProblemReport analyzeReverse() 
	{
		// Construct refactoring transformation.
		super.transformation = null;
		attach(this.field, this.type, this.position);
		return setProblemReport(EQUIVALENCE);
	}
	
	protected boolean mayRefactor(FieldDeclaration fd)
	{			
		TypeDeclaration td =  MiscKit.getParentTypeDeclaration(fd);
		 
		if ((td.isEnumType()) || (td instanceof TypeParameterDeclaration))
			return false;
		else
		{
			for (int i = 0; i < fd.getVariables().size(); i++)			
				if (getCrossReferenceSourceInfo().getReferences(fd.getVariables().get(i)).size() > 0)
					return false;	
			
			return true;
		}
	}
}