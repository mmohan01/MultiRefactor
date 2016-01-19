package recoder.testsuite.basic.syntax;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;
import recoder.ParserException;
import recoder.ProgramFactory;
import recoder.convenience.Format;
import recoder.io.SourceFileRepository;
import recoder.java.CompilationUnit;
import recoder.java.ProgramElement;
import recoder.testsuite.basic.BasicTestsSuite;
import recoder.testsuite.basic.analysis.TestBase;

public class ParserPrinterTest extends TestBase {
    public void testParserPrinter() throws ParserException {
        SourceFileRepository sfr = config.getSourceFileRepository();
        ProgramFactory pf = config.getProgramFactory();
        List<CompilationUnit> units = sfr.getCompilationUnits();
        for (int i = 0; i < units.size(); i += 1) {
            CompilationUnit cu = units.get(i);
            String buffer1 = cu.toSource();
            CompilationUnit cv = pf.parseCompilationUnit(buffer1);
            if (!ProgramElement.STRUCTURAL_EQUALITY.equals(cu, cv)) {
                Assert.fail("Printed tree of " + Format.toString("%u", cu) + " has changed its structure");
            }
            String buffer2 = cv.toSource();
            assertEquals(buffer1, buffer2);
        }
    }
}
