// This file is part of the RECODER library and protected by the LGPL.

package recoder.service;

import java.util.List;

import recoder.Service;
import recoder.abstraction.ArrayType;
import recoder.abstraction.ClassType;
import recoder.abstraction.ClassTypeContainer;
import recoder.abstraction.Constructor;
import recoder.abstraction.Field;
import recoder.abstraction.Member;
import recoder.abstraction.Method;
import recoder.abstraction.Package;
import recoder.abstraction.PrimitiveType;
import recoder.abstraction.ProgramModelElement;
import recoder.abstraction.Type;
import recoder.abstraction.TypeArgument;
import recoder.abstraction.TypeParameter;

/**
 * The program model info computes predicates for program model elements
 * contained in {@link recoder.abstraction}. This service obeys type and
 * visibility rules defined by the language type system, such as subtyping,
 * coersion, overloading resolution, and type narrowing.
 *  
 */
public interface ProgramModelInfo extends Service {
	/**
	 * Removes those ErasedTypes from the list that are also
	 * available in a non-erased manner. <code>getSupertypes()</code>
	 * returns types both erased and non-erased; this method
	 * then removes those ErasedTypes.
	 * 
	 * @param classes
	 * @return
	 */
	public List<ClassType> removeErasedTypesFromList(
			List<? extends ClassType> classes);
	
    /**
     * Returns the type of the given program model element. For types this is
     * the model element itself, for variables it is their defined type and for
     * methods it is their statically defined return type.
     * 
     * @param pme
     *            the program model element to analyze.
     * @return the type of the program element or <tt>null</tt> if the type is
     *         unknown or unavailable.
     */
    Type getType(ProgramModelElement pme);

    /**
     * Returns the package of the given program model element.
     * 
     * @param pme
     *            a program model element.
     * @return the package of that object (may be <CODE>null</CODE>).
     */
    Package getPackage(ProgramModelElement pme);

    /**
     * Returns the class types locally defined within the given container.
     * Returns inner types when the container itself is a class type. Note that
     * there is no guarantee about the order of the types returned.
     * 
     * @param ctc
     *            a class type container.
     * @return a list of contained class types.
     */
    List<? extends ClassType> getTypes(ClassTypeContainer ctc);

    /**
     * Returns all class types that are member types of the given class types,
     * including visible inherited types. The list does not contain all declared
     * classes; instead, classes not visible to the given class (e.g. private or
     * hidden ones) are filtered already.
     * 
     * @param ct
     *            a class type.
     * @return a list of class types that are members of the given type or any
     *         of its supertypes.
     * @see #getAllSupertypes
     */
    List<ClassType> getAllTypes(ClassType ct);

    /**
     * Returns the class type container for the given class type.
     * 
     * @param ct
     *            a class type.
     * @return the container of the given type.
     */
    ClassTypeContainer getClassTypeContainer(ClassType ct);

    /**
     * Returns the list of locally declared supertypes of the given class type.
     * 
     * @param ct
     *            a class type.
     * @return the list of locally defined supertypes of the given type.
     */
    List<ClassType> getSupertypes(ClassType ct);

    /**
     * Returns the supertypes of a primitive type, as specified in 
     * JLS, 3rd edition, §4.10.1
     * @param pt
     * @return
     */
    List<PrimitiveType> getSupertypes(PrimitiveType pt);

    /**
     * Returns the list of all supertypes of the given class type, in
     * topological order, including the class type itself as first element. The
     * order allows to resolve member overloading or overloading.
     * 
     * @param ct
     *            a class type.
     * @return the list of all supertypes of the given type in topological
     *         order.
     */
    List<ClassType> getAllSupertypes(ClassType ct);

    /**
     * Returns the list of all supertypes of the given class type, in
     * topological order, including the class type itself as first element. The
     * order allows to resolve member overloading or overloading.
     * 
     * @param pt
     *            a class type.
     * @return the list of all supertypes of the given type in topological
     *         order.
     */
    List<PrimitiveType> getAllSupertypes(PrimitiveType pt);
    /**
     * Returns all known subtypes of the given class type. Subtypes are linked
     * automatically in a cross reference service configuration; otherwise,
     * non-bytecode subtypes will not be reported.
     * 
     * @param ct
     *            a class type.
     * @return the list of the known direct subtypes of the given class type.
     * 
     * @since 0.72
     */
    List<ClassType> getSubtypes(ClassType ct);

    List<PrimitiveType> getSubtypes(PrimitiveType pt);
    
