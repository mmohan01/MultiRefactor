/*
 * $Header: /cvsroot/jrdf/jrdf/src/org/jrdf/graph/mem/Attic/CollectionImpl.java,v 1.5 2005/06/21 20:53:02 newmana Exp $
 * $Revision: 1.5 $
 * $Date: 2005/06/21 20:53:02 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2004 The JRDF Project.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        the JRDF Project (http://jrdf.sf.net/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The JRDF Project" and "JRDF" must not be used to endorse
 *    or promote products derived from this software without prior written
 *    permission. For written permission, please contact
 *    newmana@users.sourceforge.net.
 *
 * 5. Products derived from this software may not be called "JRDF"
 *    nor may "JRDF" appear in their names without prior written
 *    permission of the JRDF Project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the JRDF Project.  For more
 * information on JRDF, please see <http://jrdf.sourceforge.net/>.
 */

package org.jrdf.graph.mem;

// Java 2 standard packages
import org.jrdf.graph.Bag;
import org.jrdf.graph.Collection;
import org.jrdf.graph.ObjectNode;

import java.util.LinkedList;

/**
 * A Collection all the statements of a particular group.
 *
 * @author Andrew Newman
 *
 * @version $Revision: 1.5 $
 */
public class CollectionImpl extends LinkedList implements Collection {

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
  private static final long serialVersionUID = -420874713471604278L;

  /**
   * ${@inheritDoc}
   */
  public CollectionImpl() {
  }

  public void add(int index, Object element) throws IllegalArgumentException {
    if (!(element instanceof ObjectNode)) {
      throw new IllegalArgumentException("Can only add Object nodes");
    }

    super.add(index, element);
  }

  public boolean add(Object o) throws IllegalArgumentException {
    if (!(o instanceof ObjectNode)) {
      throw new IllegalArgumentException("Can only add Object nodes");
    }

    return super.add(o);
  }

  public boolean addAll(java.util.Collection c)
      throws IllegalArgumentException {
    if (!(c instanceof Collection)) {
      throw new IllegalArgumentException("Can only add collections to other " +
          "collections");
    }

    return super.addAll(c);
  }

  /**
   * @throws IllegalArgumentException if the given object is not the correct
   *   type, Collection.
   */
  public boolean addAll(int index, java.util.Collection c)
      throws IllegalArgumentException {
    if (!(c instanceof Collection)) {
      throw new IllegalArgumentException("Can only add sequences to other " +
          "sequences");
    }

    return super.addAll(index, c);
  }

  /**
   * @throws IllegalArgumentException if the given object is not the correct
   *   type, ObjectNode.
   */
  public void addFirst(Object o) {
    if (!(o instanceof ObjectNode)) {
      throw new IllegalArgumentException("Can only add object nodes");
    }

    super.addFirst(o);
  }

  /**
   * @throws IllegalArgumentException if the given object is not the correct
   *   type, ObjectNode.
   */
  public void addLast(Object o) {
    if (!(o instanceof ObjectNode)) {
      throw new IllegalArgumentException("Can only add object nodes");
    }

    super.addLast(o);
  }

  /**
   * @throws IllegalArgumentException if the given object is not the correct
   *   type, ObjectNode.
   */
  public boolean contains(Object o) {
    if (!(o instanceof ObjectNode)) {
      throw new IllegalArgumentException("Can only add object nodes");
    }

    return super.contains(o);
  }

  /**
   * @throws IllegalArgumentException if the given object is not the correct
   *   type, ObjectNode.
   */
  public int indexOf(Object o) throws IllegalArgumentException {
    if (!(o instanceof ObjectNode)) {
      throw new IllegalArgumentException("Can only get Object nodes");
    }

    return super.indexOf(o);
  }

  /**
   * @throws IllegalArgumentException if the given object is not the correct
   *   type, ObjectNode.
   */
  public int lastIndexOf(Object o) throws IllegalArgumentException {
    if (!(o instanceof ObjectNode)) {
      throw new IllegalArgumentException("Can only get Object nodes");
    }

    return super.lastIndexOf(o);
  }

  /**
   * @throws IllegalArgumentException if the given object is not the correct
   *   type.
   */
  public boolean remove(Object o) throws IllegalArgumentException {
    if (!(o instanceof ObjectNode)) {
      throw new IllegalArgumentException("Can only add Object nodes");
    }

    return super.remove(o);
  }

  /**
   * @throws IllegalArgumentException if the given object is not the correct
   *   type, Collection.
   */
  public boolean containsAll(java.util.Collection c) {
    if (!(c instanceof Collection)) {
      throw new IllegalArgumentException("Can only add sequences to other " +
          "sequences");
    }

    return super.containsAll(c);
  }

  /**
   * @throws IllegalArgumentException if the given object is not the correct
   *   type, Collection.
   */
  public boolean removeAll(java.util.Collection c)
      throws IllegalArgumentException {
    if (!(c instanceof Bag)) {
      throw new IllegalArgumentException("Can only add bags to other bags");
    }

    return super.removeAll(c);
  }


  /**
   * @throws IllegalArgumentException if the given object is not the correct
   *   type, Collection.
   */
  public boolean retainAll(java.util.Collection c)
      throws IllegalArgumentException {
    if (!(c instanceof Bag)) {
      throw new IllegalArgumentException("Can only add bags to other bags");
    }

    return super.retainAll(c);
  }

  /**
   * @throws IllegalArgumentException if the given object is not the correct
   *   type, ObjectNode.
   */
  public Object set(int index, Object element) throws IllegalArgumentException {
    if (!(element instanceof ObjectNode)) {
      throw new IllegalArgumentException("Can only get Object nodes");
    }

    return super.set(index, element);
  }
}

