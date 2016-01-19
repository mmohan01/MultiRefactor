package uk.co.jezuk.mango;

import junit.framework.*;
import java.util.Iterator;

public class StringIteratorTest extends TestCase {
    public StringIteratorTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(StringIteratorTest.class); }

    public void test1()
     {
        Iterator i = Iterators.StringIterator(null);
        assertEquals(false, i.hasNext());
    } // test1

    public void test2()
     {
        Iterator i = Iterators.StringIterator("");
        assertEquals(false, i.hasNext());
    } // test2

    public void test3()
     {
        Iterator i = Iterators.StringIterator("123");
        assertEquals(true, i.hasNext());
        assertEquals("1", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("2", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("3", i.next());
        assertEquals(false, i.hasNext());
    } // test3
} // class StringIteratorTest
