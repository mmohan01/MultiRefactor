package uk.co.jezuk.mango;

import junit.framework.*;
import java.util.List;
import java.util.Iterator;

public class UnaryOneOfTest extends TestCase {
    public UnaryOneOfTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(UnaryOneOfTest.class); }

    public void test1()
     {
        Predicate<Object> t = Predicates.OneOf(Predicates.True());
        assertEquals(true, t.test(null));
    } // test1

    public void test2()
     {
        Predicate<Object> t = Predicates.OneOf(Predicates.False());
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
                Predicate<Object> xor = Predicates.Xor(a, b);
                Predicate<Object> all = Predicates.OneOf(a, b);

                assertEquals(xor.test(null), all.test(null));
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
            Predicate<Object> nota = Predicates.Not(a);
            for (Iterator<Predicate<Object>> middle = p.iterator(); middle.hasNext();)
             {
                Predicate<Object> b = middle.next();
                Predicate<Object> notb = Predicates.Not(b);
                for (Iterator<Predicate<Object>> inner = p.iterator(); inner.hasNext();)
                 {
                    Predicate<Object> c = inner.next();
                    Predicate<Object> notc = Predicates.Not(c);

                    Predicate<Object> xor = Predicates.Any(Predicates.All(nota, notb, c),
                                                 Predicates.All(nota, b, notc),
                                                 Predicates.All(a, notb, notc));
                    Predicate<Object> all = Predicates.OneOf(a, b, c);

                    assertEquals(xor.test(null), all.test(null));
                }
            }
        }
    } // test4
} // class UnaryOneOfTest
