package uk.co.jezuk.mango;

import uk.co.jezuk.mango.binarypredicates.*;

/**
 * <strong>The Mango Library Binary Predicates</strong>
 * <br/><br/>
 * A <code>BinaryPredicate</code> is some function taking two arguments -
 * <code>fn(x, y)</code> and returning the result of some test.
 * It returns <code>true</code> if the conditions of the test are satisfied,
 * <code>false</code> otherwise
 * @see BinaryPredicate
 * @author Jez Higgins, jez@jezuk.co.uk
 */
public class BinaryPredicates
{
  /////////////////////////////////////////////////
  // Binary Predicates
  /**
   * <code>BinaryPredicates</code> that always returns <code>true</code>
   */
  static public <T1, T2> BinaryPredicate<T1, T2> True()
  {
    return (BinaryPredicate<T1, T2>)True.INSTANCE;
  } // True

  /**
   * <code>BinaryPredicates</code> that always returns <code>false</code>
   */
  static public <T1, T2> BinaryPredicate<T1, T2> False()
  {
    return (BinaryPredicate<T1, T2>)False.INSTANCE;
  } // False

  /** 
   * A <code>BinaryPredicate</code> which returns a precomputed constant value
   */
  static public <T1, T2> BinaryPredicate<T1, T2> Constant(boolean constant)
  {

    return constant ? (BinaryPredicate<T1, T2>)True.INSTANCE : 
                      (BinaryPredicate<T1, T2>)False.INSTANCE;
  } // Constant



  /**
   * <code>BinaryPredicate</code> testing for equality.
   * <code>true</code> if <code>x.equals(y)</code> or <code>(x == null && y == null)</code>
   */
  static public <T1, T2> BinaryPredicate<T1, T2> EqualTo() 
  { 
    return new EqualTo<T1, T2>(); 
  } // EqualTo

  /**
   * <code>BinaryPredicate</code> that returns true if <code>x</code> is greater than <code>y</code>.
   * <code>x</code> and <code>y</code> must implement the <code>java.lang.Comparable<code> interface.
   */
  static public <T1, T2> BinaryPredicate<T1, T2> GreaterThan() 
  { 
    return new GreaterThan<T1, T2>(); 
  } // GreaterThan

  /**
   * <code>BinaryPredicate</code> that returns true if <code>x</code> is greater than or equal to <code>y</code>.
   * <code>x</code> and <code>y</code> must implement the <code>java.lang.Comparable<code> interface.
   */
  static public <T1, T2> BinaryPredicate<T1, T2> GreaterThanEquals() 
  { 
    return new GreaterThanEquals<T1, T2>(); 
  } // GreaterThanEquals

  /**
   * <code>BinaryPredicate</code> that returns true if <code>x</code> is less than <code>y</code>.
   * <code>x</code> and <code>y</code> must implement the <code>java.lang.Comparable<code> interface.
   */
  static public <T1, T2> BinaryPredicate<T1, T2> LessThan() 
  { 
    return new LessThan<T1, T2>(); 
  } // LessThan

  /**
   * <code>BinaryPredicate</code> that returns true if <code>x</code> is less than or equal to <code>y</code>.
   * <code>x</code> and <code>y</code> must implement the <code>java.lang.Comparable<code> interface.
   */
  static public <T1, T2> BinaryPredicate<T1, T2> LessThanEquals() 
  { 
    return new LessThanEquals<T1, T2>(); 
  } // LessThanEquals

  /**
   * <code>true</code> if <code>not(x.equals(y))</code>, <code>(x == null) && not(y == null)</code> or <code>not(x == null) && (y == null)</code>
   */
  static public <T1, T2> BinaryPredicate<T1, T2> NotEqualTo() 
  { 
    return new NotEqualTo<T1, T2>(); 
  } // NotEqualTo

  /**
   * A <code>BinaryPredicate</code> which is the logical negation of some other <code>BinaryPredicate</code>.  If <code>n</code>
   * is a <code>Not</code> object, and <code>pred</code> is the <code>Predicate</code> it was constructed with,
   * then <code>n.test(x,y)</code> returns <code>!pred.test(x,y)</code>.
   */
  static public <T1, T2> BinaryPredicate<T1, T2> Not(BinaryPredicate<T1, T2> pred) 
  { 
    return new Not<T1, T2>(pred); 
  } // Not

  /**
   * A <code>BinaryPredicate</code> which returns the logical AND of two other <code>BinaryPredicate</code>.  If <code>a</code>
   * is an <code>And</code> object, constructed with <code>pred1</code> and <code>pred2</code>, then 
   * <code>a.test(x,y)</code> returns <code>pred1.test(x,y) && pred2.test(x,y)</code>
   */
  static public <T1, T2> BinaryPredicate<T1, T2> And(BinaryPredicate<T1, T2> pred1, 
                                                     BinaryPredicate<T1, T2> pred2) 
  { 
    return new And<T1, T2>(pred1, pred2); 
  } // And

  /**
   * A <code>BinaryPredicate</code> which returns the logical OR of two other <code>BinaryPredicate</code>.  If <code>a</code>
   * is an <code>Or</code> object, constructed with <code>pred1</code> and <code>pred2</code>, then 
   * <code>a.test(x,y)</code> returns <code>pred1.test(x,y) || pred2.test(x,y)</code>
   */
  static public <T1, T2> BinaryPredicate<T1, T2> Or(BinaryPredicate<T1, T2> pred1, 
                                                    BinaryPredicate<T1, T2> pred2) 
  { 
    return new Or<T1, T2>(pred1, pred2); 
  } // Or

  //////////////////////////////////
  private BinaryPredicates() { }
} // Predicates


