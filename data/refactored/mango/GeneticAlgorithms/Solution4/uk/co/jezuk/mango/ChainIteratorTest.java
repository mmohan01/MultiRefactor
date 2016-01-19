package uk.co.jezuk.mango;

import junit.framework.*;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public class ChainIteratorTest extends TestCase {
    public ChainIteratorTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(ChainIteratorTest.class); }

    public void test0()
     {
        Iterator<String> i = Iterators.ChainIterator();
        assertEquals(false, i.hasNext());
    } // test0

    public void test1()
     {
        Iterator<String> i = Iterators.ChainIterator(Iterators.NullIterator());
        assertEquals(false, i.hasNext());
    } // test1

    public void test2()
     {
        Iterator<String> i = Iterators.ChainIterator("fish");
        assertEquals(true, i.hasNext());
        assertEquals("fish", i.next());
        assertEquals(false, i.hasNext());
    } // test2

    public void testWithNull()
     {
        Iterator<String> i = Iterators.ChainIterator((Object)null);
        assertEquals(true, i.hasNext());
        assertEquals(null, i.next());
        assertEquals(false, i.hasNext());
    } // test2

    public void test3()
     {
        Iterator<String> i = Iterators.ChainIterator("fish", "wish");
        assertEquals(true, i.hasNext());
        assertEquals("fish", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("wish", i.next());
        assertEquals(false, i.hasNext());
    } // test3

    public void test3a()
     {
        Iterator<String> i = Iterators.ChainIterator("fish", null);
        assertEquals(true, i.hasNext());
        assertEquals("fish", i.next());
        assertEquals(true, i.hasNext());
        assertEquals(null, i.next());
        assertEquals(false, i.hasNext());
    } // test3a

    public void test3b()
     {
        Iterator<String> i = Iterators.ChainIterator(null, "wish");
        assertEquals(true, i.hasNext());
        assertEquals(null, i.next());
        assertEquals(true, i.hasNext());
        assertEquals("wish", i.next());
        assertEquals(false, i.hasNext());
    } // test3b

    public void test4()
     {
        List<String> l = new ArrayList<String>();
        l.add("fish");
        l.add("wish");
        Iterator<String> i = Iterators.ChainIterator(l);
        assertEquals(true, i.hasNext());
        assertEquals("fish", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("wish", i.next());
        assertEquals(false, i.hasNext());
    } // test4

    public void test5()
     {
        List<String> l = new ArrayList<String>();
        l.add("fish");
        l.add("wish");
        Iterator<String> i = Iterators.ChainIterator(l.iterator());
        assertEquals(true, i.hasNext());
        assertEquals("fish", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("wish", i.next());
        assertEquals(false, i.hasNext());
    } // test5

    public void test6()
     {
        List<String> l = new ArrayList<String>();
        l.add("fish");
        l.add("wish");
        Iterator<String> i = Iterators.ChainIterator(l, l);
        assertEquals(true, i.hasNext());
        assertEquals("fish", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("wish", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("fish", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("wish", i.next());
        assertEquals(false, i.hasNext());
    } // test6


    public void test7()
     {
        List<String> l = new ArrayList<String>();
        l.add("fish");
        l.add("wish");
        Iterator<String> i = Iterators.ChainIterator(l, "tree", "free");
        assertEquals(true, i.hasNext());
        assertEquals("fish", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("wish", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("tree", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("free", i.next());
        assertEquals(false, i.hasNext());
    } // test7

    public void test8()
     {
        List<String> l = new ArrayList<String>();
        l.add("fish");
        l.add("wish");
        Iterator<String> i = Iterators.ChainIterator("tree", "free", l);
        assertEquals(true, i.hasNext());
        assertEquals("tree", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("free", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("fish", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("wish", i.next());
        assertEquals(false, i.hasNext());
    } // test8

    public void test9()
     {
        List<String> l = new ArrayList<String>();
        l.add("fish");
        l.add("wish");
        Iterator<String> i = Iterators.ChainIterator("tree", l, "free");
        assertEquals(true, i.hasNext());
        assertEquals("tree", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("fish", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("wish", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("free", i.next());
        assertEquals(false, i.hasNext());
    } // test9

    public void test10()
     {
        String[] l = new String[] { "fish", "wish" };
        Iterator<String> i = Iterators.ChainIterator((Object)l);
        assertEquals(true, i.hasNext());
        assertEquals("fish", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("wish", i.next());
        assertEquals(false, i.hasNext());
    } // test10

    public void test11()
     {
        String[] l = new String[] { "fish", "wish" };
        Iterator<String> i = Iterators.ChainIterator(l, l);
        assertEquals(true, i.hasNext());
        assertEquals("fish", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("wish", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("fish", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("wish", i.next());
        assertEquals(false, i.hasNext());
    } // test11

    public void test12()
     {
        String[] l = new String[] { "fish", "wish" };
        Iterator<String> i = Iterators.ChainIterator(l, "tree", "free");
        assertEquals(true, i.hasNext());
        assertEquals("fish", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("wish", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("tree", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("free", i.next());
        assertEquals(false, i.hasNext());
    } // test12

    public void test13()
     {
        String[] l = new String[] { "fish", "wish" };
        Iterator<String> i = Iterators.ChainIterator("tree", "free", l);
        assertEquals(true, i.hasNext());
        assertEquals("tree", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("free", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("fish", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("wish", i.next());
        assertEquals(false, i.hasNext());
    } // test13

    public void test14()
     {
        String[] l = new String[] { "fish", "wish" };
        Iterator<String> i = Iterators.ChainIterator("tree", l, "free");
        assertEquals(true, i.hasNext());
        assertEquals("tree", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("fish", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("wish", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("free", i.next());
        assertEquals(false, i.hasNext());
    } // test14

    public void test15()
     {
        String[] l = new String[] { "fish", "wish" };
        List<String> p = new ArrayList<String>();
        p.add("frog");
        p.add("bog");
        Iterator<String> i = Iterators.ChainIterator("tree", l, p, "free");
        assertEquals(true, i.hasNext());
        assertEquals("tree", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("fish", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("wish", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("frog", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("bog", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("free", i.next());
        assertEquals(false, i.hasNext());
    } // test15

    public void test16()
     {
        Iterator<String> i = Iterators.ChainIterator(Iterators.NullIterator(), Iterators.NullIterator(), Iterators.NullIterator(), Iterators.NullIterator());
        assertEquals(false, i.hasNext());
    } // test16

    public void test17()
     {
        Iterator<String> i = Iterators.ChainIterator(Iterators.NullIterator(), Iterators.NullIterator(), Iterators.NullIterator(), Iterators.NullIterator(), "result");
        assertEquals(true, i.hasNext());
        assertEquals("result", i.next());
        assertEquals(false, i.hasNext());
    } // test17

    public void test18()
     {
        String[] l = new String[] { null, "wish" };
        Iterator<String> i = Iterators.ChainIterator("tree", l, "free");
        assertEquals(true, i.hasNext());
        assertEquals("tree", i.next());
        assertEquals(true, i.hasNext());
        assertEquals(null, i.next());
        assertEquals(true, i.hasNext());
        assertEquals("wish", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("free", i.next());
        assertEquals(false, i.hasNext());
    } // test18

    public void test19()
     {
        String[] l = new String[] { "fish", "wish" };
        Iterator<String> i = Iterators.ChainIterator("tree", l, null);
        assertEquals(true, i.hasNext());
        assertEquals("tree", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("fish", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("wish", i.next());
        assertEquals(true, i.hasNext());
        assertEquals(null, i.next());
        assertEquals(false, i.hasNext());
    } // test19

    public void test20()
     {
        String[] l = new String[] { "fish", "wish" };
        Iterator<String> i = Iterators.ChainIterator(null, l, null);
        assertEquals(true, i.hasNext());
        assertEquals(null, i.next());
        assertEquals(true, i.hasNext());
        assertEquals("fish", i.next());
        assertEquals(true, i.hasNext());
        assertEquals("wish", i.next());
        assertEquals(true, i.hasNext());
        assertEquals(null, i.next());
        assertEquals(false, i.hasNext());
    } // test20
} // ChainIteratorTest
