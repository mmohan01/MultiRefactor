package refactorings;

import recoder.CrossReferenceServiceConfiguration;
import recoder.convenience.TreeWalker;
import recoder.java.declaration.TypeDeclaration;

public abstract class TypeRefactoring extends Refactoring 
{	
	public TypeRefactoring(CrossReferenceServiceConfiguration sc) 
	{
		super(sc);
	}
	
	public TypeRefactoring() 
	{
		super();
	}
	
	protected abstract boolean mayRefactor(TypeDeclaration td);
	
	public int getAmount(int unit)
	{
		int counter = 0;
		TreeWalker tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));

		// Only counts the relevant program element.
		while (tw.next(TypeDeclaration.class))
		{
			TypeDeclaration td = (TypeDeclaration) tw.getProgramElement();
			if (mayRefactor(td))
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
			tw.next(TypeDeclaration.class);
			TypeDeclaration fd = (TypeDeclaration) tw.getProgramElement();
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
		int from  = refactoringInfo.indexOf(" class ") + 7;
		int to = (refactoringInfo.indexOf(' ', from) == -1) ? refactoringInfo.length() : refactoringInfo.indexOf(' ', from);
		String name = refactoringInfo.substring(from,  to);

		while (tw.next(TypeDeclaration.class))
		{
			element++;
			TypeDeclaration td = (TypeDeclaration) tw.getProgramElement();
			
			if ((td.getName() != null) && (td.getName().equals(name)))
				return (mayRefactor(td)) ? element : -1;
		}
		
		return -1;
	}
}