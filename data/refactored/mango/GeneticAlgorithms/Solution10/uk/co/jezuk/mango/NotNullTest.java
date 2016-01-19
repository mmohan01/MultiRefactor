package uk.co.jezuk.mango;

import junit.framework.*;

public class NotNullTest extends TestCase {
    public NotNullTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(NotNullTest.class); }

    public void test1()
     {
        Predicate<String> t = Predicates.NotNull();
        assertEquals(false, t.test(null));
        assertEquals(true, t.test("hello"));
    } // test1
} // class NotNullTest
