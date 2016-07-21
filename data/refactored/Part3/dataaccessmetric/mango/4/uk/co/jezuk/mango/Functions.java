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
   * Compose is a unary function adaptor.  If <code>f</code> and <code>g</code>
   * are <code>Functions</code>, then <code>Compose</code> creates a new
   * function <code>h</code>, where <code>h(x)</code> is equal to <code>f(g(x))</code>.
   */
    static public <T, T2, R> Function<T, R> Compose(final Function<T2, R> f,
                          final Function<T, T2> g)
     {
        return new Function<T, R>(){
            public R fn(final T x)
             {
                return f.fn(g.fn(x));
            } // fn
        };
    } // Compose



  ////////////////////////////////////////////////////
  // Unary Functions
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
    static public <T, R> Function<T, R> Constant(R value)
     {
        return (Function<T, R>)new Constant<R>(value);
    } // Constant
} // class Functions
