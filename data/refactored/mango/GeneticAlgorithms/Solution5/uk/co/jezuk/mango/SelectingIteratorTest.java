package uk.co.jezuk.mango;

import junit.framework.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class SelectingIteratorTest  extends TestCase {
    List<Integer> list;
    List<String> slist;

    public SelectingIteratorTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(SelectingIteratorTest.class); }

    protected void setUp()
     {
        list = new ArrayList<Integer>();
        for (int i = 0; i < 10; ++i)
         list.add(i);
    } // setUp

    class LessThanFive implements Predicate<Integer> {
        public boolean test(Integer i)
         {
            return i.intValue() < 5;
        } // test
    }

    public void test1()
     {
        Iterator<Integer> iter = Iterators.SelectingIterator(list.iterator(), new LessThanFive());
        int i = 0;
        while (iter.hasNext())
         {
            ++i;
            iter.next();
        } // while ...

        assertEquals(
        5, i);
    } // test1

    public void test2()
     {
        slist = new ArrayList<String>();
        slist.add("hawkeye pierce");
        slist.add("sacremento");
        slist.add("GOBBLE");
        slist.add("SINGLETON");
        slist.add("BILBO");
        slist.add("ERNEST");
        slist.add("DAVID");
        slist.add("BILLY");
        slist.add("SCAGGS");
        slist.add("CHARLES");
        slist.add("SIMEON");

        Iterator<String> iter = Iterators.SelectingIterator(slist.iterator(),
                                       new Predicate<String>(){
                                           public boolean test(String s) {
                                             return s.charAt(0) == 'S';
            }
        });
        int i = 0;
        while (iter.hasNext())
         {
            ++i;
            String s = iter.next();
            assertEquals(true, s.startsWith("S"));
        } // while
        assertEquals(
        3, i);

        try {
            iter.next();
            fail();
        }
         catch (NoSuchElementException e) {
    } // test2
    }

    public void test3()
     {
        slist = new ArrayList<String>();
        slist.add("hawkeye pierce");
        slist.add("sacremento");
        slist.add("GOBBLE");
        slist.add("SINGLETON");
        slist.add("BILBO");
        slist.add("ERNEST");
        slist.add("DAVID");
        slist.add("BILLY");
        slist.add("SCAGGS");
        slist.add("CHARLES");
        slist.add("SIMEON");

        Iterator<String> iter = Iterators.SelectingIterator(slist.iterator(),
                                       new Predicate<String>(){
                                           public boolean test(String s) {
                                             return s.charAt(0) == 'S';
            }
        });
        while (iter.hasNext())
         iter.remove();

        assertEquals(8, slist.size());
        assertEquals("hawkeye pierce", slist.get(0));
        assertEquals("sacremento", slist.get(1));
        assertEquals("GOBBLE", slist.get(2));
        assertEquals("BILBO", slist.get(3));
        assertEquals("ERNEST", slist.get(4));
        assertEquals("DAVID", slist.get(5));
        assertEquals("BILLY", slist.get(6));
        assertEquals("CHARLES", slist.get(7));
    } // test3
} //
