package org.jrdf.connection;

import org.jrdf.query.Answer;
import org.jrdf.query.InvalidQuerySyntaxException;

/**
 * A connection through which to send textual commands.
 *
 * @author Tom Adams
 * @version $Revision: 1.1 $
 */
public interface JrdfConnection {

  /**
   * Executes a query that returns results.
   *
   * @param query The query to execute.
   * @return The answer to the query, will never be <code>null</code>.
   * @throws org.jrdf.query.InvalidQuerySyntaxException If the syntax of the <code>query</code> is incorrect.
   * @throws JrdfConnectionException If an error occurs while executing the query.
   */
  Answer executeQuery(String query) throws InvalidQuerySyntaxException, JrdfConnectionException;

  /**
   * Closes the connection to the graph.
   * <p>
   * Calling this method will close the underlying {@link org.jrdf.graph.Graph}, making it unusable for future use.
   * </p>
   */
  void close();
}
