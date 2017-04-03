package uk.co.jezuk.mango.algorithms;

import java.util.Iterator;
import uk.co.jezuk.mango.Function;

/**
 * The algorithm ForEach applies the function <code>fn</code> to
 * each element in the <code>iterator</code> sequence. 
 */
public class ForEach
{
  static public <T, R> void execute(Iterator<T> iterator, 
                                    Function<? super T, R> fn)
  {
    if(iterator == null || fn == null)
	    return;
    
    while(iterator.hasNext())
	    fn.fn(iterator.next());
  } // execute

  //////////////////////////////////
  private ForEach() { }
} // ForEach
