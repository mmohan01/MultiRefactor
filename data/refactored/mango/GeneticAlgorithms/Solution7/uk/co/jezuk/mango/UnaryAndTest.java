package uk.co.jezuk.mango;

import junit.framework.*;

public class UnaryAndTest  extends TestCase {
    public UnaryAndTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(UnaryAndTest.class); }

    public void test1()
     {
        Predicate<Object> t = Predicates.And(Predicates.True(), Predicates.True());
        assertEquals(true, t.test(null));
    } // test1

    public void test2()
     {
        Predicate<Object> t = Predicates.And(Predicates.False(), Predicates.True());
        assertEquals(false, t.test(null));
    } // test2

    public void test3()
     {
        Predicate<Object> t = Predicates.And(Predicates.True(), Predicates.False());
        assertEquals(false, t.test(null));
    } // test3

    public void test4()
     {
        Predicate<Object> t = Predicates.And(Predicates.False(), Predicates.False());
        assertEquals(false, t.test(null));
    } // test4
} // UnaryAndTest
