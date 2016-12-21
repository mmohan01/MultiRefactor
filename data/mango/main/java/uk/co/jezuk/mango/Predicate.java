package uk.co.jezuk.mango;

/**
 * A <code>Predicate</code> is a special form of a <code>Function</code>, 
 * whose result represents the truth or otherwise of some condition.
 * Returns <code>true</code> if the condition the Predicate tests for 
 * is satisfied, <code>false</code> otherwise.
 * @see Predicates
 * @author Jez Higgins, jez@jezuk.co.uk
 */
public interface Predicate<T>
{
  public boolean test(T x);
} // Predicate

