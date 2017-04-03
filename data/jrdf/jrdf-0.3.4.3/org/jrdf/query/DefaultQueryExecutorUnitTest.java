package org.jrdf.query;

import java.net.URI;

import junit.framework.TestCase;
import org.jrdf.graph.Graph;
import org.jrdf.query.DefaultQueryExecutor;
import org.jrdf.connection.MockBadGraph;
import org.jrdf.connection.JrdfConnectionFactory;

/**
 * Unit test for {@link DefaultQueryExecutor}.
 *
 * @author Tom Adams
 * @version $Revision: 624 $
 */
public class DefaultQueryExecutorUnitTest extends TestCase {

  private static final Graph BAD_GRAPH = new MockBadGraph();
  private static final URI NO_SECURITY_DOMAIN = JrdfConnectionFactory.NO_SECURITY_DOMAIN;

  public void testNullQueryThrowsException() throws Exception {
    try {
      new DefaultQueryExecutor(BAD_GRAPH, NO_SECURITY_DOMAIN).executeQuery(null);
      fail("Null query should have thrown IllegalArgumentException");
    } catch (IllegalArgumentException expected) { }
  }

//  public void testQueryExceptionWrapped() throws InvalidQuerySyntaxException {
//    try {
//      new DefaultQueryExecutor(BAD_GRAPH, NO_SECURITY_DOMAIN).executeQuery(buildQuery());
//      fail("QueryException should jave been wrapped as JrdfConnectionException");
//    } catch (JrdfConnectionException expected) { }
//  }

  public void testNullSessionInConstructor() {
    try {
      new DefaultQueryExecutor(null, NO_SECURITY_DOMAIN);
      fail("Null session should have thrown IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testNullSesurityDomainInConstructor() {
    try {
      new DefaultQueryExecutor(BAD_GRAPH, null);
      fail("Null security domain should have thrown IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
    }
  }
}
