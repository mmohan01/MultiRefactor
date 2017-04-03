package uk.co.jezuk.mango.iterators;

import java.util.Iterator;
import java.util.List;

/**
 * A <code>BoundedIterator</code> enumerates of a subset of a collection, in the
 * range [<code>start</code>, <code>end</code>).  A normal <code>Iterator</code> 
 * traverses [0, collection.size()), so BoundedIterator allows you
 * to pick out a sub-set without using <code>list.subList()</code> 
 * or equivalent.
 * 
 * @author Jez Higgins, jez@jezuk.co.uk
 */
public class BoundedIterator<T> implements Iterator<T>
{
  /**
   * This form of <code>BoundedIterator</code> limits the range traversed by the 
   * underlying <code>iterator</code>. <p>
   * If <code>iterator.hasNext()</code> fails before <code>end</code> is
   * reached, the traversal will stop prematurely.<p>
   * @throws java.lang.IndexOutOfBoundsException if start<0, end<0 or start>end
   */
  public BoundedIterator(Iterator<? extends T> iterator, int start, int end)
  {
    iter_ = new iteratorWrapper<T>(iterator, start, end);
  } // BoundIterator
  
  /**
   * The form of <code>BoundedIterator</code> uses indexed access directly into
   * the list.<p>
   * If <code>end</code> > list.end() the travesal will stop with
   * list.end() is reached.<p>
   * For <code>ArrayLists</code> and <code>Vectors</code> it 
   * should be slightly quicker.  For <code>LinkedLists</code> it
   * will be slower.
   * @throws java.lang.IndexOutOfBoundsException if start<0, end<0 or start>end
   */
  public BoundedIterator(List<? extends T> list, int start, int end)
  {
    iter_ = new listIterator<T>(list, start, end);
  } // BoundedIterator

  public boolean hasNext()
  {
    return iter_.hasNext();
  } // hasNext

  public T next()
  {
    return iter_.next();
  } // next

  public void remove()
  {
    iter_.remove();
  } // remove

  private Iterator<T> iter_;

  ///////////////////////////////////////////////////
  static private void checkConstraints(int start, int end)
  {
    if(start < 0)
      throw new IndexOutOfBoundsException("start < 0");
    if(end < 0)
      throw new IndexOutOfBoundsException("end < 0");
    if(start > end)
      throw new IndexOutOfBoundsException("start > end");
  } // checkConstraints

  static private class iteratorWrapper<T> implements Iterator<T>
  {
    iteratorWrapper(Iterator<? extends T> iterator, int start, int end)
    {
      BoundedIterator.checkConstraints(start, end);

      iter_ = iterator;
      for(index_ = 0; iter_.hasNext() && index_ < start; ++index_, iter_.next())
	;

      end_ = iter_.hasNext() ? end : index_;
    } // iteratorWrapper

    public boolean hasNext()
    {
      end_ = iter_.hasNext() ? end_ : index_;
      return (index_ < end_);
    } // hasNext()

    public T next()
    {
      ++index_;
      return (T)iter_.next();
    } // next

    public void remove()
    {
      iter_.remove();
    } // remove

    private Iterator<? extends T> iter_;
    private int index_;
    private int end_;
  } // iteratorWrapper

  static private class listIterator<T> implements Iterator<T>
  {
    listIterator(List<? extends T> list, int start, int end)
    {
      BoundedIterator.checkConstraints(start, end);

      list_ = list;
      index_ = start;
      end_ = end;

      if(end_ > list_.size())
        end_ = list.size();
    } // listIterator

    public boolean hasNext()
    {
      return (index_ < end_);
    } // hasNext

    public T next()
    {
      return (T)list_.get(index_++);
    } // next

    public void remove()
    {
      list_.remove(index_ - 1);
    } // remove

    private List<? extends T> list_;
    private int index_;
    private int end_;
  } // listIterator
} // BoundedIterator
