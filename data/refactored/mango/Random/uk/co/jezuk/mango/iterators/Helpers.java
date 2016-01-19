package uk.co.jezuk.mango.iterators;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

abstract

 class Helpers {
    static public <T> List<Iterator<T>> toIterators(final Object ... iterables)
     {
        final List<Iterator<T>> list = new ArrayList<Iterator<T>>();

        for (final Object o: iterables)
         {
            final Iterator<T> i = toIterator(o);
            list.add(i);
        } // for ...

        return list;
    } // toIterators

    @
    SuppressWarnings("unchecked")
     static public <T> Iterator<T> toIterator(final Object o)
     {
        if (o instanceof Iterable)
         return ((Iterable<T>)o).iterator();
        if (o instanceof Iterator)
         return (Iterator<T>)o;
        if (o != null && o.getClass().isArray())
         return new ArrayIterator<T>((T[])o);

        return new SingletonIterator<T>((T)o);
    } // toIterator
} // Helpers
