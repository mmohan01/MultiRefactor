/*
 * $Header: /cvsroot/jrdf/jrdf/src/org/jrdf/graph/AbstractLiteral.java,v 1.16 2004/08/26 09:32:56 newmana Exp $
 * $Revision: 1.16 $
 * $Date: 2004/08/26 09:32:56 $
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
import java.net.URI;
import java.util.regex.*;

/**
 * A base implementation of an RDF {@link Literal}.
 *
 * @author Andrew Newman
 * @author Simon Raboczi
 *
 * @version $Revision: 1.16 $
 */
public abstract class AbstractLiteral implements Literal {

  /**
   * The lexical form of the literal.
   */
  protected String lexicalForm = null;

  /**
   * The language code of the literal.
   */
  protected String language = null;

  /**
   * Whether the literal is well formed XML.
   */
  protected boolean wellFormedXML = false;

  /**
   * RDF datatype URI, <code>null</code> for untyped literal.
   */
  protected URI datatypeURI = null;

  /**
   * A regular expression to pick out characters needing escape from Unicode to
   * ASCII.
   *
   * This is used by the {@link #escape} method.
   */
  private static final Pattern pattern = Pattern.compile(
      "\\p{InHighSurrogates}\\p{InLowSurrogates}" + // surrogate pairs
      "|" +                                         // ...or...
      "[\\x00-\\x1F\\x22\\\\\\x7F-\\uFFFF]"         // all other escaped chars
      );

  /**
   * The matcher instance used to escape characters from Unicode to ASCII.
   *
   * This is lazily initialized and used by the {@link #escape} method.
   */
  private transient Matcher matcher;

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
   * When no language is specified, this field contains a zero-length
   * {@link String} rather than being <code>null</code>.
   *
   * @return the language code of the literal, never <code>null</code>
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
   *     for an untyped node.
   *
   * @return the URI of the RDF datatype of this resource, or <code>null</code>
   *     for an untyped node.
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
    if (obj != null) {

      try {
        // Set default return value and cast given obj.
        Literal tmpLiteral = (Literal) obj;

        // Ensure that the lexical form is equal character by character.
        if (getLexicalForm().equals(tmpLiteral.getLexicalForm())) {

          // If datatype is null then test language equality.
          if ((getDatatypeURI() == null) && (tmpLiteral.getDatatypeURI() == null)) {

            // If languages are equal by value then its equal.
            if ((String.valueOf(getLanguage()).equals(String.valueOf(tmpLiteral.getLanguage())))) {
              returnValue = true;
            }
          }
          // Data type URIs are equal by their string values.
          else if ((getDatatypeURI() != null) && (tmpLiteral.getDatatypeURI() != null) &&
              (getDatatypeURI().toString().equals(tmpLiteral.getDatatypeURI().
              toString()))) {
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

    if (getDatatypeURI() != null) {
      hashCode = hashCode ^ getDatatypeURI().hashCode();
    }

    if (getLanguage() != null) {
      hashCode = hashCode ^ getLanguage().hashCode();
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
    return escape(getLexicalForm());
  }

  /**
   * Returns the lexical form.
   *
   * @return the lexical form.
   */
  public String toString() {
    return getLexicalForm();
  }

  /**
   * @param string  a string to escape, never <code>null</code>
   * @return a version of the <var>string</var> with N-Triples escapes applied
   */
  private String escape(String string) {
    assert string != null;

    // Obtain a fresh matcher
    if (matcher == null) {
      // Lazily initialize the matcher
      matcher = pattern.matcher(string);
    }
    else {
      // Reuse the existing matcher
      matcher.reset(string);
    }
    assert matcher != null;

    // Try to short-circuit the whole process -- maybe nothing needs escaping?
    if (!matcher.find()) {
      return string;
    }

    // Perform escape character substitutions on each match found by the
    // matcher, accumulating the escaped text into a stringBuffer
    StringBuffer stringBuffer = new StringBuffer();
    do {
      // The escape text with which to replace the current match
      String escapeString;

      // Depending of the character sequence we're escaping, determine an
      // appropriate replacement
      String groupString = matcher.group();
      switch (groupString.length()) {
        case 1: // 16-bit characters requiring escaping
          switch (groupString.charAt(0)) {
            case '\t': // tab
              escapeString = "\\\\t";
            break;
            case '\n': // newline
              escapeString = "\\\\n";
            break;
            case '\r': // carriage return
              escapeString = "\\\\r";
            break;
            case '"':  // quote
              escapeString = "\\\\\\\"";
            break;
            case '\\': // backslash
              escapeString = "\\\\\\\\";
            break;
            default:   // other characters use 4-digit hex escapes
              String hexString =
                  Integer.toHexString(groupString.charAt(0)).toUpperCase();
              escapeString =
                  "\\\\u0000".substring(0, 7 - hexString.length()) + hexString;

              assert escapeString.length() == 7;
              assert escapeString.startsWith("\\\\u");
            break;
          }
        break;

        case 2: // surrogate pairs are represented as 8-digit hex escapes
          assert Character.getType(groupString.charAt(0)) ==
              Character.SURROGATE;
          assert Character.getType(groupString.charAt(1)) ==
              Character.SURROGATE;

          String hexString = Integer.toHexString(
              ( (groupString.charAt(0) & 0x3FF) << 10) + // high surrogate
              (groupString.charAt(1) & 0x3FF) + // low surrogate
              0x10000 // base codepoint U+10000
              ).toUpperCase();
          escapeString =
              "\\\\U00000000".substring(0, 11 - hexString.length()) + hexString;

          assert escapeString.length() == 11;
          assert escapeString.startsWith("\\\\U000");
        break;

        default:
          throw new Error("Escape sequence " + groupString + " has no handler");
      }
      assert escapeString != null;

      // Having determined an appropriate escapeString, add it to the
      // stringBuffer
      matcher.appendReplacement(stringBuffer, escapeString);
    }
    while (matcher.find());

    // Finish off by appending any remaining text that didn't require escaping,
    // and return the assembled buffer
    matcher.appendTail(stringBuffer);
    return stringBuffer.toString();
  }
}
