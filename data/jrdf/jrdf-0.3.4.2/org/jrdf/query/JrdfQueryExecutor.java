package org.jrdf.query;

import org.jrdf.query.Query;
import org.jrdf.query.Answer;
import org.jrdf.connection.JrdfConnectionException;

/**
 * Executes queries against a graph.
 *
 * @author Tom Adams
 * @version $Revision: 1.1 $
 */
interface JrdfQueryExecutor {

  /**
   * Executes a query against a graph.
   *
   * @param query The query to execute.
   * @return The answer to the query, will never be <code>null</code>.
   * @throws org.jrdf.connection.JrdfConnectionException If an error occurs while executing the query.
   */
  Answer executeQuery(Query query) throws JrdfConnectionException;
}
