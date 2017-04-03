package org.jrdf.connection;

/**
 * Indicates that a connection error occured while connected/ing to a graph.
 *
 * @author Tom Adams
 * @version $Revision: 624 $
 */
public class JrdfConnectionException extends Exception {

  public JrdfConnectionException(String message) {
    super(message);
  }

  public JrdfConnectionException(String message, Throwable cause) {
    super(message, cause);
  }
}
