package uk.co.jezuk.mango;

import junit.framework.*;

public class UnaryNorTest  extends TestCase {
    public UnaryNorTest(String name) { super(name); }
    public Test suite() { return new TestSuite(UnaryNorTest.class); }

    public void test1()
     {
        Predicate<Object> t = Predicates.Nor(Predicates.True(), Predicates.True());
        assertEquals(false, t.test(null));
    } // test1

    public void test2()
     {
        Predicate<Object> t = Predicates.Nor(Predicates.False(), Predicates.True());
        assertEquals(false, t.test(null));
    } // test2

    public void test3()
     {
        Predicate<Object> t = Predicates.Nor(Predicates.True(), Predicates.False());
        assertEquals(false, t.test(null));
    } // test3

    public void test4()
     {
        Predicate<Object> t = Predicates.Nor(Predicates.False(), Predicates.False());
        assertEquals(true, t.test(null));
    } // test4
} // UnaryNorTest