    /**
     * Returns all known subtypes of the given class type. Subtypes are linked
     * automatically in a cross reference service configuration; otherwise,
     * non-bytecode subtypes will not be reported. Note that this method does
     * not report the base type itself. Known array types are reported to be
     * subtypes of <code>java.lang.Object</code>, where <i>known</i> means
     * that they are used at some point in the program model. 
     * 
     * @param ct
     *            a class type.
     * @return the list of all known subtypes of the given class type.
     * @see #getAllSupertypes
     * 
     * @since 0.72
     */
    List<ClassType> getAllSubtypes(ClassType ct);
    
    List<? extends Type> getAllSubtypes(Type t);
    
    List<PrimitiveType> getAllSubtypes(PrimitiveType pt);
    
    /**
     * Returns the fields locally defined within the given class type. If the
     * type is represented in source code, the returned list matches the
     * syntactic order.
     * 
     * @param ct
     *            a class type.
     * @return the list of field members of the given type.
     */
    List<? extends Field> getFields(ClassType ct);

    /**
     * Returns all visible fields that are defined in the given class type or
     * any of its supertypes. The fields are in topological order with respect
     * to the inheritance hierarchy. The list does not contain all declared
     * fields; instead, fields not visible to the given class (e.g. private or
     * hidden ones) are filtered already.
     * 
     * @param ct
     *            a class type.
     * @return the list of visible field members of the given type and its
     *         supertypes.
     */
    List<Field> getAllFields(ClassType ct);

    /**
     * Returns the constructors locally defined within the given class type. If
     * the type is represented in source code, the returned list matches the
     * syntactic order.
     * 
     * @param ct
     *            a class type.
     * @return the list of constructors of the given type.
     */
    List<? extends Constructor> getConstructors(ClassType ct);

    /**
     * Returns the methods locally defined within the given class type. If the
     * type is represented in source code, the returned list matches the
     * syntactic order.
     * 
     * @param ct
     *            a class type.
     * @return the list of methods of the given type.
     */
    List<Method> getMethods(ClassType ct);

    /**
     * Returns all visible methods that are defined in the given class type or
     * any of its supertypes. The methods are in topological order with respect
     * to the inheritance hierarchy. The list does not contain all declared
     * methods; instead, methods not visible to the given class (e.g. private or
     * hidden ones) are filtered already.
     * 
     * @param ct
     *            a class type.
     * @return the list of visible methods of the given type and its supertypes.
     */
    List<Method> getAllMethods(ClassType ct);

    /**
     * Returns the logical parent class of the given member.
     * 
     * @param m
     *            a member.
     * @return the class type containing the given member.
     */
    ClassType getContainingClassType(Member m);

    /**
     * Returns the signature of the given method or constructor.
     * 
     * @param m
     *            a method (or constructor).
     * @return the signature of the given method.
     */
    List<Type> getSignature(Method m);

    /**
     * Returns the exceptions of the given method or constructor. If the method
     * is represented in source code, the returned list matches the syntactic
     * order.
     * 
     * @param m
     *            a method (or constructor).
     * @return the exceptions of the given method.
     */
    List<ClassType> getExceptions(Method m);

    /**
     * Returns the return type of the given method.
     * 
     * @param m
     *            a method.
     * @return the return type of the given method.
     */
    Type getReturnType(Method m);

    /**
     * Returns the promoted type for binary operations between the the given
     * primitive types. The rules are defined in the Java language
     * specification §5.6.2; in short, the promoted type is is the "bigger" type of the
     * two, but <i>at least</i> <code>int</code>. Note: the latter (at least int) has been fixed in 0.96.
     * 
     * @param a
     *            a primitive type.
     * @param b
     *            a primitive type.
     * @return the promoted type for the pair of types, or null if a type
     *         represents the boolean type, while the other does not.
     */
    PrimitiveType getPromotedType(PrimitiveType a, PrimitiveType b);

    /**
     * Checks if the first given primitive type is widening to the second one.
     * The rules are defined in the Java language specification; in short, a
     * "smaller" type is widening to a "bigger" one.
     * 
     * @param from
     *            a primitive type that might be widening into another type.
     * @param to
     *            a primitive type that is the destination of a widening
     *            operation.
     * @return <CODE>true</CODE> if the first type widens into the second one,
     *         <CODE>false</CODE> otherwise.
     */
    boolean isWidening(PrimitiveType from, PrimitiveType to);

    /**
     * Checks if the first given class type is widening to the second one. This
     * is the case iff the first type is a subtype of the second.
     * 
     * @param from
     *            a class type that might be widening into another type.
     * @param to
     *            a class type that is the destination of a widening operation.
     * @return <CODE>true</CODE> if the first type widens into the second one,
     *         <CODE>false</CODE> otherwise.
     */
    boolean isWidening(ClassType from, ClassType to);

