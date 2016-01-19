package uk.co.jezuk.mango;

import junit.framework.*;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

public class RemoveIfTest  extends TestCase {
    List<Integer> list;

    public RemoveIfTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(RemoveIfTest.class); }

    protected void setUp()
     {
        list = new ArrayList<Integer>();
        for (int i = 0; i < 10; ++i)
         list.add(i);
    } // setUp

    public void test1()
     {
        Algorithms.removeIf(list, Bind.First(BinaryPredicates.LessThanEquals(), new Integer(7)));

        assertEquals(7, list.size());
        assertEquals(null, Algorithms.find(list, 7));
        assertEquals(null, Algorithms.find(list, 8));
        assertEquals(null, Algorithms.find(list, 9));
    } // test1

    public void test2()
     {
        Algorithms.removeIf(list, 1, 5, Bind.First(BinaryPredicates.LessThanEquals(), 7));

        assertEquals(10, list.size());
        assertEquals(list.get(7), Algorithms.find(list, 7));
    } // test2
} // RemoveIfTest
