package uk.co.jezuk.mango.iterators;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterators over a single object 
 *
 * @author Jez Higgins, jez@jezuk.co.uk
 */
public class SingletonIterator<T> implements Iterator<T>
{
  public SingletonIterator(T object)
  {
    object_ = object;
    spent_ = false;
  } // SingletonIterator

  public boolean hasNext()
  {
    return (spent_ == false);
  } // hasNext

  public T next()
  {
    if(spent_)
      throw new java.util.NoSuchElementException();
    spent_ = true;
    return object_;
  } // next

  public void remove()
  {
    throw new UnsupportedOperationException("uk.co.jezuk.mango.SingletonIterator does not support the remove method");
  } // remove

  //////////////////////
  private final T object_;
  private boolean spent_;
} // SingletonIterator
