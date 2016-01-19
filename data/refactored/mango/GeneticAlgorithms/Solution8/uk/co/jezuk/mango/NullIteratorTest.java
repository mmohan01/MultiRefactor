package uk.co.jezuk.mango;

import junit.framework.*;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class NullIteratorTest extends TestCase {
    public NullIteratorTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(NullIteratorTest.class); }

    public void test1()
     {
        Iterator i = Iterators.NullIterator();
        assertEquals(false, i.hasNext());
        try {
            i.next();
            fail();
        }
         catch (NoSuchElementException e) {
    }
    } // test1
} // NullIteratorTest
