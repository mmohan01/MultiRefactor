package uk.co.jezuk.mango;

import junit.framework.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class BoundedIteratorTest  extends TestCase {
    List<Integer> list;

    public BoundedIteratorTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(BoundedIteratorTest.class); }

    protected void setUp()
     {
        list = new ArrayList<Integer>();
        for (int i = 0; i < 10; ++i)
         list.add(i);
    } // setUp

    public void test1()
     {
        Iterator<Integer> bi = Iterators.BoundedIterator(list, 2, 5);
        assertEquals(true, bi.hasNext());
        assertEquals(new Integer(2), bi.next());
        assertEquals(true, bi.hasNext());
        assertEquals(new Integer(3), bi.next());
        assertEquals(true, bi.hasNext());
        assertEquals(new Integer(4), bi.next());
        assertEquals(false, bi.hasNext());
    } // test1

    public void test2()
     {
        Iterator<Integer> bi = Iterators.BoundedIterator(list, 8, 12);
        assertEquals(true, bi.hasNext());
        assertEquals(new Integer(8), bi.next());
        assertEquals(true, bi.hasNext());
        assertEquals(new Integer(9), bi.next());
        assertEquals(false, bi.hasNext());
    } // test2

    public void test3()
     {
        Iterator<Integer> bi = Iterators.BoundedIterator(list, 12, 12);
        assertEquals(false, bi.hasNext());
    } // test3

    public void test4()
     {
        Iterator<Integer> bi = Iterators.BoundedIterator(list.iterator(), 2, 5);
        assertEquals(true, bi.hasNext());
        assertEquals(new Integer(2), bi.next());
        assertEquals(true, bi.hasNext());
        assertEquals(new Integer(3), bi.next());
        assertEquals(true, bi.hasNext());
        assertEquals(new Integer(4), bi.next());
        assertEquals(false, bi.hasNext());
    } // test4

    public void test5()
     {
        Iterator<Integer> bi = Iterators.BoundedIterator(list.iterator(), 8, 12);
        assertEquals(true, bi.hasNext());
        assertEquals(new Integer(8), bi.next());
        assertEquals(true, bi.hasNext());
        assertEquals(new Integer(9), bi.next());
        assertEquals(false, bi.hasNext());
    } // test5

    public void test6()
     {
        Iterator<Integer> bi = Iterators.BoundedIterator(list.iterator(), 12, 12);
        assertEquals(false, bi.hasNext());
    } // test6

    public void test7()
     {
        try {
            Iterator<Integer> bi =  Iterators.BoundedIterator(list.iterator(), -1, 12);
            fail();
        }
         catch (IndexOutOfBoundsException e)
         {
    } // catch
    }

    public void test8()
     {
        try {
            Iterator<Integer> bi =  Iterators.BoundedIterator(list.iterator(), 12, -12);
            fail();
        }
         catch (IndexOutOfBoundsException e)
         {
    } // catch
    }

    public void test9()
     {
        try {
            Iterator<Integer> bi =  Iterators.BoundedIterator(list.iterator(), 12, 6);
            fail();
        }
         catch (IndexOutOfBoundsException e)
         {
    } // catch
    }
} // BoundedIteratorTest
