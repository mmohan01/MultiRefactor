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
	
	public String getName(int unit, int element)
	{		
		TreeWalker tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));

		for (int i = 0; i < element; i++)
			tw.next(FieldDeclaration.class);

		FieldDeclaration fd = (FieldDeclaration) tw.getProgramElement();
		return fd.toString();
	}
	
	public int checkElements(int unit, String name)
	{		
		TreeWalker tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));
		int element = 0;

		while (tw.next(FieldDeclaration.class))
		{
			element++;
			FieldDeclaration fd = (FieldDeclaration) tw.getProgramElement();
			
			if ((fd.toString() != null) && (fd.toString().equals(name)))
				return (mayRefactor(fd)) ? element : -1;
		}
		
		return -1;
	}
}