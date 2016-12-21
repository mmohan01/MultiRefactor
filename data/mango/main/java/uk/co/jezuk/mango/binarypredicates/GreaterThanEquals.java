package uk.co.jezuk.mango.binarypredicates;

import uk.co.jezuk.mango.BinaryPredicate;

/**
 * <code>BinaryPredicate</code> that returns true if <code>x</code> is greater than or equal to <code>y</code>.
 * <code>x</code> and <code>y</code> must implement the <code>java.lang.Comparable<code> interface.
 * @author Jez Higgins, jez@jezuk.co.uk
 */
public class GreaterThanEquals <T1, T2> 
  implements BinaryPredicate<T1, T2>
{
  /**
   * @return <code>true</code> if <code>x.compareTo(y) &gt; 0</code> 
   */
  @SuppressWarnings("unchecked")
  public boolean test(T1 x, T2 y)
  {
    if((x == null) && (y == null))
      return true;
    if(x == null)
      return false;
    if(y == null)
      return true;

    return ((Comparable<T2>)x).compareTo(y) >= 0;
  } // test
} // GreaterThanEquals

