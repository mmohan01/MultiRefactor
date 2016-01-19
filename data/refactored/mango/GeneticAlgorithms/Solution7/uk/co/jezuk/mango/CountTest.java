package uk.co.jezuk.mango;

import junit.framework.*;

import java.util.List;
import java.util.ArrayList;

public class CountTest  extends TestCase {
    List<Integer> list;

    public CountTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(CountTest.class); }

    protected void setUp()
     {
        list = new ArrayList<Integer>();
        for (int i = 0; i < 10; ++i)
         list.add(i);
    } // setUp

    public void test1()
     {
        assertEquals(0, Algorithms.count(list, new String("hello")));
        assertEquals(0, Algorithms.count(list, null));
        assertEquals(1, Algorithms.count(list, 5));
        list.add(5);
        list.add(5);
        list.add(5);
        list.add(5);
        assertEquals(5, Algorithms.count(list, 5));
    } //
} // CountTest
