package uk.co.jezuk.mango;

import junit.framework.*;
import java.util.List;
import java.util.ArrayList;

public class TransformTest extends TestCase {
    public TransformTest(String name)
     {
        super(name);
    } // TransformTest

    public static Test suite()
     {
        return new TestSuite(TransformTest.class);
    } // suite

    public void test1()
     {
        List<Integer> in = new ArrayList<Integer>();
        in.add(1);
        in.add(2);
        in.add(3);
        List<Integer> out = Algorithms.transform(in.iterator(), new Square());
        assertEquals(3, out.size());
        assertEquals(1, out.get(0).intValue());
        assertEquals(4, out.get(1).intValue());
        assertEquals(9, out.get(2).intValue());
    } // test1

    public class Square implements Function<Integer, Integer> {
        public Integer fn(Integer x)
         {
            return x * x;
        } // fn
    } // Square

    public void test2()
     {
        List<String> in = new ArrayList<String>();
        in.add("A");
        in.add("B");
        in.add("C");
        List<String> out = Algorithms.transform(in.iterator(), new Duplicator());
        assertEquals(3, out.size());
        assertEquals("AA", out.get(0));
        assertEquals("BB", out.get(1));
        assertEquals("CC", out.get(2));
    } // test2

    public class Duplicator implements Function<String, String> {
        public String fn(String x)
         {
            return x + x;
        } // fn
    } // Duplicator

    public
     final void test3()
     {
        List<Foo> fooList = new ArrayList<Foo>();
        fooList.add(new Foo());
        fooList.add(new Foo());
        List<Baz> out = Algorithms.transform(fooList, new Bazifier());
        assertEquals(2, out.size());
    }

    public void test5()
     {
        List<Foo> fooList = new ArrayList<Foo>();
        fooList.add(new Foo());
        fooList.add(new Foo());
        List<Bar> out = Algorithms.transform(fooList, new Bazifier(), new ArrayList<Bar>());
        assertEquals(2, out.size());
    }

    class Foo {
    }
    class Bar {
    }
    class Baz extends Bar {
    }
    class Bazifier implements Function<Foo, Baz> {
        public Baz fn(Foo foo) { return new Baz(); }
    } // an
} // TransformTest
