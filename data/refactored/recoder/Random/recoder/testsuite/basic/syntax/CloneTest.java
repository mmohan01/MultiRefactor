package recoder.testsuite.basic.syntax;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;
import recoder.ParserException;
import recoder.convenience.Format;
import recoder.io.SourceFileRepository;
import recoder.java.CompilationUnit;
import recoder.java.ProgramElement;
import recoder.testsuite.basic.analysis.TestBase;

public class CloneTest extends TestBase {
    public void testClone() {
        SourceFileRepository sfr = config.getSourceFileRepository();
        List<CompilationUnit> units = sfr.getCompilationUnits();
        for (int i = 0; i < units.size(); i += 1) {
            CompilationUnit cu = units.get(i);
            String buffer1 = cu.toSource();
            CompilationUnit cv = cu.deepClone();
            if (!ProgramElement.STRUCTURAL_EQUALITY.equals(cu, cv)) {
                Assert.fail("Printed tree of " + Format.toString("%u", cu) + " has changed its structure");
            }
            String buffer2 = cv.toSource();
            if (!buffer1.equals(buffer2)) {
                Assert.fail(Format.toString("Reprint of parsed %u differs", cu));
            }
        }
    }
}
