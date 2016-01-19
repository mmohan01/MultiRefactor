package uk.co.jezuk.mango;

import junit.framework.*;

public class ConstantTest  extends TestCase {
    public ConstantTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(ConstantTest.class); }

    public void test1()
     {
        Function<String, String> constant = Functions.Constant("woo");
        assertEquals("woo", constant.fn(null));
        assertEquals("woo", constant.fn("hello"));
    } // test1

    public void test2()
     {
        Function<Integer, Integer> constant = Functions.Constant(5);
        assertEquals(new Integer(5), constant.fn(null));
        assertEquals(new Integer(5), constant.fn(12));
    } // test2

    public void test3()
     {
        Function<Integer, String> constant = Functions.Constant("woo");
        assertEquals("woo", constant.fn(null));
        assertEquals("woo", constant.fn(5));
    } // test3
} // ConstantTest
