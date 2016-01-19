package uk.co.jezuk.mango.algorithms;

import uk.co.jezuk.mango.iterators.SkippingIterator;

import uk.co.jezuk.mango.Predicate;

import java.util.Iterator;

/**
 * <code>CountIfNot</code> is the complement of <code>CountIf</code>.
 * It counts the number of elements in the sequence which fail some condition.  
 * The condition is a described in the user-supplied <code>test</code> object, and 
 * <code>CountIfNot</code> computes the number of objects such that <code>test.test(o)</code>
 * is <code>false</code>.
 * @see Count
 * @see CountIf
 */
public class CountIfNot
{
  static public <T> int execute(Iterator<T> iterator, 
                                Predicate<? super T> test)
  {
    if((iterator == null) || (test == null))
      return 0;  

    int c = 0;
    for(Iterator<T> filter = new SkippingIterator<T>(iterator, test); 
        filter.hasNext();
        filter.next(), ++c);

    return c;
  } // execute

  /////////////////////////////////////////////////////////
  private CountIfNot() { }
} // CountNotIf
