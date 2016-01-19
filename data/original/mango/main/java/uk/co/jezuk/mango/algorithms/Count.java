package uk.co.jezuk.mango.algorithms;

import java.util.Iterator;

/**
 * <code>Count</code> computes the number of elements in the sequence that 
 * are equal to <code>value</code>.  <br>
 * <code>value</code> may be <code>null</code>.<br>
 * The objects in the sequence and <code>value</code> must be comparable using
 * <code>Object.equals</code> (unless <code>value</code> is <code>null</code>).
 * @see CountIf
 */
public class Count
{
  static public <T> int execute(Iterator<? extends T> iterator, 
                                T value)
  {
    if(iterator == null)
      return 0;  

    if(value == null)
      return execute_null(iterator);

    int c = 0;
    while(iterator.hasNext())
      if(value.equals(iterator.next()))
        ++c;

    return c;
  } // execute

  static private <T> int execute_null(Iterator<T> iterator)
  {
    int c = 0;
      
    while(iterator.hasNext())
      if(iterator.next() == null)
        ++c;
    return c;
  } // executer_null

  private Count() { }
} // Count
