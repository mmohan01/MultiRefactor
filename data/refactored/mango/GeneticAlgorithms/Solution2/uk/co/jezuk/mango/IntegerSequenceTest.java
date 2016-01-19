package uk.co.jezuk.mango;

import junit.framework.*;

public class IntegerSequenceTest  extends TestCase {
    public IntegerSequenceTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(IntegerSequenceTest.class); }

    public void test1()
     {
        Generator<Integer> gen = Generators.IntegerSequence();
        assertEquals(new Integer(0), gen.fn());
        assertEquals(new Integer(1), gen.fn());
        assertEquals(new Integer(2), gen.fn());
        assertEquals(new Integer(3), gen.fn());
        assertEquals(new Integer(4), gen.fn());
    } // test1

    public void test2()
     {
        Generator<Integer> gen = Generators.IntegerSequence(5);
        assertEquals(new Integer(5), gen.fn());
        assertEquals(new Integer(6), gen.fn());
        assertEquals(new Integer(7), gen.fn());
        assertEquals(new Integer(8), gen.fn());
        assertEquals(new Integer(9), gen.fn());
    } // test2

    public void test3()
     {
        Generator<Integer> gen = Generators.IntegerSequence(new Integer(5));
        assertEquals(new Integer(5), gen.fn());
        assertEquals(new Integer(6), gen.fn());
        assertEquals(new Integer(7), gen.fn());
        assertEquals(new Integer(8), gen.fn());
        assertEquals(new Integer(9), gen.fn());
    } // test3
} // IntegerSequenceTest
