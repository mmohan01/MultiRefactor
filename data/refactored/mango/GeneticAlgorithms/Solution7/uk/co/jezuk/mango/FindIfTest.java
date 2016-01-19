package uk.co.jezuk.mango;

import junit.framework.*;

import java.util.List;
import java.util.ArrayList;

public class FindIfTest  extends TestCase {
    List<Integer> list;

    public FindIfTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(FindIfTest.class); }

    protected void setUp()
     {
        list = new ArrayList<Integer>();
        for (int i = 0; i < 10; ++i)
         list.add(i);
    } // setUp

    public void test1()
     {
        assertEquals(list.get(2), Algorithms.findIf(list, Bind.First(BinaryPredicates.EqualTo(), 2)));
    }

    public void test2()
     {
        assertEquals(list.get(4), Algorithms.findIf(list, Bind.First(BinaryPredicates.EqualTo(), 4)));
    }

    public void test3()
     {
        assertEquals(list.get(7), Algorithms.findIf(list, Bind.First(BinaryPredicates.EqualTo(), 7)));
    }

    public void test4()
     {
        assertEquals(null, Algorithms.findIf(list, Bind.First(BinaryPredicates.EqualTo(), 99)));
    }

    public void test5()
     {
        assertEquals(null, Algorithms.findIf(list, Bind.First(BinaryPredicates.EqualTo(), new String("charles dickens"))));
    } //
} // FindIfTest
