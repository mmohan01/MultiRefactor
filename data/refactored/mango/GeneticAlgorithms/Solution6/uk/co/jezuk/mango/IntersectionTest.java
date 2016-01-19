package uk.co.jezuk.mango;

import junit.framework.*;

import java.util.List;
import java.util.ArrayList;

public class IntersectionTest extends TestCase {
    public IntersectionTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(IntersectionTest.class); }

    public void test1()
     {
        List<String> l1 = new ArrayList<String>();
        Algorithms.intersection(l1, l1, l1);

        assertEquals(0, l1.size());
    } // test1

    public void test2()
     {
        List<String> l1 = new ArrayList<String>();
        l1.add("1");
        l1.add("2");
        l1.add("3");
        List<String> i = new ArrayList<String>();
        Algorithms.intersection(l1, l1, i);

        assertEquals(3, i.size());
        assertEquals("1", i.get(0));
        assertEquals("2", i.get(1));
        assertEquals("3", i.get(2));
    } // test2

    public void test3()
     {
        List<String> l1 = new ArrayList<String>();
        l1.add("1");
        l1.add("2");
        l1.add("3");
        List<String> l2 = new ArrayList<String>();
        l2.add("1");
        l2.add("2");
        l2.add("3");
        List<String> i = new ArrayList<String>();
        Algorithms.intersection(l1, l2, i);

        assertEquals(3, i.size());
        assertEquals("1", i.get(0));
        assertEquals("2", i.get(1));
        assertEquals("3", i.get(2));
    } // test3

    public void test4()
     {
        List<String> l1 = new ArrayList<String>();
        l1.add("1");
        l1.add("2");
        l1.add("3");
        List<String> l2 = new ArrayList<String>();
        l2.add("1");
        l2.add("3");
        List<String> i = new ArrayList<String>();
        Algorithms.intersection(l1, l2, i);

        assertEquals(2, i.size());
        assertEquals("1", i.get(0));
        assertEquals("3", i.get(1));
    } // test4

    public void test5()
     {
        List<String> l1 = new ArrayList<String>();
        l1.add("1");
        l1.add("2");
        l1.add("3");
        List<String> l2 = new ArrayList<String>();
        l2.add("2");
        List<String> i = new ArrayList<String>();
        Algorithms.intersection(l1, l2, i);

        assertEquals(1, i.size());
        assertEquals("2", i.get(0));
    } // test5

    public void test6()
     {
        List<String> l1 = new ArrayList<String>();
        l1.add("1");
        l1.add("2");
        l1.add("3");
        List<String> l2 = new ArrayList<String>();
        l2.add("2");
        l2.add("1");
        List<String> i = new ArrayList<String>();
        Algorithms.intersection(l1, l2, i);

        assertEquals(2, i.size());
        assertEquals("1", i.get(0));
        assertEquals("2", i.get(1));
    } // test6

    public void test7()
     {
        List<String> l1 = new ArrayList<String>();
        l1.add("1");
        l1.add("3");
        List<String> l2 = new ArrayList<String>();
        l2.add("1");
        l2.add("2");
        l2.add("3");
        List<String> i = new ArrayList<String>();
        Algorithms.intersection(l1, l2, i);

        assertEquals(2, i.size());
        assertEquals("1", i.get(0));
        assertEquals("3", i.get(1));
    } // test7

    public void test8()
     {
        List<String> l1 = new ArrayList<String>();
        l1.add("2");
        List<String> l2 = new ArrayList<String>();
        l2.add("1");
        l2.add("2");
        l2.add("3");
        List<String> i = new ArrayList<String>();
        Algorithms.intersection(l1, l2, i);

        assertEquals(1, i.size());
        assertEquals("2", i.get(0));
    } // test8

    public void test9()
     {
        List<String> l1 = new ArrayList<String>();
        l1.add("1");
        l1.add("2");
        l1.add("3");
        List<String> l2 = new ArrayList<String>();
        l2.add("4");
        l2.add("5");
        l2.add("6");
        List<String> i = new ArrayList<String>();
        Algorithms.intersection(l1, l2, i);

        assertEquals(0, i.size());
    } // test9
} // class IntersectionTest
