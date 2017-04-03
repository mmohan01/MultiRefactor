package org.jrdf.sparql;

import junit.framework.TestCase;
import org.jrdf.query.InvalidQuerySyntaxException;
import org.jrdf.util.param.ParameterTestUtil;
import org.jrdf.sparql.SparqlQueryBuilder;

/**
 * Unit test for {@link org.jrdf.sparql.SparqlQueryBuilder}.
 *
 * @author Tom Adams
 * @version $Revision: 624 $
 */
public class SparqlQueryBuilderUnitTest extends TestCase {

  private static final String BUILD_QUERY_METHOD = "buildQuery";
  private static final String NULL_STRING = ParameterTestUtil.NULL_STRING;
  private static final String EMPTY_STRING = ParameterTestUtil.EMPTY_STRING;
  private static final String SINGLE_SPACE = ParameterTestUtil.SINGLE_SPACE;

  public void testBadParams() throws Exception {
    SparqlQueryBuilder builder = new SparqlQueryBuilder();
    checkBadParam(builder, NULL_STRING);
    checkBadParam(builder, EMPTY_STRING);
    checkBadParam(builder, SINGLE_SPACE);
  }

  public void testBuildGoodQuery() throws InvalidQuerySyntaxException {
    // TODO: Is there any more testing we can do on the form of the query?
//    checkGoodQuery("select $s $p $o from <rmi://localhost/server1#> where $s $p $o ;");
  }

  public void testBuildUnsupportedUpdateQuery() throws InvalidQuerySyntaxException {
//    checkUnsupportedQuery(" backup <rmi://localhost/server1> to <file:/tmp/bar.gz>;");
  }

  public void testBuildBadQuery() throws InvalidQuerySyntaxException {
//    checkBadQuery("select $s $p $o from <rmi://localhost/server1#> where $s $p $o ");
  }

//  private void checkGoodQuery(String query) throws InvalidQuerySyntaxException {
//    assertNotNull(new SparqlQueryBuilder().buildQuery(query));
//  }

//  private void checkBadQuery(String badQuery) {
//    try {
//      new SparqlQueryBuilder().buildQuery(badQuery);
//    } catch (InvalidQuerySyntaxException expected) {
//    } catch (IllegalArgumentException expected) {  }
//  }

//  private void checkUnsupportedQuery(String unsupportedQuery) throws InvalidQuerySyntaxException {
//    try {
//      new SparqlQueryBuilder().buildQuery(unsupportedQuery);
//    } catch (IllegalArgumentException expected) { }
//  }

  private void checkBadParam(SparqlQueryBuilder builder, String param) throws Exception {
    ParameterTestUtil.checkBadStringParam(builder, BUILD_QUERY_METHOD, param);
  }
}
