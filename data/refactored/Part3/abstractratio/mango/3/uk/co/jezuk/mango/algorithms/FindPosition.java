package uk.co.jezuk.mango.algorithms;

import java.util.Iterator;

/**
 * Searchs the sequence travesed by the Iterator for the given value.
 * Returns the index of the value in the collection, or <code>-1</code>
 * if the value is not found.  The iterator will have been advanced to 
 * the next object in the sequence.
 * The objects in the sequence and <code>value</code> must be comparable using
 * <code>Object.equals</code>.
 * @see Find
 */
public class FindPosition {
    static public <T> int execute(Iterator<? extends  T> iterator, T value)
     {
        if (iterator == null)
         return -1;

        int count = 0;
        while (iterator.hasNext())
         {
            T obj = iterator.next();
            if (value.equals(obj))
                return count;
            ++count;
        } // while ...

        return -1;
    } FindPosition() {} // execute
} // Find
