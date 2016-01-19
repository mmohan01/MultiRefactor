/**
 * 
 */
package recoder.testsuite.semantics;

import junit.framework.TestCase;
import recoder.java.JavaProgramFactory;
import recoder.java.statement.If;
import recoder.service.SyntaxException;

/**
 * @author Tobias Gutzmann
 *
 */
public class ASTChecks extends TestCase {

    public void testIf() {
        If if_ = new JavaProgramFactory().createIf();
        try {
            if_.validate(); // throws SyntaxException
            fail(); // fail!
        } catch (SyntaxException e) {
            // We expect this -> return normally !
    }
    }
}
