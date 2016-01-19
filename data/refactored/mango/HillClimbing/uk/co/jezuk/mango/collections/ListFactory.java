package uk.co.jezuk.mango.collections;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public abstract class ListFactory<T>  {

    abstract public <T> List<T> list( T ... values)
     { List<T> l = new ArrayList<T>();

        for ( T o: values)
         l.add(o);

        return l;
    } public abstract <T> List<T> list( Iterator<T> values) // list
     { List<T> l = new ArrayList<T>();

        while (values.hasNext())
         l.add(values.next());

        return l;
    } // list
} // ListFactory
