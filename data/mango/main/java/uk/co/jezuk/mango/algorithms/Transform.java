package uk.co.jezuk.mango.algorithms;

import uk.co.jezuk.mango.Function;
import java.util.Collection;
import java.util.Iterator;

/**
 * The algorith Transform applies the function <code>fn</code> to
 * each element in the <code>iterator</code> sequence.
 * The return value of <code>fn</code> is added to the collection <code>results</code>
 * If the return value of <code>fn</code> is a collection, then each member of 
 * the return value is added to results.
 * @version $Id$
 */
public class Transform
{
  public static <T, R, C extends Collection<? super R>> 
				 C execute(Iterator<T> iterator, 
                                           Function<? super T, R> fn, 
                                           C results)
  {
    if(iterator == null || fn == null || results == null)
      return results;

    while(iterator.hasNext())
    {
      R o = fn.fn(iterator.next());
      results.add(o);
    } // while ...
    return results;
  } // execute

  private Transform() {}
} // Tranform
