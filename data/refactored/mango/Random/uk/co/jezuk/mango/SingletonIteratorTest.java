package uk.co.jezuk.mango;

import junit.framework.*;
import java.util.Iterator;
import java.util.NoSuchElementException;

public final class SingletonIteratorTest  extends TestCase {
    public SingletonIteratorTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(SingletonIteratorTest.class); }

    public void test1()
     {
        Iterator i = Iterators.SingletonIterator(new String("one"));
        assertEquals(true, i.hasNext());
        assertEquals("one", i.next());
        assertEquals(false, i.hasNext());
    } // test1

    public void test2()
     {
        Iterator i = Iterators.SingletonIterator(null);
        assertEquals(true, i.hasNext());
        assertEquals(null, i.next());
        assertEquals(false, i.hasNext());
        try {
            i.next();
            fail();
        }
         catch (NoSuchElementException e) {
    }
    } // test2
} // SingletonIteratorTest
