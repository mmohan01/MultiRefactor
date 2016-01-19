package recoder.testsuite.basic.syntax;

import java.io.IOException;

import junit.framework.Assert;
import recoder.ParserException;
import recoder.convenience.Format;
import recoder.convenience.TreeWalker;
import recoder.io.SourceFileRepository;
import recoder.java.CompilationUnit;
import recoder.java.ProgramElement;
import recoder.java.SourceElement.Position;
import recoder.testsuite.basic.analysis.TestBase;

public class WalkPositionTest extends TestBase {
    public void testWalkPosition() {
        SourceFileRepository sfr = config.getSourceFileRepository();
        for (CompilationUnit u: sfr.getCompilationUnits()) {
            ProgramElement oldPe = u;
            Position oldPos = u.getFirstElement().getStartPosition();
            TreeWalker tw = new TreeWalker(u);
            while (tw.next()) {
                ProgramElement pe = tw.getProgramElement();
                Position newPos = pe.getFirstElement().getStartPosition();
                if (newPos.equals(Position.UNDEFINED)) {
                    fail("Position undefined: " + Format.toString("%c @%p in %u", pe));
                }
                if (newPos.getLine() < oldPos.getLine()
                        || (newPos.getLine() == oldPos.getLine() && newPos.getColumn() < newPos.getColumn())) {
                    Assert.fail("Position mismatch: " + Format.toString("%c @%p in %u", oldPe) + "/" + Format.toString("%c @%p", pe));
                }
                oldPos = newPos;
                oldPe = pe;
            }
        }
    }
}
