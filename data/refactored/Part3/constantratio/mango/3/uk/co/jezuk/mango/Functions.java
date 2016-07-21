package uk.co.jezuk.mango;

import uk.co.jezuk.mango.functions.*;

/**
 * <strong>The Mango Library Unary Functions</strong>
 * <br/><br/>
 * A <code>{@link Function}</code> describes a function which takes one argument
 * - <code>fn(x)</code>. It returns some <code>T</code> and returns and 
 * object of type <code>R</code>.  T and R may be the same time).  A 
 * <code>Function</code> may return the same object or different objects 
 * for each invocation, even given the same argument.  It can refer to 
 * local state, perform disk reads or writes, or whatever.  
 * @see Function
 * @see Predicates
 * @author Jez Higgins, jez@jezuk.co.uk
 */
public class Functions {
  ////////////////////////////////////////////////////
  // Function adaptors
  /**
   * The identity function.  Takes a single argument, 
   * and returns it unchanged.
   */
    static public <T> Function<T, T> Identity()
     {
        return (Function<T, T>)Identity.INSTANCE;
    } // Identity

  /**
   * Returns the initial value, regardless of the argument
   * passed to the function
   */
    static <T, R> Function<T, R> Constant(R value)
     {
        return (Function<T, R>)new Constant<R>(value);
    } // Constant
} // class Functions
