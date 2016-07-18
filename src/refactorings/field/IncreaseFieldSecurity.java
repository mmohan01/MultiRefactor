package refactorings.field;

import java.util.ArrayList;

import multirefactor.AccessFlags;
import recoder.CrossReferenceServiceConfiguration;
import recoder.convenience.AbstractTreeWalker;
import recoder.convenience.TreeWalker;
import recoder.java.ProgramElement;
import recoder.java.declaration.EnumConstantDeclaration;
import recoder.java.declaration.FieldDeclaration;
import recoder.java.declaration.InterfaceDeclaration;
import recoder.java.declaration.TypeDeclaration;
import recoder.java.declaration.modifier.Private;
import recoder.java.reference.FieldReference;
import recoder.kit.MiscKit;
import recoder.kit.Problem;
import recoder.kit.ProblemReport;
import recoder.kit.transformation.Modify;
import recoder.service.CrossReferenceSourceInfo;
import refactorings.Refactoring;

public class IncreaseFieldSecurity extends Refactoring 
{	
	public IncreaseFieldSecurity(CrossReferenceServiceConfiguration sc) 
	{
		super(sc);
	}
	
	public IncreaseFieldSecurity() 
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
			super.tw.next(FieldDeclaration.class);
			FieldDeclaration fd = (FieldDeclaration) super.tw.getProgramElement();
			if (!mayRefactor(fd))
				i--;
		}
		
		ProgramElement pe = super.tw.getProgramElement();
		FieldDeclaration fd = (FieldDeclaration) pe;
		int last = pe.toString().lastIndexOf(">");

		// Construct refactoring transformation.
		super.transformation = new Modify(config, true, fd, super.visibilityDown(fd.getVisibilityModifier()));
		report = super.transformation.analyze();
		if (report instanceof Problem) 
			return setProblemReport(report);

		// Specify refactoring information for results information.
		super.refactoringInfo = "Iteration " + iteration + ": \"Increase Field Security\" applied at class " 
				+ super.getFileName(getSourceFileRepository().getKnownCompilationUnits().get(unit).getName())
				+ " to field " + pe.toString().substring(last + 2) + " from " + super.currentModifier(fd.getVisibilityModifier()) 
				+ " to " + super.refactoredDownModifier(fd.getVisibilityModifier());

		return setProblemReport(EQUIVALENCE);
	}
	
	public ProblemReport analyzeReverse() 
	{
		// Initialise and pick the element to visit.
		CrossReferenceServiceConfiguration config = getServiceConfiguration();
		ProblemReport report = EQUIVALENCE;	
		FieldDeclaration fd = (FieldDeclaration) super.tw.getProgramElement();

		// Construct refactoring transformation.
		super.transformation = new Modify(config, true, fd, super.visibilityUp(fd.getVisibilityModifier()));
		report = super.transformation.analyze();
		if (report instanceof Problem) 
			return setProblemReport(report);
		
		return setProblemReport(EQUIVALENCE);
	}
	
	public boolean mayRefactor(FieldDeclaration fd)
	{
		if ((fd.getVisibilityModifier() instanceof Private) || (fd instanceof EnumConstantDeclaration) || 
			(fd.getMemberParent() instanceof InterfaceDeclaration))
			return false;
		else
		{
			CrossReferenceSourceInfo si = getCrossReferenceSourceInfo();
			ArrayList<TypeDeclaration> referenceTypes = new ArrayList<TypeDeclaration>();
			
			// Checks any reference to the field in the program 
			// and whether its class will be able to access the field.
			for (FieldReference fr : si.getReferences(((FieldDeclaration) fd).getFieldSpecifications().get(0)))
			{
				if (MiscKit.getParentTypeDeclaration(fr) == null)
					return false;
				else if (!(referenceTypes.contains(MiscKit.getParentTypeDeclaration(fr))))
					referenceTypes.add(MiscKit.getParentTypeDeclaration(fr));
			}
			
			for (TypeDeclaration td : referenceTypes)
			{
				if (super.visibilityDown(fd.getVisibilityModifier()) == AccessFlags.PRIVATE)
				{
					if (!(fd.getMemberParent().equals(td)))
						return false;
				}
				else if (!(fd.getMemberParent().getPackage().equals(td.getPackage())))
				{
					if (!(super.visibilityDown(fd.getVisibilityModifier()) == AccessFlags.PROTECTED))
						return false;
					else if (!(td.getAllSupertypes().contains(fd.getMemberParent())))
						return false;
				}
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
		while (tw.next(FieldDeclaration.class))
		{
			FieldDeclaration fd = (FieldDeclaration) tw.getProgramElement();
			if (mayRefactor(fd))
				counter++;
		}

		return counter;
	}
	
	public String getName(int unit, int element)
	{
		AbstractTreeWalker tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));

		for (int i = 0; i < element; i++)
		{
			tw.next(FieldDeclaration.class);
			FieldDeclaration fd = (FieldDeclaration) tw.getProgramElement();
			if (!mayRefactor(fd))
				i--;
		}

		FieldDeclaration fd = (FieldDeclaration) tw.getProgramElement();
		return fd.toString();
	}
}