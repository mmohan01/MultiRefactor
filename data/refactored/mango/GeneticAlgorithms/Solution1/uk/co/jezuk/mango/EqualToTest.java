package uk.co.jezuk.mango;

import junit.framework.*;

public class EqualToTest  extends TestCase {
    public EqualToTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(EqualToTest.class); }

    private BinaryPredicate<Integer, Integer> p_;
    private BinaryPredicate<String, String> p2_;

    protected void setUp()
     {
        p_ = BinaryPredicates.EqualTo();
        p2_ = BinaryPredicates.EqualTo();
    } // setUp

    public void test1()
     {
        assertEquals(false, p_.test(1, 3));
    } // test1

    public void test2()
     {
        assertEquals(false, p2_.test("hello", "zibignew"));
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
        assertEquals(true, p_.test(3, 3));
    } // test5

    public void test6()
     {
        assertEquals(true, p2_.test("hello", "hello"));
    } // test6

    public void test7()
     {
        assertEquals(true, p2_.test(null, null));
    } // test7

    public void test8()
     {
        assertEquals(false, p2_.test(null, "word"));
    } // test8

    public void test9()
     {
        assertEquals(false, p2_.test("word", null));
    } // test9
} // EqualsToTest
