package uk.co.jezuk.mango.iterators;

import java.util.Iterator;
import uk.co.jezuk.mango.Function;

/**
 * A <code>TransfromIterator</code> applies a <code>Function</code> to 
 * each element in the sequence, returning the the function result at each step.
 * 
 * @author Jez Higgins, jez@jezuk.co.uk
 */
public class TransformIterator<T, R> implements Iterator<R>
{
  public TransformIterator(Iterator<T> iterator, 
                           Function<? super T, R> transform)
  {
    iter_ = iterator;
    transform_ = transform;
  } // TransformIterator

  public boolean hasNext()
  {
    return iter_.hasNext();
  } // hasNext

  public R next()
  {
    return transform_.fn(iter_.next());
  } // next

  public void remove()
  {
    iter_.remove();
  } // remove

  ////////////////////////
  private Iterator<T> iter_;
  private Function<? super T, R> transform_;
} // TransformIterator


