package uk.co.jezuk.mango;

import junit.framework.*;

public class NullSequenceTest extends TestCase {
    public NullSequenceTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(NullSequenceTest.class); }

    public void test1()
     {
        Generator<String> gen = Generators.NullSequence();
        assertEquals(null, gen.fn());
        assertEquals(null, gen.fn());
        assertEquals(null, gen.fn());
    } // test1

    public void test2()
     {
        Generator<Integer> gen = Generators.NullSequence();
        assertEquals(null, gen.fn());
        assertEquals(null, gen.fn());
        assertEquals(null, gen.fn());
    } // test2
} // class NullSequenceTest
