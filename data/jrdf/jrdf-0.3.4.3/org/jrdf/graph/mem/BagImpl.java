/*
 * $Header$
 * $Revision: 624 $
 * $Date: 2006-06-24 21:02:12 +1000 (Sat, 24 Jun 2006) $
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
import org.jrdf.graph.ObjectNode;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * An implementation of {@link Bag}.
 *
 * @author Andrew Newman
 *
 * @version $Revision: 624 $
 */
public class BagImpl extends AbstractUnorderedContainer implements Bag {

  public boolean containsAll(java.util.Collection c) {
    if (!(c instanceof Bag)) {
      throw new IllegalArgumentException("Can only add bags to other bags");
    }

    return elements.values().containsAll(c);
  }

  public boolean addAll(java.util.Collection c) throws IllegalArgumentException {
    if (!(c instanceof Bag)) {
      throw new IllegalArgumentException("Can only add bags to other bags");
    }

    Bag bag = (Bag) c;

    // Iterate through the bag adding object nodes
    Iterator iter = bag.iterator();
    boolean modified = iter.hasNext();
    while (iter.hasNext()) {
      ObjectNode obj = (ObjectNode) iter.next();
      elements.put(new Long(key++), obj);
    }

    return modified;
  }

  public boolean removeAll(java.util.Collection c) throws IllegalArgumentException {
    if (!(c instanceof Bag)) {
      throw new IllegalArgumentException("Can only add bags to other bags");
    }

    Bag bag = (Bag) c;

    // Iterate through the bag adding object nodes
    Iterator iter = bag.iterator();
    boolean modified = iter.hasNext();
    while (iter.hasNext()) {
      remove(iter.next());
    }

    return modified;
  }


  public boolean retainAll(java.util.Collection c) throws IllegalArgumentException {
    if (!(c instanceof Bag)) {
      throw new IllegalArgumentException("Can only add bags to other bags");
    }

    boolean modified = false;

    // Iterate through this bag removing elements that are not in the given
    // bag c.
    Iterator iter = iterator();
    while (iter.hasNext()) {

      ObjectNode obj = (ObjectNode) iter.next();
      if (!c.contains(obj)) {
        modified = true;
        remove(obj);
      }
    }

    return modified;
  }

  public int hashCode() {
    return super.hashCode();
  }

  public boolean equals(Object obj) {

    // Check equal by reference
    if (this == obj) {
      return true;
    }

    // Check for null and ensure exactly the same class - not subclass.
    if (null == obj ||
        getClass() != obj.getClass()) {
      return false;
    }

    Bag bag = (Bag) obj;

    boolean returnValue = false;
    if (size() == bag.size()) {

      List myValues = Arrays.asList(toArray());
      List altValues = Arrays.asList(bag.toArray());
      returnValue = myValues.equals(altValues);
    }
    return returnValue;
  }
}
