// This file is part of the RECODER library and protected by the LGPL.

package recoder.service;

import java.util.List;

import recoder.abstraction.ClassType;
import recoder.abstraction.DefaultConstructor;
import recoder.abstraction.ImplicitEnumMethod;
import recoder.abstraction.Method;
import recoder.abstraction.ParameterizedType;
import recoder.abstraction.TypeArgument;
import recoder.java.declaration.EnumDeclaration;

/**
 * Handles requests for implicitly defined program model elements. In
 * particular these are {@link recoder.abstraction.NullType},
 * {@link recoder.abstraction.Package},{@link recoder.abstraction.ArrayType},
 * {@link recoder.abstraction.DefaultConstructor}, 
 * {@link recoder.abstraction.ImplicitEnumMethod},
 * {@link recoder.abstraction.IntersectionType},
 * and {@link recoder.abstraction.ParameterizedType}.
 */
public interface ImplicitElementInfo extends ProgramModelInfo {

    /**
     * Returns the default constructor associated with the given class type, or
     * <CODE>null</CODE> if there is none.
     * 
     * @param ct
     *            a class type.
     * @return the default constructor of the given type, or <CODE>null</CODE>.
     */
    DefaultConstructor getDefaultConstructor(ClassType ct);
    
    /**
     * Returns the implicitly defined methods for source-code enum-types. 
     * @param etd
     * @return
     */
    List<ImplicitEnumMethod> getImplicitEnumMethods(EnumDeclaration etd);
    
    ParameterizedType getParameterizedType(ClassType genericType, List<? extends TypeArgument> typeArgs);
	ParameterizedType getParameterizedType(ClassType innerGenericType, 
		    List<? extends TypeArgument> typeArgs,
			ParameterizedType enclosingType);

	Method getJavaLangObjectGetClass();

}