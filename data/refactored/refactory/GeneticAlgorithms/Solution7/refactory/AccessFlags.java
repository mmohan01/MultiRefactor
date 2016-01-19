package refactory;

//compatible with the byte code standard and with java.lang.reflect.Modifier
public interface AccessFlags {
    // Accessibility
    public static final int PUBLIC = 0x0001;
    public static final int PRIVATE = 0x0002;
    public static final int PROTECTED = 0x0004;
    public static final int PACKAGE = 0x0200; // Shared with INTERFACE

    // Modifiers
    public static final int STATIC = 0x0008;
    public static final int FINAL = 0x0010;
    public static final int ABSTRACT = 0x0400;

    // Class Modifiers
    public static final int INTERFACE = 0x0200;
    public static final int ANNOTATION = 0x2000; // As of Java 5

    // Others
    public static final int SUPER = 0x0020; // Shared with SYNCHRONIZED
    public static final int SYNCHRONIZED = 0x0020;
    public static final int VOLATILE = 0x0040;
    public static final int TRANSIENT = 0x0080;
    public static final int NATIVE = 0x0100;
    public static final int STRICT = 0x0800;
    public static final int BRIDGE = 0x0040; // As of Java 5, shared with VOLATILE
    public static final int VARARGS = 0x0080; // As of Java 5, shared with TRANSIENT
    public static final int SYNTHETIC = 0x1000; // As of Java 5 (optional flag)
    public static final int ENUM = 0x4000; // As of Java 5
}
