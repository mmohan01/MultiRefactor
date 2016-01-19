package uk.co.jezuk.mango;

import junit.framework.*;

public class UnaryXnorTest  extends TestCase {
    public UnaryXnorTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(UnaryXnorTest.class); }

    public void test1()
     {
        Predicate<Object> t = Predicates.Xnor(Predicates.True(), Predicates.True());
        assertEquals(true, t.test(null));
    } // test1

    public void test2()
     {
        Predicate<Object> t = Predicates.Xnor(Predicates.False(), Predicates.True());
        assertEquals(false, t.test(null));
    } // test2

    public void test3()
     {
        Predicate<Object> t = Predicates.Xnor(Predicates.True(), Predicates.False());
        assertEquals(false, t.test(null));
    } // test3

    public void test4()
     {
        Predicate<Object> t = Predicates.Xnor(Predicates.False(), Predicates.False());
        assertEquals(true, t.test(null));
    } // test4
} // UnaryXnorTest
