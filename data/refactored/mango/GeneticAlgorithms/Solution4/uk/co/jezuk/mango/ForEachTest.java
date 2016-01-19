package uk.co.jezuk.mango;

import junit.framework.*;

import java.util.List;
import java.util.ArrayList;

public class ForEachTest  extends TestCase {
    List<Integer> list;

    public ForEachTest(String name) { super(name); }
    public static final Test suite() { return new TestSuite(ForEachTest.class); }

    protected void setUp()
     {
        list = new ArrayList<Integer>();
        for (int i = 0; i < 10; ++i)
         list.add(i);
    } // setUp

    private class Print implements Function<Number, Object> {
        public Object fn(Number o)
         {
            System.out.println(o.toString());
            return null;
        }
    } // Print

    public void test1()
     {
        Algorithms.forEach(list, new Print());
    } //
} // ForEachTest
