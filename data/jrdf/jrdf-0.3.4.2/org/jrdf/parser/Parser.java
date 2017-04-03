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
package org.jrdf.parser;

import java.io.*;

/**
 * A general interface for RDF parsers.
 **/
public interface Parser {

  /**
   * Constant indicating that datatypes semantics should be ignored.
   **/
  int DT_IGNORE = 10;

  /**
   * Constant indicating that values of datatyped literals should be
   * verified.
   **/
  int DT_VERIFY = 20;

  /**
   * Constant indicating that values of datatyped literals should be
   * normalized to their canonical representation.
   */
  int DT_NORMALIZE = 30;

  /**
   * Sets the StatementHandler that will be notified of statements that
   * are parsed by this parser.
   *
   * @param sh the StatementHandler.
   */
  void setStatementHandler(StatementHandler sh);

  /**
   * Sets the ParseErrorListener that will be notified of any errors
   * that this parser finds during parsing.
   *
   * @param el the error listener.
   */
  void setParseErrorListener(ParseErrorListener el);

  /**
   * Sets the ParseLocationListener that will be notified of the parser's
   * progress during the parse process.
   *
   * @param ll the parser location listener.
   */
  void setParseLocationListener(ParseLocationListener ll);

  /**
   * Sets the NamespaceListener that will be notified of any namespace
   * declarations that the parser finds during parsing.
   *
   * @param nl the namespace listener.
   */
  void setNamespaceListener(NamespaceListener nl);

  /**
   * Sets whether the parser should verify the data it parses (default value
   * is <tt>true</tt>).
   *
   * @param verifyData true to verify the data parsed in.
   */
  void setVerifyData(boolean verifyData);

  /**
   * Set whether the parser should preserve bnode identifiers specified
   * in the source (default is <tt>false</tt>).
   *
   * @param preserveBNodeIds true to presever blank node identifier.
   */
  void setPreserveBNodeIds(boolean preserveBNodeIds);

  /**
   * Sets whether the parser should stop immediately if it finds an error
   * in the data (default value is <tt>true</tt>).
   *
   * @param stopAtFirstError true if an error should stop parsing.
   */
  void setStopAtFirstError(boolean stopAtFirstError);

  /**
   * Sets the datatype handling mode. There are three modes for
   * handling datatyped literals: <em>ignore</em>, <em>verify</em>
   * and <em>normalize</em>. If set to <em>ignore</em>, no special
   * action will be taken to handle datatyped literals. If set to
   * <em>verify</em> (the default value), any literals with known
   * (XML Schema built-in) datatypes are checked to see if their
   * values are valid. If set to <em>normalize</em>, the literal
   * values are not only checked, but also normalized to their
   * canonical representation. The default value is <em>verify</em>.
   *
   * @param datatypeHandling One of the constants
   * <tt>DT_IGNORE</tt>, <tt>DT_VERIFY</tt> or
   * <tt>DT_NORMALIZE</tt>.
   * @see #DT_IGNORE
   * @see #DT_VERIFY
   * @see #DT_NORMALIZE
   */
  void setDatatypeHandling(int datatypeHandling);

  /**
   * Parses the data from the supplied InputStream, using the supplied
   * baseURI to resolve any relative URI references.
   *
   * @param in The InputStream from which to read the data.
   * @param baseURI The URI associated with the data in the InputStream.
   * @exception IOException If an I/O error occurred while data was read
   * from the InputStream.
   * @exception ParseException If the parser has found an unrecoverable
   * parse error.
   * @exception StatementHandlerException If the configured statement handler
   * has encountered an unrecoverable error.
   */
  void parse(InputStream in, String baseURI) throws IOException,
      ParseException, StatementHandlerException;

  /**
   * Parses the data from the supplied Reader, using the supplied
   * baseURI to resolve any relative URI references.
   *
   * @param reader The Reader from which to read the data.
   * @param baseURI The URI associated with the data in the InputStream.
   * @exception IOException If an I/O error occurred while data was read
   * from the InputStream.
   * @exception ParseException If the parser has found an unrecoverable
   * parse error.
   * @exception StatementHandlerException If the configured statement handler
   * has encountered an unrecoverable error.
   */
  void parse(Reader reader, String baseURI) throws IOException,
      ParseException, StatementHandlerException;
}
