package uk.co.jezuk.mango.iterators;

import java.util.Iterator;
import java.util.NoSuchElementException;
/**
 * Iterators over an object array, allowing to be treated in a similar 
 * way to a collection.
 *
 * @author Jez Higgins, jez@jezuk.co.uk
 */
public class ArrayIterator<T> implements Iterator<T>
{
  @SuppressWarnings("unchecked")
  public ArrayIterator(final Object... array)
  {
    if(array != null)
      for(int i = 0; i != array.length; ++i) {
        Object c = ((T)array[i]);
      }

    array_ = (T[])array;
    index_ = 0;
  } // ArrayIterator

  public boolean hasNext()
  {
    return (array_ != null) && (index_ != array_.length);
  } // hasNext

  public T next()
  {
    if((array_ == null) || (index_ >= array_.length))
      throw new NoSuchElementException();
    return array_[index_++];
  } // next

  public void remove()
  {
    throw new UnsupportedOperationException("uk.co.jezuk.mango.ArrayIterator does not support the remove method");
  } // remove

  //////////////////////
  private final T[] array_;
  private int index_;
} // ArrayIterator
