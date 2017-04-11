package refactorings.field;

import java.util.ArrayList;

import recoder.CrossReferenceServiceConfiguration;
import recoder.convenience.TreeWalker;
import recoder.java.declaration.FieldDeclaration;
import recoder.java.declaration.modifier.Public;
import recoder.kit.MiscKit;
import recoder.kit.Problem;
import recoder.kit.ProblemReport;
import recoder.kit.transformation.Modify;
import refactorings.FieldRefactoring;

public class DecreaseFieldVisibility extends FieldRefactoring
{	
	public DecreaseFieldVisibility(CrossReferenceServiceConfiguration sc) 
	{
		super(sc);
	}
	
	public DecreaseFieldVisibility() 
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
	
		// Prevents "Zero Service" outputs logged to the console.
		if (fd.getMemberParent().getProgramModelInfo() == null)
			fd.getFactory().getServiceConfiguration().getChangeHistory().updateModel();
		
		// Construct refactoring transformation.
		super.transformation = new Modify(config, true, fd, super.visibilityUp(fd.getVisibilityModifier()));
		report = super.transformation.analyze();
		if (report instanceof Problem) 
			return setProblemReport(report);
		
		// Specify refactoring information for results information.
		String unitName = getSourceFileRepository().getKnownCompilationUnits().get(unit).getName();
		String packageName = MiscKit.getParentTypeDeclaration(fd).getPackage().getFullName();
		String className = MiscKit.getParentTypeDeclaration(fd).getFullName().substring(packageName.length() + 1).replace('.', '\\');
		super.refactoringInfo = "Iteration " + iteration + ": \"Decrease Field Visibility\" applied at class " 
				+ className + " to field " + fd.toString().substring(last + 2) 
				+ " from " + super.currentModifier(fd.getVisibilityModifier()) 
				+ " to " + super.refactoredUpModifier(fd.getVisibilityModifier());
		
		// Stores list of names of classes affected by refactoring.
		super.affectedClasses = new ArrayList<String>(1);
		super.affectedClasses.add(super.getFileName(unitName, className));
		super.affectedElement = "::" + fd.toString();

		return setProblemReport(EQUIVALENCE);
	}
	
	public ProblemReport analyzeReverse() 
	{
		// Initialise and pick the element to visit.
		CrossReferenceServiceConfiguration config = getServiceConfiguration();
		ProblemReport report = EQUIVALENCE;	
		FieldDeclaration fd = (FieldDeclaration) super.tw.getProgramElement();

		// Construct refactoring transformation.
		super.transformation = new Modify(config, true, fd, super.visibilityDown(fd.getVisibilityModifier()));
		report = super.transformation.analyze();
		if (report instanceof Problem) 
			return setProblemReport(report);
		
		return setProblemReport(EQUIVALENCE);
	}
	
	protected boolean mayRefactor(FieldDeclaration fd)
	{		
		if (fd.getVisibilityModifier() instanceof Public)
			return false;
		else
			return true;	
	}
}