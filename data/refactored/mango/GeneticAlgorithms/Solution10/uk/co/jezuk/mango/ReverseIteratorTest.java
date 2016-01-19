package uk.co.jezuk.mango;

import junit.framework.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class ReverseIteratorTest extends TestCase {
    List<Integer> list;

    public ReverseIteratorTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(ReverseIteratorTest.class); }

    protected void setUp()
     {
        list = new ArrayList<Integer>();
        for (int i = 0; i < 10; ++i)
         list.add(i);
    } // setUp

    public void test1()
     {
        Iterator<Integer> iter = Iterators.ReverseIterator(list);
        for (int i = 9; i >= 0; --i)
         {
            assertTrue(iter.hasNext());
            assertEquals(list.get(i), iter.next());
        }
        assertFalse(iter.hasNext());
    }
}
