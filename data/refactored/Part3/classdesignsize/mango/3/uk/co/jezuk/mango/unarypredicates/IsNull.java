package uk.co.jezuk.mango.unarypredicates;

import uk.co.jezuk.mango.Predicate;

public enum IsNull implements Predicate<Object> {
    
     INSTANCE;

    public static boolean test(Object x) { return x == null; }
} // IsNull
