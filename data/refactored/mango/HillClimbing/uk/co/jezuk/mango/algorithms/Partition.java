package uk.co.jezuk.mango.algorithms;

import java.util.Iterator;
import java.util.Collection;

import uk.co.jezuk.mango.Predicate;

public abstract class Partition {

    abstract public <T, C extends  Collection<? super  T>>
                      C execute(Iterator<T> iterator,
                                Predicate<? super  T> test,
                                C results)
     {
        if ((iterator == null) || (test == null))
         return results;

        while (iterator.hasNext())
         {
            T obj = iterator.next();
            if (test.test(obj))
             {
                iterator.remove();
                if (results != null)
                    results.add(obj); // if ... // while ...
            }
        }

        return results;
    } // execute

    private Partition() {}
} // Partition
