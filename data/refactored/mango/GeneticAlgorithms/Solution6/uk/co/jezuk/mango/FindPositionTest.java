package uk.co.jezuk.mango;

import junit.framework.*;

import java.util.List;
import java.util.ArrayList;

public class FindPositionTest  extends TestCase {
    List<Integer> list;

    public FindPositionTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(FindPositionTest.class); }

    protected void setUp()
     {
        list = new ArrayList<Integer>();
        for (int i = 0; i < 10; ++i)
         list.add(i);
    } // setUp

    public void test1()
     {
        assertEquals(2, Algorithms.findPosition(list, 2));
    }

    public void test2()
     {
        assertEquals(4, Algorithms.findPosition(list, 4));
    }

    public void test3()
     {
        assertEquals(7, Algorithms.findPosition(list, 7));
    }

    public void test4()
     {
        assertEquals(-1, Algorithms.findPosition(list, 99));
    }

    public void test5()
     {
        assertEquals(-1, Algorithms.findPosition(list, new String("charles dickens")));
    } //
} // FindPositionTest
