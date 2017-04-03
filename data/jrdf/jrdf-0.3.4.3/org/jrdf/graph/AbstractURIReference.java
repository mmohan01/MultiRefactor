/*
 * $Header$
 * $Revision: 624 $
 * $Date: 2006-06-24 21:02:12 +1000 (Sat, 24 Jun 2006) $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003, 2004 The JRDF Project.  All rights reserved.
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

package org.jrdf.graph;

// Java 2 standard packages

import java.io.Serializable;
import java.net.URI;

/**
 * A base implementation of an RDF {@link URIReference}.
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @author Andrew Newman
 *
 * @version $Revision: 624 $
 */
public abstract class AbstractURIReference implements URIReference,
    Serializable {

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
  private static final long serialVersionUID = 8034954863132812197L;

  /**
   * The URI of the node.
   */
  private URI uri;

  /**
   * Constructor.
   *
   * Enforces a non-<code>null</code> and absolute <var>newUri</var> parameter.
   *
   * @param newUri the URI to use in creation.
   * @throws IllegalArgumentException if <var>newUri</var> is <code>null</code> or
   *     not absolute
   */
  protected AbstractURIReference(URI newUri) {

    // Validate "newUri" parameter
    if (null == newUri) {
      throw new IllegalArgumentException("Null \"newUri\" parameter");
    }

    if (!newUri.isAbsolute()) {
      throw new IllegalArgumentException("\"" + newUri + "\" is not absolute");
    }

    // Initialize the field
    uri = newUri;
  }

  /**
   * Constructor.
   *
   * Enforces a non-<code>null</code> parameter.  Use only for applications
   * where enforcement of valid URIs is too expensive or not necessary.
   *
   * @param newUri the URI to use in creation.
   * @param validate whether to enforce valid RDF URIs.
   * @throws IllegalArgumentException if <var>newUri</var> is not absolute and
   *   validate is true.
   */
  protected AbstractURIReference(URI newUri, boolean validate) {

    // Validate "newUri" parameter
    if (null == newUri) {
      throw new IllegalArgumentException("Null \"newUri\" parameter");
    }

    if (validate && !newUri.isAbsolute()) {
      throw new IllegalArgumentException("\"" + newUri + "\" is not absolute");
    }

    // Initialize the field
    uri = newUri;
  }

  /**
   * The {@link URI} identifiying this resource.
   *
   * @return the {@link URI} identifying this resource.
   */
  public URI getURI() {
    return uri;
  }

  /**
   * Accept a call from a TypedNodeVisitor.
   *
   * @param visitor the object doing the visiting.
   */
  public void accept(TypedNodeVisitor visitor) {
    visitor.visitURIReference(this);
  }

  public boolean equals(Object obj) {

    // Check equal by reference
    if (this == obj) {
      return true;
    }

    boolean returnValue = false;

    // Check for null and ensure exactly the same class - not subclass.
    if (null != obj) {

      try {
        URIReference tmpURIReference = (URIReference) obj;
        returnValue = getURI().equals(tmpURIReference.getURI());
      }
      catch (ClassCastException cce) {
        // Leave return value to be false.
      }
    }

    return returnValue;
  }

  public int hashCode() {
    return uri.hashCode();
  }

  /**
   * Provide a legible representation of a URI reference. Currently, just the
   * URI of the reference.
   *
   * @return the <var>uri</var> property called toString() on.
   */
  public String toString() {
    return uri.toString();
  }
}
