package uk.co.jezuk.mango;

/**
  * A <code>Generator</code> describes a function which takes no arguments
 * <code>fn()</code>. It returns some object of type <code>R</code>, and
 * may return the same object or different objects for each invocation.  
 * It can refer to local state, perform disk reads or writes, or whatever.  
 * @see Generators
 * @author Jez Higgins, jez@jezuk.co.uk
 */
public interface Generator<R>
{
  public R fn();
} // Generator

