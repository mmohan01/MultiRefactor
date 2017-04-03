package uk.co.jezuk.mango.binarypredicates;

import uk.co.jezuk.mango.BinaryPredicate;

/**
 * <code>BinaryPredicate</code> that returns true if <code>x</code> is less that <code>y</code>.
 * <code>x</code> and <code>y</code> must implement the <code>java.lang.Comparable<code> interface.
 * @author Jez Higgins, jez@jezuk.co.uk
 */
public class LessThan<T1, T2> 
  implements BinaryPredicate<T1, T2>
{
  /**
   * @return <code>true</code> if <code>x.compareTo(y) &lt; 0</code> 
   */
  @SuppressWarnings("unchecked")
  public boolean test(T1 x, T2 y)
  {
    if(x == null && y == null)
      return false;
    if(x == null)
      return true;
    if(y == null)
      return false;

    return ((Comparable<T2>)x).compareTo(y) < 0;
  } // test
} // LessThan

