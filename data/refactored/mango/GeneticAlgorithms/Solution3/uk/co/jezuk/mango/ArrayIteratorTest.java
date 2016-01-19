package uk.co.jezuk.mango;

import junit.framework.*;
import java.util.NoSuchElementException;
import java.util.Iterator;

public class ArrayIteratorTest  extends TestCase {
    public ArrayIteratorTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(ArrayIteratorTest.class); }

    public void test1()
     {
        Iterator i = Iterators.ArrayIterator(new String[] { "one", "two", "three" });
        assertEquals(true, i.hasNext());
        assertEquals("one", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("two", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("three", i.next());
        assertEquals(false, i.hasNext());
        try {
            i.next();
            fail();
        }
         catch (NoSuchElementException e)
         {
    } // test1
    }

    public void test2()
     {
        Iterator i = Iterators.ArrayIterator(null);
        assertEquals(false, i.hasNext());
        try {
            i.next();
            fail();
        }
         catch (NoSuchElementException e)
         {
    } // catch
    } // test2
} // ArrayIteratorTest
