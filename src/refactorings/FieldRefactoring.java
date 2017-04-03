package refactorings;

import recoder.CrossReferenceServiceConfiguration;
import recoder.convenience.TreeWalker;
import recoder.java.declaration.FieldDeclaration;

public abstract class FieldRefactoring extends Refactoring 
{
	public FieldRefactoring(CrossReferenceServiceConfiguration sc) 
	{
		super(sc);
	}
	
	public FieldRefactoring() 
	{
		super();
	}
	
	protected abstract boolean mayRefactor(FieldDeclaration fd);
	
	public int getAmount(int unit)
	{
		int counter = 0;
		TreeWalker tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));

		// Only counts the relevant program element.
		while (tw.next(FieldDeclaration.class))
		{
			FieldDeclaration fd = (FieldDeclaration) tw.getProgramElement();
			if (mayRefactor(fd))
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
			tw.next(FieldDeclaration.class);
			FieldDeclaration fd = (FieldDeclaration) tw.getProgramElement();
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
		int from  = refactoringInfo.indexOf(" to field ") + 10;
		int to = (refactoringInfo.indexOf(' ', from) == -1) ? refactoringInfo.length() : refactoringInfo.indexOf(' ', from);
		String name = refactoringInfo.substring(from,  to);

		while (tw.next(FieldDeclaration.class))
		{
			element++;
			FieldDeclaration fd = (FieldDeclaration) tw.getProgramElement();
			int last = fd.toString().lastIndexOf(">");
			
			if ((fd.toString() != null) && (fd.toString().substring(last + 2).equals(name)))
				return (mayRefactor(fd)) ? element : -1;
		}
		
		return -1;
	}
}