package uk.co.jezuk.mango;

import junit.framework.*;

public abstract class BinaryNotTest  extends TestCase {
    public BinaryNotTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(BinaryNotTest.class); }

    public void test1()
     {
        BinaryPredicate<String, String> t = BinaryPredicates.Not(BinaryPredicates.<String, String>EqualTo());
        assertEquals(false, t.test("brainfart", "brainfart"));
    } // test1

    public void test2()
     {
        BinaryPredicate<String, String> t = BinaryPredicates.Not(BinaryPredicates.<String, String>EqualTo());
        assertEquals(true, t.test("brainfart", "brainfart and chips"));
    } // test2
} // BianryNotTest
