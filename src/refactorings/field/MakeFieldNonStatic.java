package refactorings.field;

import java.util.ArrayList;

import multirefactor.AccessFlags;
import recoder.CrossReferenceServiceConfiguration;
import recoder.convenience.TreeWalker;
import recoder.java.Import;
import recoder.java.declaration.EnumConstantDeclaration;
import recoder.java.declaration.FieldDeclaration;
import recoder.java.declaration.FieldSpecification;
import recoder.java.declaration.InterfaceDeclaration;
import recoder.java.reference.FieldReference;
import recoder.java.reference.TypeReference;
import recoder.kit.MiscKit;
import recoder.kit.Problem;
import recoder.kit.ProblemReport;
import recoder.kit.UnitKit;
import recoder.kit.transformation.Modify;
import refactorings.FieldRefactoring;

public class MakeFieldNonStatic extends FieldRefactoring 
{	
	public MakeFieldNonStatic(CrossReferenceServiceConfiguration sc) 
	{
		super(sc);
	}
	
	public MakeFieldNonStatic() 
	{
		super();
	}
	
	public ProblemReport analyze(int iteration, int unit, int element) 
	{
		// Initialise and pick the element to visit.
		super.tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));
		
		for (int i = 0; i < element; i++)
			super.tw.next(FieldDeclaration.class);
			
		FieldDeclaration fd = (FieldDeclaration) super.tw.getProgramElement();
		int last = fd.toString().lastIndexOf(">");
		
		// Prevents "Zero Service" outputs logged to the console.
		if (fd.getMemberParent().getProgramModelInfo() == null)
			fd.getFactory().getServiceConfiguration().getChangeHistory().updateModel();
		
		// Find iterator in declaration list.
		int counter = -1;
		for (int i = 0; i < fd.getDeclarationSpecifiers().size(); i++)
			if (fd.getDeclarationSpecifiers().get(i).toString().contains("Static"))
				counter = i;

		// Construct refactoring transformation.
		// The transformation is handled here manually and the transformation
		// method will do nothing for this refactoring when it is called.
		super.transformation = null;
		getChangeHistory().begin(this);
		detach(fd.getDeclarationSpecifiers().get(counter));

		// Specify refactoring information for results information.
		String unitName = getSourceFileRepository().getKnownCompilationUnits().get(unit).getName();
		String typeName = MiscKit.getParentTypeDeclaration(fd).getFullName();
		super.refactoringInfo = "Iteration " + iteration + ": \"Make Field Non Static\" applied at class " 
				+ super.getClassName(unitName, typeName) + " to field " + fd.toString().substring(last + 2);
		
		// Stores list of names of classes affected by refactoring.
		super.affectedClasses = new ArrayList<String>(1);
		super.affectedClasses.add(super.getFileName(unitName, typeName));
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
		super.transformation = new Modify(config, true, fd, AccessFlags.STATIC);
		report = super.transformation.analyze();
		if (report instanceof Problem) 
			return setProblemReport(report);
		
		return setProblemReport(EQUIVALENCE);
	}

	protected boolean mayRefactor(FieldDeclaration fd)
	{
		if (!fd.isStatic() || (fd instanceof EnumConstantDeclaration) || (MiscKit.getParentTypeDeclaration(fd) instanceof InterfaceDeclaration))
			return false;
		else
		{
			for (FieldSpecification fs : fd.getFieldSpecifications())
			{
				for (FieldReference fr : getCrossReferenceSourceInfo().getReferences(fs))
				{
					if (fr.getReferencePrefix() instanceof TypeReference)
						return false;
					
					for (Import i : UnitKit.getCompilationUnit(fr).getImports())
						if ((i.isStaticImport()) && (i.toString().contains(MiscKit.getParentTypeDeclaration(fd).getName())))
							return false;
				}
			}
			
			return true;
		}	
	}
}