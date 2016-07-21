package uk.co.jezuk.mango.algorithms;

import java.util.Iterator;
import uk.co.jezuk.mango.Predicate;

public final class FindPositionIf {
    static public <T> int execute(Iterator<T> iterator,
                                Predicate<? super  T> test)
     {
        if (iterator == null)
         return -1;

        int count = 0;
        while (iterator.hasNext())
         {
            T obj = iterator.next();
            if (test.test(obj))
                return count;
            ++count;
        } // while ...

        return -1;
    } // execute

    protected FindPositionIf() {}
} // Find
