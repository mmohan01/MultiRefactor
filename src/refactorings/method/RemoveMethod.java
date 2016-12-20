package refactorings.method;

import java.util.ArrayList;

import recoder.CrossReferenceServiceConfiguration;
import recoder.abstraction.Method;
import recoder.abstraction.Type;
import recoder.convenience.AbstractTreeWalker;
import recoder.convenience.TreeWalker;
import recoder.java.ProgramElement;
import recoder.java.declaration.ConstructorDeclaration;
import recoder.java.declaration.MethodDeclaration;
import recoder.java.declaration.TypeDeclaration;
import recoder.kit.MethodKit;
import recoder.kit.ProblemReport;
import refactorings.Refactoring;

public class RemoveMethod extends Refactoring 
{
	private TypeDeclaration type, abstractType;
	private MethodDeclaration method, abstractMethod;
	private int position, abstractMethodPosition;
	
	public RemoveMethod(CrossReferenceServiceConfiguration sc) 
	{
		super(sc);
	}
	
	public RemoveMethod() 
	{
		super();
	}
	
	public ProblemReport analyze(int iteration, int unit, int element) 
	{
		// Initialise and pick the element to visit.
		super.tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));
		
		for (int i = 0; i < element; i++)
		{
			super.tw.next(MethodDeclaration.class);
			MethodDeclaration md = (MethodDeclaration) super.tw.getProgramElement();
			if (!mayRefactor(md))
				i--;
		}
		
		ProgramElement pe = super.tw.getProgramElement();
		this.method = (MethodDeclaration) pe;
		this.type = this.method.getMemberParent();
		ArrayList<Type> types = (ArrayList<Type>) this.method.getSignature();
		types.add(this.method.getReturnType());
		this.position = super.getPosition(this.type, this.method);
		this.abstractMethodPosition = -1;
		
		for (Method m : MethodKit.getAllRedefinedMethods(this.method))
		{
			if (m.isAbstract())
			{
				this.abstractMethod = (MethodDeclaration) m;
				this.abstractType = this.abstractMethod.getMemberParent();
				this.abstractMethodPosition = super.getPosition(this.abstractType, (MethodDeclaration) m);
			}
		}
		
		// Construct refactoring transformation.
		// The transformation is handled here manually and the transformation
		// method will do nothing for this refactoring when it is called.
		super.transformation = null;
		getChangeHistory().begin(this);
		detach(this.method);
		
		if (this.abstractMethodPosition != -1)
		{
			detach(this.abstractMethod);
		}

		// Specify refactoring information for results information.
		super.refactoringInfo = "Iteration " + iteration + ": \"Remove Method\" applied at class " 
				+ super.getFileName(getSourceFileRepository().getKnownCompilationUnits().get(unit).getName())
				+ " to method " + ((MethodDeclaration) pe).getName();
		
		// Stores list of names of classes affected by refactoring.
		super.affectedClasses = new ArrayList<String>(1);
		super.affectedClasses.add(super.getFileName(getSourceFileRepository().getKnownCompilationUnits().get(unit).getName()));
	
		return setProblemReport(EQUIVALENCE);
	}

	public ProblemReport analyzeReverse() 
	{
		// Construct refactoring transformation.
		super.transformation = null;
		attach(this.method, this.type, this.position);	
		
		if (this.abstractMethodPosition != -1)
			attach(this.abstractMethod, this.abstractType, this.abstractMethodPosition);
		
		return setProblemReport(EQUIVALENCE);
	}
	
	public boolean mayRefactor(MethodDeclaration md)
	{			
		// Prevents "Zero Service" outputs logged to the console.
		if (md.getMemberParent().getProgramModelInfo() == null)
			md.getFactory().getServiceConfiguration().getChangeHistory().updateModel();
		
		// Makes a number of checks against the method in order to exclude any insufficient candidates. 
		if ((md instanceof ConstructorDeclaration) || (md == null) || (getCrossReferenceSourceInfo().getReferences(md).size() > 0) ||
			((md.isAbstract()) && (MethodKit.getRedefiningMethods(getCrossReferenceSourceInfo(), md).size() > 0)) || 
			(MethodKit.isMain(getServiceConfiguration().getNameInfo(), md)))
			return false;
		else
		{
			for (Method m : MethodKit.getAllRedefinedMethods(md))
				if (((m.isAbstract()) && (MethodKit.getRedefiningMethods(getCrossReferenceSourceInfo(), m).size() > 1)) || 
					!(m instanceof MethodDeclaration) || (getCrossReferenceSourceInfo().getReferences(m).size() > 0))
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
		while (tw.next(MethodDeclaration.class))
		{
			MethodDeclaration md = (MethodDeclaration) tw.getProgramElement();
			if (mayRefactor(md))
				counter++;
		}

		return counter;
	}
	
	public String getName(int unit, int element)
	{		
		AbstractTreeWalker tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));

		for (int i = 0; i < element; i++)
		{
			tw.next(MethodDeclaration.class);
			MethodDeclaration md = (MethodDeclaration) tw.getProgramElement();
			if (!mayRefactor(md))
				i--;
		}

		MethodDeclaration md = (MethodDeclaration) tw.getProgramElement();
		return md.getName();
	}
}