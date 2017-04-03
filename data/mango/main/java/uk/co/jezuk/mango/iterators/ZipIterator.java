package uk.co.jezuk.mango.iterators;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public class ZipIterator<T> implements Iterator<List<T>>
{
  private final List<Iterator<T>> zip_;

  public ZipIterator(final Object... iterables)
  {
    zip_ = Helpers.toIterators(iterables);
  } // ZipIterator

  public List<T> next()
  {
    List<T> l = new ArrayList<T>();
    for(Iterator<T> iter : zip_)
      l.add(iter.next());
    return l;
  } // next

  public boolean hasNext()
  {
    for(Iterator<T> iter : zip_)
      if(!iter.hasNext())
        return false;

    return true;
  } // hasNext

  public void remove()
  {
    throw new UnsupportedOperationException("uk.co.jezuk.mango.ZipIterator does not support the remove method");
  } // remove
} // ZipIterator