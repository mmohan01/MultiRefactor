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

// Java 2 standard
import org.jrdf.util.EscapeUtil;

import java.io.Serializable;
import java.net.URI;

/**
 * A base implementation of an RDF {@link Literal}.
 *
 * @author Andrew Newman
 * @author Simon Raboczi
 *
 * @version $Revision: 624 $
 */
public abstract class AbstractLiteral implements Literal, Serializable {

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
  private static final long serialVersionUID = 2589574733270452078L;

  /**
   * The lexical form of the literal.
   */
  private String lexicalForm;

  /**
   * The language code of the literal.
   */
  private String language;

  /**
   * Whether the literal is well formed XML.
   */
  private boolean wellFormedXML;

  /**
   * RDF datatype URI, <code>null</code> for untyped literal.
   */
  private URI datatypeURI;

  /**
   * Construct a plain literal.
   *
   * @param newLexicalForm  the text part of the literal
   * @throws IllegalArgumentException if <var>newLexicalForm</var> is <code>null</code>
   */
  protected AbstractLiteral(String newLexicalForm) {

    // Validate "newLexicalForm" parameter
    if (null == newLexicalForm) {
      throw new IllegalArgumentException("Null \"newLexicalForm\" parameter");
    }

    // Initialize fields
    lexicalForm = newLexicalForm;
    language = "";
    datatypeURI = null;
  }

  /**
   * Construct a literal with language.
   *
   * @param newLexicalForm  the text part of the literal
   * @param newLanguage  the language code, possibly the empty string but not
   *    <code>null</code>
   * @throws IllegalArgumentException if <var>lexicalForm</var> or
   *    <var>lang</var> are <code>null</code>
   */
  protected AbstractLiteral(String newLexicalForm, String newLanguage) {

    // Validate "lexicalForm" parameter
    if (null == newLexicalForm) {
      throw new IllegalArgumentException("Null \"lexicalForm\" parameter");
    }

    // Validate "language" parameter
    if (null == newLanguage) {
      throw new IllegalArgumentException("Null \"language\" parameter");
    }

    // Initialize fields
    lexicalForm = newLexicalForm;
    language = newLanguage;
    datatypeURI = null;
  }

  /**
   * Construct a datatyped literal.
   *
   * @param newLexicalForm  the text part of the literal
   * @param newDatatypeURI  the URI for a datatyped literal
   * @throws IllegalArgumentException if <var>lexicalForm</var> or
   *     <var>datatype</var> are <code>null</code>
   */
  protected AbstractLiteral(String newLexicalForm, URI newDatatypeURI) {

    // Validate "lexicalForm" parameter
    if (null == newLexicalForm) {
      throw new IllegalArgumentException("Null \"lexicalForm\" parameter");
    }

    // Validate "datatype" parameter
    if (null == newDatatypeURI) {
      throw new IllegalArgumentException("Null \"datatype\" parameter");
    }

    // Initialize fields
    lexicalForm = newLexicalForm;
    language = null;
    datatypeURI = newDatatypeURI;
  }

  /**
   * Obtain the text of this literal.
   *
   * @return the text of the literal, never <code>null</code>
   */
  public String getLexicalForm() {
    return lexicalForm;
  }

  /**
   * Returns the language code of the literal.
   *
   * When no language is specified for a plain literal, this field contains a
   * zero-length {@link String}.  Otherwise, this will be <code>null</code>.
   *
   * @return the language code of the literal or <code>null</code> in the case
   *   of a datatyped literal.
   */
  public String getLanguage() {
    return language;
  }

  /**
   * Whether the literal is well formed XML.
   *
   * @return whether the literal is wll formed XML.
   */
  public boolean isWellFormedXML() {
    return wellFormedXML;
  }

  /**
   * Returns the URI of the RDF datatype of this resource, or <code>null</code>
   *     for a plain literal.
   *
   * @return the URI of the RDF datatype of this resource, or <code>null</code>
   *     for a plain literal.
   */
  public URI getDatatypeURI() {
    return datatypeURI;
  }

  /**
   * Accept a call from a TypedNodeVisitor.
   *
   * @param visitor the object doing the visiting.
   */
  public void accept(TypedNodeVisitor visitor) {
    visitor.visitLiteral(this);
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
        // Set default return value and cast given obj.
        Literal tmpLiteral = (Literal) obj;

        // Ensure that the lexical form is equal character by character.
        if (getLexicalForm().equals(tmpLiteral.getLexicalForm())) {

          // If datatype is null then test language equality.
          if (null == getDatatypeURI() && null == tmpLiteral.getDatatypeURI()) {

            // If languages are equal by value then its equal.
            if (getLanguage().equals(tmpLiteral.getLanguage())) {
              returnValue = true;
            }
          }
          // Data type URIs are equal by their string values.
          else if (null != getDatatypeURI() && null !=
              tmpLiteral.getDatatypeURI() &&
              getDatatypeURI().toString().equals(tmpLiteral.getDatatypeURI().
              toString())) {
            returnValue = true;
          }
        }
      }
      catch (ClassCastException cce) {
        // Leave return value to be false.
      }
    }
    return returnValue;
  }

  public int hashCode() {
    int hashCode = getLexicalForm().hashCode();

    if (null != getDatatypeURI()) {
      hashCode ^= getDatatypeURI().hashCode();
    }

    if (null != getLanguage()) {
      hashCode ^= getLanguage().hashCode();
    }

    return hashCode;
  }

  /**
   * Provide a legible representation of a literal, following the N-Triples
   * format defined in
   * <a href="http://www.w3.org/TR/2004/REC-rdf-testcases-20040210/#ntrip_strings">&sect;3.2</a>
   * of the <a href="http://www.w3.org/">
   * <acronym title="World Wide Web Consortium">W3C</acronym></a>'s
   * <a href="http://www.w3.org/TR/2004/REC-rdf-testcases-20040210">RDF Test
   * Cases</a> Recommendation.
   *
   * Well-formed Unicode surrogate pairs in the lexical form are escaped as a
   * single 8-digit hexadecimal <code>\U</code> escape sequence rather than a
   * pair of 4-digit <code>&x5C;u</code> sequences representing the surrogates.
   *
   * @return this instance in N-Triples format
   */
  public String getEscapedForm() {
    String escaped = EscapeUtil.escape(getLexicalForm());
    return '\"' + escaped + '\"' + appendType();
  }

  /**
   * Returns the lexical form.
   *
   * @return the lexical form.
   */
  public String toString() {
    return '\"' + getEscapedLexicalForm() + '\"' + appendType();
  }

  public String getEscapedLexicalForm() {
    return getLexicalForm().replaceAll("\\\\", "\\\\\\\\").replaceAll("\\\"",
        "\\\\\\\"");
  }

  /**
   * Appends the datatype URI or language code of a literal.
   *
   * @return String the datatype URI in the form ^^<->, or language code @- or
   *   an empty string.
   */
  private String appendType() {
    String appendString = "";

    if (null != getDatatypeURI()) {
      appendString = "^^<" + getDatatypeURI() + '>';
    }
    else if (!"".equals(language)) {
      appendString = '@' + language;
    }

    return appendString;
  }
}
