package org.jrdf.query;

/**
 * Builds queries in {@link String} form into {@link org.jrdf.query.Query} objects.
 *
 * @author Tom Adams
 * @version $Revision: 1.1 $
 */
public interface QueryBuilder {

  /**
   * Builds a query in {@link String} form into a {@link org.jrdf.query.Query}.
   *
   * @param query The query in {@link String} form of the query.
   * @return The <code>query</code> in {@link org.jrdf.query.Query} form.
   * @throws org.jrdf.query.InvalidQuerySyntaxException If the syntax of the <code>query</code> is incorrect.
   */
  Query buildQuery(String query) throws InvalidQuerySyntaxException;
}
