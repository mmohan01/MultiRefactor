package uk.co.jezuk.mango;

import junit.framework.*;

public class LessThanTest  extends TestCase {
    public LessThanTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(LessThanTest.class); }

    private BinaryPredicate<Integer, Integer> p_;
    private BinaryPredicate<String, String> p2_;

    protected void setUp()
     {
        p_ = BinaryPredicates.LessThan();
        p2_ = BinaryPredicates.LessThan();
    } // setUp

    public void test1()
     {
        assertEquals(true, p_.test(1, 3));
    } // test1

    public void test2()
     {
        assertEquals(true, p2_.test("hello", "zibignew"));
    } // test2

    public void test3()
     {
        assertEquals(false, p_.test(3, 1));
    } // test3

    public void test4()
     {
        assertEquals(false, p2_.test("hello", "aerosol"));
    } // test4

    public void test5()
     {
        assertEquals(false, p_.test(3, 3));
    } // test5

    public void test6()
     {
        assertEquals(false, p2_.test("hello", "hello"));
    } // test6

    public void test7()
     {
        assertEquals(false, p2_.test(null, null));
    } // test7

    public void test8()
     {
        assertEquals(true, p2_.test(null, "word"));
    } // test8

    public void test9()
     {
        assertEquals(false, p2_.test("word", null));
    } // test9
} // LessThanTest
