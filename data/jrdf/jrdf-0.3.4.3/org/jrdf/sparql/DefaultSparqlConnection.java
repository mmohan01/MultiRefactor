package org.jrdf.sparql;

import java.net.URI;

import org.jrdf.query.DefaultQueryExecutor;
import org.jrdf.query.InvalidQuerySyntaxException;
import org.jrdf.connection.JrdfConnectionException;
import org.jrdf.util.param.ParameterUtil;
import org.jrdf.graph.Graph;
import org.jrdf.query.Answer;
import org.jrdf.query.Query;


/**
 * Default implementation of a {@link org.jrdf.sparql.SparqlConnection}.
 *
 * @author Tom Adams
 * @version $Revision: 624 $
 */
public final class DefaultSparqlConnection implements SparqlConnection {

  // FIXME: Make connections threadsafe.

  private Graph graph;
  private URI securityDomain;

  /**
   * Creates a new SPARQL connection.
   *
   * @param graph The graph to query.
   * @param securityDomain The security domain of the graph.
   */
  public DefaultSparqlConnection(Graph graph, URI securityDomain) {
    ParameterUtil.checkNotNull("graph", graph);
    ParameterUtil.checkNotNull("securityDomain", securityDomain);
    this.graph = graph;
    this.securityDomain = securityDomain;
  }

  /**
   * Executes a query that returns results.
   *
   * @param query The query to execute.
   * @return The answer to the query, will never be <code>null</code>.
   * @throws InvalidQuerySyntaxException If the syntax of the query is incorrect.
   * @throws JrdfConnectionException If an error occurs while executing the query.
   */
  public Answer executeQuery(String query) throws InvalidQuerySyntaxException, JrdfConnectionException {
    ParameterUtil.checkNotEmptyString("query", query);
    Query builtQuery = new SparqlQueryBuilder().buildQuery(query);
    return new DefaultQueryExecutor(graph, securityDomain).executeQuery(builtQuery);
  }

  /**
   * Closes the connection to the graph.
   * <p>
   * Calling this method will close the underlying {@link Graph}, making it unusable for future use.
   * </p>
   */
  public void close() {
    graph.close();
  }

  /**
   * Attempt to close the underlying session in case the client did not.
   * <p>
   * Clients should not rely on this method being called, it is only here as a last minute check to see if any cleanup
   * can be performed. This method is not guarenteed to be executed by the JVM.
   * </p>
   *
   * @throws Throwable An unknown error occurs, possibly in object finalisation.
   */
  protected void finalize() throws Throwable {
    super.finalize();
    try {
      close();
    } catch (Throwable ignored) { }
  }
}
