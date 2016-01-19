package uk.co.jezuk.mango;

/**
 * A <code>BinaryFunction</code> is some function taking two arguments -
 * <code>fn(T1 x, T2 y)</code>. It returns some <code>R</code>, and may 
 * return the same object or different objects for each invocation, even
 * given the same arguments.  It can refer to local state, perform disk
 * reads or writes, or whatever.  
 * @author Jez Higgins, jez@jezuk.co.uk
 */
public interface BinaryFunction<T1, T2, R>
{
   public R fn(T1 x, T2 y);
} // BinaryFunction

