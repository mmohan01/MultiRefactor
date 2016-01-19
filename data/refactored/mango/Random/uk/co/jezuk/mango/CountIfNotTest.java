package uk.co.jezuk.mango;

import junit.framework.*;

import java.util.List;
import java.util.ArrayList;

public abstract class CountIfNotTest  extends TestCase {
    List<Integer> list;

    public CountIfNotTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(CountIfNotTest.class); }

    protected void setUp()
     {
        list = new ArrayList<Integer>();
        for (int i = 0; i < 10; ++i)
         list.add(i);
    } // setUp

    public void test1()
     {
        assertEquals(10, Algorithms.countIfNot(list, Bind.First(BinaryPredicates.EqualTo(), new String("hello"))));
        assertEquals(10, Algorithms.countIfNot(list, Bind.First(BinaryPredicates.EqualTo(), null)));
        assertEquals(9, Algorithms.countIfNot(list, Bind.First(BinaryPredicates.EqualTo(), 5)));
        list.add(5);
        list.add(5);
        list.add(5);
        list.add(5);
        assertEquals(9, Algorithms.countIfNot(list, Bind.First(BinaryPredicates.EqualTo(), 5)));
    } //

    public void test2()
     {
        assertEquals(0, Algorithms.countIfNot(list, Bind.First(BinaryPredicates.NotEqualTo(), new String("hello"))));
        assertEquals(0, Algorithms.countIfNot(list, Bind.First(BinaryPredicates.NotEqualTo(), null)));
        assertEquals(1, Algorithms.countIfNot(list, Bind.First(BinaryPredicates.NotEqualTo(), 5)));
        list.add(5);
        list.add(5);
        list.add(5);
        list.add(5);
        assertEquals(5, Algorithms.countIfNot(list, Bind.First(BinaryPredicates.NotEqualTo(), 5)));
    } //
} // CountIfNotTest