    /**
     * Checks if the first given array type is widening to the second one. The
     * rules are defined in the Java language specification.
     * As of Recoder 0.90, isWidening(ClassType, ClassType) takes over the
     * functionality of this method, as ArrayType is now implemented as
     * subtype of ClassType.
     * 
     * @param from
     *            an array type that might be widening into another type.
     * @param to
     *            an array type that is the destination of a widening operation.
     * @return <CODE>true</CODE> if the first type widens into the second one,
     *         <CODE>false</CODE> otherwise.
     */
    boolean isWidening(ArrayType from, ArrayType to);

    /**
     * Checks if the first given type is widening to the second one. This method
     * makes no assumptions between the corrsponding types and handles
     * combinations of different meta types (e.g. array types to class types
     * such as "Cloneable").
     * This method does not handle autoboxing.
     * 
     * @param from
     *            a type that might be widening into another type.
     * @param to
     *            a type that is the destination of a widening operation.
     * @return <CODE>true</CODE> if the first type widens into the second one,
     *         <CODE>false</CODE> otherwise.
     */
    boolean isWidening(Type from, Type to);

    /**
     * Checks if the first given class type is a subtype of the second.
     * 
     * @param a
     *            class type.
     * @param b
     *            class type.
     * @return <CODE>true</CODE> if the first class type is a subtype of the
     *         second or if both types are equal, <CODE>false</CODE>
     *         otherwise.
     */
    boolean isSubtype(ClassType a, ClassType b);

    /**
     * Checks if the first given class type is a supertype of the second.
     * 
     * @param a
     *            class type.
     * @param b
     *            class type.
     * @return <CODE>true</CODE> if the first class type is a supertype of the
     *         second or if both types are equal, <CODE>false</CODE>
     *         otherwise.
     */
    boolean isSupertype(ClassType a, ClassType b);

    /**
     * Checks visibility of the given member from the point of view of the given
     * class type.
     * 
     * @param m
     *            a member that might be referred to from within the given
     *            classtype.
     * @param t
     *            the class type that might refer to the given member.
     * @return <CODE>true</CODE> if the member is visible for the class type,
     *         <CODE>false</CODE> otherwise.
     */
    boolean isVisibleFor(Member m, ClassType t);

    /**
     * Checks if the first signature is compatible to the second one. A
     * signature is compatible if all types are widing to their counterparts.
     * This method is backwards compatible to non-Java-5 recoder and thus
     * checks neither for autoboxing conversions nor for variable arity methods.
     * Equivalent to <code>isCompatibleSignature(a,b,false,false)</code>  
     * 
     * @param a
     *            a signature.
     * @param b
     *            a signature.
     * @return <CODE>true</CODE> if the first signature is compatible with the
     *         second one, <CODE>false</CODE> otherwise.
     */
    boolean isCompatibleSignature(List<Type> a, List<Type> b);
    
    /**
     * Checks if the first signature is compatible to the second one.
     * @param a	a signature 
     * @param b a signature
     * @param allowAutoboxing wether or not to allow autoboxing 
     * @param bIsVariableArityMethod wether or not b denotes a variable arity method. This
     * 			assumes the last element in b to be an array type! 
     * @return
     */
    boolean isCompatibleSignature(List<Type> a, List<Type> b, boolean allowAutoboxing, boolean bIsVariableArityMethod);

    /**
     * Retains methods with signatures that are not compatible to each other. If
     * used upon methods that apply to a certain method reference, the most
     * specific methods are returned. This handles method overloading, while
     * ambiguous lists are considered semantical errors (list.size() > 1 after
     * return).
     * 
     * @param list
     *            the list of methods to compare.
     * @see #filterApplicableMethods
     */
    void filterMostSpecificMethods(List<Method> list);

    /**
     * Same as filterMostSpecificMethods, but takes auto(un)boxing into account
     * @param list
     * 			 	the list of methods to compare
     * @see #filterMostSpecificMethods
     */
    void filterMostSpecificMethodsPhase2(List<Method> list);
    
    /**
     * Same as filterMostSpecificMethods, but takes auto(un)boxing and variable arity (vararg)
     * methods into account
     * @param list
     * 			 	the list of methods to compare
     * @see #filterMostSpecificMethods
     */
    void filterMostSpecificMethodsPhase3(List<Method> list);
    
