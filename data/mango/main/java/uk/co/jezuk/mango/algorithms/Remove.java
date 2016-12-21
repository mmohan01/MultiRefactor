package uk.co.jezuk.mango.algorithms;

import java.util.Iterator;

public class Remove
{
  static public <T> void execute(Iterator<? extends T> iterator, T value)
  {
    if(iterator == null)
      return;  

    while(iterator.hasNext())
    {
      T obj = iterator.next();
      if((value == null && obj == null) || (value != null && value.equals(obj)))
        iterator.remove();
    } // while ...
  } // execute

  private Remove() { }
} // Remove
