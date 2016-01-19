package uk.co.jezuk.mango;

import junit.framework.*;

import java.util.List;
import java.util.ArrayList;
import uk.co.jezuk.mango.algorithms.Unique;

public class UniqueTest extends TestCase {
    public UniqueTest(String name) { super(name);  }

    public static Test suite() { return new TestSuite(UniqueTest.class); }

    protected void setUp() {}

    public void test1()
     {
        List<String> list = new ArrayList<String>();
        Algorithms.unique(list);
    } // test1

    public void test2()
     {
        List<String> list = new ArrayList<String>();
        list.add("one");

        Algorithms.unique(list);
        assertEquals(1, list.size());
    } // test2

    public void test3()
     {
        List<String> list = new ArrayList<String>();
        list.add("one");
        list.add("two");

        Algorithms.unique(list);
        assertEquals(2, list.size());
    } // test3

    public void test4()
     {
        List<String> list = new ArrayList<String>();
        list.add("one");
        list.add("two");
        list.add("three");
        list.add("four");
        list.add("five");

        Algorithms.unique(list);
        assertEquals(5, list.size());
    } // test4

    public void test5()
     {
        List<String> list = new ArrayList<String>();
        list.add("one");
        list.add("two");
        list.add("two");
        list.add("three");
        list.add("four");
        list.add("five");

        Algorithms.unique(list);
        assertEquals(5, list.size());
    } // test5

    public void test6()
     {
        List<String> list = new ArrayList<String>();
        list.add("one");
        list.add("two");
        list.add("two");
        list.add("two");
        list.add("two");
        list.add("two");
        list.add("two");
        list.add("two");
        list.add("two");
        list.add("two");
        list.add("two");
        list.add("two");
        list.add("two");
        list.add("three");
        list.add("four");
        list.add("five");

        Algorithms.unique(list);
        assertEquals(5, list.size());
    } // test6

    public void test7()
     {
        List<String> list = new ArrayList<String>();
        list.add("one");
        list.add("one");
        list.add("two");
        list.add("two");
        list.add("three");
        list.add("three");
        list.add("four");
        list.add("four");
        list.add("five");
        list.add("five");

        Algorithms.unique(list);
        assertEquals(5, list.size());
    } // test7

    public void test8()
     {
        assertTrue(Unique.match(null, null, null));
        assertFalse(Unique.match(null, "hello", null));
        assertFalse(Unique.match(null, null, "hello"));
    } // test8
} // class UniqueTest
