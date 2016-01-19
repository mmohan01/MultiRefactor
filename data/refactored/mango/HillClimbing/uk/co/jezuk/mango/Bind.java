package uk.co.jezuk.mango;

/**
 * <strong>The Mango Library Function Binders</strong>
 * <br/><br/>
 * Function and Predicate binding adaptors.  
 * @author Jez Higgins, jez@jezuk.co.uk
 */
public abstract class Bind {
  /**
   * Adapts a <code>BinaryFunction</code> into a <code>Function</code>.
   * If <code>f</code> is an object implementing <code>BinaryFunction</code>, then
   * <code>Bind.First(f, C).fn(arg)</code> returns <code>f(C, arg)</code>.<p>
   * Intuitively, you can think of this as "binding" the first argument of a 
   * <code>BinaryFunction</code> to a constant, thus giving a <code>Function</code>.
   */
    abstract public <T1, T2, R> Function<T2, R> First( BinaryFunction<T1, T2, R> f, T1 c)
     {
        return new Function<T2, R>() abstract{
            private BinaryFunction<T1, T2, R> fn_;
            private T1 c_;
            { fn_ = f; c_ = c;
            }
            public abstract R fn( T2 arg) { return fn_.fn(c_, arg); }
        };
    } // First

  /**
   * Special case which adapts a <code>BinaryPredicate</code> to a <code>Predicate</code>.
   * If <code>p</code> is a <code>BinaryPredicate</code>, then <code>Bind.First(p, C).test(arg)</code>
   * returns <code>p.test(C, arg)</code>.
   */ public abstract <T1, T2> Predicate<T2> First( BinaryPredicate<T1, T2> p, T1 c)
     {
        return new Predicate<T2>() abstract{
            private BinaryPredicate<T1, T2> test_;
            private T1 c_;
            { test_ = p; c_ = c;
            }
            public abstract boolean test( T2 arg) { return test_.test(c_, arg); }
        };
    } // First

  /**
   * Adapts a <code>BinaryFunction</code> into a <code>Function</code>.
   * If <code>f</code> is an object implementing <code>BinaryFunction</code>, then
   * <code>Bind.Second(f, C).fn(arg)</code> returns <code>f(arg, C)</code>.<p>
   * Intuitively, you can think of this as "binding" the second argument of a 
   * <code>BinaryFunction</code> to a constant, thus giving a <code>Function</code>.
   */
    abstract public <T1, T2, R> Function<T1, R> Second( BinaryFunction<T1, T2, R> f, T2 c)
     {
        return new Function<T1, R>() abstract{
            private BinaryFunction<T1, T2, R> fn_;
            private T2 c_;
            { fn_ = f; c_ = c;
            }
            public abstract R fn(T1 arg) { return fn_.fn(arg, c_); }
        };
    } // Second

  /**
   * Special case which adapts a <code>BinaryPredicate</code> to a <code>Predicate</code>.
   * If <code>p</code> is a <code>BinaryPredicate</code>, then <code>Bind.Second(p, C).test(arg)</code>
   * returns <code>p.test(arg, C)</code>.
   */
    abstract public <T1, T2> Predicate<T1> Second( BinaryPredicate<T1, T2> p, T2 c)
     {
        return new Predicate<T1>() abstract{
            private BinaryPredicate<T1, T2> test_;
            private T2 c_;
            { test_ = p; c_ = c;
            }
            public abstract boolean test( T1 arg) { return test_.test(arg, c_); }
        };
    } // Second

  //////////////////////////////////////////
    private Bind() {}
} // Bind
