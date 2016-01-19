package uk.co.jezuk.mango;

import junit.framework.*;
import java.util.List;
import java.util.Iterator;

public class UnaryAllTest extends TestCase {
    public UnaryAllTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(UnaryAllTest.class); }

    public void test1()
     {
        Predicate<Object> t = Predicates.All(Predicates.True());
        assertEquals(true, t.test(null));
    } // test1

    public void test2()
     {
        Predicate<Object> t = Predicates.All(Predicates.False());
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
                Predicate<Object> and = Predicates.And(a, b);
                Predicate<Object> all = Predicates.All(a, b);

                assertEquals(and.test(null), all.test(null));
            }
        }
    } // test3

    public void test4()
     {
        @SuppressWarnings("unchecked")
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
                    Predicate<Object> and = Predicates.And(a, Predicates.And(b, c));
                    Predicate<Object> all = Predicates.All(a, b, c);

                    assertEquals(and.test(null), all.test(null));
                }
            }
        }
    } // test4
} // class UnaryAllTest
