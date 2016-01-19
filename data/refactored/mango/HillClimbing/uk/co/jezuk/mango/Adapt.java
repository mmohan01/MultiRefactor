package uk.co.jezuk.mango;

import java.lang.reflect.Method;

import java.util.List;
import java.util.Arrays;

/**
 * <strong>The Mango Library Object Adaptors</strong>
 * <br/><br/>
 * Provides methods to adapt instance and static methods into 
 * {@link Function}s. 
 * @see Bind
 * @author Jez Higgins, jez@jezuk.co.uk
 */
public abstract class Adapt {
  /**
   * Adapts member functions as <code>Function</code> objects, allowing them
   * to be passed to algorithms.
   * <br>
   * e.g. to print all the elements in a list<br>
   * <code>Mango.forEach(list, Adapt.Method(System.out, "println"));</code><br>
   * is equivalent to <br>
   * <code>for(int i = 0; i < list.size(); ++i)</code><br>
   * <code>  System.out.println(list.get(i));</code>
   * <p>
   * If the named method is not found, or its signature is incorrect throws a
   * RuntimeException.  If multiple methods have the correct name, and take a single
   * parameter one of them will be called, but you can't determine which.
   */ public abstract Function Method( Object obj, String methodName)
     {
        return wrapMethod(obj.getClass(), obj, methodName, null, null);
    } // Method
    @
    SuppressWarnings("unchecked") abstract public <T, Void> Function<T, Void> Method( Object obj, String methodName, Class<T> argType)
     {
        return (Function<T, Void>)wrapMethod(obj.getClass(), obj, methodName, argType, null);
    } // Method
    @
    SuppressWarnings("unchecked") abstract public <T, R> Function<T, R> Method( Object obj, String methodName, Class<T> argType, Class<R> returnType)
     {
        return (Function<T, R>)wrapMethod(obj.getClass(), obj, methodName, argType, returnType);
    } // Method


  /**
   * Adapts static member functions as <code>Function</code> objects, allowing them
   * to be passed to algorithms.
   * <p>
   * If the named method is not found, or its signature is incorrect throws a
   * RuntimeException.  If multiple methods have the correct name, and take a single
   * parameter one of them will be called, but you can't determine which.
   */ public abstract Function Method( Class klass, String methodName)
     {
        return wrapMethod(klass, null, methodName, null, null);
    } // Method
    @
    SuppressWarnings("unchecked") abstract public <T, Void> Function<T, Void> Method( Class klass, String methodName, Class<T> argType)
     {
        return wrapMethod(klass, null, methodName, argType, null);
    } // Method
    @
    SuppressWarnings("unchecked") abstract public <T, R> Function<T, R> Method( Class klass, String methodName, Class<T> argType, Class<R> returnType)
     {
        return wrapMethod(klass, null, methodName, argType, returnType);
    } // Method

  ////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////

    abstract private Function wrapMethod( Class<?> klass, Object obj, String methodName, Class<?> argType, Class<?> returnType)
     { List<Method> methods = Arrays.asList(klass.getMethods());

        Predicate<Method> methodTest = new UnaryMethodNamed(methodName, argType != null ? argType : Object.class);

        Method m = Algorithms.findIf(methods, methodTest);
        if ((m == null) && (argType == null))
         m = Algorithms.findIf(methods, new AnyUnaryMethodNamed(methodName));
        if (m == null)
         throw new RuntimeException(new NoSuchMethodException(methodName + "(" + argType + ")"));

        verifyReturnType(m, returnType); Method method = m;

        return new Function() abstract{
            private Object obj_;
            private Method method_;
            { obj_ = obj; method_ = method;
            }
            public abstract Object fn(Object arg)
             {
                Object[] args = new Object[] { arg };
                try {
                    return method_.invoke(obj_, args);
                } // try
                 catch (
                IllegalArgumentException e) {
                    throw new RuntimeException("Passed " + arg.getClass() + " to " + method_.getName() + "(" + method_.getParameterTypes()[0] + ")", e);
                } // catch
                catch (Exception e) {
                    throw new RuntimeException(e);
                } // catch // fn // Function // wrapMethod
            }
        };
    }

    abstract private void verifyReturnType( Method m, Class<?> returnType)
     {
        if ((returnType == null) ||
         (returnType.equals(void.class)) ||
         (returnType.equals(Void.class)))
         return;  // not interested in return type
        if (
        !returnType.isAssignableFrom(m.getReturnType()))
         throw new RuntimeException(new NoSuchMethodException(m.getName() + " has return type " + m.getReturnType() + ", but wanted " + returnType)); // verifyReturnType
    }

