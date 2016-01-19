package uk.co.jezuk.mango;

import junit.framework.*;

public class TrueTest  extends TestCase {
    public TrueTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(TrueTest.class); }

    public void test1()
     {
        Predicate<Object> t = Predicates.True();
        assertEquals(true, t.test(new Integer(7)));
        assertEquals(true, t.test(t));
        assertEquals(true, t.test(new String("brainfart")));
    } // test1
} // TrueTest
