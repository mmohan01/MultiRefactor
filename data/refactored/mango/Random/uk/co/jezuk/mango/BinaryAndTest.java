package uk.co.jezuk.mango;

import junit.framework.*;

public class BinaryAndTest  extends TestCase {
    public BinaryAndTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(BinaryAndTest.class); }

    public void test1()
     {
        BinaryPredicate<Object, Object> o = BinaryPredicates.And(BinaryPredicates.True(), BinaryPredicates.True());
        assertEquals(true, o.test(null, null));
    } // test1

    public void test2()
     {
        BinaryPredicate<Object, Object> o = BinaryPredicates.And(BinaryPredicates.False(), BinaryPredicates.True());
        assertEquals(false, o.test(null, null));
    } // test2

    public void test3()
     {
        BinaryPredicate<Object, Object> o = BinaryPredicates.And(BinaryPredicates.True(), BinaryPredicates.False());
        assertEquals(false, o.test(null, null));
    } // test3

    public void test4()
     {
        BinaryPredicate<Object, Object> o = BinaryPredicates.And(BinaryPredicates.False(), BinaryPredicates.False());
        assertEquals(false, o.test(null, null));
    } // test4
} // BinaryAndTest
