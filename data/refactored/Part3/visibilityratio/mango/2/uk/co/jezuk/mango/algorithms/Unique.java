package uk.co.jezuk.mango.algorithms;

import java.util.Iterator;
import java.util.Comparator;

public final class Unique {
    @SuppressWarnings("unchecked")
     static public <T> void execute(Iterator<T> iterator,
                                 Comparator<? super  T> comparator)
     {
        if (!iterator.hasNext())
         return;

        T prev = iterator.next();
        while (iterator.hasNext())
         {
            T next = iterator.next();
            if (match(comparator, prev, next))
             iterator.remove();
             else
             prev = next; // while // execute
        }
    }

    static public <T> boolean match(Comparator<T> c, T o1, T o2)
     {
        if (c != null)
         return (c.compare(o1, o2) == 0);

        if (o1 == null)
         return (o2 == null);

        return o1.equals(o2);
    } // compare

    protected Unique() {}
} // class Unique
