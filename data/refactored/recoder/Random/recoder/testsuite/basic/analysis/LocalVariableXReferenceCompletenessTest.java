package recoder.testsuite.basic.analysis;

import java.util.List;

import junit.framework.Assert;
import recoder.abstraction.Field;
import recoder.abstraction.Variable;
import recoder.convenience.TreeWalker;
import recoder.io.SourceFileRepository;
import recoder.java.CompilationUnit;
import recoder.java.ProgramElement;
import recoder.java.reference.VariableReference;
import recoder.service.CrossReferenceSourceInfo;

public class LocalVariableXReferenceCompletenessTest extends XReferenceCompletenessTest {

    public void testLocalVariableXReferenceCompletenessTest() {
        CrossReferenceSourceInfo xrsi = config.getCrossReferenceSourceInfo();
        SourceFileRepository sfr = config.getSourceFileRepository();

        List<CompilationUnit> units = sfr.getCompilationUnits();
        for (int i = 0; i < units.size(); i += 1) {
            CompilationUnit u = units.get(i);
            TreeWalker tw = new TreeWalker(u);
            while (tw.next()) {
                ProgramElement pe = tw.getProgramElement();
                if ((pe instanceof Variable) && !(pe instanceof Field)) {
                    Variable x = (Variable) pe;
                    List<? extends  VariableReference> list = xrsi.getReferences(x);
                    for (int k = 0; k < list.size(); k += 1) {
                        VariableReference r = list.get(k);
                        Variable y = xrsi.getVariable(r);
                        if (x != y) {
                            Assert.fail(makeResolutionError(r, x, y));
                        }
                    }
                }
            }
        }
    }
}
