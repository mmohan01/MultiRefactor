package uk.co.jezuk.mango;

import junit.framework.*;

import java.util.Iterator;

public class GeneratorIteratorTest extends TestCase {
    public GeneratorIteratorTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(GeneratorIteratorTest.class); }

    public void test1()
     {
        Iterator<Integer> iter = Iterators.GeneratorIterator(Generators.IntegerSequence());
        assertEquals(true, iter.hasNext());
        assertEquals(new Integer(0), iter.next());
        assertEquals(true, iter.hasNext());
        assertEquals(new Integer(1), iter.next());
        assertEquals(true, iter.hasNext());
        assertEquals(new Integer(2), iter.next());
        assertEquals(true, iter.hasNext());
        assertEquals(new Integer(3), iter.next());
    } // test1

    static private final class Foo {
        public String toString() { return "Foo"; }
    }

    static private class Bar extends Foo {
        public String toString() { return "Bar"; }
    }

    public void test2()
     {
        Iterator<Foo> iter = Iterators.GeneratorIterator(Generators.ConstantSequence(new Foo()));
        assertEquals(true, iter.hasNext());
        assertEquals("Foo", iter.next().toString());
        assertEquals(true, iter.hasNext());
        assertEquals("Foo", iter.next().toString());
        assertEquals(true, iter.hasNext());
        assertEquals("Foo", iter.next().toString());
        assertEquals(true, iter.hasNext());
        assertEquals("Foo", iter.next().toString());
    } // test2

    public void test3()
     {
        Iterator<Bar> iter = Iterators.GeneratorIterator(Generators.ConstantSequence(new Bar()));
        assertEquals(true, iter.hasNext());
        assertEquals("Bar", iter.next().toString());
        assertEquals(true, iter.hasNext());
        assertEquals("Bar", iter.next().toString());
        assertEquals(true, iter.hasNext());
        assertEquals("Bar", iter.next().toString());
        assertEquals(true, iter.hasNext());
        assertEquals("Bar", iter.next().toString());
    } // test3

    public void test4()
     {
        Iterator<Foo> iter = Iterators.<Foo>GeneratorIterator(Generators.ConstantSequence(new Bar()));
        assertEquals(true, iter.hasNext());
        assertEquals("Bar", iter.next().toString());
        assertEquals(true, iter.hasNext());
        assertEquals("Bar", iter.next().toString());
        assertEquals(true, iter.hasNext());
        assertEquals("Bar", iter.next().toString());
        assertEquals(true, iter.hasNext());
        assertEquals("Bar", iter.next().toString());
    } // test4
} // class GeneratorIteratorTest
