package uk.co.jezuk.mango;

/**
 * <strong>The Mango Library Binary Functions</strong>
 * <br/><br/>
 * A <code>BinaryFunction</code> is some function taking two arguments -
 * <code>fn(T1 x, T2 y)</code>. It returns some <code>R</code>, and may 
 * return the same object or different objects for each invocation, even
 * given the same arguments.  It can refer to local state, perform disk
 * reads or writes, or whatever.  
 * @see BinaryFunction
 * @author Jez Higgins, jez@jezuk.co.uk
 */
public class BinaryFunctions
{
  ////////////////////////////////////////////////////
  // Function adaptors
  /**
   * Compose is a function adaptor.  If <code>f</code> is a <code>BinaryFunction</code>
   * and <code>g1</code> and <code>g2</code> are <code>Functions</code>, then Compose
   * returns a new <code>BinaryFunction</code> <code>h</code> such that <code>h(x, y)</code>
   * is <code>f(g1(x), g2(y))</code>
   */
    static public <T1, R1, T2, R2, R> BinaryFunction<T1, T2, R>
	Compose(final BinaryFunction<R1, R2, R> f, 
		final Function<T1, R1> g1, 
		final Function<T2, R2> g2)
  {
    return new BinaryFunction<T1, T2, R>() {
      public R fn(final T1 x, final T2 y)
      {
        return f.fn(g1.fn(x), g2.fn(y));
      } // fn
    };
  } // Compose
  
  private BinaryFunctions() { }
} // class BinaryFunctions