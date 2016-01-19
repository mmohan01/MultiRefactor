package uk.co.jezuk.mango;

import junit.framework.*;
import java.util.Iterator;
import java.util.List;

public class ZipIteratorTest extends TestCase {
    public ZipIteratorTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(ZipIteratorTest.class); }

    public void test1()
     {
        String[] s = new String[] { "one", "two", "three" };
        String[] m = new String[] { "cat", "dog", "rhino", "fruitbat" };

        Iterator<List<String>> zip = Iterators.ZipIterator(s, m);
        assertTrue(zip.hasNext());
        List<String> l = zip.next();
        assertEquals("one", l.get(0));
        assertEquals("cat", l.get(1));
        assertTrue(zip.hasNext());
        l = zip.next();
        assertEquals("two", l.get(0));
        assertEquals("dog", l.get(1));
        assertTrue(zip.hasNext());
        l = zip.next();
        assertEquals("three", l.get(0));
        assertEquals("rhino", l.get(1));
    }
} // ZipIteratorTest
