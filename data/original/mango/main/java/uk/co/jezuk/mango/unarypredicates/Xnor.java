package uk.co.jezuk.mango.unarypredicates;

import uk.co.jezuk.mango.Predicate;

public class Xnor<T> implements Predicate<T>
{
  public Xnor(final Predicate<T> pred1, 
             final Predicate<T> pred2) 
  { 
    p1_ = pred1; 
    p2_ = pred2; 
  } // Xnor

  public boolean test(final T x) 
  { 
    return p1_.test(x) == p2_.test(x); 
  } // test

  private final Predicate<T> p1_;
  private final Predicate<T> p2_;
} // Xnor
