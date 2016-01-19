package uk.co.jezuk.mango;

import junit.framework.*;

public class UnaryNandTest  extends TestCase {
    public UnaryNandTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(UnaryNandTest.class); }

    public void test1()
     {
        Predicate<Object> t = Predicates.Nand(Predicates.True(), Predicates.True());
        assertEquals(false, t.test(null));
    } // test1

    public void test2()
     {
        Predicate<Object> t = Predicates.Nand(Predicates.False(), Predicates.True());
        assertEquals(true, t.test(null));
    } // test2

    public void test3()
     {
        Predicate<Object> t = Predicates.Nand(Predicates.True(), Predicates.False());
        assertEquals(true, t.test(null));
    } // test3

    public void test4()
     {
        Predicate<Object> t = Predicates.Nand(Predicates.False(), Predicates.False());
        assertEquals(true, t.test(null));
    } // test4
} // UnaryNandTest
