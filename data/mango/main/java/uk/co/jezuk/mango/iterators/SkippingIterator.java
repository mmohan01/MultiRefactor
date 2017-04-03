package uk.co.jezuk.mango.iterators;

import java.util.Iterator;
import uk.co.jezuk.mango.Predicate;

/**
 * A <code>SkippingIterator</code> enumerates a sequence,
 * stepping over the elements
 * that match the supplied <code>Predicate</code>.
 * 
 * @see PredicatedIterator
 * @author Jez Higgins, jez@jezuk.co.uk
 */
public class SkippingIterator<T> implements Iterator<T>
{
  public SkippingIterator(Iterator<T> iterator, Predicate<? super T> predicate)
  {
    iter_ = iterator;
    pred_ = predicate;

    findNext();
  } // SkippingIterator

  public boolean hasNext()
  {
    return (next_ != null);
  } // hasNext

  public T next()
  {
    T current = next_;
    findNext();
    return current;
  } // next

  public void remove()
  {
    throw new UnsupportedOperationException("SkippingIterator does not support the remove method");
  } // remove

  private void findNext()
  {
    next_ = null;
    while(iter_.hasNext() && next_ == null)
    {
      T candidate = iter_.next();
      if(!pred_.test(candidate))
        next_ = candidate;
    } // while
  } // findNext

  ////////////////////////
  private final Iterator<T> iter_;
  private final Predicate<? super T> pred_;
  private T next_;
} // SkippingIterator


