package uk.co.jezuk.mango;

import junit.framework.*;
import java.util.List;
import java.util.Iterator;

public class UnaryAnyTest extends TestCase {
    public UnaryAnyTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(UnaryAnyTest.class); }

    public void test1()
     {
        Predicate<Object> t = Predicates.Any(Predicates.True());
        assertEquals(true, t.test(null));
    } // test1

    public void test2()
     {
        Predicate<Object> t = Predicates.Any(Predicates.False());
        assertEquals(false, t.test(null));
    } // test2

    @
    SuppressWarnings("unchecked")
     public void test3()
     {
        List<Predicate<Object>> p = Collections.list(Predicates.True(), Predicates.False());

        for (Iterator<Predicate<Object>> outer = p.iterator(); outer.hasNext();)
         {
            Predicate<Object> a = outer.next();
            for (Iterator<Predicate<Object>> inner = p.iterator(); inner.hasNext();)
             {
                Predicate<Object> b = inner.next();
                Predicate<Object> and = Predicates.Or(a, b);
                Predicate<Object> all = Predicates.Any(a, b);

                assertEquals(and.test(null), all.test(null));
            }
        }
    } // test3

    @
    SuppressWarnings("unchecked")
     public void test4()
     {
        List<Predicate<Object>> p = Collections.list(Predicates.True(), Predicates.False());

        for (Iterator<Predicate<Object>> outer = p.iterator(); outer.hasNext();)
         {
            Predicate<Object> a = outer.next();
            for (Iterator<Predicate<Object>> middle = p.iterator(); middle.hasNext();)
             {
                Predicate<Object> b = middle.next();
                for (Iterator<Predicate<Object>> inner = p.iterator(); inner.hasNext();)
                 {
                    Predicate<Object> c = inner.next();
                    Predicate<Object> and = Predicates.Or(a, Predicates.Or(b, c));
                    Predicate<Object> all = Predicates.Any(a, b, c);

                    assertEquals(and.test(null), all.test(null));
                }
            }
        }
    } // test4
} // class UnaryAnyTest
