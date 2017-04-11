package refactorings.field;

import java.util.ArrayList;

import multirefactor.AccessFlags;
import recoder.CrossReferenceServiceConfiguration;
import recoder.convenience.TreeWalker;
import recoder.java.declaration.EnumConstantDeclaration;
import recoder.java.declaration.FieldDeclaration;
import recoder.java.declaration.FieldSpecification;
import recoder.java.declaration.InterfaceDeclaration;
import recoder.java.declaration.TypeDeclaration;
import recoder.java.declaration.modifier.Private;
import recoder.java.reference.FieldReference;
import recoder.java.reference.SuperReference;
import recoder.java.reference.ThisReference;
import recoder.java.reference.TypeReference;
import recoder.kit.MiscKit;
import recoder.kit.Problem;
import recoder.kit.ProblemReport;
import recoder.kit.transformation.Modify;
import recoder.service.CrossReferenceSourceInfo;
import refactorings.FieldRefactoring;

public class IncreaseFieldVisibility extends FieldRefactoring 
{	
	public IncreaseFieldVisibility(CrossReferenceServiceConfiguration sc) 
	{
		super(sc);
	}
	
	public IncreaseFieldVisibility() 
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
		super.transformation = new Modify(config, true, fd, super.visibilityDown(fd.getVisibilityModifier()));
		report = super.transformation.analyze();
		if (report instanceof Problem) 
			return setProblemReport(report);

		// Specify refactoring information for results information.
		String unitName = getSourceFileRepository().getKnownCompilationUnits().get(unit).getName();
		String packageName = MiscKit.getParentTypeDeclaration(fd).getPackage().getFullName();
		String className = MiscKit.getParentTypeDeclaration(fd).getFullName().substring(packageName.length() + 1).replace('.', '\\');
		super.refactoringInfo = "Iteration " + iteration + ": \"Increase Field Visibility\" applied at class " 
				+ className + " to field " + fd.toString().substring(last + 2) 
				+ " from " + super.currentModifier(fd.getVisibilityModifier()) 
				+ " to " + super.refactoredDownModifier(fd.getVisibilityModifier());
		
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
		super.transformation = new Modify(config, true, fd, super.visibilityUp(fd.getVisibilityModifier()));
		report = super.transformation.analyze();
		if (report instanceof Problem) 
			return setProblemReport(report);
		
		return setProblemReport(EQUIVALENCE);
	}
	
	protected boolean mayRefactor(FieldDeclaration fd)
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
			for (FieldSpecification fs : ((FieldDeclaration) fd).getFieldSpecifications())
			{
				for (FieldReference fr : si.getReferences(fs))
				{
					// Check if a sub class can access the field.
					if ((si.getType(fr.getReferencePrefix()) instanceof TypeDeclaration) && ((fr.getReferencePrefix() instanceof TypeReference) || 
						 ((si.getType(fr.getReferencePrefix()) != null) && !(fr.getReferencePrefix() instanceof ThisReference) && 
						  !(fr.getReferencePrefix() instanceof SuperReference))))	
					{
						if (!(referenceTypes.contains(si.getType(fr.getReferencePrefix()))))
							referenceTypes.add((TypeDeclaration) si.getType(fr.getReferencePrefix()));
					}
					
					if (MiscKit.getParentTypeDeclaration(fr) == null)
						return false;
					else if (!(referenceTypes.contains(MiscKit.getParentTypeDeclaration(fr))))
						referenceTypes.add(MiscKit.getParentTypeDeclaration(fr));
				}
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
}