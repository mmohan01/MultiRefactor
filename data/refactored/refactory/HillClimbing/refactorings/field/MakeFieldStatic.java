package refactorings.field;

import recoder.CrossReferenceServiceConfiguration;
import recoder.convenience.AbstractTreeWalker;
import recoder.convenience.TreeWalker;
import recoder.java.ProgramElement;
import recoder.java.declaration.FieldDeclaration;
import recoder.kit.Problem;
import recoder.kit.ProblemReport;
import recoder.kit.transformation.Modify;
import refactorings.Refactoring;
import refactory.AccessFlags;

public abstract class MakeFieldStatic extends Refactoring {
    public MakeFieldStatic(CrossReferenceServiceConfiguration sc)
     {
        super(sc);
    }

    public MakeFieldStatic()
     {
        super();
    }

    public abstract ProblemReport analyze(int iteration, int unit, int element)
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
        super.transformation = new Modify(config, true, fd, AccessFlags.STATIC);
        report = super.transformation.analyze();
        if (report instanceof Problem)
            return setProblemReport(report);

        // Specify refactoring information for results information.
        super.refactoringInfo = "Iteration " + iteration + ": \"Make Field Static\" applied at class " + super.getFileName(getSourceFileRepository().getKnownCompilationUnits().get(unit).getName()) + " to element " + pe.getClass().getSimpleName() + " (" + pe.toString().substring(last + 2) + ")";

        return setProblemReport(EQUIVALENCE);
    }

    public abstract ProblemReport analyzeReverse()
     {
        // Initialise and pick the element to visit.
        //CrossReferenceServiceConfiguration config = getServiceConfiguration();
        ProblemReport report = EQUIVALENCE;
        //FieldDeclaration fd = (FieldDeclaration) super.tw.getProgramElement();

        // Construct refactoring transformation.
        //super.transformation = new Modify(config, true, fd, AccessFlags.STATIC);
        super.transformation = null;
        report = super.transformation.analyze();
        if (report instanceof Problem)
            return setProblemReport(report);

        return setProblemReport(EQUIVALENCE);
    }

    public abstract boolean mayRefactor(FieldDeclaration fd)
     {
        if (fd.isStatic())
            return false;
         else
            return true;
    }

    // Count the amount of available elements in the chosen class for refactoring.
    // If an element is not applicable for the current refactoring it is not counted.
    public abstract int getAmount(int unit)
     {
        int counter = 0;
        AbstractTreeWalker tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));

        // Only counts the relevant program element.
        while (tw.next(FieldDeclaration.class))
         {
            //counter++;
            FieldDeclaration fd = (FieldDeclaration) tw.getProgramElement();
            if (mayRefactor(fd))
                counter++;
        }

        return counter;
    }
}
