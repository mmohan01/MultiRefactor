package refactorings.field;

import recoder.CrossReferenceServiceConfiguration;
import recoder.convenience.AbstractTreeWalker;
import recoder.convenience.TreeWalker;
import recoder.java.ProgramElement;
import recoder.java.declaration.VariableDeclaration;
import recoder.java.declaration.modifier.Private;
import recoder.kit.Problem;
import recoder.kit.ProblemReport;
import recoder.kit.transformation.Modify;
import refactorings.Refactoring;

public abstract final class DecreaseFieldSecurity extends Refactoring {
    public DecreaseFieldSecurity(CrossReferenceServiceConfiguration sc)
     {
        super(sc);
    }

    public DecreaseFieldSecurity()
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
            super.tw.next(VariableDeclaration.class);
            VariableDeclaration vd = (VariableDeclaration) super.tw.getProgramElement();
            if (!mayRefactor(vd))
                i--;
        }

        ProgramElement pe = super.tw.getProgramElement();
        VariableDeclaration vd = (VariableDeclaration) pe;
        int last = pe.toString().lastIndexOf(">");

        // Construct refactoring transformation.
        super.transformation = new Modify(config, true, vd, super.visibilityDown(vd.getVisibilityModifier()));
        report = super.transformation.analyze();
        if (report instanceof Problem)
            return setProblemReport(report);

        // Specify refactoring information for results information.
        super.refactoringInfo = "Iteration " + iteration + ": \"Decrease Field Security\" applied at class " + super.getFileName(getSourceFileRepository().getKnownCompilationUnits().get(unit).getName()) + " to element " + pe.getClass().getSimpleName() + " (" + pe.toString().substring(last + 2) + ") from " + super.currentModifier(vd.getVisibilityModifier()) + " to " + super.refactoredDownModifier(vd.getVisibilityModifier());

        return setProblemReport(EQUIVALENCE);
    }

    public ProblemReport analyzeReverse()
     {
        // Initialise and pick the element to visit.
        CrossReferenceServiceConfiguration config = getServiceConfiguration();
        ProblemReport report = EQUIVALENCE;
        VariableDeclaration vd = (VariableDeclaration) super.tw.getProgramElement();

        // Construct refactoring transformation.
        super.transformation = new Modify(config, true, vd, super.visibilityUp(vd.getVisibilityModifier()));
        report = super.transformation.analyze();
        if (report instanceof Problem)
            return setProblemReport(report);

        return setProblemReport(EQUIVALENCE);
    }

    public boolean mayRefactor(VariableDeclaration vd)
     {
        if (vd.getVisibilityModifier() instanceof Private)
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
        while (tw.next(VariableDeclaration.class))
         {
            //counter++;
            VariableDeclaration vd = (VariableDeclaration) tw.getProgramElement();
            if (mayRefactor(vd))
                counter++;
        }

        return counter;
    }
}
