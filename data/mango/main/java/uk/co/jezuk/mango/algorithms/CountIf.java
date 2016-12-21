package uk.co.jezuk.mango.algorithms;

import uk.co.jezuk.mango.Predicate;
import uk.co.jezuk.mango.iterators.SelectingIterator;
import java.util.Iterator;
/**
 * <code>CountIf</code> is similar to <code>Count</code>, but more general.
 * It computes the number of elements in the sequence which satisfy some condition.  
 * The condition is a described in the user-supplied <code>test</code> object, and 
 * <code>CountIf</code> computes the number of objects such that <code>test.test(o)</code>
 * is <code>true</code>.
 * @see Count
 * @see CountIfNot
 */
public class CountIf
{
  static public <T> int execute(Iterator<T> iterator, 
                                Predicate<? super T> test)
  {
    if((iterator == null) || (test == null))
      return 0;  

    int c = 0;
    for(Iterator<T> filter = new SelectingIterator<T>(iterator, test); 
				filter.hasNext();
				filter.next(), ++c);
    
    return c;
  } // execute

  /////////////////////////////////////////////////////////
  private CountIf() { }
} // CountIf
