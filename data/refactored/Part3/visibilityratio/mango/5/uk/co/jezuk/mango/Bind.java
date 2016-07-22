package uk.co.jezuk.mango;

/**
 * <strong>The Mango Library Function Binders</strong>
 * <br/><br/>
 * Function and Predicate binding adaptors.  
 * @author Jez Higgins, jez@jezuk.co.uk
 */
public class Bind {

  /**
   * Adapts a <code>BinaryFunction</code> into a <code>Function</code>.
   * If <code>f</code> is an object implementing <code>BinaryFunction</code>, then
   * <code>Bind.Second(f, C).fn(arg)</code> returns <code>f(arg, C)</code>.<p>
   * Intuitively, you can think of this as "binding" the second argument of a 
   * <code>BinaryFunction</code> to a constant, thus giving a <code>Function</code>.
   */
    protected <T1, T2, R> Function<T1, R> Second(final BinaryFunction<T1, T2, R> f,
                                                   final T2 c)
     {
        return new Function<T1, R>(){
            private final BinaryFunction<T1, T2, R> fn_;
            private final T2 c_;
            { fn_ = f; c_ = c;
            }
            public R fn(T1 arg) { return fn_.fn(arg, c_); }
        };
    } // Second

  /**
   * Special case which adapts a <code>BinaryPredicate</code> to a <code>Predicate</code>.
   * If <code>p</code> is a <code>BinaryPredicate</code>, then <code>Bind.Second(p, C).test(arg)</code>
   * returns <code>p.test(arg, C)</code>.
   */ public <T1, T2> Predicate<T1> Second(final BinaryPredicate<T1, T2> p, final T2 c)
     {
        return new Predicate<T1>(){
            private final BinaryPredicate<T1, T2> test_;
            private final T2 c_;
            { test_ = p; c_ = c;
            }
            public boolean test(final T1 arg) { return test_.test(arg, c_); }
        };
    } // Second

  //////////////////////////////////////////
    private Bind() {}
} // Bind
