/**
 * Created on 10 nov 2010
 */
package recoder.testsuite.parser;

import recoder.convenience.TreeWalker;
import recoder.java.CompilationUnit;
import recoder.java.ProgramElement;
import recoder.java.SourceElement.Position;
import recoder.testsuite.RecoderTestCase;

/**
 * @author Tobias Gutzmann
 *
 */
public class ParserTest extends RecoderTestCase {
    public void testIdentifierPositions() throws Exception {
        String cuText = "class X {\n" +
                        "\tvoid foo(java.lang.String vs) {\n" +
                        "\t\tfoo(vs);\n" +
                        "\t}\n" +
                        "}\n";
        CompilationUnit cu = runIt(cuText).get(0);
        TreeWalker tw = new TreeWalker(cu);
        // TODO actually check that all elements have proper positions set...
    }
}
