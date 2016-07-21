package uk.co.jezuk.mango.algorithms;

import uk.co.jezuk.mango.iterators.SkippingIterator;
import java.util.Iterator;
import uk.co.jezuk.mango.Predicate;

/**
 * Searchs the sequence traversed by the Iterator and returns the first
 * object encountered for which the Predicate returns <code>false</code>.
 * @see Find
 * @see FindIf
 */
public class FindIfNot {
    static public <T> T execute(Iterator<T> iterator, Predicate<? super  T> test)
     {
        if ((iterator == null) || (test == null))
         return null;
        final

         Iterator<T> filter = new SkippingIterator<T>(iterator, test);
        return filter.hasNext() ? filter.next() : null;
    } FindIfNot() {} // execute
} // FindIfNot
