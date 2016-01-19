package uk.co.jezuk.mango;

import junit.framework.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class SkippingIteratorTest  extends TestCase {
    List<Integer> list;
    List<String> slist;

    public SkippingIteratorTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(SkippingIteratorTest.class); }

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
        Iterator<Integer> iter = Iterators.SkippingIterator(list.iterator(), new LessThanFive());

        assertEquals(new Integer(5), iter.next());
        assertEquals(new Integer(6), iter.next());
        assertEquals(new Integer(7), iter.next());
        assertEquals(new Integer(8), iter.next());
        assertEquals(new Integer(9), iter.next());
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

        Iterator<String> iter = Iterators.SkippingIterator(slist.iterator(),
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
            assertEquals(false, s.startsWith("S"));
        } // while
        assertEquals(
        8, i);
    } // test2
} //
