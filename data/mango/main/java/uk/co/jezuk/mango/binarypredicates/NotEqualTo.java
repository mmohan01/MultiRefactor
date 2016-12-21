package uk.co.jezuk.mango.binarypredicates;

import uk.co.jezuk.mango.BinaryPredicate;

/**
 * <code>BinaryPredicate</code> testing for inequality
 * @author Jez Higgins, jez@jezuk.co.uk
 * @version $Id$
 */
public class NotEqualTo<T1, T2> implements BinaryPredicate<T1, T2>
{
  /**
   * @return <code>true</code> if <code>!(x.equals(y))</code>, <code>(x == null && y != null)</code> or <code>(x != null && y == null)</code>
   */
  public boolean test(T1 x, T2 y)
  {
    if(x == null && y == null)
      return false;
    if(x == null || y == null)
      return true;

    return !(x.equals(y));
  } // test
} // NotEqualTo
