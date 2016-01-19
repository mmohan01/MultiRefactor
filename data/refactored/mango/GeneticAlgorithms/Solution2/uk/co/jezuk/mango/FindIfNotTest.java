package uk.co.jezuk.mango;

import junit.framework.*;

import java.util.List;
import java.util.ArrayList;

public class FindIfNotTest  extends TestCase {
    List<Integer> list;

    public FindIfNotTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(FindIfNotTest.class); }

    protected void setUp()
     {
        list = new ArrayList<Integer>();
        for (int i = 0; i < 10; ++i)
         list.add(i);
    } // setUp

    public void test1()
     {
        assertEquals(list.get(2), Algorithms.findIfNot(list, Bind.First(BinaryPredicates.GreaterThanEquals(), 1)));
    } // test1

    public void test2()
     {
        assertEquals(list.get(4), Algorithms.findIfNot(list, Bind.First(BinaryPredicates.GreaterThan(), 4)));
    } // test2

    public void test3()
     {
        assertEquals(list.get(7), Algorithms.findIfNot(list, Bind.First(BinaryPredicates.GreaterThan(), 7)));
    } // test3

    public void test4()
     {
        assertEquals(null, Algorithms.findIfNot(list, Bind.First(BinaryPredicates.GreaterThan(), 99)));
    } // test4

    public void test5()
     {
        assertEquals(null, Algorithms.findIfNot(list, Bind.First(BinaryPredicates.NotEqualTo(), new String("charles dickens"))));
    } // test5

    public void test6()
     {
        assertEquals(list.get(0), Algorithms.findIfNot(list, Bind.First(BinaryPredicates.EqualTo(), new String("charles dickens"))));
    } //
} // FindIfNotTest
