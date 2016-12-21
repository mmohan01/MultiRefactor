package uk.co.jezuk.mango.algorithms;

import java.util.Iterator;

/**
 * Searchs the sequence travesed by the Iterator for the given value.
 * Returns the <code>Object</code>, or <code>null</code> if the value
 * is not found.  The iterator will have been advanced to the next object 
 * in the sequence.
 * The objects in the sequence and <code>value</code> must be comparable using
 * <code>Object.equals</code> (unless <code>value</code> is <code>null</code>).
 * @see FindIf
 * @see FindNotIf
 */
public class Find
{
  static public <T> T execute(Iterator<? extends T> iterator, T value)
  {
    if((iterator == null) || (value == null))
      return null;  

    while(iterator.hasNext())
    {
      T obj = iterator.next();
      if(value.equals(obj))
        return obj;
    } // while ...

    return null;
  } // execute

  private Find() { }
} // Find
