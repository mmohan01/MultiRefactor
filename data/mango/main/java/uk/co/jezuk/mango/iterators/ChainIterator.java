package uk.co.jezuk.mango.iterators;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public class ChainIterator<T> implements Iterator<T>
{
  private final Iterator<Iterator<T>> chain_;
  private Iterator<T> current_;

  public ChainIterator(final Object... iterables)
  {
    final List<Iterator<T>> list = Helpers.toIterators(iterables);
    chain_ = list.iterator();
    current_ = chain_.hasNext() ? chain_.next() : null;
  } // ChainIterator

  public boolean hasNext()
  {
    if(current_ == null)
      return false;

    if(current_.hasNext())
      return true;

    current_ = chain_.hasNext() ? chain_.next() : null;
    return hasNext();
  } // hasNext

  public T next()
  {
    return current_.next();
  } // next

  public void remove()
  {
    throw new UnsupportedOperationException("uk.co.jezuk.mango.ChainIterator does not support the remove method");
  } // remove
} // ChainIterator