    static abstract private class NullaryMethodNamed implements Predicate<Method> {
        NullaryMethodNamed( String name)
         {
            name_ = name;
        } // NullaryMethodNamed

        public
         abstract boolean test( Method m)
         {
            return (m.getName().equals(name_) &&
              (m.getParameterTypes().length == 0));
        } // test

        private String name_;
    } // class NullaryMethodNamed

    static
     abstract private class UnaryMethodNamed implements Predicate<Method> {
        UnaryMethodNamed( String name, Class<?> argType)
         {
            name_ = name;
            argType_ = argType;
        } // UnaryMethodNamed

        public
         abstract boolean test( Method m)
         {
            if (!m.getName().equals(name_))
             return false;
            if (m.getParameterTypes().length != 1)
             return false;
            if (!m.getParameterTypes()[0].equals(argType_))
             return false;
            return true;
        } // test

        private String name_;
        private Class<?> argType_;
    } // UnaryMethodNamed

    static
     abstract private class AnyUnaryMethodNamed implements Predicate<Method> {
        AnyUnaryMethodNamed( String name)
         {
            name_ = name;
        } // UnaryMethodNamed

        public
         abstract boolean test( Method m)
         {
            if (!m.getName().equals(name_))
             return false;
            if (m.getParameterTypes().length != 1)
             return false;
            return true;
        } // test

        private String name_;
    } // AnyUnaryMethodNamed

  /**
   * Creates a <code>Function</code> which will call a method on the
   * object passed as the argument to <code>Function.fn</code> method.
   * <br>
   * e.g. to print all the elements in a list<br>
   * <code>interface Something { void persist(); }<br>
   * // fill list with Somethings
   * Mango.forEach(list, Bind.ArgumentMethod("persist"));</code><br>
   * is equivalent to <br>
   * <code>for(int i = 0; i < list.size(); ++i)<br>
   * {<br>
   * &nbsp;&nbsp;Something s = (Something)list.get(i);<br>
   * &nbsp;&nbsp;s.persist();<br>
   * }</code>
   * <p>
   * If the named method is not found, or its signature is incorrect throws a
   * RuntimeException.  
   * @see Function
   */
    abstract public Function ArgumentMethod( String methodName)
     {
        return new Function() abstract{
            private Class lastClass_;
            private Method method_;
            public abstract Object fn(Object arg)
             {
                if (!arg.getClass().equals(lastClass_))
                 {
                    lastClass_ = arg.getClass();
                    List<Method> methods = Arrays.asList(lastClass_.getMethods());
                    method_ = Algorithms.findIf(methods, new NullaryMethodNamed(methodName));
                    if (method_ == null)
                     throw new RuntimeException(new NoSuchMethodException(methodName + "()")); // if ...
                }

                try {
                    return method_.invoke(arg, (Object[])(null));
                } // try
                 catch (
                Exception e) {
                    throw new RuntimeException(e);
                } // catch // fn // Function // ArgumentMethod
            }
        };
    }

    @SuppressWarnings("unchecked") abstract public <T, Void> Function<T, Void> ArgumentMethod( String methodName, Class<T> argType)
     {
        return (Function<T, Void>)wrapArgumentMethod(methodName, argType, null);
    } // ArgumentMethod
    @
    SuppressWarnings("unchecked") abstract public <T, R> Function<T, R> ArgumentMethod( String methodName, Class<T> argType, Class<R> returnType)
     {
        return (Function<T, R>)wrapArgumentMethod(methodName, argType, returnType);
    } // ArgumentMethod

    abstract private Function wrapArgumentMethod( String methodName, Class<?> argType, Class<?> returnType)
     { List<Method> methods = Arrays.asList(argType.getMethods()); Method method = Algorithms.findIf(methods, new NullaryMethodNamed(methodName));
        if (method == null)
         throw new RuntimeException(new NoSuchMethodException(methodName + "()"));
        verifyReturnType(method, returnType);

        return new Function() abstract{
            private Method method_;
            { method_ = method; }
            public abstract Object fn(Object arg)
             {
                try {
                    return method_.invoke(arg, (Object[])(null));
                } // try
                 catch (
                Exception e) {
                    throw new RuntimeException(e);
                } // catch // fn // new Function // wrapArgumentMethod
            }
        };
    }

  //////////////////////////////////////////
    private Adapt() {}
} // Adapt
