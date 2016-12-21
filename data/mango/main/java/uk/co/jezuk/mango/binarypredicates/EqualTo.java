package uk.co.jezuk.mango.binarypredicates;

import uk.co.jezuk.mango.BinaryPredicate;

/**
 * <code>BinaryPredicate</code> testing for equality
 * @author Jez Higgins, jez@jezuk.co.uk
 */
public class EqualTo<T1, T2> implements BinaryPredicate<T1, T2>
{
  /**
   * @return <code>true</code> if <code>x.equals(y)</code> or <code>(x == null && y == null)</code>
   */
  public boolean test(T1 x, T2 y)
  {
    if(x == null && y == null)
      return true;
    if(x == null || y == null)
      return false;

    return x.equals(y);
  } // test
} // EqualTo
