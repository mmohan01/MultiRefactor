package uk.co.jezuk.mango;

import junit.framework.*;

public class ConstantSequenceTest extends TestCase {
    public ConstantSequenceTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(ConstantSequenceTest.class); }

    public void test1()
     {
        Generator<Integer> gen = Generators.ConstantSequence(new Integer(1));
        assertEquals(new Integer(1), gen.fn());
        assertEquals(new Integer(1), gen.fn());
        assertEquals(new Integer(1), gen.fn());
    } // test1

    public void test2()
     {
        Generator<Integer> gen = Generators.ConstantSequence(null);
        assertEquals(null, gen.fn());
        assertEquals(null, gen.fn());
        assertEquals(null, gen.fn());
    } // test2

    public void test3()
     {
        Generator<String> gen = Generators.ConstantSequence("Fruit");
        assertEquals("Fruit", gen.fn());
        assertEquals("Fruit", gen.fn());
        assertEquals("Fruit", gen.fn());
    } // test3
} // class ConstantSequenceTest
