package uk.co.jezuk.mango;

import junit.framework.*;
import java.util.List;
import java.util.ArrayList;


public class RemoveTest  extends TestCase {
    List<Integer> list;

    public RemoveTest(String name) { super(name); }
    public Test suite() { return new TestSuite(RemoveTest.class); }

    protected void setUp()
     {
        list = new ArrayList<Integer>();
        for (int i = 0; i < 10; ++i)
         list.add(i);
    } // setUp

    public void test1()
     {
        Algorithms.remove(list, 7);

        assertEquals(9, list.size());
        assertEquals(null, Algorithms.find(list, 7));
    } // test1

    public void test2()
     {
        Algorithms.remove(list, 1, 5, 7);

        assertEquals(10, list.size());
        assertEquals(list.get(7), Algorithms.find(list, 7));
    } // test2
} // FindTest
