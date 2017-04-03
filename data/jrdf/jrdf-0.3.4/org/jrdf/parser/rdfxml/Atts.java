/*  Sesame - Storage and Querying architecture for RDF and RDF Schema
 *  Copyright (C) 2001-2004 Aduna
 *  Copyright (C) 2004-2005 Andrew Newman - Conversion to JRDF
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.jrdf.parser.rdfxml;

import java.util.*;

/**
 * A collection of XML attributes.
 **/
class Atts {

  /**
   * List containing Att objects.
   **/
  private List _attributes;

  /**
   * Creates a new <tt>Atts</tt> object.
   **/
  Atts() {
    this(4);
  }

  /**
   * Creates a new <tt>Atts</tt> object.
   *
   * @param size The initial size of the array for storing attributes.
   **/
  Atts(int size) {
    _attributes = new ArrayList(size);
  }

  /**
   * Adds an attribute.
   **/
  public void addAtt(Att att) {
    _attributes.add(att);
  }

  /**
   * Get an iterator on the attributes.
   * @return an Iterator over Att objects.
   **/
  public Iterator iterator() {
    return _attributes.iterator();
  }

  /**
   * Gets the attribute with the specified QName.
   *
   * @param qName The QName of an attribute.
   * @return The attribute with the specified QName, or
   * <tt>null</tt> if no such attribute could be found.
   **/
  public Att getAtt(String qName) {
    for (int i = 0; i < _attributes.size(); i++) {
      Att att = (Att) _attributes.get(i);

      if (att.getQName().equals(qName)) {
        return att;
      }
    }

    return null;
  }

  /**
   * Gets the attribute with the specified namespace and local name.
   *
   * @param namespace The namespace of an attribute.
   * @param localName The local name of an attribute.
   * @return The attribute with the specified namespace and local
   * name, or <tt>null</tt> if no such attribute could be found.
   **/
  public Att getAtt(String namespace, String localName) {
    for (int i = 0; i < _attributes.size(); i++) {
      Att att = (Att) _attributes.get(i);

      if (att.getLocalName().equals(localName) &&
          att.getNamespace().equals(namespace)) {
        return att;
      }
    }

    return null;
  }

  /**
   * Removes the attribute with the specified QName and returns it.
   *
   * @param qName The QName of an attribute.
   * @return The removed attribute, or <tt>null</tt> if no attribute
   * with the specified QName could be found.
   **/
  public Att removeAtt(String qName) {
    for (int i = 0; i < _attributes.size(); i++) {
      Att att = (Att) _attributes.get(i);

      if (att.getQName().equals(qName)) {
        _attributes.remove(i);
        return att;
      }
    }

    return null;
  }

  /**
   * Removes the attribute with the specified namespace and local
   * name and returns it.
   *
   * @param namespace The namespace of an attribute.
   * @param localName The local name of an attribute.
   * @return The removed attribute, or <tt>null</tt> if no attribute
   * with the specified namespace and local name could be found.
   **/
  public Att removeAtt(String namespace, String localName) {
    for (int i = 0; i < _attributes.size(); i++) {
      Att att = (Att) _attributes.get(i);

      if (att.getLocalName().equals(localName) &&
          att.getNamespace().equals(namespace)) {
        _attributes.remove(i);
        return att;
      }
    }

    return null;
  }

  /**
   * Returns the number of attributes contained in this object.
   **/
  public int size() {
    return _attributes.size();
  }

  /**
   * Produces a String-representation of this object.
   **/
  public String toString() {
    StringBuffer result = new StringBuffer();
    result.append("Atts[");
    for (int i = 0; i < _attributes.size(); i++) {
      Att att = (Att) _attributes.get(i);
      result.append(att.getQName());
      result.append("=");
      result.append(att.getValue());
      result.append("; ");
    }
    result.append("]");
    return result.toString();
  }
}
