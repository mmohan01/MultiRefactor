package uk.co.jezuk.mango.binarypredicates;

import uk.co.jezuk.mango.BinaryPredicate;

/**
 * @author Jez Higgins, jez@jezuk.co.uk
 */
public class Not<T1, T2> implements BinaryPredicate<T1, T2>
{
  public Not(BinaryPredicate<T1, T2> p) { p_ = p; }
  public boolean test(T1 x, T2 y) { return !p_.test(x, y); }
  private BinaryPredicate<T1, T2> p_;
} // Not
