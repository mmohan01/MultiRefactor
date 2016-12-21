package uk.co.jezuk.mango;

import uk.co.jezuk.mango.generators.*;

/**
 * <strong>The Mango Library Generators</strong>
 * <br/><br/>
 * A <code>{@link Generator}</code> describes a function which takes no arguments
 * <code>fn()</code>. It returns some object of type <code>R</code>, and
 * may return the same object or different objects for each invocation.  
 * It can refer to local state, perform disk reads or writes, or whatever.  
 * @see Generator
 * @author Jez Higgins, jez@jezuk.co.uk
 */
public class Generators
{
  /**
   * Returns Integer(seed), Integer(seed+1), Integer(seed+2) ... 
   * on successive calls to fn().
   * Sequence begins at 0 if no seed value is given.
   */
  static public Generator<Integer> IntegerSequence() { return new IntegerSequence(); }
  static public Generator<Integer> IntegerSequence(int seed) { return new IntegerSequence(seed); }
  static public Generator<Integer> IntegerSequence(Integer seed) { return new IntegerSequence(seed); }

  /**
   * Returns seed, seed, ... on successive calls to fn()
   */  
  static public <T> Generator<T> ConstantSequence(T constant) { return new ConstantSequence<T>(constant); }

  /**
   * Always returns null
   */
  static public <T> Generator<T> NullSequence() { return (Generator<T>)NullSequence.INSTANCE; }
} // class Generators
