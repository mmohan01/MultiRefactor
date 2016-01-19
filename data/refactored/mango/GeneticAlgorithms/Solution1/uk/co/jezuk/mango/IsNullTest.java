package uk.co.jezuk.mango;

import junit.framework.*;

public class IsNullTest extends TestCase {
    public IsNullTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(IsNullTest.class); }

    public void test1()
     {
        Predicate<String> t = Predicates.IsNull();
        assertEquals(true, t.test(null));
        assertEquals(false, t.test("hello"));
    } // test1
} // class IsNullTest
