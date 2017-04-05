package refactorings.method;

import java.util.ArrayList;

import multirefactor.AccessFlags;
import recoder.CrossReferenceServiceConfiguration;
import recoder.abstraction.Member;
import recoder.convenience.TreeWalker;
import recoder.java.declaration.ConstructorDeclaration;
import recoder.java.declaration.MethodDeclaration;
import recoder.kit.MethodKit;
import recoder.kit.MiscKit;
import recoder.kit.Problem;
import recoder.kit.ProblemReport;
import recoder.kit.transformation.Modify;
import refactorings.MethodRefactoring;

public class MakeMethodStatic extends MethodRefactoring 
{		
 	public MakeMethodStatic(CrossReferenceServiceConfiguration sc) 
	{
		super(sc);
	}
	
	public MakeMethodStatic() 
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
			super.tw.next(MethodDeclaration.class);
	
		MethodDeclaration md = (MethodDeclaration) super.tw.getProgramElement();
		
		// Prevents "Zero Service" outputs logged to the console.
		if (md.getMemberParent().getProgramModelInfo() == null)
			md.getFactory().getServiceConfiguration().getChangeHistory().updateModel();
		
		// Construct refactoring transformation.
		super.transformation = new Modify(config, true, md, AccessFlags.STATIC);
		report = super.transformation.analyze();
		if (report instanceof Problem) 
			return setProblemReport(report);

		// Specify refactoring information for results information.
		String unitName = getSourceFileRepository().getKnownCompilationUnits().get(unit).getName();
		String packageName = MiscKit.getParentTypeDeclaration(md).getPackage().getFullName();
		String className = MiscKit.getParentTypeDeclaration(md).getFullName().substring(packageName.length() + 1).replace('.', '\\');
		super.refactoringInfo = "Iteration " + iteration + ": \"Make Method Static\" applied at class " + className + " to method " + super.getMethodName(md);
		
		// Stores list of names of classes affected by refactoring.
		super.affectedClasses = new ArrayList<String>(1);
		super.affectedClasses.add(super.getFileName(unitName, className));
		super.affectedElement = ":" + super.getMethodName(md) + ":";

		return setProblemReport(EQUIVALENCE);
	}

	public ProblemReport analyzeReverse() 
	{
		// Initialise and pick the element to visit.	
		MethodDeclaration md = (MethodDeclaration) super.tw.getProgramElement();

		// Find iterator in declaration list.
		int counter = -1;
		for (int i = 0; i < md.getDeclarationSpecifiers().size(); i++)
			if (md.getDeclarationSpecifiers().get(i).toString().contains("Static"))
				counter = i;

		// Construct refactoring transformation.
		super.transformation = null;
		detach(md.getDeclarationSpecifiers().get(counter));
		return setProblemReport(EQUIVALENCE);
	}

	protected boolean mayRefactor(MethodDeclaration md)
	{
		if ((md.isNative()) || (md.isStatic()) || (md.isAbstract()) || (md instanceof ConstructorDeclaration) || (MiscKit.getParentTypeDeclaration(md).isInterface()))
			return false;
		else
		{
			// Prevents "Zero Service" outputs logged to the console.
			if (md.getMemberParent().getProgramModelInfo() == null)
				md.getFactory().getServiceConfiguration().getChangeHistory().updateModel();
			
			if (MethodKit.getRedefiningMethods(getCrossReferenceSourceInfo(), md).size() > 0)
				return false;
			else
			{
				ArrayList<Member> allMembers = new ArrayList<Member>();
				allMembers.addAll(MiscKit.getParentTypeDeclaration(md).getAllMethods());
				allMembers.addAll(MiscKit.getParentTypeDeclaration(md).getAllFields());

				for (Member m : allMembers)
				{
					if ((m == null) || (m.equals(md)))
							continue;

					if (!(m.isStatic()) && (md.getBody().toSource().contains(m.getName())))
						return false;
				}	
			}
			
			return true;
		}
	}
}