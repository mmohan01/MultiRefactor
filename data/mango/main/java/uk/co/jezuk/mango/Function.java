package uk.co.jezuk.mango;

/**
 * A <code>Function</code> describes a function which takes one argument
 * - <code>fn(x)</code>. It returns some <code>T</code> and returns and 
 * object of type <code>R</code>.  T and R may be the same time).  A 
 * <code>Function</code> may return the same object or different objects 
 * for each invocation, even given the same argument.  It can refer to 
 * local state, perform disk reads or writes, or whatever.  
 * @see Functions
 * @author Jez Higgins, jez@jezuk.co.uk
 */
public interface Function<T, R>
{
  public R fn(T x);
} // Function

