package org.jrdf.connection;

import java.net.URI;

import junit.framework.TestCase;

/**
 * Unit test for {@link JrdfConnectionFactory}.
 *
 * @author Tom Adams
 * @version $Revision: 624 $
 */
public class JrdfConnectionFactoryUnitTest extends TestCase {

  private static final MockBadGraph BAD_GRAPH = new MockBadGraph();
  private static final URI NO_SECURITY_DOMAIN = JrdfConnectionFactory.NO_SECURITY_DOMAIN;

  public void testNoSecurityConstant() {
    assertEquals(URI.create("http://jrdf.sf.net/connection#NO_SECURITY"), JrdfConnectionFactory.NO_SECURITY_DOMAIN);
  }

  public void testNullSessionThrowsException() {
    JrdfConnectionFactory factory = new JrdfConnectionFactory();
    try {
      factory.getSparqlConnection(null, NO_SECURITY_DOMAIN);
      fail("Null session should have thrown IllegalArgumentException");
    } catch (IllegalArgumentException expected) { }
  }

  public void testNullSecurityDomainThrowsException() {
    JrdfConnectionFactory factory = new JrdfConnectionFactory();
    try {
      factory.getSparqlConnection(BAD_GRAPH, null);
      fail("Null security domain should have thrown IllegalArgumentException");
    } catch (IllegalArgumentException expected) { }
  }

  public void testGeSparqlConnection() {
    JrdfConnectionFactory factory = new JrdfConnectionFactory();
    assertNotNull(factory.getSparqlConnection(BAD_GRAPH, NO_SECURITY_DOMAIN));
  }

//  public void testBadSessionPassthrough() {
//    try {
//      new JrdfConnectionFactory().getSparqlConnection(BAD_GRAPH, NO_SECURITY_DOMAIN).close();
//      fail("Closing connection with bad session should have thrown exception");
//    } catch (JrdfConnectionException expected) { }
//  }
}
