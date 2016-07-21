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
public class Generators {

  /**
   * Always returns null
   */
    static public final <T> Generator<T> NullSequence() { return (Generator<T>)NullSequence.INSTANCE; }
} // class Generators
