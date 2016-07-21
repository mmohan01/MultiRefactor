package uk.co.jezuk.mango.algorithms;

import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;

public class SymmetricDifference {
    static public <T, C extends  Collection<? super  T>>
                  C execute(Iterator<? extends  T> iter,
                    Collection<? extends  T> coll,
                    C results)
     {
        Collection<T> intersection = new ArrayList<T>();
        while (iter.hasNext())
         {
            T o = iter.next();
            if (!coll.contains(o))
             results.add(o);
             else
             intersection.add(o); // while
        }
        iter = coll.iterator();
        while (iter.hasNext())
         {
            T o = iter.next();
            if (!intersection.contains(o))
             results.add(o); // while
        }
        return results;
    } // execute

    static public <T, C extends  Collection<? super  T>>
                  C execute(Iterator<? extends  T> iter,
                    Iterator<? extends  T> iter2,
                    C results)
     {
        Collection<T> coll = new ArrayList<T>();
        while (iter2.hasNext())
         coll.add(iter2.next());
        return execute(iter, coll, results);
    } // execute

    protected SymmetricDifference() {}
} // class SymmetricDifference
