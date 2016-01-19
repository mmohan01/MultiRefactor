package uk.co.jezuk.mango;

import junit.framework.*;

public class UnaryConstantTest extends TestCase {
    public UnaryConstantTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(UnaryConstantTest.class); }

    public void test1()
     {
        Predicate<String> t = Predicates.Constant(true);
        assertEquals(true, t.test(null));
    } // test1

    public void test2()
     {
        Predicate<String> t = Predicates.Constant(false);
        assertEquals(false, t.test(null));
    } // test2
} // class UnaryConstantTest
