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

/**
 * An XML attribute.
 **/
class Att {

  private String _namespace;
  private String _localName;
  private String _qName;
  private String _value;

  Att(String namespace, String localName, String qName, String value) {
    _namespace = namespace;
    _localName = localName;
    _qName = qName;
    _value = value;
  }

  public String getNamespace() {
    return _namespace;
  }

  public String getLocalName() {
    return _localName;
  }

  public String getURI() {
    return _namespace + _localName;
  }

  public String getQName() {
    return _qName;
  }

  public String getValue() {
    return _value;
  }
}
