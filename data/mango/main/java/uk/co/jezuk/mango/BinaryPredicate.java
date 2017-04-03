package uk.co.jezuk.mango;

/**
 * A <code>BinaryPredicate</code> is some function taking two arguments -
 * <code>fn(x, y)</code> and returning the result of some test.
 * It returns <code>true</code> if the conditions of the test are satisfied,
 * <code>false</code> otherwise
 * @author Jez Higgins, jez@jezuk.co.uk
 */
public interface BinaryPredicate<T1, T2>
{
  public boolean test(T1 x, T2 y);
} // BinaryPredicate

