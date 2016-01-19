package refactorings.type;

import recoder.CrossReferenceServiceConfiguration;
import recoder.convenience.AbstractTreeWalker;
import recoder.convenience.TreeWalker;
import recoder.java.ProgramElement;
import recoder.java.declaration.TypeDeclaration;
import recoder.kit.Problem;
import recoder.kit.ProblemReport;
import recoder.kit.transformation.Modify;
import refactorings.Refactoring;
import refactory.AccessFlags;

public abstract class MakeClassFinal extends Refactoring {
    public MakeClassFinal(CrossReferenceServiceConfiguration sc)
     {
        super(sc);
    }

    public MakeClassFinal()
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
            super.tw.next(TypeDeclaration.class);
            TypeDeclaration td = (TypeDeclaration) super.tw.getProgramElement();
            if (!mayRefactor(td))
                i--;
        }

        ProgramElement pe = super.tw.getProgramElement();
        TypeDeclaration td = (TypeDeclaration) pe;

        // Construct refactoring transformation.
        super.transformation = new Modify(config, true, td, AccessFlags.FINAL);
        report = super.transformation.analyze();
        if (report instanceof Problem)
            return setProblemReport(report);

        // Specify refactoring information for results information.
        super.refactoringInfo = "Iteration " + iteration + ": \"Make Class Final\" applied at class " + super.getFileName(getSourceFileRepository().getKnownCompilationUnits().get(unit).getName()) + " to element " + pe.getClass().getSimpleName() + " (" + ((TypeDeclaration) pe).getName() + ")";

        return setProblemReport(EQUIVALENCE);
    }

    public ProblemReport analyzeReverse()
     {
        // Initialise and pick the element to visit.
        //CrossReferenceServiceConfiguration config = getServiceConfiguration();
        ProblemReport report = EQUIVALENCE;
        //TypeDeclaration td = (TypeDeclaration) super.tw.getProgramElement();

        // Construct refactoring transformation.
        //super.transformation = new Modify(config, true, td, AccessFlags.STATIC);
        super.transformation = null;
        report = super.transformation.analyze();
        if (report instanceof Problem)
            return setProblemReport(report);

        return setProblemReport(EQUIVALENCE);
    }

    public final boolean mayRefactor(TypeDeclaration td)
     {
        if (td.isFinal())
            return false;
         else
            return true;
    }

    // Count the amount of available elements in the chosen class for refactoring.
    // If an element is not applicable for the current refactoring it is not counted.
    public int getAmount(int unit)
     {
        int counter = 0;
        AbstractTreeWalker tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));

        // Only counts the relevant program element.
        while (tw.next(TypeDeclaration.class))
         {
            //counter++;
            TypeDeclaration td = (TypeDeclaration) tw.getProgramElement();
            if (mayRefactor(td))
                counter++;
        }

        return counter;
    }
}
