package uk.co.jezuk.mango.algorithms;

import uk.co.jezuk.mango.iterators.SelectingIterator;

import java.util.Iterator;

import uk.co.jezuk.mango.Predicate;

/**
 * Searchs the sequence traversed by the Iterator and returns the first
 * object encountered for which the Predicate returns <code>true</code>.
 * Returns the <code>Object</code>, or <code>null</code> if the value
 * is not found.  The iterator will have been advanced to the next object 
 * in the sequence.
 * @see Find
 * @see FindNotIf
 */
public class FindIf
{
  static public <T> T execute(Iterator<T> iterator, 
                              Predicate<? super T> test)
  {
    if((iterator == null) || (test == null))
      return null;  

    Iterator<T> filter = new SelectingIterator<T>(iterator, test);
    return filter.hasNext() ? filter.next() : null;
  } // execute

  private FindIf() { }
} // FindIf






