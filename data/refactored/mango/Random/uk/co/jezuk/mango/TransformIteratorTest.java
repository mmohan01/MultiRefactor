package uk.co.jezuk.mango;

import junit.framework.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class TransformIteratorTest  extends TestCase {
    List<Integer> list;

    public TransformIteratorTest(String name) { super(name); }
    public Test suite() { return new TestSuite(TransformIteratorTest.class); }

    protected void setUp()
     {
        list = new ArrayList<Integer>();
        for (int i = 0; i < 10; ++i)
         list.add(i);
    } // setUp

    class DoubleUp implements Function<Integer, Integer> {
        public Integer fn(Integer i)
         {
            return (i * 2);
        } // test
    }

    public void test1()
     {
        Iterator<Integer> iter = Iterators.TransformIterator(list.iterator(), new DoubleUp());

        int i = 0;
        while (iter.hasNext())
         {
            Integer n = iter.next();
            assertEquals(n.intValue(), i);
            i += 2;
        } // while ...
    } // test1

    class NameObject {
        public NameObject(String name) { name_ = name; }

        public String getName() { return name_; }

        private String name_;
    } // NameObject

    public void test2()
     {
        List<NameObject> slist = new ArrayList<NameObject>();
        slist.add(new NameObject("hawkeye pierce"));
        slist.add(new NameObject("sacremento"));
        slist.add(new NameObject("GOBBLE"));
        slist.add(new NameObject("SINGLETON"));
        slist.add(new NameObject("BILBO"));
        NameObject theOneIWant = new NameObject("CORGAN");
        slist.add(theOneIWant);
        slist.add(new NameObject("ERNEST"));
        slist.add(new NameObject("DAVID"));
        slist.add(new NameObject("BILLY"));
        slist.add(new NameObject("SCAGGS"));
        slist.add(new NameObject("CHARLES"));
        slist.add(new NameObject("SIMEON"));

    // find the object called CORGAN
        String found = Algorithms.find(Iterators.TransformIterator(slist.iterator(),
                                   Adapt.ArgumentMethod("getName", NameObject.class, String.class)),
                   new String("CORGAN"));
        assertEquals("CORGAN", found);
    } // test2
} //
