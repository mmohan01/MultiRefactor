/**
 * 
 */
package recoder.testsuite.completeCoverage;

import java.util.List;

import recoder.ParserException;
import recoder.abstraction.Method;
import recoder.kit.MethodKit;
import recoder.testsuite.RecoderTestCase;

/**
 * @author Tobias Gutzmann
 *
 */
public class KitCoverage extends RecoderTestCase {
    public KitCoverage() {
        // default constructor
    }
    public KitCoverage(String name) {
        super(name);
    }

    public void testGetAllRedefinedMethods() throws ParserException {
        String cuText1 =
            "abstract class A<T> {" +
            "abstract void foo(T t);" +
            "}" +
            "class B extends A<String> {" +
            "void foo(String s) {}" +
            "}" +
            "class C<T> extends A<T> {" +
            "void foo(String s) { }" +
            "void foo(T t) { }" +
            "}";
        runIt(cuText1);
        List<Method> meths = MethodKit.getAllRedefinedMethods(sc.getNameInfo().getClassType("B").getMethods().get(0));
        assertEquals(1, meths.size());
        meths = MethodKit.getAllRedefinedMethods(sc.getNameInfo().getClassType("C").getMethods().get(0));
        assertEquals(0, meths.size());
        meths = MethodKit.getAllRedefinedMethods(sc.getNameInfo().getClassType("C").getMethods().get(1));
        assertEquals(1, meths.size());
    }
}
