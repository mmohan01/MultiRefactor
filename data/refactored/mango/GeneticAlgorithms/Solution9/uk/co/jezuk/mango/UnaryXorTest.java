package uk.co.jezuk.mango;

import junit.framework.*;

public class UnaryXorTest  extends TestCase {
    public UnaryXorTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(UnaryXorTest.class); }

    public void test1()
     {
        Predicate<Object> t = Predicates.Xor(Predicates.True(), Predicates.True());
        assertEquals(false, t.test(null));
    } // test1

    public void test2()
     {
        Predicate<Object> t = Predicates.Xor(Predicates.False(), Predicates.True());
        assertEquals(true, t.test(null));
    } // test2

    public void test3()
     {
        Predicate<Object> t = Predicates.Xor(Predicates.True(), Predicates.False());
        assertEquals(true, t.test(null));
    } // test3

    public void test4()
     {
        Predicate<Object> t = Predicates.Xor(Predicates.False(), Predicates.False());
        assertEquals(false, t.test(null));
    } // test4
} // UnaryXorTest
