// This file is part of the RECODER library and protected by the LGPL.

package recoder.abstraction;

import java.util.List;

/**
 * A program model element that may contain class types.
 * 
 * @author AL
 * @author RN
 */
public interface ClassTypeContainer extends ProgramModelElement {

    /**
     * Returns the class types locally defined within this container. Returns
     * member types if this container is a class type, local and
	 * anonymous types if this container is a method.
     * 
     * @return a list of contained class types.
     */
	List<? extends ClassType> getTypes();

    /**
     * Returns the package this element is defined in. Packages have no
     * recursive scope and report themselves.
     * 
     * @return the package of this element.
     */
    Package getPackage();

    /**
     * Returns the enclosing package or class type, or method. A package will
     * report <tt>null</tt>, a method its enclosing class.
     * 
     * @return the container of this element.
     */
    ClassTypeContainer getContainer();

}