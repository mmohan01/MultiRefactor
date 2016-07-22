package uk.co.jezuk.mango.algorithms;

import java.util.Iterator;
import uk.co.jezuk.mango.Predicate;

public class FindPositionIf {
    static public <T> int execute(Iterator<T> iterator,
                                Predicate<? super  T> test)
     {
        if (iterator == null)
         return -1;

        int count = 0;
        while (iterator.hasNext())
         {
            final
             T obj = iterator.next();
            if (test.test(obj))
                return count;
            ++count;
        } // while ...

        return -1;
    } // execute

    public FindPositionIf() {}
} // Find
