package refactorings;

import recoder.CrossReferenceServiceConfiguration;
import recoder.convenience.AbstractTreeWalker;
import recoder.java.declaration.modifier.Private;
import recoder.java.declaration.modifier.Protected;
import recoder.java.declaration.modifier.Public;
import recoder.java.declaration.modifier.VisibilityModifier;
import recoder.kit.Problem;
import recoder.kit.ProblemReport;
import recoder.kit.TwoPassTransformation;
import refactory.AccessFlags;

public abstract class Refactoring extends TwoPassTransformation 
{
	protected TwoPassTransformation transformation;
	protected String refactoringInfo; 
	protected AbstractTreeWalker tw;
	
	public Refactoring(CrossReferenceServiceConfiguration sc) 
	{
		super(sc);
	}
	
	public Refactoring() 
	{
		super();
	}
	
	
	public abstract ProblemReport analyze(int iteration, int unit, int element);
	
	public abstract ProblemReport analyzeReverse();
	
	public abstract int getAmount(int unit);
	
	public void transform(ProblemReport p) 
	{
		if (p instanceof Problem)
        {
        	System.out.println("\nPROBLEM REPORT: ");
        	System.err.println(p.toString());
        }
		else
		{
			super.transform();
			
			if (this.transformation != null)
				this.transformation.transform();
		}
	}	
	
	protected String currentModifier(VisibilityModifier vm)
	{		
		if (vm instanceof Public)
			return "public";
		else if (vm instanceof Protected)
			return "protected";
		else if (vm instanceof Private)
			return "private";
		else
			return "package";
	}
	
	protected int visibilityUp(VisibilityModifier vm)
	{
		if ((vm instanceof Public) || (vm instanceof Protected))
			return AccessFlags.PUBLIC;
		else if (vm instanceof Private)
			return AccessFlags.PACKAGE;
		else
			return AccessFlags.PROTECTED;
	}
	
	protected String refactoredUpModifier(VisibilityModifier vm)
	{		
		if ((vm instanceof Protected) || (vm instanceof Public))
			return "public";
		else if (vm instanceof Private)
			return "package";
		else
			return "protected";
	}
	
	protected int visibilityDown(VisibilityModifier vm)
	{
		if (vm instanceof Public)
			return AccessFlags.PROTECTED;
		else if (vm instanceof Protected)
			return AccessFlags.PACKAGE;
		else
			return AccessFlags.PRIVATE;
	}
	
	protected String refactoredDownModifier(VisibilityModifier vm)
	{		
		if (vm instanceof Public)
			return "protected";
		else if (vm instanceof Protected)
			return "package";
		else 
			return "private";
	}
	
	// Truncates a path name to just the element name.
	protected String getFileName(String input)
	{
		int lastDash = input.lastIndexOf("\\");
		int lastDot = input.lastIndexOf(".");
		if ((lastDash >= 0) && (lastDot >= 0)) 
			input = input.substring(lastDash + 1, lastDot);

		return input;
	}
	
	public String getRefactoringInfo()
	{
		return this.refactoringInfo;
	}
}