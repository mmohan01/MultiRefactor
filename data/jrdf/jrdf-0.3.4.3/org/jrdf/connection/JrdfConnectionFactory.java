package org.jrdf.connection;

import java.net.URI;

import org.jrdf.sparql.DefaultSparqlConnection;
import org.jrdf.sparql.SparqlConnection;
import org.jrdf.util.param.ParameterUtil;
import org.jrdf.graph.Graph;

/**
 * Returns query oriented connections to a graph.
 *
 * @author Tom Adams
 * @version $Revision: 624 $
 */
public final class JrdfConnectionFactory {
  private static final String JRDF_NAMESPACE = "http://jrdf.sf.net/";
  private static final String JRDF_CONNECTION_NAMESPACE = JRDF_NAMESPACE + "connection";

  /**
   * Indicates that no security is enabled on the server being queried.
   */
  public static final URI NO_SECURITY_DOMAIN = URI.create(JRDF_CONNECTION_NAMESPACE + "#NO_SECURITY");

  /**
   * Returns a connection to through which to send SPARQL queries.
   * <p/>
   * Note. A new connection is returned for each call, they are not pooled and are not thread safe. Clients should ensure
   * that they call close on the connection once it is no longer required, the system will not clean the connection up
   * automatically.
   * </p>
   *
   * @param graph The graph to query.
   * @param securityDomain The security domain.
   * @return A connection through which to issue SPARQL queries.
   */
  public SparqlConnection getSparqlConnection(Graph graph, URI securityDomain) {
    ParameterUtil.checkNotNull("graph", graph);
    ParameterUtil.checkNotNull("securityDomain", securityDomain);
    return new DefaultSparqlConnection(graph, securityDomain);
  }
}
