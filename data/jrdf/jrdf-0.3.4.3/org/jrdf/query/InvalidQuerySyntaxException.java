package org.jrdf.query;

/**
 * Indicates that the format of a query does not match the required syntax for its language.
 *
 * @author Tom Adams
 * @version $Revision: 624 $
 */
public class InvalidQuerySyntaxException extends Exception {

  public InvalidQuerySyntaxException(String message) {
    super(message);
  }

  public InvalidQuerySyntaxException(String message, Throwable cause) {
    super(message, cause);
  }
}