    /**
     * Returns the list of most specific methods with the given name that are
     * defined in the given type or in a supertype where they are visible for
     * the given type, and have a signature that is compatible to the given one.
     * The visibilityContext is used to perform visibility checks.
     * 
     * @param ct
     *            the class type to get methods from.
     * @param name
     *            the name of the methods in question.
     * @param signature
     *            the statical type signature of a callee.
     * @param visibilityContext
     * 			  the context which to use for visibility checks. If null, no visibility check is performed.
     * @return the methods that correspond best to the given constraints.
     */
    List<Method> getMethods(ClassType ct, String name, List<Type> signature, ClassType visibilityContext);
    
    List<Method> getMethods(ClassType ct, String name, List<Type> signature, boolean allowBoxing, boolean allowVarArgs, ClassType visibilityContext);
    
    /**
     * Returns the list of most specific methods with the given name that are
     * defined in the given type or in a supertype where they are visible for
     * the given type, and have a signature that is compatible to the given one.
     * The visibilityContext is used to perform visibility checks.
     * 
     * @param ct
     *            the class type to get methods from.
     * @param name
     *            the name of the methods in question.
     * @param signature
     *            the statical type signature of a callee.
     * @param typeArgs
     * 			  the type arguments provided to a generic method invocation. May be <code>null</code>
     * @param visibilityContext
     * 			  the context which to use for visibility checks. If null, no visibility check is performed.
     * @return the methods that correspond best to the given constraints.
     */
    List<Method> getMethods(ClassType ct, String name, List<Type> signature, List<? extends TypeArgument> typeArgs, ClassType visibilityContext);
    
    /**
     * Returns the list of most specific constructors that are defined in the
     * given type and have a signature that is compatible to the given one. 
     * The visibilityContext is used to perform visibility checks.
     * 
     * @param ct
     *            the class type to get constructors from.
     * @param signature
     *            the statical type signature of a callee.
     * @param context
     * 			  the context which to use for visibility checks. If null, no visibility check is performed.
     * @return the constructors that correspond best to the given constraints.
     */
    List<? extends Constructor> getConstructors(ClassType ct, List<Type> signature, ClassType visibilityContext);
    
    /**
     * returns the ClassType this primitive type can be boxed to,
     * as specified in Java Language Specification, 3rd edition, ? 5.1.8 
     * @param unboxedType the unboxed, primitive type
     * @return the ClassType this primitive type can be boxed to
     */
    ClassType getBoxedType(PrimitiveType unboxedType);
    
    /**
     * return the PrimitiveType this ClassType can be unboxed to,
     * or <code>null</code> if this ClassType cannot be unboxed.
     * Follows the Java Language Specification, 3rd edition, ? 5.1.8. 
     * @param boxedType the ClassType to be unboxed
     * @return The PrimitveType this ClassType can be unboxed to, 
     * <code>null</code> if unboxing is not possible.
     */
    PrimitiveType getUnboxedType(ClassType boxedType);
    
    /**
     * @param types
     * @return the common supertype of all the types,
     * possibly an intersection type. 
     * <br>
     * <b>NOTE:</b>This method has been extended in 0.90 to take an arbitrary number of
     * types. The parameter types have been changed to ClassType, as ArrayType now is a 
     * subtype of ClassType, and therefore all valid parameters must be of type ClassType. 
     * 
     * @since 0.85
     */
    ClassType getCommonSupertype(ClassType ... types);
    
    /**
     * Convenience method to check if the type <code>t</code> contains 
     * a type parameter, i.e., it is either of type TypeParameter itself, 
     * or it is of ParameterizedType and contains a TypeParameter in it's 
     * type arguments. 
     * @return true if <code>t</code> contains a type parameter, 
     * <code>false</code> otherwise.
     * @since 0.86
     */
    boolean containsTypeParameter(Type t);
    
    /**
     * Calls <code>containssTypeParameter(Type)</code> on its return type
     * as well as the types in the signature, including exceptions.
     * @param m
     * @return 
     * @since 0.86
     */
    boolean containsTypeParameter(Method m);
    
    /**
     * Calls <code>containssTypeParameter(Type)</code> on the fields type.
     * @param m
     * @return 
     * @since 0.90
     */
    boolean containsTypeParameter(Field f);
    
    /**
     * TODO doc
     * @param t
     * @return
     * @since 0.90
     */
    public Type eraseType(Type t);
    
    /**
     * TODO doc
     * @param t
     * @return
     * @since 0.90
     */
    public <T extends Type> List<T> eraseTypes(List<T> types);

    /**
     * TODO doc
     * @param typeArgument
     * @param typeParameter
     * @return 
     * @since 0.96
     */
	boolean isMatchingTypeArg(TypeArgument typeArgument,
			TypeParameter typeParameter);
    

}