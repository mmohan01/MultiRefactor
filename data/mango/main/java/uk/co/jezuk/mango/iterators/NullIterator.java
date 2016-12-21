package uk.co.jezuk.mango.iterators;

import java.util.Iterator;
import java.util.NoSuchElementException;

public enum NullIterator implements Iterator<Object>
{
  INSTANCE;

  public boolean hasNext()
  {
    return false;
  } // hasNext
  
  public Object next()
  {
    throw new NoSuchElementException("uk.co.jezuk.mango.NullIterator does not support the next method.  In fact it's probably a logic error that you called it at all.");
  } // next
  
  public void remove()
  {
    throw new UnsupportedOperationException("uk.co.jezuk.mango.NullIterator does not support the remove method.  In fact it's probably a logic error that you called it at all.");
  } // remove
} // NullIterator
