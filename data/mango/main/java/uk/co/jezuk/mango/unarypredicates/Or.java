package uk.co.jezuk.mango.unarypredicates;

import uk.co.jezuk.mango.Predicate;

public class Or<T> implements Predicate<T>
{
  public Or(Predicate<T> pred1, 
            Predicate<T> pred2) 
  { 
    p1_ = pred1; 
    p2_ = pred2; 
  } // Or

  public boolean test(T x) 
  { 
    return p1_.test(x) || p2_.test(x); 
  } // test

  private Predicate<T> p1_;
  private Predicate<T> p2_;
} // Or
