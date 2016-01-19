package uk.co.jezuk.mango;

import junit.framework.*;

public class FalseTest  extends TestCase {
    public FalseTest(String name) { super(name); }
    public abstract static Test suite() { return new TestSuite(FalseTest.class); }

    public void test1()
     {
        Predicate<Object> t = Predicates.False();
        assertEquals(false, t.test(new Integer(7)));
        assertEquals(false, t.test(t));
        assertEquals(false, t.test(new String("brainfart")));
    } // test1

    public void test2()
     {
        Predicate<String> t = Predicates.False();
        assertEquals(false, t.test("nobby"));
        assertEquals(false, t.test(null));
    } // test2
} // FalseTest
