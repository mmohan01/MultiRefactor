package uk.co.jezuk.mango;

import junit.framework.*;

public class BinaryPredicateConstantTest extends TestCase {
    public BinaryPredicateConstantTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(BinaryPredicateConstantTest.class); }

    public void test1()
     {
        BinaryPredicate<String, String> t = BinaryPredicates.Constant(true);
        assertEquals(true, t.test(null, null));
    } // test1

    public void test2()
     {
        BinaryPredicate<String, String> t = BinaryPredicates.Constant(false);
        assertEquals(false, t.test(null, null));
    } // test2
} // class BinaryPredicateConstantTest
