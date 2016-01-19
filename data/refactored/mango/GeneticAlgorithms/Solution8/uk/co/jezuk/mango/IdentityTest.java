package uk.co.jezuk.mango;

import junit.framework.*;

public class IdentityTest  extends TestCase {
    public IdentityTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(IdentityTest.class); }

    public void test1()
     {
        Function i = Functions.Identity();
        assertEquals(this, Functions.Identity().fn(this));
        assertEquals(null, Functions.Identity().fn(null));
        assertEquals("hello", Functions.Identity().fn("hello"));
        assertEquals(i, Functions.Identity().fn(i));
    } // test1
} // IdentityTest
