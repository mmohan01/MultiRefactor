package org.jrdf.query;

import junit.framework.TestCase;
import org.jrdf.util.ClosableIterator;

/**
 * Unit test for {@link DefaultAnswer}.
 *
 * @author Tom Adams
 * @version $Revision: 1.1 $
 */
public final class DefaultAnswerUnitTest extends TestCase {

  public void testGetAnswer() {
    MockClosableIterator expected = new MockClosableIterator();
    Answer answer = new DefaultAnswer(expected);
    ClosableIterator actual = answer.getClosableIterator();
    assertEquals(expected, actual);
  }
}
