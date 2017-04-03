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
 * A parse exception that can be thrown by a parser when it encounters
 * an error from which it cannot or doesn't want to recover.
 **/
public class ParseException extends Exception {

  private int _lineNo;

  private int _columnNo;

  private Exception _source;

  /**
   * Creates a new ParseException.
   *
   * @param msg An error message.
   * @param lineNo A line number associated with the message.
   * @param columnNo A column number associated with the message.
   **/
  public ParseException(String msg, int lineNo, int columnNo) {
    super(msg);
    _lineNo = lineNo;
    _columnNo = columnNo;
  }

  /**
   * Creates a new ParseException wrapping another exception.
   *
   * @param msg An error message.
   * @param source The source exception.
   * @param lineNo A line number associated with the message.
   * @param columnNo A column number associated with the message.
   **/
  public ParseException(String msg, Exception source, int lineNo, int columnNo) {
    super(msg);
    _source = source;
    _lineNo = lineNo;
    _columnNo = columnNo;
  }

  /**
   * Creates a new ParseException wrapping another exception. The
   * ParseException will inherit its message from the supplied
   * source exception.
   *
   * @param source The source exception.
   * @param lineNo A line number associated with the message.
   * @param columnNo A column number associated with the message.
   **/
  public ParseException(Exception source, int lineNo, int columnNo) {
    super(source.getMessage());
    _source = source;
    _lineNo = lineNo;
    _columnNo = columnNo;
  }

  public void printStackTrace() {
    printStackTrace(System.err);
  }

  public void printStackTrace(PrintStream ps) {
    super.printStackTrace(ps);

    if (null != _source) {
      ps.println("Source is:");
      _source.printStackTrace(ps);
    }
  }

  public void printStackTrace(PrintWriter pw) {
    super.printStackTrace(pw);

    if (null != _source) {
      pw.println("Source is:");
      _source.printStackTrace(pw);
    }
  }

  /**
   * Gets the line number associated with this parse exception.
   * @return A line number, or -1 if no line number is available
   * or applicable.
   **/
  public int getLineNumber() {
    return _lineNo;
  }

  /**
   * Gets the column number associated with this parse exception.
   * @return A column number, or -1 if no column number is available
   * or applicable.
   **/
  public int getColumnNumber() {
    return _columnNo;
  }
}
