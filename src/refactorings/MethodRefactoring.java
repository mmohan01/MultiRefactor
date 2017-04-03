package refactorings;

import recoder.CrossReferenceServiceConfiguration;
import recoder.convenience.TreeWalker;
import recoder.java.declaration.MethodDeclaration;

public abstract class MethodRefactoring extends Refactoring 
{	
	public MethodRefactoring(CrossReferenceServiceConfiguration sc) 
	{
		super(sc);
	}
	
	public MethodRefactoring() 
	{
		super();
	}
	
	protected abstract boolean mayRefactor(MethodDeclaration md);
	
	public int getAmount(int unit)
	{
		int counter = 0;
		TreeWalker tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));

		// Only counts the relevant program element.
		while (tw.next(MethodDeclaration.class))
		{
			MethodDeclaration md = (MethodDeclaration) tw.getProgramElement();
			if (mayRefactor(md))
				counter++;
		}

		return counter;
	}
	
	public int getAbsolutePosition(int unit, int element)
	{		
		TreeWalker tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));
		int absolutePosition = 0;

		for (int i = 0; i < element; i++)
		{
			tw.next(MethodDeclaration.class);
			MethodDeclaration fd = (MethodDeclaration) tw.getProgramElement();
			if (!mayRefactor(fd))
				i--;

			absolutePosition++;
		}

		return absolutePosition;
	}
		
	public int checkElements(int unit, String refactoringInfo)
	{		
		TreeWalker tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));
		int element = 0;
		int from  = refactoringInfo.indexOf(" to method ") + 11;
		int to = (refactoringInfo.indexOf(") ", from) == -1) ? refactoringInfo.length() : refactoringInfo.indexOf(") ", from) + 1;
		String name = refactoringInfo.substring(from,  to);

		while (tw.next(MethodDeclaration.class))
		{
			element++;
			MethodDeclaration md = (MethodDeclaration) tw.getProgramElement();
			
			if ((md.getName() != null) && (getMethodName(md).equals(name)))
				return (mayRefactor(md)) ? element : -1;
		}
		
		return -1;
	}
}