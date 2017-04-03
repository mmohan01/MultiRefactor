package org.jrdf.sparql;

import java.net.URI;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jrdf.query.InvalidQuerySyntaxException;
import org.jrdf.connection.JrdfConnectionException;
import org.jrdf.connection.JrdfConnectionFactory;
import org.jrdf.connection.MockBadGraph;
import org.jrdf.util.param.ParameterTestUtil;
import org.jrdf.query.Answer;
import org.jrdf.sparql.DefaultSparqlConnection;

/**
 * Unit test for {@link DefaultSparqlConnection}.
 *
 * @author Tom Adams
 * @version $Revision: 1.2 $
 */
public class DefaultSparqlConnectionUnitTest extends TestCase {

  private static final MockBadGraph BAD_GRAPH = new MockBadGraph();
  private static final URI NO_SECURITY_DOMAIN = JrdfConnectionFactory.NO_SECURITY_DOMAIN;
  private static final String EXECUTE_UPDATE_METHOD = "executeUpdate";
  private static final String EXECUTE_QUERY_METHOD = "executeQuery";
  private static final String NULL = ParameterTestUtil.NULL_STRING;
  private static final String EMPTY_STRING = ParameterTestUtil.EMPTY_STRING;
  private static final String SINGLE_SPACE = ParameterTestUtil.SINGLE_SPACE;

  public DefaultSparqlConnectionUnitTest(String name) {
    super(name);
  }

  /**
   * Don't run any of the tests for now.
   */
  public static Test suite() {
    return new TestSuite();
  }

  public void testNullSessionInConstructor() {
    try {
      new DefaultSparqlConnection(null, NO_SECURITY_DOMAIN);
      fail("Null session should have thrown IllegalArgumentException");
    } catch (IllegalArgumentException expected) { }
  }

  public void testNullSesurityDomainInConstructor() {
    try {
      new DefaultSparqlConnection(BAD_GRAPH, null);
      fail("Null security domain should have thrown IllegalArgumentException");
    } catch (IllegalArgumentException expected) { }
  }

  public void testClose() {
    new DefaultSparqlConnection(BAD_GRAPH, NO_SECURITY_DOMAIN).close();
  }

  public void testExecuteSimpleBadQuery() throws Exception {
    DefaultSparqlConnection connection = new DefaultSparqlConnection(BAD_GRAPH, NO_SECURITY_DOMAIN);
    checkBadParam(connection, EXECUTE_UPDATE_METHOD, NULL);
    checkBadParam(connection, EXECUTE_UPDATE_METHOD, EMPTY_STRING);
    checkBadParam(connection, EXECUTE_UPDATE_METHOD, SINGLE_SPACE);
  }

  public void testExecuteSimpleBadUpdate() throws Exception {
    DefaultSparqlConnection connection = new DefaultSparqlConnection(BAD_GRAPH, NO_SECURITY_DOMAIN);
    checkBadParam(connection, EXECUTE_QUERY_METHOD, NULL);
    checkBadParam(connection, EXECUTE_QUERY_METHOD, EMPTY_STRING);
    checkBadParam(connection, EXECUTE_QUERY_METHOD, SINGLE_SPACE);
  }

  public void testExecuteQuery() throws JrdfConnectionException, InvalidQuerySyntaxException {
    DefaultSparqlConnection connection = new DefaultSparqlConnection(BAD_GRAPH, NO_SECURITY_DOMAIN);
    String query = "select $s $p $o from <rmi://localhost/server1#> where $s $p $o ;";
    Answer answer = connection.executeQuery(query);
    assertNotNull(answer);
  }

  private void checkBadParam(DefaultSparqlConnection connection, String method, String param) throws Exception {
    ParameterTestUtil.checkBadStringParam(connection, method, param);
  }
}
