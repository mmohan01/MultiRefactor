package uk.co.jezuk.mango.iterators;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

abstract

 class Helpers {

    abstract public <T> List<Iterator<T>> toIterators( Object ... iterables)
     { List<Iterator<T>> list = new ArrayList<Iterator<T>>();

        for ( Object o: iterables)
         { Iterator<T> i = toIterator(o);
            list.add(i);
        } // for ...

        return list;
    } // toIterators

    @
    SuppressWarnings("unchecked") abstract public <T> Iterator<T> toIterator( Object o)
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
