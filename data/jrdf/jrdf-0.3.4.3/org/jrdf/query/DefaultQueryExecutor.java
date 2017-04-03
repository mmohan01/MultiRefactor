package org.jrdf.query;

import java.net.URI;

import org.jrdf.connection.JrdfConnectionException;
import org.jrdf.graph.Graph;
import org.jrdf.util.param.ParameterUtil;

/**
 * Default implementation of a {@link org.jrdf.query.JrdfQueryExecutor}.
 *
 * @author Tom Adams
 * @version $Revision: 624 $
 */
public final class DefaultQueryExecutor implements JrdfQueryExecutor {

  private Graph graph;
  private URI securityDomain;

  /**
   * Creates executor to execute queries.
   *
   * @param graph The graph to communicate with.
   * @param securityDomain The security domain of the graph.
   */
  public DefaultQueryExecutor(Graph graph, URI securityDomain) {
    ParameterUtil.checkNotNull("session", graph);
    ParameterUtil.checkNotNull("securityDomain", securityDomain);
    this.graph = graph;
    this.securityDomain = securityDomain;
  }

  /**
   * Executes a query against a graph.
   *
   * @param query The query to execute.
   * @return The answer to the query, will never be <code>null</code>.
   * @throws org.jrdf.connection.JrdfConnectionException If an error occurs while executing the query.
   */
  public Answer executeQuery(Query query) throws JrdfConnectionException {
    ParameterUtil.checkNotNull("query", query);
//    try {
//      return graph.query(query);
//    } catch (QueryException qe) {
//      throw new JrdfConnectionException("Unable to execute query - " + query.toString(), qe);
//    }
    throw new UnsupportedOperationException("Implement me!");
  }
}
